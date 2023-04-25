package userInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import inventoryService.api.InventoryService;
import order.OrderCoordinator;
import order.Result;
import userService.MyLogger;
import userService.Request;
import userService.SerializedFuture;
import userService.UserServer;
import userService.UserServerInterface;

public class UserInterface {
	
	String username;
	Registry registry;
	MyLogger logger;
	ShoppingCart shoppingCart;
	
	// References to a replica of each of the three servers
	UserServerInterface userServer;
	OrderCoordinator orderServer;		
	InventoryService inventoryServer;   //TODO
	
	// Constructor
	public UserInterface() {
		try {
			this.logger = new MyLogger("userInterfaceLog.csv");

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Cannot create log file. Exiting...");
			System.exit(-1);
		}
	}
	
	
	// Get references to one replica of the userServer, inventoryServer, and orderServer
	private void getServerRefs() {
		try {
			// Get rmi registry
			registry = LocateRegistry.getRegistry(8013);

			// Get references to the servers of each microservice
			userServer = (UserServerInterface) registry.lookup("userServer0");
			orderServer = (OrderCoordinator) registry.lookup("order-coordinator"); 
			inventoryServer = (InventoryService) registry.lookup("inventoryServer");
			this.shoppingCart = new ShoppingCart(inventoryServer);

		} catch (RemoteException e) {
			e.printStackTrace();
			logger.log(true, Level.SEVERE, "Unable to access rmi registry. Exiting...");
			System.exit(-1);
		} catch (NotBoundException e) {
			e.printStackTrace();
			logger.log(true, Level.SEVERE, "Unable to find servers in rmi registry. Exiting...");
			System.exit(-1);
		}
	}
	
	
	// Log the request & response from/to the server
	public void handleLog(Request request) {
		String reqType = request.getType();
		if (reqType.toLowerCase().equals("signup")) {
			// log the request
			logger.log(true, Level.INFO,
					"Requested " + reqType.toUpperCase() + " for email=\"" + request.getEmail() + "\"");
			// log the response
			logger.log(true, Level.INFO, request.getMsg());
		} else if (reqType.toLowerCase().equals("login")) {
			// log the request
			logger.log(true, Level.INFO, "Requested " + reqType.toUpperCase() + " for email=\"" + request.getEmail()
					+ "\"");
			// log the response
			logger.log(true, Level.INFO, "Server Response: \"" + request.getMsg() + "\"");
		}
	}
	
	
	// Make request to "this.server", if the server isn't available (raises a RemoteException), 
	// find another server and try again.  Try 10 times. If still no success -> Exit.
	protected synchronized Request makeRequest(String requestType, String email, String password) {
		int i = 0;
		while (true) {
			try {

				if (i>9) {
					throw new Exception("Failed to make " + requestType.toUpperCase() + " request after 10 attempts. Exiting...");
				}
				

				if (requestType.toLowerCase().equals("signup")) {
					// get the SerializedFuture object
					SerializedFuture<Request> future = this.userServer.signup(email, password);
					// Block until the Future object returns with a Request object
					Request response = future.get(2, TimeUnit.SECONDS);
					return response;
				} 
				else if (requestType.toLowerCase().equals("login")) {
					// get the SerializedFuture object
					Request response = this.userServer.login(email, password);
					return response;
				} else {
					logger.log(true, Level.SEVERE, "Invalid request! Imploding!!!");
					System.exit(-1);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(true, Level.SEVERE, e.getMessage());
				System.exit(-1);
			}
			i++;
		}
	}
	
	// Helper function to determine if a string is numeric
	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	
	// Handle user login and/or signup
	protected void handleAuthentication() throws RemoteException {
		Scanner scanner = new Scanner(System.in);
		List<String> validCommands = new ArrayList<String>(Arrays.asList("login", "signup"));
		Boolean isValidCommand;
		// Listen for keyboard input
		while (true) {
			// Print instructions
			System.out.println(
					">>> Enter a command: \"signup [username], [password]\", or \"login [username] [password]\", or \"EXIT:\"");

			// Get Input from user
			String userInput = scanner.nextLine();


			// Split user input into different words
			List<String> wordList = new ArrayList<String>(Arrays.asList(userInput.trim().split(" "))); 
			
																										
			String firstWord = wordList.get(0);

			// Check for EXIT
			if (firstWord.trim().toLowerCase().equals("exit") || userInput.trim().toLowerCase().equals("exit")) {
				scanner.close();
				this.logger.log(true, Level.INFO, "** Exiting!! **");
				logger.close();
				System.exit(0);
				break; // break the loop to exit program
			}
			
			// Minimum response is 4 chars ("EXIT")
			if (userInput.length() < 6) {
				logger.log(true, Level.INFO, "Invalid command: \"" + userInput + "\"");
				continue;
			}
			
			// Check for 3 or more words
			if (wordList.size() < 3) {
				logger.log(true, Level.INFO, "Invalid input");
				continue;
			}

			// Check if the command is valid
			if (!validCommands.contains(firstWord.toLowerCase())) {
				logger.log(true, Level.INFO, "Invalid command: \"" + firstWord + "\"");
				continue;
			}


			// Respond to input
			String password;
			Request response = null;

			// Switch statement on the first word in the wordlist
			switch (wordList.get(0).toLowerCase()) {
			case "signup":
				// make sure user entered both username and password
				if (userInput.trim().split(" ").length < 3) {
					logger.log(true, Level.INFO,
							"Invalid. Username and password required for SIGNUP request -> \"" + userInput + "\"");
					break;
				}

				// Make request
				wordList.remove(0); // remove first word ("signup")
				password = wordList.get(wordList.size() - 1);
				wordList.remove(wordList.size() - 1); // remove last word (the password)
				username = String.join(" ", wordList);
				response = makeRequest("signup", username, password);
				handleLog(response);
				break;
			case "login":
				// make sure user entered both username and password
				if (userInput.trim().split(" ").length < 3) {
					logger.log(true, Level.INFO,
							"Invalid. username and password required for LOGIN request -> \"" + userInput + "\"");
					break;
				}

				// Make request
				wordList.remove(0); // remove first word ("login")
				password = wordList.get(wordList.size() - 1);
				wordList.remove(wordList.size() - 1); // remove last word (the value)
				username = String.join(" ", wordList);
				response = makeRequest("login", username, password);
				handleLog(response);
				break;
			}
			
			if (response.isSuccessful()) {
				break;
			}
		}
	}
	
	// handle the shopping experience once a user is logged in
	private void handleShopping() throws RemoteException {

		List<String> validCommands = new ArrayList<String>(Arrays.asList("add", "update", "remove", "empty-cart", "clear-cart", "checkout", "order-history" ));

		// Print the options available to choose from
		Scanner scanner = new Scanner(System.in);
		
		
		Boolean isValidCommand;
		while (true) {
			// Print instructions
			System.out.println("** The following items are available for sale: **");
			System.out.println("id	Name	Description		Rating	Stock	Price ($)");
			System.out.println("1	Apple	xxxxxxxxxxxxx		4.2	10	1.99");
			System.out.println("2	Orange	xxxxxxxxxxxxx		4.8	10	2.99");
			System.out.println("3	Banana	xxxxxxxxxxxxx		4.1	10	3.99");
			System.out.println("4	Peach	xxxxxxxxxxxxx		3.5	10	4.99");
			System.out.println("5	Pear	xxxxxxxxxxxxx		5.0	10	5.99");
			System.out.println("\n** Available Commands: **");
			System.out.println(" - \"add [id] [quantity]\"  --> Add a product to your shopping cart");
			System.out.println(" - \"update [id] [quantity]\"  --> Update the quantity of product in your shopping cart");
			System.out.println(" - \"remove [id]\" --> Remove a product from your shopping cart");
			System.out.println(" - \"show-cart\"  --> View your shopping cart");
			System.out.println(" - \"empty-cart\"  --> Empty your shopping cart");
			System.out.println(" - \"checkout\"  --> checkout. Clears the shopping cart and removes products from available stock");
			System.out.println(" - \"order-history\"  --> view your order history");

			// Get Input from user
			String userInput = scanner.nextLine();


			// Split user input into different words
			List<String> wordList = new ArrayList<String>(Arrays.asList(userInput.trim().split(" "))); 
			
																										
			String firstWord = wordList.get(0);

			// Check for EXIT
			if (firstWord.trim().toLowerCase().equals("exit")) {
				scanner.close();
				this.logger.log(true, Level.INFO, "** Exiting!! **");
				logger.close();
				System.exit(0);
				break; // break the loop to exit program
			}
			
			// Minimum response is 4 chars ("cart")
			if (userInput.length() < 6) {
				logger.log(true, Level.INFO, "Invalid command: \"" + userInput + "\"");
				continue;
			}


			// Check if the command is valid
			if (!validCommands.contains(firstWord.toLowerCase())) {
				logger.log(true, Level.INFO, "Invalid command: \"" + firstWord + "\"");
				continue;
			}


			// Respond to user input
			Integer productId;
			Integer quantity;
			String response;
			
			// Switch statement on the first word in the wordlist
			switch (wordList.get(0).toLowerCase()) {
			case "add":
				// make sure user entered enough arguments
				if (userInput.trim().split(" ").length < 3) {
					logger.log(true, Level.INFO,
							"Invalid. Product id and quantity required to ADD to shopping cart:\"" + userInput + "\"");
					break;
				}

				// remove first word ("add")
				wordList.remove(0); 
				
				
				
				// make sure the product ID and Quantity are numeric
				if (!isNumeric(wordList.get(0))) {
					logger.log(true, Level.INFO,
							"Invalid product id:\"" + userInput + "\"");
					break;
				} else if (!isNumeric(wordList.get(1))) {
					logger.log(true, Level.INFO,
							"Invalid quntity:\"" + userInput + "\"");
					break;
				}

				productId = Integer.valueOf(wordList.get(0));
				quantity = Integer.valueOf(wordList.get(1));

				// Update shopping cart and print the response to terminal
				response = this.shoppingCart.add(productId, quantity);
				logger.log(true, Level.INFO, "add ->" + response);
				break;
				
			case "update":
				// make sure user entered enough arguments
				if (userInput.trim().split(" ").length < 3) {
					logger.log(true, Level.INFO,
							"Invalid. Product id and quantity required to UPDATE shopping cart:\"" + userInput + "\"");
					break;
				}

				// remove first word ("update")
				wordList.remove(0); 
				
				// make sure the product ID and Quantity are numeric
				if (!isNumeric(wordList.get(0))) {
					logger.log(true, Level.INFO,
							"Invalid product id:\"" + userInput + "\"");
					break;
				} else if (!isNumeric(wordList.get(1))) {
					logger.log(true, Level.INFO,
							"Invalid quntity:\"" + userInput + "\"");
					break;
				}

				productId = Integer.valueOf(wordList.get(0));
				quantity = Integer.valueOf(wordList.get(1));
				
				// Update shopping cart and print the response to terminal
				response = this.shoppingCart.update(productId, quantity);
				logger.log(true, Level.INFO, "Update ->" + response);
				break;
				
			case "remove":
				// make sure user entered only 2 arguments
				if (userInput.trim().split(" ").length != 2) {
					logger.log(true, Level.INFO,
							"Invalid. remove + id are required to remove item from shopping cart:\"" + userInput + "\"");
					break;
				}
				
				// make sure the product ID is numeric
				if (!isNumeric(wordList.get(1))) {
					logger.log(true, Level.INFO,
							"Invalid product id:\"" + userInput + "\"");
					break;
				} 

				
				productId = Integer.valueOf(wordList.get(1));
				
				// Update shopping cart and print the response to terminal
				response = this.shoppingCart.remove(productId);
				logger.log(true, Level.INFO, "Remove ->" + response);
				break;
				
			case "show-cart":
				this.shoppingCart.printCart();
				break;
				
			case "empty-cart":
//				String response = shoppingCart.clearAll();
//				logger.log(true, Level.INFO, "empty cart -> " + response);
				break;
				
			case "checkout":
				// TODO - @TANUJ
				break;
				
			case "order-history":
//				List<ShoppingCart> orders = orderServer.getOrders(this.username);
//				for (ShoppingCart s : orders) {
//					s.printCart();
//					System.out.println("---------------------------"); //
//				}
				break;
	
			}
		}
	}
	
	
	// Run the user interface
	public void run() throws RemoteException {
		// Get server references
		getServerRefs();

		// Handle user login or signup
		try {
			handleAuthentication();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		// Handle user shopping
		handleShopping();
	
	}
	
	// Main method to run terminal interaction
	public static void main(String args[]) throws RemoteException {
		UserInterface ui = new UserInterface();
		ui.run();
	}
	
}
