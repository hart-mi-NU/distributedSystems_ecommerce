package order;

import userInterface.ShoppingCart;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to bind the paxos server in the RMI registry.
 */
public class OrderServerStarter {
    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println(Helper.logWithTimestamp("Server port number or coordinator host or coordinator port number not give"));
            return;
        }

        ConcurrentHashMap<Integer, ShoppingCart> hashMap = new ConcurrentHashMap<>();

        try {
            PaxosServer paxosServer = new OrderStoreManagerImpl(args[0], args[1], args[2], hashMap);
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.rebind("paxos-server", paxosServer);
            Helper.logWithTimestamp(String.format("Server running at port %s", args[0]));
        } catch (RemoteException e) {
            Helper.logWithTimestamp("Can not create the two phase commit server");
        }
    }
}
