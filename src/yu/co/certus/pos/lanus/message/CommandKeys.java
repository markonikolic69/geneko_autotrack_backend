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
public class CommandKeys {


  public static final String COMMAND_KEY = "Command";

  public static final String LOGIN_COMMAND_KEY_VALUE = "PING";
  public static final String PREPAID_COMMAND_KEY_VALUE = "TRN";
  public static final String PREPAID_WITH_REFRESH_COMMAND_KEY_VALUE = "TRNE";
  public static final String CANCEL_COMMAND_KEY_VALUE = "RTRN";
  public static final String RETRY_REQUEST_COMMAND_KEY_VALUE = "SYN";
  public static final String REPORT_REQUEST_COMMAND_KEY_VALUE = "REPORT";
  public static final String VREPORT_REQUEST_COMMAND_KEY_VALUE = "VREPORT";
  public static final String READ_PARAMETERS_COMMAND_KEY_VALUE = "RDPAR";
  public static final String SAVE_PARAMETERS_COMMAND_KEY_VALUE = "SVPAR";
  public static final String ANNOUNCEMENT_PARAMETERS_COMMAND_KEY_VALUE = "ANN";
  public static final String OLD_ANNOUNCEMENT_PARAMETERS_COMMAND_KEY_VALUE = "OLDANN";

}
