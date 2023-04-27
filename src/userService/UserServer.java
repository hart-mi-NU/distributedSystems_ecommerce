package userService;
import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;;

public class UserServer extends UnicastRemoteObject implements UserServerInterface {
	
	private static final long serialVersionUID = 1L;
	public boolean isProposer;
	public boolean isLearner;
	public boolean isAcceptor;
	public Learner learner;
	public Acceptor acceptor;
	private MyLogger logger;;
	private Integer port = 4000;
	private Integer serverCount;
	private Integer serverId;
	private HashMap<String, String> userMap;
	private ExecutorService executor;
	private Double proposerId; 
	
	
	public UserServer(boolean isProposer, boolean isAcceptor, boolean isLearner, Integer serverCount) throws RemoteException {
		super();
		this.serverCount = serverCount;

		// If this server fails, unregister it from RMI registry
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				unregisterRMI(); 
			}
		});
		
		// Register this server with RMI registry (this sets this.serverId also)		
		registerRMI();
		
		// Create logger
		try {
			this.logger = new MyLogger("userServer" + this.serverId + "Log.csv");
		} catch (IOException e) {
			System.out.println("Unable to create logger. Exiting...");
			e.printStackTrace();
			System.exit(-1);
		}
		logger.log(true, Level.INFO, "userServer" + this.serverId + " created!");
		
		// Create executor service with max 12 threads (10 proposers, 1 acceptor, 1 learner)
		this.executor = Executors.newFixedThreadPool(12); 
		
		// Handle proposer creation of executor
		if (isProposer) {
			this.isProposer = true;
			this.proposerId = (double) this.serverId;
		} else {
			this.isProposer = false;
		}
		
		// Handle acceptor instantiation
		if (isAcceptor) {
			this.isAcceptor  = true;
			this.acceptor = new Acceptor(this.serverId, this.logger, this.serverCount);
			executor.submit(this.acceptor);
		}
		
		// Handle learner instantiation
		if (isLearner) {
			this.isLearner = true;
			this.learner = new Learner(this.serverId, this.logger, this.serverCount);
			executor.submit(this.learner);
		}
		
		// Instantiate the menu (key/value store)
		this.userMap = new HashMap<String, String>();
		userMap.put("dhruv", "dhar");
	}


	@Override
	public SerializedFuture<Request> signup(String email, String password) {
		Request request = new Request(email, password, "signup", false);
		SerializedFuture<Request> response = submitProposerThread(request);
		return response;
	}

	
	@Override
	public Request login(String email, String password) {
		Request request = new Request(email, password, "login", false);
		if (this.userMap.containsKey(email.trim().toLowerCase()) && this.userMap.get(request.getEmail().trim().toLowerCase()).equals(request.getPassword())) {
			request.setSuccessful(true);
			request.setMsg("Login successful");
		} else if (this.userMap.containsKey(email.trim().toLowerCase())) {
			request.setMsg("Invalid password. Try again");
		} else {
			request.setMsg("Username \"" + email + "\" does not exist");
		}
		return request;
	}
	
	
	// add a new user to the usermap
	@Override
	public void updateUserMap(Request request) {
		if (request.getType().trim().toLowerCase().equals("signup")) {
			this.userMap.put(request.getEmail().trim().toLowerCase(), request.getPassword().trim());
		}
	}


	@Override
	public boolean isProposer() throws RemoteException {
		return this.isProposer;
	}



	@Override
	public boolean isAcceptor() throws RemoteException {
		return this.isAcceptor;
	}
	

	
	// Create a proposer thread and process the client request
	// Return a CompleteableFuture so the sever doesn't block
	private SerializedFuture<Request> submitProposerThread(Request clientRequest) {
		
		// Create a future
		SerializedFuture<Request> future = new SerializedFuture<Request>();
		
		// Create a Proposer to handle and return the client request
		Proposer proposer;
		try {
			// Run a Proposer in its own thread managed by executor
			Integer latestLearnedId = this.acceptor.getHighestIdSeen().intValue() + 1;
			proposer = new Proposer(this.proposerId, this.serverId, latestLearnedId, clientRequest, this.logger, this.serverCount);
			incrementProposerId();
			Future<Request> result = executor.submit(proposer);
			// Get the Future<Request> object in the Proposer thread --> future
			executor.execute(() -> {
				Request request;
				try {
					request = result.get(5, TimeUnit.SECONDS);
					future.complete(request);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					e.printStackTrace();
					future.completeExceptionally(e);
				}
			});
			// Return the future to the client
			return future;			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return future;			
	}


	@Override
	public boolean isLearner() throws RemoteException {
		return this.isLearner;
	}



	@Override
	public Integer getServerId() throws RemoteException {
		return this.serverId;
	}
	
	
	// Register this server in RMI registry
	public void registerRMI() {
		try {
			checkCreateRmiServer();
			Registry registry = LocateRegistry.getRegistry(this.port);
			// We know there are a maximum of 30 servers. Get iterate 0-serverCount and check rmi registry to get the smallest open Id
			for (int i = 0; i < this.serverCount; i++) {
				try {
					UserServerInterface serv = (UserServerInterface)registry.lookup("userServer" + i); // Try to get a server at the id = i
					if (i == serverCount - 1) {
						// Failed to find an unused server - we've reached the max of 30. Exit
						System.out.println("Already have max servers (max reached). Exiting...");
						System.exit(-1);
					}
					continue;
				} catch (Exception e) {
					this.serverId = Integer.valueOf(i); // Found an unused Id, set the serverId
					registry.rebind("userServer" + i, this);
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("userServer" + this.serverId + " unable to access rmiregistry. Cannot create server. Exiting...");
			System.exit(-1);
		}
	}
	
	// Unregister this client in RMI registry
	public void unregisterRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.unbind("userServer" + this.serverId);
			logger.log(false, Level.INFO, this.serverId + " was unregistrered from rmi registry.");
		} catch (Exception e) {
			logger.log(true, Level.WARNING, "Failed to unregister userServer" + this.serverId + " from rmi registry!");
			return;
		}
	}
	
	// Check if RMIServer exists, if not, create it
	private void checkCreateRmiServer() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			String[] boundNames = registry.list();
			// no exception thrown... rmi server running
			return;
		} catch (RemoteException e) {
			// Create rmi server
			try {
				LocateRegistry.createRegistry(this.port);
			} catch (RemoteException e1) {
				System.out.println("Unable to create rmi registry. Exiting...");
				System.exit(-1);
			}
		}
	}
	
	private void printRegisteredItems() {
		try {
			Registry reg = LocateRegistry.getRegistry(this.port);
			String[] names = reg.list();
			System.out.println("Servers registered with rmiRegistry:");
			for (String x : names) {
				System.out.println("  -" + x);
			}
		} catch (Exception e) {
			
		}
	}


	
	// Increment the proposer Id from X.0 to X.9 (and then begin at X.0). There can be max 10 proposer threads active at one time
	private void incrementProposerId() {
		String stringValue = this.proposerId.toString();
	    int decimalIndex = stringValue.indexOf('.');    
	    String decimalString = stringValue.substring(decimalIndex + 1);
	    Integer newDec;
	    if (decimalString.equals("9")) {
	    	newDec = 0;
	    } else {
	    	newDec = Integer.valueOf(decimalString) + 1;    	
	    }
	    this.proposerId = (double) this.serverId + (double) newDec/10;   
	}

	
	private void restartAcceptor() {
		try {
			logger.log(true, Level.INFO, "server" + this.serverId + " detected inactive acceptor. Restarting acceptor...");
			this.acceptor = new Acceptor(this.serverId, this.logger, this.serverCount);
			executor.submit(this.acceptor);
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.log(true, Level.SEVERE, "Unable to create acceptor. Exiting...");
		}
	}
	
	
	private void restartLearner() {
		try {
			logger.log(true, Level.INFO, "server" + this.serverId + " detected inactive learner. Restarting learner...");
			this.learner = new Learner(this.serverId, this.logger, this.serverCount);
			executor.submit(this.learner);
		} catch (RemoteException e) {
			e.printStackTrace();
			logger.log(true, Level.SEVERE, "Unable to create learner. Exiting...");
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
	
	
	// Main method
	public static void main(String args[]) {
		Scanner scanner = new Scanner(System.in);
		Integer serverCount = 3;
//		String input;
//		while (true) {
//
//			System.out.println("How many servers would you like to create? Enter 3 or more");
//			input = scanner.nextLine();
//			if (isNumeric(input.trim()) && Integer.valueOf(input) >= 3) {
//				serverCount = Integer.valueOf(input);
//				scanner.close();
//				break;
//			} else {
//				System.out.println("Invalid input!");
//			}
//		}
		try {
			for (int i =0; i<serverCount; i++) {
				UserServer server = new UserServer(true, true, true, serverCount);
				server.printRegisteredItems();
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
}
