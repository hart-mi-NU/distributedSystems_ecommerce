package order;

import userInterface.ShoppingCart;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * It is an interface which handles the coordination between the client and the order server.
 */
public interface OrderCoordinator extends Remote {

    /**
     * Submit a request to remove the item from the inventory
     * @param itemId id of the item to remove from the inventory
     * @return the result from the inventory service, true if item removed else false
     * @throws RemoteException
     */
    Boolean removeItem(Integer itemId, Integer stock) throws RemoteException;

    /**
     * Check if the item is in stock in the inventory
     * @param itemId id of the item to check for stock
     * @return the result from the inventory service, number of pieces in stock
     * @throws RemoteException
     */
    int inStock(Integer itemId) throws RemoteException;

    /**
     * Used to create an order and run it in a PAXOS fashion.
     *
     * @param shoppingCart shopping cart of the user
     * @return result after executing the operation
     * @throws RemoteException
     */
    Result createOrder(ShoppingCart shoppingCart) throws RemoteException;

    /**
     * Used to get orders of the current user.
     *
     * @param username  user who is placing the order
     * @return result after executing the operation
     * @throws RemoteException
     */
    Result getOrders(String username) throws RemoteException;

    /**
     * Used to start the execution of the proposal across the servers.
     *
     * @param proposal the proposal to send to the acceptors.
     * @return the result from the execution
     * @throws RemoteException
     */
    Result execute(Proposal proposal) throws RemoteException;
}
