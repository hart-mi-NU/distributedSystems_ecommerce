package order;

import java.rmi.RemoteException;
import java.util.List;

public interface OrderStoreManager {
    /**
     * Used to create an order and run it in a PAXOS fashion.
     *
     * @param userId  user who is placing the order
     * @param itemIds id of items in the order
     * @return result after executing the operation
     * @throws RemoteException
     */
    Result createOrder(Integer orderId, Integer userId, List<List<Integer>> itemIds) throws RemoteException;
}
