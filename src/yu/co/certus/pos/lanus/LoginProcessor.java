package yu.co.certus.pos.lanus;

import yu.co.certus.pos.lanus.message.AbstractRequest;
import yu.co.certus.pos.lanus.message.LoginRequest;
import yu.co.certus.pos.lanus.message.AbstractResponse;
import yu.co.certus.pos.lanus.message.LoginResponse;



import yu.co.certus.pos.lanus.data.DatabaseException;

import yu.co.certus.pos.lanus.data.TransactionData;
import yu.co.certus.pos.lanus.util.RandomCode;

import java.sql.SQLException;

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
public class LoginProcessor
    extends OperationProcessor {

  

//  private TransactionData _tData = null;

  public LoginProcessor(String terminal_id) {
    super(terminal_id, false, null);
    
  }

  /**
   * process
   *
   * @param request AbstractRequest
   * @param response AbstractResponse
   * @todo Implement this yu.co.certus.pos.lanus.OperationProcessor method
   */
  public void process(AbstractRequest request, AbstractResponse response) {

    LoginRequest log_req = (LoginRequest) request;
    LoginResponse log_response = (LoginResponse) response;

//    try {
//      setTransactionData(log_req.getUserId(), log_req.getOperatorPass());
      log_response.addResponseCode(log_response.LOGIN_SUCCESSFUL);
      //hardcoded for now
//      log_response.addCnt("" + _tData.getChangeCounter());
//      log_response.addOperatorName(_tData.getSellerName());
//      log_response.addmID(_tData.getSerialNumber());
 //     log_response.addCookie(RandomCode.generateCode(log_response.COOKIE_LENGTH));
//    }
//    catch (DatabaseException de) {
//      //            if (Service.logger.isDebugEnabled()) {
//      //                Service.logger.error("SQLException , details : " +
//      //                                     e.getMessage(), e);
//
//      //            }
//      log_response.addResponseCode(log_response.MPOS_NOT_REGISTERED_ERROR);
//      return;
//    }
//    catch (Throwable e) {
//      //            if (Service.logger.isDebugEnabled()) {
//      //                Service.logger.error("SQLException , details : " +
//      //                                     e.getMessage(), e);
//
//      //            }
//      log_response.addResponseCode(log_response.SYSTEM_FAILURE_ERROR);
//      return;
//    }


//    try {
//      _login_agent.checkUserCredential(log_req.getUserId(),
//                                       log_req.getOperatorPass());
//
//
//    }
//    catch (DatabaseException de) {
//      //            if (Service.logger.isDebugEnabled()) {
//      //                Service.logger.error("SQLException , details : " +
//      //                                     e.getMessage(), e);
//
//      //            }
//      log_response.addCnt("" + _tData.getChangeCounter());
//      log_response.addmID(_tData.getSerialNumber());
//      log_response.addOperatorName(_tData.getSellerName());
//      log_response.addResponseCode(log_response.OPERATOR_NOT_ACTIVE_ERROR);
//
//    }
//    catch (Throwable e) {
//      //            if (Service.logger.isDebugEnabled()) {
//      //                Service.logger.error("SQLException , details : " +
//      //                                     e.getMessage(), e);
//
//      //            }
//      log_response.addResponseCode(log_response.SYSTEM_FAILURE_ERROR);
//
//    }


  }

//  public void setTransactionData(String terminal_id, String pass)
//      throws SQLException, DatabaseException{
//
//      _tData =  _login_agent.getTransactionData(terminal_id, pass);
//
//  }
//
//  public TransactionData getTransactionData(){
//    return _tData;
//  }
}
