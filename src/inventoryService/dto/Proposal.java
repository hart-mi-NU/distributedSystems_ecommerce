package inventoryService.dto;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import inventoryService.server.InventoryServiceLogger;

public class Proposal implements Serializable {

  public static long appStartTime = System.currentTimeMillis();

  private static final long serialVersionUID = 1L;
  private int proposalId;
  private String operation;
  private Product product;
  private Integer quantity;

  public Proposal(int proposalId, String operation, Product product, Integer quantity) {
    this.proposalId = proposalId;
    this.operation = operation;
    this.product = product;
    this.quantity = quantity;
  }

  public static long getAppStartTime() {
    return appStartTime;
  }

  public int getProposalId() {
    return proposalId;
  }

  public String getOperation() {
    return operation;
  }

  public Product getProduct() {
    return product;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public static synchronized Proposal generateProposal(String operation, Product product, Integer quantity) {
    InventoryServiceLogger.info("Generating new proposal");
    int counter = (int) TimeUnit.SECONDS.convert(
            System.currentTimeMillis() - appStartTime, TimeUnit.MILLISECONDS);
    Proposal proposal = new Proposal(counter, operation, product, quantity);
    return proposal;
  }

  @Override
  public String toString() {
    return "Proposal{" +
            "proposalId=" + proposalId +
            ", operation='" + operation + '\'' +
            ", product=" + product +
            ", quantity=" + quantity +
            '}';
  }
}
