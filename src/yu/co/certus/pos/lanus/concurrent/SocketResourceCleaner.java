package yu.co.certus.pos.lanus.concurrent;

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

import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import yu.co.certus.pos.lanus.service.Service;






public abstract class SocketResourceCleaner implements Runnable{


  private Socket _soc = null;
  private BufferedReader _bReader = null;
  private BufferedWriter _bWriter = null;


  public SocketResourceCleaner(Socket soc,
      BufferedReader bReader, BufferedWriter bWriter) {
    _soc = soc;
    _bReader = bReader;
    _bWriter = bWriter;
    init();
  }

  private void init(){
    try{
      System.out.println(_bReader.readLine());
      System.out.println(_bReader.readLine());
      System.out.println(_bReader.readLine());
      System.out.println(_bReader.readLine());
    }catch(IOException ioe){
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to clean the read buffer "+
                             " details: " + ioe.getMessage());
      }

    }
  }

  public void run(){

  }

  public void send(String message){
    try{
      _bWriter.write(message);
      _bWriter.flush();
    }catch(IOException ioe){
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to write message "+ message +
                             " details: " + ioe.getMessage());
      }
    }
  }

  public void sendWithIOException(String message) throws IOException{
    try{
      _bWriter.write(message);
      _bWriter.flush();
    }catch(IOException ioe){
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to write message "+ message +
                             " details: " + ioe.getMessage());
      }
      throw ioe;
    }
  }


  public String readMessage() throws IOException{

    char[] buffer = new char[4];





    try{
      int n = _bReader.read(buffer, 0, 4);
      return getContentString(buffer).trim();
    }catch(IOException ioe){
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to read from socket,"+
                             " details: " + ioe.getMessage());
      }
      throw ioe;
    }
  }


  public void waitInSec(int timeInSec){
    try{
      Thread.sleep(timeInSec * 1000);
    }catch(InterruptedException ie){
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("Sleep in clean for "+ timeInSec +
                             " sec interrupted ");

      }
    }
  }


  private String getContentString(char[] content) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < content.length; i++) {
            if (content[i] != 0)
                builder.append(content[i]);
            else
                break;
        }
        return builder.toString();
    }


  public static final String IS_CONNECTED = "isConnected";
  public static final String IS_SEND = "isSend";
  public static final String IS_PACKET_SEND = "isPacketSent";
  public static final String KILL = "kill";

  public static final String YES_RECEIVE = "YES";
  public static final String NO_RECEIVE = "NO";


  public static final int FIRST_WAIT_IN_SEC = 5;
  public static final int SECOND_WAIT_IN_SEC  = 60;
  public static final int THIRD_WAIT_IN_SEC = 90;
}
