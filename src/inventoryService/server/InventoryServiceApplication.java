package inventoryService.server;

import java.rmi.RemoteException;

import inventoryService.api.InventoryService;
import inventoryService.dto.Product;

public class ServerApp {
  public static void main(String[] args) throws RemoteException {

    Product prod1 = new Product();
    prod1.setName("iphone");
    prod1.setProductId(1001);
    prod1.setDescription("Mobile phone");
    prod1.setPrice(999);
    prod1.setRating(4.8);

    Product prod2 = new Product();
    prod2.setName("Airpods pro");
    prod2.setProductId(1002);
    prod2.setDescription("Earphones");
    prod2.setPrice(249);
    prod2.setRating(4.5);

    InventoryService service = new InventoryServiceImpl();
    service.addProduct(prod1);
    service.updateProductStock(1001, 10);

    service.addProduct(prod2);
    service.updateProductStock(1002, 59);

    System.out.println(service.getProductAndInventory().toString());
  }
}
