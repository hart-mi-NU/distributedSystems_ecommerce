package inventoryService.server;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import inventoryService.api.InventoryService;


public class InventoryServiceApplication extends InventoryServiceImpl{

  public static String host;
  protected InventoryServiceApplication(String serverNum) throws RemoteException {
    InventoryService service = new InventoryServiceImpl();
    Registry registry = LocateRegistry.getRegistry(4000);
    String serverName = new StringBuilder("inventoryService").append("-").append(serverNum).toString();
    registry.rebind(serverName, service);
    InventoryServiceLogger.info("Inventory Server ready!");
  }

  public static void main(String[] args) throws RemoteException {
    host = args[0];
    String serverNum = args[1];

    new InventoryServiceApplication(serverNum);
  }
}
