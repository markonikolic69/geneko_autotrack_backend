package yu.co.certus.pos.lanus.data;


import java.sql.SQLException;
import java.util.List;

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
public interface BaseDBIface {

    public void close() throws SQLException;

    public boolean isTerminalBlocked(String terminalId) throws SQLException;

    public boolean isTerminalBlockable(String terminalId) throws SQLException;

    public void blockTerminal(String terminalId) throws SQLException;

    public void insertResponse(String terminalId, String message) throws
    SQLException;

    public void updateBalance(int terminalId, int amount) throws SQLException;

    public void updateTransactionStatus(int transactionId, int statusId) throws
    SQLException;
    
    public void updateInternetTransactionStatus(int transactionId, int statusId) throws
    SQLException;

    public BalanceData getBalance(int post_id) throws SQLException;

    public int insertPlatformInvoke(int transactionId, int methodId,
            int reversalTransactionId ) throws SQLException;

    public int insertPlatformInvoke(int transactionId, int methodId,
            String paymentPhone, double amount,
            int reversalTransactionId) throws
            SQLException;

    public List getValidServices(String terminalId) throws SQLException;

    public String getLastMessage(String terminal_id) throws SQLException;

    public LastPosTransactionData getLastPosTransactionData(int postId, boolean isAbatel) throws SQLException;

    public void checkUserCredential(String terminalId, String userPass)
    throws DatabaseException,  SQLException;

    public TransactionData getTransactionData(String terminalId)
    throws DatabaseException, SQLException;
    
    public void resetAdditInfoFlag(int pos_id)
    throws DatabaseException, SQLException;
    
    
    public void setProviderData(InternetData internetData) throws SQLException;

}
