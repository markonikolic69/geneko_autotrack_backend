package yu.co.certus.pos.lanus;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import yu.co.certus.pos.lanus.message.AbstractRequest;
import yu.co.certus.pos.lanus.message.AbstractResponse;
import yu.co.certus.pos.lanus.util.CRC16;
import yu.co.certus.pos.lanus.util.HmacSha1Signature;

public abstract class AbstractFiscalDataProcessor {
    
    protected Properties properties;
    
    private String _terminal_id = "";
    private String _clientSignature = "";
    
    private int _counter = -1;
    private int _audit_counter = -1;
    private int _retry_counter = -1;
    
    private String _broj_racuna = "";
    
    public AbstractFiscalDataProcessor(String terminal_id, String clientSignature,
            int brojacPoruka, int brojacAuditPoruka, int brojacRetry, String broj_racuna){
        _terminal_id = terminal_id;
        _clientSignature = clientSignature;
        _counter = brojacPoruka;
        _audit_counter = brojacAuditPoruka;
        _retry_counter = brojacRetry;
        _broj_racuna = broj_racuna;
        initializeProperties();
    }
    
    public int getCounter(){
        return _counter;
    }
    
    public int getAuditCounter(){
        return _audit_counter;
    }
    
    public int getRetryCounter(){
        return _retry_counter;
    }
    
    public String getTerminalID(){
        return _terminal_id;
    }
    
    public String getClientSignature(){
        return _clientSignature;
    }
    
    public String getBrojRacuna(){
        return _broj_racuna;
    }
    
    public static byte[] getServerSignature(String key, String data) throws Exception{
        return HmacSha1Signature.calculateRFC2104HMAC(data, key);
    }
    
    public byte[] getCRC16(byte[] data){
        return CRC16.getCRC16(data);
    }
    
    
    protected void initializeProperties() {
        properties = new Properties();
        try {
          FileInputStream stream = new FileInputStream(
              "application.properties");
          properties.load(stream);
          stream.close();
        }
        catch (IOException e) {}
        // TODO what to do?
      }
    
    
    public abstract void process(String request);
    
    public abstract int getResponseCode();

}
