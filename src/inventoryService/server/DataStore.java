package server;

import java.util.HashMap;
import java.util.Map;

import dto.Product;

public class DataStore {

  private DataStore singletonDataStore;
  private final Map<Integer, Integer> inventoryMap;
  private final Map<Integer, Product> productMap;

  private DataStore() {
    inventoryMap = new HashMap<>();
    productMap = new HashMap<>();
  }

  public DataStore getInstance() {
    if(null == singletonDataStore) {
      singletonDataStore = new DataStore();
    }
    return singletonDataStore;
  }

  synchronized void addProductQuantity(int prodId, int quantity) {
    if(inventoryMap.containsKey(prodId)) {
      int currentQuantity = inventoryMap.get(prodId);
      inventoryMap.put(prodId, (currentQuantity+quantity));
    }
    throw new IllegalStateException("Product not found.");
  }

  synchronized void addProduct(Product product) {
    int prodId = product.getProductId();
    if(productMap.containsKey(prodId)) {
      throw new IllegalStateException("Product id already exists. Use a different productId");
    }
  }

  synchronized void updateProduct(Product product) {
    int prodId = product.getProductId();
    if(!productMap.containsKey(prodId)) {
      throw new IllegalStateException("product not found!");
    }
    productMap.put(prodId, product);
  }

  synchronized int getProductQuantity(int prodId) {
    if(inventoryMap.containsKey(prodId)) {
      return inventoryMap.get(prodId);
    }
    throw new IllegalStateException("product not found!");
  }

  synchronized Product getProductInfo(int prodId) {
    if(productMap.containsKey(prodId)) {
      return productMap.get(prodId);
    }
    throw new IllegalStateException("product not found!");
  }
}
