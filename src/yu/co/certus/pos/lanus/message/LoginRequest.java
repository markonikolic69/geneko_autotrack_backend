package yu.co.certus.pos.lanus.message;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LoginRequest
    extends AbstractRequest {
  public LoginRequest() {
  }







  public String toString(){
    return "LOGIN REQUEST: terminal_id = " + getUserId() +
        ", pass = " + getOperatorPass();
  }
}
