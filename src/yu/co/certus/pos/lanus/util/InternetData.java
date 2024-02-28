package yu.co.certus.pos.lanus.util;



public class InternetData extends PaymentNumber {



    public InternetData(String ispCode) {
        //old
        //super(null);
        super(ispCode);

        parseNumber(ispCode);

    }


    private String providerCode;
    private String userCode;
    private String providerName;
    private String providerUrl;
    private String providerUser; //our id at ISP server ("certus")
    private String providerPassword; //our password at ISP server
    //private String providerTrustFile; //file used for our authentification at ISP server



    private int connectionTimeout;

    //clanovi koje salje ISP u svom odgovoru
    private int status;
    private int ispTransactionId;
    private String userName;
    private String packet;
    private String packetDescription;
    //////////////////////INTERNET_IZMENE-kraj bloka//////////////////////



    public void parseNumber(String number) {
        //old
        //setCode(number);
        if (number.length() == 0) {
            setProviderCode("");
            setUserCode("");
        } else {
            setProviderCode(number.substring(1, 3));
            setUserCode(number.substring(3));
        }
    }


    /**
     * @return Returns the providerId.
     */
    public String getProviderCode() {
        return providerCode;
    }

    /**
     * @param providerId The providerId to set.
     */
    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    /**
     * @return Returns the userCode.
     */
    public String getUserCode() {
        return userCode;
    }

    /**
     * @param userCode The userCode to set.
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    /**
     * @return
     */
    public boolean isTest() {
        return providerCode.equals("00") && userCode.equals("000000");
    }


    ////////////////////////////INTERNET_IZMENE////////////////////////////
    public String toString(){

        return  "code = " + getCode() +
                " providerCode = " + providerCode +
                " providerName = " + providerName +
                " userCode = " + userCode +
                " userName = " + userName +
                " ispTransactionId = " + ispTransactionId +
                " status = " + status +
                " providerUrl = " + providerUrl +
                " providerUser = " + providerUser +
//                " providerTrustFile = " + providerTrustFile +
                " packet = " + packet +
                " packetDescription = " + packetDescription;
     }

     /**
      * Setter method for bulk setting of parameters returned by ISP
      * @param status int
      * @param ispTransactionId int
      * @param username String
      * @param packet String
      * @param packetDescription String
      */
     public void setIspReturnValues(int status, int ispTransactionId, String userName, String packet, String packetDescription) {
         this.status = status;
         this.ispTransactionId = ispTransactionId;
         this.userName = userName;
         this.packet = packet;
         this.packetDescription = packetDescription;

     }

     public int getStatus() {
         return status;
     }

     public int getIspTransactionId() {
         return ispTransactionId;
     }

     public String getUsername() {
         return userName;
     }

     public String getPacket() {
         return packet;
     }

     public String getPacketDescription() {
         return packetDescription;
     }

     public String getProviderUrl() {
         return providerUrl;
     }
     public void setProviderUrl(String url) {
         this.providerUrl = url;
     }

     public String getProviderName() {
         return providerName;
     }
     public void setProviderName(String name) {
         this.providerName = name;
     }

     public String getProviderUser() {
         return providerUser;
     }
     public void setProviderUser(String user) {
         providerUser = user;
     }

     public String getProviderPassword() {
         return providerPassword;
     }
     public void setProviderPassword(String password) {
         providerPassword = password;
     }
/*
     public String getProviderTrustFile() {
         return providerTrustFile;
     }
     public void setProviderTrustFile(String file) {
         providerTrustFile = file;
     }
*/
     public int getConnectionTimeout() {
         return connectionTimeout;
     }
     public void setConnectionTimeout(int timeout) {
         connectionTimeout = timeout;
     }


 


     private boolean isUnitTestMode(){
        boolean flag1 =  System.getProperty("unittest") != null;
        if(flag1){
            boolean flag2 = System.getProperty("unittest").equals("on");
            return (flag1 && flag2);
        }
        return  false;
    }
     //////////////////////INTERNET_IZMENE-kraj bloka//////////////////////
}
