package order;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the coordinator interface.
 */
public class OrderCoordinatorImpl extends UnicastRemoteObject implements OrderCoordinator {
    List<PaxosServer> servers = new ArrayList<>();
    private Integer orderId = 0;

    /**
     * Builds the CoordinatorImpl object based on the server ip and ports.
     * @param serverIPPorts a list of lists that contains the server ip and port information
     * @throws RemoteException
     */
    public OrderCoordinatorImpl(List<List<String>> serverIPPorts) throws RemoteException {
        super();

        serverIPPorts.forEach((serverIPAndPort) -> {
            servers.add(getServerReference(serverIPAndPort.get(0), Integer.parseInt(serverIPAndPort.get(1))));
        });
    }

    private PaxosServer getServerReference(String ip, int port) {
        try {
            Registry registry = LocateRegistry.getRegistry(ip, port);
            PaxosServer server = (PaxosServer) registry.lookup("paxos-server");
            System.out.println(Helper.logWithTimestamp(String.format("Connected with server at ip:%s and port:%s", ip, port)));
            return server;
        } catch (NotBoundException | RemoteException exception) {
            System.out.println(Helper.logWithTimestamp("Not able to find reference for the server"));
        }
        System.out.println("Returning null server reference");

        return null;
    }

    @Override
    public Boolean removeItem(Integer itemId, Integer stock) throws RemoteException {
        // TODO: contact inventory service
        return true;
    }

    @Override
    public int inStock(Integer itemId) throws RemoteException {
        // TODO: contact inventory service
        return 100;
    }

    @Override
    public Result createOrder(Integer userId, List<List<Integer>> itemIds) throws RemoteException {
        this.orderId += 1;
        return servers.get(0).createOrder(orderId, userId, itemIds);
    }

    @Override
    public Result execute(Proposal proposal) throws RemoteException {
        int majority = Math.floorDiv(servers.size(), 2) + 1;
        int promised = 0;
        int accepted = 0;

        // phase 1
        for(PaxosServer acceptor: servers) {
            try {
                Promise promise = acceptor.promise(proposal);

                if(promise == null) {
                    System.out.println(Helper.logWithTimestamp("A server did not respond to proposal " + proposal.toString()));
                }

                if (promise.getStatus().equals("promised") || promise.getStatus().equals("accepted")) {
                    promised++;
                    System.out.println(Helper.logWithTimestamp("A server promised proposal " + proposal.toString()));
                } else {
                    System.out.println(Helper.logWithTimestamp("A server rejected proposal " + proposal.toString()));
                }
            } catch(Exception e) {
                System.out.println(Helper.logWithTimestamp("A server did not respond to proposal " + proposal.toString() + e.getMessage()));
            }
        }

        // phase 2
        if (promised < majority) {
            return new Result(proposal.getOperation(), Collections.emptyList(), "consensus not reached");
        }

        for(PaxosServer acceptor: servers) {
            try {
                Boolean isAccepted = acceptor.accept(proposal);

                if(isAccepted == null) {
                    System.out.println(Helper.logWithTimestamp("A server did not respond to proposal " + proposal.toString()));
                }

                if (isAccepted) {
                    accepted++;
                    System.out.println(Helper.logWithTimestamp("A server accepted proposal " + proposal.toString()));
                } else {
                    System.out.println(Helper.logWithTimestamp("A server rejected proposal " + proposal.toString()));
                }
            } catch(Exception e) {
                System.out.println(Helper.logWithTimestamp("A server did not respond to proposal " + proposal.toString()));
            }
        }

        if (accepted < majority) {
            return new Result(proposal.getOperation(), Collections.emptyList(), "consensus not reached");
        }

        Result res = null;

        for(PaxosServer acceptor: servers) {
            try {
                res = acceptor.learn(proposal);

            } catch(Exception e) {
                System.out.println(Helper.logWithTimestamp("A server did not respond to proposal at learn stage " + proposal.toString()+ e.toString()));
            }
        }

        return res;
    }

}
