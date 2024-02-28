/*
 * Created on Jun 28, 2005
 *
 * Aleksandar Naumovic [aleksandar.naumovic@certus.co.yu]
 * Certus [www.certus.co.yu]
 *
 */
package yu.co.certus.pos.lanus.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.activation.DataHandler;
import java.io.File;
import javax.mail.PasswordAuthentication;
import javax.mail.Authenticator;

import yu.co.certus.pos.lanus.service.Service;


/**
 * @author naum
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Mail
{
	private Properties properties;




        private String sendTo = "stornomobtel@telekom.yu";
        private String subject = "Mobtel - NAZIV_DISTRIBUTERA-DATUM-NAS_TRANSACTION_ID.txt";
        private String text = "Molimo vas da stornirate transakciju iz priloga";



	protected void initializeProperties()
	{
		properties = new Properties();
		try
		{
			FileInputStream stream = new FileInputStream("application.properties");
			properties.load(stream);
			stream.close();
		}
		catch (IOException e) {}
		// TODO what to do?
	}
	public Mail()
	{
		initializeProperties();
	}
	private void send(String text)
	{
		try
		{
			Session session = Session.getDefaultInstance(properties);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress("aleksandar.naumovic@certus.co.yu"));
			// TODO should be configurable
			message.setRecipient(Message.RecipientType.TO, new InternetAddress("predrag.bijelic@certus.co.yu"));
			message.setRecipient(Message.RecipientType.CC, new InternetAddress("aleksandar.naumovic@certus.co.yu"));
			message.setSubject("PosService warning");
			message.setSentDate(new Date());
			message.setText(text);
			Transport.send(message);
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
		}
	}



        public void sendMobtelStorno(File toSend, String from) throws MessagingException{
            java.util.Properties props = new java.util.Properties();
            String _smtp_host = properties.getProperty("mail.smtp.host","192.168.0.254");
            String _smtp_port = properties.getProperty("mail.smtp.port","25");

            props.put("mail.smtp.host", _smtp_host);
            props.put("mail.smtp.port", "" + _smtp_port);
            Session session = Session.getDefaultInstance(props, null);

            // Construct the message
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(from));

            String to = properties.getProperty("mobtel.storno.recipient","marko.nikolic@certus.co.yu"/*"stornomobtel@telekom.yu"*/);

            msg.setRecipient(Message.RecipientType.TO,
                              new InternetAddress(to));
    msg.setRecipient(Message.RecipientType.CC,new InternetAddress("maja.dimitrijevic@certus.co.yu"));
            msg.setSubject("Telenor - " + toSend.getName());

            // create the message part
            MimeBodyPart messageBodyPart =
                    new MimeBodyPart();

            //fill message
            messageBodyPart.setText(
                    "Molimo vas da stornirate transakciju iz priloga");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);


            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source =
                    new FileDataSource(toSend);
            messageBodyPart.setDataHandler(
                    new DataHandler(source));
            messageBodyPart.setFileName(toSend.getName());
            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);

            // Send the message
            try{
                Transport.send(msg);
            }catch(Throwable e) {
                throw new MessagingException("Unable to send message, unknown problem, details :" +
                                             e.getMessage());
            }
    }

    public void sendMaintenanceReportMail(String text, String recipient) throws MessagingException{
           sendMail(text, recipient, "merge report");
    }

    public void sendMTSCancelFailure(String text, String recipient) throws MessagingException{
           sendMail(text, recipient, "neuspeo storno - raport");
    }


    private void sendMail(String text, String recipient, String subject) throws MessagingException{
            java.util.Properties props = new java.util.Properties();
            String _smtp_host = properties.getProperty("mail.smtp.host","192.168.0.254");
            String _smtp_port = properties.getProperty("mail.smtp.port","25");

            props.put("mail.smtp.host", _smtp_host);
            props.put("mail.smtp.port", "" + _smtp_port);
            Session session = Session.getDefaultInstance(props, null);

            // Construct the message
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress("marko.nikolic@certus.co.yu"));

            String to = recipient;

            msg.setRecipient(Message.RecipientType.TO,
                              new InternetAddress(to));

            msg.setSubject(subject);

            // create the message part
            MimeBodyPart messageBodyPart =
                    new MimeBodyPart();

            //fill message
            messageBodyPart.setText(
                    text);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);




            msg.setContent(multipart);

            // Send the message
            try{
                Transport.send(msg);
            }catch(Throwable e) {
                throw new MessagingException("Unable to send message, unknown problem, details :" +
                                             e.getMessage());
            }
    }


    public void sendMobilkomStorno(String text, String from, String subject) throws MessagingException{
        if (Service.logger.
                isDebugEnabled()) {
              Service.logger.info(
                  "Try to send MOBILKOM STORNO mail, tekst :" +
                  text + ", from : " + from);
              

            }
            java.util.Properties props = new java.util.Properties();
            String _smtp_host = properties.getProperty("mail.smtp.host","192.168.0.254");
            String _smtp_port = properties.getProperty("mail.smtp.port","25");
            String _smtp_user = properties.getProperty("mail.smtp.user","office@certus.rs");
            String _smtp_pass = properties.getProperty("mail.smtp.password","mailsifra");

            System.out.println("_smtp_host = " + _smtp_host);
            System.out.println("_smtp_port = " + _smtp_port);
            System.out.println("_smtp_user = " + _smtp_user);
            System.out.println("_smtp_pass = " + _smtp_pass);

            props.put("mail.smtp.host", _smtp_host);
            props.put("mail.smtp.port", "" + _smtp_port);

            props.put("mail.smtp.auth", "true");
            Session session = Session.getDefaultInstance(props, new SMTAuthenticator(_smtp_user, _smtp_pass));

            // Construct the message
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(from));

            String to = properties.getProperty("mobilkom.storno.recipient","marko.nikolic@certus.rs"/*"etopup.support@vipmobile.rs"*/);
System.out.println("to = " + to);
            msg.setRecipient(Message.RecipientType.TO,
                              new InternetAddress(to));
//    msg.setRecipient(Message.RecipientType.CC,new InternetAddress("predrag.bijelic@certus.rs"));
//            msg.setSubject("Certus timeout transaction cancelation SRBIJA_CERTUS");
            msg.setSubject(subject);
            // create the message part
            MimeBodyPart messageBodyPart =
                    new MimeBodyPart();

            //fill message
            messageBodyPart.setText(
                    text);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);

            // Send the message
            try{
                Transport.send(msg);
                if (Service.logger.
                        isDebugEnabled()) {
                      Service.logger.info(
                          "MOBILKOM STORNO mail sent");
                    }
            }catch(Throwable e) {
                throw new MessagingException("Unable to send message, unknown problem, details :" +
                                             e.getMessage());
            }
    }


    private class SMTAuthenticator extends Authenticator{

      private String _user = "";
      private String _pass = "";
      private SMTAuthenticator(String user, String pass){
        _user = user;
        _pass = pass;

      }


      public PasswordAuthentication getPasswordAuthentication(){
        return new PasswordAuthentication(_user,_pass);
      }


    }






}
