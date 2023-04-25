package domain;

import java.io.Serializable;

public class Promise implements Serializable {

  private static final long serialVersionUID = 1L;
  private String status;
  private String value;

  public Promise() {
  }

  public Promise(String status, String value) {
    this.status = status;
    this.value = value;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
