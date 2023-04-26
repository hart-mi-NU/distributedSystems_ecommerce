package order;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to construct the coordinator and bind it on the RMI.
 */
public class CoordinatorStarter {
    public static void main(String[] args) {
//        if(args.length < 1) {
//            System.out.println(Helper.logWithTimestamp("Coordinator port number not given"));
//            return;
//        }

        List<List<String>> serverIPAndPorts = new ArrayList<>();
        serverIPAndPorts.add(new ArrayList<>(Arrays.asList("localhost", "5001")));
        serverIPAndPorts.add(new ArrayList<>(Arrays.asList("localhost", "5002")));
        serverIPAndPorts.add(new ArrayList<>(Arrays.asList("localhost", "5003")));
        serverIPAndPorts.add(new ArrayList<>(Arrays.asList("localhost", "5004")));
        serverIPAndPorts.add(new ArrayList<>(Arrays.asList("localhost", "8013")));


        try {
            OrderCoordinator coordinator = new OrderCoordinatorImpl(serverIPAndPorts);
            Registry registry = LocateRegistry.getRegistry(4000);
            registry.rebind("order-coordinator", coordinator);
            Helper.logWithTimestamp("Coordinator running at port 8013");
//            Helper.logWithTimestamp(String.format("Coordinator running at port %s", args[0]));
        } catch (RemoteException e) {
            Helper.logWithTimestamp("Can not create the paxos server");
        }
    }
}

