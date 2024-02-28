package yu.co.certus.pos.lanus.message;

import java.text.DecimalFormat;
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
public class RetryResponse
    extends AbstractResponse {

  private boolean _isPrepaidAndSuccessful = false;
  private String _transactionId = "00000000000000";

  private String _forPos = "";

  public RetryResponse() {
  }

  public void addTransactionId(String transactionId){
    addParameter("TId", transactionId);
    _transactionId = transactionId;
  }

  public String getTransactionId(){
    return _transactionId;
  }

  public void addAmount(double amount){
      int amnt = (int)amount;
    addParameter("Amt", "" + amnt/*new DecimalFormat("#0,00").format( amount )*/);
  }
  
  public void addMID(String post_terminal_id){
      addParameter("mID",post_terminal_id);
  }
  
  public void addMSISDN(String msisdn){
      addParameter("Uid", msisdn);  
  }
  
  public void addCustomerCare(String cc){
      addParameter("CC", cc);  
  }
  
  public void addTime(Date date){
      addParameter("Time",pos_date_formatter.format(date));
    }
  
  public void addOperatorName(String name){
      addParameter("OP", name);
  }
  
  public void addREGParameter(String value){
      addParameter("REG", value);
  }

  public void setIsPrepaidAndSuccessful(boolean value){
    _isPrepaidAndSuccessful = value;
  }

  public boolean isPrepaidAndSuccessful(){
    return _isPrepaidAndSuccessful;
  }

  public void setForPos(String forPos){
    _forPos = forPos;
  }

  public String forPos(){
    if(_forPos.equals("")){
      return super.forPos();
    }else{
      return _forPos;
    }
  }

  public static boolean isRetry(String forPos){
    return (forPos.indexOf(OK) != -1) ||
        (forPos.indexOf(OVER_THE_LIMIT_ERROR) != -1) ||
        (forPos.indexOf(NO_TRANSACTION_ERROR) != -1) ||
        (forPos.indexOf(NOT_PREPAID_ERROR) != -1) ||
        (forPos.indexOf(PHONE_NOT_REGISTERED_ERROR) != -1) ||
        (forPos.indexOf(OVER_THE_CARD_LIMIT_ERROR) != -1) ||
        (forPos.indexOf(SYSTEM_FAILURE_ERROR) != -1) ||
        (forPos.indexOf(TIMEOUT_ERROR) != -1) ||
        (forPos.indexOf(WRONG_PIN_ERROR) != -1) ||
        (forPos.indexOf(UNDER_MINIMUM_AMOUNT_ERROR) != -1) ||
        (forPos.indexOf(ILLEGAL_AMOUNT_ERROR) != -1) ||
        (forPos.indexOf(ILLEGAL_TRANSACTION_ID_ERROR) != -1) ||
        (forPos.indexOf(CANCEL_UNABLE_ERROR) != -1);
  }


  public static final String OK = "17 OK";
  public static final String OVER_THE_LIMIT_ERROR =
      "27 over the transaction limit";
  public static final String NO_TRANSACTION_ERROR =
      "37 Prethodna akcija nije izvrsena";
  public static final String NOT_PREPAID_ERROR =
      "47 the phone number is not prepaid";
  public static final String PHONE_NOT_REGISTERED_ERROR =
      "57 the customer phone is not registered";
  public static final String OVER_THE_CARD_LIMIT_ERROR = "67 over the card limit";
  public static final String SYSTEM_FAILURE_ERROR = "77 System failure";
  public static final String TIMEOUT_ERROR = "87 Timeout";
  public static final String WRONG_PIN_ERROR = "97 Wron mPIN";
  public static final String UNDER_MINIMUM_AMOUNT_ERROR = "107 Under minimum amount";
  public static final String ILLEGAL_AMOUNT_ERROR = "117 Illegal amount";
  public static final String ILLEGAL_TRANSACTION_ID_ERROR = "127 Illegal Transaction ID";
  public static final String CANCEL_UNABLE_ERROR = "137 Transaction cannot be reversed";





}
