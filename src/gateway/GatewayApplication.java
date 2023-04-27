package gateway;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import inventoryService.server.InventoryServiceLogger;

public class GatewayApplication extends EcomInterfaceImpl {

  public static String host;

  protected GatewayApplication() throws RemoteException {
    try {
      EcomInterface service = new EcomInterfaceImpl();
      Registry registry = LocateRegistry.getRegistry(4000);
      String serverName = new StringBuilder("gateway-service").toString();
      registry.rebind(serverName, service);
      InventoryServiceLogger.info("Gateway ready!");
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
//    host = args[0];
    new GatewayApplication();
  }
}
