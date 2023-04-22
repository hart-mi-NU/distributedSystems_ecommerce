package userService;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LearnerInterface extends Remote {
	
	public void receiveMessage(PaxosMessage message) throws RemoteException;
	public void terminate() throws RemoteException;
	public boolean ping() throws RemoteException;
}
