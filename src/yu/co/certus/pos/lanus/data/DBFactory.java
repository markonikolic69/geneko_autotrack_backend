package yu.co.certus.pos.lanus.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

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
public class DBFactory {
  public DBFactory() {
  }

  public PrepaidDBIface getPrepaidDBIface() throws SQLException, DatabaseException{
    return new DatabaseAgent();
  }

  public CancelDBIface getCancelDBIface() throws SQLException, DatabaseException{
    return new DatabaseAgent();
  }

  public BaseDBIface getBaseDBIface() throws SQLException, DatabaseException{
      return new DatabaseAgent();
  }

  public LastTopUpIface getLastTopUpDBIface() throws SQLException, DatabaseException{
      return new DatabaseAgent();
  }
  
  public PakistanFiskalDBIface getPakistanDBIface() throws SQLException, DatabaseException{
      return new DatabaseAgent();
  }
  
  public InvoiceIface getInvoiceDBIface() throws SQLException, DatabaseException{
      
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
  
  
  



}
