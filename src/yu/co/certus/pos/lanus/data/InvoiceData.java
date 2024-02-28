package yu.co.certus.pos.lanus.data;


import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author marko nikolic (marko.nikolic@certus.co.yu)
 * @version 1.0
 */
public class InvoiceData {

    private String _posName = "";
    private String _accountNumber = "";
    /**
     * it is hardcoded
     */
    private String _model = "97";

    private String _invoiceDetail = "";

    private String _fromDate = null;
    private String _toDate = null;

    private double _mtsPart = 0;
    private double _vipPart = 0;
    private double _telenorPart = 0;
    private double _internet = 0;
    private double _mtelCGPart = 0;
    private double _globaltelPart = 0;
    public double get_globaltelPart() {
        return _globaltelPart;
    }

    public void set_globaltelPart(double part) {
        _globaltelPart = part;
    }

    public double get_mundioPart() {
        return _mundioPart;
    }

    public void set_mundioPart(double part) {
        _mundioPart = part;
    }


    private double _mundioPart = 0;
    
    private double _callCardPart = 0;

    private double _forPayment = 0;

    private double _forPaymentWithoutQVoucher = 0;

    private double _qVoucherPart = 0;
    private double _qVoucherNominalniRabat = 0;

    private Date _toPayDate = null;

    private SimpleDateFormat date_format = new SimpleDateFormat(
            "dd.MM.yyyy.");
    
    private double _terminalRentFee = 0;

    public double get_terminalRentFee() {
        return _terminalRentFee;
    }

    public void set_terminalRentFee(double rentFee) {
        _terminalRentFee = rentFee;
    }

    public InvoiceData() {
    }

    public void setQVoucherPart(double sum){
      _qVoucherPart = sum;
    }

    public double getQVoucherPart(){
      return _qVoucherPart;
    }

    public void setQVoucherNominalniRabat(double rabat){
      _qVoucherNominalniRabat = rabat;
    }

    public double getQVoucherNominalniRabat(){
      return _qVoucherNominalniRabat;
    }

    public void setPosName(String posName) {
        _posName = posName;
    }

    public void setPozivNaBroj(String pozNaBr) {
        _invoiceDetail = pozNaBr;
    }

    public void setFromDate(String fromDate) {
        _fromDate = fromDate;
    }

    public void setToDate(String toDate) {
        _toDate = toDate;
    }

    public void setMtsPart(double mts) {
        _mtsPart = mts;
    }

    public void setMtelCGPart(double mtelCG) {
        _mtelCGPart = mtelCG;
    }


    public void setVipPart(double vip) {
        _vipPart = vip;
    }
    
    public void setTelenorPart(double telenor) {
        _telenorPart = telenor;
    }

    public void setCallCardPart(double callCardPart) {
      _callCardPart = callCardPart;
    }

    public void setInternet(double internet) {
        _internet = internet;
    }

    public void setForPayment(double pay) {
        _forPayment = pay;
    }

    public void setForPaymentWithoutQV(double payWQV) {
        _forPaymentWithoutQVoucher = payWQV;
    }


    public void setToPayDate(Date toPay) {
        _toPayDate = toPay;
    }


    public String getPosName() {
        return _posName;
    }

    public void setAccountNumber(String accNum) {
        _accountNumber = accNum;
    }

    public String getAccountNumber() {
        return _accountNumber;
    }

    public String getInvoiceDetail() {
        return _invoiceDetail;
    }

    public String getModel() {
        return _model;
    }

    public String getFromDate() {
        return _fromDate;
    }

    public String getToDate() {
        return _toDate;
    }

    public double getMtsPart() {
        return _mtsPart;
    }

    public double getMtelCGPart() {
        return _mtelCGPart;
    }


    public double getVipPart() {
        return _vipPart;
    }
    
    public double getTelenorPart() {
        return _telenorPart;
    }

    public double getInternetPart() {
        return _internet;
    }

    public double getCallingCardPart() {
        return _callCardPart;
    }


    public double getOverallPart() {
        return _mtsPart + _vipPart + _internet + _callCardPart + _mtelCGPart + 
        _terminalRentFee + _telenorPart + _mundioPart + _globaltelPart;
    }

    public double getForPayment() {
        return _forPayment;
    }

    public double getForPaymentWithoutQV(){
      return _forPaymentWithoutQVoucher;
    }

//    public double getTurnoverCommision() {
//        return getOverallPart() - getForPayment();
//    }

    public String getToPayDate() {
        return date_format.format(_toPayDate);
    }


    public String toString() {
        return "_posName = " + _posName +
                ", _accountNumber = " + _accountNumber +
                ", _model = " + _model +
                ", _invoiceDetail = " + _invoiceDetail +
                ", _fromDate = " + _fromDate +
                ", _toDate = " + _toDate +
                ", _mtsPart = " + _mtsPart +
                ", _telenorPart = " + _telenorPart +
                ", _mtelCGPart = " + _mtelCGPart +
                ", _vipPart = " + _vipPart +
                ", _internet = " + _internet +
                ", _forPayment = " + _forPayment +
                ", _toPayDate = " + _toPayDate +
                ", _callCardPart = " + _callCardPart;

    }


}
