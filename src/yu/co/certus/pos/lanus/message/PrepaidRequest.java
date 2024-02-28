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
public class PrepaidRequest
    extends AbstractRequest {

  private int _prepaidTransactionId = 0;

  private boolean _includeAddInfo = false;

  public PrepaidRequest(){

  }

  public void setIncludeAddInfo(boolean include){
    _includeAddInfo = include;
  }

  public boolean includeAddInfo(){
    return _includeAddInfo;
  }


  public double getAmount(){
    //must be integer
    return Integer.parseInt(getParam(PrepaidRequestCommEnum.AMOUNT_KEY))/100;
  }

  public Phone getPhone(){
    return Phone.parse(getParam(PrepaidRequestCommEnum.PHONE_KEY));
  }

  public void setPrepaidTraId(int transactionId){
    _prepaidTransactionId = transactionId;
  }

  public int getPrepaidTraId(){
    return _prepaidTransactionId;
  }
  
//  public boolean isMTS(){
//      return getParam(PrepaidRequestCommEnum.MOBILE_NETWORK_KEY).equals("1");
//  }
//  
//  public boolean isVIP(){
//      return getParam(PrepaidRequestCommEnum.MOBILE_NETWORK_KEY).equals("2");
//  }
//  
//  public boolean isTelenor(){
//      return getParam(PrepaidRequestCommEnum.MOBILE_NETWORK_KEY).equals("3");
//  }
  
  public boolean isAbatel(){
      try{
          return getMobOperator() == MobileOperatorEnum.ABATEL;
      }catch(UnknownMobileNetworkException umne){
          return false;
      }
  }
  
//  public String getOperatorName(){
//      if(isVIP()) return "VIP";
//      if(isTelenor()) return "Telenor";
//      if(isAbatel()) return "ABAtel";
//      return "mts";
//  }
  
  public MobileOperatorEnum getMobOperator() throws UnknownMobileNetworkException{
      return MobileOperatorEnum.from_post(Integer.parseInt(getParam(PrepaidRequestCommEnum.MOBILE_NETWORK_KEY)));
  }

  public String toString(){
    return "PREPAID REQUEST: Amount = "+ getAmount() + ", phone = " + getPhone() +
        ", cookie = " + getCookie();
  }

  public static void main(String[] args){
    System.out.println(Integer.parseInt("15099")/100);
  }
  
  public String getOriginalPhoneNumber(){
      return getParam(PrepaidRequestCommEnum.PHONE_KEY);
  }
  

}
