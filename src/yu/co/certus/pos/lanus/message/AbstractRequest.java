package yu.co.certus.pos.lanus.message;


import java.util.Map;
import java.util.HashMap;

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
public abstract class AbstractRequest {
  private Map _client_params = new HashMap<String,String>();

  private String _command = "Unknown";


  public void addParam(String key, String value){
    if(key.equals(CommandKeys.COMMAND_KEY)){
      _command = value;
    }else{
      _client_params.put(key, value);
    }
  }

  public String getCommand(){
    return _command;
  }

  public String getParam(String key){
    return _client_params.get(key) == null ? "" :
        _client_params.get(key).toString();
  }

  public String getCookie(){
    return getParam(AbstractTransactionRequestCommEnum.COOKIE_KEY);
  }

  public String getVersion(){
      String toReturn = getParam(AbstractTransactionRequestCommEnum.POS_VERSION_KEY);
      if(toReturn != null){
          if(toReturn.startsWith("mPOS")){
              toReturn = toReturn.substring("mPOS".length());
          }
      }
      return toReturn;
  }
  
  public String getUserId(){
      String pos_id = getParam(AbstractRequestCommEnum.USER_KEY);
      return pos_id.startsWith(AbstractRequestCommEnum.USER_KEY_PREFIX) ?
          pos_id.substring(AbstractRequestCommEnum.USER_KEY_PREFIX.length())
          :
          pos_id;
    }
  
  public String getOperatorPass(){
      String pass = getParam(AbstractRequestCommEnum.PASS_KEY);
      if(pass.startsWith("0000")){
      return pass.substring(4);
      }else{
          return pass;
      }
    }

}
