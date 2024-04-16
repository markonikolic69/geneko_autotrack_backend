package yu.co.certus.pos.geneco.data;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Properties;


import org.apache.log4j.Logger;

import yu.co.certus.pos.geneco.protocol.impl.ProtocolDecoderOldGeneko;
import yu.co.certus.pos.geneco.protocol.message.GPSMessage;
import yu.co.certus.pos.geneco.util.AlarmHandler;
import yu.co.certus.pos.lanus.service.Service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * @author Ognjen Simic (ognjen.simic@certus.co.yu)
 *
 */
/**
 * This class holds methods for work wih database
 *
 */
public class PostGreAgent {
    Logger logger = Service.logger;

    public Connection cn;
    public Statement query;

    private String whereString = "";

    private GeoAgent geoagent;
    private String _streetNumber = "";
    private String _community = "";
    private String _city = "";
    private String _state = "";
    private double _distance = 0;
    private double _latitude = 0;
    private double _longitude = 0;
    private int _timeZone = 1;
    private int _direction = 0;
    private int _angle = 0;
    private String _fix = "";
    
    
    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    private DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private DateFormat timestampFormatterSMS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    
    private static final int SONDA_FUEL_DIFF_THRESHOLD_ALARD = 5;
    private static final int SONDA_FUEL_DIFF_ALARM_TYPE_ID = 2;
    
    
////for test
//    private static final long ONE_DAY_MILLI_SEC = 24 * 60 * 60 * 1000;
//    public String _oneYearFromStartingServerDate;
    public PostGreAgent() throws Exception {

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
            
            String host = properties.getProperty("Database.host");
            String database = properties.getProperty("Database.database");
            String username = properties.getProperty("Database.username");
            String password = properties.getProperty("Database.password");
            
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

    public UnitData getSerialNumber(String simCard) {

        if (logger.isDebugEnabled()) {
            logger.debug("--> simCard = " + simCard);
        }
        

        UnitData to_return = new UnitData();

        String serialNumber = "sim" + simCard;
        String driver = "";
        Integer driver_id = null;
        to_return.set_serialNumber(serialNumber);
        //teltonika
        if(simCard.length()>13){
            serialNumber=simCard;
        }

        try {

            ResultSet result = query.executeQuery("select id, current_driver, current_driver_id from unit where sim_card='" + simCard + "'");

            if(result.next()) {

                serialNumber = result.getString("id");
                driver = result.getString("current_driver");
                int driver_id_int = result.getInt("current_driver_id");
                if(!result.wasNull()){
                    driver_id = new Integer(driver_id_int);
                }
            }
            to_return.set_serialNumber(serialNumber);
            to_return.set_current_driver(driver);
            to_return.set_current_driver_id(driver_id);
            
            result.close();

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while getting serial number  - "
                        + sqle.getMessage());
            }

            return to_return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<-- to_return = " + to_return);
        }

        return to_return;
    }

    public String calculateSondaFuel(String serialNumber, Integer messageValue, Integer rezervoar) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "--> calculate Sonda fuel, serialNumber = "
                    + serialNumber + ", serialNumber = " + serialNumber + ", rezervoar = " + rezervoar);
            
        }
        

        DecimalFormat oneDigit = new DecimalFormat("0.0");
        double sondaFuel = -1;

        //use double to get koeficijent as double
        double lowerValue = 0;
        double greaterValue = 0;
        double lowerLitar = 0;
        double greaterLitar = 0;

        double previousLowerValue = 0;
        double previousLowerLitar = 0;

        

        try {

            ResultSet result = query.executeQuery("select distinct value,litar from sonda_fuel_map where unit_id='" + serialNumber
                    + "' and rezervoar=" + rezervoar + " order by value");

            while (result.next()) {

                previousLowerValue = lowerValue;
                previousLowerLitar = lowerLitar;

                if (result.getInt("value") <= messageValue) {
                    lowerValue = result.getInt("value");
                    lowerLitar = result.getInt("litar");
                }

                if (result.getInt("value") > messageValue && greaterValue == 0) {
                    greaterValue = result.getInt("value");
                    greaterLitar = result.getInt("litar");
                }

            }
            result.close();

            if (lowerValue == 0 && greaterValue == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "calculate Sonda  return 0.0");
                }
                return "0.0";
            }

            double koeficijent = 0;

            System.out.println("greaterValue " + greaterValue);

            System.out.println(greaterLitar + " do " + lowerLitar + " val " + greaterValue + " do " + lowerValue);

            if (greaterValue > 0) {
                System.out.println("great low" + greaterValue + "" + lowerValue);
                koeficijent = (greaterLitar - lowerLitar) / (greaterValue - lowerValue);
            } else {
                koeficijent = (lowerLitar - previousLowerLitar) / (lowerValue - previousLowerValue);
                System.out.println("lowerLitar previousLowerLitar" + lowerLitar + "" + previousLowerLitar);
                System.out.println("lower previousLowerValue" + lowerValue + "" + previousLowerValue);
            }

            sondaFuel = lowerLitar + (messageValue - lowerValue) * koeficijent;

            System.out.println(koeficijent + "koef, val " + messageValue + "in litars =" + sondaFuel);
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while calculate Sonda  - "
                        + sqle.getMessage());
            }

        } catch (Exception e) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while calculate Sonda  - "
                        + e.getMessage() + " return 0.0");
            }
            return "0.0";

        }

        String formatedSondaFuel = oneDigit.format(sondaFuel);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "calculate Sonda fule in litars - "
                    + formatedSondaFuel);
        }
        return formatedSondaFuel;
    }
    
 
    
     
     
     
  
     
     
      public void deleteUnsentMessage(String id) {
            try {
                String queryString = "delete from unsent_messages where id=" + id;
               
                query.executeUpdate(queryString);
            } catch (SQLException sqle) {

           
                logger.error(
                        "'SQLException' - while delete from unsent_messages  - "
                        + sqle.getMessage());
            

            }

      }
     
      
 
      
      
//      public String saveRfidRequest(String simCard, String rfidString,String timestampString) {
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("-->");
//        }
//
//        String rfidResponse="ok";
//       
//        try {
//            
//           
//            
//             String unitId = getSerialNumber(simCard);
////        Calendar cal = Calendar.getInstance();
////        Date now = cal.getTime();
////
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
//        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
//        DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        
//        
//                
//            System.out.println("timestampString " + timestampString);
//        Date timestampIbutton=timestampFormatter.parse(timestampString);
////
//        String dan = dateFormatter.format(timestampIbutton);
//        String vremeOd = timeFormatter.format(timestampIbutton);
//        
//        
//        if(new Date(timestampIbutton.getTime()).compareTo(dateFormatter.parse("2090-01-01"))>0){
//            
//           
//            //replace with now
//                    timestampIbutton=new Date();
//                    dan = dateFormatter.format(timestampIbutton);
//                    vremeOd = timeFormatter.format(timestampIbutton);
//                    
//                     logger.error(
//                        timestampString + " change to "
//                        + dan + vremeOd);
//                    
//        }
//       
//        //String vremeOd=timeStamp;
//       
//            
//            ResultSet result = query.executeQuery("select current_ibutton,ibutton_time from unit where id='" +  unitId +  "';");
//
//            String current_ibutton="";
//            Timestamp ibuttonTime=null;
//            while (result.next()) {
//               
//                current_ibutton=result.getString("current_ibutton");
//                ibuttonTime=result.getTimestamp("ibutton_time");
//                
//            };
//            
//            result.close();
//            
//            
//            result = query.executeQuery("select id,ime_prezime from vozaci where ibutton='" + rfidString + "';");
//
//            Integer driverId=0;
//            String driver="";
//            while (result.next()) {
//                driverId=result.getInt("id");
//                driver=result.getString("ime_prezime");
//                
//            };
//            
//            result.close();
//            
//             System.out.println(" rfid driver " + driver);
//            
//            if(driverId>0){
//                
//                
//                
//                //generisanje automatic odjave sebe sa drugih vozila
//                 result = query.executeQuery("select * from unit where id<>'"
//                        + unitId + "' and current_driver_id=" + driverId + ";");
//
//                 String oldUnitId="";
//                while (result.next()) {
//                    oldUnitId=result.getString("id");
//                    
//
//                };
//                result.close();
//                
//                if(!oldUnitId.equals("")){
//                    
//                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + oldUnitId + "','"
//                        + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',false," + driverId + ",true)");
//                
//                }
//                
//                
//                
//                //generisanje automatic odjave drugog sa ovog vozila
//                 result = query.executeQuery("select current_driver_id,current_ibutton,current_driver from unit where id='"
//                        + unitId + "' and current_driver_id<>" + driverId + ";");
//
//                 String oldDriverId="";
//                 String oldDriverName="";
//                  String oldIButton="";
//                while (result.next()) {
//                    oldDriverId=result.getString("current_driver_id");
//                    
//                    oldIButton=result.getString("current_ibutton");
//                    oldDriverName=result.getString("current_driver");        
//
//                };
//                result.close();
//                
//                if(!oldDriverId.equals("")){
//                    
//                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + unitId + "','"
//                        + dan + "','" + vremeOd + "','" + oldIButton + "','" + oldDriverName + "',false," + oldDriverId + ",true)");
//                
//                }
//                
//                
//                //odjava sa drugih vozila
//                query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id<>'"
//                        + unitId + "' and current_driver_id=" + driverId);
//
//                
//                
//                
//                
//                
//                
//                
//                
//                
//                
//                if(rfidString.equals(current_ibutton)){
//                    long secondsDiff=600;
//                    if(ibuttonTime!=null){
//                        Date ibuttonDate= new Date(ibuttonTime.getTime());
//                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
//                    }
//
//                    System.out.println("diff sec " + secondsDiff);
//
//                   if(secondsDiff>60 ){
//                       query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id='" + unitId + "'");
//
//
//                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
//                        + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',false," + driverId + ")");
//                        
//                        rfidResponse="ok2";//odjava
//                   }else{
//                       //ignore
//                       System.out.println("ignore rfid diff " + secondsDiff);
//                       
//                       if(secondsDiff<0){
//                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
//                        + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',false," + driverId + ")");
//                       }
//                       
//                       rfidResponse="ok1";//i dalje prijava dok ne prodje 60 sekundi
//                   }
//                }else{
//
//                     long secondsDiff=600;
//                     
//                     
//                    
//                    
//                    if(new Date(timestampIbutton.getTime()).compareTo(dateFormatter.parse("2090-01-01"))>0){
//                             secondsDiff=-1;
//                    }else{
//                        if(ibuttonTime!=null){
//                        Date ibuttonDate= new Date(ibuttonTime.getTime());
//                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
//           
//                        }
//                    }
//
//                    System.out.println("diff sec " + secondsDiff);
//
//                    //nova prijava mzoe da dodje samo posle stare
//                    if(secondsDiff>0 ){
//                    //nova prijava
//                    System.out.println(" rfid prijava " + rfidString);
//                     rfidResponse="ok1";//prijava
//
//                    query.executeUpdate("update unit set current_ibutton='" + rfidString + "',current_driver='" + driver
//                            + "',current_driver_id=" + driverId + ",ibutton_time='"+ timestampFormatter.format(timestampIbutton) + "' where id='" + unitId + "'");
//
//
//                    
//                    }
//                    
//                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
//                            + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',true," + driverId + ")");
//
//                    
//                }
//            
//            }else{
//                
//                //za ugradnju
//                query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava) values ('" + unitId + "','"
//                            + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',true)");
//                
//                 rfidResponse="ok1";//uvek prijava na ugradnji ako nemamo vozaca
//
//            }
//            
//            
//            
//        } catch (Exception e) {
//
//            e.printStackTrace();
//            if (logger.isDebugEnabled()) {
//                logger.error(
//                        "'Exception' - while updating unit rfid - "
//                        + e.getMessage());
//            }
//
//        }
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("<--");
//        }
//        
//        return rfidResponse;
//
//    }
      
      
      public String saveRfidRequest(String simCard, String rfidString,String timestampString,String responseBase) {

       
        String rfidResponse=responseBase;
       
        try {
            
           
            
             String unitId = getSerialNumber(simCard).get_serialNumber();
            
//        Calendar cal = Calendar.getInstance();
//        Date now = cal.getTime();
//
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
        DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        
                
            System.out.println("timestampString " + timestampString);
        Date timestampIbutton=timestampFormatter.parse(timestampString);
//
        String dan = dateFormatter.format(timestampIbutton);
        String vremeOd = timeFormatter.format(timestampIbutton);
        
        
        if(new Date(timestampIbutton.getTime()).compareTo(dateFormatter.parse("2090-01-01"))>0){
            
           
            //replace with now
                    timestampIbutton=new Date();
                    dan = dateFormatter.format(timestampIbutton);
                    vremeOd = timeFormatter.format(timestampIbutton);
                    
                     logger.error(
                        timestampString + " change to "
                        + dan + vremeOd);
                    
        }
       
        //String vremeOd=timeStamp;
       
            
            ResultSet result = query.executeQuery("select current_ibutton,ibutton_time from unit where id='" +  unitId +  "';");

            String current_ibutton="";
            Timestamp ibuttonTime=null;
            while (result.next()) {
               
                current_ibutton=result.getString("current_ibutton");
                ibuttonTime=result.getTimestamp("ibutton_time");
                
            };
            
            result.close();
            
            
            
            result = query.executeQuery("select id,ime_prezime from vozaci where ibutton='" + rfidString + "';");

            Integer driverId=0;
            String driver="";
            while (result.next()) {
                driverId=result.getInt("id");
                driver=result.getString("ime_prezime");
                
            };
            
            result.close();
            
             System.out.println(" rfid driver " + driver);
            
            if(driverId>0){
                
                
                
                //generisanje automatic odjave sebe sa drugih vozila
                 result = query.executeQuery("select * from unit where id<>'"
                        + unitId + "' and current_driver_id=" + driverId + ";");

                 String oldUnitId="";
                while (result.next()) {
                    oldUnitId=result.getString("id");
                    

                };
                result.close();
                
                if(!oldUnitId.equals("")){
                    
                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + oldUnitId + "','"
                        + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',false," + driverId + ",true)");
                
                }
                
                
                
                //generisanje automatic odjave drugog sa ovog vozila
                 result = query.executeQuery("select current_driver_id,current_ibutton,current_driver from unit where id='"
                        + unitId + "' and current_driver_id<>" + driverId + ";");

                 String oldDriverId="";
                 String oldDriverName="";
                  String oldIButton="";
                while (result.next()) {
                    oldDriverId=result.getString("current_driver_id");
                    
                    oldIButton=result.getString("current_ibutton");
                    oldDriverName=result.getString("current_driver");        

                };
                result.close();
                
                if(!oldDriverId.equals("")){
                    
                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + unitId + "','"
                        + dan + "','" + vremeOd + "','" + oldIButton + "','" + oldDriverName + "',false," + oldDriverId + ",true)");
                
                }
                
                
                //odjava sa drugih vozila
                query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id<>'"
                        + unitId + "' and current_driver_id=" + driverId);

                
                
                
                
                
                
                
                
                
                
                if(rfidString.equals(current_ibutton)){
                    long secondsDiff=600;
                    if(ibuttonTime!=null){
                        Date ibuttonDate= new Date(ibuttonTime.getTime());
                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
                    }

                    System.out.println("diff sec " + secondsDiff);

                   if(secondsDiff>60 ){
                       query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id='" + unitId + "'");


                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
                        + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',false," + driverId + ")");
                        
                        rfidResponse=responseBase + "2";//odjava ok2 ili ack2
                   }else{
                       //ignore
                       System.out.println("ignore rfid diff " + secondsDiff);
                       
                       if(secondsDiff<0){
                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
                        + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',false," + driverId + ")");
                       }
                       
                       rfidResponse=responseBase + "1";//i dalje prijava dok ne prodje 60 sekundi
                   }
                }else{

                     long secondsDiff=600;
                     
                     
                    
                    
                    if(new Date(timestampIbutton.getTime()).compareTo(dateFormatter.parse("2090-01-01"))>0){
                             secondsDiff=-1;
                    }else{
                        if(ibuttonTime!=null){
                        Date ibuttonDate= new Date(ibuttonTime.getTime());
                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
           
                        }
                    }

                    System.out.println("diff sec " + secondsDiff);

                    //nova prijava mzoe da dodje samo posle stare
                    if(secondsDiff>0 ){
                    //nova prijava
                    System.out.println(" rfid prijava " + rfidString);
                     rfidResponse=responseBase + "1";//prijava

                    query.executeUpdate("update unit set current_ibutton='" + rfidString + "',current_driver='" + driver
                            + "',current_driver_id=" + driverId + ",ibutton_time='"+ timestampFormatter.format(timestampIbutton) + "' where id='" + unitId + "'");


                    
                    }
                    
                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
                            + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',true," + driverId + ")");

                    
                }
            
            }else{
                
                //za ugradnju
                query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava) values ('" + unitId + "','"
                            + dan + "','" + vremeOd + "','" + rfidString + "','" + driver + "',true)");
                
                //uvek prijava na ugradnji ako nemamo vozaca, ali vrati Rajku 3
                 rfidResponse=responseBase + "3";

            }
            
            
            
        } catch (Exception e) {

            e.printStackTrace();
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while updating unit rfid - "
                        + e.getMessage());
            }

        }

       
        
        return rfidResponse;

    }
     
     
      
      private  String rearrangeRfidHex(String inputHex){
          
          String to_return = inputHex.substring(0,inputHex.length() - 2);
          logger.debug(inputHex + " rearrangeRfidHex  " + to_return);
          return to_return;
//          String output=inputHex;
//          try{
//            //System.out.println("inputHex " + inputHex);
//            StringBuffer sb=new StringBuffer();
//
//            inputHex=inputHex.substring(2, inputHex.length());
//
//             //System.out.println("cut 01 inputHex " + inputHex);
//
//            for(int i=inputHex.length()-1;i>0;i--){
//                i--;
//
//
//                sb.append(inputHex.substring(i,i+2));
//
//                //System.out.println("##" + inputHex.substring(i,i+2));
//            }
//             output=sb.toString();
//
//            output=output.substring(8, output.length());
//
//            logger.debug(inputHex + " rearranged  " + output);
//          }catch (Exception e){
//              logger.error(inputHex + " rearranged  error");
//          }
//            return output;
    }
      
 

 
    
    
    public String getUnitOpis(String serialNo) {

     

      String opis="";

        try {

            ResultSet result = query.executeQuery("select ime from unit where id='" + serialNo + "'");

            result.next();

            opis = result.getString("ime");
         
            result.close();

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while getting serial number  - "
                        + sqle.getMessage());
            }

        }

        
        return opis;
    }

    /**
     *
     * Vraca vreme od kada ne stizu paketi
     */
    public String getLastMessageTime() {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        String lastMessageTime = "";

        try {

            ResultSet result = query.executeQuery("select max(gsm_vreme) as max_vreme from last_position where tip_poruke='USSD'");

            result.next();

            lastMessageTime = result.getString("max_vreme").substring(8, 10)
                    + "." + result.getString("max_vreme").substring(5, 7)
                    + "." + result.getString("max_vreme").substring(0, 4)
                    + " " + result.getString("max_vreme").substring(11, 19);

            result.close();

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while getting last message time  - "
                        + sqle.getMessage());
            }

            return lastMessageTime;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        return lastMessageTime;
    }

    /**
     * This method returns last position of vehicle
     *
     */
    public String getLastPosition(String serialNumber) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        String lastPositionInfo = "";

        try {

            ResultSet result = query.executeQuery("select unit_id,gps_vreme,ulica_i_broj,grad,udaljenost,drzava,brzina,stanje_vozila from last_position where unit_id = '" + serialNumber + "'");

            result.next();

            String[] fields = new String[6];

            fields[0] = result.getString("gps_vreme").substring(8, 10)
                    + "." + result.getString("gps_vreme").substring(5, 7)
                    + "." + result.getString("gps_vreme").substring(0, 4)
                    + " " + result.getString("gps_vreme").substring(11) + ", ";

            DecimalFormat myFormatter = new DecimalFormat("###.#");

            if (result.getDouble("udaljenost") < 1) {
                fields[1] = (int) (result.getDouble("udaljenost") * 1000) + " m od ";
            } else {
                fields[1] = myFormatter.format(result.getDouble("udaljenost")) + " km od ";

            }

            if (!result.getString("ulica_i_broj").equals("")) {
                fields[2] = result.getString("ulica_i_broj") + ", ";
            } else {
                fields[2] = "";
            }

            fields[3] = result.getString("grad") + ", ";
            fields[4] = result.getString("drzava") + ", ";

            if (result.getString("stanje_vozila").equals("Voznja")) {
                fields[5] = result.getString("brzina") + " km/h";
            } else {
                fields[5] = result.getString("stanje_vozila");
            }

            for (int i = 0; i < 6; i++) {
                lastPositionInfo += fields[i];
            }

            result.close();

        } catch (SQLException sqle) {

            lastPositionInfo = "Nema podataka za trazeno vozilo.";

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while getting last position info  - "
                        + sqle.getMessage());
            }

            return lastPositionInfo;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        return lastPositionInfo;
    }

    /**
     * This method saves GPS message to database.
     *
     */
    public void saveMessage(GPSMessage poruka, String unit_id) {
        if (logger.isDebugEnabled()) {
            logger.debug("-->, poruka = " +
                    poruka + ", unit_id = " + unit_id);
        }
        //koristi se samo ako se desio error
        String tip_poruke = "";
        GeoAgent geo_data = initializeGeoAgent(poruka.getGPSLat(), poruka.getGPSLong());
        
        boolean is_with_sonda_data = false;
        
        if (logger.isDebugEnabled()) {
            logger.debug("geo_data = " + geo_data);
        }
        
//        private String _streetNumber;
//        private String _community;
//        private String _city;
//        private String _state;
//        private double _distance;
//        private double _direction;
//        private double _angleRt;
//        private double _latitude;
//        private double _longitude;
//        private int _timeZone;
//        private String _fix;
        
        poruka.setStreetNumber(geo_data.getStreetNumber());
        poruka.setCommunity(geo_data.getCommunity());
        poruka.setCity(geo_data.getCity());
        poruka.setState(geo_data.getState());
        poruka.setDistance(geo_data.getDistance());
        poruka.setAngleRt((int)geo_data.getAngleRt());
        
        
        int geneko_status_int = Integer.parseInt(poruka.getGeneco_status_id());
        
        //////////////////////izbaciti poruke koje imaju longitude i latitude 0 ////
        
        if(poruka.getGPSLat() <= 0 && poruka.getGPSLong() <= 0){
            if (logger.isDebugEnabled()) {
                logger.warn("#### poruka sa long i lat = 0, ne obradjujemo je ");
            }
            return;
        }
         
        //////////////////////////kraj za longitude i latitude 0////////////////////
        
        
        ////////////////izbacujemo i poruke o statusu gps antene, samo smetaju///////
        
        if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_GSM_ANTENNA || 
                geneko_status_int == ProtocolDecoderOldGeneko.STATUS_13_UNKNOWN){
            if (logger.isDebugEnabled()) {
                logger.warn("#### poruka statusa GPS Antene i status 13 - UNKNOWN, ne obradjujemo je ");
            }
            return;
        }
        
        ///////////////kraj poruke o statusu gps antene///////////////////////////////
 
        

        String gpsPorukaTimeStamp = poruka.getLocalDateTime();
        String init_stanje_vozila = poruka.getSpeed() > 0 ? "Voznja" : "Parkiran";
        String lastPositionPorukaTimeStampAndStatusAndLongLatAndSondaFuel = getLastPositionTimeAndStatusAndLongLatAndSondaFuel(poruka.getSerialNumber(), init_stanje_vozila);
        if (logger.isDebugEnabled()) {
            logger.debug("#### lastPositionPorukaTimeStampAndStatusAndLongLatAndSondaFuel = " + lastPositionPorukaTimeStampAndStatusAndLongLatAndSondaFuel);
        }
        String[] ret = lastPositionPorukaTimeStampAndStatusAndLongLatAndSondaFuel.split(",");
        String lastPositionPorukaTimeStamp = ret[0];
        String lastPositionPorukaStatus = ret[1];
        String last_longitude = ret[2];
        String last_latitude = ret[3];
        String last_ulica_broj = ret[4];
        String last_opstina = ret[5];
        String last_grad = ret[6];
        String last_drzava = ret[7];
        String last_distance = ret[8];
        String last_angle_rt = ret[9];
        String last_sonda_fuel_1 = ret[10];
        String last_sonda_fuel_2 = ret[11];
        double last_sonda_fuel_1_double = Double.parseDouble(last_sonda_fuel_1);
        double last_sonda_fuel_2_double = Double.parseDouble(last_sonda_fuel_2);
        
        if (logger.isDebugEnabled()) {
            logger.debug("#### logika odredjivanja statusa: dobio geneko_status_id = " + poruka.getGeneco_status_id() + 
                    ", prethodni status = " + lastPositionPorukaStatus + ", poruka.getGPSSpeed() = " + poruka.getSpeed() + 
                    ", geneko_status_int = " + geneko_status_int + ", input_polje = " + poruka.get_input());
        }
        
        boolean is_uredjaj_upaljen = false;
        if(poruka.get_input().length() > 10){
            is_uredjaj_upaljen = poruka.get_input().subSequence(8, 9).equals("1");
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("#### is_uredjaj_upaljen = " + is_uredjaj_upaljen);
        }
        
        
///////////////////////////////////////////////geneko advanced bug resenje /////////////////////////////////////////////////
        //kod geneko advanced uredjaja, kada se probudi, odnosno upali, dolazi da slanja poruka iz cache-a uredjaja u istoj sekundi
        //ove poruke sve treba ignorisati sem poruke da se uredjaj upalio jer poruke nastaju 
        //kada je vozilo parkirano
        
        Calendar local_gsm_date_time_calendar = getLocalDateTimeCalendar(poruka.getLocalDateTime());
        
       
        if (logger.isDebugEnabled()) {
            logger.debug("#### za logika resavanja GENEKO Advance problema, local_gsm_date_time_calendar " + 
                    local_gsm_date_time_calendar);
        }


        
        if(local_gsm_date_time_calendar != null){
            //sve ove poruke su sa geneko_
            if(  lastPositionPorukaStatus.equals("Parkiran") &&  geneko_status_int == 0 &&  is_poruka_from_past(local_gsm_date_time_calendar)){
                //ovu poruku ignorisati
                if (logger.isDebugEnabled()) {
                    logger.debug("#### logika resavanja GENEKO Advance problema, IGNORISE se ova poruka, zakasnela 5 sekundi" + 
                            ", prethodni status = " + lastPositionPorukaStatus + ", geneko_status_int = " + geneko_status_int + 
                            ", gps vreme = " + poruka.getLocalDateTime());
                }
                return;
            }

            if(lastPositionPorukaStatus.equals("Parkiran") &&  geneko_status_int == ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_ON){
                if (logger.isDebugEnabled()) {
                    logger.debug("#### logika resavanja GENEKO Advance problema, vreme poruke upaljen ubaceno u cache, " + 
                            ", prethodni status = " + lastPositionPorukaStatus + ", geneko_status_int = " + geneko_status_int + 
                            ", gps vreme = " + poruka.getLocalDateTime());
                }
                GenekoPorukaCache.put(unit_id, local_gsm_date_time_calendar);
            }
            
            Calendar poruka_upaljen_vreme = GenekoPorukaCache.getHashedPorukaGsmDateTime(unit_id);
            
            //ignorisemo ako je poruka sa uredjaja pristigla za manje od pet sekundi posto je pristigla poruka da je vozilo upaljeno
            if(poruka_upaljen_vreme != null){
                if(geneko_status_int == 0 ){
                    if(is_druga_poruka_less_5_seconds(poruka_upaljen_vreme, local_gsm_date_time_calendar)){
                        if (logger.isDebugEnabled()) {
                            logger.debug("#### logika resavanja GENEKO Advance problema, IGNORISE se ova poruka, dosla u periodu od 5 sekundi posto je dosla poruka UKLJUCEN " + 
                                    ", prethodni status = " + lastPositionPorukaStatus + ", geneko_status_int = " + geneko_status_int + 
                                    ", gps vreme = " + poruka.getLocalDateTime());
                        }
                        return;
                    }
                }
            }
            
            

        }
        
        
        
        
        
//////////////////////////////////////geneko advanced bug resenje END ///////////////////////////////////////////////////////
        if(is_sonda_fuel_poruka(geneko_status_int)){
            is_with_sonda_data = true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("#### is_with_sonda_data = " + is_with_sonda_data );
        }
        
        String novi_status = "";
//////////////////////////////////odredjivanje statusa ////////////////////////////////////////////
        
        if(unit_id.equalsIgnoreCase("15804") || unit_id.equalsIgnoreCase("15562") ||
                unit_id.equalsIgnoreCase("16050") || unit_id.equalsIgnoreCase("1248") ||
                unit_id.equalsIgnoreCase("15602") ) {
            if (logger.isDebugEnabled()) {
                logger.debug("#### unit_id = "+unit_id+" odredjivanje statusa BEZZ input polja, uredjaji sa bug-om. Oslanjamo se najvise na brzinu "  );
                logger.debug("#### unit_id = "+unit_id+" BEZZ lastPositionPorukaStatus="+lastPositionPorukaStatus+", poruka.getSpeed()="+poruka.getSpeed()+
                        " , geneko_status_int="+geneko_status_int  );
            } 
            if(lastPositionPorukaStatus.equals("Parkiran")){
                if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_ON
                /*poruka.getSpeed() > 1*/ /*#### korekcija BUG-a 27, ostaje Parkiran iako je brzina veca od 1 */){
                    //ide u upaljen
                    novi_status = "Upaljen";
                }else{
                    if(poruka.getSpeed() > 0){
                        novi_status = "Voznja";
                    }else{
                    //ostaje parkiran
                        novi_status = "Parkiran";
                    }
                }
            }else{
                if(lastPositionPorukaStatus.equals("Upaljen")){
                    if(poruka.getSpeed() > 0){
                        //ide u upaljen
                        novi_status = "Voznja";
                    }else{
                        //ostaje parkiran
                        novi_status = "Parkiran";
                    }
                }else{
                    if(lastPositionPorukaStatus.equals("Voznja")){
                        if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_OFF){
                            novi_status = "Parkiran";
                        }else{
                            if(poruka.getSpeed() > 0){
                                //ostaje voznja
                                novi_status = "Voznja";
                            }else{
                                //ide u parkiran
                                novi_status = "Parkiran";
                            }
                        }
                    }else{
                        if(lastPositionPorukaStatus.equals("Alarm sonda")){
                            //ide u novi status
                            if(is_uredjaj_upaljen){
                                novi_status = "Voznja";
                            }else{
                                novi_status = "Parkiran";
                            }
                        }else{
                            novi_status = lastPositionPorukaStatus;
                        }
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("#### unit_id = "+unit_id+" BEZZ novi_status="+novi_status );
            } 
            
        }else{
            if (logger.isDebugEnabled()) {
                logger.debug("#### odredjivanje statusa SA input poljem "  );
            }    
            if(lastPositionPorukaStatus.equals("Parkiran")){
                if(is_uredjaj_upaljen || (geneko_status_int == ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_ON)
                /*poruka.getSpeed() > 1*/ /*#### korekcija BUG-a 27, ostaje Parkiran iako je brzina veca od 1 */){
                    //ide u upaljen
                    novi_status = "Upaljen";
                }else{
                    //ostaje parkiran
                    novi_status = "Parkiran";
                }
            }else{
                if(lastPositionPorukaStatus.equals("Upaljen")){
                    if(is_uredjaj_upaljen || poruka.getSpeed() > 1){
                        //ide u upaljen
                        novi_status = "Voznja";
                    }else{
                        //ostaje parkiran
                        novi_status = "Parkiran";
                    }
                }else{
                    if(lastPositionPorukaStatus.equals("Voznja")){
                        if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_OFF){
                            novi_status = "Parkiran";
                        }else{
                            if(is_uredjaj_upaljen || poruka.getSpeed() > 1){
                                //ostaje voznja
                                novi_status = "Voznja";
                            }else{
                                //ide u parkiran
                                novi_status = "Parkiran";
                            }
                        }
                    }else{
                        if(lastPositionPorukaStatus.equals("Alarm sonda")){
                            //ide u novi status
                            if(is_uredjaj_upaljen){
                                novi_status = "Voznja";
                            }else{
                                novi_status = "Parkiran";
                            }
                        }else{
                            novi_status = lastPositionPorukaStatus;
                        }
                    }
                }
            }
        }
        
        //ispravka bug-a
        if(novi_status.equals("Upaljen") && lastPositionPorukaStatus.equals("Upaljen")){
            if (logger.isDebugEnabled()) {
                logger.debug("#### upao u bug, oba Upaljen. Postavio novi na Voznja "  );
            }
            if(is_uredjaj_upaljen){
                novi_status = "Voznja";
            }else{
                novi_status = "Parkiran";
            }
        }
         
 ////////////////////////////////////////odredjivanje statusa end///////////////////////////////////////
        
        
        if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_WDT_START){
            //izmena iz TStatus u SLEEP - ticket 115
            novi_status = "SLEEP";
            //novi_status = "TStatus";
            //ovde uvodimo novinu. Posto znamo da je u ovom statusu vozilo parkirano, ako 
            //prethodni status nije Parkiran, onda novi setujemo na Parkiran. U suprotnom, ostaje TStatus
            if(!lastPositionPorukaStatus.equalsIgnoreCase("Parkiran")){
                novi_status = "Parkiran";
            }
            if (logger.isDebugEnabled()) {
                logger.debug("PORUKA STATUS_WDT_START , emituje se na 4 sata = " + novi_status + ", prethodni status = " + lastPositionPorukaStatus);
            }
            
        }
        
        if(poruka.getGPSFix() != null && !poruka.getGPSFix().equals("A")){
            

            //ukoliko je signal dosao da je upaljen, ili ugasen, onda ovu poruku moramo da uzmemo u obzir. Poruka ce biti da je
            //upaljen ili ugasen ali moraju se uzeti podaci o latitudi i longitudi iz prethodne poruke
            if(novi_status.equalsIgnoreCase("Upaljen") || novi_status.equalsIgnoreCase("Parkiran")){
                //ostavi taj status iako je poruka bez fix ali moras da ubacis polozaj od prethodne poruke

                poruka.setStreetNumber(last_ulica_broj);
                poruka.setCommunity(last_opstina);
                poruka.setCity(last_grad);
                poruka.setState(last_drzava);
                poruka.setDistance(Double.parseDouble(last_distance));
                poruka.setAngleRt(Integer.parseInt(last_angle_rt));
                
                poruka.setGpsLongNonFormated(Double.parseDouble(last_longitude));
                poruka.setGpsLatNonFormated(Double.parseDouble(last_latitude)); 
                //koje vreme da stavimo, da li je u error poruci lose vreme ili nije
                if(isGpsPorukaDateToday(gpsPorukaTimeStamp)){
                    //ostaje isto, ako ne, onda od prethodnog
                }else{
                    poruka.setLocalDateTime(lastPositionPorukaTimeStamp);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("PORUKA BEZ FIX-a, posebna logika za evente, ukljucen na napajnje i iskljucen sa napajanja, poruka = " +
                            poruka);
                }
            }else{
                novi_status = lastPositionPorukaStatus;
                //po ognjenovoj preporuci
                if(!poruka.getGPSFix().equals("V")){
                    tip_poruke = "ERROR";
                }
                
            }
            if (logger.isDebugEnabled()) {
                logger.debug("PORUKA BEZ FIX-a = " + novi_status + ", prethodni status = " + lastPositionPorukaStatus + ", tip_poruke = " +
                        tip_poruke);
            }
        }
        
        if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_GSM_ANTENNA){
            tip_poruke = "ERROR_GSM_GENECO";
        }
        
        Calendar jedan_mesec_u_buducnosti = Calendar.getInstance();
        //stavicemo jedan dan
        jedan_mesec_u_buducnosti.add(Calendar.DAY_OF_YEAR, 1);
        String jedan_mesec_u_buducnosti_str = timestampFormatter.format(jedan_mesec_u_buducnosti.getTime());
        
        if(isGpsPorukaDateBiggerThanDate2(gpsPorukaTimeStamp, jedan_mesec_u_buducnosti_str)){
            tip_poruke = "ERROR";
            if (logger.isDebugEnabled()) {
                logger.debug("PORUKA koja je pristigla je sa gsm datumom vecim od 1-og dana u odnosu na sadasnje vreme" +
                		", gsmVreme = " + gpsPorukaTimeStamp + 
                		". Poruka je oznacena kao ERROR");
            }
        }
        if( lastPositionPorukaStatus.equals("Parkiran") && novi_status.equalsIgnoreCase("Parkiran")){
            //sve OSNOVNE poruke sa statusom parkiran stanjem moraju da se NE vide na user aplikaciji
            tip_poruke = "HIDDEN";
        }
        
        if( lastPositionPorukaStatus.equals("Alarm sonda") && novi_status.equalsIgnoreCase("Parkiran")){
            //sve OSNOVNE poruke sa statusom parkiran stanjem moraju da se NE vide na user aplikaciji
            tip_poruke = "HIDDEN";
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("is_sonda_fuel_poruka = " + 
                    is_sonda_fuel_poruka(geneko_status_int));
        }

        
        ////////////////////////deo zaduzen za sonde//////////////////////////
        if(is_sonda_fuel_poruka(geneko_status_int)){
            
            //zbog problema u hardware-u i firmware-u advanced geneko uredjaja, ovakve poruke ignorisemo
            //ispostavilo se da vremenom napon na sonda uredjajima pada ako je vozilo dugo parkirano, tako da 
            //dobijeni nivo goriva nije realan
            if(novi_status.equals("Parkiran") && lastPositionPorukaStatus.equals("Parkiran")){
                if (logger.isDebugEnabled()) {
                    logger.info("######kada je sonda_fuel poruka i kada je status poruke Parkiran, poruku ignorisemo " );
                }
                return;
            }
            
            //svaka poruka koja se odnosi na sonde mora da sadrzi vrednost za obe sonde
            //ukoliko je poruka pristigla za sondu 1, onda vrednost za sondu 2 se uzima poslednja iz last_position tabele
            //nova vrednost za sondu 1 ili 2 ce overrideovati odgovarajucu vrednost u poruci koja ide u bazu
            
            poruka.setSonda1FuelLitars(last_sonda_fuel_1);
            poruka.setSonda2FuelLitars(last_sonda_fuel_2);
            
            
            double last_sonda_fuel_ukupno_double = last_sonda_fuel_1_double + last_sonda_fuel_2_double;
            poruka.setSondaFuelLitars(decimalFormat(last_sonda_fuel_ukupno_double));
            
            if (logger.isDebugEnabled()) {
                logger.debug("last_sonda_fuel_1_double = " + 
                        last_sonda_fuel_1_double + ", last_sonda_fuel_2_double = " + last_sonda_fuel_2_double
                        + ", last_sonda_fuel_ukupno_double = " + decimalFormat(last_sonda_fuel_ukupno_double));
            }
            
            
            int current_sonda_fuel_litars_1 = 0;
            int current_sonda_fuel_litars_2 = 0;
            if(poruka.getSondaFuelValue() != null){


                //prvo izracunati srednju vrednost
                //u nasem slucaju, messageValue koja se dobija sa geneko uredjaja ima 16 Integer vrednosti (odvojene su prazninom)
                //njih treba usrednjiti te se tako dobija Integer vrednost


                //setujemo za prvu sondu i za total litars - svi genekovi uredjaji se setuju da sabiraju
                //vrednositi sa prve i druge sonde pa se zato  setuje total litars
                String litars = calculateSondaFuel(poruka.getSerialNumber(), poruka.getSondaFuelValue(), new Integer(1));
                current_sonda_fuel_litars_1 = (int)Double.parseDouble(litars);
                double ukupno_litara = current_sonda_fuel_litars_1 + last_sonda_fuel_2_double;
                if (logger.isDebugEnabled()) {
                    logger.debug("Sonda 1, postavljanje vrednosti za litre usrednjena vrednost za fule value = " + 
                            poruka.getSondaFuelValue());
                    logger.debug("Sonda 1 poruka,ukupno litara = current_sonda_fuel_litars_1 + last_sonda_fuel_2. " + 
                            "current_sonda_fuel_litars_1 = " + current_sonda_fuel_litars_1 + ", last_sonda_fuel_2_double = " + last_sonda_fuel_2_double +
                            ". Ukupno = " + ukupno_litara);
                }
                poruka.setSondaFuelLitars(decimalFormat(ukupno_litara));
                poruka.setSonda1FuelLitars(litars);
                if (logger.isDebugEnabled()) {
                    logger.debug("Sonda 1, ukupno, ukupno_litara = " + decimalFormat(ukupno_litara));
                }

            }
            if(poruka.getSonda2FuelValue() != null){

                if (logger.isDebugEnabled()) {
                    logger.debug("Sonda 2, postavljanje vrednosti za litre usrednjena vrednost za fule value = " + 
                            poruka.getSonda2FuelValue());
                }
                //prvo izracunati srednju vrednost
                //u nasem slucaju, messageValue koja se dobija sa geneko uredjaja ima 16 Integer vrednosti (odvojene su prazninom)
                //njih treba usrednjiti te se tako dobija Integer vrednost
                String litars = calculateSondaFuel(poruka.getSerialNumber(), poruka.getSonda2FuelValue(), new Integer(2));

                current_sonda_fuel_litars_2 = (int)Double.parseDouble(litars);
                
                double ukupno_litara = current_sonda_fuel_litars_2 + last_sonda_fuel_1_double;
                if (logger.isDebugEnabled()) {
                    logger.debug("Sonda 2, postavljanje vrednosti za litre usrednjena vrednost za fule value = " + 
                            poruka.getSonda2FuelValue());
                    logger.debug("Sonda 2 poruka,ukupno litara = current_sonda_fuel_litars_2 + last_sonda_fuel_1. " + 
                            "current_sonda_fuel_litars_2 = " + current_sonda_fuel_litars_2 + ", last_sonda_fuel_1_double = " + last_sonda_fuel_1_double +
                            ". Ukupno = " + decimalFormat(ukupno_litara));
                }
                poruka.setSondaFuelLitars(decimalFormat(ukupno_litara));
                poruka.setSonda2FuelLitars(litars);
                if (logger.isDebugEnabled()) {
                    logger.debug("Sonda 2, litri = " + litars);
                }
            }

            /////////////////////////deo za alarm prilikom istakanja goriva dok je vozilo parkirano/////////////
            if(lastPositionPorukaStatus.equalsIgnoreCase("Parkiran") && novi_status.equalsIgnoreCase("Parkiran")){
                

                //int last_sonda_fuel_int = 0;
                int razlika_fuel = 0;
    //////////////////////////////////////////za sondu 1////////////////////////////////////      
                if(!is_second_fuel_senzor(geneko_status_int)){

//                    try{
//                        last_sonda_fuel_int = (int)Double.parseDouble(last_sonda_fuel_1.trim());
//                    }catch(Throwable e){
//                        e.printStackTrace();
//                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Provera za sonda BROJ 1 alarm za uredjaj = " + 
                                poruka.getSerialNumber() + ". current_sonda_fuel_litars = " + current_sonda_fuel_litars_1 + 
                                ", last_sonda_fuel_1_double = " + last_sonda_fuel_1_double 
                                //+ ", last_sonda_fuel_int = " + last_sonda_fuel_int
                                );
                    }

                    razlika_fuel = (int)last_sonda_fuel_1_double - current_sonda_fuel_litars_1;

                    if (logger.isDebugEnabled()) {
                        logger.debug("Razlika fuel prethodni od tekuceg za sonda 1 alarm za uredjaj = " + 
                                razlika_fuel);
                    }

                    if( razlika_fuel >= SONDA_FUEL_DIFF_THRESHOLD_ALARD) {
                        //salji alarm
                        logger.debug("Salje sonda 1 alarm za uredjaj = " + 
                                poruka.getSerialNumber() );
                        tip_poruke = "ALARM";
                        sendSondaSMSAlarm(poruka, "" + current_sonda_fuel_litars_1, "" + last_sonda_fuel_1_double, poruka.getLocalDateTime());
//                        AlarmDBAgent alarm_agent = null;
//                        try{
//                            alarm_agent = new AlarmDBAgent();
//                            alarm_agent.insertReport(poruka.getSerialNumber(), "Aktiviran alarm.", SONDA_FUEL_DIFF_ALARM_TYPE_ID, "GENEKO ALARM GORIVO SONDA 1: " + poruka.getPosition(), "call centar");
//                        }catch(Exception e){
//                            logger.error("Unable to insert Alarm for fuel leak, device serial = " + 
//                                    poruka.getSerialNumber() + ", details: " + e.getMessage(),
//                                    e );
//                        }finally{
//                            if(alarm_agent != null){
//                                alarm_agent.close();
//                            }
//                        }

                    }
                }
                //////////////////////////////////////////kraj za sondu 1////////////////////////////////////
                else {
                    //////////////////////////////////////////za sondu 2////////////////////////////////////            

//                    try{
//                        last_sonda_fuel_int = (int)Double.parseDouble(last_sonda_fuel_2.trim());
//                    }catch(Throwable e){
//                        e.printStackTrace();
//                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Provera za sonda BROJ 2 alarm za uredjaj = " + 
                                poruka.getSerialNumber() + ". current_sonda_fuel_litars_2 = " + current_sonda_fuel_litars_2 + 
                                ", last_sonda_fuel_2_double = " + last_sonda_fuel_2_double 
                                //+ ", last_sonda_fuel_int = " + last_sonda_fuel_int
                                );
                    }
                    
                    razlika_fuel = (int)last_sonda_fuel_2_double - current_sonda_fuel_litars_2;

                    if (logger.isDebugEnabled()) {
                        logger.debug("Razlika fuel prethodni od tekuceg za sonda 2 alarm za uredjaj = " + 
                                razlika_fuel);
                    }

                    if( razlika_fuel >= SONDA_FUEL_DIFF_THRESHOLD_ALARD) {
                        //salji alarm
                        logger.debug("Salje sonda 2 alarm za uredjaj = " + 
                                poruka.getSerialNumber() );
                        tip_poruke = "ALARM";
                        sendSondaSMSAlarm(poruka, "" + current_sonda_fuel_litars_2, "" + last_sonda_fuel_2_double, poruka.getLocalDateTime());
//                        AlarmDBAgent alarm_agent = null;
//                        try{
//                            alarm_agent = new AlarmDBAgent();
//                            alarm_agent.insertReport(poruka.getSerialNumber(), "Aktiviran alarm.", SONDA_FUEL_DIFF_ALARM_TYPE_ID, "GENEKO ALARM GORIVO SONDA 2: " + poruka.getPosition(), "call centar");
//                        }catch(Exception e){
//                            logger.error("Unable to insert Alarm for fuel leak, device serial = " + 
//                                    poruka.getSerialNumber() + ", details: " + e.getMessage(),
//                                    e );
//                        }finally{
//                            if(alarm_agent != null){
//                                alarm_agent.close();
//                            }
//                        }

                    }
                }
                //////////////////////////////////////////kraj za sondu 2////////////////////////////////////
                
                
            }
            /////////////////////////kraj dela za alarme////////////////////////////////////////////////////////
            
        }
        /////////////////////////kraj dela zaduzenog za sonde ////////////////

        
        if (logger.isDebugEnabled()) {
            logger.debug("tip_poruke = " + tip_poruke);
            logger.debug("novi_status = " + novi_status + ", prethodni status = " + lastPositionPorukaStatus);
        }
        
        //ispravka bug-a
        if(novi_status.equals("Upaljen") && lastPositionPorukaStatus.equals("Upaljen")){
            if (logger.isDebugEnabled()) {
                logger.debug("#### upao u bug, oba Upaljen. Postavio novi na Voznja "  );
            }
            if(is_uredjaj_upaljen){
                novi_status = "Voznja";
            }else{
                novi_status = "Parkiran";
            }
        }
        
        if(tip_poruke.equals("ALARM")){
            novi_status = "Alarm sonda";
        }
        
        poruka.setMessageType(tip_poruke);
        poruka.setStatus(novi_status);
        
        //staviti poslednju vrednost sa sondi na poruku vozilo upaljeno i vozilo ugaseno
        if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_ON || 
                geneko_status_int == ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_OFF ) {
            if(last_sonda_fuel_1 != null){
                if(!last_sonda_fuel_1.equals("")){
                    double last_ukupno = last_sonda_fuel_1_double + last_sonda_fuel_2_double;
                    poruka.setSondaFuelLitars(decimalFormat(last_ukupno));
                    if (logger.isDebugEnabled()) {
                        logger.debug("######poruka contact on off, ukupno sonda value = " + decimalFormat(last_ukupno));
                    }
                    poruka.setSonda1FuelLitars(last_sonda_fuel_1);
                }
            }
            if(last_sonda_fuel_2 != null){
                if(!last_sonda_fuel_2.equals("")){
                    poruka.setSonda2FuelLitars(last_sonda_fuel_2);
                }
            }
        }
        
        
        if (logger.isDebugEnabled()) {
            logger.debug("######poruka pred upisivanjem = " + poruka);
            logger.info("######poruka pred upisivanjem, TIP PORUKE = " + tip_poruke + ", status = " + novi_status);
        }

        //ako poruka nije error, ako joj je vreme vece od poslednje poruke i
        //ako joj vreme nije vece od godinu dana od starta servera upisujemo u last position  
        if (/*!poruka.isError() && !poruka.getMessageType().equals("HIDDEN")  && */
                isGpsPorukaDateBiggerThanDate2(gpsPorukaTimeStamp, lastPositionPorukaTimeStamp)
//                && !isGpsPorukaDateBiggerThanDate2(gpsPorukaTimeStamp, Service._oneYearFromStartingServerDate)
                ) {
             logger.debug(poruka.getGSMNumber() + " / " + poruka.getSerialNumber() + " last position on " + gpsPorukaTimeStamp + " saved " );
             saveToHistory(poruka, unit_id);
             
             //status upaljen with lat/long 0 is not error and is replaced with last position
             if(poruka.getGPSLat()!=0 && poruka.getGPSLong()!=0){
                 if(novi_status.equalsIgnoreCase("TStatus") || tip_poruke.equalsIgnoreCase("ERROR") || 
                         tip_poruke.equalsIgnoreCase("ERROR_GSM_GENECO") || tip_poruke.equalsIgnoreCase("SLEEP")){
                     
                 }else{
                     saveLastPosition(poruka, unit_id, is_with_sonda_data);
                     is_with_sonda_data = false;
                 }
                
             }
       
        }else {
            logger.debug(poruka.getGSMNumber() + " / " + poruka.getSerialNumber() + " last position on " + gpsPorukaTimeStamp + " NOT saved " );
           
            saveToHistory(poruka, unit_id);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    public boolean isGpsPorukaDateBiggerThanDate2(String gpsPorukaTimeStamp, String date2) {
        boolean value = true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String porukaDatum = gpsPorukaTimeStamp.split(" ")[0];
            String porukaVreme = gpsPorukaTimeStamp.split(" ")[1];

            int porukaGodina = Integer.parseInt(porukaDatum.split("-")[0]);
            //meseci idu od 0 do 11
            int porukaMesec = Integer.parseInt(porukaDatum.split("-")[1]) - 1;
            int porukaDan = Integer.parseInt(porukaDatum.split("-")[2]);

            int porukaSat = Integer.parseInt(porukaVreme.split(":")[0]);
            int porukaMinut = Integer.parseInt(porukaVreme.split(":")[1]);
            int porukaSekund = Integer.parseInt("" + Math.round(new Double(porukaVreme.split(":")[2])));

            Calendar calendarGpsPoruka = new GregorianCalendar(porukaGodina, porukaMesec, porukaDan, porukaSat, porukaMinut, porukaSekund);

            String lastPositionPorukaDatum = date2.split(" ")[0];
            String lastPositionPorukaVreme = date2.split(" ")[1];

            int lastPositionPorukaGodina = Integer.parseInt(lastPositionPorukaDatum.split("-")[0]);
            //meseci idu od 0 do 11
            int lastPositionPorukaMesec = Integer.parseInt(lastPositionPorukaDatum.split("-")[1]) - 1;
            int lastPositionPorukaDan = Integer.parseInt(lastPositionPorukaDatum.split("-")[2]);

            int lastPositionPorukaSat = Integer.parseInt(lastPositionPorukaVreme.split(":")[0]);
            int lastPositionPorukaMinut = Integer.parseInt(lastPositionPorukaVreme.split(":")[1]);
            int lastPositionPorukaSekund = Integer.parseInt("" + Math.round(new Double(lastPositionPorukaVreme.split(":")[2])));

            Calendar calendarLastPositionPoruka = new GregorianCalendar(lastPositionPorukaGodina, lastPositionPorukaMesec, lastPositionPorukaDan, lastPositionPorukaSat, lastPositionPorukaMinut, lastPositionPorukaSekund);

            if ((calendarGpsPoruka.compareTo(calendarLastPositionPoruka)) > 0) {

                //System.out.println(sdf.format(calendarGpsPoruka.getTime()) + " vece od  " + sdf.format(calendarLastPositionPoruka.getTime()));
                value = true;

            } else {
                //System.out.println(sdf.format(calendarGpsPoruka.getTime()) + " manje/jednako od " + sdf.format(calendarLastPositionPoruka.getTime()));
                value = false;

            }

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while comparing dates  - "
                        + e.getMessage());
            }
        }

        return value;
    }
    
    private boolean isGpsPorukaDateToday(String gpsPorukaTimeStamp){
        if (logger.isDebugEnabled()) {
            logger.warn("--> isGpsPorukaDateToday ,  gpsPorukaTimeStamp = " + gpsPorukaTimeStamp);
        }
        try{
        String porukaDatum = gpsPorukaTimeStamp.split(" ")[0];
        String porukaVreme = gpsPorukaTimeStamp.split(" ")[1];
        
        Calendar today = new GregorianCalendar();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        String today_str = sdf.format(today.getTime());
        
        return porukaDatum.equals(today_str);
        }catch(Exception e){
            if (logger.isDebugEnabled()) {
                logger.warn("isGpsPorukaDateToday exception, detailes : " + e.getMessage() + ", will return false", e);
            }
            return false;
        }
    }

    private String getLastPositionTime(String unitSerialNo) {
        String lastPositionTime = "2000-01-01 00:00:00";
        try {
            ResultSet result = query.executeQuery("select gps_vreme from last_position where unit_id='"
                    + unitSerialNo + "' limit 1");

            while (result.next()) {
                lastPositionTime = result.getString("gps_vreme");
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while getting LastPositionTime  - "
                        + sqle.getMessage());
            }

        }

        return lastPositionTime;

    }
    
    private boolean is_sonda_fuel_poruka(int geneko_status_int){
        if(geneko_status_int == ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET || 
                geneko_status_int == ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET_FIRST_FUEL_SENSOR || 
                geneko_status_int == ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET_SECOND_FUEL_SENSOR ){
            return true;
        }else{
            return false;
        }
    }
    
    private boolean is_second_fuel_senzor(int geneko_status_int){
        return geneko_status_int == ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET_SECOND_FUEL_SENSOR;
    }
    
    private String getLastPositionTimeAndStatusAndLongLatAndSondaFuel(String unitSerialNo, String init_stanje_vozila) {
        String lastPositionTime = "2000-01-01 00:00:00";
        String last_stanje_vozila = init_stanje_vozila;
        double last_longitude = 0;
        double last_latitude = 0;
        String last_ulica_broj = "";
        String last_opstina = "";
        String last_grad = "";
        String last_drzava = "";
        double last_udaljenost = 0;
        int last_ugao_rt = 0;
        String last_sonda_fuel_1 = "0";
        String last_sonda_fuel_2 = "0";
        try {
            

            
            
            ResultSet result = query.executeQuery("select gps_vreme, stanje_vozila, ulica_i_broj, opstina, grad, drzava, udaljenost, ugao_rt, longitude, latitude, sonda1_fuel, sonda2_fuel  from last_position where unit_id='"
                    + unitSerialNo + "' limit 1");

            while (result.next()) {
                String gps_vreme = result.getString("gps_vreme");
                String stanje_vozila = result.getString("stanje_vozila");
                String ulica_i_broj = result.getString("ulica_i_broj");
                String opstina = result.getString("opstina");
                String grad = result.getString("grad");
                String drzava = result.getString("drzava");
                double udaljenost = result.getDouble("udaljenost");
                int ugao_rt = result.getInt("ugao_rt");
                double longitude = result.getDouble("longitude");
                double latitude = result.getDouble("latitude");
                String sonda_fuel_1 = result.getString("sonda1_fuel");
                String sonda_fuel_2 = result.getString("sonda2_fuel");
                if(gps_vreme != null){
                    lastPositionTime = gps_vreme;
                }
                if(stanje_vozila != null){
                    last_stanje_vozila = result.getString("stanje_vozila");
                }
                if(ulica_i_broj != null){
                    last_ulica_broj = ulica_i_broj;
                }
                if(opstina != null){
                    last_opstina = opstina;
                }
                if(grad != null){
                    last_grad = grad;
                }
                if(drzava != null){
                    last_drzava = drzava;
                }
                
                last_udaljenost = udaljenost;
                
                
                last_ugao_rt = ugao_rt;
                
                
                
                
                last_longitude = longitude;
                last_latitude = latitude;
                
                if(sonda_fuel_1 != null){
                    last_sonda_fuel_1 = sonda_fuel_1;
                }
                if(sonda_fuel_2 != null){
                    last_sonda_fuel_2 = sonda_fuel_2;
                }
                
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while getting LastPositionTime  - "
                        + sqle.getMessage());
            }

        }

        return lastPositionTime + "," + last_stanje_vozila + "," + last_longitude + "," + last_latitude +
        "," + last_ulica_broj +
        "," + last_opstina +
        "," + last_grad +
        "," + last_drzava +
        "," + last_udaljenost +
        "," + last_ugao_rt +
        "," + last_sonda_fuel_1 +
        "," + last_sonda_fuel_2;

    }
    
    public void deleteFmsFirstPart(String serialNumber,String matchCode) {
        try { 
        query.executeUpdate("delete from fms_firstpart where unit_id='"+ serialNumber+   "' and match_code='"
                + matchCode + "'");
        }catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while  deleteFmsFirstPart  - "
                        + sqle.getMessage());
            }

        }
    }
    
 
    
  

    private void saveToHistory(GPSMessage poruka, String unit_id) {

        if (logger.isDebugEnabled()) {
            logger.debug("--> poruka = " + poruka + ", poruka.getSerialNumber() = " + poruka.getSerialNumber() + 
                    ", poruka.getGSMNumber() = " + poruka.getGSMNumber() + ", unit_id = " + unit_id);
        }

        try {

            
            
            query.executeUpdate("insert into ussd_history (unit_id,sim_card,blokada,sirena,napon,stanje_vozila,gps_vreme,longitude,latitude,gps_podatak,brzina,kurs_kretanja,tip_poruke,"
                    + "ulica_i_broj,opstina,grad,drzava,udaljenost,ugao_rt,"
                    + "fms_status,fms_rpm,fms_total_km,fms_temp,fms_fuel,fms_total_fuel,sonda_fuel,sonda1_raw,sonda2_raw,sonda3_raw,sonda1_fuel,sonda2_fuel,sonda3_fuel"
                    +",ruka_aktivna,obd_speed,obd_rpm,obd_fuel,ibutton,driver,vozac_id,brojac1,brojac2,protok,firmware,obd_total_km,obd_acc_pedal,obd_fuel_percent) values ('"
                    + poruka.getSerialNumber() + "','"
                    + unit_id/*poruka.getGSMNumber()*/ + "','"
                    + poruka.getBlock() + "','"
                    + poruka.getAlarm() + "',"
                    + poruka.getVoltage() + ",'"
                    + poruka.getStatus() + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ",'"
                    + poruka.getGPSFix() + "',"
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection() + ",'"
                    + poruka.getMessageType() + "','"
                    + poruka.getStreetNumber() + "','"
                    + poruka.getCommunity() + "','"
                    + poruka.getCity() + "','"
                    + poruka.getState() + "',"
                    + poruka.getDistance() + ","
                    + poruka.getAngleRt() + ","
                    + poruka.getFmsStatus() + ","
                    + poruka.getFmsRpm() + ","
                    + poruka.getFmsTotalKm() + ","
                    + poruka.getFmsTemp() + ","
                    + poruka.getFmsFuel() + ","
                    + poruka.getFmsTotalFuel() + ","
                    + poruka.getSondaFuelLitars() + ","
                    + poruka.getSondaFuelValue() + ","
                    + poruka.getSonda2FuelValue() + ","
                    + poruka.getSonda3FuelValue() + ","
                    + poruka.getSonda1FuelLitars() + ","
                    + poruka.getSonda2FuelLitars() + ","
                    + poruka.getSonda3FuelLitars() + ","
                    + poruka.getRuka_aktivna() + ","
                    + poruka.getObdSpeed() + ","
                    + poruka.getObdRpm() + ","
                    + poruka.getObdFuel() +  ",'"
                    + poruka.getiButton() + "','"
                    + poruka.getDriver() + "',"
                    + poruka.getDriverId() + ","
                     + poruka.getBrojac1() + ","
                     + poruka.getBrojac2() + ","
                     + poruka.getProtok() + ",'"
                    + poruka.getFirmware() + "',"
                     + poruka.getObdMileage() + ","                    
                    + poruka.getObdAccPedal()  + ","
                    + poruka.getObdFuelPercent()
                    + ")");

        } catch (SQLException sqle) {

           
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting message into ussd_history  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
    
    
 

    private void saveLastPosition(GPSMessage poruka, String unit_id, boolean is_with_sonda_fuel) {
        int recordCount = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->unit_id = " + unit_id + ", is_with_sonda_fuel = " + is_with_sonda_fuel);
        }

        try {
            ResultSet result = query.executeQuery("select unit_id from last_position where unit_id='"
                    + poruka.getSerialNumber() + "'");

            while (result.next()) {
                recordCount += 1;
            }

            if (recordCount == 0) {
                insertLastPosition(poruka, unit_id);
            } else {
                updateLastPosition(poruka, unit_id, is_with_sonda_fuel);
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while saving last position  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    private void insertLastPosition(GPSMessage poruka, String unit_id) {

        if (logger.isDebugEnabled()) {
            logger.debug("--> poruka = " + poruka);
        }

        try {
            query.executeUpdate("insert into last_position (unit_id,sim_card,blokada,sirena,napon,stanje_vozila,gps_vreme,longitude,latitude,gps_podatak,brzina,kurs_kretanja,tip_poruke,"
                    + "ulica_i_broj,opstina,grad,drzava,udaljenost,ugao_rt,"
                    + "fms_status,fms_rpm,fms_total_km,fms_temp,fms_fuel,sonda_fuel,ussd_kod,ibutton,driver,vozac_id) values ('"
                    + poruka.getSerialNumber() + "','"
                    + unit_id/*poruka.getGSMNumber()*/ + "','"
                    + poruka.getBlock() + "','"
                    + poruka.getAlarm() + "',"
                    + poruka.getVoltage() + ",'"
                    + poruka.getStatus() + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ",'"
                    + poruka.getGPSFix() + "',"
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection() + ",'"
                    + poruka.getMessageType() + "','"
                    + poruka.getStreetNumber() + "','"
                    + poruka.getCommunity() + "','"
                    + poruka.getCity() + "','"
                    + poruka.getState() + "',"
                    + poruka.getDistance() + ","
                    + poruka.getAngleRt() + ","
                    + poruka.getFmsStatus() + ","
                    + poruka.getFmsRpm() + ","
                    + poruka.getFmsTotalKm() + ","
                    + poruka.getFmsTemp() + ","
                    + poruka.getFmsFuel() + ","
                    + poruka.getSondaFuelLitars() + ",'"
                    + poruka.get_ussdKod() +  "','"
                    + poruka.getiButton() + "','"
                    + poruka.getDriver() + "',"
                    + poruka.getDriverId() + ""
                    + ")");
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting message into table last_position  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    private void updateLastPosition(GPSMessage poruka, String unit_id, boolean is_with_sonda_litres) {
        if (logger.isDebugEnabled()) {
            logger.debug("--> poruka = " + poruka);
        }

        try {
            String sqlQuery = "update last_position set unit_id='"
                    + poruka.getSerialNumber() + "',sim_card='"
                    + unit_id/*poruka.getGSMNumber()*/ + "',blokada='"
                    + poruka.getBlock() + "',sirena='"
                    + poruka.getAlarm() + "',napon="
                    + poruka.getVoltage() + ",stanje_vozila='"
                    + poruka.getStatus() + "',gps_vreme='"
                    + poruka.getLocalDateTime() + "'"
                    + ",gsm_vreme=now(),longitude="
                    + poruka.getGPSLongNonFormated() + ",latitude="
                    + poruka.getGPSLatNonFormated() + ",gps_podatak='"
                    + poruka.getGPSFix() + "',brzina="
                    + poruka.getSpeed() + ",kurs_kretanja="
                    + poruka.getGPSDirection() + ",tip_poruke='"
                    + poruka.getMessageType() + "',ulica_i_broj='"
                    + poruka.getStreetNumber() + "',opstina='"
                    + poruka.getCommunity() + "',grad='"
                    + poruka.getCity() + "',drzava='"
                    + poruka.getState() + "',udaljenost="
                    + poruka.getDistance() + ",ugao_rt="
                    + poruka.getAngleRt() + ",fms_status="
                    + poruka.getFmsStatus() + ",fms_rpm="
                    + poruka.getFmsRpm() + ",fms_total_km="
                    + poruka.getFmsTotalKm() + ",fms_temp="
                    + poruka.getFmsTemp() + ",fms_fuel="
                    + poruka.getFmsFuel() 
                    //+ ",sonda_fuel="
                    //+ poruka.getSondaFuelLitars()
                                            + ",ussd_kod='"
                    + poruka.get_ussdKod() + "',obd_speed="
                    + poruka.getObdSpeed() + ",obd_acc_pedal="
                    + poruka.getObdAccPedal()+ ", obd_rpm="
                    + poruka.getObdRpm() + ", obd_fuel="
                    + poruka.getObdFuel()+ ", obd_total_km="
                    + poruka.getObdMileage()+ ", obd_fuel_percent="
                    + poruka.getObdFuelPercent()+ ",ibutton='"
                    + poruka.getiButton() + "',driver='"
                    + poruka.getDriver() + "',vozac_id="
                    + poruka.getDriverId()  + ",brojac1="
                    + poruka.getBrojac1()+ ",brojac2="
                    + poruka.getBrojac2()+ ",protok="
                    + poruka.getProtok()
                    + " where unit_id='" + poruka.getSerialNumber() + "'";
            
            if(is_with_sonda_litres){
                int sonda1_raw = (int)Double.parseDouble(poruka.getSonda1FuelLitars());
                int sonda2_raw = (int)Double.parseDouble(poruka.getSonda2FuelLitars());
                int sonde_raw = sonda1_raw + sonda2_raw;
                sqlQuery = "update last_position set unit_id='"
                    + poruka.getSerialNumber() + "',sim_card='"
                    + unit_id/*poruka.getGSMNumber()*/ + "',blokada='"
                    + poruka.getBlock() + "',sirena='"
                    + poruka.getAlarm() + "',napon="
                    + poruka.getVoltage() + ",stanje_vozila='"
                    + poruka.getStatus() + "',gps_vreme='"
                    + poruka.getLocalDateTime() + "'"
                    + ",gsm_vreme=now(),longitude="
                    + poruka.getGPSLongNonFormated() + ",latitude="
                    + poruka.getGPSLatNonFormated() + ",gps_podatak='"
                    + poruka.getGPSFix() + "',brzina="
                    + poruka.getSpeed() + ",kurs_kretanja="
                    + poruka.getGPSDirection() + ",tip_poruke='"
                    + poruka.getMessageType() + "',ulica_i_broj='"
                    + poruka.getStreetNumber() + "',opstina='"
                    + poruka.getCommunity() + "',grad='"
                    + poruka.getCity() + "',drzava='"
                    + poruka.getState() + "',udaljenost="
                    + poruka.getDistance() + ",ugao_rt="
                    + poruka.getAngleRt() + ",fms_status="
                    + poruka.getFmsStatus() + ",fms_rpm="
                    + poruka.getFmsRpm() + ",fms_total_km="
                    + poruka.getFmsTotalKm() + ",fms_temp="
                    + poruka.getFmsTemp() + ",fms_fuel="
                    + poruka.getFmsFuel() + ",sonda_fuel="
                    + sonde_raw + ",sonda1_raw="
                    + sonda1_raw + ",sonda2_raw="
                    + sonda2_raw + ",sonda1_fuel="
                    + poruka.getSonda1FuelLitars() + ",sonda2_fuel="
                    + poruka.getSonda2FuelLitars() + ",ussd_kod='"
                    + poruka.get_ussdKod() + "',obd_speed="
                    + poruka.getObdSpeed() + ",obd_acc_pedal="
                    + poruka.getObdAccPedal()+ ", obd_rpm="
                    + poruka.getObdRpm() + ", obd_fuel="
                    + poruka.getObdFuel()+ ", obd_total_km="
                    + poruka.getObdMileage()+ ", obd_fuel_percent="
                    + poruka.getObdFuelPercent()+ ",ibutton='"
                    + poruka.getiButton() + "',driver='"
                    + poruka.getDriver() + "',vozac_id="
                    + poruka.getDriverId()  + ",brojac1="
                    + poruka.getBrojac1()+ ",brojac2="
                    + poruka.getBrojac2()+ ",protok="
                    + poruka.getProtok()
                    + " where unit_id='" + poruka.getSerialNumber() + "'";
            }
            
            

            query.executeUpdate(sqlQuery);

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while updating last position  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    /**
     * This method returns to GPS message GeoAgent object which holds street and
     * number, community, city and state for latitude and longitude, distance
     * and direction of distance from them and time zone for city
     */
    public GeoAgent initializeGeoAgent(double latituda, double longituda) {

        final double BG_MINLONG = 20.248775;
        final double BG_MAXLONG = 20.598779;
        final double BG_MINLAT = 44.638655;
        final double BG_MAXLAT = 44.918346;

        final double NS_MINLONG = 19.77654526344;
        final double NS_MAXLONG = 19.896469347669;
        final double NS_MINLAT = 45.2118344630071;
        final double NS_MAXLAT = 45.3108905441527;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        if ((longituda >= BG_MINLONG && longituda < BG_MAXLONG) && (latituda >= BG_MINLAT && latituda < BG_MAXLAT)) {
            searchBeograd(latituda, longituda);
        } else if ((longituda >= NS_MINLONG && longituda < NS_MAXLONG) && (latituda >= NS_MINLAT && latituda < NS_MAXLAT)) {
            searchNoviSad(latituda, longituda);
        } else {
            searchCities(latituda, longituda);
        }

        geoagent = new GeoAgent();

        /*U promenljive objekta GeoAgent smestamo vrednosti koje nam
         * je vratila odgovarajuca search metoda
         */
        geoagent.setDistance(_distance);
        geoagent.setStreetNumber(_streetNumber);
        geoagent.setCommunity(_community);
        geoagent.setCity(_city);
        geoagent.setState(_state);
        geoagent.setLatitude(_latitude);
        geoagent.setLongitude(_longitude);
        geoagent.setAngleRt(calculateAngleRt(geoagent.getLatitude(), geoagent.getLongitude(), latituda, longituda));
        geoagent.setTimeZone(_timeZone);

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        return geoagent;

    }

    public GeoAgent fillLastPositionGeoAgent(String unitID) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            ResultSet result = query.executeQuery("select longitude,latitude,"
                    + "ulica_i_broj,opstina,grad,kurs_kretanja,ugao_rt,gps_podatak,"
                    + "udaljenost,drzava from last_position where unit_id='"
                    + unitID + "'");

            while (result.next()) {

                _longitude = result.getDouble("longitude");
                _latitude = result.getDouble("latitude");
                _streetNumber = result.getString("ulica_i_broj");
                _community = result.getString("opstina");
                _city = result.getString("grad");
                _direction = result.getInt("kurs_kretanja");
                _angle = result.getInt("ugao_rt");
                _fix = result.getString("gps_podatak");
                _distance = result.getDouble("udaljenost");
                _state = result.getString("drzava");

            }

            geoagent = new GeoAgent();

            /*U promenljive objekta GeoAgent smestamo vrednosti iz last position upita
             */
            geoagent.setLongitude(_longitude);
            geoagent.setLatitude(_latitude);
            geoagent.setStreetNumber(_streetNumber);
            geoagent.setCommunity(_community);
            geoagent.setCity(_city);
            geoagent.setDirection(_direction);
            geoagent.setAngleRt(_angle);
            geoagent.setFix(_fix);
            geoagent.setDistance(_distance);
            geoagent.setState(_state);

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while searching last position  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        return geoagent;

    }

    private void searchBeograd(double latituda, double longituda) {
        double razmeraLat = 111.132;
        double razmeraLong = 79.518;
        double min_udaljenost = 100000;
        double udaljenost;
        int tolerancijaAdreseBeograd = 500;
        int recordCount = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        /*ogranicavamo pretragu baze na adrese koje su udaljene od latitude i longitude
         manje od vrednosti tolerancijaAdreseBeograd
         */
        whereString = " where latituda>" + (latituda - (1 / razmeraLat) * tolerancijaAdreseBeograd / 1000)
                + " and latituda<" + (latituda + (1 / razmeraLat) * tolerancijaAdreseBeograd / 1000)
                + " and longituda>" + (longituda - (1 / razmeraLong) * tolerancijaAdreseBeograd / 1000)
                + " and longituda<" + (longituda + (1 / razmeraLong) * tolerancijaAdreseBeograd / 1000);

        try {
            ResultSet result = query.executeQuery("select latituda, longituda, ulica, broj, opstina from zgrade_bg " + whereString);

            while (result.next()) {
                recordCount += 1;

                udaljenost = Math.sqrt(Math.pow(result.getDouble("latituda") - latituda, 2) * Math.pow(razmeraLat, 2) + Math.pow(result.getDouble("longituda") - longituda, 2) * Math.pow(razmeraLong, 2));
                if (udaljenost < min_udaljenost) {
                    min_udaljenost = udaljenost;

                    _distance = min_udaljenost;
                    _streetNumber = result.getString("ulica") + " " + result.getString("broj");
                    _community = result.getString("opstina");
                    _city = "Beograd";
                    _state = "Srbija";
                    _latitude = result.getDouble("latituda");
                    _longitude = result.getDouble("longituda");

                }

            }

            if (recordCount == 0) {
                searchCities(latituda, longituda);
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while searching Beograd  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    private void searchNoviSad(double latituda, double longituda) {
        double razmeraLat = 111.132;
        double razmeraLong = 79.518;
        double min_udaljenost = 100000;
        double udaljenost;
        int tolerancijaAdreseNoviSad = 1000;
        int recordCount = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        whereString = " where latituda>" + (latituda - (1 / razmeraLat) * tolerancijaAdreseNoviSad / 1000)
                + " and latituda<" + (latituda + (1 / razmeraLat) * tolerancijaAdreseNoviSad / 1000)
                + " and longituda>" + (longituda - (1 / razmeraLong) * tolerancijaAdreseNoviSad / 1000)
                + " and longituda<" + (longituda + (1 / razmeraLong) * tolerancijaAdreseNoviSad / 1000);

        try {
            ResultSet result = query.executeQuery("select latituda, longituda, ulica_broj from zgrade_ns " + whereString);

            while (result.next()) {
                recordCount += 1;

                udaljenost = Math.sqrt(Math.pow(result.getDouble("latituda") - latituda, 2) * Math.pow(razmeraLat, 2) + Math.pow(result.getDouble("longituda") - longituda, 2) * Math.pow(razmeraLong, 2));
                if (udaljenost < min_udaljenost) {

                    min_udaljenost = udaljenost;

                    _distance = min_udaljenost;
                    _streetNumber = result.getString("ulica_broj");
                    _community = "";
                    _city = "Novi Sad";
                    _state = "Srbija";
                    _latitude = result.getDouble("latituda");
                    _longitude = result.getDouble("longituda");

                }

            }
            if (recordCount == 0) {
                searchCities(latituda, longituda);
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while searching Novi Sad  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    private void searchCities(double latituda, double longituda) {
        double razmeraLat = 111.132;
        double razmeraLong = 79.518;
        double min_udaljenost = 100000;
        double udaljenost;
        int tolerancijaAdreseGrad = 50000;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        whereString = " where latituda>" + (latituda - (1 / razmeraLat) * tolerancijaAdreseGrad / 1000)
                + " and latituda<" + (latituda + (1 / razmeraLat) * tolerancijaAdreseGrad / 1000)
                + " and longituda>" + (longituda - (1 / razmeraLong) * tolerancijaAdreseGrad / 1000)
                + " and longituda<" + (longituda + (1 / razmeraLong) * tolerancijaAdreseGrad / 1000);

        try {
            ResultSet result = query.executeQuery("select latituda, longituda, naziv,drzava,vremenska_zona from gradovi" + whereString);

            while (result.next()) {

                udaljenost = Math.sqrt(Math.pow(result.getDouble("latituda") - latituda, 2) * Math.pow(razmeraLat, 2) + Math.pow(result.getDouble("longituda") - longituda, 2) * Math.pow(razmeraLong, 2));
                if (udaljenost < min_udaljenost) {
                    min_udaljenost = udaljenost;

                    _distance = min_udaljenost;
                    _city = result.getString("naziv");
                    _state = result.getString("drzava");
                    _latitude = result.getDouble("latituda");
                    _longitude = result.getDouble("longituda");
                    _timeZone = result.getInt("vremenska_zona");
                }

            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while searching cities  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    /*
     * This method returns direction between two coordinates.
     * */
    private double calculateAngleRt(double lat1, double long1, double lat2, double long2) {
        //flat earth approximation

        lat1 = Math.toRadians(lat1);
        long1 = Math.toRadians(long1);
        lat2 = Math.toRadians(lat2);
        long2 = Math.toRadians(long2);

        double dlat = lat2 - lat1;
        double dlon = long2 - long1;

        double distanceNorth = dlat;
        double distanceEast = dlon * Math.cos(lat1);

        double angleRt = Math.atan2(distanceEast, distanceNorth) % (2 * Math.PI);
        angleRt = Math.toDegrees(angleRt);
        if (angleRt < 0) {
            angleRt += 360;
        }

        return angleRt;
    }

    /**
     * This method cheks if info phone is master phone
     */
    public boolean isMasterPhone(String phone) {

        int recordCount = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            ResultSet result = query.executeQuery("select phone from info_master_phones where phone='"
                    + phone + "'");

            while (result.next()) {
                recordCount += 1;
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while checkin is master phone  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        if (recordCount == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method cheks if unit is defined
     */
    public boolean isUnitDefined(String unitID) {

        int recordCount = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            ResultSet result = query.executeQuery("select id from unit where id='"
                    + unitID + "'");

            while (result.next()) {
                recordCount += 1;
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while checkin is unit defined  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        if (recordCount == 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isUserActive(String unitID) {

        int recordCount = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            ResultSet result = query.executeQuery("select user_id,gpsuser.aktivan as active from unit,gpsuser where gpsuser.id=unit.user_id and unit.id='"
                    + unitID + "' and gpsuser.aktivan=true");

            while (result.next()) {
                recordCount += 1;
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while checkin is unit defined  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        if (recordCount == 0) {
            return false;
        } else {
            return true;
        }
    }

    public int checkDriverRequestGetId(String unitID, String telefon) {

        int response = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            ResultSet result = query.executeQuery("select unit.id,unit.ime as unit_ime,vozaci.id as vozac_id,vozaci.ime_prezime from vozaci,unit where vozaci.user_id=unit.user_id and unit.id='"
                    + unitID + "' and vozaci.telefon='" + telefon + "' limit 1");

            while (result.next()) {

                response = result.getInt("vozac_id");
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while checkin is unit defined  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        return response;

    }

    public void insertDriverSchedual(int driverId, String unitId) {
        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {

            String queryString = "update  vozaci_raspored set active=false where  vozac_id=" + driverId + " or unit_id='" + unitId + "';";

            queryString += "insert into vozaci_raspored (vozac_id,unit_id) values ("
                    + driverId + ",'" + unitId + "')";

            query.executeUpdate(queryString);
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting driver schedual  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    /**
     * This method cheks if phone is authorised for unit info
     */
    public boolean isPhoneAuthorised(String phone, String unit) {

        int recordCount = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            ResultSet result = query.executeQuery("select phone from unit,gpsuser,gpsuser_info_phones where unit.user_id=gpsuser.id and gpsuser.id=gpsuser_info_phones.user_id and gpsuser_info_phones.phone='"
                    + phone + "' and unit.id='" + unit + "'");

            while (result.next()) {
                recordCount += 1;
            }
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while checkin is phone authorised  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        if (recordCount == 0) {
            return false;
        } else {
            return true;
        }
    }

    public ArrayList<String> listStolenVehicles() {
        ArrayList lista = new ArrayList<String>();

        try {

            ResultSet result = query.executeQuery("select unit_serial_no from ukradena_vozila order by id desc");

            while (result.next()) {
                lista.add(result.getString("unit_serial_no"));
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while listing stolen vehicles  - "
                        + e.getMessage());
            }

        }

        return lista;
    }

    public boolean checkAlarmAbroad(String unit_id) {
        boolean value = false;
        try {

            ResultSet result = query.executeQuery("select alarm_abroad from unit,gpsuser where unit.user_id=gpsuser.id  and unit.id='" + unit_id + "'");

            while (result.next()) {
                value = result.getBoolean("alarm_abroad");
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while checking alarm abroad  - "
                        + e.getMessage());
            }

        }

        return value;
    }

    /**
     * This method closes connection to database.
     */
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

    public void insertTaxiRequest(String msisdn, String messageText) {
        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {

            String sessionId = Integer.toString(new Random().nextInt(999999 - 100000) + 100000);

            String queryString = "insert into taxi_sms_request (msisdn,message_text,session_id,status) values ('"
                    + msisdn + "','" + messageText + "','" + sessionId + "','Na cekanju')";

            query.executeUpdate(queryString);
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting taxi request  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

//za Austrijance
    
    
 
    
    
    public boolean isLKWUser(String unit_id) {
        boolean value = false;
       
        try {

            ResultSet result = query.executeQuery("select user_id,lkw_user from unit,gpsuser where unit.user_id=gpsuser.id  and unit.id='" + unit_id + "'");

            while (result.next()) {
                value = result.getBoolean("lkw_user");
                
            };

            result.close();
            
            
            
            

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while checking lkw user  - "
                        + e.getMessage());
            }

        }

        return value;
    }

    public Integer getGatehouseUser(String unit_id) {
        Integer userId = 0;
        try {

            ResultSet result = query.executeQuery("select user_id,gatehouse_user from unit,gpsuser where unit.user_id=gpsuser.id  and gpsuser.gatehouse_user=true and unit.id='" + unit_id + "'");

            while (result.next()) {
                userId = result.getInt("user_id");
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while checking gatehouse user  - "
                        + e.getMessage());
            }

        }

        return userId;
    }
    
    public String getUnitGroupEmail(String unit_id) {
        String email = "";
        try {

            ResultSet result = query.executeQuery("select email_address from groups,group_units where groups.id=group_units.group_id and unit_id='" + unit_id + "'");

            while (result.next()) {
                email = result.getString("email_address");
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while getUnitGroupEmail  - "
                        + e.getMessage());
            }

        }

        return email;
    }
    
    public Integer getAgheeraUser(String unit_id) {
        Integer userId = 0;
        try {

            ResultSet result = query.executeQuery("select user_id,agheera_user from unit,gpsuser where unit.user_id=gpsuser.id  and gpsuser.agheera_user=true and unit.id='" + unit_id + "'");

            while (result.next()) {
                userId = result.getInt("user_id");
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while checking agheera user  - "
                        + e.getMessage());
            }

        }

        return userId;
    }

    public boolean isSistemSUser(String unit_id) {

        int sistemSUserId = 2737;

        boolean value = false;
        try {

            ResultSet result = query.executeQuery("select user_id from unit where unit.user_id=" + sistemSUserId + "  and unit.id='" + unit_id + "'");

            while (result.next()) {
                value = true;
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while checking sistem s user  - "
                        + e.getMessage());
            }

        }

        return value;
    }
    
    
     public boolean isDvdVracarUser(String unit_id) {

        int userId = 3243;
       
        

        boolean value = false;
        try {

            ResultSet result = query.executeQuery("select user_id from unit where unit.user_id=" + userId + "  and unit.id='" + unit_id + "'");

            while (result.next()) {
                value = true;
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while checking sistem s user  - "
                        + e.getMessage());
            }

        }

        return value;
        
    }
     
     public int getUnitUserId(String unit_id) {

        int userId = 0;
       
        

       
        try {

            ResultSet result = query.executeQuery("select user_id from unit where id='" + unit_id + "'");

            while (result.next()) {
                userId = result.getInt("user_id");
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while checking  user  - "
                        + e.getMessage());
            }

        }

        return userId;
        
    }

    private void saveToLKWData(GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            query.executeUpdate("insert into lkw_data (unit_id,gps_vreme,longitude,latitude,brzina,kurs_kretanja) values ('"
                    + poruka.getSerialNumber() + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection()
                    + ")");
        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting lkw_data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }

    private void saveToGatehouseData(Integer userId, GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            
             String unitDescription=getUnitOpis(poruka.getSerialNumber());

            query.executeUpdate("insert into gatehouse_data (user_id,unit_id,gps_vreme,longitude,latitude,brzina,kurs_kretanja,unit_description) values ("
                    + userId + ",'"
                    + poruka.getSerialNumber() + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection() + ",'"
                    + unitDescription + "'"
                    + ")");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting gatehouse_data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
    
    
    private void saveToAgheeraData(Integer userId, GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {

            query.executeUpdate("insert into agheera_data (user_id,unit_id,gps_vreme,longitude,latitude,brzina,kurs_kretanja) values ("
                    + userId + ",'"
                    + poruka.getSerialNumber() + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection()
                    + ")");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting agheera data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
    
    private void saveToFourkitesData(Integer userId, GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            
            String unitDescription=getUnitOpis(poruka.getSerialNumber());

            query.executeUpdate("insert into fourkites_data (user_id,unit_id,unit_description,gps_vreme,longitude,latitude,brzina,kurs_kretanja) values ("
                    + userId + ",'"
                    + poruka.getSerialNumber() + "','"
                     + unitDescription + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection()
                    + ")");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting fourkites data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
    
    
    private void saveToSixfoldData(Integer userId, GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            
            String unitDescription=getUnitOpis(poruka.getSerialNumber());

            query.executeUpdate("insert into sixfold_data (user_id,unit_id,unit_description,gps_vreme,longitude,latitude,brzina,kurs_kretanja) values ("
                    + userId + ",'"
                    + poruka.getSerialNumber() + "','"
                     + unitDescription + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection()
                    + ")");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting fourkites data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
    
    private void saveToVerooData(Integer userId, GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            
            String unitDescription=getUnitOpis(poruka.getSerialNumber());

            query.executeUpdate("insert into veroo_data (user_id,unit_id,unit_description,gps_vreme,longitude,latitude,brzina,kurs_kretanja) values ("
                    + userId + ",'"
                    + poruka.getSerialNumber() + "','"
                     + unitDescription + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection()
                    + ")");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting veroo data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
    
    private void saveToGefcoData(Integer userId, GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->save gefco");
        }

        try {
            
            String unitDescription=getUnitOpis(poruka.getSerialNumber());

            query.executeUpdate("insert into gefco_data (user_id,unit_id,unit_description,gps_vreme,longitude,latitude,brzina,kurs_kretanja) values ("
                    + userId + ",'"
                    + poruka.getSerialNumber() + "','"
                     + unitDescription + "','"
                    + poruka.getUtcDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection()
                    + ")");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting gefco data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }
    
    
    //saveToShippeoData
    private void saveToShippeoData(Integer userId, GPSMessage poruka) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            
            String unitDescription=getUnitOpis(poruka.getSerialNumber());

            query.executeUpdate("insert into shippeo_data (user_id,unit_id,unit_description,gps_vreme,longitude,latitude,brzina,kurs_kretanja) values ("
                    + userId + ",'"
                    + poruka.getSerialNumber() + "','"
                     + unitDescription + "','"
                    + poruka.getLocalDateTime() + "',"
                    + poruka.getGPSLongNonFormated() + ","
                    + poruka.getGPSLatNonFormated() + ","
                    + poruka.getSpeed() + ","
                    + poruka.getGPSDirection()
                    + ")");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while inserting veroo data  - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }

 
 

//    public void setStartWorkingTime(String unitId,int startHour)
//    {
//
//        if (logger.isDebugEnabled())
//        {
//            logger.debug("-->");
//        }
//
//        try
//        {
//            query.executeUpdate("update unit set local_automatski_izvestaj_breakhour=" + startHour + " where id='" + unitId + "';");
//
//        }
//        catch(SQLException sqle)
//        {
//
//            if (logger.isDebugEnabled()) {
//                logger.error(
//                        "'SQLException' - while updating unit wtime - "
//                        + sqle.getMessage());
//            }
//
//
//        }
//
//        if (logger.isDebugEnabled())
//        {
//            logger.debug("<--");
//        }
//
//    }
// DL 18/08/2015 begin
    public void setIButtonID(String simCard, String ibuttonId) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        String unitId = getSerialNumber(simCard).get_serialNumber();
        Calendar now = Calendar.getInstance();
        Date datum = now.getTime();

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

        String dan = dateFormatter.format(datum);
        String vremeOd = timeFormatter.format(datum);
        //String vremeDo = "23:59:59";

        System.out.println(vremeOd + "------" + dan);
        try {
            query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton) values ('" + unitId + "','"
                    + dan + "','" + vremeOd + "','" + ibuttonId + "')");

        } catch (SQLException sqle) {

            sqle.printStackTrace();
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while updating unit ibutton - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }

// DL 18/08/2015 end
    public void setStartWorkingTime(String unitId) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        Calendar now = Calendar.getInstance();
        Date datum = now.getTime();

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

        String dan = dateFormatter.format(datum);
        String vremeOd = timeFormatter.format(datum);
        String vremeDo = "23:59:59";

        System.out.println(vremeOd + "------" + dan);
        try {
            query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,vreme_do) values ('" + unitId + "','"
                    + dan + "','" + vremeOd + "','" + vremeDo + "')");

        } catch (SQLException sqle) {

            sqle.printStackTrace();
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while updating unit wtime - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }

    public void setStopWorkingTime(String unitId) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        Calendar now = Calendar.getInstance();
        Date datum = now.getTime();

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

        String dan = dateFormatter.format(datum);
        String vremeDo = timeFormatter.format(datum);

        try {
            query.executeUpdate("update vozaci_prijava_odjava set vreme_do='" + vremeDo + "' where unit_id='" + unitId
                    + "' and dan='" + dan + "' and vreme_do='23:59:59'");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while updating unit wtime - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }

 

    public Boolean isUnitCheckedOut(String unitId) {
        Boolean response = false;

        try {

            ResultSet result = query.executeQuery("select * from unit_check where checked_in=false and unit_id='" + unitId + "';");

            while (result.next()) {

                response = true;
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while getting is check out - "
                        + e.getMessage());
            }

        }

        return response;
    }

    public void updateUnitCheck(String userPhone, Boolean checkedIn) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {
            query.executeUpdate("update unit_check set check_time=now(),checked_in=" + checkedIn
                    + " where user_phone='" + userPhone + "';");

        } catch (SQLException sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'SQLException' - while updating unit check - "
                        + sqle.getMessage());
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }

    public String getLastOdjava(String dan, String unitId) {
        String response = "";
        try {

            ResultSet result = query.executeQuery("select vreme_do from vozaci_prijava_odjava where dan='" + dan + "' and unit_id='" + unitId + "' order by vreme_do desc limit 1;");

            while (result.next()) {

                response = result.getString("vreme_do");
            };

            result.close();

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while getting last odjava - "
                        + e.getMessage());
            }

        }

        return response;
    }
    
    
//    public void saveIButtonRequest(String simCard, String iButtonHexString,String timestampString) {
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("-->");
//        }
//
//       
//       
//        try {
//            
//             String unitId = getSerialNumber(simCard);
//             Integer userId=getUnitUserId(unitId);
//             
//             Integer adamspedId=3519;//adamsped 3519
//             Integer lanusId=1112;//lanus
//             Integer mtsKosovoId=3688;
////        Calendar cal = Calendar.getInstance();
////        Date now = cal.getTime();
////
//
//        
//        Date timestampIbutton=timestampFormatter.parse(timestampString);
////
//        String dan = dateFormatter.format(timestampIbutton);
//        String vremeOd = timeFormatter.format(timestampIbutton);
//       
//        //String vremeOd=timeStamp;
//       
//            
//            ResultSet result = query.executeQuery("select current_ibutton,ibutton_time,ime from unit where id='" +  unitId +  "';");
//
//            String current_ibutton="";
//            Timestamp ibuttonTime=null;
//            String registracija="";
//            while (result.next()) {
//               
//                current_ibutton=result.getString("current_ibutton");
//                ibuttonTime=result.getTimestamp("ibutton_time");
//                registracija=result.getString("ime");
//                
//            };
//            
//            result.close();
//            
//            
//            result = query.executeQuery("select id,ime_prezime,telefon from vozaci where lower(ibutton)='" + iButtonHexString.toLowerCase() + "';");
//
//            Integer driverId=0;
//            String driver="";
//            String telefon=null;
//            while (result.next()) {
//                driverId=result.getInt("id");
//                driver=result.getString("ime_prezime");
//                telefon=result.getString("telefon");
//                
//            };
//            
//            result.close();
//            
//            
//            
//             if(driverId==0){
//                 result = query.executeQuery("select id,ime_prezime,telefon from vozaci where lower(ibutton) like '%" + rearrangeRfidHex(iButtonHexString.toLowerCase()) + "%';");
//
//           
//                    while (result.next()) {
//                        driverId=result.getInt("id");
//                        driver=result.getString("ime_prezime");
//                        telefon=result.getString("telefon");
//
//                    };
//
//                    result.close();
//            
//             }
//            
//             System.out.println(" ibutt driverId " + driverId);
//            
//            if(driverId>0){
//                
//                //generisanje automatic odjave sebe sa drugih vozila
//                 result = query.executeQuery("select * from unit where id<>'"
//                        + unitId + "' and current_driver_id=" + driverId + ";");
//
//                 String oldUnitId="";
//                while (result.next()) {
//                    oldUnitId=result.getString("id");
//                    
//
//                };
//                result.close();
//                
//                if(!oldUnitId.equals("")){
//                    
//                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + oldUnitId + "','"
//                        + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',false," + driverId + ",true)");
//                
//                }
//                
//                
//                
//                //generisanje automatic odjave drugog sa ovog vozila
//                 result = query.executeQuery("select current_driver_id,current_ibutton,current_driver from unit where id='"
//                        + unitId + "' and current_driver_id<>" + driverId + ";");
//
//                 String oldDriverId="";
//                 String oldDriverName="";
//                  String oldIButton="";
//                while (result.next()) {
//                    oldDriverId=result.getString("current_driver_id");
//                    
//                    oldIButton=result.getString("current_ibutton");
//                    oldDriverName=result.getString("current_driver");        
//
//                };
//                result.close();
//                
//                if(!oldDriverId.equals("")){
//                    
//                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + unitId + "','"
//                        + dan + "','" + vremeOd + "','" + oldIButton + "','" + oldDriverName + "',false," + oldDriverId + ",true)");
//                
//                }
//                
//                
//                //odjava sa drugih vozila
//                query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id<>'"
//                        + unitId + "' and current_driver_id=" + driverId);
//
//                
//                
//                
//                if(iButtonHexString.equals(current_ibutton)){
//                    long secondsDiff=600;
//                    if(ibuttonTime!=null){
//                        Date ibuttonDate= new Date(ibuttonTime.getTime());
//                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
//                    }
//
//                    System.out.println("diff sec " + secondsDiff);
//
//                   if(secondsDiff>60 ){
//                       query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id='" + unitId + "'");
//
//
//                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
//                        + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',false," + driverId + ")");
//                        
//                        
//                        //ODJAVA VOZACA SA VOZILA, SMS
//                        if( (userId.equals(adamspedId)  || userId.equals(mtsKosovoId)  || userId.equals(lanusId) ) && telefon!=null){//za adamsped
//                            StringBuilder smsMessage=new StringBuilder();
//                            smsMessage.append("ODJAVA\nVozac: ");
//                            smsMessage.append(driver);
//                            smsMessage.append("\nVozilo: ");
//                            smsMessage.append(registracija);
//                            smsMessage.append("\nVreme: ");
//                            smsMessage.append( timestampFormatterSMS.format(timestampIbutton));
//                           
//                            logger.info(telefon + " sms " + smsMessage.toString());
//                            
////                            if(userId.equals(mtsKosovoId)){
////                                 putInTelekomSMSQueue(telefon + "_" + smsMessage.toString());
////                            }else{
////                                putInSMSQueue(telefon + "_" + smsMessage.toString());
////                            }
//                        }
//                   }else{
//                       //ignore
//                       System.out.println("ignore ibutt diff " + secondsDiff);
//                       
//                       if(secondsDiff<0){
//                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
//                        + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',false," + driverId + ")");
//                       }
//                   }
//                }else{
//
//                     long secondsDiff=600;
//                     
//                     
//                    
//                    
//                    if(new Date(timestampIbutton.getTime()).compareTo(dateFormatter.parse("2090-01-01"))>0){
//                             secondsDiff=-1;
//                    }else{
//                        if(ibuttonTime!=null){
//                        Date ibuttonDate= new Date(ibuttonTime.getTime());
//                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
//           
//                        }
//                    }
//
//                    System.out.println("diff sec " + secondsDiff);
//
//                    //nova prijava mzoe da dodje samo posle stare
//                    if(secondsDiff>0 ){
//                    //nova prijava
//                    System.out.println(" ibutt prijava " + iButtonHexString);
//
//                    query.executeUpdate("update unit set current_ibutton='" + iButtonHexString + "',current_driver='" + driver
//                            + "',current_driver_id=" + driverId + ",ibutton_time='"+ timestampFormatter.format(timestampIbutton) + "' where id='" + unitId + "'");
//
//
//                    
//                    }
//                    
//                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
//                            + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',true," + driverId + ")");
//                    
//                    
//                    //PRIJAVA VOZACA NA VOZILO, SMS
//                 if( (userId.equals(adamspedId)  || userId.equals(mtsKosovoId) || userId.equals(lanusId) ) && telefon!=null){//za adamsped
//                        StringBuilder smsMessage=new StringBuilder();
//                        smsMessage.append("PRIJAVA\nVozac: ");
//                        smsMessage.append(driver);
//                        smsMessage.append("\nVozilo: ");
//                        smsMessage.append(registracija);
//                        smsMessage.append("\nVreme: ");
//                        smsMessage.append( timestampFormatterSMS.format(timestampIbutton));
//                        
//                          logger.info(telefon + " sms " + smsMessage.toString());
//                        
////                            if(userId.equals(mtsKosovoId)){
////                                 putInTelekomSMSQueue(telefon + "_" + smsMessage.toString());
////                            }else{
////                                putInSMSQueue(telefon + "_" + smsMessage.toString());
////                           }
//                    }
//
//                    
//                }
//            
//            }else{
//                
//                //za ugradnju
//                query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava) values ('" + unitId + "','"
//                            + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',true)");
//
//            }
//            
//            
//            
//        } catch (Exception e) {
//
//            e.printStackTrace();
//            if (logger.isDebugEnabled()) {
//                logger.error(
//                        "'Exception' - while updating unit ibutton - "
//                        + e.getMessage());
//            }
//
//        }
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("<--");
//        }
//
//    }
    
    
    public void saveIButtonRequest(String simCard, String iButtonHexString,String timestampString) {

        if (logger.isDebugEnabled()) {
            logger.debug("-->, simCard = " +
                    simCard + ", iButtonHexString = " + iButtonHexString + ", timestampString = " + timestampString);
        }

        try {

             String unitId = getSerialNumber(simCard).get_serialNumber();
             if (logger.isDebugEnabled()) {
                 logger.debug("-->, unitId = " +
                         unitId);
             }
             Integer userId=getUnitUserId(unitId);
             if (logger.isDebugEnabled()) {
                 logger.debug("-->, userId = " +
                         userId);
             }
//             Integer adamspedId=3519;//adamsped 3519
//             Integer lanusId=1112;//lanus
//             Integer mtsKosovoId=3688;

//        Calendar cal = Calendar.getInstance();

//        Date now = cal.getTime();

//

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
        DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//        DateFormat timestampFormatterSMS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");


        Date timestampIbutton=timestampFormatter.parse(timestampString);
        String dan = dateFormatter.format(timestampIbutton);
        String vremeOd = timeFormatter.format(timestampIbutton);


            ResultSet result = query.executeQuery("select current_ibutton,ibutton_time,ime from unit where id='" +  unitId +  "';");

            String current_ibutton="";
            Timestamp ibuttonTime=null;
            String registracija="";
            while (result.next()) {
                current_ibutton=result.getString("current_ibutton");
                ibuttonTime=result.getTimestamp("ibutton_time");
                registracija=result.getString("ime");
            };

            result.close();
            
            if (logger.isDebugEnabled()) {
                logger.debug(", current_ibutton = " +
                        current_ibutton + ", ibuttonTime = " + ibuttonTime + ", registracija = " + registracija);
            }

            result = query.executeQuery("select id,ime_prezime,telefon from vozaci where ibutton = '"+rearrangeRfidHex(iButtonHexString)+"'");
                    //"select id,ime_prezime,telefon from vozaci where lower(ibutton)='" + iButtonHexString.toLowerCase() + "';");
            Integer driverId=0;
            String driver="";
            String telefon=null;

            while (result.next()) {
                driverId=result.getInt("id");
                driver=result.getString("ime_prezime");
                telefon=result.getString("telefon");
            };
            
            
            if (logger.isDebugEnabled()) {
                logger.debug(", driverId = " +
                        driverId + ", driver = " + driver + ", telefon = " + telefon);
            }

            result.close();

             if(driverId==0){

                 result = query.executeQuery("select id,ime_prezime,telefon from vozaci where ibutton = '"+rearrangeRfidHex(iButtonHexString)+"'");
                         //"select id,ime_prezime,telefon from vozaci where lower(ibutton) like '%" + rearrangeRfidHex(iButtonHexString.toLowerCase()) + "%';");

                    while (result.next()) {
                        driverId=result.getInt("id");
                        driver=result.getString("ime_prezime");
                        telefon=result.getString("telefon");
                    };

                    result.close();

             }
             if (logger.isDebugEnabled()) {
                 logger.debug(" ibutt driverId " + driverId);
             }


            if(driverId>0){

                //generisanje automatic odjave sebe sa drugih vozila

                 result = query.executeQuery("select * from unit where id<>'"

                        + unitId + "' and current_driver_id=" + driverId + ";");

                 String oldUnitId="";

                while (result.next()) {

                    oldUnitId=result.getString("id");

                };
                if (logger.isDebugEnabled()) {
                    logger.debug(" oldUnitId " + oldUnitId);
                }
                result.close();

                if(!oldUnitId.equals("")){

                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + oldUnitId + "','"
                        + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',false," + driverId + ",true)");

                }

                //generisanje automatic odjave drugog sa ovog vozila

                 result = query.executeQuery("select current_driver_id,current_ibutton,current_driver from unit where id='"
                        + unitId + "' and current_driver_id<>" + driverId + ";");

                 String oldDriverId="";
                 String oldDriverName="";
                  String oldIButton="";

                while (result.next()) {

                    oldDriverId=result.getString("current_driver_id");
                    oldIButton=result.getString("current_ibutton");
                    oldDriverName=result.getString("current_driver");       

                };
                result.close();
                if (logger.isDebugEnabled()) {
                    logger.debug(" oldDriverId " + oldDriverId + " oldIButton " + oldIButton + " oldDriverName " + oldDriverName );
                }
                if(!oldDriverId.equals("")){

                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id,automatic) values ('" + unitId + "','"
                        + dan + "','" + vremeOd + "','" + oldIButton + "','" + oldDriverName + "',false," + oldDriverId + ",true)");

                }
                //odjava sa drugih vozila
                query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id<>'"
                        + unitId + "' and current_driver_id=" + driverId);
                if (logger.isDebugEnabled()) {
                    logger.debug(" odjava sa drugih vozila "  );
                }
                if(iButtonHexString.equals(current_ibutton)){
                    long secondsDiff=600;
                    if(ibuttonTime!=null){
                        Date ibuttonDate= new Date(ibuttonTime.getTime());
                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
                    }
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("diff sec " + secondsDiff);
                    }
                   if(secondsDiff>60 ){
                       query.executeUpdate("update unit set current_ibutton='',current_driver='',current_driver_id=null,ibutton_time=null where id='" + unitId + "'");
                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
                        + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',false," + driverId + ")");

                   }else{
                       //ignore
                       if (logger.isDebugEnabled()) {
                           logger.debug("ignore ibutt diff " + secondsDiff);
                       }
                       if(secondsDiff<0){
                        query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
                        + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',false," + driverId + ")");
                       }
                   }
                }else{
                     long secondsDiff=600;
                    if(new Date(timestampIbutton.getTime()).compareTo(dateFormatter.parse("2090-01-01"))>0){
                             secondsDiff=-1;
                    }else{
                        if(ibuttonTime!=null){
                        Date ibuttonDate= new Date(ibuttonTime.getTime());
                         secondsDiff = (timestampIbutton.getTime()-ibuttonDate.getTime())/1000;
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("diff sec " + secondsDiff);
                    }
                    //nova prijava mzoe da dodje samo posle stare
                    if(secondsDiff>0 ){
                    //nova prijava
                        if (logger.isDebugEnabled()) {
                            logger.debug(" ibutt prijava " + iButtonHexString);
                        }
                    query.executeUpdate("update unit set current_ibutton='" + iButtonHexString + "',current_driver='" + driver
                            + "',current_driver_id=" + driverId + ",ibutton_time='"+ timestampFormatter.format(timestampIbutton) + "' where id='" + unitId + "'");
                    }
                    query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava,vozac_id) values ('" + unitId + "','"
                            + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',true," + driverId + ")");     
                }
            }else{
                //za ugradnju
                if (logger.isDebugEnabled()) {
                    logger.debug(" za ugradnju" );
                }
                query.executeUpdate("insert into vozaci_prijava_odjava (unit_id,dan,vreme_od,ibutton,vozac,prijava) values ('" + unitId + "','"
                            + dan + "','" + vremeOd + "','" + iButtonHexString + "','" + driver + "',true)");

            }

        } catch (Exception e) {
            e.printStackTrace();
            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while updating unit ibutton - "
                        + e.getMessage());
            }
        }
    }
    
    /**
     * 
     * @param localDateTimeCalendar, vreme kad je poruka generisana
     * @return true ako je poruka koja je pristigla generisana vise od minuta u proslosti
     */
    private boolean is_poruka_from_past(Calendar localDateTimeCalendar){
        

            localDateTimeCalendar.add(Calendar.SECOND, 5);

            Calendar now = Calendar.getInstance();
            return now.after(localDateTimeCalendar);

    }
    
    /**
     * 
     * @param prva_poruka, vreme kad pristigla prva poruka
     * @param druga_poruka, vreme kad je pristigla druga poruka
     * @return true ako je druga poruka 
     */
    private boolean is_druga_poruka_less_5_seconds(Calendar prva_poruka, Calendar druga_poruka){
        
        druga_poruka.add(Calendar.SECOND, -5);
        return druga_poruka.before(prva_poruka);
    }
    
    private Calendar getLocalDateTimeCalendar(String localDateTime){
        SimpleDateFormat formatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            Date localDateTimeDate = formatedDate.parse(localDateTime);
            Calendar localGPSDate = Calendar.getInstance();
            localGPSDate.setTime(localDateTimeDate);
            return localGPSDate;
        }catch(Exception e){
            if (logger.isDebugEnabled()) {
                logger.warn(
                        "'Exception' in is_poruka_from_past:  "
                        + e.getMessage() + ", will return false", e);
            }
            return null;
        }
    }
    
    
    private static String decimalFormat(double value){
        DecimalFormat decimalFormat = new DecimalFormat("##.#"); 
        //decimalFormat.setMinimumFractionDigits(1); 
        
        return decimalFormat.format(value);
    }
    
    
    private void sendSondaSMSAlarm(GPSMessage poruka, String novi_nivo_goriva_litars, String prethodni_nivo_goriva_litars, String formattedAlarmDate) {
        if (logger.isDebugEnabled()) {
            logger.info(
                    "-->'sendSondaSMSAlarm' novi_nivo_goriva_litars:  "
                    + novi_nivo_goriva_litars + ", prethodni_nivo_goriva_litars = " + prethodni_nivo_goriva_litars + 
                    ", formattedAlarmDate = " + formattedAlarmDate);
        }
        AlarmDBAgent alarmAgent = null;

        try{
            
            alarmAgent = new AlarmDBAgent();

            String alarmBroj= alarmAgent.getAlarmBroj(poruka.getSerialNumber());

            boolean isKvar= alarmAgent.isKvar(poruka.getSerialNumber());

            boolean aktivanKorisnik = alarmAgent.isAktivanKorisnik(poruka.getSerialNumber());
            boolean aktivnoVozilo= alarmAgent.isAktivnoVozilo(poruka.getSerialNumber());
            
            if (logger.isDebugEnabled()) {
                logger.info(
                        "'sendSondaSMSAlarm' alarmBroj:  "
                        + alarmBroj + ", isKvar = " + isKvar + 
                        ", aktivanKorisnik = " + aktivanKorisnik + ", aktivnoVozilo = " + aktivnoVozilo);
            }

            poruka.setStatus("Alarm sonda");
            poruka.setMessageType("USSD");

            //check if already sent in last 30 minutes
            boolean shouldSend= shouldSendAlarmSms(poruka.getSerialNumber());

            boolean solved = false;
            if (logger.isDebugEnabled()) {
                logger.info(
                        "'sendSondaSMSAlarm' shouldSend:  "
                        + shouldSend );
            }

            String action="";
            if(shouldSend){
                if(alarmBroj!=null && !alarmBroj.isEmpty()){
                    action="Poslat sms na " + alarmBroj;
                    solved=true;
                }else{
                    action="Korisnik nema alarm sms broj";
                    solved=false;
                }  
            }else{
                action="Obavesten u poslednjih 30 minuta";
                solved=true;
            }

            if(!isKvar){   

                new AlarmHandler(poruka.getSerialNumber(),"GENEKO ALARM GORIVO SONDA: " + formattedAlarmDate + ", " + poruka.getPosition(),
                        2,action,solved, alarmAgent, isKvar);
                
            }else{
                shouldSend=false;
            }


            String smsMessage = "";

            if(shouldSend){
                try{
                    String opisVozila=getUnitOpis(poruka.getSerialNumber());

                    DateFormat dfDatabase=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateAlarm=dfDatabase.parse(poruka.getLocalDateTime());
                    DateFormat dfAlarmDate=new SimpleDateFormat("dd.MM.yyyy");
                    DateFormat dfAlarmTime=new SimpleDateFormat("HH:mm");

                    String alarmDate=dfAlarmDate.format(dateAlarm);
                    String alarmTime=dfAlarmTime.format(dateAlarm);

                    if(alarmBroj!=null && !alarmBroj.isEmpty()){

                        smsMessage="Alarm za gorivo" + 
                        "\nDatum: "  +  alarmDate +
                        "\nVreme: "  + alarmTime + 
                        "\nVozilo: "  + opisVozila.replace("_", "-") + 
                        "\nPrethodni nivo: " + prethodni_nivo_goriva_litars + " l" + 
                        "\nNovi nivo: " + novi_nivo_goriva_litars + " l";

                    }else{
                        smsMessage="Alarm za gorivo na vozilu " + opisVozila.replace("_", "-")  + ". Korisnik nema setovan broj za sms alarm.";
                    }
                }catch(Exception e){
                    logger.error("error alarm sms " + e.getMessage());
                }

            }

            if(!isKvar){
                if(shouldSend){

                    insertAlarmSms(poruka.getSerialNumber(), alarmBroj);


                    logger.info("send sms for " + poruka.getSerialNumber() + ", alarmBroj " + alarmBroj + 
                            ", smsMessage = " + smsMessage);
                    
                    if(alarmBroj!=null && !alarmBroj.isEmpty()){

                        putInSMSQueue(alarmBroj + "_" + smsMessage);                      
                        putInSMSQueue("0648822013" + "_" + smsMessage);
                        putInSMSQueue("0648822017" + "_" + smsMessage);

                    }else{

                        putInSMSQueue("0648822013" + "_" + smsMessage);
                        putInSMSQueue("0648822017" + "_" + smsMessage);

                    }
                }
            }

        }
        catch(Exception e){
            logger.debug("Unable to instantiate alarmDBAgent, details : " + e.getMessage(), e);
        }
        finally{
            if(alarmAgent != null){
                alarmAgent.close();
            }
        }
    }
    
    private boolean shouldSendAlarmSms(String unitId) {

        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sentTime=null;

        try {

            ResultSet result = query.executeQuery("select max(sent_time) as max_sent_time from alarm_sms where unit_id='" + unitId + "'");

            while(result.next()){

            sentTime = result.getString("max_sent_time");
            }

            result.close();
            
            
            if(sentTime==null){
                //never sent
                return true;
            }else{
                Date sent=sdf.parse(sentTime);
                
                Date now=new Date();
                 long diffInMillies = Math.abs(now.getTime() - sent.getTime());
                 long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    
                 logger.debug(unitId + " alarm sms  sent before " + diff);
                if(diff>30){
                    return true;
                }else{
                     logger.debug(unitId + " alarm sms already sent at " + sentTime);
                    return false;
                    
                   
                }
                
            }
            

        } catch (Exception sqle) {

            if (logger.isDebugEnabled()) {
                logger.error(
                        "'Exception' - while shouldSendAlarmSms  - "
                        + sqle.getMessage());
            }
            
            return false;
        }
    }

    
    private void insertAlarmSms(String unitId,String mobile){

        try {
            Calendar cal = Calendar.getInstance();
            Date now = cal.getTime();

            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String vreme = dateFormatter.format(now);

            query.executeUpdate("insert into alarm_sms (unit_id,sent_time,mobile) values ('" 
                    + unitId + "','" + vreme + "','" + mobile + "')");

        } catch (SQLException sqle) {


            logger.error(
                    "'SQLException' - while insertAlarmSms  - "
                    + sqle.getMessage());

        }       
    }
    
    
    private void putInSMSQueue(String content){

        try{
            logger.debug("ibuttonSmsQueue  size " +  Service.ibuttonSmsQueue.size());
            if(Service.ibuttonSmsQueue.size() < Service.ibuttonSmsQueueSize-5)
            {
                Service.ibuttonSmsQueue.put(content);
            }else{
                logger.error("ibuttonSmsQueue full, " + content);
            }

        }catch(Exception e){
            logger.error("error putting in ibuttonSmsQueue ");
        }
    }
  


    public static void main(String[] args) throws Exception {
//        PostGreAgent agent = new PostGreAgent();
//        System.out.println(agent.getLastPositionTimeAndStatusAndLongLat("777777010389", "ERROR"));
//        
//        System.out.println("unit_data = " + agent.getSerialNumber("777777010389"));
//        agent.close();
        
//        String inputHex = "017D91691B00001D00";
//            String to_return = inputHex.substring(0,inputHex.length() - 2);
//            System.out.println(inputHex + " rearrangeRfidHex  " + to_return);
        
        
//        int current_sonda_fuel_litars_2 = (int)Double.parseDouble("268.9");
//        System.out.println(" current_sonda_fuel_litars_2  " + current_sonda_fuel_litars_2);   
        
//        //u voznji
//        String input = "1110111111111101";
//        System.out.println("is uredjaj upalje = " +input.subSequence(8, 9).equals("1"));
//        //parkiran
//        input = "1110111101111101";
//        System.out.println("is uredjaj upalje = " +input.subSequence(8, 9).equals("1"));
        
        System.out.println(decimalFormat(34243.26));
        
        String fuel = "108.9";
        System.out.println((int)Double.parseDouble(fuel));
      
    }
    

}

