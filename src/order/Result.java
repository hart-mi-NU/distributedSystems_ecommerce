package order;

import userInterface.ShoppingCart;

import java.io.Serializable;
import java.util.List;

public class Result                                                                                                                                                                                     implements Serializable {
    private String operation;
    private List<ShoppingCart> shoppingCart;
    private String message;

    public Result(String operation, List<ShoppingCart> shoppingCart, String message) {
        this.operation = operation;
        this.shoppingCart = shoppingCart;
        this.message = message;
    }

    public List<ShoppingCart> getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(List<ShoppingCart> shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
