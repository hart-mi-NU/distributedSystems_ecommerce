package order;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class DummyClient {

    public static void main(String[] args) throws IOException {
        System.out.println(Helper.logWithTimestamp("Starting Client...\n"));

        try {
        Registry registry = LocateRegistry.getRegistry("localhost", Integer.parseInt(args[0]));
        OrderCoordinator storeManager = (OrderCoordinator) registry.lookup("coordinator");

        System.out.println(Helper.logWithTimestamp("Sending create operation Order Store ..."));

        List<List<Integer>> itemsWithQuantity = new ArrayList<>();
        List<Integer> firstItem = new ArrayList<>(List.of(1,2));
        List<Integer> secondItem = new ArrayList<>(List.of(2,1));
        itemsWithQuantity.add(firstItem);
        itemsWithQuantity.add(secondItem);

        Result result = storeManager.createOrder(1, itemsWithQuantity);

        System.out.println("Result: " + result);

            // If the server gives unexpected response log it and continue with rest of the operations
        if (!Helper.isServerResponseValid(result.getOperation(), "createOrder")) {
            System.out.println(Helper.logWithTimestamp("received unsolicited response acknowledging unknown PUT/GET/DELETE with an invalid KEY"));
//            continue;
        }

        }
            catch (RemoteException re) {
            System.out.println(Helper.logWithTimestamp(String.format("RemoteException %s", re)));
        }
            catch (NotBoundException nbe) {
            System.out.println(Helper.logWithTimestamp(String.format("NotBoundException %s", nbe)));
        }
            catch (java.lang.ArithmeticException ae) {
            System.out.println(Helper.logWithTimestamp(String.format("java.lang.ArithmeticException %s", ae)));
        }
    }
}
