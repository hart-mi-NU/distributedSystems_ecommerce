package inventoryService.coordinator;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import inventoryService.api.InventoryService;
import inventoryService.api.PaxosInterface;
import inventoryService.dto.Promise;
import inventoryService.dto.Proposal;
import inventoryService.dto.Response;
import inventoryService.server.InventoryServiceLogger;

import static inventoryService.server.InventoryServiceApplication.host;


public class CoordinatorService {

  private final ExecutorService executor = Executors.newFixedThreadPool(5); //todo
  private List<PaxosInterface> inventoryServiceAcceptors = new ArrayList<>();
  private final int half = 1; //todo

  private void init(){
    try {
      PaxosInterface service1 = (PaxosInterface) Naming.lookup("//"+host+"/inventoryService-4000");
      PaxosInterface service2 = (PaxosInterface) Naming.lookup("//"+host+"/inventoryService-4001");

      inventoryServiceAcceptors.add(service1);
      inventoryServiceAcceptors.add(service2);
    } catch (NotBoundException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
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

    //stage-1: send proposal
    boolean prepareResult = sendPrepare(proposal);
    if(!prepareResult) {
      InventoryServiceLogger.error("Promise failed!");
      return null; //todo: return error response
    }

    //Phase2: send accept
    InventoryServiceLogger.info("Starting acceptance...");
    boolean acceptResult = sendAccept(proposal);
    if(!acceptResult) {
      InventoryServiceLogger.error("Accept failed!");
      return null;
      //todo: return error response
    }

    InventoryServiceLogger.info("Accept complete!!!");

    //Phase3: send learn request
    InventoryServiceLogger.info("Sending learn requests");
    sendLearn(proposal);
    return null; //todo: return success response
  }
}
