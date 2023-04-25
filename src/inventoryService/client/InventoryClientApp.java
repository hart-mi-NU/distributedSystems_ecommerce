package inventoryService.client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import gateway.EcomInterface;
import gateway.EcomInterfaceImpl;
import inventoryService.dto.Product;

public class InventoryClientApp {
  public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
    String host = args[0];
    String serverName = new StringBuilder("gateway-service").toString();
    EcomInterface gatewayStub = (EcomInterface) Naming.lookup("//"+host+"/"+serverName);


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

    gatewayStub.addProduct(prod1);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    gatewayStub.addProduct(prod2);

//    service.updateProductStock(1001, 10);
//    service.updateProductStock(1002, 59);

    System.out.println("O/P: " + gatewayStub.getProductAndInventory().toString());
  }
}
