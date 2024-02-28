package yu.co.certus.pos.lanus.message;



import yu.co.certus.pos.lanus.service.Service;

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
public class RequestParser {

  private String _request = "";

  private AbstractRequest _parsedRequest = new UnknownRequest();

  public RequestParser(String from_client) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("--> from_client = " + from_client);
        }
    _request = from_client;
    String[] params = _request.substring(REQUEST_PREFIX.length(),
                                         _request.length() -
                                         REQUEST_SUFFIX.length()).
        split(AbstractRequestCommEnum.COMMAND_SEPARATOR);

    _parsedRequest = parse(params);
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<--" );
      }
  }

  public AbstractRequest getRequest(){
    return _parsedRequest;
  }

  private AbstractRequest parse(String[] params) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
    AbstractRequest toReturn = new UnknownRequest();
    try {

      if (params.length > 1) {
        String command = params[0].split("=")[1];
        if (command.equals(CommandKeys.LOGIN_COMMAND_KEY_VALUE)) {
          toReturn = parseLogin(params);
        }
        if (command.equals(CommandKeys.PREPAID_COMMAND_KEY_VALUE) ||
            command.equals(CommandKeys.PREPAID_WITH_REFRESH_COMMAND_KEY_VALUE)) {
          toReturn = parseTransaction(params,
              command.equals(CommandKeys.PREPAID_WITH_REFRESH_COMMAND_KEY_VALUE));
//          String phone = toReturn.getParam(PrepaidRequestCommEnum.PHONE_KEY);
//          if(phone.startsWith("99")){
//            LastTopUpRequest ltRequest = new LastTopUpRequest();
//            int amount = Integer.parseInt(toReturn.
//                                          getParam(PrepaidRequestCommEnum.AMOUNT_KEY))/100;
//            ltRequest.setAmount(amount);
//            ltRequest.setPhone(phone.substring("99".length()));
//            toReturn = ltRequest;
//          }
        }
        if (command.equals(CommandKeys.READ_PARAMETERS_COMMAND_KEY_VALUE)) {
          toReturn = parseReadParam(params);
        }
        if (command.equals(CommandKeys.RETRY_REQUEST_COMMAND_KEY_VALUE)) {
          toReturn = parseRetry(params);
        }
        if (command.equals(CommandKeys.SAVE_PARAMETERS_COMMAND_KEY_VALUE)) {
          toReturn = parseSaveParam(params);
        }
        if (command.equals(CommandKeys.CANCEL_COMMAND_KEY_VALUE)) {
          toReturn = parseCancel(params);
        }
        if (command.equals(CommandKeys.REPORT_REQUEST_COMMAND_KEY_VALUE) ||
                command.equals(CommandKeys.VREPORT_REQUEST_COMMAND_KEY_VALUE)) {
            toReturn = parseReport(params, command.equals(CommandKeys.VREPORT_REQUEST_COMMAND_KEY_VALUE));
        }
        if (command.equals(CommandKeys.ANNOUNCEMENT_PARAMETERS_COMMAND_KEY_VALUE) ||
                command.equals(CommandKeys.OLD_ANNOUNCEMENT_PARAMETERS_COMMAND_KEY_VALUE)) {
            toReturn = parseAnnouncement(params,
                    command.equals(CommandKeys.OLD_ANNOUNCEMENT_PARAMETERS_COMMAND_KEY_VALUE));
        }
      }
 
    }
    catch (Throwable e) {
        if (Service.logger.isDebugEnabled()) {
            Service.logger.error("Unknow exception when parse request, details: " +
                    e.getMessage(), e);
          }
    }
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<-- return = " + toReturn);
      }
    return toReturn;
  }

  private AbstractRequest parseLogin(String[] params) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
    LoginRequest toReturn = new LoginRequest();
    parse(toReturn, params);
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<-- toReturn = " + toReturn);
      }
    return toReturn;
  }

  private AbstractRequest parseTransaction(String[] params, boolean includeAddInfo) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      PrepaidRequest toReturn = new PrepaidRequest();
    toReturn.setIncludeAddInfo(includeAddInfo);
    parse(toReturn, params);
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<-- toReturn = " + toReturn);
      }
    return toReturn;
  }

  private AbstractRequest parseCancel(String[] params) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      CancelRequest toReturn = new CancelRequest();
    parse(toReturn, params);
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<-- toReturn = " + toReturn);
      }
    return toReturn;
  }
  
  private AbstractRequest parseReport(String[] params, boolean is_V_REPORT) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      ReportRequest toReturn = new ReportRequest();
      toReturn.set_isV_REPORT(is_V_REPORT);
      parse(toReturn, params);
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("<-- toReturn = " + toReturn );
        }
      return toReturn;
  }
  
  private AbstractRequest parseAnnouncement(String[] params, boolean isOldAnnouncement) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      AnnouncementRequest toReturn = new AnnouncementRequest();
      toReturn.set_isOld(isOldAnnouncement);
      parse(toReturn, params);
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("<-- toReturn = " + toReturn );
        }
      return toReturn;
  }

  private AbstractRequest parseRetry(String[] params) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      RetryRequest toReturn = new RetryRequest();
    for (int i = 0; i < params.length; i++) {
      String currentName = params[i].split("=")[0];
      String currentValue = params[i].split("=").length > 1
          ? params[i].split("=")[1] : "";
      if (currentName.equals(RetryRequestCommEnum.LAST_COMMAND_KEY/*RetryRequestCommEnum.RETRY_COMMAND_KEY*/)) {
        String[] originalParams = new String[params.length - i];
        if (originalParams.length > 0) {
          System.arraycopy(params, i, originalParams, 0, originalParams.length);
          toReturn.setOriginalRequest(parse(originalParams));
        }
      }
      else {
        toReturn.addParam(currentName, currentValue);
      }

    }
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<-- toReturn = " + toReturn);
      }
    return toReturn;
  }

  private AbstractRequest parseSaveParam(String[] params) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      SaveParamRequest toReturn = new SaveParamRequest();
    parse(toReturn, params);
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<-- toReturn = " + toReturn);
      }
    return toReturn;
  }

  private AbstractRequest parseReadParam(String[] params) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      ReadParamRequest toReturn = new ReadParamRequest();
    parse(new ReadParamRequest(), params);
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<-- toReturn = " + toReturn);
      }
    return toReturn;
  }

  public String fromClient() {
    return _request;
  }

  private void parse(AbstractRequest request, String[] params) {
      if (Service.logger.isDebugEnabled()) {
          Service.logger.debug("-->");
        }
      try {
      for (int i = 0; i < params.length; i++) {
        String currentName = params[i].split("=")[0];
        String currentValue = params[i].split("=").length == 2 ?
            params[i].split("=")[1]
            :
            "0";
        request.addParam(currentName, currentValue);
      }

    }
    catch (Throwable e) {
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error(
            "Unknown request parsing error for request "+_request+", details : " +
            e.getMessage(), e);

      }

    }
    if (Service.logger.isDebugEnabled()) {
        Service.logger.debug("<--");
      }

  }

  public static final String REQUEST_PREFIX = "GET /mPOS/mPOS.aspx?MfcISAPI";
  public static final String REQUEST_SUFFIX = " HTTP/1.1";
  
  
  //GET /mPOS/mPOS.aspx?MfcISAPICommand=PING&TerType=KONSING&User=mpos_354687000539847 HTTP/1.1
 



  public static void main(String[] args){
    RequestParser logParser = new RequestParser("GET /mPOS/mPOS.aspx?MfcISAPICommand=PING&TerType=KONSING&User=mpos_354687000539847 HTTP/1.1");
    AbstractRequest logReq = logParser.getRequest();
    System.out.println(logReq);

    RequestParser prepaidParser = new RequestParser("GET /mPOS/mPOS.aspx?MfcISAPICommand=TRN&Ver=WAVETEC&Cookie=T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5&Uid=381641243145&Cid=1&mP=1&Amt=500&Pmt=1&PT=1 HTTP/1.1");
    AbstractRequest preReq = prepaidParser.getRequest();
    System.out.println(preReq);

    RequestParser canParser = new RequestParser("GET /mPOS/mPOS.aspx?MfcISAPICommand=RTRN&Ver=WAVETEC&Cookie=T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5&TxnId=44400111222333 HTTP/1.1");
    AbstractRequest canReq = canParser.getRequest();
    System.out.println(canReq);
    
    RequestParser retryParser1_consing_topup = new RequestParser("GET /mPOS/mPOS.aspx?MfcISAPICommand=SYN&Ver=WAVETEC&Last=TRN&Uid=381641243145&Cid=1&mP=1&Amt=5000&Pmt=1&PT=1 HTTP/1.1");
    AbstractRequest retryReq1 = retryParser1_consing_topup.getRequest();
    System.out.println(retryReq1);
    
  RequestParser retryParser3_consing_cancel = new RequestParser("GET /mPOS/mPOS.aspx?MfcISAPICommand=SYN&Ver=WAVETEC&Last=RTRN&TxnId=14800111222333 HTTP/1.1");
  AbstractRequest retryReq3 = retryParser3_consing_cancel.getRequest();
  System.out.println(retryReq3);
  
  RequestParser report_consing = new RequestParser("GET /mPOS/mPOS.aspx?MfcISAPICommand=REPORT&Ver=mPOS1.2&Encr=PLN&TerType=KONSING&User=mpos_354687000539847&DTF=16.02.2012 HTTP/1.1");
  AbstractRequest rep_cons = report_consing.getRequest();
  System.out.println(rep_cons);
  
  RequestParser announcement = new RequestParser("GET /mPOS/mPOS.aspx?MfcISAPICommand=ANN&User=mpos_354687000539847&TerType=KONSING HTTP/1.1");
  AbstractRequest announ = announcement.getRequest();
  System.out.println(announ);
  
  
  //GET /mPOS/mPOS.aspx?MfcISAPICommand=ANN&User=mpos_354687000539847&TerType=KONSING HTTP/1.1
  
  
  //GET /mPOS/mPOS.aspx?MfcISAPICommand=REPORT&Ver=mPOS1.2&Encr=PLN&TerType=KONSING&User=mpos_354687000539847&DTF=16.02.2012 HTTP/1.1

//    RequestParser retryParser1 = new RequestParser("GET /mPOS/mPOS.dll?MfcISAPICommand=SYN&Ver=WAVETEC&Cookie=T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5&Last=TRNE&Ltid=&RetryCmd=TRN&Uid=381641243145&Cid=1&mP=1&Amt=5000&Pmt=1&PT=1 HTTP/1.1");
//    AbstractRequest retryReq1 = retryParser1.getRequest();
//    System.out.println(retryReq1);
    
//    RequestParser retryParser2 = new RequestParser("GET /mPOS/mPOS.dll?MfcISAPICommand=SYN&Ver=WAVETEC&Cookie=T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5&Last=TRNE&Ltid= HTTP/1.1");
//    AbstractRequest retryReq2 = retryParser2.getRequest();
//    System.out.println(retryReq2);
//
//    RequestParser retryParser3 = new RequestParser("GET /mPOS/mPOS.dll?MfcISAPICommand=SYN&Ver=WAVETEC&Cookie=T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5&Last=TRNE&Ltid=&RetryCmd=RTRN&TxnId=14800111222333 HTTP/1.1");
//    AbstractRequest retryReq3 = retryParser3.getRequest();
//    System.out.println(retryReq3);
//
//    RequestParser retryParser4 = new RequestParser("GET /mPOS/mPOS.dll?MfcISAPICommand=SYN&Ver=WAVETEC&Cookie=T9I8CV39TE9CHS2C2IQ4Y0EK2BPJ25H5&Last=TRNE&Ltid= HTTP/1.1");
//    AbstractRequest retryReq4 = retryParser4.getRequest();
//    System.out.println(retryReq4);
//
//
//
//
//    RequestParser transProblem = new RequestParser("GET /mPOS/mPOS.dll?MfcISAPICommand=TRN&Ver=mPOS1.2&Cookie=7284d13423921f5264b6af4c3d200a12&Uid=381645693118&Cid=&mP=&Amt=5000&Pmt=1&PT=1 HTTP/1.1");
//    AbstractRequest transReq4 = transProblem.getRequest();
//    System.out.println("Problematicni :" + transReq4);












  }
}
