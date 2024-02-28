package yu.co.certus.pos.lanus;

import yu.co.certus.pos.lanus.data.DBFactory;
import yu.co.certus.pos.lanus.service.Service;
import yu.co.certus.pos.lanus.util.ByteConverter;
import yu.co.certus.pos.lanus.util.AESEncrypter;

public class FiscalPakistanAuditProcessor extends AbstractFiscalDataProcessor {
    
    private int _response_code = 0;
    
    private byte[] _broj_racuna_bytes_duzine_5 = new byte[5];
    
    private byte[] _term_id_bytes = null;
    
    
    private boolean _is_ref_auddata = false;
    
    
    public FiscalPakistanAuditProcessor(String terminal_id, String clientSignature,
            int brojacPoruka, int brojacAuditPoruka, int brojacRetry, String broj_racuna, byte[] broj_racuna_bytes,
            byte[] term_id_bytes, boolean is_refund_audit_data){
        super(terminal_id, clientSignature, brojacPoruka, brojacAuditPoruka, brojacRetry, broj_racuna );
        _broj_racuna_bytes_duzine_5[0] = broj_racuna_bytes[0];
        _broj_racuna_bytes_duzine_5[1] = broj_racuna_bytes[1];
        _broj_racuna_bytes_duzine_5[2] = broj_racuna_bytes[2];
        _broj_racuna_bytes_duzine_5[3] = broj_racuna_bytes[3];
        _broj_racuna_bytes_duzine_5[4] = 0x00;
        _term_id_bytes = term_id_bytes;
        _is_ref_auddata = is_refund_audit_data;
    }

    @Override
    public int getResponseCode() {
        // TODO Auto-generated method stub
        return _response_code;
    }

    @Override
    public void process(String request) {
        // TODO Auto-generated method stub
        try{
            
            byte[] to_encript = new byte[]{
                    _term_id_bytes[0], _term_id_bytes[1], _term_id_bytes[2], _term_id_bytes[3],
                    _term_id_bytes[4], _term_id_bytes[5], _term_id_bytes[6], _term_id_bytes[7],
                    _term_id_bytes[8], _term_id_bytes[9], 0x00, _broj_racuna_bytes_duzine_5[0],
                    _broj_racuna_bytes_duzine_5[1], _broj_racuna_bytes_duzine_5[2], _broj_racuna_bytes_duzine_5[3],
                    _broj_racuna_bytes_duzine_5[4]};
            
            
            System.out.println("Value to encript = " + ByteConverter.hexify(to_encript));
            
            
            String enc_value = AESEncrypter.encryptSimple(to_encript);
            
            String receipt_type = "";
            String receipt_id = "";
            String[] parsed = request.split(",");
            //po dokumentaciji
            receipt_id = parsed[1];
            if(_is_ref_auddata){
                receipt_type = "R";
            }else{
                receipt_type = "H";
            }
        
            
            
            byte[] encodedBytes = 
                org.apache.commons.codec.binary.Base64.encodeBase64(getServerSignature(SHA_1_KEY, 
                        request));
            
            
        new DBFactory().getPakistanDBIface().insertAuditData(request, getTerminalID(), 
                getClientSignature(), new String(encodedBytes/*getServerSignature(SHA_1_KEY, 
                        request)*/), getCounter(), getAuditCounter(), getRetryCounter(),
                        getBrojRacuna(), enc_value, receipt_type, receipt_id);
        }catch(Exception e){
            _response_code = 2;
            Service.logger.error("Exception when try to insert db record, detals:  " + 
                    e.getMessage(), e);
        }
    }
    
    
    private static final String SHA_1_KEY = "key";
    
    public static void main(String[] args) throws Exception{
        byte[] encodedBytes = 
            org.apache.commons.codec.binary.Base64.encodeBase64(getServerSignature(SHA_1_KEY, 
                    "wqrqdfdsfdsfewewrewrewdvxvzfvsdfdaf"));
        
        System.out.println("" + new String(encodedBytes));
    }

}
