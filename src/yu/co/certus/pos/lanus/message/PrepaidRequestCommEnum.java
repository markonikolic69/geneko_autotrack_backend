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
public class PrepaidRequestCommEnum
    extends AbstractTransactionRequestCommEnum {
  public PrepaidRequestCommEnum() {
  }

  /**
   * getCommandKey
   *
   * @return String
   * @todo Implement this
   *   yu.co.certus.pos.lanus.message.AbstractRequestCommEnum method
   */
  public String getCommandKey() {
    return CommandKeys.PREPAID_COMMAND_KEY_VALUE;
  }

  public static final String PHONE_KEY = "Uid";
  public static final String CARD_PHONE_KEY = "Cid";
  public static final String ENCRIPTION_MPIN_KEY = "mP";
  public static final String AMOUNT_KEY = "Amt";
  public static final String PAYMENT_KEY = "Pmt";
  public static final String WAY_OF_PAYMENT_KEY = "PT";
  public static final String POINT_OF_SALE_1_KEY = "PM1";
  public static final String POINT_OF_SALE_2_KEY = "PM2";
  public static final String ADVERTISING_LINE_1_KEY = "AD1";
  public static final String ADVERTISING_LINE_2_KEY = "AD2";
  public static final String ADVERTISING_LINE_3_KEY = "AD3";
  public static final String ADVERTISING_LINE_4_KEY = "AD4";
  public static final String ADVERTISING_LINE_5_KEY = "AD5";
  public static final String ANNOUNCEMENT_KEY = "Ann";
  public static final String ANNOUNCEMENT_VALUE = "1";
  public static final String MOBILE_NETWORK_KEY = "MobileNetwork";
}
