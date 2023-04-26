package inventoryService.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import gateway.EcomInterface;

public class InventoryClientApp {
  public static void main(String[] args) throws NotBoundException, RemoteException {
    String host = args[0];
    String serverName = new StringBuilder("gateway-service").toString();
    Registry registry = LocateRegistry.getRegistry(4000);
    EcomInterface gatewayStub = (EcomInterface) registry.lookup(serverName);

    int currentStock1 = gatewayStub.getProductStock(1001);
    System.out.println("Old stock val 1: " + currentStock1);
    gatewayStub.updateProductStock(1001, currentStock1+1);
    System.out.println("New stock val 1: " + gatewayStub.getProductStock(1001));

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    int currentStock2 = gatewayStub.getProductStock(1002);
    System.out.println("Old stock val 1: " + currentStock2);
    gatewayStub.updateProductStock(1002, currentStock2-1);
    System.out.println("New stock val 1: " + gatewayStub.getProductStock(1002));

    System.out.println("O/P: " + gatewayStub.getProductAndInventory().toString());
  }
}
