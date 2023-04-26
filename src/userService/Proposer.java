package userService;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class Proposer extends UnicastRemoteObject implements Callable<Request>, ProposerInterface {

	private static final long serialVersionUID = 1L;
	private Request request;
	private Integer serverId;
	private Double uniqueId;
	private Registry registry;
	private MyLogger logger;
	BlockingQueue<PaxosMessage> messageQueue = new LinkedBlockingQueue<>();
	private Integer promiseCount;
	private Integer nackCount;
	private Integer acceptedCount;
	private Integer serverCount;
	private Integer consensusMin;
	private Integer port = 4000;
	private Double proposerId;
	
	public Proposer(Double proposerId, Integer serverId, Integer uniqueId, Request request, MyLogger logger, Integer serverCount) throws RemoteException {
		this.proposerId = proposerId;
		this.serverCount = serverCount;
		this.consensusMin = serverCount / 2 + 1;
		this.request = request;
		this.serverId = serverId;
		this.uniqueId = (double) uniqueId +  ((double) serverId/10.0) + ((double)proposerId/100.0);
		this.registry = LocateRegistry.getRegistry(8013);
		this.logger = logger;
		this.nackCount = 0;
		this.promiseCount = 0;
		this.acceptedCount = 0;
	}

	@Override
	public Request call() throws Exception {
		logger.log(true, Level.INFO, "Proposer" + this.proposerId + " started request for " + request.getType().toUpperCase() + " username="+request.getEmail());
		
		registerRMI();
		// If this proposer fails/stops, unregister it from RMI registry
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				unregisterRMI(); 
			}
		});
		
		
		// Try until successful
		int count = 1;
		while (true) {
			logger.log(true, Level.INFO, "Proposer" + this.proposerId + " starting paxos run " + count++);
			uniqueId = uniqueId + 1.0; // Increment the unique Id by 1

			String consensus;
			boolean restart = true;
			
			// Send "prepare" to all acceptors and wait for response or timeout
			// Move on after we get >50% promise messages
			logger.log(true, Level.INFO, "proposer" + this.proposerId.toString() + "sending prepare to acceptors");
			this.nackCount = 0;
			this.promiseCount = 0;
			sendMessageToAcceptors("prepare"); // will the timing ruin this?
			while (true) {
				PaxosMessage message = messageQueue.take();
				consensus = processMessage("promise", message);
				if (consensus == null) {
					// do nothing
				}
				else if (consensus.equals("pass")) {
					restart = false;
					break;
				} else if (consensus.equals("fail")) {
					restart = true;
					break;
				}
			}
			if (restart) {
				restart = false;
				continue; 
			}
									
			this.messageQueue.clear();
			this.nackCount = 0;
			this.acceptedCount = 0;
			
			// Send "accept" to all acceptors and wait for responses or timeout 
			// Move on after we get >50% accept messages
			sendMessageToAcceptors("accept"); 
			while (true) {
				PaxosMessage message = messageQueue.take();
				consensus = processMessage("accepted", message);
				if (consensus == null) {
					// do nothing
				}
				else if (consensus.equals("pass")) {
					break;
				} else if (consensus.equals("fail")) {
					restart = true;
					break;
				}
			}
			if (restart) {
				restart = false;
				continue; 
			}
			
			logger.log(true, Level.INFO, "proposer" + this.proposerId + " got accept consensus --> SUCCESS!");
			
			break;
		}

		request.setSuccessful(true);
		request.setMsg("Success: request=" + request.getType().toUpperCase() + ", username=\"" + request.getEmail() + "\", Password=\"" + request.getPassword().replaceAll("a-zA-Z*", "*") + "\"");
		logger.log(true, Level.INFO, "proposer confirmed successful " + request.getType() + " request");
		return request;
	}

	@Override
	public void receiveMessage(PaxosMessage message) throws RemoteException {
		try {
			this.messageQueue.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	// Send promise message to all acceptors
	private void sendMessageToAcceptors(String messageType) {
		PaxosMessage msg = new PaxosMessage(messageType, this.uniqueId, this.request, this.proposerId);
    	for (int i=0; i<30; i++) {
    		try {
				AcceptorInterface a = (AcceptorInterface) registry.lookup("acceptor" + i);
				a.receiveMessage(msg);
				
			} catch (RemoteException | NotBoundException e) {
				continue;
			}
		}
    }
	
	
	
		
	
	// Process a message from an acceptor
	private String processMessage(String testString, PaxosMessage message) {
		logger.log(false,  Level.INFO, "proposer" + this.proposerId.toString() + " processing message of type " + message.getType() + " with id=" + message.getMessageId().toString());
		
		if (message.getMessageId() < this.uniqueId) {
			return null;
		}
		else if (testString.equals("promise") && message.getMessageId().toString().equals(this.uniqueId.toString()) && message.getType().equals("promise")) {
			this.promiseCount++;
			// Test for "promise" consesnsus
			if (promiseCount >= this.consensusMin) {
				return "pass";
			} else {
				return null;
			}	
		}
		else if (testString.equals("promise") && message.getMessageId().toString().equals(this.uniqueId.toString()) && message.getType().equals("nack")) {
			this.nackCount++;
			// Test for "promise" consesnsus
			if (nackCount >= this.consensusMin) {
				return "fail";
			} else {
				return null;
			}
		}
		else if (testString.equals("accepted") && message.getMessageId().toString().equals(this.uniqueId.toString()) && message.getType().equals("accepted")) {
			this.acceptedCount++;
			if (this.acceptedCount >= this.consensusMin) {
				return "pass";
			} else {
				return null;
			}
		}
		else if (testString.equals("accepted") && message.getMessageId().toString().equals(this.uniqueId.toString()) && message.getType().equals("nack")) {
			this.nackCount++;
			if (this.nackCount >= this.consensusMin) {
				return "fail";
			} else {
				return null;
			}
		}
		return null;
	}
	

	// Register this proposer in RMI registry
	public void registerRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.rebind("proposer" + this.proposerId.toString(), this);
		} catch (Exception e) {
			logger.log(true, Level.SEVERE, "Proposer" + this.proposerId.toString() + " is unable to access rmiregistry. Cannot create acceptor. Exiting...");
			System.exit(-1);
		}
	}
	
	// Unregister this acceptor in RMI registry
	public void unregisterRMI() {
		try {
			Registry registry = LocateRegistry.getRegistry(this.port);
			registry.unbind("proposer" + this.proposerId.toString());
			logger.log(false, Level.INFO, "proposer" + this.proposerId.toString() + " was unregistrered from rmi registry.");
		} catch (Exception e) {
			logger.log(true, Level.WARNING, "Failed to unregister acceptor" + this.proposerId + " from rmi registry!");
			return;
		}
	}
}
