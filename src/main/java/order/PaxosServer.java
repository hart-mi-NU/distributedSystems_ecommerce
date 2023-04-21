package order;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * A paxos server that has the ability to promise, accept and lear the proposals.
 */
public interface PaxosServer extends Remote {
    /**
     * Promise a proposal.
     * @param proposal the proposal to promise
     * @return the promise object
     * @throws RemoteException
     */
    Promise promise(Proposal proposal) throws RemoteException;

    /**
     * Check if the paxos server for acceptance of the proposal.
     * @param proposal the proposal to accept
     * @return true if accepted proposal else false
     * @throws RemoteException
     */
    Boolean accept(Proposal proposal) throws RemoteException;

    /**
     * Once the proposal has been accepted, last step is to learn it.
     *
     * @param proposal the proposal to learn
     * @return the result after executing the proposal
     * @throws RemoteException
     */
    Result learn(Proposal proposal) throws RemoteException;

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
