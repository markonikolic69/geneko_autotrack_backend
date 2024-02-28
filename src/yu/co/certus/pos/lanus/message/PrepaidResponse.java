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
public class PrepaidResponse
    extends AbstractResponse {

  private boolean _isSuccessful  = false;

  private String _transactionId = "00000000000000";


  public PrepaidResponse() {
  }



  public void addTransactionId(String transactionId){
    addParameter("TId", transactionId);
    _transactionId = transactionId;
  }

  public String getTransactionId(){
    return _transactionId;
  }

  public void setIsSuccessfull(boolean value){
    _isSuccessful = value;
  }

  public void addTime(String time){
    addParameter("Time",time);
  }

  public boolean isSuccessful(){
    return _isSuccessful;
  }
  
  public void addMID(String post_terminal_id){
      addParameter("mID",post_terminal_id);
  }
  
  public void addVersion(String firmware_update_version){
      addParameter("Ver",firmware_update_version);
  }
  
  public void addAnnouncement(){
      addParameter(PrepaidRequestCommEnum.ANNOUNCEMENT_KEY,
              PrepaidRequestCommEnum.ANNOUNCEMENT_VALUE);
  }
  
  public void addCustomerCare(String cc){
      addParameter("CC", cc);  
  }
  
  public void addOperatorName(String name){
      addParameter("OP", name);
  }
  
  public void addREGParameter(String value){
      addParameter("REG", value);
  }

  public void addAdditinalInfo(String posName, String address){
      String insertPosName = posName;
      String insertAddress = address;
      if(posName.length() > 32){
          insertPosName =  posName.substring(0,32);
      }
      if(address.length() > 32){
          insertAddress =  address.substring(0,32);
      }
    addParameter(PrepaidRequestCommEnum.POINT_OF_SALE_1_KEY,
            insertPosName);
    addParameter(PrepaidRequestCommEnum.POINT_OF_SALE_2_KEY,
            insertAddress);
    addParameter(PrepaidRequestCommEnum.ADVERTISING_LINE_1_KEY,
                 "");
    addParameter(PrepaidRequestCommEnum.ADVERTISING_LINE_2_KEY,
                 "");
    addParameter(PrepaidRequestCommEnum.ADVERTISING_LINE_3_KEY,
                 "");
    addParameter(PrepaidRequestCommEnum.ADVERTISING_LINE_4_KEY,
    "");
    addParameter(PrepaidRequestCommEnum.ADVERTISING_LINE_5_KEY,
    "");
  }

  public static boolean isPrepaid(String forPos){
    return (forPos.indexOf(TRANSACTION_OK) != -1) ||
        (forPos.indexOf(OVER_THE_LIMIT_ERROR) != -1) ||
        (forPos.indexOf(OVER_THE_DAILY_LIMIT_ERROR) != -1) ||
        (forPos.indexOf(NOT_PREPAID_ERROR) != -1) ||
        (forPos.indexOf(PHONE_NOT_REGISTERED_ERROR) != -1) ||
        (forPos.indexOf(OVER_THE_CARD_LIMIT_ERROR) != -1) ||
        (forPos.indexOf(SYSTEM_FAILURE_ERROR) != -1) ||
        (forPos.indexOf(TIMEOUT_ERROR) != -1) ||
        (forPos.indexOf(WRONG_PIN_ERROR) != -1) ||
        (forPos.indexOf(UNDER_MINIMUM_AMOUNT_ERROR) != -1) ||
        (forPos.indexOf(POS_NOT_REGISTERED_ERROR) != -1) ||
        (forPos.indexOf(OPERATOR_NOT_ACTIVE_ERROR) != -1) ||
        (forPos.indexOf(ILLEGAL_AMOUNT_ERROR) != -1);
  }


  public static final String TRANSACTION_OK = "12 Transaction Successful";
  public static final String OVER_THE_LIMIT_ERROR =
      "22 over the transaction limit";
  public static final String OVER_THE_DAILY_LIMIT_ERROR =
      "32 over the daily limit";
  public static final String NOT_PREPAID_ERROR =
      "42 the phone number is not prepaid";
  public static final String PHONE_NOT_REGISTERED_ERROR =
      "52 the customer phone is not registered";
  public static final String OVER_THE_CARD_LIMIT_ERROR = "62 over the card limit";
  public static final String SYSTEM_FAILURE_ERROR = "72 System failure";
  public static final String TIMEOUT_ERROR = "82 Timeout";
  public static final String WRONG_PIN_ERROR = "92 Wrong mPIN";
  public static final String REMOTE_SERVER_UNAVAILABLE_ERROR = "92 Remote server is unavailable";
  public static final String UNDER_MINIMUM_AMOUNT_ERROR = "102 Under minimum amount";
  public static final String ILLEGAL_AMOUNT_ERROR = "112 Illegal amount";
  
  public static final String OPERATOR_NOT_ACTIVE_ERROR = "31 Operator not active";
}
