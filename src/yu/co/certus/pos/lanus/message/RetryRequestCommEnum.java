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
public class RetryRequestCommEnum
    extends AbstractTransactionRequestCommEnum {
  public RetryRequestCommEnum() {
  }

  /**
   * getCommandKey
   *
   * @return String
   * @todo Implement this
   *   yu.co.certus.pos.lanus.message.AbstractRequestCommEnum method
   */
  public String getCommandKey() {
    return CommandKeys.RETRY_REQUEST_COMMAND_KEY_VALUE;
  }

  public static final String LAST_COMMAND_KEY = "Last";
  public static final String LAST_TRANSACTION_ID_KEY = "Ltid";

  //public static final String RETRY_COMMAND_KEY  = "RetryCmd";

}
