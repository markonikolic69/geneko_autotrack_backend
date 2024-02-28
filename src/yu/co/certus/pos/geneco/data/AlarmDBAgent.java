package yu.co.certus.pos.geneco.data;

import java.io.FileInputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class AlarmDBAgent extends PostGreAgent {

    public AlarmDBAgent() throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        Properties properties = new Properties();

        try {

            FileInputStream stream = new FileInputStream("application.properties");
            properties.load(stream);
            stream.close();
            //


            Class.forName("org.postgresql.Driver");

            String host = properties.getProperty("alarm.Database.host");
            String database = properties.getProperty("alarm.Database.database");
            String username = properties.getProperty("alarm.Database.username");
            String password = properties.getProperty("alarm.Database.password");

            if (logger.isDebugEnabled()) {
                logger.debug("--> host = " + host + ", database = " + database + ", username = " +
                        username + ", password = " + password);
            }

            cn = DriverManager.getConnection("jdbc:postgresql://" + host + "/"
                    + database,
                    username,
                    password);




            //            cn = DriverManager.getConnection("jdbc:postgresql:" +
            //                    properties.getProperty(
            //                    "Database.database"),
            //                    properties.getProperty("Database.username"),
            //                    properties.getProperty("Database.password"));




            query = cn.createStatement();
        } 
        //        catch (IOException ioe) {
        //            System.out.println("Couldn't read or load database configuration from application.properties");
        //            throw new PostGreAgentException("Couldn't read or load database configuration from application.properties.", ioe);
        //        } 
        catch (ClassNotFoundException cnfe) {
            System.out.println("Couldn't find a postgresql driver. Probably missing a classpath.");
            throw new Exception("Couldn't find a postgresql driver. Probably missing a classpath.", cnfe);

        } catch (SQLException sqle) {
            System.out.println("Couldn't connect to database.");
            throw new Exception("Couldn't connect to database.", sqle);

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }

    public void insertReport(String unitSerialNo,String dogadjaj,Integer alarmTipId,String alarmDescription,String dezurni){

        try {
            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();

            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

            String dan = dateFormatter.format(now);
            String vreme = timeFormatter.format(now);


            query.executeUpdate("insert into reports (unit_serial_no,dogadjaj,datum,vreme,alarm_tip_id,alarm_detalji,dezurni) values ('" 
                    + unitSerialNo + "','" + dogadjaj + "','" + dan + "','"  + vreme  + "',"
                    + alarmTipId + ",'" + alarmDescription + "','" + dezurni  + "')");
        } catch (SQLException sqle) {


            logger.error(
                    "'SQLException' - while insert report  - "
                    + sqle.getMessage());

        }       
    }   

    public Boolean isKvar(String unitSerialNo) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        Boolean kvar=false;

        try {

            ResultSet result = query.executeQuery("select kvar from units where unit_serial_no='" + unitSerialNo + "'");

            while(result.next()){

                kvar = result.getBoolean("kvar");
            }

            result.close();

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while getting is kvar  - "
                        + sqle.getMessage());
            }

            return kvar;
        }



        return kvar;
    }

    public String getAlarmBroj(String unitSerialNo) {
        if (logger.isDebugEnabled()) {
            logger.info(
                    "'getAlarmBroj' - unitSerialNo  - "
                    + unitSerialNo);
        }
        String mobile=null;
        try {
            ResultSet result = query.executeQuery("select alarm_broj from units where unit_serial_no='" + unitSerialNo + "'");

            while(result.next()){

                mobile = result.getString("alarm_broj");
            }
        } catch (Exception sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while getAlarmBroj  - "
                        + sqle.getMessage());
            }



        }
        if (logger.isDebugEnabled()) {
            logger.info(
                    "<--'getAlarmBroj' - mobile  - "
                    + mobile);
        }
        return mobile;
    }

    public boolean isAktivanKorisnik(String unitSerialNo) {
        if (logger.isDebugEnabled()) {
            logger.info(
                    "'isAktivanKorisnik' - unitSerialNo  - "
                    + unitSerialNo);
        }
        boolean aktivan=true;
        try {
            ResultSet result = query.executeQuery("select users.aktivan from users,units where users.user_id=units.user_id and unit_serial_no='" + unitSerialNo + "'");

            while(result.next()){

                aktivan = result.getBoolean("aktivan");
            }
        } catch (Exception sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while isAktivanKorisnik  - "
                        + sqle.getMessage());
            }



        }
        if (logger.isDebugEnabled()) {
            logger.info(
                    "<--'isAktivanKorisnik' - aktivan  - "
                    + aktivan);
        }
        return aktivan;
    }

    public boolean isAktivnoVozilo(String unitSerialNo) {
        if (logger.isDebugEnabled()) {
            logger.info(
                    "'isAktivnoVozilo' - unitSerialNo  - "
                    + unitSerialNo);
        }
        boolean active=true;
        try {
            ResultSet result = query.executeQuery("select units.active from units where  unit_serial_no='" + unitSerialNo + "'");

            while(result.next()){

                active = result.getBoolean("active");
            }
        } catch (Exception sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while isAktivnoVozilo  - "
                        + sqle.getMessage());
            }



        }
        if (logger.isDebugEnabled()) {
            logger.info(
                    "<--'isAktivnoVozilo' - unitSerialNo  - "
                    + unitSerialNo);
        }
        return active;
    }

    public void insertReport(String unitSerialNo,String dogadjaj,Integer alarmTipId,String alarmDescription,String dezurni,String opis,boolean solved){
        if (logger.isDebugEnabled()) {
            logger.info(
                    "-->'insertReport' - unitSerialNo  - "
                    + unitSerialNo + ", dogadjaj = " + dogadjaj + ", alarmTipId = " + alarmTipId + 
                    ", alarmDescription = " + alarmDescription + ", dezurni = " + dezurni + 
                    ", opis = " + opis + ", solved = " + solved);
        }
        try {
            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();

            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

            String dan = dateFormatter.format(now);
            String vreme = timeFormatter.format(now);


            query.executeUpdate("insert into reports (unit_serial_no,dogadjaj,datum,vreme,alarm_tip_id,alarm_detalji,dezurni,opis,resen,solved_time) values ('" 
                    + unitSerialNo + "','" + dogadjaj + "','" + dan + "','"  + vreme  + "',"
                    + alarmTipId + ",'" + alarmDescription + "','" + dezurni  + "','" + opis + "'," + solved + ",now())");
        } catch (SQLException sqle) {


            logger.error(
                    "'SQLException' - while insert report  - "
                    + sqle.getMessage());



        }       
    }
    
    
  //called from alarmhandler
    private void insertCallCentarReport(String serialNumber, Integer alarmTypeId, String alarmDescription,
            String action, boolean solved){

        Boolean isKvar= isKvar(serialNumber);

        if(!isKvar){
            insertReport(serialNumber, "Aktiviran alarm.", alarmTypeId, alarmDescription, "call centar",action, solved);
        }            

    }
    


    public void close() {
        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            cn.close();
        } catch (SQLException e) {
            logger.error("Couldn't close connection.");

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
}
