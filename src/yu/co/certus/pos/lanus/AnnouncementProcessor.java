package yu.co.certus.pos.lanus;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import yu.co.certus.pos.lanus.data.DatabaseException;
import yu.co.certus.pos.lanus.data.NoInvoiceDataException;
import yu.co.certus.pos.lanus.data.InvoiceData;
import yu.co.certus.pos.lanus.message.AbstractRequest;
import yu.co.certus.pos.lanus.message.AbstractResponse;
import yu.co.certus.pos.lanus.message.AnnouncementRequest;
import yu.co.certus.pos.lanus.message.AnnouncementResponse;

import yu.co.certus.pos.lanus.data.InvoiceIface;
import yu.co.certus.pos.lanus.data.TransactionData;
import yu.co.certus.pos.lanus.service.Service;


public class AnnouncementProcessor extends OperationProcessor {

    private TransactionData _tData = null;


    private Calendar _lastFromDate = null;
    private Calendar _lastToDate = null;
    private Calendar _prevFromDate = null;
    private Calendar _prevToDate = null;

    private static final String _processorName = "Centrosinergija";

    private InvoiceIface _dbIface = null;


    public AnnouncementProcessor(String terminal_id, TransactionData tData, 
            InvoiceIface dbIface) {
        super(terminal_id, false, dbIface);
        _tData = tData;
        _dbIface = dbIface;
        setDates();
    }

    @Override
    public void process(AbstractRequest request, AbstractResponse response) {
        // TODO Auto-generated method stub
        AnnouncementResponse annRes = (AnnouncementResponse)response;

        if(request instanceof AnnouncementRequest){
            if(((AnnouncementRequest)request).is_isOld()){
                annRes.addResponseCode(annRes.NO_ANNOUNCEMENT_ERROR);
                return;
            }
        }
        if(_tData == null){
            annRes.addResponseCode(annRes.POS_NOT_REGISTERED_ERROR);
            return;
        }

        try{
            
            String ann = "";

//            if(true){
//                ann = formatTextForPost();
//            }else{

                ann = formatInvoice(
                        getInvoice(_tData.getPointOfSaleId(), annRes), 
                        _processorName);
 //           }
        
//        String ann = SEPARATOR + "test";
//        ann = ann + SEPARATOR + "test";
//        ann = ann + SEPARATOR + "test";
//        ann = ann + SEPARATOR + "test";
//        ann = ann + SEPARATOR + "test";
//        ann = ann + SEPARATOR + "test";
//        ann = ann + SEPARATOR;
        
        
//        String ann = SEPARATOR + "PREDRACUN:";
//        ann = ann + SEPARATOR + "Period";
//        ann = ann + SEPARATOR + "UKUPAN PROMET";
//        ann = ann + SEPARATOR + "MTS:17535.0, VIP:5150.0";
//        ann = ann + SEPARATOR + "VIP 5150.0";
//        ann = ann + SEPARATOR + "UKUPNO ZA UPLATU";
//        ann = ann + SEPARATOR;

        if(ann.equals("")){
            annRes.addResponseCode(annRes.NO_ANNOUNCEMENT_ERROR);
        }else{
            annRes.addResponseCode(annRes.ANN_SUCCESSFUL + ann);
        }
        
        }catch(PosException pos){
            if (Service.logger.isDebugEnabled()) {
                Service.logger.error(pos.getMessage(), pos);
               
              }
        }

    }




    private void setDates(){

        Calendar curr = new GregorianCalendar();
        int currDayInWeek = curr.get(Calendar.DAY_OF_WEEK);
        boolean isFirstInvoicePeriod = currDayInWeek <= Calendar.WEDNESDAY &&
        curr.get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY;



        Calendar lastDFrom = isFirstInvoicePeriod ? getLastDateFromDayInWeek(Calendar.SUNDAY,0)
                : getLastDateFromDayInWeek(Calendar.WEDNESDAY,0);
        //ispitujemo promenu meseca u tekucem periodu
        if(isMonthChanged(lastDFrom,curr)){

            //ako za zadnji dan u mesecu poklopi da je nedelja
            if(isLastDayInMonthSunday() || isLastDayInMonthWednesday()){
                _lastFromDate = isFirstInvoicePeriod ?
                        getLastDateFromDayInWeek(Calendar.THURSDAY, 0)
                        : getLastDateFromDayInWeek(Calendar.MONDAY, 0);
                        _lastToDate = isFirstInvoicePeriod ?
                                getLastDateFromDayInWeek(Calendar.SUNDAY, 0)
                                : getLastDateFromDayInWeek(Calendar.WEDNESDAY, 0);
                                _prevFromDate = isFirstInvoicePeriod ?
                                        getLastDateFromDayInWeek(Calendar.MONDAY, 1)
                                        : getLastDateFromDayInWeek(Calendar.THURSDAY, 0);
                                        if(isMonthChanged(lastDFrom,curr)){
                                            _prevFromDate.add(Calendar.DAY_OF_WEEK,-7);
                                        }
                                        _prevToDate = isFirstInvoicePeriod ?
                                                getLastDateFromDayInWeek(Calendar.WEDNESDAY, 0)
                                                : getLastDateFromDayInWeek(Calendar.SUNDAY, 0);

            }else{
                _lastFromDate = isFirstInvoicePeriod ?
                        getLastDateFromDayInWeek(Calendar.MONDAY, 0)
                        : getLastDateFromDayInWeek(Calendar.THURSDAY, 0);
                        _lastToDate = getLastDateFromPrevMonth();
                        _prevFromDate = isFirstInvoicePeriod ?
                                getLastDateFromDayInWeek(Calendar.THURSDAY, 0)
                                : getLastDateFromDayInWeek(Calendar.MONDAY, 0);
                                _prevToDate = isFirstInvoicePeriod ?
                                        getLastDateFromDayInWeek(Calendar.SUNDAY, 0)
                                        : getLastDateFromDayInWeek(Calendar.WEDNESDAY, 0);
            }
        }else{
            //ukoliko nije bilo promene meseca u tekucem periodu

            _lastFromDate = isFirstInvoicePeriod ?
                    getLastDateFromDayInWeek(Calendar.THURSDAY, 0)
                    : getLastDateFromDayInWeek(Calendar.MONDAY, 0);
                    _lastToDate = isFirstInvoicePeriod ?
                            getLastDateFromDayInWeek(Calendar.SUNDAY, 0)
                            : getLastDateFromDayInWeek(Calendar.WEDNESDAY, 0);
                            //ispitujemo promenu meseca u prethodnom periodu
                            if(isMonthChanged(_lastFromDate,_lastToDate)){
                                _lastFromDate = getFirstDayInMonth();
                                _prevToDate = getLastDateFromPrevMonth();
                                _prevFromDate = isFirstInvoicePeriod ?
                                        getLastDateFromDayInWeek(Calendar.THURSDAY, 0)
                                        : getLastDateFromDayInWeek(Calendar.MONDAY, 0);

                            }else{
                                _prevToDate = isFirstInvoicePeriod ?
                                        getLastDateFromDayInWeek(Calendar.WEDNESDAY, 0)
                                        : getLastDateFromDayInWeek(Calendar.SUNDAY, 0);

                                        int num_of_weeks = isFirstInvoicePeriod ?
                                                currDayInWeek == Calendar.MONDAY ? 0 : 1
                                                        : currDayInWeek == Calendar.THURSDAY ? 0 : 1;

                                        _prevFromDate = isFirstInvoicePeriod ?
                                                getLastDateFromDayInWeek(Calendar.MONDAY, num_of_weeks)
                                                : getLastDateFromDayInWeek(Calendar.THURSDAY, num_of_weeks);

                                                //ispitujemo promenu meseca u pre - prethodnom periodu
                                                if (isMonthChanged(_prevFromDate, _prevToDate)) {
                                                    _prevFromDate = getFirstDayInMonth();
                                                }

                            }

        }

    }

    private void setMonthlyDates(){
        _lastToDate = getLastDateFromPrevMonth();
        _lastFromDate = getLastDateFromPrevMonth();
        _lastFromDate.set(Calendar.DAY_OF_MONTH,1);
        //        System.out.println("monthly _lastToDate = " + _lastToDate.getTime());
        //        System.out.println("monthly _lastFromDate = " + _lastFromDate.getTime());
    }


    private boolean isLastDayInMonthSunday(){
        Calendar lastDayInMonth = getLastDateFromPrevMonth();
        int dayOfWeek = lastDayInMonth.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == 1;
    }


    private boolean isLastDayInMonthWednesday(){
        Calendar lastDayInMonth = getLastDateFromPrevMonth();
        int dayOfWeek = lastDayInMonth.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == 4;
    }









    public String formatInvoice(InvoiceData data,
            String processorName){
        if (Service.logger.isDebugEnabled()) {
            Service.logger.debug("-->  InvoiceData = " + data );
        }

        StringBuffer buffer = new StringBuffer();

//        buffer.append("\t\t" +
//                new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").
//                format(new Date()) + "\t\t");
        //
//        buffer.append(processorName);
//        buffer.append("\t");
//        buffer.append("Prodajno mesto:\t" + data.getPosName());
//        buffer.append("\t\t");

        buffer.append(SEPARATOR);
        buffer.append("OBRACUN ZA PERIOD");

        buffer.append(SEPARATOR);


        buffer.append("od " + data.getFromDate() );
        buffer.append(SEPARATOR);
        buffer.append("do " + data.getToDate() );
        buffer.append(SEPARATOR);
        buffer.append(SEPARATOR);
        buffer.append("PROMET");
        buffer.append(SEPARATOR);
        buffer.append("MTS " + data.getMtsPart());
//        buffer.append("\t");
        buffer.append(SEPARATOR);
        buffer.append("VIP " + data.getVipPart());
        buffer.append(SEPARATOR);
        if(data.getTelenorPart() > 0){
        buffer.append(SEPARATOR);
        buffer.append("TELENOR " + data.getTelenorPart());
        buffer.append(SEPARATOR);
        }
        if(data.get_globaltelPart() > 0){
            buffer.append(SEPARATOR);
            buffer.append("GLT  " + data.get_globaltelPart());
            buffer.append(SEPARATOR);
            }
        if(data.get_mundioPart() > 0){
            buffer.append(SEPARATOR);
            buffer.append("VCT  " + data.get_mundioPart());
            buffer.append(SEPARATOR);
            }
        if(data.getInternetPart() > 0){
        buffer.append(SEPARATOR);
        buffer.append("ABT " + data.getInternetPart());
        buffer.append(SEPARATOR);
        }
        buffer.append("NAJAM " + data.get_terminalRentFee());
        buffer.append(SEPARATOR);
        buffer.append(SEPARATOR);
//        buffer.append("TOTAL: " + data.getOverallPart());
//        buffer.append("\t\t");
//
//
//        buffer.append("UKUPNA PROVIZIJA:\t" +
//                new DecimalFormat("#.00").format((data.getOverallPart()  - data.getForPayment())) + "\r");

        buffer.append("ZA UPLATU " );
       
        buffer.append(new DecimalFormat("#.00").format(data.getForPayment()) + " DIN");

        buffer.append(SEPARATOR);
        buffer.append(SEPARATOR);
        buffer.append("UPLATITI NA");
        buffer.append(SEPARATOR);
        buffer.append(data.getAccountNumber());
        buffer.append(SEPARATOR);
        buffer.append("MODEL " + data.getModel());
        buffer.append(SEPARATOR);
        buffer.append("POZIV NA BR. "  + data.getInvoiceDetail());

//        buffer.append("\t");
        buffer.append(SEPARATOR);
        buffer.append("DOSPECE " + data.getToPayDate());
//        buffer.append("U sve iznose uracunati su\tzakonim definisani porezi");




        return buffer.toString();

    }





    public static Calendar getLastDateFromDayInWeek(int day_of_week, int weeksAgo){
        Calendar toReturn = new GregorianCalendar();

        int todaysDayInWeek = toReturn.get(Calendar.DAY_OF_WEEK);
        int offset = -1*todaysDayInWeek + day_of_week + -1*weeksAgo*NUMBER_OF_DAYS_IN_WEEK;
        if (todaysDayInWeek <= day_of_week){
            offset = offset - (weeksAgo+1)*NUMBER_OF_DAYS_IN_WEEK;
        }
        toReturn.add(Calendar.DAY_OF_WEEK, offset );

        return toReturn;

    }

    public static boolean isMonthChanged(Calendar from, Calendar to){
        return from.get(Calendar.MONTH) != to.get(Calendar.MONTH);
    }

    public static Calendar getLastDateFromPrevMonth() {
        Calendar toReturn = Calendar.getInstance();
        toReturn.set(Calendar.DAY_OF_MONTH, 1);
        toReturn.add(Calendar.DATE, -1);
        return toReturn;
    }

    public static Calendar getFirstDayInMonth() {
        Calendar toReturn = Calendar.getInstance();
        toReturn.set(Calendar.DAY_OF_MONTH, 1);
        return toReturn;
    }


    private InvoiceData getInvoice(int pointOfSaleId, 
            AnnouncementResponse response) 
    throws PosException{

        try{

            return _dbIface.getInvoice( pointOfSaleId, 
                    _lastFromDate.getTime(), _lastToDate.getTime());

        }catch(NoInvoiceDataException nide){
            response.addResponseCode(AnnouncementResponse.NO_ANNOUNCEMENT_ERROR);
            throw new PosException("No invoice for point_of_sale_id = " + pointOfSaleId);
        }catch(SQLException sqle){
            response.addResponseCode(AnnouncementResponse.NO_ANNOUNCEMENT_ERROR);
            throw new PosException("SQLException when try to get InvoiceData for point_of_sale_id = " + 
                    pointOfSaleId + ", details :" + sqle.getMessage());

        }

    }
    
    
    private String formatTextForPost(){
        
        if (Service.logger.isDebugEnabled()) {
            Service.logger.debug("-->  formatTextForPost = " );
        }

        StringBuffer buffer = new StringBuffer();



        buffer.append(SEPARATOR);
        buffer.append("Postovani,");

        buffer.append(SEPARATOR);
        buffer.append("obavestavamo Vas");
        buffer.append(SEPARATOR);
        buffer.append("da od Ponedeljka 1.2.");
        buffer.append(SEPARATOR);
        buffer.append("minimalan iznos za");
        buffer.append(SEPARATOR);
        buffer.append("uplatu VIP dopune");
        buffer.append(SEPARATOR);
        buffer.append("iznosi 200 din.");
        buffer.append(SEPARATOR);
        buffer.append("Srdacan pozdrav");
        buffer.append(SEPARATOR);
        buffer.append("CENTROSINERGIJA");
        
        return buffer.toString();
        
        
        
    }






    private static final int NUMBER_OF_DAYS_IN_WEEK = 7;
    
    
    private static final String SEPARATOR = "\n";



}
