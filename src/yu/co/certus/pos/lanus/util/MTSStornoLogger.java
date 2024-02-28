package yu.co.certus.pos.lanus.util;

import java.io.*;
import java.text.*;
import java.util.*;

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
public class MTSStornoLogger {

  private static final SimpleDateFormat sdf = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss");

  private static final SimpleDateFormat _for_file_name_format = new
      SimpleDateFormat("yyyy-MM-dd@HH");

  private static final SimpleDateFormat _hour_format = new SimpleDateFormat(
      "HH");

  private Logging _fileLog = null;

  private static MTSStornoLogger _singletonInstance = null;

  private String _last_log_hour = "";

  private MTSStornoLogger() {
    setFileLog(new Date());
  }

  public static MTSStornoLogger getInstance() {
    if (_singletonInstance == null) {
      _singletonInstance = new MTSStornoLogger();
    }
    return _singletonInstance;
  }

  public synchronized void logStorno(int transactionId, String serialNumber,
                                                double originalAmount,
                                                double stornoAmount,
                                                String msIsdn, boolean uspesna) {
    logPlainMessage("storno,transaction",
                    transactionId + "," + serialNumber + "," +
                    originalAmount + "," + stornoAmount + "," + msIsdn +
                    "," + uspesna +
                    "," + sdf.format(new Date()));
  }


  protected void finalize() throws Throwable {
    try {
      closeCurrentLog(); // close open files
    }
    finally {
      super.finalize();
    }
  }

  private static String getFileName(Date currentDate) {
    String toReturn = "mtslog" + File.separator + "storno_" +
        _for_file_name_format.format(currentDate) +
        ".log";
    return toReturn;
  }

  private void setFileLog(Date currentDate) {
    _last_log_hour = _hour_format.format(currentDate);
    try {
//                new File(
//                        getFileName(currentDate)).createNewFile();
      _fileLog = new Logging(new FileOutputStream(new File(
          getFileName(currentDate)), true));
    }
    catch (java.io.FileNotFoundException fnfe) {
      System.out.println("FileNot fount exception, details: " + fnfe.getMessage());
    }
    catch (java.io.IOException ioe) {
      System.out.println("IOException exception, details: " + ioe.getMessage());
    }
  }

  private void logPlainMessage(String operation, String message) {
    if (_fileLog != null) {
      Date now = new Date();
      String hour_now = _hour_format.format(now);
      if (!hour_now.equals(_last_log_hour)) {
        closeCurrentLog();
        setFileLog(now);
        _last_log_hour = hour_now;
      }
      _fileLog.write(operation + "," + message);
    }
  }

  private void closeCurrentLog() {
    if (_fileLog != null) {
      _fileLog.exit();
    }
  }

  private class Logging {

    private PrintStream _log;

    private Logging(OutputStream os) {
      try {
        _log = new PrintStream(os);
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    private void write(String text) {
      _log.println(text);
    }

    private void exit() {
      _log.flush();
      _log.close();
    }

  }

}
