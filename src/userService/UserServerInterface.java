package userService;
import java.rmi.*;


public interface UserServerInterface extends Remote {
	public boolean isProposer() throws RemoteException;
	public boolean isAcceptor() throws RemoteException;
	public boolean isLearner() throws RemoteException;
	public Integer getServerId() throws RemoteException;
	
	// Handle request for user sign-up
	public SerializedFuture<Request> signup(String email, String password) throws RemoteException;

	// Handle request for user login
	public Request login(String email, String password) throws RemoteException;
	
	// Update the user map
	public void updateUserMap(Request request) throws RemoteException;
	
}
