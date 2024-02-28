package yu.co.certus.pos.lanus.util;

import yu.co.certus.pos.lanus.UnknownMobileNetworkException;


public enum MobileOperatorEnum {
    
    
    
    
    MTS ("MTS", 1, 1, "148", ServiceData.SERVICE_PREPAID_DOPUNA_TELEKOM),
    VIP ("VIP", 2, 4, "444", ServiceData.SERVICE_PREPAID_DOPUNA_MOBILKOM),
    GLOBALTEL ("GLOBALTEL", 3, 9, "555", ServiceData.SERVICE_GLOBALTEL_DOPUNA),
    MUNDIO ("MUNDIO", 4, 8, "606", ServiceData.SERVICE_MUNDIO_DOPUNA),
    TELENOR ("TELENOR", 5, 2, "222", ServiceData.SERVICE_PREPAID_DOPUNA_MOBTEL),
    ABATEL ("ABATEL", 9, -1, "914", ServiceData.SERVICE_Q_PAY_SPOT_INTERNET);
    
    
    private String _name;
    private int _mob_operator_id_from_pos;
    private int _mob_operator_id_in_db;
    private String _transaction_prefix;
    private ServiceData _service;
    
    private MobileOperatorEnum(String name, int mob_operator_id_from_pos,
            int mob_operator_id_in_db, String transaction_prefix, ServiceData service){
        _name = name;
        _mob_operator_id_from_pos = mob_operator_id_from_pos;
        _mob_operator_id_in_db = mob_operator_id_in_db;
        _transaction_prefix = transaction_prefix;
        _service = service;
    }
    
    
    public static MobileOperatorEnum from_post(int post_oper_id) throws UnknownMobileNetworkException{
        switch(post_oper_id){
        case 1 : return MTS;
        case 2 : return VIP;
        case 3 : return GLOBALTEL;
        case 4 : return MUNDIO;
        case 5 : return TELENOR;
        case 9 : return ABATEL;
        default : throw new UnknownMobileNetworkException(post_oper_id);
        }
    }
    
    public static MobileOperatorEnum from_database(int mob_net_id_in_db) throws UnknownMobileNetworkException{
        switch(mob_net_id_in_db){
        case 1 : return MTS;
        case 4 : return VIP;
        case 9 : return GLOBALTEL;
        case 8 : return MUNDIO;
        case 2 : return TELENOR;
        default : throw new UnknownMobileNetworkException(mob_net_id_in_db);
        }
    }
    
    public static MobileOperatorEnum from_transaction_prefix(String transaction_prefix) throws UnknownMobileNetworkException{
        if(transaction_prefix.equals("148")){return MTS;}
        if(transaction_prefix.equals("444")){return VIP;}
        if(transaction_prefix.equals("555")){return GLOBALTEL;}
        if(transaction_prefix.equals("606")){return MUNDIO;}
        if(transaction_prefix.equals("222")){return TELENOR;}
        if(transaction_prefix.equals("914")){return ABATEL;}
        throw new UnknownMobileNetworkException(transaction_prefix);
    }
    
    public String getName(){
        return _name;
    }
    
    public int get_mob_operator_id_from_pos(){
        return _mob_operator_id_from_pos;
    }
    
    public int get_mob_operator_id_in_db(){
        return _mob_operator_id_in_db;
    }
    
    public String get_transaction_prefix(){
        return _transaction_prefix;
    }
    
    public ServiceData getService(){
        return _service;
    }
    
    
    public String toString(){
        return _name;
    }
    


}
