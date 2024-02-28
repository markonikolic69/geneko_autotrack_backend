package yu.co.certus.pos.lanus.message;


import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import java.text.SimpleDateFormat;

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
public abstract class AbstractResponse {

  private List _returnParams = new ArrayList<String>();


  private String _responseCodeValue = "";



  private SimpleDateFormat _http_date_formatter =
      new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");

  public SimpleDateFormat pos_date_formatter =
      new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");


  public AbstractResponse() {
  }

  public void addTime(){
    addParameter("Time",pos_date_formatter.format(new Date()));
  }

  public void addParameter(String name, String value){
    _returnParams.add(name + ":" + value.trim());
  }

  public void addResponseCode(String responseCode){
    addParameter("Err",responseCode);
    _responseCodeValue = responseCode;
  }

  public String getResponseCode(){
    return _responseCodeValue;
  }



  public String forPos(){
    Date now = new Date();
    StringBuffer data_buf = new StringBuffer();
    for(Iterator iter = _returnParams.iterator();iter.hasNext();){
      data_buf.append(iter.next());
      data_buf.append(PARAMETER_SEPARATOR);
    }

    StringBuffer toReturn = new StringBuffer();
    ///start http header
  toReturn.append(HTTP_HEADER_FIRST_LINE);
  toReturn.append(RESPONSE_SEPARATOR);
  toReturn.append(HTTP_HEADER_SECOND_LINE);
  toReturn.append(RESPONSE_SEPARATOR);
  toReturn.append(HTTP_HEADER_THIRD_LINE);
  toReturn.append(RESPONSE_SEPARATOR);
  toReturn.append(HTTP_HEADER_FORTH_LINE);
  toReturn.append(RESPONSE_SEPARATOR);
  toReturn.append(HTTP_HEADER_DATE_PREFICS + _http_date_formatter.format(now));
  toReturn.append(RESPONSE_SEPARATOR);
  toReturn.append(HTTP_HEADER_LENGTH_PREFICS + data_buf.length());
  toReturn.append(RESPONSE_SEPARATOR);
  toReturn.append(RESPONSE_SEPARATOR);

    ///end of http header////
    ///start with data
    toReturn.append(data_buf);
    data_buf.append(PARAMETER_SEPARATOR);
    data_buf.append(PARAMETER_SEPARATOR);
    data_buf.append(PARAMETER_SEPARATOR);
    //toReturn.append(RESPONSE_SEPARATOR);


    ///end data
    return toReturn.toString();

  }


  private static final String HTTP_HEADER_FIRST_LINE = "HTTP/1.1 200 OK";
  private static final String HTTP_HEADER_SECOND_LINE = "Cache-Control: private";
  private static final String HTTP_HEADER_THIRD_LINE = "Content-Type: text/html; charset=utf-8";
  private static final String HTTP_HEADER_FORTH_LINE = "Server: Apache-Coyote/1.1";
  private static final String HTTP_HEADER_LENGTH_PREFICS = "Content-Length: ";
  private static final String HTTP_HEADER_DATE_PREFICS = "Date: ";
  public static final String RESPONSE_SEPARATOR = "\r\n";
  public static final String PARAMETER_SEPARATOR = "\n";
  
  
  public static final String UNKNOWN_ERROR = " Unknown error";
  
  public static final String POS_NOT_REGISTERED_ERROR = "28 mPOS is not registered";
}
