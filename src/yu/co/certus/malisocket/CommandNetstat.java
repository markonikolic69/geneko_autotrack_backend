package yu.co.certus.malisocket;

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
public class CommandNetstat {

  private String _ip = "";
  private String _port = "";
  public CommandNetstat(String ip, String port) {
    _ip = ip;
    _port = port;
  }

 /*
  public String getCommandOutput(){
    String command =
        "netstat -nal | grep 172.18.22.5";

    Process child = Runtime.getRuntime().exec(command);


    try {
        // Execute command
        String command = "ls";
        Process child = Runtime.getRuntime().exec(command);

        // Get the input stream and read from it
        InputStream in = child.getInputStream();
        int c;
        while ((c = in.read()) != -1) {
            process((char)c);
        }
        in.close();
    } catch (IOException e) {
    }



    return

  }
  */
}
