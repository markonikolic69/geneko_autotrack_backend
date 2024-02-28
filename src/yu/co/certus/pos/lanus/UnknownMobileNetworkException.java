package yu.co.certus.pos.lanus;

public class UnknownMobileNetworkException extends Exception {
    
    public UnknownMobileNetworkException(int network_id){
        super("Unknown network id received = " + network_id + 
                ", must be one of 1,2,3,4");
    }
    
    public UnknownMobileNetworkException(String transactionPrefix){
        super("Unknown transaction prefix received = " + transactionPrefix + 
                ", must be one of 148,444,555,606");
    }

}
