package order;

import userInterface.ShoppingCart;

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
    private ShoppingCart shoppingCart;

    public Proposal(long id, String operation, Integer orderId, ShoppingCart shoppingCart) {
        this.id = id;
        this.operation = operation;
        this.orderId = orderId;
        this.shoppingCart = shoppingCart;
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

    public void setShoppingCart(ShoppingCart shoppingCart) {this.shoppingCart = shoppingCart;}

    public Integer getOrderId() { return this.orderId; }

    public ShoppingCart getShoppingCart() { return this.shoppingCart;}

    /**
     * Generate proposal.
     * @param operation name of operation
     * @param orderId order id for the order
     * @param shoppingCart shopping cart of the user
     * @return
     */
    public static synchronized Proposal createProposal(String operation, Integer orderId, ShoppingCart shoppingCart) {
        uniqueId += 1;
        return new Proposal(uniqueId, operation, orderId, shoppingCart);
    }

    public String toString() {
        return "Proposal: " + "Id: " + id + " Operation: " + operation + " OrderId: " + orderId + " Shopping cart: " + shoppingCart;
    }
}
