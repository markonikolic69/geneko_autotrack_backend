package yu.co.certus.pos.geneco.util;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import yu.co.certus.pos.lanus.service.Service;
import org.apache.log4j.Logger;

public class ComtradeSmsSender{

    Logger logger = Service.logger;

    private String _phoneNumber = "";
    private String _message = "";


    public ComtradeSmsSender(String phoneNumber, String message) {
        _phoneNumber = phoneNumber;
        _message = message;
    }

    private static final String SMSsender = "AutoTrack";



    public void send(){

        String user = "qvoucher";
        String password = "qvoucher123";


        //        if(!phoneNumber.startsWith("+")){
        //            phoneNumber="+" + phoneNumber;
        //        }


        try {

            String messageText = URLEncoder.encode(_message, "utf-8");

            //messageText = messageText.replaceAll("\n","%0A");


            String endpoint = "http://sms.comtrade.com/send.asp?phone=" +
            _phoneNumber +
            "&user=" + user + "&pass=" +
            password + "&sms=" +
            messageText + "&display=" +
            SMSsender   + "&id=0";
            String result = null;

            if (endpoint.startsWith("http://")) {
                // Send a GET request to the servlet
                try {


                    // Send data
                    String urlStr = endpoint;
                    //        if (requestParameters != null && requestParameters.length() > 0) {
                    //          urlStr += "?" + requestParameters;
                    //        }
                    System.out.println(urlStr);
                    URL url = new URL(urlStr);
                    URLConnection conn = url.openConnection();

                    // Get the response
                    BufferedReader rd = new BufferedReader(new
                            InputStreamReader(conn.
                                    getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    rd.close();
                    result = sb.toString();
                    logger.info("sms sent result=" + result + " " + _phoneNumber + " - " + _message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.error( "sms send error " + _phoneNumber +", "+ e.getMessage());
        }
        //return result;

    }

}
