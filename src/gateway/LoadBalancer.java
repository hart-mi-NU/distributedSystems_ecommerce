package gateway;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import inventoryService.api.InventoryService;
import order.OrderCoordinator;
import userService.UserServerInterface;

import static gateway.GatewayApplication.host;

public class LoadBalancer {

  private static int inventoryServiceInd = 0;
  private static int orderServiceInd = 0;
  private static int userServiceInd = 0;

  public static InventoryService getInventoryService(){
    try {
      List<String> registry = ServerRegistry.getInventoryServiceRegistry();
      int selectedInd = (inventoryServiceInd++) % registry.size();
      String selectedServerName = registry.get(selectedInd);
      System.out.println("selected server: " + selectedServerName);
      InventoryService inventoryService = (InventoryService) Naming.lookup("//"+host+"/"+selectedServerName);
      return inventoryService;
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static OrderCoordinator getOrderService() {
    try {
      List<String> registry = ServerRegistry.getOrderServiceRegistry();
      int selectedInd = (orderServiceInd++) % registry.size();
      System.out.println("Selected ind: " + selectedInd);

      String selectedServerName = registry.get(selectedInd);
      OrderCoordinator orderCoordinator = (OrderCoordinator) Naming.lookup("//"+host+"/"+selectedServerName);
      return orderCoordinator;
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static UserServerInterface getUserService() {
    try {
      List<String> registry = ServerRegistry.getUserServiceRegistry();
      int selectedInd = (userServiceInd++) % registry.size();
      System.out.println("Selected ind: " + selectedInd);

      String selectedServerName = registry.get(selectedInd);
      UserServerInterface userServerInterface = (UserServerInterface) Naming.lookup("//"+host+"/"+selectedServerName);
      return userServerInterface;
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return null;
  }
}
