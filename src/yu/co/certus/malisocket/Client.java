package yu.co.certus.malisocket;


import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

import yu.co.certus.pos.lanus.util.HmacSha1Signature;

import java.io.*;
import java.net.*;

class Client extends JFrame
                 implements ActionListener {

   JLabel text, clicked;
   JButton button;
   JPanel panel;
   JTextField textField;
   Socket socket = null;
   PrintWriter out = null;
   BufferedReader in = null;
   BufferedWriter wr = null;
   OutputStream out_wr = null;

   Client(){ //Begin Constructor
     text = new JLabel("Text to send over socket:");
     textField = new JTextField(20);
     button = new JButton("Click Me");
     button.addActionListener(this);

     panel = new JPanel();
     panel.setLayout(new BorderLayout());
     panel.setBackground(Color.white);
     getContentPane().add(panel);
     panel.add("North", text);
     panel.add("Center", textField);
     panel.add("South", button);
   } //End Constructor

  public void actionPerformed(ActionEvent event){
     Object source = event.getSource();

     if(source == button){
//Send data over socket
          String text = textField.getText();
          text = text + "\n";
          try{
          wr.write(text);
          System.out.println("write text = " + text);
          wr.flush();
          System.out.println("flush text = " + text);
          }catch(IOException ioe){
              System.out.println("IOException when writing = " + ioe.getMessage());
          }
          //out.println(text);
          textField.setText(new String(""));
//Receive text from server
       try{
         String line;
         line = in.readLine();

           System.out.println("Text received :" + line);

       } catch (IOException e){
         System.out.println("Read failed");
         System.exit(1);
       }
     }
  }

  public void listenSocket(){
//Create socket connection
     try{
//         byte[] ipAddr = new byte[] { 93, 87, 76, 75 };
//       InetAddress addr = InetAddress.getByAddress(ipAddr);//Name(/*"127.0.0.1"*/"93.87.76.75");
       InetAddress addr = InetAddress.getByName(/*"127.0.0.1"*/"93.87.76.75");
       socket = new Socket(addr, 1521);
       //out = new PrintWriter(socket.getOutputStream(), true);
       try {
           out_wr = socket.getOutputStream();
           wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
           
       } catch (IOException e) {
       }

       in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       String value = new String("prsti prsti bela staza evo deda mraza".getBytes("UTF-8"));
       java.nio.charset.Charset UTF8_CHARSET = java.nio.charset.Charset.forName("UTF-8");
       byte[] value_bytes = value.getBytes(UTF8_CHARSET);
       byte[] header = {0x01, 0x31, 0x00, 0x00, 0x00, 0x4F, 0x46, 0x44, 0x43, 0x30, 
               0x30, 0x31, 0x12, 0x34, 0x56}; 
       byte[] duzina_response_poruke_bytes = java.nio.ByteBuffer.allocate(4).putInt(header.length + 4 +
               value_bytes.length + 35).array();
       
//               0x00, 0x00, 0x11, 0x22, //duzina poruke
//               0x31,0x31,0x31,0x31,0x31,0x31,0x31,0x31, 
               
               
       byte[] crc16 = {       0x11, 0x22};//crc4
       byte[] kraj = {        0x02};
       
       
       byte[] base32_potpis = new byte[32];
       org.apache.commons.codec.binary.Base32 base32 = new org.apache.commons.codec.binary.Base32();
       try{
       byte[] server_potpis = HmacSha1Signature.calculateRFC2104HMAC(value, "0648822005");
       base32_potpis = base32.encode(server_potpis);
       }catch(Exception ex){
           System.out.print("Exception when try to create server signature, details: " + ex.getMessage());
           ex.printStackTrace();
       }
       
       
       
       
       out_wr.write(header);
       out_wr.write(duzina_response_poruke_bytes);
       out_wr.write(value_bytes);
       out_wr.write(base32_potpis);
       out_wr.write(crc16);
       out_wr.write(kraj);
       out_wr.flush();
       System.out.println("write to output stream");
       out_wr.flush();
       System.out.println("flush");
       
       
     } catch (UnknownHostException e) {
       System.out.println("Unknown host: localhost");
       System.exit(1);
     } catch  (IOException e) {
       System.out.println("No I/O");
       System.exit(1);
     }
  }

   public static void main(String[] args){
        Client frame = new Client();
        frame.setTitle("Client Program");
        WindowListener l = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                        System.exit(0);
                }
        };

        frame.addWindowListener(l);
        frame.pack();
        frame.setVisible(true);
        frame.listenSocket();
  }
}
