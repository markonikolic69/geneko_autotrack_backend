package yu.co.certus.pos.lanus.message;

import java.text.DecimalFormat;


public class ReportResponse extends AbstractResponse {
    
    private String _firm_ver = "";
    

    
    public String get_firm_ver() {
        return _firm_ver;
    }

    public void set_firm_ver(String _firm_ver) {
        this._firm_ver = _firm_ver;
    }






//    private boolean _midAdded = false;
//    
//    private boolean _telekom_count_added = false;
//    private boolean _telekom_storno_count_added = false;
//    private boolean _vip_count_added = false;
//    private boolean _vip_storno_count_added = false;
//    private boolean _telenor_count_added = false;
//    private boolean _telenor_storno_count_added = false;
//    private boolean _abatel_count_added = false;
//    private boolean _abatel_storno_count_added = false;
//    
//    private boolean _telekom_sum_added = false;
//    private boolean _telekom_storno_sum_added = false;
//    private boolean _vip_sum_added = false;
//    private boolean _vip_storno_sum_added = false;
//    private boolean _telenor_sum_added = false;
//    private boolean _telenor_storno_sum_added = false;
//    private boolean _abatel_sum_added = false;
//    private boolean _abatel_storno_sum_added = false;
//    
//    private int _telekom_count = 0;
//    private int _telekom_storno_count = 0;
//    private int _vip_count = 0;
//    private int _vip_storno_count = 0;
//    private int _telenor_count = 0;
//    private int _telenor_storno_count = 0;
//    private int _abatel_count = 0;
//    private int _abatel_storno_count = 0;
//    
//    private int _telekom_sum = 0;
//    private int _telekom_storno_sum = 0;
//    private int _vip_sum = 0;
//    private int _vip_storno_sum = 0;
//    private int _telenor_sum = 0;
//    private int _telenor_storno_sum = 0;
//    private int _abatel_sum = 0;
//    private int _abatel_storno_sum = 0;
//    
//    private String _mID = "";
    
    
    public void addmID(String mid){
        addParameter("mID", mid);
        //_midAdded = true;
      }
    
    public void addTelekomCount(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
            addParameter(CNT_TELEKOM_TR_V21,"" + cnt);
        }else{
            addParameter(CNT_TELEKOM_TR_V22,"" + cnt);   
        }
        //_telekom_count_added = true;
    }
    
    public void addTelekomStornoCount(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(CNT_TELEKOM_STORNO_TR_V21,"" + cnt);
        }else{
            addParameter(CNT_TELEKOM_STORNO_TR_V22,"" + cnt);   
        }
        //_telekom_storno_count_added = true;
    }
    
    public void addVipCount(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(CNT_VIP_TR_V21,"" + cnt);
        }else{
            addParameter(CNT_VIP_TR_V22,"" + cnt); 
        }
        //_vip_count_added = true;
    }
    
    public void addVipStornoCount(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(CNT_VIP_STORNO_TR_V21,"" + cnt);
        }else{
            addParameter(CNT_VIP_STORNO_TR_V22,"" + cnt);
        }
        //_vip_storno_count_added = true;
    }
    
    public void addAbatelCount(int cnt, boolean isV_REPORT){
        
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            if(isV_REPORT){
                addParameter(CNT_ABATEL_TR_V25,"" + cnt);
            }
        }else{
            if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
                addParameter(CNT_ABATEL_TR_V22,"" + cnt);
            }
        }
        
        //_abatel_count_added = true;
    }
    
    public void addAbatelStornoCount(int cnt, boolean isV_REPORT){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            if(isV_REPORT){
                addParameter(CNT_ABATEL_STORNO_TR_V25,"" + cnt);
            }
        }else{
            if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
                addParameter(CNT_ABATEL_STORNO_TR_V22,"" + cnt);
            }
        }
        
        //_abatel_storno_count_added = true;
    }
    
    public void addTelenorCount(int cnt){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(CNT_TELENOR_TR_V22,"" + cnt);
        }
        }
        //_telenor_count_added = true;
    }
    
    public void addTelenorStornoCount(int cnt){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(CNT_TELENOR_STORNO_TR_V22,"" + cnt);
        }
        }
        //_telenor_storno_count_added = true;
    }
    
    
    public void addTelekomSum(int sum, int storno_sum){
        int res_sum = _firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5) ? sum - storno_sum : sum;
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(SUM_TELEKOM_TR_V21,new DecimalFormat("#0.00").format( res_sum ));
        }else{
            addParameter(SUM_TELEKOM_TR_V22,new DecimalFormat("#0.00").format( res_sum ));  
        }
        //_telekom_sum_added = true;
    }
    
    public void addTelekomStornoSum(int sum){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
                addParameter(SUM_TELEKOM_STORNO_TR_V21,new DecimalFormat("#0.00").format( sum ));
            }else{
                addParameter(SUM_TELEKOM_STORNO_TR_V22,new DecimalFormat("#0.00").format( sum ));  
            }
        }
        //_telekom_storno_sum_added = true;
    }
    
    public void addVipSum(int sum, int storno_sum){
        int res_sum = _firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5) ? sum - storno_sum : sum;
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(SUM_VIP_TR_V21,new DecimalFormat("#0.00").format(res_sum));
        }else{
            addParameter(SUM_VIP_TR_V22,new DecimalFormat("#0.00").format(res_sum));
        }
        //_vip_sum_added = true;
    }
    
    public void addVipStornoSum(int sum){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
                addParameter(SUM_VIP_STORNO_TR_V21,new DecimalFormat("#0.00").format(sum));
            }else{
                addParameter(SUM_VIP_STORNO_TR_V22,new DecimalFormat("#0.00").format(sum));
            }
        }
        //_vip_storno_sum_added = true;
    }
    
    public void addTelenorSum(int sum){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(SUM_TELENOR_TR_V22,new DecimalFormat("#0.00").format( sum ));
        }
        }
        //_telenor_sum_added = true;
    }
    
    public void addTelenorStornoSum(int sum){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
        if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
        addParameter(SUM_TELENOR_STORNO_TR_V22,new DecimalFormat("#0.00").format( sum ));
        }
        }
        //_telenor_storno_sum_added = true;
    }

    
    public void addAbatelSum(int sum, int storno_sum,boolean isV_REPORT){
        
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            if(isV_REPORT){
                addParameter(SUM_ABATEL_TR_V25,new DecimalFormat("#0.00").format( sum - storno_sum ));
            }
        }else{
            if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
                addParameter(SUM_ABATEL_TR_V22,new DecimalFormat("#0.00").format( sum ));
            }
        }

        //_abatel_sum_added = true;
    }
    
    public void addAbatelStornoSum(int sum, boolean isV_REPORT){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            if(isV_REPORT){
                //addParameter(SUM_ABATEL_STORNO_TR_V25,new DecimalFormat("#0.00").format( sum ));
            }
        }else{
            if(!_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_1)){
                addParameter(SUM_ABATEL_STORNO_TR_V22,new DecimalFormat("#0.00").format( sum ));
            }
        }
        //_abatel_storno_sum_added = true;
    }
    
    
    public void addGlobaltelCoint(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            addParameter(CNT_GLOBALTEL_TR_V25,"" + cnt);
        }
    }
    
    public void addGlobaltelStornoCount(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            addParameter(CNT_GLOBALTEL_STORNO_TR_V25,"" + cnt);
        }
        //_telekom_storno_count_added = true;
    }

    public void addGlobaltelSum(int sum){

        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
        addParameter(SUM_GLOBALTEL_TR_V25,new DecimalFormat("#0.00").format( sum ));
        }
        //_telenor_sum_added = true;
    }
    
    
    public void addVectoneCoint(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            addParameter(CNT_VECTONE_TR_V25,"" + cnt);
        }
    }
    
    public void addVectoneStornoCount(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            addParameter(CNT_VECTONE_STORNO_TR_V25,"" + cnt);
        }
        //_telekom_storno_count_added = true;
    }

    public void addVectoneSum(int sum){

        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
        addParameter(SUM_VECTONE_TR_V25,new DecimalFormat("#0.00").format( sum ));
        }
        //_telenor_sum_added = true;
    }
    
    public void addInocallCoint(int cnt){
        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
            addParameter(CNT_INOCALL_TR_V25,"" + cnt);
        }
    }
    
    public void addInocallSum(int sum){

        if(_firm_ver.equalsIgnoreCase(FIRMWARE_VERSION_2_5)){
        addParameter(SUM_INOCALL_TR_V25,new DecimalFormat("#0.00").format( sum ));
        }
        //_telenor_sum_added = true;
    }
    
    
    
    
    public static final String REPORT_CREATED_SUCCESS = "18 Report created";
    public static final String MPOS_NOT_REGISTERED_ERROR = "28 mPOS is not registered";
    public static final String WRONG_DATE_FORMAT_ERROR = "38 Wrong DateTime format";
    public static final String SYSTEM_FAILURE_ERROR = "72 System failure";
    
    
    public static void main(String[] args){
        System.out.println(new DecimalFormat("#0.00").format(20.3));
    }
    
    
    private static final String CNT_TELEKOM_TR_V21 = "COUNT_TELEKOM_TR";
    private static final String CNT_TELEKOM_TR_V22 = "C1TR";
    private static final String CNT_TELEKOM_STORNO_TR_V21 = "COUNT_TELEKOM_ST";
    private static final String CNT_TELEKOM_STORNO_TR_V22 = "C1ST";
    private static final String SUM_TELEKOM_TR_V21 = "SUM_TELEKOM_TR";
    private static final String SUM_TELEKOM_TR_V22 = "S1TR";
    private static final String SUM_TELEKOM_STORNO_TR_V21 = "SUM_TELEKOM_ST";
    private static final String SUM_TELEKOM_STORNO_TR_V22 = "S1ST";
    
    private static final String CNT_VIP_TR_V21 = "COUNT_VIP_TR";
    private static final String CNT_VIP_TR_V22 = "C2TR";
    private static final String CNT_VIP_STORNO_TR_V21 = "COUNT_VIP_ST";
    private static final String CNT_VIP_STORNO_TR_V22 = "C2ST";
    private static final String SUM_VIP_TR_V21 = "SUM_VIP_TR";
    private static final String SUM_VIP_TR_V22 = "S2TR";
    private static final String SUM_VIP_STORNO_TR_V21 = "SUM_VIP_ST";
    private static final String SUM_VIP_STORNO_TR_V22 = "S2ST";
    
    private static final String CNT_TELENOR_TR_V22 = "C3TR";
    private static final String CNT_TELENOR_STORNO_TR_V22 = "C3ST";
    private static final String SUM_TELENOR_TR_V22 = "S3TR";
    private static final String SUM_TELENOR_STORNO_TR_V22 = "S3ST";
    
    private static final String CNT_ABATEL_TR_V22 = "C4TR";
    private static final String CNT_ABATEL_STORNO_TR_V22 = "C4ST";
    private static final String SUM_ABATEL_TR_V22 = "S4TR";
    private static final String SUM_ABATEL_STORNO_TR_V22 = "S4ST";
    
    private static final String CNT_GLOBALTEL_TR_V25 = "C3TR";
    private static final String CNT_GLOBALTEL_STORNO_TR_V25 = "C3ST";
    private static final String SUM_GLOBALTEL_TR_V25 = "S3TR";
 //   private static final String SUM_GLOBALTEL_STORNO_TR_V22 = "";
    
    private static final String CNT_VECTONE_TR_V25 = "C4TR";
    private static final String CNT_VECTONE_STORNO_TR_V25 = "C4ST";
    private static final String SUM_VECTONE_TR_V25 = "S4TR";
//    private static final String SUM_VECTONE_STORNO_TR_V22 = "S4ST";
    
    
    private static final String CNT_ABATEL_TR_V25 = "C9TR";  //ABATEL - Broj dopuna trans
    private static final String SUM_ABATEL_TR_V25 = "S9TR";  //ABATEL - Suma dopuna
    private static final String CNT_ABATEL_STORNO_TR_V25 = "C9ST"; //ABATEL - Broj storno trans
    private static final String SUM_ABATEL_STORNO_TR_V25 = "S9ST";  //ABATEL - Suma storno
    private static final String CNT_INOCALL_TR_V25 = "C7TR";  //Ino Call - Broj dopuna trans
    private static final String SUM_INOCALL_TR_V25 = "S7TR";  //Ino Call - Suma dopuna

    
    private static final String FIRMWARE_VERSION_2_1 = "1.2";
    private static final String FIRMWARE_VERSION_2_5 = "2.5";
}
