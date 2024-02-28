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
public class ReadParamResponse
    extends AbstractResponse {
  public ReadParamResponse() {
  }

  public static final String PARAMETERS_READ = "15 Parameters read";
  public static final String OPERATOR_NOT_ACTIVE_ERROR = "25 Operator not active";
  public static final String TIMEOUT_ERROR = "35 Timeout";
  public static final String SYSTEM_FAILURE_ERROR = "45 System failure";

}
