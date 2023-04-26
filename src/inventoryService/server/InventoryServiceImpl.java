package inventoryService.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import inventoryService.api.InventoryService;
import inventoryService.api.PaxosInterface;
import inventoryService.coordinator.CoordinatorService;
import inventoryService.dto.Product;
import inventoryService.dto.Promise;
import inventoryService.dto.Proposal;


public class InventoryServiceImpl extends UnicastRemoteObject implements InventoryService, PaxosInterface {

  private int maxId;
  private final DataStore storeInstance;
  private final CoordinatorService coordinatorService;

  protected InventoryServiceImpl() throws RemoteException {
    super();
    storeInstance = DataStore.getInstance();
    coordinatorService = new CoordinatorService();
  }

  @Override
  public void addProduct(Product product) throws RemoteException {
    Proposal generatedProposal = Proposal.generateProposal("add-product", product, 0);
    InventoryServiceLogger.info("Received add product request");
    coordinatorService.execute(generatedProposal);
  }

  @Override
  public Product getProductInfo(int productId) {
    return storeInstance.getProductInfo(productId);
  }

  @Override
  public int getProductStock(int prodId) {
    return storeInstance.getProductQuantity(prodId);
  }

  @Override
  public void updateProductStock(int productId, int stockVal) throws RemoteException {
    InventoryServiceLogger.info("Received update product stock request");
    Product stub = new Product();
    stub.setProductId(productId);
    Proposal generatedProposal = Proposal.generateProposal("update-product-stock", stub, stockVal);
    coordinatorService.execute(generatedProposal);
  }

  @Override
  public void updateProductInfo(Product product) throws RemoteException {
    InventoryServiceLogger.info("Received update product info request");
    Proposal generatedProposal = Proposal.generateProposal("update-product", product, 0);
    coordinatorService.execute(generatedProposal);
  }

  @Override
  public Map<Product, Integer> getProductAndInventory() {
    return storeInstance.getAllProductsAndInventory();
  }

  @Override
  public Promise propose(Proposal proposal) throws RemoteException {
    Promise response = new Promise();
    if(proposal.getProposalId() > this.maxId) {
      this.maxId = proposal.getProposalId();
      InventoryServiceLogger.info("Setting new max id: " + this.maxId);
      InventoryServiceLogger.info("Propose complete: " + proposal);
      response.setStatus("200");
    } else {
      InventoryServiceLogger.error("Propose rejected: " + proposal.toString());
      response.setStatus("500");
    }

    return response;
  }

  @Override
  public Boolean accept(Proposal proposal) throws RemoteException {
    InventoryServiceLogger.info("Accept request received");

    if(proposal.getProposalId() != this.maxId) {
      InventoryServiceLogger.error("Accept failed: " + proposal);
      return Boolean.FALSE;
    } else {
      InventoryServiceLogger.info("Accept completed: " + proposal);
      return Boolean.TRUE;
    }
  }

  @Override
  public void learn(Proposal proposal) throws RemoteException {
    InventoryServiceLogger.info("Learn request received");

    String operation = proposal.getOperation();
    Product product = proposal.getProduct();
    int quantity = proposal.getQuantity();

    if(operation.equals("add-product")) {
      storeInstance.addProduct(product);
    } else if(operation.equals("update-product-stock")) {
      storeInstance.updateProductQuantity(product.getProductId(), quantity);
    } else if(operation.equals("update-product")) {
      storeInstance.updateProduct(product);
    }

    InventoryServiceLogger.info("Learn operation completed.");
  }
}
