package gateway;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import inventoryService.dto.Product;

public interface EcomInterface extends Remote {
  Map<Product, Integer> getProductAndInventory() throws RemoteException;

  void addProduct(Product product) throws RemoteException;

  Product getProductInfo(int productId) throws RemoteException;

  void updateProductInfo(Product product) throws RemoteException;

  int getProductStock(int prodId) throws RemoteException;

  void updateProductStock(int productId, int stockVal) throws RemoteException;

}
