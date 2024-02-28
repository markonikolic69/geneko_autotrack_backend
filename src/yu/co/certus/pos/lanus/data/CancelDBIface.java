package yu.co.certus.pos.lanus.data;

import java.sql.SQLException;
import yu.co.certus.pos.lanus.message.CancelRequest;

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
public interface CancelDBIface
    extends BaseDBIface {

  public int getTransactionId(int platformTransactionId, String terminalId, 
          boolean isInternet) throws
      SQLException, DatabaseException;

  public void fillPrepaid(int transactionId, CancelRequest request) throws
      SQLException;

  public int insertPlatformInvoke(int transactionId, int methodId,
                                  String paymentPhone) throws SQLException;

  public void updatePlatformTransactionId(int transactionId,
                                          int platformTransactionId) throws
      SQLException;

  public String getTransactionTime(int transactionId, boolean isInternet) throws SQLException;


  public void insertVipMailStorno(String orig_tr_id, String transaction_id, String msisdn,
          double amount, String subject, String message) throws SQLException;
  
  public String getIspTransactionID(int dbId) throws SQLException;
  
  public void fillPrepaidMobtel(int transactionId, CancelRequest request,
          TransactionData transaction,
          MobtelResponse oldResponse) throws
          SQLException;


}
