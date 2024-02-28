package yu.co.certus.pos.lanus;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import yu.co.certus.pos.lanus.message.*;
import yu.co.certus.pos.lanus.service.Service;
import yu.co.certus.pos.lanus.data.*;
import yu.co.certus.pos.lanus.data.impl.DatabaseAgent;

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
public class ProcessorFactory {
  public ProcessorFactory() {
  }

  public LoginProcessor getLoginProcessor(String terminalId) throws
      PosException {
    try {
      return new LoginProcessor( terminalId);
    }
    catch (Throwable e) {
      throw new PosException(
          "Exception when try to open login database connection, details:" +
          e.getMessage());
    }
  }

  public OperationProcessor getTransactionProcessor(AbstractRequest request,
                                         
                                         String terminalId) throws PosException {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.info("--> request = " + request + ", terminalId = " + terminalId );
      }
      DatabaseAgent agent = null;
      try{
          
          agent =  new DatabaseAgent();
          boolean isBlockable = agent.isTerminalBlockable(terminalId);
          TransactionData tData = getTransactionData(agent, terminalId);
//          if(request instanceof PrepaidRequest){
//              return new PrepaidProcessor(agent, terminalId, isBlockable, tData);
//          }
//          if (request instanceof CancelRequest) {
//              return new CancelProcessor(agent, terminalId, tData, false);
//          }
//          if (request instanceof RetryRequest) {
//              return new RetryProcessor(terminalId, isBlockable, tData,agent);
//          }
//          if(request instanceof ReportRequest){
//              return new ReportProcessor(terminalId,  tData, agent);
//          }
          if (request instanceof AnnouncementRequest) {
              agent.close();
              agent =  getInvoiceDBAgent();
              return new AnnouncementProcessor( terminalId,tData, agent);
          }

          throw new PosException(
                  "Cannot find processor for request "+ request +
                  ", processing currently not supported:" );
      }catch(DatabaseException de){
          if (Service.logger.isDebugEnabled()) {
              Service.logger.error("DatabaseException when try to open database connection, details: " 
                      + de.getMessage(), de );
          }
          throw new PosException(
                  "DatabaseException when try to open database connection, details:" +
                  de.getMessage());
             
      }
      catch(SQLException sqle){
          if (Service.logger.isDebugEnabled()) {
              Service.logger.error("SQLException when try to open database connection or check is blockable or get transactionData, details: " 
                      + sqle.getMessage(), sqle );
          }
          throw new PosException(
                  "SQLException when try to open database connection or check is blockable or get transactionData, details:" +
                  sqle.getMessage());
             
      }
      catch(Throwable e){
          if (Service.logger.isDebugEnabled()) {
              Service.logger.error("Unknown exception when try to create processor, details: " 
                      + e.getMessage(), e );
          }
          throw new PosException(
                  "Unknown exception when try to create processor, details: " 
                      + e.getMessage());
             
      }
      //prepaid
//      if (request instanceof PrepaidRequest) {
//        try {
//          PrepaidDBIface dbIface = new DBFactory().getPrepaidDBIface();
//          boolean isBlockable = dbIface.isTerminalBlockable(terminalId);
//          
//          return new PrepaidProcessor(dbIface, terminalId, isBlockable, 
//                  getTransactionData(dbIface, terminalId));
//        }
//        catch (Throwable e) {
//          throw new PosException(
//              "Exception when try to open prepaid database connection, details:" +
//              e.getMessage());
//        } //end trycatch
//      }
//      else {
//        if (request instanceof CancelRequest) {
//          try {
//              CancelDBIface dbIface = new DBFactory().getCancelDBIface();
//              return new CancelProcessor(dbIface,
//                      terminalId,
//                      getTransactionData(dbIface, terminalId), false);
//          }
//          catch (Throwable e) {
//            throw new PosException(
//                "Exception when try to open cancel database connection, details:" +
//                e.getMessage());
//          } //end trycatch
//        }
//        else {
//          if (request instanceof SaveParamRequest) {
//            try {
//
//              return new SaveParamProcessor(terminalId,
//                                            new DBFactory().getBaseDBIface());
//            }
//            catch (Throwable e) {
//              throw new PosException(
//                  "Exception when try to open cancel database connection, details:" +
//                  e.getMessage());
//            } //end trycatch
//          }
//          else {
//            if (request instanceof ReadParamRequest) {
//              try {
//
//                return new ReadParamProcessor(terminalId,
//                                              new DBFactory().getBaseDBIface());
//              }
//              catch (Throwable e) {
//                throw new PosException(
//                    "Exception when try to open cancel database connection, details:" +
//                    e.getMessage());
//              } //end trycatch
//            }
//            else {
//              if (request instanceof RetryRequest) {
//                try {
//                  DatabaseAgent agent = new DatabaseAgent();
//                  boolean isBlockable = agent.isTerminalBlockable(terminalId);
//                  return new RetryProcessor(terminalId, isBlockable, getTransactionData(agent, terminalId),
//                                            agent);
//                }
//                catch (Throwable e) {
//                  throw new PosException(
//                      "Exception when try to open cancel database connection, details:" +
//                      e.getMessage());
//                } //end trycatch
//              }
//              else {
//
//                if (request instanceof LastTopUpRequest) {
//                  try {
//                    LastTopUpIface agent = new DBFactory().getLastTopUpDBIface();
//                    return new LastTopUpProcessor(terminalId,agent,
//                            getTransactionData(agent, terminalId));
//                  }
//                  catch (Throwable e) {
//                    throw new PosException(
//                        "Exception when try to open cancel database connection, details:" +
//                        e.getMessage());
//                  } //end trycatch
//                }
//                else {
//                    if(request instanceof ReportRequest){
//                        try {
//                            DatabaseAgent agent = new DatabaseAgent();
//
//                            return new ReportProcessor(terminalId,  getTransactionData(agent, terminalId),
//                                                      agent);
//                          }
//                          catch (Throwable e) {
//                            throw new PosException(
//                                "Exception when try to open cancel database connection, details:" +
//                                e.getMessage());
//                          } //end trycatch
//                    }else{
//                        if (request instanceof AnnouncementRequest) {
//                            try {
//                                BaseDBIface dbIface = new DBFactory().getBaseDBIface();
//                                return new AnnouncementProcessor(
//                                        terminalId,
//                                        getTransactionData(dbIface, terminalId), dbIface);
//                            }
//                            catch (Throwable e) {
//                              throw new PosException(
//                                  "Exception when try to open cancel database connection, details:" +
//                                  e.getMessage());
//                            } //end trycatch
//                          }
//                    }
//
//                  return null;
//                }
//              }
//            }
//          }
//        }
//      }
 //   }
  }
  
  
  private DatabaseAgent getInvoiceDBAgent() throws DatabaseException, SQLException{
      Properties properties = new Properties();
      try {
        FileInputStream stream = new FileInputStream(
            "application.properties");
        properties.load(stream);
        stream.close();
      }
      catch (IOException e) {
        throw new DatabaseException(
            "Couldn't find or read configuration file");
      }
      return new DatabaseAgent(          
              properties.getProperty(
      "Invoice.Database.database"),
      properties.getProperty(
          "Invoice.Database.username"),
      properties.getProperty(
          "Invoice.Database.password"));
  }
  
  
  public static TransactionData getTransactionData(BaseDBIface dbIface, String terminalID) 
  throws SQLException{
      
  
      try{
      return dbIface.getTransactionData(terminalID);
      }catch(DatabaseException de){
          if (Service.logger.isDebugEnabled()) {
              Service.logger.error(
                  "DatabaseException when try to get transactionData for terminalID = " 
                      + terminalID +
                  " , details: " + de.getMessage(), de);
            }
          return null;
      }
  }
  

}
