package yu.co.certus.pos.geneco.protocol.message;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import yu.co.certus.pos.lanus.service.Service;

import yu.co.certus.pos.geneco.protocol.DeviceProperty;
import yu.co.certus.pos.geneco.protocol.impl.ProtocolDecoderOldGeneko;
import yu.co.certus.pos.geneco.util.GeoLocationUtil;

public class GPSMessage {

    Logger logger = Service.logger;
    protected String _content;
    protected String _gsmNumber;
    protected String _serialNumber;
    protected String _autotrackMessage;
    protected double _tempSpeed;
    protected String _alarm;
    protected String _block;
    protected String _status;
    protected double _voltage;
    protected int _gpsHour;
    protected int _gpsMinute;
    protected int _gpsSecond;
    protected int _gpsYear;
    protected int _gpsMonth;
    protected int _gpsDay;
    protected String utcDateTime;
    protected String _localDateTime;
    protected String _gpsFix;
    protected double _gpsLat;
    protected double _gpsLatNonFormated;
    protected String _gpsLatHem;
    protected double _gpsLong;
    protected double _gpsLongNonFormated;
    protected String _gpsLongHem;
    protected double _gpsSpeed;
    protected double _gpsDirection;
    protected String _ussdKod;
    protected String _messageType = "GENECO";
    protected String _streetNumber = "";
    protected String _community = "";
    protected String _city = "";
    protected String _state = "";
    protected double _distance;
    protected int _angleRt;
    protected String _obd_cool_temp = "null";
    protected String _obd_speed = "null";
    protected String _obd_fuel = "null";
    protected String _obd_rpm = "null";
    protected String _obd_mileage = "null";
    protected String _obd_fuel_percent = "null";
    protected String _obd_acc_pedal = "null";
    protected String _brod_m1 = "";
    protected String _brod_m2 = "";
    protected String _brod_m3 = "";
    protected String _brod_rpm = "";
    protected Boolean hasFms = false;
    protected Boolean hasNewFms = false;
        
    protected Boolean hasSonda = false;
    protected Boolean obd = false;
    protected Integer fmsStatus = null;
    protected Integer fmsRpm = null;
    protected Integer fmsTotalKm = null;
    protected Integer fmsTotalFuel=null;
    protected Integer fmsTemp = null;
    protected Integer fmsFuel = null;
    protected Integer sondaFuelValue = null;
    protected String sondaFuelLitars = null;
    protected Integer sonda2FuelValue = null;
    protected String sonda2FuelLitars = null;
    protected Integer sonda3FuelValue = null;
    protected String sonda3FuelLitars = null;
    protected Integer sonda1FuelValue = null;
    protected String sonda1FuelLitars = null;
    protected Boolean ruka_aktivna = false;
    protected Integer brojac1= null;
    protected Integer brojac2= null;
    protected Boolean isFmsFirstPart=false;
    protected String matchCode=null;
    protected Double protok;
    protected String firmware;
    protected String iButton="";
    protected String driver="";
    protected Integer driverId=null;
    private String geneco_status_id = "";
    private String _input = "";
    
    public String get_input() {
        return _input;
    }

    private static final double KNOTS_TO_KMH_CONVERSION_PARAM = 1.852;
    
    public String getGeneco_status_id() {
        return geneco_status_id;
    }

    //    protected PostGreAgent postgreAgent;
//    protected GeoAgent geoAgent;
    protected DecimalFormat distanceFormat = new DecimalFormat("####.##");

    /**
     * Constructor for GPSMessage sets content of message.
     */
//    public GPSMessage(String messageContent) {
//        if (logger.isDebugEnabled()) {
//            logger.debug("-->");
//        }
//        _content = messageContent.replace('*', '&');
//
//
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("<--");
//        }
//    }
    
    
    /**
     * Constructor for GPSMessage sets content of message.
     */
    public GPSMessage( Map<String, String> message) {
        if (logger.isDebugEnabled()) {
            logger.debug("--> message = " + message);
        }
        _ussdKod = "";
        _messageType = "GENECO";
        _input = message.get("input");
        
        
        _serialNumber = message.get(DeviceProperty.VEHICLE_ID);
        //_gsmNumber = message.get(DeviceProperty.VEHICLE_ID);
        //_autotrackMessage;
        _tempSpeed = Double.parseDouble(message.get(DeviceProperty.SPEED));
        
        //_alarm =  message.get(DeviceProperty.VEHICLE_ID);
        //_block = message.get(DeviceProperty.VEHICLE_ID);
        //set status
        geneco_status_id = message.get(DeviceProperty.ID_STATUS);
        if (logger.isDebugEnabled()) {
            logger.debug("--> geneco_status_id = " + geneco_status_id);
            logger.debug("--> _tempSpeed = " + _tempSpeed + " u knots per hour");
            logger.debug("--> _serialNumber = " + _serialNumber);
            logger.debug("--> _input = " + _input);
        }
        
        _tempSpeed = _tempSpeed * KNOTS_TO_KMH_CONVERSION_PARAM;

        if (logger.isDebugEnabled()) {

            logger.debug("--> _tempSpeed konvertovana u KMH = " + _tempSpeed + "");

        }
        
//        if(_tempSpeed > 0){
//            _status = "Voznja";
//        }else{
//            if(geneco_status_id.equals(ProtocolDecoderOldGeneko.STATUS_RESPONSE_CONTACT_KEY_ON)){
//                _status = "Upaljen";
//            }else{
//                _status = "Parkiran";
//            }
//        }
        
        //  setovanje statusa prebaceno na postgres klasu

        //protected double _voltage;
        _gpsHour = Integer.parseInt(message.get(DeviceProperty.HOUR));
        _gpsMinute = Integer.parseInt(message.get(DeviceProperty.MINUTE));
        _gpsSecond = Integer.parseInt(message.get(DeviceProperty.SECOND));
        _gpsYear = Integer.parseInt(message.get(DeviceProperty.YEAR));
        _gpsMonth = Integer.parseInt(message.get(DeviceProperty.MONTH));
        _gpsDay = Integer.parseInt(message.get(DeviceProperty.DAY));
        utcDateTime=createUTCDateTime();
        _localDateTime = createLocalDateTime();

        _gpsFix = message.get(DeviceProperty.VALID_POSITION);
        

        
//        if ((b[BIN_YEAR] & 0x80) == 0)
//            message.put(DeviceProperty.VALID_POSITION, "A"); //Good position
//       else
//           message.put(DeviceProperty.VALID_POSITION, "V");
        //TODO videti da li ovo to

        _gpsLat = Double.parseDouble(message.get(DeviceProperty.LAT));
        _gpsLatNonFormated = GeoLocationUtil.getNonFormatedValue(_gpsLat);//Double.parseDouble(message.get(DeviceProperty.LAT_SIROVI));

        

//        protected double _gpsLatNonFormated;
        //TODO da li nam ovo treba
//        protected String _gpsLatHem;
        //TODO sta je ovo
        
        _gpsLong = Double.parseDouble(message.get(DeviceProperty.LON));
        _gpsLongNonFormated = GeoLocationUtil.getNonFormatedValue(_gpsLong);//Double.parseDouble(message.get(DeviceProperty.LON_SIROVI));
        
//
//        protected double _gpsLongNonFormated;
//        protected String _gpsLongHem;
        //TODO - da li nam ovo treba
        String gps_speed = message.get(DeviceProperty.SPEED_GPS);
        
        if(gps_speed != null){
        _gpsSpeed = Double.parseDouble(gps_speed);
        }
        
        String gpsDirection = message.get(DeviceProperty.DIRECTION);
        if(gpsDirection != null){
            _gpsDirection = Double.parseDouble(gpsDirection);
        }

        String power = message.get(DeviceProperty.MAIN_POWER);
        
        if(power != null){
            _voltage = Integer.parseInt(power);
            _voltage = _voltage / 10;
        }
        
        
        if(geneco_status_id.equals("" + ProtocolDecoderOldGeneko.STATUS_IBUTTON_LOGIN_MESSAGE) ||
                geneco_status_id.equals("" + ProtocolDecoderOldGeneko.STATUS_IBUTTON_LOGOUT_MESSAGE)){
            if (logger.isDebugEnabled()) {

                logger.debug("--> _tempSpeed konvertovana u KMH = " + _tempSpeed + "");

            }
            iButton = message.get(DeviceProperty.EVENT_DATA);
            if (logger.isDebugEnabled()) {

                logger.info("--> iButton = " + iButton );

            }
        }
        
        int redni_broj_sonde = 0;
        
        if(geneco_status_id.equals("" + ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET ) || 
                geneco_status_id.equals("" + ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET_FIRST_FUEL_SENSOR) ||
                geneco_status_id.equals("" + ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET_SECOND_FUEL_SENSOR)){
            redni_broj_sonde = geneco_status_id.equals("" + ProtocolDecoderOldGeneko.STATUS_FUEL_DATA_PACKET_SECOND_FUEL_SENSOR) ?
                2 : 1;
            
            if(redni_broj_sonde == 1){
                if (logger.isDebugEnabled()) {
                    if (logger.isDebugEnabled()) {
                        logger.info("Sonda 1 value = " + message.get(DeviceProperty.EVENT_DATA) + ", kraj");
                    }
                }
                sondaFuelValue = calculateSrednjuVrednostSondaFuelValue(message.get(DeviceProperty.EVENT_DATA));
                if (logger.isDebugEnabled()) {
                    logger.info("Sonda 1 usrednjena value = " + sondaFuelValue );
                }
            }else{

                sonda2FuelValue = calculateSrednjuVrednostSondaFuelValue(message.get(DeviceProperty.EVENT_DATA));;
                if (logger.isDebugEnabled()) {
                    logger.info("Sonda 2 value = " + message.get(DeviceProperty.EVENT_DATA) + ", kraj");
                    logger.info("Sonda 2 usrednjena value = " + sonda2FuelValue );
                }
            }
        }
        
        
        

//        protected String _streetNumber = "";
//        protected String _community = "";
//        protected String _city = "";
//        protected String _state = "";
//        protected double _distance;
//        protected int _angleRt;
//TODO - podaci za mape, da li nam trebaju        
        
//        protected String _obd_cool_temp = "null";
//        protected String _obd_speed = "null";
//        protected String _obd_fuel = "null";
//        protected String _obd_rpm = "null";
//        protected String _obd_mileage = "null";
//        protected String _obd_fuel_percent = "null";
//        protected String _obd_acc_pedal = "null";
//        protected String _brod_m1 = "";
//        protected String _brod_m2 = "";
//        protected String _brod_m3 = "";
//        protected String _brod_rpm = "";
//        protected Boolean hasFms = false;
//        protected Boolean hasNewFms = false;
//TODO za sada ne obd
        
//        protected Boolean hasSonda = false;
//        protected Boolean obd = false;
//        protected Integer fmsStatus = null;
//        protected Integer fmsRpm = null;
//        protected Integer fmsTotalKm = null;
//        protected Integer fmsTotalFuel=null;
//        protected Integer fmsTemp = null;
//        protected Integer fmsFuel = null;
//        protected Integer sondaFuelValue = null;
//        protected String sondaFuelLitars = null;
//        protected Integer sonda2FuelValue = null;
//        protected String sonda2FuelLitars = null;
//        protected Integer sonda3FuelValue = null;
//        protected String sonda3FuelLitars = null;
//        protected Integer sonda1FuelValue = null;
//        protected String sonda1FuelLitars = null;
//        protected Boolean ruka_aktivna = false;
//        protected Integer brojac1= null;
//        protected Integer brojac2= null;
//        protected Boolean isFmsFirstPart=false;
//        protected String matchCode=null;
//        protected Double protok;
//        protected String firmware;
//        protected String iButton="";
//        protected String driver="";
//        protected Integer driverId=null;
        //TODO - za sada ne FMS i iButton 

        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }
    
    private Integer calculateSrednjuVrednostSondaFuelValue(String messageValueString){
        if (logger.isDebugEnabled()) {
            logger.info("calculateSrednjuVrednostSondaFuelValue, value = " + 
                    messageValueString);
        }
        String[] message_values = messageValueString.trim().split(" ");
        int message_values_sum = 0;
        for(String current_value : message_values){
            message_values_sum = message_values_sum + Integer.parseInt(current_value);
        }
        
        int srednja_vrednost = (int) (message_values_sum/message_values.length + 0.5);
        return new Integer(srednja_vrednost);
    }
    

    /**
     * Returns content of GPS message.
     */
    public String getContent() {
        return _content;
    }

    /**
     * Parses and saves GPS Message.
     */
//    public void parseSaveGPSMessage() throws GPSMessageException {
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("-->");
//        }
//
//        try {
//            postgreAgent = new PostGreAgent();
//            parseGSMNumber();
//            //_serialNumber=postgreAgent.getSerialNumber(_gsmNumber);
//            this.setSerialNumber(postgreAgent.getSerialNumber(this.getGSMNumber()));
//            //logger.info("##################### ser br" + _serialNumber + "  kartica " + _gsmNumber);
//            parseGPSData();
//            //logger.info("##################### 111111111111111111111111");
//            setMessageType();
//
//            if (!this.isError()) {
//                setGeoData();
//            }
//
//            if (!this.isAlarm()) {
//                for (String ukraden : postgreAgent.listStolenVehicles()) {
//                    //izbacujemo slovo k iz serijskog broja ukradenog vozila
//                    //if(this.getSerialNumber().equals(ukraden.substring(1)))
//                    if (this.getSerialNumber().equals(ukraden)) {
//                        AlarmHandler alarmHandler = new AlarmHandler(this.getSerialNumber(), "STARTOVANO UKRADENO VOZILO",5);
//
//                    }
//                }
//            }
//
//            //NOT USED ANYMORE
////            try {
////                if (postgreAgent.isUnitCheckedOut(this.getSerialNumber())) {
////                    //logger.info("#####################" + this.getGSMNumber() + " checked out");
////                    _messageType = "HIDDEN";
////                }
////            } catch (Exception e) {
////                logger.error(e.getMessage());
////            }
//
//            postgreAgent.saveMessage(this);
//
//        } catch (ArrayIndexOutOfBoundsException aiobe) {
//            throw new GPSMessageException("Bad message format. Missing parameters", aiobe);
//
//        } catch (NumberFormatException nfe) {
//            throw new GPSMessageException("Bad message format. Expected numeric value of parameter.", nfe);
//
//        } catch (StringIndexOutOfBoundsException siobe) {
//            throw new GPSMessageException("Bad message format. Invalid parameter.", siobe);
//
//        } catch (IllegalArgumentException iae) {
//            throw new GPSMessageException("Bad message format. Couldn't create date or time from gps message.", iae);
//
//        } catch (PostGreAgentException pgae) {
//
//            throw new GPSMessageException("'PostGreAgentException' - while parsing and saving GPS message  - "
//                    + pgae.getMessage());
//        } finally {
//            if (postgreAgent != null) {
//                postgreAgent.close();
//            }
//        }
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("<--");
//        }
//
//    }

    protected void parseGSMNumber() {
        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        String[] parametri = _content.split("&");
        _gsmNumber = parametri[0];
        _autotrackMessage = parametri[2];


        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }

    /**
     * Returns GSM number that sent USSD message. Also represents serial number
     * of autotrack device.
     */
    public String getGSMNumber() {
        return _gsmNumber;
    }

    /**
     * Returns message without GSM number.
     */
    public String getAutotrackMessage() {
        return _autotrackMessage;
    }

    private void parseGPSData() {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        _ussdKod = "150";

        String[] parametri = _autotrackMessage.split("_");

        _alarm = parametri[0];
        _block = parametri[1];




        if (parametri[2].equals("1")) {
            _status = "Upaljen";
        } else if (parametri[2].equals("2")) {
            _status = "Voznja";
        } else if (parametri[2].equals("3")) {
            _status = "Parkiran";
        } else if (parametri[2].equals("4")) {
            _status = "Programiran";
        } else if (parametri[2].equals("5")) {
            _status = "Provera";
        } else if (parametri[2].equals("6")) {
            _status = "Reset napajanja";
        }else if (parametri[2].equals("7")) {
            _status = "Alarm napon";
        }else if (parametri[2].equals("8")) {
            _status = "Alarm sonda";
        }else if (parametri[2].equals("9")) {
            _status = "Nema napona";
        }

        System.out.println("_status " + _status);

        _voltage = Double.parseDouble(parametri[3]) * 0.05859375 + 0.6;

        _gpsHour = Integer.parseInt(parametri[4].substring(0, 2));
        _gpsMinute = Integer.parseInt(parametri[4].substring(2, 4));
        _gpsSecond = Integer.parseInt(parametri[4].substring(4, 6));



        if (parametri[5].equals("A")) {
            _gpsFix = "FIX";
        } else {
            _gpsFix = "NFIX";
        }

        String latString = formatCoordinate(parametri[6]);
        _gpsLatNonFormated = Double.parseDouble(parametri[6]);
        _gpsLat = Double.parseDouble(latString.substring(0, 3))
                + Double.parseDouble(latString.substring(3)) / 60;
        _gpsLatHem = parametri[7];
        if (_gpsLatHem.equals("S")) {
            _gpsLatNonFormated = (-1) * _gpsLatNonFormated;
            _gpsLat = (-1) * _gpsLat;
        }


        String longString = formatCoordinate(parametri[8]);
        _gpsLongNonFormated = Double.parseDouble(parametri[8]);
        _gpsLong = Double.parseDouble(longString.substring(0, 3))
                + Double.parseDouble(longString.substring(3)) / 60;
        _gpsLongHem = parametri[9];
        if (_gpsLongHem.equals("W")) {
            _gpsLongNonFormated = (-1) * _gpsLongNonFormated;
            _gpsLong = (-1) * _gpsLong;
        }

        _tempSpeed = Double.parseDouble(parametri[10]) * 1.852;

        //ako nema brzinu ili je status upaljen ili parkiran
//        if (parametri[10].length() == 0 || parametri[2].equals("1") || parametri[2].equals("3")) {
//            _gpsSpeed = 0;
//        } else {
//            _gpsSpeed = Double.parseDouble(parametri[10]) * 1.852;
//        }

//        if (parametri[11].length() == 0) {
//            _gpsDirection = 0;
//        } else {
//            _gpsDirection = Double.parseDouble(parametri[11]);
//        }

        //Januar je mesec 0, Februar 1, ... , Decembar 11   
        _gpsYear = Integer.parseInt("20" + parametri[12].substring(4, 6));
        _gpsMonth = Integer.parseInt(parametri[12].substring(2, 4)) - 1;
        _gpsDay = Integer.parseInt(parametri[12].substring(0, 2));



        /*posto GPS vreme leti kasni 2 sata, a zimi 1 sat u odnosu na nase
         pretvaramo GPS vreme u lokalno 
         */
        _localDateTime = createLocalDateTime();

         utcDateTime=createUTCDateTime();
        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }
    }
    
    protected String createLocalDateTime() {
        if (logger.isDebugEnabled()) {
            logger.debug("--> _gpsYear = " + _gpsYear + 
                    ", _gpsMonth = " + _gpsMonth + 
                    ", _gpsDay = " + _gpsDay +
                    ", _gpsHour = " + _gpsHour +
                    ", _gpsMinute = " + _gpsMinute +
                    ", _gpsSecond = " + _gpsSecond );
        }



        String[] ids = TimeZone.getAvailableIDs(0 * 60 * 60 * 1000);

        /*kreiramo greenwich vremensku zonu*/
        SimpleTimeZone gmt = new SimpleTimeZone(0 * 60 * 60 * 1000, ids[0]);

        /*kreiramo nasu vremensku zonu sa pravilima
         pomeranja vremena
         */
        SimpleTimeZone gmt1 = new SimpleTimeZone(3600000,
                "Europe/Belgrade",
                Calendar.MARCH, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                Calendar.OCTOBER, -1, Calendar.SUNDAY,
                3600000, SimpleTimeZone.UTC_TIME,
                3600000);

        /*kreiramo objekat calendar u greenwich zoni*/
        Calendar calendar = new GregorianCalendar(gmt);

        calendar.setLenient(false);
        try{
            calendar.set(_gpsYear, _gpsMonth - 1, _gpsDay, _gpsHour, _gpsMinute, _gpsSecond);


            calendar.get(Calendar.DST_OFFSET);


            /* prebacujemo calendar u nasu zonu*/
            calendar.setTimeZone(gmt1);
        }catch(Throwable e){
            if (logger.isDebugEnabled()) {
                logger.warn("--> unable to set local date_time for  gpsYear = "  + _gpsYear + 
                    ", _gpsMonth = " + _gpsMonth + 
                    ", _gpsDay = " + _gpsDay +
                    ", _gpsHour = " + _gpsHour +
                    ", _gpsMinute = " + _gpsMinute +
                    ", _gpsSecond = " + _gpsSecond + ", will set for now");
            }
            calendar = new GregorianCalendar(gmt);
        }



        /* vracamo format datuma i vremena za postgre*/
        SimpleDateFormat formatedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");




        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

        return formatedDate.format(calendar.getTime());

    }

    protected String createUTCDateTime() {
       

        
        
      
       

//SimpleDateFormat formatedDate1 = new SimpleDateFormat("yyyy-M-d H:m:s");
       String utc=_gpsYear+ "-" + _gpsMonth+ "-" + _gpsDay+ " " + _gpsHour+ ":" + _gpsMinute+ ":" + _gpsSecond;
       
//      Date datum= formatedDate1.parse(input);
//       
//        SimpleDateFormat formatedDate2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//
//

        return utc;

    }

    /**
     * Returns if message is with alarm status.
     */
    public boolean isAlarm() {
        if (_alarm.equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    
    public boolean isAlarmSonda() {
        if (_status.equals("Alarm sonda")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns if message type is error.
     */
    public boolean isError() {
        /*ako su latituda ili longituda 0,a status nije upaljen, poruka je ERROR*/
        
        //logger.info(_gpsLat + " / " + _gpsLong + " _gpsLat _gpsLong " );
        
        if ((_gpsLat == 0 || _gpsLong == 0) && !_status.equals("Upaljen")) {
            return true;

        }

        if (_tempSpeed > 165) {
            return true;
        }

        return false;

    }

//    protected void setMessageType() {
//        if (logger.isDebugEnabled()) {
//            logger.debug("-->");
//        }
//
//        if (this.isAlarm()) {
//            _messageType = "ALARM";
//        } else {
//            if (this.isError()) {
//                _messageType = "ERROR";
//            }
//        }
//        /*inace je poruka po default-u USSD*/
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("<--");
//        }
//    }

    /**
     * Ovaj metod pronalazi najblizu ulicu i broj,opstinu, mesto i drzavu
     * udaljenost i smer od njih i vremensku zonu ako su koordinate ok odnosno
     * uzima poslednju poziciju kad su koordinate nula, a status upaljen
     */
//    protected void setGeoData() {
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("-->");
//        }
//
//        //ako smo dosli ovde sa lat i long nulama, znaci da je status upaljen i 
//        //poruka nije error pa uzimamo poslednju poziciju
//        if (_gpsLat == 0 || _gpsLong == 0) {
//            geoAgent = postgreAgent.fillLastPositionGeoAgent(_serialNumber);
//            _localDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
//                    new java.util.Date(System.currentTimeMillis()));
//            _gpsLongNonFormated = geoAgent.getLongitude();
//            _gpsLatNonFormated = geoAgent.getLatitude();
//            _streetNumber = geoAgent.getStreetNumber();
//            _community = geoAgent.getCommunity();
//            _city = geoAgent.getCity();
//            _gpsDirection = geoAgent.getDirection();
//            _angleRt = (int) geoAgent.getAngleRt();
//            _gpsFix = geoAgent.getFix();
//            _distance = geoAgent.getDistance();
//            _state = geoAgent.getState();
//
//            //ako nema poslednje pozicije, po defaultu su koordinate nula u pgagentu
//            //pa stavljamo da je poruka error
//            if (_gpsLongNonFormated == 0 || _gpsLatNonFormated == 0) {
//                _messageType = "ERROR";
//            }
//
//
//        } else {
//
//            geoAgent = postgreAgent.initializeGeoAgent(_gpsLat, _gpsLong);
//            _streetNumber = geoAgent.getStreetNumber();
//            _community = geoAgent.getCommunity();
//            _city = geoAgent.getCity();
//            _state = geoAgent.getState();
//            _distance = geoAgent.getDistance();
//            _angleRt = (int) geoAgent.getAngleRt();
//
//            try {
//                if (!_state.equals("Srbija")) {
//
//
//                    if (postgreAgent.checkAlarmAbroad(_serialNumber)) {
//
//                        GeoAgent lastPositionGeoAgent = postgreAgent.fillLastPositionGeoAgent(_serialNumber);
//                        if (lastPositionGeoAgent.getState().equals("Srbija")) {
//
//
//                            logger.info(_serialNumber + " napustio Srbiju.");
//
//                            AlarmHandler alarmHandler = new AlarmHandler(_serialNumber, "Vozilo je napustilo Srbiju.",3);
//
//                        }
//                    }
//
//
//
//                }
//
//            } catch (Exception ex) {
//                if (logger.isDebugEnabled()) {
//                    logger.error(
//                            "'Exception' - while sending alarm abroad sms - "
//                            + ex.getMessage());
//                }
//
//            }
//        }
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("<--");
//        }
//    }

    /**
     * Returns state of alarm (0 or 1)
     */
    public String getAlarm() {
        return _alarm;
    }

    /**
     * Returns state of blocking (0 or 1)
     */
    public String getBlock() {
        return _block;
    }

    /**
     * Returns state of vehicle (1, 2 or 3)
     */
    public String getStatus() {
        return _status;
    }

    /**
     * Returns voltage
     */
    public double getVoltage() {
        return _voltage;
    }

    /**
     * Returns date and time of autotrack message in GMT+1
     */
    public String getLocalDateTime() {
        return _localDateTime;
    }

    public String getUtcDateTime() {
        return utcDateTime;
    }

    public void setUtcDateTime(String utcDateTime) {
        this.utcDateTime = utcDateTime;
    }
    
    

    public void setAlarm(String _alarm) {
        this._alarm = _alarm;
    }

    public void setAngleRt(int _angleRt) {
        this._angleRt = _angleRt;
    }

    public void setBlock(String _block) {
        this._block = _block;
    }

    public void setCity(String _city) {
        this._city = _city;
    }

    public void setDistance(double _distance) {
        this._distance = _distance;
    }

    public void setLocalDateTime(String _localDateTime) {
        this._localDateTime = _localDateTime;
    }

    public void setMessageType(String _messageType) {
        this._messageType = _messageType;
    }

    public void setState(String _state) {
        this._state = _state;
    }

    public void setStatus(String _status) {
        this._status = _status;
    }

    public void setGpsLat(double _gpsLat) {
        this._gpsLat = _gpsLat;
    }

    public void setGpsLong(double _gpsLong) {
        this._gpsLong = _gpsLong;
    }
    
    

    
    

    public void setStreetNumber(String _streetNumber) {
        this._streetNumber = _streetNumber;
    }

    public void setVoltage(double _voltage) {
        this._voltage = _voltage;
    }

    public void setGpsFix(String _gpsFix) {
        this._gpsFix = _gpsFix;
    }

    public void setGpsLatHem(String _gpsLatHem) {
        this._gpsLatHem = _gpsLatHem;
    }

    public void setGpsLatNonFormated(double _gpsLatNonFormated) {
        this._gpsLatNonFormated = _gpsLatNonFormated;
    }

    public void setGpsLongHem(String _gpsLongHem) {
        this._gpsLongHem = _gpsLongHem;
    }

    public void setGpsLongNonFormated(double _gpsLongNonFormated) {
        this._gpsLongNonFormated = _gpsLongNonFormated;
    }

    public void setCommunity(String _community) {
        this._community = _community;
    }

    public void setGpsDirection(double _gpsDirection) {
        this._gpsDirection = _gpsDirection;
    }

    public void setGpsSpeed(double _gpsSpeed) {
        this._gpsSpeed = _gpsSpeed;
    }

    public void setGsmNumber(String _gsmNumber) {
        this._gsmNumber = _gsmNumber;
    }
    
    

    /**
     * Returns GPS fix
     */
    public String getGPSFix() {
        return _gpsFix;
    }

    /**
     * Returns GPS latitude
     */
    public double getGPSLat() {
        return _gpsLat;
    }

    public double getGPSLatNonFormated() {
        return _gpsLatNonFormated;
    }

    /**
     * Returns GPS latitude hemisphere
     */
    public String getGPSLatHem() {
        return _gpsLatHem;
    }

    /**
     * Returns GPS longitude
     */
    public double getGPSLong() {
        return _gpsLong;
    }

    public double getGPSLongNonFormated() {
        return _gpsLongNonFormated;
    }

    /**
     * Returns GPS longitude hemisphere
     */
    public String getGPSLongHem() {
        return _gpsLongHem;
    }

    /**
     * Returns speed
     */
    public double getSpeed() {
        return _tempSpeed;
        //return _gpsSpeed;
    }

    /**
     * Returns direction (0 to 360)
     */
    public double getGPSDirection() {
        return _gpsDirection;
    }

    /**
     * Returns type of message (USSD,ERROR,ALARM)
     */
    public String getMessageType() {
        return _messageType;
    }

    public String getSonda2FuelLitars() {
        return sonda2FuelLitars;
    }

    public void setSonda2FuelLitars(String sonda2FuelLitars) {
        this.sonda2FuelLitars = sonda2FuelLitars;
    }

    public String getSonda3FuelLitars() {
        return sonda3FuelLitars;
    }

    public void setSonda3FuelLitars(String sonda3FuelLitars) {
        this.sonda3FuelLitars = sonda3FuelLitars;
    }

    public Integer getSonda2FuelValue() {
        return sonda2FuelValue;
    }

    public void setSonda2FuelValue(Integer sonda2FuelValue) {
        this.sonda2FuelValue = sonda2FuelValue;
    }

    public Integer getSonda3FuelValue() {
        return sonda3FuelValue;
    }

    public void setSonda3FuelValue(Integer sonda3FuelValue) {
        this.sonda3FuelValue = sonda3FuelValue;
    }

    public Integer getSonda1FuelValue() {
        return sonda1FuelValue;
    }

    public void setSonda1FuelValue(Integer sonda1FuelValue) {
        this.sonda1FuelValue = sonda1FuelValue;
    }

    public Boolean getIsFmsFirstPart() {
        return isFmsFirstPart;
    }

    public void setIsFmsFirstPart(Boolean isFmsFirstPart) {
        this.isFmsFirstPart = isFmsFirstPart;
    }

    public String getMatchCode() {
        return matchCode;
    }

    public void setMatchCode(String matchCode) {
        this.matchCode = matchCode;
    }

    
    
    /**
     * Returns street and number for latitude and longitude if any
     */
    public String getStreetNumber() {
        return _streetNumber;
    }

    /**
     * Returns city for latitude and longitude
     */
    public String getCity() {
        return _city;
    }

    /**
     * Returns community for latitude and longitude in Belgrade
     */
    public String getCommunity() {
        return _community;
    }

    /**
     * Returns state for latitude and longitude
     */
    public String getState() {
        return _state;
    }

    /**
     * Returns distance for latitude and longitude from nearest street and
     * number or city
     */
    public double getDistance() {
        return _distance;
    }

    /**
     * Returns distance direction from nearest street and number or city
     */
    public double getAngleRt() {
        return _angleRt;
    }

    public String getObdCoolTemperature() {
        return _obd_cool_temp;
    }

    public String getObdSpeed() {
        return _obd_speed;
    }

    public String getObdFuel() {
        return _obd_fuel;
    }

    public String getObdRpm() {
        return _obd_rpm;
    }

    public String getObdMileage() {
        return _obd_mileage;
    }

    public void setObdMileage(String _obd_mileage) {
        this._obd_mileage = _obd_mileage;
    }

    public String getObdFuelPercent() {
        return _obd_fuel_percent;
    }

    public void setObdFuelPercent(String _obd_fuel_percent) {
        this._obd_fuel_percent = _obd_fuel_percent;
    }

    public String getObdAccPedal() {
        return _obd_acc_pedal;
    }

    public void setObdAccPedal(String _obd_acc_pedal) {
        this._obd_acc_pedal = _obd_acc_pedal;
    }
    
    
    
    

    public void setObd_rpm(String _obd_rpm) {
        this._obd_rpm = _obd_rpm;
    }

    public void setObd_speed(String _obd_speed) {
        this._obd_speed = _obd_speed;
    }

    public void setObd_fuel(String _obd_fuel) {
        this._obd_fuel = _obd_fuel;
    }
    
    

    public String get_brod_m1() {
        return _brod_m1;
    }

    public void set_brod_m1(String _brod_m1) {
        this._brod_m1 = _brod_m1;
    }

    public String get_brod_m2() {
        return _brod_m2;
    }

    public void set_brod_m2(String _brod_m2) {
        this._brod_m2 = _brod_m2;
    }

    public String get_brod_m3() {
        return _brod_m3;
    }

    public void set_brod_m3(String _brod_m3) {
        this._brod_m3 = _brod_m3;
    }

    public String get_brod_rpm() {
        return _brod_rpm;
    }

    public void set_brod_rpm(String _brod_rpm) {
        this._brod_rpm = _brod_rpm;
    }

    public String get_ussdKod() {
        return _ussdKod;
    }

    public String getPosition() {

        return distanceFormat.format(_distance) + " km od " + _streetNumber + ", " + _community + ", " + _city + ", " + _state;
    }

    public String getSerialNumber() {
        return _serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this._serialNumber = serialNumber;
    }

    public Integer getFmsFuel() {
        return fmsFuel;
    }

    public void setFmsFuel(Integer fmsFuel) {
        this.fmsFuel = fmsFuel;
    }

    public Integer getFmsRpm() {
        return fmsRpm;
    }

    public void setFmsRpm(Integer fmsRpm) {
        this.fmsRpm = fmsRpm;
    }

    public Integer getFmsStatus() {
        return fmsStatus;
    }

    public void setFmsStatus(Integer fmsStatus) {
        this.fmsStatus = fmsStatus;
    }

    public Integer getFmsTemp() {
        return fmsTemp;
    }

    public void setFmsTemp(Integer fmsTemp) {
        this.fmsTemp = fmsTemp;
    }

    public Integer getFmsTotalKm() {
        return fmsTotalKm;
    }

    public void setFmsTotalKm(Integer fmsTotalKm) {
        this.fmsTotalKm = fmsTotalKm;
    }

    public Integer getFmsTotalFuel() {
        return fmsTotalFuel;
    }

    public void setFmsTotalFuel(Integer fmsTotalFuel) {
        this.fmsTotalFuel = fmsTotalFuel;
    }
    
    
    

    public Integer getSondaFuelValue() {
        return sondaFuelValue;
    }

    public void setSondaFuelValue(Integer sondaFuelValue) {
        this.sondaFuelValue = sondaFuelValue;
    }

    public Boolean getHasFms() {
        return hasFms;
    }

    public void setHasFms(Boolean hasFms) {
        this.hasFms = hasFms;
    }

    public Boolean getHasSonda() {
        return hasSonda;
    }

    public void setHasSonda(Boolean hasSonda) {
        this.hasSonda = hasSonda;
    }

    public Boolean getObd() {
        return obd;
    }

    public void setObd(Boolean obd) {
        this.obd = obd;
    }

    
    public String getSondaFuelLitars() {
        return sondaFuelLitars;
    }

    public void setSondaFuelLitars(String sondaFuelLitars) {
        this.sondaFuelLitars = sondaFuelLitars;
    }

    public String getSonda1FuelLitars() {
        return sonda1FuelLitars;
    }

    public void setSonda1FuelLitars(String sondaFuelLitars) {
        this.sonda1FuelLitars = sondaFuelLitars;
    }

    public Integer getBrojac1() {
        return brojac1;
    }

    public void setBrojac1(Integer brojac1) {
        this.brojac1 = brojac1;
    }

    public Integer getBrojac2() {
        return brojac2;
    }

    public void setBrojac2(Integer brojac2) {
        this.brojac2 = brojac2;
    }

    public Double getProtok() {
        return protok;
    }

    public void setProtok(Double protok) {
        this.protok = protok;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }
    
    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public String getiButton() {
        return iButton;
    }

    public void setiButton(String iButton) {
        this.iButton = iButton;
    }

    
    
    /**
     * Returns string representation of GPS message
     */
    public String toString() {

        return "*Uredjaj: " + _serialNumber
        + ", _serialNumber: " + _gsmNumber
                + ", kartica: " + _gsmNumber
                + ", Alarm: " + _alarm
                + ",_tempSpeed = " + _tempSpeed
                + ", Status: " + _status
                + ", Napon: " + _voltage
                + ", Fix: " + _gpsFix
                + ", Latituda: " + _gpsLat + ' ' + _gpsLatHem
                + ", Longituda: " + _gpsLong + ' ' + _gpsLongHem
                + ", _gpsLatNonFormated: " + _gpsLatNonFormated
                + ", _gpsLongNonFormated: " + _gpsLongNonFormated
                + ", Brzina: " + _gpsSpeed
                + ", Smer: " + _gpsDirection
                + ", Tip poruke: " + _messageType
                + ", iButton: " + iButton
                + ", Pozicija: " + distanceFormat.format(_distance) + " km od " + _streetNumber + ", " + _community + ", " + _city + ", " + _state
                + ", Vreme: " + _localDateTime;
    }

    protected String formatCoordinate(String koordString) {
        String response = koordString;
        double koordinata = new Double(koordString);

        double abs = koordinata / Math.abs(koordinata);

        if (Math.abs(Math.floor(koordinata)) > 999 && Math.abs(Math.floor(koordinata)) < 10000) {
            response = "0" + Math.abs(koordinata);
        } else if (Math.abs(Math.floor(koordinata)) > 99 && Math.abs(Math.floor(koordinata)) < 1000) {
            response = "00" + Math.abs(koordinata);
        } else if (Math.abs(Math.floor(koordinata)) > 9 && Math.abs(Math.floor(koordinata)) < 100) {
            response = "000" + Math.abs(koordinata);
        } else if (Math.abs(Math.floor(koordinata)) < 10) {
            response = "0000" + Math.abs(koordinata);
        }

        return response;

    }

   
    public Boolean getRuka_aktivna() {
        return ruka_aktivna;
    }

 
    public void setRuka_aktivna(Boolean ruka_aktivna) {
        this.ruka_aktivna = ruka_aktivna;
    }

    /**
     * @return the hasNewFms
     */
    public Boolean getHasNewFms() {
        return hasNewFms;
    }

    /**
     * @param hasNewFms the hasNewFms to set
     */
    public void setHasNewFms(Boolean hasNewFms) {
        this.hasNewFms = hasNewFms;
    }
    
      public static void main(String[] args) {
          
//          int volate = 141;
//          System.out.println("int volate =  " + volate);
//          double voltage_double = volate;
//          System.out.println("double voltage_double =  " + voltage_double);
//          voltage_double = voltage_double / 10;
//          System.out.println("double voltage_double =  " + voltage_double);
          
          
          Map<String, String> message = new HashMap<String, String>();
         //_gpsYear = 2000, _gpsMonth = 0, _gpsDay = 0, _gpsHour = 0, _gpsMinute = 0, _gpsSecond = 0
          //_tempSpeed = Double.parseDouble(message.get(DeviceProperty.SPEED));
          message.put(DeviceProperty.SPEED, "0");
          message.put(DeviceProperty.HOUR, "0");
          message.put(DeviceProperty.MINUTE, "0");
          message.put(DeviceProperty.SECOND, "0");
          message.put(DeviceProperty.YEAR, "2000");
          message.put(DeviceProperty.MONTH, "0");
          message.put(DeviceProperty.DAY, "0");
//          _gpsHour = Integer.parseInt(message.get(DeviceProperty.HOUR));
//          _gpsMinute = Integer.parseInt(message.get(DeviceProperty.MINUTE));
//          _gpsSecond = Integer.parseInt(message.get(DeviceProperty.SECOND));
//          _gpsYear = Integer.parseInt(message.get(DeviceProperty.YEAR));
//          _gpsMonth = Integer.parseInt(message.get(DeviceProperty.MONTH));
//          _gpsDay = Integer.parseInt(message.get(DeviceProperty.DAY));
          
              try{
                  GPSMessage gsm_mesage = new GPSMessage(message);
                //GPSMessage falcomGPSMessage=new GPSMessage("381658822013*150*0_0_2_232_175954.0_A_4455.058_N_02018.299_E_82.07_171.32_230411_#");
            //falcomGPSMessage.parseSaveGPSMessage();
              }catch(Exception e){
                  e.printStackTrace();
              }
              
              
              
              
              
//           try{
//           PostGreAgent postgreAgent=new PostGreAgent();
//           System.out.println("sonda" + postgreAgent.calculateSondaFuel("777777000361",0,2));
//           }catch(Exception e){
//               e.printStackTrace();
//           }
        }
}

