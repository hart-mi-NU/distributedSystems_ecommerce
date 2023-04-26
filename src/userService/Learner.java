package userService;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class Learner extends UnicastRemoteObject implements LearnerInterface, Runnable  {

	private static final long serialVersionUID = 1L;
	private Integer serverId;
	private Double highestLearnedId;
	private Integer serverCount;
	private Integer port = 4000;
	private UserServerInterface myServer;
	private MyLogger logger;
	private Registry registry;
	private boolean running = true;
	private BlockingQueue<PaxosMessage> messageQueue;
	private HashMap<Double, Request> acceptedRequests;
	private HashMap<Double, Integer> acceptedCounts;
	private List<Double> completedRequests;
	
	protected Learner(Integer serverId, MyLogger logger, Integer serverCount) throws RemoteException {
		super();
		this.serverCount = serverCount;
		this.serverId = serverId;
		this.logger = logger;
		this.messageQueue = new LinkedBlockingQueue<>(); // Queue for messages
		this.acceptedRequests = new HashMap<>();
		this.acceptedCounts = new HashMap<>();
		this.completedRequests = new ArrayList<>();
		
		// Register learner with rmi registry
		registry = LocateRegistry.getRegistry(this.port);
		registerRMI();
		
		
		try {
			this.myServer = (UserServerInterface) registry.lookup("userServer" + this.serverId);
		} catch (RemoteException | NotBoundException e) {
			logger.log(true, Level.SEVERE, "learner" + this.serverId + " unable to connect to parent userServer. Exiting...");
			e.printStackTrace();
			System.exit(-1);
		}
		
		// If this server fails, unregister it from RMI registry
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				unregisterRMI(); 
			}
		});

	}

	@Override
	public void run() {

		while (running) {		
			
			try {
                // Wait for a message to be received in the message queue
                PaxosMessage message = messageQueue.take();

                // Process the message
                processMessage(message);

            } catch (InterruptedException e) {
                // Handle InterruptedException, if needed
            	logger.log(true,  Level.INFO, "learner thread was interrupted and will exit.");
                break;
            }
		}
		
	}
	
    // The Learner thread will receive a message. This method is called by acceptors.
    public void receiveMessage(PaxosMessage message) {
        try {
        	if (!message.getType().equals("ping")) {        		
        		logger.log(true,  Level.INFO, "learner" + this.serverId + " received a message for id=" + message.getMessageId().toString());
        		messageQueue.put(message); 
        	}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    
    // Process a PaxosMessage
    private void processMessage(PaxosMessage message) {
    	logger.log(true,  Level.INFO, "learner" + this.serverId + " processing message with id=" + message.getMessageId().toString());
    	// EXIT
    	if (message.getType().toLowerCase().equals("stop")) {
    		this.running = false;
    	// ACCEPTED
    	} else if (message.getType().toLowerCase().equals("accepted")) {
    		if (this.completedRequests.contains(message.getMessageId())) {
    			// Already completed - don't double count
    			return;
    		}
    		// increment counter for the given message ID and check if count reaches consensus
    		
    		else if (this.acceptedCounts.containsKey(message.getMessageId())) {
    			Integer newCount = this.acceptedCounts.get(message.getMessageId()) + 1;
    			this.acceptedCounts.put(message.getMessageId(), newCount);
    			
    			if (newCount >= this.serverCount/2 + 1) {
    				try {
    					// Update the key/value store (menu)
    					logger.log(true,  Level.INFO, "leaner" + this.serverId + " received consensus on " + message.getRequest().getType() + " request, email=" + message.getRequest().getEmail());
						this.myServer.updateUserMap(message.getRequest());
						this.completedRequests.add(message.getMessageId());
					} catch (RemoteException e) {
						logger.log(true, Level.WARNING, "learner" + this.serverId + " failed to update the menu. Exiting...");
						e.printStackTrace();
						System.exit(-1);
					}
    			}
    		} else {
    			this.acceptedCounts.put(message.getMessageId(), 1);
    			this.acceptedRequests.put(message.getMessageId(), message.getRequest());
    		}
    	}
    }
    

	
	// Register this learner in RMI registry
	public void registerRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);			
			registry.rebind("learner" + this.serverId, this);

		} catch (Exception e) {
			logger.log(true,  Level.SEVERE, "Learner" + this.serverId + " unable to access rmiregistry. Cannot create learner. Exiting...");
			System.exit(-1);
		}
	}
	
	// Unregister this learner in RMI registry
	public void unregisterRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.unbind("learner" + this.serverId);
			logger.log(false, Level.INFO, this.serverId + " was unregistrered from rmi registry.");
		} catch (Exception e) {
			logger.log(true, Level.WARNING, "Failed to unregister learner" + this.serverId + " from rmi registry!");
			return;
		}
	}

	// Kill this thread
	@Override
	public void terminate() throws RemoteException {
		this.running = false;
		logger.log(true, Level.WARNING, "Learner" + this.serverId + " terminated");
	}

	@Override
	public boolean ping() throws RemoteException {
		if (this.running) {
			return true;
		}
		return false;
	}

		
	
	
		
}
