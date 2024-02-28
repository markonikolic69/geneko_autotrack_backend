package yu.co.certus.pos.lanus;

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
public class TerminalNotConnectedException
    extends Exception {
  public TerminalNotConnectedException(String teminalId, int timeInSec) {
    super("Terminal " + teminalId + " is not connected after " + timeInSec +
        " seconds");
  }
}
