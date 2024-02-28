/*
 * Created on Feb 22, 2005
 *
 * Aleksandar Naumovic [aleksandar.naumovic@certus.co.yu]
 * Certus [www.certus.co.yu]
 *
 * 18/09/2005 Migration to RMI SMS Service, remove external sms application
 * usage
 *
 */
package yu.co.certus.pos.lanus.util;

import java.io.FileInputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.util.Properties;

import java.net.MalformedURLException;




import yu.co.certus.sms.ISmsServer;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;




/*

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.*;

*/


/**
 * @author naum/Marko Nikolic
 *
 */
public class Sms
{

    //lazy cache for the sms service
    private static ISmsServer _smsServiceRef = null;




	public static void send(Phone phone, String message) throws SmsException
	{
//            System.out.println("Send sms message = " + message + " to phone = " +
//                         phone.getFull());

            Properties properties = new Properties();
		try
		{
			FileInputStream stream = new FileInputStream("application.properties");
			properties.load(stream);
			stream.close();
		}
		catch (IOException e)
		{
//			System.out.println("IOException when loading application properties");

                    throw new SmsException("Couldn't read configuration");
		}
		if (Boolean.valueOf(properties.getProperty("Sms.send")).booleanValue())
		{
			if (properties.getProperty("Sms.testPhoneNumber") != null)
				phone = Phone.parse(properties.getProperty("Sms.testPhoneNumber"));
			String phoneNumber = phone.getFull();

                        //Process proc = Runtime.getRuntime().exec(new String[] {"./smsSend", phoneNumber, message});
                        //proc.waitFor();

			sendViaRMIService(phoneNumber, message);
		}


	}

        private static void sendViaRMIService(String phoneNumber, String message) throws
                SmsException {
            try {



                //resolve the service for teh first time
                if (_smsServiceRef == null) {
                    resolveService();
                }
                //send the message
                _smsServiceRef.sendMessage(phoneNumber,message);
            }  catch (RemoteException re) {

                if (re instanceof java.rmi.ConnectException) {
//                    System.out.println(
//                                "SMS Service is dead or restarted, will try to reconnect");


                    _smsServiceRef = null;
                    sendViaRMIService(phoneNumber, message);
                }

//                System.out.println(
//                        "Couldn't send sms, unable to contact sms service, " +
//                            "Remote RMI Exception, details : " + re.getMessage());


                throw new SmsException(
                        "Couldn't send sms, unable to contact sms service");
            }




        }



        private static void resolveService() throws SmsException {
            try {

                _smsServiceRef =
                        (ISmsServer) Naming.lookup("rmi://" + serverIP +
                                                   "/SmsServer");

            } catch (NotBoundException nbe) {
//                System.out.println(
//                        "Couldn't send sms, unable to contact sms service, " +
//                        "Not Bound RMI Exception, details : " + nbe.getMessage());
                throw new SmsException(
                        "Couldn't send sms, unable to contact sms service");
            } catch (MalformedURLException mue) {
//                System.out.println(
//                        "Couldn't send sms, unable to contact sms service, " +
//                        "Malformed URL Exception, details : " + mue.getMessage());
                throw new SmsException(
                        "Couldn't send sms, unable to contact sms service");
            } catch (RemoteException re) {

//                System.out.println(
//                        "Couldn't send sms, unable to contact sms service, " +
//                        "Remote RMI Exception, details : " + re.getMessage());
                throw new SmsException(
                        "Couldn't send sms, unable to contact sms service");
            }



        }

        //LOCAL HOST as a default
        private static String serverIP = "localhost";

        static {
            if(System.getProperty("yu.co.sms.service.host") != null){
                serverIP = System.getProperty("yu.co.sms.service.host");
            }
//            System.out.println("SMS service host = " + serverIP);
        }

        //little class test for rmi connectivity

        public static void main (String arg[]){
//            setFrame();
            try{
                sendViaRMIService("381641243145",
                                  "Broj 0641243145 dopunjen za vrednost od 150.0 dinara. Broj transakcije: 11100000001613. Hvala sto koristite MTS 064.");
            }catch(SmsException se){
//                System.out.println("Test failed, details : " + se.getMessage());
            }
        }
/*
        private static void setFrame() {
            JFrame win = new JFrame();
            win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            win.setSize(200, 200);
            JButton sendSMSBtn = new JButton("Send");
            JButton exitSMSServerBtn = new JButton("Exit");

            JPanel panel = new JPanel();
            panel.setSize(100, 100);
            panel.add(sendSMSBtn);
            panel.add(exitSMSServerBtn);

            sendSMSBtn.addActionListener(new SMSSendButtonAction());
            exitSMSServerBtn.addActionListener(new ExitSMSButtonAction());

            win.getContentPane().add(panel);

            win.setVisible(true);

        }


  static class SMSSendButtonAction implements ActionListener {
      public void actionPerformed(ActionEvent ae) {
          try{
              sendViaRMIService("12341", "Prsti, prsti bela staza");
          }catch(SmsException se){
                System.out.println("Test failed, details : " + se.getMessage());
            }

      }
  }


  static class ExitSMSButtonAction
      implements ActionListener {
    public void actionPerformed(ActionEvent ae) {

    }
  }

*/



}
