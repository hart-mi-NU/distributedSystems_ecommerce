package order;

import java.io.Serializable;

/**
 * Promise object to communicate between the paxos servers.
 */
public class Promise implements Serializable {

    private String status;

    public Promise(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
}
