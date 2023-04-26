package gateway;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import inventoryService.dto.Product;
import order.Result;
import userInterface.ShoppingCart;
import userService.Request;
import userService.SerializedFuture;

public class EcomInterfaceImpl extends UnicastRemoteObject implements EcomInterface {

	
	
	
  protected EcomInterfaceImpl() throws RemoteException {
    super();
  }

  @Override
  public Map<Product, Integer> getProductAndInventory() throws RemoteException {
    return LoadBalancer.getInventoryService().getProductAndInventory();
  }

  @Override
  public void addProduct(Product product) throws RemoteException {
    LoadBalancer.getInventoryService().addProduct(product);
  }

@Override
public SerializedFuture<Request> signup(String username, String password) throws RemoteException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Request login(String username, String password) throws RemoteException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Integer getProductStock(Integer productId) throws RemoteException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Product getProduct(Integer productId) throws RemoteException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Result createOrder(ShoppingCart cart) throws RemoteException {
	// TODO Auto-generated method stub
	return null;
}

@Override
public Result getOrders(String username) throws RemoteException {
	// TODO Auto-generated method stub
	return null;
}
}
