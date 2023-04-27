package gateway;

import java.util.ArrayList;
import java.util.List;

public class ServerRegistry {
  public static List<String> getInventoryServiceRegistry() {
    List<String> servers = new ArrayList<>();
    servers.add("inventoryService-4000");
    servers.add("inventoryService-4001");
//    servers.add("inventoryService-3");
//    servers.add("inventoryService-4");
//    servers.add("inventoryService-5");
    return servers;
  }

  public static List<String> getOrderServiceRegistry() {
    //todo
    List<String> servers = new ArrayList<>();
    servers.add("order-coordinator");
//    servers.add("order-service2");
//    servers.add("order-service3");
//    servers.add("order-service4");
//    servers.add("order-service5");
    return servers;
  }

  public static List<String> getUserServiceRegistry() {
    //todo
    List<String> servers = new ArrayList<>();
    servers.add("userServer0");
    servers.add("userServer1");
    servers.add("userServer2");
//    servers.add("userServer3");
//    servers.add("userServer4");

    return servers;
  }
}
