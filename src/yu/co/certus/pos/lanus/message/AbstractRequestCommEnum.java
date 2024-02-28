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
public abstract class AbstractRequestCommEnum {


  public abstract String getCommandKey();


  public static final String COMMAND_SEPARATOR = "&";
  public static final String USER_KEY = "User";
  public static final String USER_KEY_PREFIX = "mpos_";
  
  public static final String PASS_KEY = "OpPsw";
}
