package yu.co.certus.pos.lanus.data;

import java.sql.SQLException;

import yu.co.certus.pos.lanus.util.InternetData;
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
public interface PrepaidDBIface
    extends BaseDBIface {

  public int insertPrepaid(int sellerId, int postId, double amount,
          Phone paymentPhone, int point_of_sale_id, MobileOperatorEnum mob_operator) throws
      SQLException;

  public int insertPlatformInvoke(int transactionId, int methodId,
                                  String paymentPhone) throws SQLException;

  public int insertPlatformInvoke(int transactionId, int methodId,
                                  String paymentPhone, double amount) throws SQLException;

  public void updatePlatformTransactionId(int transactionId,
                                            int platformTransactionId) throws
            SQLException;
  
  public void resetAnnouncementFlag(int pos_id) throws
SQLException;
  
  public boolean isKupacBlocked(int pos_id) throws
  SQLException;
  
  public boolean checkCSSubconractorService(String cs_contractor_sifra_kupca, 
          int service_id);
  
  public int insertInternetTransaction(TransactionData transaction,
          InternetData internetData, double amount,
          String qPaySpotNumber) throws
          SQLException;
  
  public void updateInternetTransactionProvTrans(int transactionId, String extTrId) throws
  SQLException;
  
  public void updateFirmwareVersion(int post_id, 
          String firmware_version);
  
  public void updateMobtel(int transactionId, String authIdentResponse,
          String responseCode, String telenor_transaction_id) throws
          SQLException;


}
