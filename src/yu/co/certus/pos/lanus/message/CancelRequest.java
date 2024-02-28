package yu.co.certus.pos.lanus.message;

import yu.co.certus.pos.lanus.UnknownMobileNetworkException;
import yu.co.certus.pos.lanus.util.MobileOperatorEnum;
import yu.co.certus.pos.lanus.util.Phone;

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
public class CancelRequest
    extends AbstractRequest {

  private int _transactionStatus = -1;

  public void addTransactionId(String transactionId){
    addParam(CancelRequestCommEnum.TRANSACTION_ID_KEY,transactionId);
  }

  public String getTransactionId(){
    return getParam(CancelRequestCommEnum.TRANSACTION_ID_KEY);
  }

  public MobileOperatorEnum getMobOperator() throws UnknownMobileNetworkException{
      return MobileOperatorEnum.from_transaction_prefix(getTransactionId().substring(0,3));
  }

//  public boolean isMTS(){
//    return getTransactionId().startsWith(PaymentAgent.MTS_TRANSACTION_PREFIX);
//  }
//
//  public boolean isVIP(){
//    return getTransactionId().startsWith(PaymentAgent.MOBILKOM_TRANSACTION_PREFIX);
//  }
//  
  public boolean isAbatel(){
      try{
      return getMobOperator() == MobileOperatorEnum.ABATEL;
      }catch(Exception e){
          return false;
      }
  }

  public void setAmount(double amount){
    addParam(PrepaidRequestCommEnum.AMOUNT_KEY,"" + amount);
  }

  public void setPhone(String phone){
    addParam(PrepaidRequestCommEnum.PHONE_KEY,phone);
  }

//  public String getOperatorName(){
//      if(isVIP()) return "VIP";
//      //if(isTelenor()) return "Telenor";
//      if(isAbatel()) return "ABAtel";
//      return "mts";
//  }
  
  public double getAmount(){
    return Double.parseDouble(getParam(PrepaidRequestCommEnum.AMOUNT_KEY));
  }

  public Phone getPhone(){
    return Phone.parse(getParam(PrepaidRequestCommEnum.PHONE_KEY));
  }

  public void setTransactionStatus(int status){
    _transactionStatus = status;
  }

  public int getTransactionStatus(){
    return _transactionStatus;
  }



  public String toString(){
    return "CANCEL REQUEST transactionId = " + getTransactionId(); 
    //+
    //    ", dbTransactionId = " + getDBTransactionId() ;
  }



//  public static final String MTS_TRANSACTION_PREFIX = "148";
//  public static final String VIP_TRANSACTION_PREFIX = "444";

}
