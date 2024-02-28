package yu.co.certus.pos.lanus.concurrent;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;

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
public class LoginResourceCleaner
    extends SocketResourceCleaner {

  public LoginResourceCleaner(Socket soc,
                              BufferedReader bReader, BufferedWriter bWriter) {

    super(soc, bReader, bWriter);

  }

  public void run() {
    waitInSec(FIRST_WAIT_IN_SEC);
    send(super.KILL);
  }

}
