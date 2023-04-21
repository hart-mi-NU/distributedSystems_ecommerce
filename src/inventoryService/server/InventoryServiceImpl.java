package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import api.InventoryService;
import dto.Product;

public class InventoryServiceImpl extends UnicastRemoteObject implements InventoryService {

  protected InventoryServiceImpl() throws RemoteException {
    super();
  }

  @Override
  public void addProduct(Product product) {
    //todo....
  }

  @Override
  public Product getProductInfo(int productId) {
    //todo....
    Product res = new Product();
    res.setName("iphone");
    res.setProductId(1001);
    res.setDescription("Mobile phone");
    res.setPrice(999);
    res.setRating(4.8);
    return res;
  }

  @Override
  public int getProductStock(int prodId) {
    //todo....
    return 5;
  }

  @Override
  public void updateProductStock(int productId, int stockVal) {
    //todo....
  }

  @Override
  public void updateProductInfo(Product product) {
    //todo....

  }
}
