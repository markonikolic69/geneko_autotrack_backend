package yu.co.certus.pos.lanus.data.impl;

import java.sql.DriverManager;
import java.io.IOException;
import java.util.Properties;
import java.sql.Statement;
import java.io.FileInputStream;
import java.sql.SQLException;
import yu.co.certus.pos.lanus.data.DatabaseException;
import yu.co.certus.pos.lanus.util.Logger;
import yu.co.certus.pos.lanus.util.InternetData;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author marko.nikolic@certus.co.yu
 * @version 1.0
 */
public class AbstractDatabaseAgent {

  protected Connection connection;
  protected Statement query;
  public AbstractDatabaseAgent() throws DatabaseException, SQLException {

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
    init(
        properties.getProperty(
            "Database.database"),
        properties.getProperty(
            "Database.username"),
        properties.getProperty(
            "Database.password"));
    connection.setAutoCommit(true);
  }

  public AbstractDatabaseAgent(String database, String user, String pass) throws
      DatabaseException, SQLException {
    init(database, user, pass);
  }

  private void init(String database, String user, String pass) throws
      DatabaseException, SQLException {
    try {
      Class.forName("org.postgresql.Driver");
    }
    catch (ClassNotFoundException e) {
      throw new DatabaseException(
          "Couldn't find a postgresql driver. Probably missing a classpath.");
    }

    connection = DriverManager.getConnection("jdbc:postgresql:" +
                                             database,
                                             user,
                                             pass);
    createStatement();

  }

  private void createStatement() throws SQLException {
    query = connection.createStatement();
  }

  public void close() throws SQLException {
    connection.close();
  }

  public boolean isTerminalBlocked( String terminalId) throws SQLException{
        ResultSet result = query.executeQuery(
                "select is_blocked from post where terminal_id = '" + terminalId + "'");
            
            if(result.next()){
                return result.getBoolean("is_blocked");
            }else{
                return false;
            }
        
    }


    public boolean isTerminalBlockable( String terminalId) throws SQLException{
        ResultSet result = query.executeQuery(
                "select block_able from post where terminal_id = '" + terminalId + "'");
            if(result.next()){
        return result.getBoolean("block_able");
            }else{
                return false;
            }

    }

    public void blockTerminal( String terminalId) throws SQLException{
        query.executeUpdate(
                "update post set is_blocked = true where terminal_id = '"
                + terminalId + "'");

    }

    public void insertResponse(String terminalId, String message) throws
            SQLException {


        query.executeUpdate("update post set last_message = '" + message +
                            "' where terminal_id = '" + terminalId+"'");

    }

    public void updateBalance(int terminalId, int amount) throws SQLException {


        query.executeUpdate("update post set daily_balance = daily_balance + " +
                            amount + ", " +
                            "weekly_balance = weekly_balance + " + amount +
                            ", " +
                            "monthly_balance = monthly_balance + " + amount +
                            " " +
                            "where id = " + terminalId);


    }

    public void updateTransactionStatus(int transactionId, int statusId) throws
            SQLException {

    	String qryStr = "update transaction set status_id = " + statusId +
        " where id = " + transactionId;
        query.executeUpdate(qryStr);
        Logger.getInstance().logPlainMessage(qryStr);


    }
    
    public void updateInternetTransactionStatus(int transactionId, int statusId) throws
    SQLException{
        String qryStr = "update internet_transactions set status_id = " + statusId +
        " where id = " + transactionId;
        query.executeUpdate(qryStr);
        Logger.getInstance().logPlainMessage(qryStr);
    }
    
    //INTERNET
    public void setProviderData(InternetData internetData) throws SQLException {





        ResultSet result = query.executeQuery("select * from internet_provider where code = '"+
                internetData.getProviderCode()+"'");

        if (result.next()) {
            internetData.setProviderName( result.getString("name"));
            internetData.setProviderUrl( result.getString("url"));
            internetData.setProviderUser( result.getString("username"));
            internetData.setProviderPassword( result.getString("passwd"));
            internetData.setConnectionTimeout(new Integer(result.getInt("connection_timeout")).intValue());
        }else{
            throw new SQLException("NO registered internet provider with code = " + internetData.getProviderCode());
        }
        
    }





}
