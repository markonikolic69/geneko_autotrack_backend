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
public class RetryRequest
    extends AbstractRequest {
  private AbstractRequest _origReq = new GiveUpRequest();
  public RetryRequest() {

  }

  public void setOriginalRequest(AbstractRequest originalRequest) {
    _origReq = originalRequest;
  }

  public AbstractRequest getOriginalRequest() {
    return _origReq;
  }

  public String toString() {
    return "RETRY REQUEST: Original request = " + getOriginalRequest();
  }

  public String getLastCommandKey(){
    return getParam(RetryRequestCommEnum.LAST_COMMAND_KEY);
  }

  public String getLastTransactionId(){
    return getParam(RetryRequestCommEnum.LAST_TRANSACTION_ID_KEY);
  }

}
