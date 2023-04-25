package domain;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import server.ServerLogger;

import static server.ServerApp.appStartTime;

public class Proposal implements Serializable {

  private static final long serialVersionUID = 1L;
  private int proposalId;
  private String operation;
  private String key;
  private String value;

  public Proposal(int proposalId, String operation, String key, String value) {
    this.proposalId = proposalId;
    this.operation = operation;
    this.key = key;
    this.value = value;
  }

  public int getProposalId() {
    return proposalId;
  }

  public String getOperation() {
    return operation;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public static synchronized Proposal generateProposal(String operation, String key, String value) {
    ServerLogger.info("Generating new proposal");
    int counter = (int) TimeUnit.SECONDS.convert(
            System.currentTimeMillis() - appStartTime, TimeUnit.MILLISECONDS);
    Proposal proposal = new Proposal(counter, operation, key, value);
    return proposal;
  }

  @Override
  public String toString() {
    return "Proposal{" +
            "proposalId=" + proposalId +
            ", operation='" + operation + '\'' +
            ", key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
  }
}
