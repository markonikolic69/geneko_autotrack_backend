package yu.co.certus.pos.lanus.data;

import java.sql.SQLException;

public interface PakistanFiskalDBIface {
    
    public void close() throws SQLException;
    
    public int insertAuditData(String data, String terminalId, String clientSignature,
            String serverSiganture, int counter, int audit_counter, 
            int retry_counter, String broj_racuna, String encripted_data, String receipt_type, 
            String receipt_id) throws SQLException;

}
