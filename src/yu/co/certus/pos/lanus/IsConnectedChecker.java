package yu.co.certus.pos.lanus;

import java.net.Socket;
import java.io.BufferedReader;
import yu.co.certus.pos.lanus.service.Service;
import java.io.IOException;
import java.io.BufferedWriter;

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
public class IsConnectedChecker {

  private Socket _soc = null;
  private BufferedReader _bReader = null;
  private BufferedWriter _bWriter = null;

  /*u najbrzem slucaju spava 4 sekunde*/
  private long _timeToSleep = 4000;
  private long _transactionTime = 0;
  private String _terminalId = "";

  public IsConnectedChecker(Socket soc,
                            BufferedReader bReader, BufferedWriter bWriter,
                            long transactionStart, String terminalId) {
    _soc = soc;
    _bReader = bReader;
    _bWriter = bWriter;
    init();
    _transactionTime = transactionStart;
    _terminalId = terminalId;
  }

  private void init() {
    try {
      _bReader.readLine();
      _bReader.readLine();
      _bReader.readLine();
      _bReader.readLine();
    }
    catch (IOException ioe) {
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to clean the read buffer " +
                             " details: " + ioe.getMessage());
      }

    }
  }

  public void checkIt() throws TerminalNotConnectedException {
    boolean isCon = true;
    try {
      long tmp = System.currentTimeMillis() - _transactionTime;
      if (Service.logger.isDebugEnabled()) {
        Service.logger.info("Proslo = " + tmp + " milisec");
      }

      long timeToSleep = (_timeToSleep - tmp) > 0 ?
          (_timeToSleep - tmp) : 0;

      if (Service.logger.isDebugEnabled()) {
        Service.logger.info("spava = " + timeToSleep + " milisec");
      }

      try {
        Thread.sleep(timeToSleep);
      }
      catch (InterruptedException ie) {
        if (Service.logger.isDebugEnabled()) {
          Service.logger.error("ISPAO NA SLEEP-u, details:  " + ie);
        }

      }

      send(IS_CONNECTED);

      String is_connected = "";

      try {

        is_connected = readMessage();

        if (Service.logger.isDebugEnabled()) {
          Service.logger.info("IS_connected citanje = " + is_connected);
        }
      }
      catch (IOException ioe) {
        if (Service.logger.isDebugEnabled()) {
          Service.logger.error(
              "--> Unable to read on first is_connected reading message," +
              " details: " + ioe.getMessage() +
              ", will finish thread");
        }
        return;
      }

      if (is_connected.equalsIgnoreCase(NO_RECEIVE)) {
        isCon = false;
        throw new TerminalNotConnectedException(_terminalId, 4);
      }
    }
    finally {
      /*
             int slepInSec = isCon ? 20 : 3;
             new Thread(new KillThread(slepInSec)).start();
       */
    }
  }

  public void send(String message) {
    try {
      _bWriter.write(message);
      _bWriter.flush();
    }
    catch (IOException ioe) {
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to write message " + message +
                             " details: " + ioe.getMessage());
      }
    }
  }

  public void sendWithIOException(String message) throws IOException {
    try {
      _bWriter.write(message);
      _bWriter.flush();
    }
    catch (IOException ioe) {
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to write message " + message +
                             " details: " + ioe.getMessage());
      }
      throw ioe;
    }
  }

  public String readMessage() throws IOException {

    char[] buffer = new char[4];

    try {
      int n = _bReader.read(buffer, 0, 4);
      return getContentString(buffer).trim();
    }
    catch (IOException ioe) {
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("--> Unable to read from socket," +
                             " details: " + ioe.getMessage());
      }
      throw ioe;
    }
  }

  public void waitInSec(int timeInSec) {
    try {
      Thread.sleep(timeInSec * 1000);
    }
    catch (InterruptedException ie) {
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("Sleep in clean for " + timeInSec +
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
/*
  private class KillThread
      implements Runnable {

    private int _sleepInSekInKill = 3;

    public KillThread(int sleepInSek){
      _sleepInSekInKill = sleepInSek;
    }

    public void run() {
      waitInSec(_sleepInSekInKill);
      if (Service.logger.isDebugEnabled()) {
        Service.logger.info("Try to sen kill after " + _sleepInSekInKill + " sec");

      }

      send(KILL);
    }
  }
*/
  public static final String IS_CONNECTED = "isConnected";
  public static final String IS_SEND = "isSend";
  public static final String IS_PACKET_SEND = "isPacketSent";
  public static final String KILL = "kill";

  public static final String YES_RECEIVE = "YES";
  public static final String NO_RECEIVE = "NO";

}
