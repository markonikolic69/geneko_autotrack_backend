package yu.co.certus.malisocket;

import java.io.*;
import java.net.*;

import java.text.SimpleDateFormat;

import java.util.Date;

class Service
    extends Thread {

  ServerSocket server = null;

  BufferedReader in = null;
  //PrintWriter out = null;
  BufferedWriter out = null;
  String line = "";

  Service() { //Begin Constructor
    try {
      //open the socket
      server = new ServerSocket(40002, 1000,
                                InetAddress.getByName("192.168.0.6"/*"172.18.22.5"*/));
    }
    catch (IOException e) {
      System.out.println("Could not listen on port 40001");
      System.exit( -1);
    }

  } //End Constructor

  public void run() {
    Socket client = null;

    if (server == null) {
      return;
    }




    boolean sendFirstTime = true;
    while (true) {

      try {
        //server listens for the client sockets
        client = server.accept();
      }
      catch (IOException e) {
        System.out.println("Accept failed: 40001");
        System.exit( -1);
      }

      // Create a new thread to process the request.
      Thread thread = new Thread(new RequestHandler(client));

      // Start the thread.
      thread.start();





/*

      try {
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //out = new PrintWriter(client.getOutputStream(), true);
        out = new BufferedWriter(
            new OutputStreamWriter(client.getOutputStream(), "ASCII"));
      }
      catch (IOException e) {
        System.out.println("Accept failed: 40001");
        System.exit( -1);
      }

      System.out.println(line);


      try {
        //System.out.println("Nesto stiglo");
        //char[] buffer = new char[10];
        //in.read(buffer);

        String path = "/mPOS/mPOS.dll?MfcISAPI"; //Command=LGN&Ver=mPOS1.2&OpPsw=00001111&Encr=PLN&User=mpos_359592000254675";

        line = in.readLine();
        String logData = "Cookie:T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5\r\nmID:D0041047\r\nCnt:1\r\n" +
            "Err:11 Login Successful\r\nOpr:Administrator\r\n";

        SimpleDateFormat transFor = new SimpleDateFormat("dd-MMM-yyyy HH.mm.ss");
        String transData = "Time:"+transFor.format(new Date())+"\r\nTid:14110042994452\r\n" +
            "Err:12 Transaction Successful\r\n";

        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        // Tue, 09 Jan 2002 22:14:02 -0500
        String s = formatter.format(new Date());


        if (sendFirstTime) {
          System.out.println("log");
          out.write("HTTP/1.1 200 OK\r\n");
          out.write("Server: Apache-Coyote/1.1\r\n");
          out.write("Content-Length: " + logData.length() + "\r\n");
          out.write("Date: "+s+"\r\n");
          out.write("\r\n");

          out.write(
              logData);
          sendFirstTime = false;
        }else{
          System.out.println("trn");
          out.write("HTTP/1.1 200 OK\r\n");
          out.write("Server: Apache-Coyote/1.1\r\n");
          out.write("Content-Length: " + transData.length() + "\r\n");
          out.write("Date: "+s+"\r\n");
          out.write("\r\n");

          out.write(
              transData);
          sendFirstTime = true;
        }
        out.flush();
        System.out.println("sendFirstTime = " + sendFirstTime);



      }
      catch (IOException e) {
        System.out.println("Read failed");
        System.exit( -1);
      }
*/
    }
  }
/*
  protected void finalize() {
//Clean up
    try {
      in.close();
      out.close();
      server.close();
    }
    catch (IOException e) {
      System.out.println("Could not close.");
      System.exit( -1);
    }
  }
*/

  public static void main(String[] args) {
    Service frame = new Service();
    frame.start();

    //frame.listenSocket();
  }
}
