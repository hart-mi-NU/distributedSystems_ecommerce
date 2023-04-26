package gateway;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import inventoryService.dto.Product;

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
  public Product getProductInfo(int productId) throws RemoteException {
    return LoadBalancer.getInventoryService().getProductInfo(productId);
  }

  @Override
  public void updateProductInfo(Product product) throws RemoteException {
    LoadBalancer.getInventoryService().updateProductInfo(product);
  }

  @Override
  public int getProductStock(int prodId) throws RemoteException {
    return LoadBalancer.getInventoryService().getProductStock(prodId);
  }

  @Override
  public void updateProductStock(int productId, int stockVal) throws RemoteException {
    LoadBalancer.getInventoryService().updateProductStock(productId, stockVal);
  }
}
