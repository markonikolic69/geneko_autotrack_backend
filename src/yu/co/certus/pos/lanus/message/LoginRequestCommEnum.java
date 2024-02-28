package yu.co.certus.pos.lanus.message;

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
public class LoginRequestCommEnum extends AbstractRequestCommEnum{



  

  public static final String ENCRIPTION_KEY = "Encr";


  public String getCommandKey(){
    return CommandKeys.LOGIN_COMMAND_KEY_VALUE;
  }

}
