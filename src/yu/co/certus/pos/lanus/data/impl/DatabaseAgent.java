package yu.co.certus.pos.lanus.data.impl;


import yu.co.certus.pos.lanus.data.*;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import yu.co.certus.pos.lanus.util.HmacSha1Signature;
import yu.co.certus.pos.lanus.util.Logger;
import yu.co.certus.pos.lanus.util.MobileOperatorEnum;
import yu.co.certus.pos.lanus.util.Phone;

import yu.co.certus.pos.lanus.message.CancelRequest;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;


import yu.co.certus.pos.lanus.message.LastTopUpRequest;
import yu.co.certus.pos.lanus.service.Service;
import yu.co.certus.pos.lanus.util.ServiceData;

import yu.co.certus.pos.lanus.util.InternetData;

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
public class DatabaseAgent
    extends AbstractDatabaseAgent implements BaseDBIface, 
    PrepaidDBIface, CancelDBIface, LastTopUpIface, ReportDBIface, InvoiceIface, PakistanFiskalDBIface{
  public DatabaseAgent() throws SQLException, DatabaseException{
  }
  
  public DatabaseAgent(String db_name, String user, String pass) throws SQLException, DatabaseException{
      super(db_name, user, pass);
  }
  
  private SimpleDateFormat _format =
      new SimpleDateFormat("yyyy-MM-dd");

///////////////////////BaseDBIface impl/////////////////////////////////////////
  
  
  public void resetAdditInfoFlag(int pos_id)
  throws DatabaseException, SQLException{
      query.executeUpdate("update point_of_sale set additional_info_flag = false where id = " +
              pos_id);
  }
  /**
     * @param terminalId
     * @return
     * @throws SQLException
     */
    public BalanceData getBalance(int post_id) throws SQLException {
 //       if (Service.logger.isDebugEnabled()) {
 //           Service.logger.debug("--> "/*terminalId = " + terminalId*/);
 //       }

        ResultSet result = query.executeQuery(
                "select daily_balance, weekly_balance, monthly_balance, " +
                "warning_sent, daily_limit, weekly_limit, monthly_limit, point.phone as point_of_sale_phone, " +
                "point.alert_send_id " +
                "from post, point_of_sale point, quota_type quota " +
                "where quota.id = post.quota_type_id and point.id = post.point_of_sale_id and post.id = " +
                post_id);
        result.next();
        // TODO 1 is hadcoded = telekom
        boolean telekomSend = false;
        if (result.getInt("alert_send_id") == 1)
            telekomSend = true;
 //       if (Service.logger.isDebugEnabled()) {
 //           Service.logger.debug("<--");
 //       }

        return new BalanceData(post_id, result.getInt("daily_balance"),
                               result.getInt("weekly_balance"),
                               result.getInt("monthly_balance"),
                               result.getBoolean("warning_sent"),
                               result.getInt("daily_limit"),
                               result.getInt("weekly_limit"),
                               result.getInt("monthly_limit"),
                               result.getString("point_of_sale_phone"),
                               telekomSend);
    }

    public String getLastMessage(String terminal_id) throws SQLException {
//        if (Service.logger.isDebugEnabled()) {
//            Service.logger.debug("--> "/*serialNumber = " + serialNumber*/);
//        }


        ResultSet result = query.executeQuery(
                "select last_message from post where terminal_id = '" + terminal_id + "'");

//        ResultSet result = query.executeQuery(
//                "select message from rec_message where psn = " + serialNumber +
//                " order by entry_time desc limit 1");

        result.next();
        String lastMessage = result.getString("last_message");

 //       if (Service.logger.isDebugEnabled()) {
 //           Service.logger.debug("<-- ");
  //      }

        return lastMessage;
    }





    public int insertPlatformInvoke(int transactionId, int methodId,
                                    int reversalTransactionId) throws
        SQLException {
    	
    	String qryStr = "insert into platform_method_invokation (transaction_id, method_id, reversal_transaction_id) " +
        "values (" + transactionId + ", " + methodId + ", " +
        reversalTransactionId + ")";

      query.executeUpdate(qryStr);
      Logger.getInstance().logPlainMessage(qryStr);
      
      ResultSet result = query.executeQuery(
          "select id from platform_method_invokation where " +
          "transaction_id = " + transactionId + " and method_id = " +
          methodId + " and " +
          "reversal_transaction_id = " + reversalTransactionId);
      result.next();

      return result.getInt("id");
    }

    public int insertPlatformInvoke(int transactionId, int methodId,
                                    String paymentPhone, double amount,
                                    int reversalTransactionId) throws
        SQLException {
    	String qryStr = "insert into platform_method_invokation (transaction_id, method_id, payment_phone, " +
        "amount, reversal_transaction_id) values " +
        "(" + transactionId + ", " + methodId + ", '" +
        paymentPhone + "', " + amount + ", " +
        reversalTransactionId + ")";
      query.executeUpdate(qryStr);
      Logger.getInstance().logPlainMessage(qryStr);
      
      ResultSet result = query.executeQuery(
          "select id from platform_method_invokation " +
          "where transaction_id = " + transactionId + " and method_id = " +
          methodId + " and " +
          "payment_phone = '" + paymentPhone + "' and amount = " + amount +
          " and " +
          "reversal_transaction_id = " + reversalTransactionId);
      result.next();

      return result.getInt("id");

    }

    public List getValidServices(String terminalId) throws SQLException {
 //       if (Service.logger.isDebugEnabled()) {
 //           Service.logger.debug("--> "/*serialNo = " +
 //                                serialNo*/);
//
 //       }

//        ResultSet result = query.executeQuery("select service.id from service " +
//                                              "inner join post_service on service.id = post_service.service_id " +
//                                              "inner join post on post_service.post_id = post.id " +
//                                              "where terminal_id = '" +
//                                              terminalId + "'");


        ResultSet result = query.executeQuery("select service_id as id from point_of_sale_service, post " +
                                              "where post.point_of_sale_id = point_of_sale_service.point_of_sale_id and terminal_id = '" +
                                              terminalId + "'");


        List services = new ArrayList();
        while (result.next()) {
            services.add(new Integer(result.getInt("id")));
        }

 //       if (Service.logger.isDebugEnabled()) {
 //           Service.logger.debug("<--");
 //       }

        return services;
    }

    public LastPosTransactionData getLastPosTransactionData(int postId, boolean isAbatel)
        throws SQLException{
      LastPosTransactionData toReturn = new LastPosTransactionData();
      
      String qry = isAbatel ?
              "select id as t_id, transaction_id as platform_transaction_id, status_id, " +
              "amount, qpayspot_id as msisdn, stop_time from internet_transactions " +
              "where " +
              "post_id = " + postId + " order by id desc limit 1"
              :
                  "select transaction.id as t_id, platform_transaction_id, status_id, "+
                  "amount, msisdn, mob_network_id, stop_time from post, transaction, payment where "+
                  "transaction.id = payment.transaction_id and " +
                  "transaction.post_id = " + postId + " order by transaction.id desc limit 1";    

      ResultSet result = query.executeQuery( qry
                );

//        ResultSet result = query.executeQuery(
//                "select message from rec_message where psn = " + serialNumber +
//                " order by entry_time desc limit 1");

        if(result.next()){
          toReturn.setAmount(result.getDouble("amount"));
          String msisdn = result.getString("msisdn");
          if(isAbatel){
              msisdn = Phone.parse(result.getString("msisdn").substring(3)).getFull(false);
          }
          toReturn.setMSIsdn(msisdn);
          toReturn.setPlatformTrId(result.getString("platform_transaction_id"));
          toReturn.setTransactionId(result.getInt("t_id"));
          toReturn.setTransactionStatus(result.getInt("status_id"));
          toReturn.set_mobNetworkID(isAbatel ? 914 /* transaction prefix*/ : result.getInt("mob_network_id"));
          toReturn.set_transTimeStamp(result.getTimestamp("stop_time"));
        }
        
        
 //       if (Service.logger.isDebugEnabled()) {
 //           Service.logger.debug("<-- ");
  //      }

        return toReturn;


    }
    
    
    public int getMTSCancelTRId(int transactionID) throws DatabaseException, SQLException{
        ResultSet result = query.executeQuery(
                "select id "+
                "from platform_method_invokation where "+
                "transaction_id = "+ transactionID + " and " +
                "method_id = 5");

//        ResultSet result = query.executeQuery(
//                "select message from rec_message where psn = " + serialNumber +
//                " order by entry_time desc limit 1");

        if(result.next()){
         return result.getInt("id");
        }
        
        throw new DatabaseException("No cancel transaction ID for MTS for transaction_id = " +
                transactionID );

    }






  public void checkUserCredential(String terminalId, String userPass)
      throws DatabaseException,  SQLException{
    ResultSet result = query.executeQuery(
      "select * from post " +
      "inner join point_of_sale on post.point_of_sale_id = point_of_sale.id " +
      "inner join seller on seller.point_of_sale_id = point_of_sale.id " +
      "where seller.pin_code = '" +
      userPass +
      "' and post.terminal_id = '" +
      terminalId + "'");

    //      if (Service.logger.isDebugEnabled()) {
    //          Service.logger.debug("<--");
    //      }

    if (!result.next()) {
      throw new DatabaseException("No user of post = " + terminalId +
                                  " with pass = " + userPass);
    }
  }

  public TransactionData getTransactionData(String terminalId)
      throws DatabaseException, SQLException{
    ResultSet result = query.executeQuery(
      "select post.id as post_id, point.id as point_of_sale_id, point.company_address_ascii as address, " +
                "point.company_town_ascii as town, point.post_code as zip, " +
                "point.company_name as point_name, point.diners_merchant_id, firmware_update_version, " +
                "contractor.id as contractor_id, contractor.pcoo_merchant_id, contractor.mobtel_merchant_id," +
                "mcc.code as mcc_code, contractor.company_name as contractor_name, seller.id as seller_id, " +
                "additional_info_flag, set_announcement_flag, " +
                "seller.contact_name as seller_name, " +
                "post.post_terminal_id as ser_num, " +
                "cs_contractor_sifra_kupca, cs_is_in_debt, cs_contractor_valid, " +
                "post.change_counter as ch_counter " +
                "from post " +
                "inner join point_of_sale point on point.id = post.point_of_sale_id " +
                "inner join contractor on contractor.id = point.contractor_id " +
                "inner join seller on seller.point_of_sale_id = point.id " +
                "left join point_of_sale_mcc point_mcc on point_mcc.point_of_sale_id = point.id " +
                "left join mcc_code mcc on mcc.id = point_mcc.mcc_id " +
                "and mcc.code != '4814' " +
                "where post.terminal_id = '" + terminalId + "' " +
                "and contractor.valid != 0 and contractor.active != 0 " +
                "and point.valid != 0 and point.active != 0 " +
                "and post.valid != 0 and post.active != 0 "  );
        if (!result.next())
            throw new DatabaseException(
                    "Couldn't find relation post - point_of_sale - contractor");
        
        
            TransactionData transactionData = new TransactionData(result.getInt(
                    "seller_id"), result.getString("point_name"),
                    result.getInt("post_id"), result.getString("contractor_name"),
                    result.getLong("diners_merchant_id"),
                    result.getInt("point_of_sale_id"));
        transactionData.setPcooMerchantId(result.getInt("pcoo_merchant_id"));
        transactionData.setMobtelMerchantId(result.getInt("mobtel_merchant_id"));
        transactionData.setMccCode(result.getString("mcc_code"));
        transactionData.setIspMerchantId(result.getInt("contractor_id"));
        transactionData.setContractorId(result.getInt("contractor_id"));
        transactionData.setPointOfSaleAddress(result.getString("address"));
        transactionData.setPointOfSaleTown(result.getString("town"));
        transactionData.setPointOfSaleZip(result.getString("zip"));
        transactionData.setSellerName(result.getString("seller_name"));
        transactionData.setSerialNumber(result.getString("ser_num"));
        transactionData.setChangeCounter(result.getInt("ch_counter"));
        transactionData.set_additional_info_flag(result.getBoolean("additional_info_flag"));
        transactionData.set_annoincement_exist_flag(result.getBoolean("set_announcement_flag"));
        transactionData.set_firmware_update_version(result.getString("firmware_update_version"));
        transactionData.set_cs_contractor_sifra_kupca(result.getString("cs_contractor_sifra_kupca"));
        transactionData.set_is_cs_subcontractor_in_debt(result.getBoolean("cs_is_in_debt"));
        transactionData.set_is_cs_subcontractor_valid(result.getBoolean("cs_contractor_valid"));

  //      if (Service.logger.isDebugEnabled()) {
  //          Service.logger.debug("<-- ");
  //      }

        return transactionData;

  }
  
  public boolean checkCSSubconractorService(String cs_contractor_sifra_kupca, 
          int service_id) {
      try{
      ResultSet result = query.executeQuery("select id from contractor_service where "+
              "contractor_cs_contractor_sifra_kupca = '" +cs_contractor_sifra_kupca+ "' and " +
              " service_id = " + service_id);
      return result.next();
      }catch(SQLException sqle){
          if (Service.logger.isDebugEnabled()) {
              Service.logger.error("SQLException when checkCSSubconractorService, details: " +
                      sqle.getMessage());
          }
          return false;
      }
  }


/////////////////PrepaidDNIface impl////////////////////////////////////////////
  public int insertPlatformInvoke(int transactionId, int methodId,
                                    String paymentPhone) throws SQLException {
		String qryStr = "insert into platform_method_invokation (transaction_id, method_id, payment_phone) " +
		"values (" + transactionId + ", " + methodId +
		", '" + paymentPhone + "')";

        query.executeUpdate(qryStr);
        Logger.getInstance().logPlainMessage(qryStr);
        
        ResultSet result = query.executeQuery(
                "select id from platform_method_invokation where transaction_id = " +
                transactionId +
                " and payment_phone = '" + paymentPhone + "' order by id desc");
        result.next();


        return result.getInt("id");
    }

    public int insertPlatformInvoke(int transactionId, int methodId,
                                    String paymentPhone, double amount) throws
        SQLException {
    	String qryStr = "insert into platform_method_invokation (transaction_id, method_id, payment_phone, " +
        "amount) " +
        "values (" + transactionId + ", " + methodId +
        ", '" + paymentPhone + "', " + amount + ")";
      query.executeUpdate(qryStr);
      Logger.getInstance().logPlainMessage(qryStr);
      
      ResultSet result = query.executeQuery(
          "select id from platform_method_invokation where transaction_id = " +
          transactionId +
          " and payment_phone = '" + paymentPhone + "' and amount = " +
          amount + " order by id asc");
      result.next();
  return result.getInt("id");
    }






    public void updatePlatformTransactionId(int transactionId,
                                            int platformTransactionId) throws
            SQLException {

    	String qryStr = "update transaction set platform_transaction_id = " +
        platformTransactionId +
        " where id = " + transactionId;
        query.executeUpdate(qryStr);
        Logger.getInstance().logPlainMessage(qryStr);


    }

    public int insertPrepaid(int sellerId, int postId, double amount,
            Phone paymntPhone, int point_of_sale_id, MobileOperatorEnum mob_operator) throws
        SQLException {

      int mobileNetworkId = mob_operator.get_mob_operator_id_in_db();
//      if(MTStrueVIPfalse){
//          mobileNetworkId = 1;
//      }
//      else{
//          mobileNetworkId = 4;
//      }
     
//      if (paymentPhone.startsWith("+38164"))
//        mobileNetworkId = 1;
//      else if (paymentPhone.startsWith("+38163"))
//        mobileNetworkId = 2;
//      else if (paymentPhone.startsWith("+38162"))
//        mobileNetworkId = 3;
//      else if (paymentPhone.startsWith("+38160"))
//        mobileNetworkId = 4;
//      else if (paymentPhone.startsWith("+38161"))
//        mobileNetworkId = 5;
//      else if (paymentPhone.startsWith("+38165") || paymentPhone.startsWith("+38166"))
//        mobileNetworkId = 6;


      int transactionId = insertTransaction(sellerId, postId, amount,
                                            point_of_sale_id);
      //((DatabaseLoggerIface)Logger.getInstance()).logInsertTransaction(sellerId,postId,amount,point_of_sale_id,transactionId);
      String qryStr = "insert into payment (transaction_id, msisdn, mob_network_id) " +
      "values (" + transactionId + ", '" + paymntPhone.getFull(true) + "', " +
      mobileNetworkId + ")";
      query.executeUpdate(qryStr);
      Logger.getInstance().logPlainMessage(qryStr);

      //((DatabaseLoggerIface)Logger.getInstance()).logInsertPayment(transactionId,paymentPhone,mobileNetworkId);
      return transactionId;

    }

    private int insertTransaction(int sellerId, int postId, double amount, int point_of_sale_id) throws
            SQLException {
    	
    	int tra_id = getNextSequenceValue("transaction_id_seq");
        
        String queryStr = "";
        
      java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      String timestmp = sdf.format(new Date());
      
      


        if (sellerId == 0){
        	queryStr = "insert into transaction (id, post_id, status_id, amount, point_of_sale_id, stop_time, start_time) " +
            "values (" + tra_id + ", "+ postId + ", 1, " + amount + ", " + point_of_sale_id  + ", '"+ timestmp +"', '"+ timestmp +"')";
            query.executeUpdate(queryStr);
        }else{
        	queryStr = "insert into transaction (id, seller_id, post_id, status_id, amount, point_of_sale_id, stop_time, start_time) " +
            "values (" + tra_id + ", " + sellerId + ", " + postId + ", 1, " + amount +
            ", " + point_of_sale_id + ", '"+ timestmp +"', '"+ timestmp +"')";
            query.executeUpdate(queryStr);
        }
        
        Logger.getInstance().logPlainMessage(queryStr);
//        ResultSet result = query.executeQuery(
//                "select id from transaction where " +
//                "post_id = " + postId + " order by id desc");
//        result.next();


//        return result.getInt("id");
        return tra_id;
    }
    
    public void resetAnnouncementFlag(int pos_id) throws
    SQLException{
        query.executeUpdate("update point_of_sale set set_announcement_flag = false where id = " +
                pos_id);    
    }
    
    public boolean isKupacBlocked(int pos_id) throws SQLException{
        ResultSet result = query.executeQuery(
                "select is_in_debt from point_of_sale " +
                
                "where id = " + pos_id );

        if(result.next()){

          return result.getBoolean("is_in_debt");
        }else{
          return false;
        }
    }
    
    /**
     * Inserts data about Internet transaction into internet_transactions table. The table has the
     * following columns:
     * <br>
     * id | provider_code | transaction_id | status | amount | post_id | seller_id | point_of_sale_id | stop_time | contractor_id
     * <br><br>
     * @param transaction TransactionData
     * @param request OperationRequest
     * @throws SQLException
     */
    public int insertInternetTransaction(TransactionData transaction,
                                          InternetData internetData, double amount,
                                          String qPaySpotNumber) throws
            SQLException {

        if (Service.logger.isDebugEnabled()) {
            Service.logger.debug("--> transactionData = " + transaction +
                                 ", request = " + internetData);
        }

        int id = getNextSequenceValue("internet_transactions_id_seq");
        
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timestmp = sdf.format(new Date());
        
        String qryStr = "insert into internet_transactions (id, provider_code, transaction_id," +
        " status_id, amount, post_id, seller_id, point_of_sale_id, qpayspot_id, stop_time)" +
        " values(" + id + ",'" + internetData.getProviderCode() +
        "'," +
        transaction.getId() + "," +
        "1" + "," +
        amount + "," + transaction.getPostId() + "," +
        transaction.getSellerId() + "," + transaction.getPointOfSaleId() +
        "," + Long.parseLong(qPaySpotNumber) +
        ", '"+ timestmp +"')";

        query.executeUpdate(
                qryStr );
        
        Logger.getInstance().logPlainMessage(qryStr);

//            Logger.getInstance().logInsertInternet(internetData.getProviderCode(),
//                                                   transaction.getId(),
//                                                   amount, transaction.getPostId(),
//                                                   transaction.getSellerId(),
//                                                   transaction.getPointOfSaleId(),
//                                                   qPaySpotNumber);

        if (Service.logger.isDebugEnabled()) {
            Service.logger.debug("<--");
        }

        return id;

    }
    
    public void updateInternetTransactionProvTrans(int transactionId, String extTrId) throws
    SQLException{
        String qryStr = "update internet_transactions set transaction_id = '" + extTrId +
        "' where id = " + transactionId;
        query.executeUpdate(qryStr);
        Logger.getInstance().logPlainMessage(qryStr);
    }
    
    public void updateFirmwareVersion(int post_id, 
            String firmware_version){
        try{
         String  firm_ver = firmware_version;
         if(firmware_version.length() > "mPOS".length()){
             firm_ver = firmware_version.substring("mPOS".length()); 
         }

        query.executeUpdate("update post set firmware_version = '" +
                firm_ver +
                "' where id = " + post_id);
        }catch(SQLException sqle){
            if (Service.logger.isDebugEnabled()) {
                Service.logger.warn("SQLException when try to update firmware_version for post_id = " + 
                        post_id + ", details: " +
                        sqle.getMessage());
            }
        }
    }
    
    public void updateMobtel(int transactionId, String authIdentResponse,
            String responseCode, String telenor_transaction_id) throws
            SQLException {
        //        if (Service.logger.isDebugEnabled()) {
        //            Service.logger.debug("--> "/*transactionId = " +
        //                                 transactionId + " authIdentResponse = " +
        //                                 authIdentResponse
        //                                 + " responseCode = " + responseCode*/);

        //        }

        query.executeUpdate("update payment set response_code = '" +
                responseCode + "', " +
                "authorization_number = '" + authIdentResponse +
                "', telenor_transaction_id = '" +
                telenor_transaction_id +
                "' where transaction_id = " + transactionId);
        //        if (Service.logger.isDebugEnabled()) {
        //            Service.logger.debug("<--");
        //        }

    }
    



//////////////////////end PrepaidDBIface impl///////////////////////////////////

/////////////////CancelDBIface impl/////////////////////////////////////////////

    public int getTransactionId(int platformTransactionId, String terminalId,
            boolean isInternet) throws
            SQLException,DatabaseException {
        
        String qryStr = isInternet ? 
                "select id from internet_transactions where transaction_id = " + 
                + platformTransactionId
                :
                    "select transaction.id from transaction " +
                    "inner join post on post.id = transaction.post_id " +
                    "where platform_transaction_id = " + platformTransactionId +
                    " " +
                    "and terminal_id = '" + terminalId + "'";
                    


        ResultSet result = query.executeQuery(
                qryStr);

        if(result.next()){

          return result.getInt("id");
        }else{
          throw new DatabaseException("No transaction to cancel for platformTransactionId = " +
                                      platformTransactionId + " and terminal = " +
                                      terminalId);
        }
    }

    public void fillPrepaid(int transactionId, CancelRequest request) throws
            SQLException {
        
        if(request.isAbatel()){
            ResultSet result = query.executeQuery(
                    "select amount, status_id, qpayspot_id from internet_transactions " +
                    "where id = " + transactionId);
            result.next();
            request.setAmount(result.getDouble("amount"));
            request.setTransactionStatus(result.getInt("status_id"));
            request.setPhone(result.getString("qpayspot_id").substring(MobileOperatorEnum.ABATEL.get_transaction_prefix().length()));
        }else{

            fillTransaction(transactionId, request);
            fillPayment(transactionId, request);
        }

    }

    public String getTransactionTime(int transactionId, boolean isInternet) throws SQLException {
      String qry = isInternet ?
              "select stop_time from internet_transactions " +
              "where id = " + transactionId + ""
              :
                  "select stop_time from transaction " +
                  "where id = " + transactionId + "";
        
        ResultSet result = query.executeQuery(
                qry);
      result.next();


      return result.getString("stop_time");

    }
    
    public void insertVipMailStorno(String orig_tr_id, String transaction_id, String msisdn,
            double amount, String subject, String message) throws SQLException{
        
        String insertQuery = "insert into mail_cancel_vip_data (original_transaction_id," +
        "cancel_transaction_id, subject, amount, msisdn, message_text) values (" +
        "'"+orig_tr_id+"', '"+transaction_id+"', '"+subject+"', "+amount+", '"+msisdn+"', '"+message+"')";
        query.executeUpdate(
                insertQuery);
        
    }
    
    public String getIspTransactionID(int dbId) throws SQLException{
        ResultSet result = query.executeQuery("select transaction_id from internet_transactions " +
        "where id = " + dbId  );
        
        if(result.next()){
            return result.getString("transaction_id");
        }else{
            return "-1";
        }
    }
    
    public void fillPrepaidMobtel(int transactionId, CancelRequest request,
            TransactionData transaction,
            MobtelResponse oldResponse) throws
            SQLException {
        //       if (Service.logger.isDebugEnabled()) {
        //           Service.logger.debug("--> "/*transactionId = " + transactionId +
        //                                " request = " + request +
        //                                " transaction = " + transaction*/);
        //       }

        fillTransaction(transactionId, request);
        fillPayment(transactionId, request);
        //System.out.println("select contractor.mobtel_merchant_id, " +
        //                          "payment.authorization_number, payment.response_code " +
        //                          "from transaction " +
        //     "inner join post on post.id = transaction.post_id " +
        //                          "inner join point_of_sale point on point.id = post.point_of_sale_id " +
        //     "inner join contractor on contractor.id = point.contractor_id " +
        //     "inner join payment on transaction.id = payment.transaction_id " +
        //     "where transaction.id = " + transactionId);
        ResultSet result = query.executeQuery(
                "select contractor.mobtel_merchant_id, " +
                "payment.authorization_number, payment.response_code, payment.telenor_transaction_id " +
                "from transaction " +
                "inner join post on post.id = transaction.post_id " +
                "inner join point_of_sale point on point.id = post.point_of_sale_id " +
                "inner join contractor on contractor.id = point.contractor_id " +
                "inner join payment on transaction.id = payment.transaction_id " +
                "where transaction.id = " + transactionId);
        result.next();
        transaction.setMobtelMerchantId(result.getInt("mobtel_merchant_id"));
        oldResponse.setAuthIdentResponse(result.getString(
        "authorization_number"));
        oldResponse.setResponseCode(result.getString("response_code"));
        oldResponse.setTelenorTransactionId(result.getString("telenor_transaction_id"));
        //     if (Service.logger.isDebugEnabled()) {
        //         Service.logger.debug("<--");
        //     }
    }

/////////////////end CancelDBIface impl/////////////////////////////////////////


//////////////////////// LastTopUpIface impl////////////////////////////////////

    public LastTopUpRequest getLastTopUpData(int postId) throws DatabaseException, SQLException {

      ResultSet result = query.executeQuery(
          "select amount, platform_transaction_id, status_id, msisdn, stop_time, mob_network_id "+
          "from transaction, payment where transaction.id = payment.transaction_id " +
          "and post_id = " + postId + " order by transaction.id desc limit 1");

      if(result.next()){
        LastTopUpRequest request = new LastTopUpRequest();
        request.setAmount(result.getDouble("amount"));
        request.setTransactionStatus(result.getInt("status_id"));
        request.setPhone(result.getString("msisdn"));
        request.setTransactionId(result.getInt("platform_transaction_id"));
        request.setStopTime(result.getString("stop_time"));
        request.set_mobNetwork(result.getInt("mob_network_id"));
        return request;
      }else{
        throw new DatabaseException("No transactions for post = " + postId + " in database");
      }
    }

//////////////////////end LastTopUpIface impl///////////////////////////////////

    
////////////////////////ReportDBIface impl////////////////////////////////////

    public ReportData getDailyReportData(int post_id,
            String dateFrom, String dateTo) throws
            SQLException {

        ReportData reportData = new ReportData();


        String timeClause = "stop_time >= abstime(timestamp '"+dateFrom+"') and stop_time < abstime(timestamp '"+dateTo+"')";


        ResultSet prepaid = query.executeQuery(
                "select sum (amount), count(*), mob_operater_id, status_id from transaction " +
                "inner join payment on payment.transaction_id = transaction.id " +
                "inner join mob_network on mob_network.id = payment.mob_network_id " +
                "where post_id = " + post_id + " " +
                "and " + timeClause + " and status_id in (2, 4) " +
                "group by mob_operater_id, status_id" );

        while(prepaid.next()) {
            int currentMobOp = prepaid.getInt("mob_operater_id");
            int currentStatus_id = prepaid.getInt("status_id");
            //MTS
            if(currentMobOp == 1){
                if(currentStatus_id == 2 || currentStatus_id == 4){
                    reportData.add_telekom_sum((int)prepaid.getDouble("sum"));
                    reportData.add_telekom_count(prepaid.getInt("count"));
                }
                if(currentStatus_id == 4){
                    reportData.add_telekom_sum_cancel((int)prepaid.getDouble("sum"));
                    reportData.add_telekom_count_cancel(prepaid.getInt("count"));
                }
            }
            //vip
            if(currentMobOp == 3){
                if(currentStatus_id == 2 || currentStatus_id == 4){
                    reportData.add_vip_sum((int)prepaid.getDouble("sum"));
                    reportData.add_vip_count(prepaid.getInt("count"));
                }
                if(currentStatus_id == 4){
                    reportData.add_vip_sum_cancel((int)prepaid.getDouble("sum"));
                    reportData.add_vip_count_cancel(prepaid.getInt("count"));
                }
            }
            //telenor
            if(currentMobOp == 2){
                if(currentStatus_id == 2 || currentStatus_id == 4){
                    reportData.add_telenor_sum((int)prepaid.getDouble("sum"));
                    reportData.add_telenor_count(prepaid.getInt("count"));
                }
                if(currentStatus_id == 4){
                    reportData.add_telenor_sum_cancel((int)prepaid.getDouble("sum"));
                    reportData.add_telenor_count_cancel(prepaid.getInt("count"));
                }
            }
            //globaltel
            if(currentMobOp == 6){
                if(currentStatus_id == 2 || currentStatus_id == 4){
                    reportData.add_globaltel_sum((int)prepaid.getDouble("sum"));
                    reportData.add_globaltel_count(prepaid.getInt("count"));
                }
                if(currentStatus_id == 4){
                    reportData.add_globaltel_sum_cancel((int)prepaid.getDouble("sum"));
                    reportData.add_globaltel_count_cancel(prepaid.getInt("count"));
                }
            }
          //mundio
            if(currentMobOp == 5){
                if(currentStatus_id == 2 || currentStatus_id == 4){
                    reportData.add_mundio_sum((int)prepaid.getDouble("sum"));
                    reportData.add_mundio_count(prepaid.getInt("count"));
                }
                if(currentStatus_id == 4){
                    reportData.add_mundio_sum_cancel((int)prepaid.getDouble("sum"));
                    reportData.add_mundio_count_cancel(prepaid.getInt("count"));
                }
            }
            
            
        }
        
        prepaid.close();
        
        ResultSet internetData = query.executeQuery(
                "select sum (amount), count(*), provider_code, status_id from internet_transactions " +
                "where post_id = " + post_id + " " +
                "and " + timeClause + " and status_id in (2, 4) " +
                "group by provider_code, status_id" );

        while(internetData.next()) {
            String currentIntOper = internetData.getString("provider_code");
            int currentStatus_id = internetData.getInt("status_id");
            if(currentIntOper.equalsIgnoreCase("14")/*abatel*/){
                if(currentStatus_id == 2 || currentStatus_id == 4){
                    reportData.add_abatel_sum((int)internetData.getDouble("sum"));
                    reportData.add_abatel_count(internetData.getInt("count"));
                }
                if(currentStatus_id == 4){
                    reportData.add_abatel_sum_cancel((int)internetData.getDouble("sum"));
                    reportData.add_abatel_count_cancel(internetData.getInt("count"));
                }
            }
        }

        return reportData;
    }
//////////////////////end ReportDBIface impl///////////////////////////////////


    
    
//////////////////////start InvoiceIface impl///////////////////////////////////    
    
    
    public InvoiceData getInvoice(int pointId, Date invoiceDateFrom,
            Date invoiceDateTo) throws NoInvoiceDataException,
            SQLException{
        
        if (Service.logger.isDebugEnabled()) {
            Service.logger.info("--> pointId = " + pointId +
                                ", invoiceDateFrom = " +
                                _format.format(invoiceDateFrom) +
                                ", invoiceDateTo = " + _format.format(invoiceDateTo));
        }
        
        String datumOd = "";
        String datumDo = "";
        
        ResultSet resDatum = query.executeQuery("SELECT to_char(datum_fakture, 'yyyy-MM-dd'), "+
                "to_char(period_od, 'yyyy-MM-dd') as period_od, to_char(period_do, 'yyyy-MM-dd') as period_do "+
                "FROM processed_proforma_invoices ORDER BY datum_fakture DESC OFFSET 0 limit 1");
        
        while(resDatum.next()){
            datumOd = resDatum.getString("period_od");
            datumDo = resDatum.getString("period_do");
        }
        
        resDatum.close();


        boolean isSet = false;
        InvoiceData toReturn = new InvoiceData();
        
        
        int point_in_invoice = 1000000 + pointId;

        ResultSet result = query.executeQuery(
                "SELECT " +
                "distribucija_detalj.pointofsaleid, " +
                "distribucija_detalj.posime as posime, " +
                "distribucija_detalj.serviceid as serviceid, " +
                "distribucija_detalj.periodod as periodod, " +
                "distribucija_detalj.perioddo as perioddo, " +
                "distribucija_detalj.prometbruto as prometbruto, " +
                "distribucija_detalj.provizijaiznos as provizijaiznos, " +
                "fakture_uplate.poziv_na_broj as poziv_na_broj, " +
                "fakture_uplate.ukupan_iznos_fakture as ukupan_iznos_fakture, " +
                "fakture_uplate.tr_uplata as broj_racuna, " +
                "fakture_uplate.ukupan_iznos_bqv as ukupan_iznos_bqv, " +
                "distribucija_detalj.rokplacanja as rokplacanja " +
                "FROM " +
                "distribucija_detalj, " +
                "fakture_uplate " +
                "WHERE " +
                "fakture_uplate.broj_izvestaja = distribucija_detalj.broj_izvestaja " +
                "AND " +
                "distribucija_detalj.periodod = '"+datumOd/*_format.format(invoiceDateFrom)*/+"' " +
                "AND " +
                "distribucija_detalj.perioddo = '"+datumDo/*_format.format(invoiceDateTo)*/+"' " +
                "AND " +
                "distribucija_detalj.pointofsaleid = "+ point_in_invoice  );

            while(result.next()){
                isSet = true;
                toReturn.setFromDate(formatInvoiceDate(datumOd)/*invoiceDateFrom*/);
                toReturn.setToDate(formatInvoiceDate(datumDo)/*invoiceDateTo*/);

                if(result.getInt("serviceid") ==
                   ServiceData.SERVICE_PREPAID_DOPUNA_TELEKOM.getServiceId()){
                    toReturn.setMtsPart(result.getDouble("prometbruto"));
                }
                if(result.getInt("serviceid") ==
                   ServiceData.SERVICE_PREPAID_DOPUNA_MOBILKOM.getServiceId()){
                    toReturn.setVipPart(result.getDouble("prometbruto"));
                }
                if(result.getInt("serviceid") ==
                    ServiceData.SERVICE_PREPAID_DOPUNA_MOBTEL.getServiceId()){
                     toReturn.setTelenorPart(result.getDouble("prometbruto"));
                 }
                if(result.getInt("serviceid") ==
                    ServiceData.SERVICE_GLOBALTEL_DOPUNA.getServiceId()){
                     toReturn.set_globaltelPart(result.getDouble("prometbruto"));
                 }
                if(result.getInt("serviceid") ==
                    ServiceData.SERVICE_MUNDIO_DOPUNA.getServiceId()){
                     toReturn.set_mundioPart(result.getDouble("prometbruto"));
                 }
                if(result.getInt("serviceid") ==
                    ServiceData.SERVICE_PREPAID_DOPUNA_MOBTEL.getServiceId()){
                     toReturn.setTelenorPart(result.getDouble("prometbruto"));
                 }
                if(result.getInt("serviceid") ==
                    ServiceData.SERVICE_Q_PAY_SPOT_INTERNET.getServiceId() || 
                    result.getInt("serviceid") ==
                        ServiceData.SERVICE_INTERNET_ABATEL.getServiceId()){
                     toReturn.setInternet(result.getDouble("prometbruto"));
                 }
                if(result.getInt("serviceid") ==  
                    ServiceData.SERVICE_TERMINAL_RENT.getServiceId()
                ){
                    toReturn.set_terminalRentFee(result.getDouble("prometbruto"));
                }


                toReturn.setPozivNaBroj(result.getString("poziv_na_broj"));
                toReturn.setPosName(result.getString("posime"));
                toReturn.setToPayDate(result.getDate("rokplacanja"));
                toReturn.setForPayment(result.getDouble("ukupan_iznos_fakture"));
                toReturn.setForPaymentWithoutQV(result.getDouble("ukupan_iznos_bqv"));
                toReturn.setAccountNumber(result.getString("broj_racuna"));
            }
            if(isSet){
                if (Service.logger.isDebugEnabled()) {
                    Service.logger.info("<-- toReturn = " + toReturn);
                }
                return toReturn;
            }else{
                throw new NoInvoiceDataException(pointId,
                                                 _format.format(invoiceDateFrom),
                                                 _format.format(invoiceDateTo));
            }


    }
    
//////////////////////end InvoiceIface impl///////////////////////////////////// 
    
    
    ///////////////////pakistan fiscal iface start ////////////////////////////
    
    public int insertAuditData(String data, String terminalId, String clientSignature,
            String serverSiganture, int counter, int audit_counter, 
            int retry_counter, String broj_racuna,  String encripted_data, 
            String receipt_type, 
            String receipt_id) throws SQLException{
        Service.logger.info("data = " + data + 
                ", terminalId = " + terminalId +
                ", clientSignature = " + clientSignature +
                ", serverSiganture = " + serverSiganture +
                ", counter = " + counter +
                ", audit_counter = " + audit_counter +
                ", retry_counter = " + retry_counter +
                ", broj_racuna = " + broj_racuna +
                ", encripted_data = " + encripted_data +
                ", receipt_type = " + receipt_type +
                ", receipt_id = " + receipt_id);
        
        data = data.replace('\'', ' ');
        data = data.replace('\"', ' ');
        int id = getNextSequenceValue("audit_data_id_seq");
        String qryStr = 
        "insert into audit_data (id, audit_data, terminal_id, client_signature, server_signature, counter, "+
        "audot_counter, retry_counter, broj_racuna, encripted_data, receipt_type, receipt_id) " +
        "values (" + id + ", '" + data + "', '" +terminalId + "', '"+ clientSignature +"', '"+serverSiganture
        +"', "+counter+", "+audit_counter+", "+retry_counter+", '"+broj_racuna + "', '"+encripted_data +
        "', '"+receipt_type + "', '"+receipt_id+"')";

        Service.logger.info(qryStr);
        
        query.executeUpdate(qryStr);
        
        
        return id;
    }
    
    
    
    ///////////////////pakistan fiskal iface stop /////////////////////////////
    
    
    public boolean isAbatelPrepaidSynch(String msisdn, double amount, int postId){
        java.sql.Timestamp int_tra = null;
        try{
            ResultSet result = query.executeQuery(
                    "select stop_time from internet_transactions " +
                    "where qpayspot_id = 914" + msisdn + " and amount = " + amount + 
                    " and post_id = " + postId);
            if(result.next()){
                int_tra = result.getTimestamp("stop_time");
                result.close();
            }else{
                return false;
            }

            java.sql.Timestamp tra = null;
            ResultSet result_2 = query.executeQuery(
                    "select stop_time from transaction, payment " +
                    "where transaction.id = payment.transaction_id and msisdn = '" + 
                    Phone.parse(msisdn).getFull(true) + "' and amount = " + amount +
                    " and post_id = " + postId);

            if(result_2.next()){
                tra = result_2.getTimestamp("stop_time");
                return int_tra.after(tra);
            }else{
                return true;
            }
        }catch(SQLException sqle){
            if (Service.logger.isDebugEnabled()) {
                Service.logger.error("SQLException when try to check isAbatel in SYNC prepaid request"
                        + ", details = " + sqle.getMessage(),sqle);

            }
            return false;
        }
        

    }
    

    
    
    
    
    private void fillTransaction(int transactionId, CancelRequest request) throws
            SQLException {


        ResultSet result = query.executeQuery(
                "select amount, status_id from transaction " +
                "where id = " + transactionId);
        result.next();
        request.setAmount(result.getDouble("amount"));
        request.setTransactionStatus(result.getInt("status_id"));

    }

    private void fillPayment(int transactionId, CancelRequest request) throws
            SQLException {

        ResultSet result = query.executeQuery("select msisdn from payment " +
                                              "inner join transaction on payment.transaction_id = transaction.id where transaction.id = " +
                                              transactionId);
        result.next();
        request.setPhone(result.getString("msisdn"));


    }
    
    private static String formatInvoiceDate(String date){
        String[] yyyymmdd = date.split("-");
        
        return yyyymmdd[2] + "." + yyyymmdd[1] + "." + yyyymmdd[0] + ".";
    }
    
    private synchronized int getNextSequenceValue(String sequenceName) throws SQLException{
        ResultSet newseqvalue = query.executeQuery("SELECT nextval ('"+sequenceName+"')");
        newseqvalue.next();
        return newseqvalue.getInt("nextval");
    }


    public static void main(String[] args) throws Exception{
        DatabaseAgent agent = new DatabaseAgent();
        
        String data = "H,11111113,MRC0123456,1,999999999,130218124513" +
"A,\"Coca cola\",130.00,1.000,2,130.00" +
"A,\"Fanta\",120.00,1.000,1,120.00" +
"A,\"Rosa voda\",100.00,1.000,2,100.00" +
"A,\"Djus\",120.00,1.000,0,120.00" +
"L,123345567783527";
        data = data.replace('\'', ' ');
        String terminalId = "OFDC00A123456";
        String clientSignature = "GEZDGMZUGU2TMNZXHAZTKMRXGEZDGMZUGU2TMNZXHAZTKMRXGM2A====";
        String serverSiganture = "";//new String(HmacSha1Signature.calculateRFC2104HMAC("key", data));
        int counter = 0;
        int audit_counter = 0;
        int retry_counter = 0; 
        String broj_racuna = "256";  
        String encripted_data = "ZMpLAek9EIhjvg+UVk8Ftw==";
        
        String receipt_type = "H";
        String receipt_id = "11111113";
        
        
        agent.insertAuditData(data, terminalId, clientSignature,
                serverSiganture, counter, audit_counter, 
                retry_counter, broj_racuna,  encripted_data, receipt_type, receipt_id);
        
        
        if (true) System.exit(0);
//      DatabaseAgent agent = new DatabaseAgent("//192.168.0.6:5432/invoice_data_cs",
//              "postgres","postgres");
//      
      System.out.println(formatInvoiceDate("2012-05-31"));
//      System.out.println(
//              agent.getInvoice(12853,new java.util.Date(), new java.util.Date()));
//      
      
      agent.resetAdditInfoFlag(4424);  
      System.out.println("getBalance");
      BalanceData balance = agent.getBalance(4424);
      System.out.println(balance);

      
      try{
          System.out.println("getLastMessage");
      System.out.println(agent.getLastMessage("354687000693685"));
      }catch(Exception e){
          
      }
      System.out.println("insertPlatformInvoke");
      System.out.println(agent.insertPlatformInvoke(153194323, 5,
              153194323));
      System.out.println("insertPlatformInvoke");
      System.out.println(agent.insertPlatformInvoke(153194323, 5,
              "381648822005", 100,107592078));
      System.out.println("getValidServices");
      System.out.println(agent.getValidServices("354687000693685").size());
      System.out.println("getLastPosTransactionData");
      System.out.println(agent.getLastPosTransactionData(4424, false));
      System.out.println("checkUserCredential");
      try{
      agent.checkUserCredential("354687000693685","00009999");
 }catch(Exception e){
          
      }
 System.out.println("getTransactionData");
      System.out.println(agent.getTransactionData("354687000743076"));
      System.out.println("getLastPosTransactionData");
      System.out.println(agent.getLastPosTransactionData(6747,true));
      System.out.println("getMTSCancelTRId");
      System.out.println(agent.getMTSCancelTRId(153151416));
      System.out.println("checkCSSubconractorService");
      System.out.println(agent.checkCSSubconractorService("111111", 
              1));
      System.out.println("insertPlatformInvoke");
      System.out.println(agent.insertPlatformInvoke(153194323, 5,
              "381648822005"));
      System.out.println("insertPlatformInvoke");
      System.out.println(agent.insertPlatformInvoke(153194323, 5,
              "381648822005", 100));
      System.out.println("updatePlatformTransactionId");
      agent.updatePlatformTransactionId(153194323,
              111111);
      System.out.println("insertPrepaid");
      System.out.println(agent.insertPrepaid(65831, 4424, 100,
              Phone.parse("381648822005"), 4424, MobileOperatorEnum.MTS));
      System.out.println("resetAnnouncementFlag");
      agent.resetAnnouncementFlag(4424);
      System.out.println("isKupacBlocked");
      System.out.println(agent.isKupacBlocked(4424));
      System.out.println("getTransactionId");
      try{
      System.out.println(agent.getTransactionId(197076724, 
              "354687000692448", false));
      }catch(Exception e){
          
      }
      System.out.println("fillPrepaid");
      try{
      agent.fillPrepaid(8710, 
              new CancelRequest() );
}catch(Exception e){
          
      }
      System.out.println("getTransactionTime");
      System.out.println(agent.getTransactionTime(153151416, false) );
      System.out.println("insertVipMailStorno");
      agent.insertVipMailStorno("107592076", "107592077", 
              "381648822005",100, "Subject", "message");
      System.out.println("getLastTopUpData");
      System.out.println(agent.getLastTopUpData(4424));
      System.out.println("getDailyReportData");
      System.out.println(agent.getDailyReportData(4424,
              "2012-12-01", "2012-12-20"));
      System.out.println("isAbatelPrepaidSynch");
        System.out.println(agent.isAbatelPrepaidSynch("0648822005", 100, 1));
        System.out.println("isAbatelPrepaidSynch");
        System.out.println(agent.isAbatelPrepaidSynch("0611099122", 50, 6747));
      
      agent.close();
      
    }






}
