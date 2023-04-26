package userService;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Client extends UnicastRemoteObject implements ClientInterface {

	private static final long serialVersionUID = 1L;
	private MyLogger logger;;
	private Integer port = 4000;
	private Integer maxServers = 30;
	private Integer clientId;
	private UserServerInterface server;
	private Integer serverId;
	private Integer requestNum;


	// Construct a client
	public Client(Integer clientId) throws RemoteException {
		this.requestNum = 0;
		this.clientId = clientId;

		// If this client fails, unregister it from RMI registry
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				unregisterRMI(); 
			}
		});

		// Get server and serverId
		this.server = getFirstAvailableServer();
		this.serverId = server.getServerId();

		
		// Register with RMI Server using your ID
		registerRMI();
		try {
			this.logger = new MyLogger("client" + this.clientId + "Log.csv");
		} catch (IOException e) {
			System.out.println("Unable to create log file. Exiting...");
			e.printStackTrace();
			System.exit(-1);
		}
		logger.log(true, Level.INFO, "Client created");
	}

	
	// Get first available server
	private UserServerInterface getFirstAvailableServer() {
		UserServerInterface server;
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(this.port);
			for (int i = 0; i < this.maxServers; i++) {
				String server_id = "server" + i;
				try {
					// lookup server
					server = (UserServerInterface) registry.lookup(server_id);
					if (server != null && server.isProposer()) {
						System.out.println("server" + server.getServerId() + " found!");						
						return server;
					}
				} catch (Exception e) {}
			}
		} catch (RemoteException e) {
			if (this.logger != null) {
				logger.log(true, Level.SEVERE, "Unable to get RMI registry. Exiting...");
			} else {				
				System.out.println("Error: Unable to get RMI registry. Exiting...");
				e.printStackTrace();
			}
			System.exit(-1);
		}
		if (this.logger != null) {
			logger.log(true, Level.SEVERE, "Unable to find an available server.");
		} else {				
			System.out.println("Error: Unable to find available server.");
		}
		System.exit(-1);
		return null;
	}


	
	
	// Register this client in RMI registry
	public void registerRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.rebind("client" + this.clientId, registry);
		} catch (Exception e) {
			logger.log(true, Level.WARNING, "Failed to register " + this.clientId + " with rmi registry.\n" + e.getMessage());
		}
	}

	// Unregister this client in RMI registry
	public void unregisterRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.unbind("client" + this.clientId);
			logger.log(false, Level.INFO, this.clientId + " was unregistrered from rmi registry.");
		} catch (Exception e) {
			logger.log(true, Level.WARNING, "Failed to unregister" + this.clientId + " from rmi registry!");
			return;
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
					SerializedFuture<Request> future = this.server.signup(email, password);
					// Block until the Future object returns with a Request object
					Request response = future.get(2, TimeUnit.SECONDS);
					return response;
				} 
				else if (requestType.toLowerCase().equals("login")) {
					// get the SerializedFuture object
					Request response = this.server.login(email, password);
					return response;
				} else {
					logger.log(true, Level.SEVERE, "Invalid request! Imploding!!!");
					System.exit(-1);
				}
				
			} catch (RemoteException e) {
				// the server or an acceptor died... -> find another server and make request again
				this.logger.log(true, Level.WARNING, requestType.toUpperCase() + " request failed. Finding new server and trying again...");
				this.server = getFirstAvailableServer(); 
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(true, Level.SEVERE, e.getMessage());
				System.exit(-1);
			}
			i++;
		}
	}
	
	
	// Do work.... 10 PUTs, 5 DELETEs, and 5 GETs
	protected void work() {
		Request r;

		// get first available server
//		ServerInterface server = getFirstAvailableServer();
		
		// 5 Signups

		r = makeRequest("signup", "cheese", "123");
		handleLog(r);
		r = makeRequest("signup", "coke", "123");
		handleLog(r);
		r = makeRequest("signup", "pizza", "123");
		handleLog(r);
		r = makeRequest("signup", "turkey", "123");
		handleLog(r);
		r = makeRequest("signup", "apple", "123");
		handleLog(r);


		// 5 logins
		r = makeRequest("login", "chese", "123");
		handleLog(r);
		r = makeRequest("login", "coke", "123");
		handleLog(r);
		r = makeRequest("login", "burger", "111");
		handleLog(r);
		r = makeRequest("login", "apple", "2222");
		handleLog(r);
		r = makeRequest("login", "milk", "123");
		handleLog(r);


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

	
	// Listen & Respond to terminal input
		protected void handleKeyboardInput() throws RemoteException {
			Scanner scanner = new Scanner(System.in);
			List<String> validCommands = new ArrayList<String>(Arrays.asList("get", "put", "delete", "exit"));
			Boolean isValidCommand;
			// Listen for keyboard input
			while (true) {
				// Print instructions
				System.out.println(
						">>> Enter a command: \"SIGNUP [username], [password]\", or \"LOGIN [username] [password]\", or \"EXIT:\"");

				// Get Input from user
				String userInput = scanner.nextLine();

				// Minimum response is 4 chars ("EXIT")
				if (userInput.length() < 4) {
					logger.log(true, Level.INFO, "Invalid command " + userInput);
					continue;
				}

				List<String> wordList = new ArrayList<String>(Arrays.asList(userInput.trim().split(" "))); // All words in
																											// user input
				String firstWord = wordList.get(0);

				// Check if the command is valid
				if (!validCommands.contains(firstWord.toLowerCase())) {
					logger.log(true, Level.INFO, "Invalid command " + firstWord.toLowerCase());
					continue;
				}

				// Check for EXIT
				if (firstWord.toLowerCase().equals("exit")) {
					scanner.close();
					this.logger.log(true, Level.INFO, "** Client Exiting!! **");
					logger.close();
					System.exit(0);
					break; // break the loop to exit program
				}

				// Respond to input
				String keyRequest;
				String value;
				Request response;
				if (this.server == null) {
					System.out.println("getting new server");
					this.server = getFirstAvailableServer();
					this.serverId = this.server.getServerId();
					System.out.println("serverId=" + this.serverId);
				}
				switch (wordList.get(0).toLowerCase()) {
				case "signup":
					// make sure user entered both username and password
					if (userInput.trim().split(" ").length < 3) {
						logger.log(true, Level.INFO,
								"Invalid. username and password required for SIGNUP request -> \"" + userInput + "\"");
						break;
					}

					// Make request
					wordList.remove(0); // remove first word ("signup")
					value = wordList.get(wordList.size() - 1);
					wordList.remove(wordList.size() - 1); // remove last word (the value)
					keyRequest = String.join(" ", wordList);
					response = makeRequest("signup", keyRequest, value);
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
					value = wordList.get(wordList.size() - 1);
					wordList.remove(wordList.size() - 1); // remove last word (the value)
					keyRequest = String.join(" ", wordList);
					response = makeRequest("login", keyRequest, value);
					handleLog(response);
					break;
				}
			}

		}
	
		
		// Main method. Create a client, run 10 PUTs, 5 DELETEs, 5 GETs. Then handle
		// terminal keyboard input
		public static void main(String args[]) {
			Scanner scanner = new Scanner(System.in);
			String input;
			try {
				Client client = new Client(1);
				client.work();
				client.handleKeyboardInput();
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
}
