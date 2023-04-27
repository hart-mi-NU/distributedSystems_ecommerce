package inventoryService.server;

import java.util.HashMap;
import java.util.Map;

import inventoryService.dto.Product;

public class DataStore {

  private static DataStore singletonDataStore;
  private final Map<Integer, Integer> inventoryMap;
  private final Map<Integer, Product> productMap;

  private DataStore() {
    inventoryMap = new HashMap<>();
    productMap = new HashMap<>();
    loadSeedData();
  }

  public static DataStore getInstance() {
    if(null == singletonDataStore) {
      singletonDataStore = new DataStore();
    }
    return singletonDataStore;
  }

  private void loadSeedData() {
    Product prod1 = new Product();
    prod1.setName("iphone");
    prod1.setProductId(1001);
    prod1.setDescription("Mobile phone");
    prod1.setPrice(999);
    prod1.setRating(4.8);
    productMap.put(1001, prod1);
    inventoryMap.put(1001, 102);

    Product prod2 = new Product();
    prod2.setName("Airpods pro");
    prod2.setProductId(1002);
    prod2.setDescription("Earphones");
    prod2.setPrice(249);
    prod2.setRating(4.5);
    productMap.put(1002, prod2);
    inventoryMap.put(1002, 49);

    Product prod3 = new Product();
    prod3.setName("Playstation 5");
    prod3.setProductId(1003);
    prod3.setDescription("Gaming console");
    prod3.setPrice(599);
    prod3.setRating(4.9);
    productMap.put(1003, prod3);
    inventoryMap.put(1003, 10);

    Product prod4 = new Product();
    prod4.setName("MyProtein whey protein");
    prod4.setProductId(1004);
    prod4.setDescription("Health supplements");
    prod4.setPrice(97);
    prod4.setRating(4.3);
    productMap.put(1004, prod4);
    inventoryMap.put(1004, 56);

    Product prod5 = new Product();
    prod5.setName("Mr. coffee coffee machine");
    prod5.setProductId(1005);
    prod5.setDescription("Kitchen appliances");
    prod5.setPrice(25);
    prod5.setRating(4.5);
    productMap.put(1005, prod5);
    inventoryMap.put(1005, 32);
  }

  synchronized void updateProductQuantity(int prodId, int quantity) {
    if(inventoryMap.containsKey(prodId)) {
      inventoryMap.put(prodId, quantity);
      return;
    }
    throw new IllegalStateException("Product not found.");
  }

  synchronized void addProduct(Product product) {
    int prodId = product.getProductId();
    if(productMap.containsKey(prodId)) {
      throw new IllegalStateException("Product id already exists. Use a different productId");
    }
    productMap.put(prodId, product);
    inventoryMap.put(prodId, 0);
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
    return -1;
  }

  synchronized Product getProductInfo(int prodId) {
    if(productMap.containsKey(prodId)) {
      return productMap.get(prodId);
    }
    throw new IllegalStateException("product not found!");
  }

  synchronized Map<Product, Integer> getAllProductsAndInventory() {
    Map<Product, Integer> productQuantityMap = new HashMap<>();
    for(int key: productMap.keySet()) {
      int quantity = inventoryMap.getOrDefault(key, 0);
      Product prod = productMap.get(key);
      productQuantityMap.put(prod, quantity);
    }
    return productQuantityMap;
  }
}
