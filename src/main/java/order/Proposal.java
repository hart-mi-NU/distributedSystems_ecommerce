package order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Proposal object to communicate between the paxos servers.
 */
public class Proposal implements Serializable {
    private static long uniqueId = 0;
    private long id;
    private String operation;
    private Integer orderId;
    private Integer userId;
    private List<Integer> itemIds = new ArrayList<>();

    public Proposal(long id, String operation, Integer orderId, Integer userId, List<Integer> itemIds) {
        this.id = id;
        this.operation = operation;
        this.orderId = orderId;
        this.userId = userId;
        this.itemIds = itemIds;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setOrderId(Integer orderId) { this.orderId = orderId;}

    public void setUserId(Integer userId) {this.userId = userId;}

    public Integer getOrderId() { return this.orderId; }

    public Integer getUserId() { return this.userId;}

    public List<Integer> getItemIds() {return this.itemIds;}

    /**
     * Generate proposal.
     * @param operation name of operation
     * @param orderId order id for the order
     * @param userId user who is placing the order
     * @param itemIds id of items in the order
     * @return
     */
    public static synchronized Proposal createProposal(String operation, Integer orderId, Integer userId, List<Integer> itemIds) {
        uniqueId += 1;
        return new Proposal(uniqueId, operation, orderId, userId, itemIds);
    }

    public String toString() {
        return "Proposal: " + "Id: " + id + " Operation: " + operation + " OrderId: " + orderId + " UserId: " + userId + " Items: " + itemIds;
    }
}
