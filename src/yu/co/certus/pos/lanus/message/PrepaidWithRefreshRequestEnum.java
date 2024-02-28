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
public class PrepaidWithRefreshRequestEnum
    extends PrepaidRequestCommEnum {
  public PrepaidWithRefreshRequestEnum() {
  }

  public String getCommandKey() {
    return CommandKeys.PREPAID_WITH_REFRESH_COMMAND_KEY_VALUE;
  }

}
