package yu.co.certus.pos.lanus;

import yu.co.certus.pos.lanus.message.AbstractRequest;
import yu.co.certus.pos.lanus.message.AbstractResponse;
import yu.co.certus.pos.lanus.message.ReadParamResponse;

import yu.co.certus.pos.lanus.data.BaseDBIface;

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
public class ReadParamProcessor
    extends OperationProcessor {

  public ReadParamProcessor(String terminal_id, BaseDBIface agent){
    super(terminal_id, false,agent);
  }

  /**
   * process
   *
   * @param request AbstractRequest
   * @param response AbstractResponse
   * @todo Implement this yu.co.certus.pos.lanus.OperationProcessor method
   */
  public void process(AbstractRequest request, AbstractResponse response) {
    ReadParamResponse readRes = (ReadParamResponse)response;
    readRes.addResponseCode(ReadParamResponse.SYSTEM_FAILURE_ERROR);
    insertTransactionResponse(readRes.forPos());
    closeAgent();
  }
}
