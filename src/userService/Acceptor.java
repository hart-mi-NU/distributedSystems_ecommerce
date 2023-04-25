package userService;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class Acceptor extends UnicastRemoteObject implements AcceptorInterface, Runnable {

	private boolean running = true;
	private BlockingQueue<PaxosMessage> messageQueue = new LinkedBlockingQueue<>(); // Queue for messages
	private Proposer proposer;
	private boolean isBusy;
	private MyLogger logger;
	private Registry registry;
	private Integer port = 8013;
	private Integer serverCount;
	private Integer acceptorId;
	private Double ignoreBelow;
	
	protected Acceptor(Integer serverId, MyLogger logger, Integer serverCount) throws RemoteException {
		super();
		this.serverCount = serverCount;
		this.logger = logger;
		this.registry = LocateRegistry.getRegistry(this.port);
		this.acceptorId = serverId;
		this.ignoreBelow = getHighestIdSeen();
		
		registerRMI();

		// If this acceptor fails, unregister it from RMI registry
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				unregisterRMI(); 
			}
		});
	}

	
	
	@Override
	public void run() {

		
		// Run forever until thread dies / is terminated
		while (running) {
		
			
			try {
                // Wait for a message to be received in the message queue
                PaxosMessage message = messageQueue.take();
                
           
                // Process the message
                processMessage(message);

            } catch (InterruptedException e) {
                // Handle InterruptedException, if needed
                break;
            }
		}
	}
	
    // Method to set the 'running' flag to false, indicating the thread should stop running
    public void stop() {
        this.running = false;
    }
    
    
    // Process a message from the queue
    private void processMessage(PaxosMessage message) {
    	PaxosMessage m;
    	// EXIT
    	if (message.getType().toLowerCase().equals("stop")) {
    		this.running = false;
    	
    	// PREPARE
    	} else if (message.getType().toLowerCase().equals("prepare")) {
    		if (message.getMessageId() >= this.ignoreBelow) {
    			// Respond with Promise
    			m = new PaxosMessage("promise", message.getMessageId(), message.getRequest());
    			this.ignoreBelow = message.getMessageId();
    		} else {
    			m = new PaxosMessage("nack", message.getMessageId(), message.getRequest());
    		}
			try {
				logger.log(true,  Level.INFO, "acceptor" + this.acceptorId.toString() + " sending promise back to proposer" + message.getProposerId().toString());
				ProposerInterface p = (ProposerInterface) registry.lookup("proposer" + message.getProposerId());
				p.receiveMessage(m);
			} catch (RemoteException | NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	// ACCEPT
    	} else if (message.getType().toLowerCase().equals("accept")) {
    		boolean accepted = false;
    		if (message.getMessageId() >= this.ignoreBelow) {
    			// Respond with Promise
    			m = new PaxosMessage("accepted", message.getMessageId(), message.getRequest());
    			this.ignoreBelow = message.getMessageId();
    			logger.log(true,  Level.INFO, "acceptor" + this.acceptorId.toString() + " sending \"accepted\" back to proposer" + message.getProposerId().toString());
    			accepted = true;
    		} else {
    			m = new PaxosMessage("nack", message.getMessageId(), message.getRequest());
    			logger.log(true,  Level.INFO, "acceptor" + this.acceptorId.toString() + " sending \"nack\" back to proposer" + message.getProposerId().toString());
    		}
			try {
				// Send message back to proposer with "accepted" or "nack"
				ProposerInterface p = (ProposerInterface) registry.lookup("proposer" + message.getProposerId());
				p.receiveMessage(m);
				// If accepted -> send message to all learners
				if (accepted) {
					for (int i=0; i<this.serverCount; i++ ) {
						try {
							LearnerInterface learner = (LearnerInterface) registry.lookup("learner" + i);
							learner.receiveMessage(m);
						} catch (NotBoundException | RemoteException e) {
							logger.log(true,  Level.SEVERE, "Learner doesnt exist!!");
							e.printStackTrace();
						}
					}
				}
			} catch (RemoteException | NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    // Method used by Proposers to send this acceptor a message
    public void receiveMessage(PaxosMessage message) {
        try {
        	if (!message.getType().equals("ping")) {
        		logger.log(true,  Level.INFO, "acceptor" + this.acceptorId + " received message from proposer" + message.getProposerId().toString());
        		messageQueue.put(message); 
        	} else {
        		// "ping"
        		
        	}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    


	// return "promise" if uniqueId > ignoreBelow, else "nack"
	@Override
	public String prepare(Double uniqueId) throws RemoteException {
		if (uniqueId > this.ignoreBelow) {
			return "promise";
		} else {
			return "nack";
		}
	}
	
	
	// Register this acceptor in RMI registry
	public void registerRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.rebind("acceptor" + this.acceptorId, this);
		} catch (Exception e) {
			logger.log(true,  Level.SEVERE, "Acceptor" + this.acceptorId + " is unable to access rmiregistry. Cannot create acceptor. Exiting...");
			System.exit(-1);
		}
	}
	
	// Unregister this acceptor in RMI registry
	public void unregisterRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.unbind("acceptor" + this.acceptorId);
			logger.log(false, Level.INFO, "acceptor" + this.acceptorId + " was unregistrered from rmi registry.");
		} catch (Exception e) {
			logger.log(true, Level.WARNING, "Failed to unregister acceptor" + this.acceptorId + " from rmi registry!");
			return;
		}
	}
		
		
	// Checks with all acceptors and returns the highest ID that has been seen
	@Override
	public Double getHighestIdSeen() throws RemoteException {
		Double highestIdSeen = 0.0; // default
		for (int i=0; i<this.serverCount; i++) {
			if (i == this.acceptorId) {
				continue;
			}
			try {
				AcceptorInterface a =(AcceptorInterface) registry.lookup("acceptor" + i);
				if (a.getIgnoreBelow() > highestIdSeen) {
					highestIdSeen = a.getIgnoreBelow();
				}
			} catch (RemoteException | NotBoundException e) {}
		}
        return highestIdSeen;
	}



	@Override
	public Double getIgnoreBelow() throws RemoteException {
		return this.ignoreBelow;
	}

	// Kill this thread
	@Override
	public void terminate() throws RemoteException {
		this.running = false;
		logger.log(true, Level.WARNING, "acceptor" + this.acceptorId.toString() + " terminated!!!");
//		UnicastRemoteObject.unexportObject(this, true);
	}

	@Override
	public boolean ping() throws RemoteException {
		if (this.running) {
			return true;
		}
		return false;
	}

}





