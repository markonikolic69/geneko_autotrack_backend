package yu.co.certus.malisocket;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Set;
import java.nio.channels.SelectionKey;
import java.nio.charset.CharsetDecoder;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.io.File;
import java.nio.charset.Charset;
import java.io.RandomAccessFile;

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
public class NonBlockingServer {

  public Selector sel = null;
  public ServerSocketChannel server = null;
  public SocketChannel socket = null;
  public int port = 40001;
  String result = null;

  public NonBlockingServer() {
    System.out.println("Inside default ctor");
  }

  public NonBlockingServer(int port) {
    System.out.println("Inside the other ctor");
    port = port;
  }

  public void initializeOperations() throws IOException, UnknownHostException {
    System.out.println("Inside initialization");
    sel = Selector.open();
    server = ServerSocketChannel.open();
    server.configureBlocking(false);
    InetAddress ia = InetAddress.getLocalHost();//getByName("172.18.22.5");
    InetSocketAddress isa = new InetSocketAddress(ia, port);
    server.socket().bind(isa);
  }

  public void startServer() throws IOException {
    System.out.println("Inside startserver");
    initializeOperations();
    System.out.println("Abt to block on select()");
    SelectionKey acceptKey = server.register(sel, SelectionKey.OP_ACCEPT);

    while (acceptKey.selector().select() > 0) {

      Set readyKeys = sel.selectedKeys();
      Iterator it = readyKeys.iterator();

      while (it.hasNext()) {
        SelectionKey key = (SelectionKey) it.next();
        it.remove();

        if (key.isAcceptable()) {
          System.out.println("Key is Acceptable");
          ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
          socket = (SocketChannel) ssc.accept();
          socket.configureBlocking(false);
          SelectionKey another = socket.register(sel,
                                                 SelectionKey.OP_READ |
                                                 SelectionKey.OP_WRITE);
        }
        if (key.isReadable()) {
          //System.out.println("Key is readable");
          String ret = readMessage(key);
          if (ret.length() > 0) {
            writeMessage(socket, ret);
          }
        }
        if (key.isWritable()) {
          //System.out.println("THe key is writable");
          String ret = readMessage(key);
          socket = (SocketChannel) key.channel();
          if (result.length() > 0) {
            writeMessage(socket, ret);
          }
        }
      }
    }
  }

  public void writeMessage(SocketChannel socket, String ret) {
    System.out.println("Inside the loop");

    if (ret.equals("quit") || ret.equals("shutdown")) {
      return;
    }
    File file = new File(ret);
    try {

      RandomAccessFile rdm = new RandomAccessFile(file, "r");
      FileChannel fc = rdm.getChannel();
      ByteBuffer buffer = ByteBuffer.allocate(1024);
      fc.read(buffer);
      buffer.flip();

      Charset set = Charset.forName("us-ascii");
      CharsetDecoder dec = set.newDecoder();
      CharBuffer charBuf = dec.decode(buffer);
      System.out.println(charBuf.toString());
      buffer = ByteBuffer.wrap( (charBuf.toString()).getBytes());
      int nBytes = socket.write(buffer);
      System.out.println("nBytes = " + nBytes);
      result = null;
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  public String readMessage(SelectionKey key) {
    System.out.println("SelectionKey = " + key);
    int nBytes = 0;
    System.out.println("key.channel() = " + key.channel());
    socket = (SocketChannel) key.channel();
    ByteBuffer buf = ByteBuffer.allocate(1024);
    try {
      System.out.println("socket.read");
      nBytes = socket.read(buf);
      System.out.println("flip");
      buf.flip();
      Charset charset = Charset.forName("us-ascii");
      CharsetDecoder decoder = charset.newDecoder();
      CharBuffer charBuffer = decoder.decode(buf);
      System.out.println("charBuffer");

      result = charBuffer.toString();
      System.out.println("Received :" + result);

    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  public static void main(String args[]) {
    NonBlockingServer nb = new NonBlockingServer();
    try {
      nb.startServer();
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit( -1);
    }

  }

}
