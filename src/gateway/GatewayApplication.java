package gateway;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import inventoryService.server.InventoryServiceLogger;

public class GatewayApplication extends EcomInterfaceImpl {

  public static String host;

  protected GatewayApplication() throws RemoteException {
    try {
      EcomInterface service = new EcomInterfaceImpl();
      String serverName = new StringBuilder("gateway-service").toString();
      Naming.rebind(serverName, service);
      InventoryServiceLogger.info("Gateway ready!");
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
    host = args[0];
    new GatewayApplication();
  }
}
