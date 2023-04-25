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
    servers.add("order-service1");
    servers.add("order-service2");
    servers.add("order-service3");
    servers.add("order-service4");
    servers.add("order-service5");
    return servers;
  }

  public static List<String> getUserServiceRegistry() {
    //todo
    List<String> servers = new ArrayList<>();
    servers.add("user-service1");
    servers.add("user-service2");
    servers.add("user-service3");
    servers.add("user-service4");
    servers.add("user-service5");
    return servers;
  }
}
