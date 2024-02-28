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
public class SaveParamResponse
    extends AbstractResponse {
  public SaveParamResponse() {
  }

  public static final String PARAMETERS_SAVED = "16 Parameters saved";
  public static final String OPERATOR_NOT_ACTIVE_ERROR = "26 Operator not active";
  public static final String TIMEOUT_ERROR = "36 Timeout";
  public static final String SYSTEM_FAILURE_ERROR = "46 System failure";
}
