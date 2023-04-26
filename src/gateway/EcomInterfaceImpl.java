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
  public SerializedFuture<Request> signup(String username, String password) throws RemoteException {
    return LoadBalancer.getUserService().signup(username, password);
  }

  @Override
  public Request login(String username, String password) throws RemoteException {
    return LoadBalancer.getUserService().login(username, password);
  }

  @Override
  public Integer getProductStock(Integer productId) throws RemoteException {
    return LoadBalancer.getInventoryService().getProductStock(productId);
  }

  @Override
  public Product getProduct(Integer productId) throws RemoteException {
    return LoadBalancer.getInventoryService().getProductInfo(productId);
  }

  @Override
  public void updateProductStock(int productId, int stockVal) throws RemoteException {
    LoadBalancer.getInventoryService().updateProductStock(productId, stockVal);
  }

  @Override
  public Result createOrder(ShoppingCart cart) throws RemoteException {
    return LoadBalancer.getOrderService().createOrder(cart);
  }

  @Override
  public Result getOrders(String username) throws RemoteException {
    return LoadBalancer.getOrderService().getOrders(username);
  }

}
