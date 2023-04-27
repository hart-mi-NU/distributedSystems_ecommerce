package gateway;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import inventoryService.api.InventoryService;
import order.OrderCoordinator;
import userService.UserServerInterface;

public class LoadBalancer {

  private static int inventoryServiceInd = 0;
  private static int orderServiceInd = 0;
  private static int userServiceInd = 0;

  public static InventoryService getInventoryService(){
    try {
      Registry rmiRegistry = LocateRegistry.getRegistry("localhost", 4000);
      List<String> registry = ServerRegistry.getInventoryServiceRegistry();
      int selectedInd = (inventoryServiceInd++) % registry.size();
      String selectedServerName = registry.get(selectedInd);
      System.out.println("selected server: " + selectedServerName);
      InventoryService inventoryService = (InventoryService) rmiRegistry.lookup(selectedServerName);
      return inventoryService;
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static OrderCoordinator getOrderService() {
    try {
      Registry rmiRegistry = LocateRegistry.getRegistry("localhost", 4000);
      List<String> registry = ServerRegistry.getOrderServiceRegistry();
      int selectedInd = (orderServiceInd++) % registry.size();
      String selectedServerName = registry.get(selectedInd);
      System.out.println("selected server: " + selectedServerName);
      OrderCoordinator orderCoordinator = (OrderCoordinator) rmiRegistry.lookup(selectedServerName);
      return orderCoordinator;
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static UserServerInterface getUserService() {
    try {
      Registry rmiRegistry = LocateRegistry.getRegistry("localhost", 4000);
      List<String> registry = ServerRegistry.getUserServiceRegistry();
      int selectedInd = (userServiceInd++) % registry.size();
      String selectedServerName = registry.get(selectedInd);
      System.out.println("selected server: " + selectedServerName);
      UserServerInterface userServerInterface = (UserServerInterface) rmiRegistry.lookup(selectedServerName);
      return userServerInterface;
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return null;
  }
}
