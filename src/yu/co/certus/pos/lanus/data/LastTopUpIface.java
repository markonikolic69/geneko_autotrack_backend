package yu.co.certus.pos.lanus.data;


import yu.co.certus.pos.lanus.message.LastTopUpRequest;

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
public interface LastTopUpIface extends BaseDBIface{

  public LastTopUpRequest getLastTopUpData(int postId) throws DatabaseException,
      SQLException;
  
  public int getMTSCancelTRId(int transactionID) throws DatabaseException, SQLException;
}
