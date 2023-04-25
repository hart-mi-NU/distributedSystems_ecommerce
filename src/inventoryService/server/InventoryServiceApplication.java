package inventoryService.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import inventoryService.api.InventoryService;
import inventoryService.dto.Product;

public class InventoryServiceApplication extends InventoryServiceImpl{

  public static String host;
  protected InventoryServiceApplication(String port) throws RemoteException, MalformedURLException {
    InventoryService service = new InventoryServiceImpl();
    String serverName = new StringBuilder("inventoryService").append("-").append(port).toString();
    Naming.rebind(serverName, service);
    InventoryServiceLogger.info("Inventory Server ready!");
  }

  public static void main(String[] args) throws RemoteException, MalformedURLException {
    host = args[0];
    String port = args[1];

    new InventoryServiceApplication(port);
  }
}
