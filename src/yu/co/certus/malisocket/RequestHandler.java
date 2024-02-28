package yu.co.certus.malisocket;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.io.InputStreamReader;
import java.util.Date;
import java.text.SimpleDateFormat;

import yu.co.certus.pos.lanus.message.LoginResponse;
import yu.co.certus.pos.lanus.message.PrepaidResponse;
import yu.co.certus.pos.lanus.message.CancelResponse;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.Charset;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RequestHandler
    implements Runnable {

  private Socket _socket = null;

  private static Charset charset = Charset.forName("US-ASCII");
  private static CharsetDecoder decoder = charset.newDecoder();

  public RequestHandler(Socket socket) {
    _socket = socket;
  }

  public void run() {

    try {
      _socket.setSoTimeout(20000);
      _socket.setSoLinger(true, 5);
    }
    catch (Exception e) {
      System.out.println("Socket linger set exception, details:" +
                         e.getMessage());
    }


    BufferedReader in = null;
    //PrintWriter out = null;
    BufferedWriter out = null;

    try {
      in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
      //out = new PrintWriter(client.getOutputStream(), true);
      out = new BufferedWriter(
          new OutputStreamWriter(_socket.getOutputStream(), "ASCII"));

      LoginResponse logRes = new LoginResponse();
      logRes.addCnt("1");
      logRes.addmID("D0041047");
      logRes.addOperatorName("Administrator");
      logRes.addResponseCode(logRes.LOGIN_SUCCESSFUL);

      String logData = logRes.forPos();
//          "Cookie:T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5\r\nmID:D0041047\r\nCnt:1\r\n" +
//          "Err:11 Login Successful\r\nOpr:Administrator\r\n";

//      SimpleDateFormat transFor = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
      PrepaidResponse preRes = new PrepaidResponse();
      preRes.addTransactionId("14110042994452");
      preRes.addTime();
      preRes.addResponseCode(preRes.TRANSACTION_OK);
      String transData = preRes.forPos();

//      CancelResponse canRes = new CancelResponse();
      //canRes.addTransactionId("14110042994452");
      //canRes.addTime();
      //canRes.addAmount(5.00);
//      canRes.addResponseCode(canRes.TIMEOUT_ERROR);
//      String canData = canRes.forPos();

//          "Time:" + transFor.format(new Date()) +
      //"\r\nTid:14110042994452\r\n" +
      //         "\r\nTId:13610001065739\r\n" +
      //         "Err:12 Transaction Successful\r\n";

      SimpleDateFormat formatter = new SimpleDateFormat(
          "E, dd MMM yyyy HH:mm:ss z");
// Tue, 09 Jan 2002 22:14:02 -0500
      String s = formatter.format(new Date());

      String line = null;
      boolean send = true;
      while (send) {
        line = in.readLine();
        System.out.println(line);

        if (line.indexOf("Command=LGN") != -1) {

          //         out.write("HTTP/1.1 200 OK\r\n");
          //         out.write("Server: Apache-Coyote/1.1\r\n");
          //         out.write("Content-Length: " + logData.length() + "\r\n");
          //         out.write("Date: " + s + "\r\n");
          //         out.write("\r\n");

          out.write(
              logData);

          out.flush();
        }

        if (line.indexOf("Command=SYN") != -1 || line.indexOf("TRN") != -1) {

          //System.out.println("Cita da li ima nesto");
          //ByteBuffer buffer = ByteBuffer.allocate(200);
          //System.out.println("Procitao duzinu" + in.read(buffer.asCharBuffer()));
          //System.out.println("Procitao " + decoder.decode(buffer));


          //         out.write("HTTP/1.1 200 OK\r\n");
          //         out.write("Server: Apache-Coyote/1.1\r\n");
          //         out.write("Content-Length: " + transData.length() + "\r\n");
          //         out.write("Date: " + s + "\r\n");
          //         out.write("\r\n");



          try {
            System.out.println("sleep");
            Thread.sleep(15000);

          }
          catch (Throwable e) {
            System.out.println("Exception when try to close, details: " +
                               e.getMessage());
          }

          out.write(
              transData);

          out.flush();
          send = false;


        }

      }

      System.out.println("HOST ADDRESS = " + _socket.getInetAddress().getHostAddress());
        System.out.println("IS REACHABLE = " + _socket.getInetAddress().isReachable(5000));

      System.out.println("Cita opet da li ima nesto");
      ByteBuffer buffer = ByteBuffer.allocate(200);
      System.out.println("Procitao duzinu" + in.read(buffer.asCharBuffer()));
      System.out.println("Procitao " + decoder.decode(buffer));



      while ( (line = in.readLine()) != null) {

        System.out.println("ima, dobio sam:" + line);
        if (line.equals("")) {
          break;
        }
      }


      try {
        System.out.println("sleep again");
        Thread.sleep(5000);

      }
      catch (Throwable e) {
        System.out.println("Exception when try to close, details: " +
                           e.getMessage());
      }


      //System.out.println("in Zatvori socket");
      //in.close();
      //System.out.println("out Zatvori socket");
      //out.close();
      //System.out.println("Zatvori socket");
      //_socket.close();

      //System.out.println("Zatvorio sam");



    }
    catch (IOException e) {
      System.out.println("IOException, details:" + e.getMessage());
      System.out.println("Accept failed: 40001");
      System.exit( -1);
    }catch(Throwable e){
      System.out.println("Unknown exception ,details: " + e.getMessage());
    }

  }
}
