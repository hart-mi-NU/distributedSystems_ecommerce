package order;

import inventoryService.api.InventoryService;
import userInterface.ShoppingCart;

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
    InventoryService inventoryService;
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
        inventoryService = getInventoryService();
    }

    private InventoryService getInventoryService() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 4000);
            return  (InventoryService) registry.lookup("inventoryService-4000");
        } catch (NotBoundException | RemoteException exception) {
            System.out.println(Helper.logWithTimestamp("Not able to find reference for the inventory service"));
        }
        System.out.println("Returning null inventory service reference");

        return null;
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
        inventoryService.updateProductStock(itemId, inventoryService.getProductStock(itemId) - stock);
        return true;
    }

    @Override
    public int inStock(Integer itemId) throws RemoteException {
        return inventoryService.getProductStock(itemId);
    }

    @Override
    public Result createOrder(ShoppingCart shoppingCart) throws RemoteException {
        this.orderId += 1;
        return servers.get(0).createOrder(orderId, shoppingCart);
    }

    @Override
    public Result getOrders(String username) throws RemoteException {
        return servers.get(0).getOrders(username);
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
            return new Result(proposal.getOperation(), null, "consensus not reached");
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
            return new Result(proposal.getOperation(), null, "consensus not reached");
        }

        Result res = null;

        for(PaxosServer acceptor: servers) {
            try {
                res = acceptor.learn(proposal);

            } catch(Exception e) {
                System.out.println(Helper.logWithTimestamp("A server did not respond to proposal at learn stage " + proposal.toString()+ e.toString()));
            }
        }
        if (res!=null && res.getOperation().equals("createOrder") && res.getShoppingCart() != null) {
            List<List<Integer>> itemIdsToAdd = res.getShoppingCart().get(0).getQuantities();

            for(List<Integer> itemId: itemIdsToAdd) {
                removeItem(itemId.get(0), itemId.get(1));
            }
        }

        return res;
    }

}
