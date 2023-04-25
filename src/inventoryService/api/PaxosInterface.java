package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import domain.Promise;
import domain.Proposal;

/**
 * Interface provides operations to initiate different stages
 * of the paxos algorithm.
 */
public interface PaxosInterface extends Remote {

  /**
   * Sends a propose/prepare request to the server. Server returns a positive
   * acknowledgement if n > maxId of the server.
   * @param proposal proposal
   * @return promise containing the result
   * @throws RemoteException if RPC communication fails
   */
  Promise propose(Proposal proposal) throws RemoteException;

  /**
   * Sends an accept request to the server. Server returns a positive
   * acknowledgement if n == maxId of the server.
   * @param proposal proposal
   * @return boolean result
   * @throws RemoteException if RPC communication fails
   */
  Boolean accept(Proposal proposal) throws RemoteException;


  /**
   * Sends a learn message for the execution of user specified operation.
   * @param proposal proposal
   * @throws RemoteException if RPC communication fails
   */
  void learn(Proposal proposal) throws RemoteException;
}
