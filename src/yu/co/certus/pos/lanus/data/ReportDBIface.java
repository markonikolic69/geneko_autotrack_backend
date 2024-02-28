package yu.co.certus.pos.lanus.data;

import java.sql.SQLException;

public interface ReportDBIface extends BaseDBIface{
    
    
    public ReportData getDailyReportData(int post_id,
            String dateFrom, String dateTo) throws
            SQLException;

}
