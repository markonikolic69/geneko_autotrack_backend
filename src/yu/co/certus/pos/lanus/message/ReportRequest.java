package yu.co.certus.pos.lanus.message;

public class ReportRequest extends AbstractRequest {
    
    
    private boolean _isV_REPORT = false;
    
    
    
    public boolean is_isV_REPORT() {
        return _isV_REPORT;
    }


    public void set_isV_REPORT(boolean _isv_report) {
        _isV_REPORT = _isv_report;
    }


    public String getDate(){
        return getParam(ReportRequestCommEnum.DATE_KEY);
        
    }
    
    
    public String toString(){
        return "REPORT REQUEST: DATE = "+ getDate() + ", firm_ver = " + getVersion();
    }
    
    
    
    

}
