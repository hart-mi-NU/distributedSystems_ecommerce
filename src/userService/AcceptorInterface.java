package userService;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AcceptorInterface extends Remote {

	public void receiveMessage(PaxosMessage message) throws RemoteException;
	public String prepare(Double uniqueId) throws RemoteException;
	public Double getHighestIdSeen() throws RemoteException;
	public Double getIgnoreBelow() throws RemoteException;
	public void terminate() throws RemoteException;
	public boolean ping() throws RemoteException;
}
