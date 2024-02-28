package yu.co.certus.pos.lanus.message;



import yu.co.certus.pos.lanus.util.Phone;


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
public class LastTopUpRequest
    extends AbstractRequest {

  private double _amount = 0;
  private String _phone = "";
  private int _transactionStatus = 0;
  private int _transactionID = 0;
  private int _mobNetwork = 0;

  public int get_mobNetwork() {
    return _mobNetwork;
}

public void set_mobNetwork(int network) {
    _mobNetwork = network;
}



private String _stopTime = "";
  public LastTopUpRequest() {
  }

  public void setAmount(double amount){
    _amount = amount;
  }

  public void setPhone(String phone){
    _phone = phone;
  }

  public void setStopTime(String stopT){
    _stopTime = stopT;
  }

  public void setTransactionStatus(int transactionStatus){
    _transactionStatus = transactionStatus;
  }

  public void setTransactionId(int transactionID){
    _transactionID = transactionID;
  }

  public double getAmount(){
    return _amount;
  }

  public Phone getPhone(){
    return Phone.parse(_phone);
  }

  public int getTransactionStatus(){
    return _transactionStatus;
  }

  public int getTransactionId(){
    return _transactionID;
  }

  public String getStopTime(){
    return _stopTime;
  }



  public String toString(){
    return "" + _amount +"," + _phone + "," + _transactionStatus + "," + _transactionID;
  }



}
