package yu.co.certus.pos.geneco.util;


import org.apache.log4j.Logger;

import yu.co.certus.pos.geneco.data.AlarmDBAgent;
import yu.co.certus.pos.lanus.service.Service;




public class AlarmHandler {

    Logger logger = Service.logger;


    private String _serialNumber;
    private String _alarmDescription;
    private Integer _alarmTypeId;
    private String _action;
    private boolean _solved;


    private AlarmDBAgent _alarmAgent;
    private boolean _isKvar;
    /**
     * Constructor for AlarmHandler receives gsm number
     * for which alarm came and the position of vehicle
     */
    public AlarmHandler(String serialNumber,String position,Integer typeId,String action,boolean solved,
            AlarmDBAgent alarmAgent, boolean isKvar)
    {


        _serialNumber=serialNumber;
        _alarmDescription=position;
        _alarmTypeId=typeId;
        _action=action;
        _solved=solved;
        _isKvar = isKvar;
//        _alarmMessage="ALARM#" + _serialNumber + "#" +  position;
        _alarmAgent = alarmAgent;
        logger.info("called alarmhandler " + _serialNumber 
        );

        insertCallCentarReport();
        logger.info("1. insertCallCentarReport for " + _serialNumber 
        );




        if (logger.isDebugEnabled())
        {
            logger.debug("<--");
        }
    }







    private void insertCallCentarReport(){

        if(!_isKvar){
            _alarmAgent.insertReport(_serialNumber, "Aktiviran alarm.", _alarmTypeId, _alarmDescription, "call centar",_action,_solved);
        }

    }








}
