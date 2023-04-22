package order;

import java.io.Serializable;

/**
 * Promise object to communicate between the paxos servers.
 */
public class Promise implements Serializable {

    private String status;
    private Proposal proposal;

    public Promise(String status, Proposal proposal) {
        this.status = status;
        this.proposal = proposal;
    }
    public String getStatus() {
        return status;
    }
}
