package gateway;

import java.util.ArrayList;
import java.util.List;

public class InventoryServerRegistry {
  public static List<String> getRegisteredServers() {
    List<String> servers = new ArrayList<>();
    servers.add("rpc-server-4000");
    servers.add("rpc-server-4001");
    servers.add("rpc-server-4002");
    servers.add("rpc-server-4003");
    servers.add("rpc-server-4004");
    return servers;
  }
}
