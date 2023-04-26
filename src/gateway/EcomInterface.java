package gateway;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import inventoryService.dto.Product;
import order.Result;
import userInterface.ShoppingCart;
import userService.Request;
import userService.SerializedFuture;


// All methods that userInterface needs to interact with all 3 servers (user, inventory, order)
public interface EcomInterface extends Remote {
	
	// USER SERVER METHODS
	SerializedFuture<Request> signup(String username, String password) throws RemoteException;
	Request login(String username, String password) throws RemoteException;
	
	// INVENTORY SERVER 
	Integer getProductStock(Integer productId) throws RemoteException;
	Map<Product, Integer> getProductAndInventory() throws RemoteException;
	Product getProduct(Integer productId) throws RemoteException;
	void addProduct(Product product) throws RemoteException; // Mike: do we need this in the gateway??
  
	
	// ORDER SERVER
	Result createOrder(ShoppingCart cart) throws RemoteException;
	Result getOrders(String username) throws RemoteException;
  
 
}
