package order;

import java.io.Serializable;
import java.util.List;

public class Result                                                                                                                                                                                     implements Serializable {
    private String operation;
    private List<Integer> itemsInOrder;
    private String message;

    public Result(String operation, List<Integer> itemsInOrder, String message) {
        this.operation = operation;
        this.itemsInOrder = itemsInOrder;
        this.message = message;
    }

    public List<Integer> getItemsInOrder() {
        return itemsInOrder;
    }

    public void setItemsInOrder(List<Integer> itemsInOrder) {
        this.itemsInOrder = itemsInOrder;
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
