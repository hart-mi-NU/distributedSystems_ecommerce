package userService;
import java.io.Serializable;

public class PaxosMessage implements Serializable {

	private String type; // options = ["exit", "prepare", "accept", "accepted"]
	private Double messageId;  // roundId . serverId   (e.g., 10.2)
	private Request request;
	private Double proposerId;
	
	public PaxosMessage(String type, Double messageId, Request request) {
		this.type = type;
		this.messageId = messageId;
		this.request = request;
		
	}
	
	public PaxosMessage(String type, Double messageId, Request request, Double proposerId) {
		this.type = type;
		this.messageId = messageId;
		this.request = request;
		this.proposerId = proposerId;
	}
	
	public PaxosMessage(String type) {
		this.type = type;
	}

	public Double getProposerId() {
		return proposerId;
	}

	public void setProposer(Double proposerId) {
		this.proposerId = proposerId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getMessageId() {
		return messageId;
	}

	public void setMessageId(Double messageId) {
		this.messageId = messageId;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
}
