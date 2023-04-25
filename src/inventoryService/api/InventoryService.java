package inventoryService.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import inventoryService.dto.Product;

public interface InventoryService extends Remote {

  void addProduct(Product product) throws RemoteException;

  Product getProductInfo(int productId) throws RemoteException;

  int getProductStock(int prodId) throws RemoteException;

  void updateProductStock(int productId, int stockVal) throws RemoteException;

  void updateProductInfo(Product product) throws RemoteException;

  Map<Product, Integer> getProductAndInventory() throws RemoteException;

}
