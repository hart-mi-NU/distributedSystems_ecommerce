package order;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * This class is basically a server that stores the order data in a in-memory map.
 */
public class OrderStoreManagerImpl extends
        java.rmi.server.UnicastRemoteObject implements OrderStoreManager, PaxosServer {

    private static int proposalId = 1;

    private long maxSeenId;
    private Proposal accepted;
    private String serverIP;
    private final String serverPort;
    private final String coordinatorIP;
    private final String coordinatorPort;
    private OrderCoordinator coordinator;

    private final ConcurrentHashMap<Integer, List<List<List<Integer>>>> keyValueStore;

    public OrderStoreManagerImpl(String serverPort, String coordinatorIP, String coordinatorPort, ConcurrentHashMap<Integer, List<List<List<Integer>>>> keyValueStore)
            throws RemoteException {
        super();

        this.serverPort = serverPort;
        this.coordinatorIP = coordinatorIP;
        this.coordinatorPort = coordinatorPort;
        this.keyValueStore = keyValueStore;
        this.maxSeenId = 0;
        this.accepted = null;

        try{
            this.serverIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException exception) {
            System.out.println(Helper.logWithTimestamp("Server:" + serverIP + " " + serverPort + " "+ "Unable to fetch server ip"));
        }
    }

    @Override
    public Result createOrder(Integer orderId, Integer userId, List<List<Integer>> itemIds) throws RemoteException {
        String clientHost;
        try {
            clientHost = getClientHost();
        } catch (ServerNotActiveException e) {
            throw new RuntimeException(e);
        }

        System.out.println(Helper.logWithTimestamp("Server:" + serverIP + " " + serverPort + " "+ String.format("Client: %s Request: %s\n", clientHost, "createOrder")));

        String operationType = Helper.getOperationType("operation");
        int[] nums = Helper.getOperationParams("operation");

        // check if request from client is malformed
        if(!Helper.isClientRequestValid(operationType, nums)) {
            System.out.println(Helper.logWithTimestamp("Server:" + serverIP + " " + serverPort+ " " + String.format("Received malformed request of length %s from %s", "createOrder".length(), clientHost)));
        }

        this.coordinator = getCoordinator();

        Result result;

        result = this.coordinator.execute(Proposal.createProposal("createOrder", orderId, userId, itemIds));

        System.out.println(Helper.logWithTimestamp("Server:" + serverIP + " " + serverPort + " "+ String.format("Client: %s Response:%s\n", clientHost, result)));

        return result;
    }

    private OrderCoordinator getCoordinator() {
        try {
            Registry registry = LocateRegistry.getRegistry(this.coordinatorIP, Integer.parseInt(this.coordinatorPort));
            return (OrderCoordinator) registry.lookup("coordinator");
        }  catch (NotBoundException | RemoteException e) {
            System.out.println(Helper.logWithTimestamp("Server:" + serverIP + " " + serverPort + " " + "Not able to find the coordinator"));
        }
        return null;
    }

    @Override
    public Promise promise(Proposal proposal) throws RemoteException {
        System.out.println(Helper.logWithTimestamp("Received a promise message for proposal: " + proposal));

        if (Math.random() <= 0.01) {
            Helper.logWithTimestamp("Server randomly failed");
            return null;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FutureTask<Promise> futureTask = new FutureTask<>(new Callable<Promise>() {
            @Override
            public Promise call() throws Exception {
                if (proposal.getId() <= OrderStoreManagerImpl.this.maxSeenId) {
                    return new Promise("rejected", null);
                } else {
                    OrderStoreManagerImpl.this.maxSeenId = proposal.getId();
                    if(OrderStoreManagerImpl.this.accepted != null) {
                        return new Promise("accepted", accepted);
                    } else {
                        return new Promise("promised", proposal);
                    }
                }
            }
        });

        try {
            executorService.submit(futureTask);
            Promise v = futureTask.get(10, TimeUnit.SECONDS);
            return v;
        } catch (Exception e) {
            System.out.println(Helper.logWithTimestamp("Server encountered error during promise of proposal"));
            return null;
        }

    }

    @Override
    public Boolean accept(Proposal proposal) throws RemoteException {
        System.out.println(Helper.logWithTimestamp("Received a accept message for proposal: " + proposal));

        if (Math.random() <= 0.01) {
            Helper.logWithTimestamp("Server randomly failed");
            return false;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        FutureTask<Boolean> futureTask = new FutureTask<>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (proposal.getId() != OrderStoreManagerImpl.this.maxSeenId) {
                    return false;
                }
                if (accepted == null) {
                    accepted = proposal;
                } else {
                    accepted.setId(proposal.getId());
                    accepted.setOperation(proposal.getOperation());
                    accepted.setOrderId(proposal.getOrderId());
                    accepted.setUserId(proposal.getUserId());
                }
                return true;
            }
        });

        try {
            executorService.submit(futureTask);
            return futureTask.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println(Helper.logWithTimestamp("Server encountered error during promise of proposal"));
            return null;
        }
    }

    @Override
    public Result learn(Proposal proposal) throws RemoteException {
        System.out.println(Helper.logWithTimestamp("Received a learn message for proposal: " + proposal));

        String operationType = proposal.getOperation();
        Integer orderIdToStore = proposal.getOrderId();
        Integer userIdToStore = proposal.getUserId();
        List<List<Integer>> itemIdsToAdd = proposal.getItemIds();
        List<List<List<Integer>>> valueToStore = new ArrayList<>();
        Result result;
        this.coordinator = getCoordinator();

        if (operationType.equals("createOrder")) {
            valueToStore.add(Collections.singletonList(Collections.singletonList(userIdToStore)));
            List<List<Integer>> itemsInStock = new ArrayList<>();
            for(List<Integer> itemId: itemIdsToAdd) {
                int quantityInStock = this.coordinator.inStock(itemId.get(0));
                if(quantityInStock >= itemId.get(1) ) {
                    itemsInStock.add(itemId);
                    this.coordinator.removeItem(itemId.get(0), itemId.get(1));
                } else {
                    itemsInStock.add(List.of(itemId.get(0), itemId.get(1) - quantityInStock));
                    this.coordinator.removeItem(itemId.get(0), quantityInStock);
                }
            }
            valueToStore.add(itemsInStock);
            this.keyValueStore.put(orderIdToStore, valueToStore);
            result = new Result("createOrder", itemsInStock, "Success");
        }
        else {
            System.out.println(Helper.logWithTimestamp("Server:" + serverIP + " " + serverPort + " " + String.format("Operation from client not recognized %s", operationType)));
            result = new Result(operationType, Collections.emptyList(), "Operation from client not recognized");
        }

        System.out.println(Helper.logWithTimestamp("State of server: "+ this.keyValueStore));

        return result;
    }
}
