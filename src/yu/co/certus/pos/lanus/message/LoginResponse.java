package yu.co.certus.pos.lanus.message;



import yu.co.certus.pos.lanus.util.RandomCode;

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
public class LoginResponse
    extends AbstractResponse {

  private String _defaultMID = "000000";
  private String _defaultOperator = "Administrator";

  private boolean _midAdded = false;
  private boolean _operAdded = false;

  public LoginResponse() {
    //addCookie(new RandomCode().generateCode(COOKIE_LENGTH));
  }

  private void addCookie(String cookie){
//    addParameter("Cookie", cookie);
  }

  public void addCnt(String cnt){
//    addParameter("Cnt",cnt);
  }

  public void addmID(String mid){
//    addParameter("mID","D0" + mid);
    _midAdded = true;
  }

  public void addOperatorName(String operator){
 //   addParameter("Opr",operator);
    _operAdded = true;
  }

  public String forPos(){
    //check mandatory fields
//    if(!_midAdded){
//      addmID(_defaultMID);
//    }
//    if(!_operAdded){
//      addOperatorName(_defaultOperator);
//    }
    //return info
    return super.forPos();
  }

  public boolean isSuccessful(){
    return getResponseCode().equals(LOGIN_SUCCESSFUL);
  }

  public static final String LOGIN_SUCCESSFUL = "11 Login Successful";
  public static final String MPOS_NOT_REGISTERED_ERROR = "21 mPOS is not registered";
  public static final String OPERATOR_NOT_ACTIVE_ERROR = "31 Operator not active";
  public static final String SYSTEM_FAILURE_ERROR = "41 System Failure";

  public static final int COOKIE_LENGTH = 32;
}
