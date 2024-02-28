package yu.co.certus.pos.lanus.data;

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
public class LastPosTransactionData {


  private int _transactionId = -1;
  private int _platformTransactionId = -1;
  private int _transactionStatus = -1;
  private double _amount = 0;
  private String _msIsdn = "";
  private int _mobNetworkID = 0;
  
  private String _cancel_transaction = "";
  
  public String get_cancel_transaction() {
    return _cancel_transaction;
}


public void set_cancel_transaction(String _cancel_transaction) {
    this._cancel_transaction = _cancel_transaction;
}

private Date _transTimeStamp = null;


  public Date get_transTimeStamp() {
    return _transTimeStamp;
}


public void set_transTimeStamp(Date timeStamp) {
    _transTimeStamp = timeStamp;
}


public int get_mobNetworkID() {
    return _mobNetworkID;
}


public void set_mobNetworkID(int networkID) {
    _mobNetworkID = networkID;
}


public LastPosTransactionData() {
  }


  public int getTransactionId(){
    return _transactionId;
  }

  public int getPlatfromTransactionId(){
    return _platformTransactionId;
  }

  public int getTransactionStatus(){
    return _transactionStatus;
  }

  public double getAmount(){
    return _amount;
  }

  public String getMSIsdn(){
      if(_msIsdn.startsWith("+")){
          return _msIsdn.substring(1);
      }
    return _msIsdn;
  }

  public void setTransactionId(int trId){
    _transactionId = trId;
  }

  public void setPlatformTrId(String plTrId){
    if(plTrId == null || plTrId.equals("")){
      return;
    }
    _platformTransactionId = Integer.parseInt(plTrId);
  }

  public void setTransactionStatus(int trStatus){
    _transactionStatus = trStatus;
  }

  public void setAmount(double amount){
    _amount = amount;
  }

  public void setMSIsdn(String msIsdn){
    _msIsdn = msIsdn;
  }

  public String toString(){
    return "_transactionId = " + _transactionId +
        ", _platformTransactionId = " + _platformTransactionId +
        ", _transactionStatus = " + _transactionStatus +
        ", _mobNetworkID = " + _mobNetworkID +
        ", _amount = " + _amount +
        ", _msIsdn = " + _msIsdn;
  }
}
