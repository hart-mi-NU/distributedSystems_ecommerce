package gateway;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import inventoryService.dto.Product;

public interface EcomInterface extends Remote {
  Map<Product, Integer> getProductAndInventory() throws RemoteException;

  void addProduct(Product product) throws RemoteException;
}
