package yu.co.certus.malisocket;

import java.nio.channels.*;
import java.nio.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.Charset;
import yu.co.certus.pos.lanus.message.LoginResponse;
import yu.co.certus.pos.lanus.message.PrepaidResponse;
import java.nio.charset.CharsetEncoder;




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
public class NioService {

  private ServerSocketChannel _serSocChann = null;

  private static Charset charset = Charset.forName("US-ASCII");
  private static CharsetDecoder decoder = charset.newDecoder();

  private static CharsetEncoder encoder = charset.newEncoder();

  public NioService() throws IOException {
    _serSocChann = createSocketChannel("192.168.0.5"/*"172.18.22.5"*/, 40001);

  }

  // Creates a non-blocking socket channel for the specified host name and port.
  // connect() is called on the new channel before it is returned.
  private static ServerSocketChannel createSocketChannel(String hostName,
      int port) throws IOException {
    // Create a non-blocking server socket and check for connections

    // Create a non-blocking server socket channel on port 80
    ServerSocketChannel ssChannel = ServerSocketChannel.open();
    ssChannel.configureBlocking(true);

    ssChannel.socket().bind(new InetSocketAddress(port));

    // See e178 Accepting a Connection on a ServerSocketChannel
    // for an example of accepting a connection request
    return ssChannel;

  }

  public void accept() {

    ByteBuffer buf = ByteBuffer.allocateDirect(1024);

    boolean isLogin = true;

    try {
      // Accept the connection request.
      // If serverSocketChannel is blocking, this method blocks.
      // The returned channel is in blocking mode.

      while (true) {
        SocketChannel sChannel = _serSocChann.accept();

        sChannel.socket().setSendBufferSize(1);
        //System.out.println("send Buffer size:" + );
        System.out.println("send Buffer size:" + sChannel.socket().getSendBufferSize());
        System.out.println("receive Buffer size:" + sChannel.socket().getReceiveBufferSize());



        // If serverSocketChannel is non-blocking, sChannel may be null
        if (sChannel == null) {
          // There were no pending connection requests; try again later.
          // To be notified of connection requests,
          // see e179 Using a Selector to Manage Non-Blocking Server Sockets.
          System.out.println("dobio null");
        }
        else {
          // Use the socket channel to communicate with the client
          // See e176 Using a Selector to Manage Non-Blocking Sockets.
          System.out.println("dobio not null");
          //try{Thread.sleep(15000);}catch(Throwable e){}
          //System.out.println("Try to write to the socket");
          boolean send = true;
          while (send) {
            buf.clear();
            int numBytesRead = sChannel.read(buf);
            System.out.println("Length of bytes to read " + numBytesRead);
            buf.flip();
            CharBuffer cBuff = decoder.decode(buf);
            System.out.println("What is read " + cBuff);
            String toWrite = "";
            CharBuffer writeBuffer = CharBuffer.allocate(100);
            //CharBuffer writeBuffer = ByteBuffer.allocateDirect(100).
            //    asCharBuffer();
            if (isLogin) {
              LoginResponse logRes = new LoginResponse();
              logRes.addCnt("1");
              logRes.addmID("D0041047");
              logRes.addOperatorName("Administrator");
              logRes.addResponseCode(logRes.LOGIN_SUCCESSFUL);

              toWrite = logRes.forPos();
              isLogin = false;
              System.out.println("Ispisao je bajtova = " + sChannel.write(
                  encoder.encode(writeBuffer.wrap(toWrite))));
            }
            else {
              PrepaidResponse preRes = new PrepaidResponse();
              preRes.addTransactionId("14110042994452");
              preRes.addTime();
              preRes.addResponseCode(preRes.TRANSACTION_OK);

              toWrite = preRes.forPos();
/*
              try {
                System.out.println("sleep");
                Thread.sleep(15000);
              }
              catch (Throwable e) {

              }
*/
/*
              try{
                write(writeBuffer, sChannel, toWrite);
              }catch(Exception e){
                System.out.println("!!!!!!!!!!!!!!!!!Uhvatio exception, details:" + e.getMessage());
              }catch(Throwable e){
                System.out.println("!!!!!!!!!!!!!!!!!Uhvatio nepoznat exception, details:" + e.getMessage());
              }
*/


              //writeBuffer.isDirect();
              //sChannel.


              sChannel.write(encoder.encode(writeBuffer.wrap(toWrite)));
/*
              try {
                System.out.println("sleep posle");
                Thread.sleep(15000);
              }
              catch (Throwable e) {

              }
*/





/*
              System.out.println("Ispisao je bajtova = " + sChannel.write(
                  encoder.encode(writeBuffer.wrap(toWrite + toWrite + toWrite + toWrite + toWrite + toWrite +
                                 toWrite + toWrite + toWrite + toWrite + toWrite))));
              System.out.println("isBlocking = " + sChannel.isBlocking());
              System.out.println("isOpen = " + sChannel.isOpen());
              System.out.println("isConnected = " + sChannel.isConnected());
              System.out.println("isRegistered = " + sChannel.isRegistered());
              System.out.println("buffer hasRemaining = " +
                                 writeBuffer.hasRemaining());
              System.out.println("buffer isDirect = " + writeBuffer.isDirect());
              System.out.println("buffer position = " + writeBuffer.position());


              System.out.println("socket send buffer size = " + sChannel.socket().getSendBufferSize());
              System.out.println("socket receive buffer size = " + sChannel.socket().getReceiveBufferSize());


              try {
                System.out.println("sleep");
                Thread.sleep(3000);
              }
              catch (Throwable e) {

              }

              System.out.println("send Buffer size posle:" + sChannel.socket().getSendBufferSize());
        System.out.println("receive Buffer size posle:" + sChannel.socket().getReceiveBufferSize());



              System.out.println("Cekam na citanju");
              int posleNaCitanju = sChannel.read(buf);
              System.out.println("procitao velicinu: " + posleNaCitanju);



              System.out.println("Cekam opet na citanju");
              int opetposleNaCitanju = sChannel.read(buf);
              System.out.println("procitao velicinu: " + opetposleNaCitanju);








              System.out.println("isBlocking = " + sChannel.isBlocking());
              System.out.println("isOpen = " + sChannel.isOpen());
              System.out.println("isConnected = " + sChannel.isConnected());
              System.out.println("isRegistered = " + sChannel.isRegistered());
              System.out.println("buffer hasRemaining = " +
                                 writeBuffer.hasRemaining());
              System.out.println("buffer position = " + writeBuffer.position());
              System.out.println("buffer isDirect = " + writeBuffer.isDirect());
              System.out.println("Ispisao je bajtova = " + sChannel.write(
                  encoder.encode(writeBuffer.wrap(toWrite))));

              System.out.println("Cekam opet 1 na citanju");
              int opet1posleNaCitanju = sChannel.read(buf);
              System.out.println("procitao velicinu: " + opet1posleNaCitanju);


              try {
                System.out.println("sleep");
                Thread.sleep(30000);
              }
              catch (Throwable e) {

              }
              System.out.println("isBlocking = " + sChannel.isBlocking());
              System.out.println("isOpen = " + sChannel.isOpen());
              System.out.println("isConnected = " + sChannel.isConnected());
              System.out.println("isRegistered = " + sChannel.isRegistered());
              System.out.println("buffer hasRemaining = " +
                                 writeBuffer.hasRemaining());
              System.out.println("buffer position = " + writeBuffer.position());
              System.out.println("buffer isDirect = " + writeBuffer.isDirect());
              System.out.println("Ispisao je bajtova = " + sChannel.write(
                  encoder.encode(writeBuffer.wrap(toWrite))));

              try {
                System.out.println("sleep");
                Thread.sleep(40000);
              }
              catch (Throwable e) {

              }
              System.out.println("isBlocking = " + sChannel.isBlocking());
              System.out.println("isOpen = " + sChannel.isOpen());
              System.out.println("isConnected = " + sChannel.isConnected());
              System.out.println("isRegistered = " + sChannel.isRegistered());
              System.out.println("buffer hasRemaining = " +
                                 writeBuffer.hasRemaining());
              System.out.println("buffer position = " + writeBuffer.position());
              System.out.println("buffer isDirect = " + writeBuffer.isDirect());
              System.out.println("Ispisao je bajtova = " + sChannel.write(
                  encoder.encode(writeBuffer.wrap(toWrite))));

              isLogin = true;
              send = false;
*/
            }
            System.out.println(toWrite);
            System.out.println("HOST ADDRESS = " + sChannel.socket().getInetAddress().getHostAddress());
        System.out.println("IS REACHABLE = " + sChannel.socket().getInetAddress().isReachable(5));

            System.out.println("finish connect = " + sChannel.finishConnect());

            sChannel.validOps();




          }
          //System.out.println("Is connected = " + sChannel.isConnected());
          //System.out.println("Is connection pending = " + sChannel.isConnectionPending());

        }
      }
    }
    catch (IOException e) {
      System.out.println(e);
      System.out.println("IOException , details:" + e.getMessage());
      e.printStackTrace(System.out);
    }
  }

  private void write(CharBuffer buf, SocketChannel ch, String toWrite) throws Exception{
    try{
      System.out.println("Configure blocking");
      SelectableChannel selCh = ch.configureBlocking(false);
      int count;
      Selector sel = Selector.open();
      System.out.println("Selector open");
      SelectionKey key = null;
      System.out.println("Buffer position " + buf.position());
      while (buf.position() > 0) {

        //buf.flip();
        count = ch.write(encoder.encode(buf.wrap(toWrite)));
        System.out.println("ch.write count =  " + count);
        //buf.compact();
        if (count == 0) {
          System.out.println("usao kada je count 0");
          System.out.println("selector = " + sel);
          System.out.println("ch = " + ch);
          try {
            key = ch.register(sel, SelectionKey.OP_WRITE);
          }
          catch (Throwable e) {

            System.out.println("bacio exception na registrovanju selectora " +
                               sel);
            throw new Exception("OK");
          }
          System.out.println("registrovao selector za OP_WRITE");
          int nsel = sel.select(5000);
          System.out.println("wratio broj selectora = " + nsel);
          if (nsel == 0)
            throw new Exception("write");
        }
        else
        if (key != null)
          key.cancel();
      }
      System.out.println("izasao, key je = " + key);
    }finally{
      ch.close();
    }

  }

  public static void main(String[] args) throws IOException {
    new NioService().accept();
  }

}
