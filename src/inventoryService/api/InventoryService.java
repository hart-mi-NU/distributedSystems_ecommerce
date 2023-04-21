package api;

import java.rmi.Remote;

import dto.Product;

public interface InventoryService extends Remote {

  void addProduct(Product product);

  Product getProductInfo(int productId);

  int getProductStock(int prodId);

  void updateProductStock(int productId, int stockVal);

  void updateProductInfo(Product product);

}
