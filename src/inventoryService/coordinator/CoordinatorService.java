package inventoryService.coordinator;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import inventoryService.api.PaxosInterface;
import inventoryService.dto.Promise;
import inventoryService.dto.Proposal;
import inventoryService.dto.Response;
import inventoryService.server.InventoryServiceLogger;


public class CoordinatorService {

  private final ExecutorService executor = Executors.newFixedThreadPool(getInventoryServiceRegistry().size() + 1);
  private List<PaxosInterface> inventoryServiceAcceptors = new ArrayList<>();
  private final int half = (getInventoryServiceRegistry().size() / 2);


  private List<String> getInventoryServiceRegistry() {
    List<String> servers = new ArrayList<>();
    servers.add("inventoryService-4000");
    servers.add("inventoryService-4001");
    return servers;
  }

  private void init(){
    try {
      Registry rmiRegistry = LocateRegistry.getRegistry("localhost", 4000);
      for(String serverName: getInventoryServiceRegistry()) {
        PaxosInterface service = (PaxosInterface) rmiRegistry.lookup(serverName);
        inventoryServiceAcceptors.add(service);
      }

    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private List<PaxosInterface> getInventoryServiceAcceptors() {
    return inventoryServiceAcceptors;
  }

  private boolean sendPrepare(Proposal proposal) {
    int promisedNodes = 0;
    List<Future<Promise>> prepFutures = new ArrayList();
    for(PaxosInterface stub: this.getInventoryServiceAcceptors()) {
      Future<Promise> prepFuture = executor.submit(() -> stub.propose(proposal));
      prepFutures.add(prepFuture);
    }

    try {
      for(Future<Promise> prepFuture: prepFutures) {
        Promise result = prepFuture.get();
        if(null != result && result.getStatus().equals("200")) {
          promisedNodes++;
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    if(promisedNodes <= half) {
      System.out.println("Prepare majority could not be obtained");
      return false;
    }
    return true;
  }

  private boolean sendAccept(Proposal proposal) {
    int acceptedNodes = 0;
    List<Future<Boolean>> acceptFutures = new ArrayList();
    for(PaxosInterface stub: this.getInventoryServiceAcceptors()) {
      Future<Boolean> acceptFuture = executor.submit(() -> stub.accept(proposal));
      acceptFutures.add(acceptFuture);
    }

    try {
      for(Future<Boolean> acceptFuture: acceptFutures) {
        Boolean result = acceptFuture.get();
        if(null != result && result.booleanValue()) {
          acceptedNodes++;
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    if(acceptedNodes <= half) {
      InventoryServiceLogger.error("Accept majority could not be obtained");
      return false;
    }
    return true;
  }

  private void sendLearn(Proposal proposal) throws RemoteException {
    for(PaxosInterface stub: this.getInventoryServiceAcceptors()) {
      stub.learn(proposal);
    }
    InventoryServiceLogger.info("Commit complete!");
  }

  public Response execute(Proposal proposal) throws RemoteException {
    if(inventoryServiceAcceptors.size() == 0) {
      init();
    }

    Response response = new Response();

    //stage-1: send proposal
    boolean prepareResult = sendPrepare(proposal);
    if(!prepareResult) {
      response.setStatus("500");
      response.setMessage("prepare failed");
      InventoryServiceLogger.error("Promise failed!");
      return response;
    }

    //Phase2: send accept
    InventoryServiceLogger.info("Starting acceptance...");
    boolean acceptResult = sendAccept(proposal);
    if(!acceptResult) {
      InventoryServiceLogger.error("Accept failed!");
      response.setStatus("500");
      response.setMessage("Accept failed");
      return response;
    }

    InventoryServiceLogger.info("Accept complete!!!");

    //Phase3: send learn request
    InventoryServiceLogger.info("Sending learn requests");
    sendLearn(proposal);
    response.setStatus("200");
    response.setMessage("operation completed successfully");
    return response;
  }
}
