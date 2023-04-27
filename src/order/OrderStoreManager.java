package order;

import userInterface.ShoppingCart;

import java.rmi.RemoteException;
import java.util.List;

public interface OrderStoreManager {
    /**
     * Used to create an order and run it in a PAXOS fashion.
     *
     * @param shoppingCart shopping cart of the user
     * @return result after executing the operation
     * @throws RemoteException
     */
    Result createOrder(Integer orderId, ShoppingCart shoppingCart) throws RemoteException;

    /**
     * Used to get orders of the current user.
     *
     * @param userId  user who is placing the order
     * @return result after executing the operation
     * @throws RemoteException
     */
    Result getOrders(Integer userId);
}
