package yu.co.certus.pos.lanus;

import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;

import yu.co.certus.pos.lanus.data.BaseDBIface;
import yu.co.certus.pos.lanus.data.BalanceData;

import yu.co.certus.pos.lanus.message.AbstractRequest;
import yu.co.certus.pos.lanus.message.AbstractResponse;
import yu.co.certus.pos.lanus.message.PrepaidResponse;
import yu.co.certus.pos.lanus.message.CancelResponse;

import java.sql.SQLException;
import yu.co.certus.pos.lanus.service.Service;
import yu.co.certus.pos.lanus.data.DBFactory;
import yu.co.certus.pos.lanus.data.DatabaseException;

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
public abstract class OperationProcessor {

  protected Properties properties;

  private BaseDBIface _base_agent = null;

  private boolean _isBlockable = false;

  protected String _terminal_id = "";

  protected OperationProcessor(String terminal_id, boolean isBlockable,
                               BaseDBIface agent) {
    _terminal_id = terminal_id;
    _base_agent = agent;
    _isBlockable = isBlockable;
    initializeProperties();
  }

  protected void initializeProperties() {
    properties = new Properties();
    try {
      FileInputStream stream = new FileInputStream(
          "application.properties");
      properties.load(stream);
      stream.close();
    }
    catch (IOException e) {}
    // TODO what to do?
  }

  public abstract void process(AbstractRequest request,
                               AbstractResponse response);


  public void closeAgent() {
    try {
      if (_base_agent != null)
        _base_agent.close();
    }
    catch (SQLException e) {
      // iskulirati
      e.printStackTrace();
    }
  }

  protected boolean isBlockable() {
    return _isBlockable;
  }


  /**
     * checks the balance for particular terminal. For each terminals, there is
     * specified Daily, Weekly and Monthly Limit of top ups
     *
     * @param agent DatabaseAgent interface towards database
     * @param terminalId int pos terminal ID
     * @param response OperationResponse holds the inforamtion to be send back
     * to pos terminal
     * @throws PosException if unable to get balance data from database,
     * in that case, method inserts the error into database
     */
    protected void checkBalance(int post_id) throws PosException, NoBalanceException {

//        if (Service.logger.isDebugEnabled()) {
//            Service.logger.debug("--> ");
//        }

        try {
            if (_base_agent.isTerminalBlocked(_terminal_id)) {

                throw new NoBalanceException(
                        "Limit prekoracen za terminal serialNumber = " +
                        _terminal_id);

            }
            BalanceData balance = _base_agent.getBalance(post_id);

            if (balance.getDaily() > balance.getDailyLimit()) {
                if (_isBlockable) {
                    _base_agent.blockTerminal(_terminal_id);
                }
            }


        } catch (SQLException e) {
//            if (Service.logger.isDebugEnabled()) {
//                Service.logger.error("SQLException , details : " +
//                                     e.getMessage(), e);

//            }


            throw new PosException("Couldn't check balance");
        }
//        if (Service.logger.isDebugEnabled()) {
//            Service.logger.debug("<--");
//        }

    }


    protected void updateBalance(int post_id,
                               double amount){

 //       if (Service.logger.isDebugEnabled()) {
 //           Service.logger.debug("--> post_id = " + post_id +
 //                                " amount = " + amount
 //                   );
 //       }

        try {
            _base_agent.updateBalance(post_id, (int) amount);
        } catch (SQLException e) {
//            if (Service.logger.isDebugEnabled()) {
//                Service.logger.error("SQLException when update balance , details : " +
//                                     e.getMessage(), e);

//            }


        }
//        if (Service.logger.isDebugEnabled()) {
//            Service.logger.debug("<--");
//        }

    }

    public void insertResponse(
                               String response) {
        try {
            _base_agent.insertResponse(_terminal_id, response);
        } catch (SQLException e) {
            // TODO nije hendlovano
//            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * updates current trabsaction status in the database
     *
     * @param transactionId int transactionID
     * @param statusId int status 1 - Zapoceta, 2 - uspesna, 3 - neuspesna
     *  and 4 - neuspesna
     * @param agent DatabaseAgent database interface
     * @throws PosException
     */
    public void updateTransactionStatus(int transactionId, int statusId, boolean isInternet) throws
            PosException {
//        if (Service.logger.isDebugEnabled()) {
//                    Service.logger.debug("--> ");
//        }

        try {
            if (transactionId != 0){
                if(isInternet){
                    _base_agent.updateInternetTransactionStatus(transactionId, statusId);
                }else{
                    _base_agent.updateTransactionStatus(transactionId, statusId);
                }
            }
                
        } catch (SQLException e) {
//            if (Service.logger.isDebugEnabled()) {
//                Service.logger.error("SQLException , details : " +
//                                     e.getMessage(), e);

//            }

            // TODO what to do here?
            throw new PosException("SQLException when try to "+
                                   " update transaction status, details: " +
                e.getMessage());
        }
//        if (Service.logger.isDebugEnabled()) {
//            Service.logger.debug("<--");
//        }

    }

    protected void insertTransactionResponse(String forPos) {
      try {
        _base_agent.insertResponse(_terminal_id, forPos);
      }
      catch (Throwable e) {
        if (Service.logger.isDebugEnabled()) {
          Service.logger.error(
              "-->Unable to insert last message for pos "
              + _terminal_id + ", details: " +
              e.getMessage());
        }

      }
    }
    
    
    protected void checkSellerPin(String pin , AbstractResponse response) 
    throws PosException{
        try {
          _base_agent.checkUserCredential(_terminal_id, pin);
        }
        catch (SQLException e) {
            if(response instanceof PrepaidResponse){
                response.addResponseCode(PrepaidResponse.SYSTEM_FAILURE_ERROR);
            }else{
                response.addResponseCode(CancelResponse.SYSTEM_FAILURE_ERROR);
            } 
          if (Service.logger.isDebugEnabled()) {
            Service.logger.error(
                "SQLException when try to check seller pin fro terminal "
                + _terminal_id + ", details: " +
                e.getMessage(), e);
          }
          throw new PosException("SQLException when try to check seller pin fro terminal "
                  + _terminal_id + ", details: " +
                  e.getMessage());
        }catch (DatabaseException e) {
            if(response instanceof PrepaidResponse){
                response.addResponseCode(PrepaidResponse.OPERATOR_NOT_ACTIVE_ERROR);
            }else{
                response.addResponseCode(CancelResponse.OPERATOR_NOT_ACTIVE_ERROR);
            } 
          if (Service.logger.isDebugEnabled()) {
            Service.logger.error(
                "Operator with PIN = "+ pin +" for terminal "+ _terminal_id +" NOT ACTIVE " );
          }
          throw new PosException("Operator with PIN = "+ pin +" for terminal "+ _terminal_id +" NOT ACTIVE ");
        }
      }
    


}
