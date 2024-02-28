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
public class ReadParamCommEnum
    extends AbstractTransactionRequestCommEnum {
  public ReadParamCommEnum() {
  }

  /**
   * getCommandKey
   *
   * @return String
   * @todo Implement this
   *   yu.co.certus.pos.lanus.message.AbstractRequestCommEnum method
   */
  public String getCommandKey() {
    return CommandKeys.READ_PARAMETERS_COMMAND_KEY_VALUE;
  }
}
