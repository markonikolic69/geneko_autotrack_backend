package yu.co.certus.pos.lanus.message;

import java.util.Date;

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
public class CancelResponse
    extends AbstractResponse {
  public CancelResponse() {
  }




  public void addTransactionId(String transactionId){
    addParameter("TId", transactionId);
  }
  
//  public void addStornoTransactionId(String transactionId){
//      addParameter("Id", transactionId);
//  }

  public void addAmount(double amount){
    addParameter("Amt", "" + (int)amount*100);
  }
  
  public void addMSISDN(String msisdn){
      addParameter("Uid", msisdn);  
  }
  
  public void addCustomerCare(String cc){
      addParameter("CC", cc);  
  }
  
  public void addOperatorName(String name){
      addParameter("OP", name);
  }
  
  public void addMID(String post_terminal_id){
      addParameter("mID",post_terminal_id);
  }

  public static boolean isCancel(String forPos){
    return (forPos.indexOf(CANCEL_SUCCESSFUL) != -1) ||
        (forPos.indexOf(ILLEGAL_TRANSACTION_ID_ERROR) != -1) ||
        (forPos.indexOf(CANCEL_UNABLE_ERROR) != -1) ||
        (forPos.indexOf(TIMEOUT_ERROR) != -1) ||
        (forPos.indexOf(POS_NOT_REGISTERED_ERROR) != -1) ||
        (forPos.indexOf(OPERATOR_NOT_ACTIVE_ERROR) != -1) ||
        (forPos.indexOf(SYSTEM_FAILURE_ERROR) != -1);
  }


  public static final String CANCEL_SUCCESSFUL = "14 Transaction Reversed";
  public static final String ILLEGAL_TRANSACTION_ID_ERROR = "24 Illegal Transaction ID";
  public static final String CANCEL_UNABLE_ERROR = "34 Transaction cannot be reversed";
  public static final String TIMEOUT_ERROR = "44 Timeout";
  public static final String SYSTEM_FAILURE_ERROR = "54 System failure";
  public static final String POS_NOT_REGISTERED_ERROR = "21 mPOS is not registered";
  public static final String OPERATOR_NOT_ACTIVE_ERROR = "31 Operator not active";
}
