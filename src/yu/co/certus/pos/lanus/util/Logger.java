package yu.co.certus.pos.lanus.util;


import java.io.*;
import java.text.*;
import java.util.*;


public class Logger {

        private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private static final SimpleDateFormat _for_file_name_format = new SimpleDateFormat("yyyy-MM-dd@HH");

        private static final SimpleDateFormat _hour_format = new SimpleDateFormat("HH");

        private  Logging _fileLog = null;

        private  static Logger _singletonInstance = null;

        private  String _last_log_hour = "";


        private Logger() {

            setFileLog(new Date());
        }

        public static Logger getInstance(){
            if(_singletonInstance == null){
                _singletonInstance = new Logger();
            }
            return _singletonInstance;
        }

        protected void finalize() throws Throwable {
            try {
                closeCurrentLog(); // close open files
            } finally {
                super.finalize();
            }
        }







        private static String getFileName(Date currentDate){
            String toReturn = "backup" + File.separator + "tran_" + _for_file_name_format.format(currentDate) +
                        ".log";
            return  toReturn;
        }

        private  void setFileLog(Date currentDate){
            _last_log_hour = _hour_format.format(currentDate);
            try{
//                new File(
//                        getFileName(currentDate)).createNewFile();
                _fileLog = new Logging(new FileOutputStream(new File(
                        getFileName(currentDate)), true));
            }catch(java.io.FileNotFoundException fnfe){
                System.out.println("FileNot fount exception, details: " + fnfe.getMessage());
            }catch(java.io.IOException ioe){
                System.out.println("IOException exception, details: " + ioe.getMessage());
            }
        }

        public synchronized void logPlainMessage(String message){
            if (_fileLog != null) {
                Date now = new Date();
                String hour_now = _hour_format.format(now);
                if( !hour_now.equals(_last_log_hour)){
                    closeCurrentLog();
                    setFileLog(now);
                    _last_log_hour = hour_now;
                }
                _fileLog.write(message + ";");
            }
        }



        private  void closeCurrentLog() {
            if(_fileLog != null){
                _fileLog.exit();
            }
        }



        private class Logging {

            private PrintStream _log;

            private Logging(OutputStream os) {
                try {
                    _log = new PrintStream(os);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            private void write(String text) {
                _log.println( text);
            }

            private void exit() {
                _log.flush();
                _log.close();
            }

        }
        
        public static void main(String[] args){
            java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            System.out.println(sdf.format(new java.util.Date()));
        }


}
