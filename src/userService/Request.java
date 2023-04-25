package userService;
import java.io.Serializable;

public class Request implements Serializable {

	private String type;
	private String email;
	private String password;
	private boolean isSuccessful;
	private String msg;
	
	public Request(String email, String password, String type, boolean isSuccessful) {
		this.email = email;
		this.password = password;
		this.type = type;
		this.isSuccessful = isSuccessful;
		this.msg = "";
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}

}
