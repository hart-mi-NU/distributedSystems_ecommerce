package userService;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProposerInterface extends Remote {
	public void receiveMessage(PaxosMessage message) throws RemoteException;
	
}
