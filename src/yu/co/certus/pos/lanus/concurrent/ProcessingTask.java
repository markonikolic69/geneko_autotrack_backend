package yu.co.certus.pos.lanus.concurrent;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import yu.co.certus.pos.lanus.util.ByteConverter;
import yu.co.certus.pos.lanus.util.CRC16;

import yu.co.certus.pos.lanus.message.*;

import yu.co.certus.pos.lanus.data.TransactionData;
import yu.co.certus.pos.lanus.data.BaseDBIface;
import yu.co.certus.pos.lanus.data.CancelDBIface;
import yu.co.certus.pos.lanus.data.DBFactory;

import yu.co.certus.pos.lanus.*;

import yu.co.certus.pos.lanus.service.Service;


import yu.co.certus.pos.lanus.IsConnectedChecker;



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
public class ProcessingTask
implements Runnable {

    private Socket _socket = null;

    //  private boolean _send = true;

    //private TransactionData _tData = null;
    private String _terminalId = "";


    private long _startTransaction = 0;

    public ProcessingTask(Socket socket) {
        _socket = socket;

    }

    public void run() {
        if (Service.logger.isDebugEnabled()) {
            Service.logger.info("--> thread started for socket = " +
                    _socket.toString());
            Service.logger.info("------------------------------------request start--------------------------------------------------");
            Service.logger.info("request from address: " + _socket.getInetAddress().getHostAddress() + " port: " + _socket.getPort());

        }
        InputStream is = null;
        OutputStream os = null;
        try {
            _socket.setSoTimeout(30000);
            is = _socket.getInputStream();
            os = _socket.getOutputStream();
        
        // read data 
///////////////////////header
        byte[] buf_start_byte = new byte[1];
        byte[] tip_poruke_byte = new byte[1];
        byte[] brojac1_byte = new byte[1];
        byte[] brojac2_byte = new byte[1];
        byte[] brojac3_byte = new byte[1];
        byte[] deo_term_id_bytes = new byte[7];
        byte[] drugi_deo_terminal_id_bytes = new byte[3];
        byte[] duzina_poruke_bytes = new byte[4];
///////////////////dodaci na header
        byte[] is_from_offline = new byte[1];
        byte[] broj_racuna = new byte[4];
        
//////////////////////end header
        byte[] telo_poruke_bytes ;
        byte[] client_signature_bytes = new byte[32];
        byte[] client_crc16_bytes = new byte[2];
        byte[] kraj_poruke_byte = new byte[1];
        int size= 0;
        byte startByte = 0x01;
        byte endByte = 0x02;
        byte is_offline = 0x01;
        byte fistOne = 0x00;
        int brojac = -1;
        int auditPackageBrojac = -1;
        int retryBrojac = -1;
        int tip_poruke = 0;
        boolean is_offline_mode = false;
        
        byte is_refund_1 = 0x02;
        byte is_refund_2 = 0x03;
        
        boolean _is_refund_audit_data = false;
        
//        for (int nChunk = is.read(buf); nChunk != /*-1*/endByte; nChunk = is.read(buf)) {
//            Service.logger.info("size = " + size);
//            size+=nChunk;
//            tagStream.write(buf,0,nChunk);
//            
//        }
//        byte[] recivedData = tagStream.toByteArray();
//        byte[] recivedData = buf;


        Service.logger.info("reading from socket: ");
        is.read(buf_start_byte);
        Service.logger.info("recived start byte hex: " + ByteConverter.hexify(buf_start_byte));
        Service.logger.info("is first byte correct = " + (buf_start_byte[0] == startByte));
 //       Service.logger.info("recived byte size: " + size);
//        Service.logger.info("recived : " + new String(recivedData));
        is.read(tip_poruke_byte);
        Service.logger.info("recived tip poruke byte hex: " + ByteConverter.hexify(tip_poruke_byte));
        tip_poruke = Integer.parseInt("" + (char)tip_poruke_byte[0]);
        Service.logger.info("tip poruke = " + tip_poruke);
        is.read(is_from_offline);
        is_offline_mode = is_from_offline[0] == is_offline;
        _is_refund_audit_data = is_from_offline[0] == is_refund_1 || is_from_offline[0] == is_refund_2;
        Service.logger.info("is offline mode = " + is_offline_mode);
        Service.logger.info("_is_refund_audit_data = " + _is_refund_audit_data);
        is.read(brojac1_byte);
        Service.logger.info("recived brojac1_byte hex: " + ByteConverter.hexify(brojac1_byte));
        brojac = brojac1_byte[0];
        Service.logger.info("brojac poruka = " + brojac);

        is.read(brojac2_byte);
        Service.logger.info("recived brojac2_byte hex: " + ByteConverter.hexify(brojac2_byte));
        auditPackageBrojac = brojac2_byte[0];
        Service.logger.info("auditPackageBrojac = " + auditPackageBrojac);

        is.read(brojac3_byte);
        Service.logger.info("recived brojac1_byte hex: " + ByteConverter.hexify(brojac3_byte));
        retryBrojac = brojac3_byte[0];
        Service.logger.info("retryBrojac = " + retryBrojac);
        
        is.read(deo_term_id_bytes);
        Service.logger.info("recived deo_term_id_bytes hex: " + ByteConverter.hexify(deo_term_id_bytes));
        String term_id = new String(deo_term_id_bytes);
        is.read(drugi_deo_terminal_id_bytes);
        int term_id_br1 = (int)((0xF0 & drugi_deo_terminal_id_bytes[0])>> 4);
        int term_id_br2 = (int)(0x0F & drugi_deo_terminal_id_bytes[0]);
        int term_id_br3 = (int)((0xF0 & drugi_deo_terminal_id_bytes[1])>> 4);
        int term_id_br4 = (int)(0x0F & drugi_deo_terminal_id_bytes[1]);
        int term_id_br5 = (int)((0xF0 & drugi_deo_terminal_id_bytes[2])>> 4);
        int term_id_br6 = (int)(0x0F & drugi_deo_terminal_id_bytes[2]);

        term_id = term_id+ term_id_br1 + term_id_br2 + term_id_br3 + term_id_br4 + term_id_br5 + term_id_br6;
        
        Service.logger.info("term_id =  " + term_id);
        
        is.read(broj_racuna);
        byte[] br_rac_obrnuto = new byte[]{broj_racuna[3], broj_racuna[2], broj_racuna[1], broj_racuna[0]};
        Service.logger.info("recived broj_racuna hex: " + ByteConverter.hexify(broj_racuna));
        int broj_racuna_int = java.nio.ByteBuffer.wrap(br_rac_obrnuto).getInt();
        Service.logger.info("broj_racuna = " + broj_racuna_int);
        is.read(duzina_poruke_bytes);
        
        Service.logger.info("recived duzina_poruke_bytes hex: " + ByteConverter.hexify(duzina_poruke_bytes));
        int duzina_poruke = java.nio.ByteBuffer.wrap(duzina_poruke_bytes).getInt();
        Service.logger.info("duzina_poruke = " + duzina_poruke);
        
        
        
        telo_poruke_bytes = new byte[duzina_poruke - 24/*header*/ - 35/*footer*/];
        is.read(telo_poruke_bytes);
        Service.logger.info("recived telo_poruke_bytes hex: " + ByteConverter.hexify(telo_poruke_bytes));
        java.nio.charset.Charset UTF8_CHARSET = java.nio.charset.Charset.forName("UTF-8");
        String poruka = new String(telo_poruke_bytes, UTF8_CHARSET);
        Service.logger.info("poruka = " + poruka);
        
        is.read(client_signature_bytes);
        Service.logger.info("recived client_signature_bytes hex: " + ByteConverter.hexify(client_signature_bytes));
        String signature_base32 = new String(client_signature_bytes);
        Service.logger.info("signature_base32 = " + signature_base32);
        org.apache.commons.codec.binary.Base32 base32 = new org.apache.commons.codec.binary.Base32();
        String signature = base32.encodeAsString(signature_base32.getBytes());
        Service.logger.info("signature = " + signature);
        
        is.read(client_crc16_bytes);
        Service.logger.info("recived client_crc16_bytes hex: " + ByteConverter.hexify(client_crc16_bytes));
        
        is.read(kraj_poruke_byte);
        Service.logger.info("recived kraj_poruke_byte hex: " + ByteConverter.hexify(kraj_poruke_byte));
        
        byte[] term_id_bytes = new byte[]{deo_term_id_bytes[0],deo_term_id_bytes[1], deo_term_id_bytes[2],
                deo_term_id_bytes[3], deo_term_id_bytes[4], deo_term_id_bytes[5], deo_term_id_bytes[6],
                drugi_deo_terminal_id_bytes[0], drugi_deo_terminal_id_bytes[1], drugi_deo_terminal_id_bytes[2]};

        FiscalPakistanAuditProcessor fpap = new FiscalPakistanAuditProcessor(term_id, signature,
                brojac, auditPackageBrojac, retryBrojac, "" + broj_racuna_int, broj_racuna , term_id_bytes, _is_refund_audit_data);
        
        fpap.process(poruka);
        

        
        //os.write(header);

        byte response_code = (byte)fpap.getResponseCode();
        byte nema_comande = 0x30;
        String response_telo = new String(new byte[]{response_code, nema_comande}, UTF8_CHARSET);
        byte[] resp_telo_byte = response_telo.getBytes(UTF8_CHARSET);
        int duzina_response_poruke = 15 + 4 + resp_telo_byte.length + 35;
        byte[] duzina_response_poruke_bytes = java.nio.ByteBuffer.allocate(4).putInt(duzina_response_poruke).array();
        

        Service.logger.info("write start byte hex: " + ByteConverter.hexify(buf_start_byte));
        os.write(buf_start_byte);
        Service.logger.info("write tip_poruke_byte hex: " + ByteConverter.hexify(tip_poruke_byte));
        os.write(tip_poruke_byte);
        Service.logger.info("write brojac1_byte hex: " + ByteConverter.hexify(brojac1_byte));
        os.write(brojac1_byte);
        Service.logger.info("write brojac1_byte hex: " + ByteConverter.hexify(brojac2_byte));
        os.write(brojac2_byte);
        Service.logger.info("write brojac3_byte hex: " + ByteConverter.hexify(brojac3_byte));
        os.write(brojac3_byte);
        Service.logger.info("write deo_term_id_bytes hex: " + ByteConverter.hexify(deo_term_id_bytes));
        os.write(deo_term_id_bytes);
        Service.logger.info("write drugi_deo_terminal_id_bytes hex: " + ByteConverter.hexify(drugi_deo_terminal_id_bytes));
        os.write(drugi_deo_terminal_id_bytes);
        Service.logger.info("write duzina_response_poruke_bytes hex: " + ByteConverter.hexify(duzina_response_poruke_bytes));
        os.write(duzina_response_poruke_bytes);
        Service.logger.info("kraj slanja header-a");
        /////////////kraj slanja header-a
        Service.logger.info("write resp_telo_byte hex: " + ByteConverter.hexify(resp_telo_byte));
        os.write(resp_telo_byte);
        Service.logger.info("kraj slanja tela poruke");
        //////////// kraj slanja body
        //serverski potpis
        byte[] base32_potpis = new byte[32];
        try{
        byte[] server_potpis = fpap.getServerSignature("0648822005", response_telo);
        Service.logger.info("server_potpis = " + server_potpis);
        base32_potpis = base32.encode(server_potpis);
        }catch(Exception ex){
            System.out.print("Exception when try to create server signature, details: " + ex.getMessage());
            ex.printStackTrace();
        }
        /////////pocetak slanja footer-a
        Service.logger.info("write base32_potpis hex: " + ByteConverter.hexify(base32_potpis));
        Service.logger.info("base32_potpis length = " + base32_potpis.length);
        os.write(base32_potpis);
        ////izracunati crc16
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( buf_start_byte );
        outputStream.write( tip_poruke_byte );
        outputStream.write( brojac1_byte );
        outputStream.write( brojac2_byte );
        outputStream.write( brojac3_byte );
        outputStream.write( deo_term_id_bytes );
        outputStream.write( drugi_deo_terminal_id_bytes );
        outputStream.write( duzina_response_poruke_bytes );
        outputStream.write( resp_telo_byte );
        outputStream.write( base32_potpis );

        byte c[] = outputStream.toByteArray( );
        

        byte[] calculated_crc = CRC16.getCRC16(c);
        Service.logger.info("write calculated_crc hex: " + ByteConverter.hexify(calculated_crc));
        os.write(calculated_crc);
        Service.logger.info("write endByte hex: " + ByteConverter.hexify(endByte));
        os.write(endByte);
        Service.logger.info("kraj slanja footer-a");
////////////////kraj slanja footer-a
        os.flush();
        Service.logger.info("kraj slanja klijentu");
//        BufferedReader in = null;
//        //PrintWriter out = null;
//        BufferedWriter out = null;

       
//            in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
//            //out = new PrintWriter(client.getOutputStream(), true);
//            out = new BufferedWriter(
//                    new OutputStreamWriter(_socket.getOutputStream(), "ASCII"));
            //60 secundi moze da stoji i da nista ne posalje
            



            //////////////////just for testing/////////////////////////////////////

            //      _socket.setSoLinger(true,15000);
            //      _socket.setKeepAlive(true);
            ////////////////////end just for testing///////////////////////////////
//            String line = null;
//
//            while (/*_send && */_socket.isConnected() && !_socket.isInputShutdown()) {
//                line = in.readLine();
//                System.out.println(line);
//                //cim primim prvu liniju ubijam idle state
//
//                if (line.startsWith(RequestParser.REQUEST_PREFIX)) {
//                    if (Service.logger.isDebugEnabled()) {
//                        Service.logger.info("--> from terminal = " + line);
//
//                    }
//                    AbstractRequest req = new RequestParser(line).getRequest();
//                    _terminalId = req.getUserId();
//                    if (req instanceof LoginRequest) {
//
//                        /*_send = */processLogin( (LoginRequest) req, out);
//                        //if (!_send) {
//
//                        // }
//                    }
//                    else {
//                        _startTransaction = System.currentTimeMillis();
//                        processTransaction(req, out, in);
//                        //_send = false;
//                    }
//
//                    //          if (Service.logger.isDebugEnabled()) {
//                    //              Service.logger.info("socket cleaner thread started ");
//                    //          }
//                    //          new Thread(new LoginResourceCleaner(_socket, in, out)).start();
//                }
//            }
        }
        catch (IOException e) {
            if (e instanceof java.net.SocketTimeoutException &&
                    e.getMessage().contains("Read")) {
                if (Service.logger.isDebugEnabled()) {
                    Service.logger.info(
                    "Socket more then 30 seconds idle, connection closed");
                }
                try{
                is.close();
                os.close();
                _socket.close();
                }catch(Exception ex){
                    if (Service.logger.isDebugEnabled()) {
                        Service.logger.error(
                        "Error when try to close input, output or connection, details: " + ex.getMessage(), ex);
                    }
                }
                /*
                 try{
          System.out.println("nisam valjda lud, hocu da ispisem " +
              LoginResourceCleaner.KILL);
          out.write(LoginResourceCleaner.KILL);
                 }catch(IOException ioe){
          if (Service.logger.isDebugEnabled()) {
            Service.logger.error(
                "IOException when try to write KILL " +
                ", details: " + e.getMessage(), e);
          }
                 }
                 */
            }
            else {
                if (Service.logger.isDebugEnabled()) {
                    if(e.getMessage().startsWith("Connection reset")){
                        Service.logger.info("Connection closed by client");
                    }else{
                        Service.logger.error(
                                "IOException when try to read or write the data to a socket" +
                                " connection, details: " + e.getMessage(), e);
                    }
                }
            }
        }finally{
            if (Service.logger.isDebugEnabled()) {
                Service.logger.info("<-- thread finished" );
            }

        }
    }

    private boolean processLogin(LoginRequest request, BufferedWriter
            out) {
        if (Service.logger.isDebugEnabled()) {
            Service.logger.debug("-->");

        }

        _terminalId = request.getUserId();
        if (Service.logger.isDebugEnabled()) {
            Service.logger.info("--> process terminal = " + _terminalId);
        }

        LoginResponse response = new LoginResponse();

        LoginProcessor lProc = null;
        try {
            lProc = new ProcessorFactory().getLoginProcessor(_terminalId);
            lProc.process(request, response);



        }
        catch (PosException pe) {
            if (Service.logger.isDebugEnabled()) {
                Service.logger.info("-->PosException when try to login, details: " +
                        pe.getMessage());
            }

            response.addResponseCode(response.SYSTEM_FAILURE_ERROR);
        }

        //return to client
        try {
            if (Service.logger.isDebugEnabled()) {
                Service.logger.info("--> login response = " + response.forPos());
            }

            out.write(response.forPos());
            out.flush();

        }
        catch (IOException ioe) {
            //doslo do prekida veze
            if (Service.logger.isDebugEnabled()) {
                Service.logger.info("-->Unable to send login response = " +
                        response.forPos() +
                        ", details: " + ioe.getMessage() +
                ", session is finished");
            }
            return false;
        }
        //do we want to continue
        if (Service.logger.isDebugEnabled()) {
            Service.logger.debug("<--");

        }

        return response.isSuccessful();
    }

    private void processTransaction(AbstractRequest req, BufferedWriter
            out, BufferedReader in) {
        if (Service.logger.isDebugEnabled()) {
            Service.logger.debug("--> req = " + req);
        }
        AbstractResponse response = getResponse(req);


        
        if (Service.logger.isDebugEnabled()) {
            Service.logger.info("--> processing transaction for request = " + req );
        }
        try {
            new ProcessorFactory().getTransactionProcessor(req,  _terminalId).
            process(req, response);
        }
        catch (PosException pe) {
            if (Service.logger.isDebugEnabled()) {
                Service.logger.error("PosException when try to process, details: " 
                        + pe.getMessage(), pe );
            }
            response.addResponseCode(getErrorResponseCode(req));
        }
        try {
            if (Service.logger.isDebugEnabled()) {
                Service.logger.info("--> transaction response = " + response.forPos());
                //Service.logger.info("HOST ADDRESS = " + _socket.getInetAddress().getHostAddress());
                //Service.logger.info("IS REACHABLE before = " + _socket.getInetAddress().isReachable(5000));
            }

            //      insertTransactionResponse(response.forPos());

            ///////////////////////for testing/////////////////////////////
            //      if(true){
            //if(_terminalId.equals("359592000254675")){
            //        try {
            //          System.out.println("sleep");
            //          Thread.sleep(20 * 1000);
            //        }
            //        catch (Throwable e) {}
            //      }
            /////////////////end for testing////////////////////
            ///////////////////////for testing/////////////////////////////
            //      System.out.println("Procitao drugu liniju , pre write = " + in.readLine());
            /////////////////end for testing////////////////////

            //ne pisemo ovde vec u cleaning threadu
            out.write(response.forPos());
            out.flush();

            /////////////////for testing////////////////////
            //      System.out.println("Procitao trecu liniju, posle write = " + in.readLine());
            /////////////////end for testing////////////////////

            //    if (Service.logger.isDebugEnabled()) {
            //Service.logger.info("IS REACHABLE after = " + _socket.getInetAddress().isReachable(5000));
            //      }

            ///////////////////when cleaning after top up///////////////////////
            //      _socket.close();
            //     new Thread(new TransactionResourceCleaner( _socket, in, out,
            //                                                 response, _terminalId, _tData, _startTransaction)).start();
            //        new Thread(new KillThread(out)).start();
            /////////////////end of just for testing//////////////////
        }
        catch (Throwable/*IOException*/ ioe) {
            if (Service.logger.isDebugEnabled()) {
                Service.logger.error(
                        "-->IOException when try to send data back in transaction, response is = "
                        + response.forPos() + ", details: " +
                        ioe.getMessage());
            }

            //doslo do prekida veze, nisam uspeo da posaljem natrag
            //ukoliko je zahtev za prepaid uradi automatski storno
            try {
                if (response instanceof PrepaidResponse) {
                    if ( ( (PrepaidResponse) response).isSuccessful()) {
                        CancelRequest cReq = new CancelRequest();
                        cReq.addTransactionId( ( (PrepaidResponse) response).
                                getTransactionId());
                        CancelDBIface dbIface = new DBFactory().getCancelDBIface();
//                        new CancelProcessor(dbIface,
//                                _terminalId, 
//                                ProcessorFactory.getTransactionData(dbIface, _terminalId) ,true).process(cReq,
//                                        new CancelResponse());
                    }
                }
                else {

                    if (response instanceof RetryResponse) {
                        if ( ( (RetryResponse) response).isPrepaidAndSuccessful()) {
                            CancelRequest cReq = new CancelRequest();
                            cReq.addTransactionId( ( (RetryResponse) response).
                                    getTransactionId());
                            CancelDBIface dbIface = new DBFactory().getCancelDBIface();
//                            new CancelProcessor(dbIface,
//                                    _terminalId, 
//                                    ProcessorFactory.getTransactionData(dbIface, _terminalId), true).process(cReq,
//                                            new CancelResponse());
                        }
                    }
                }
            }
            catch (Exception e) {
                if (Service.logger.isDebugEnabled()) {
                    Service.logger.error(
                            "-->Exception when try to rollback prepaid transaction, response was "
                            + response.forPos() + ", details: " +
                            e.getMessage());
                }
            }
        }
    }
    
    private AbstractResponse getResponse(AbstractRequest req){
        if (req instanceof PrepaidRequest) {
            return new PrepaidResponse();
        }
        if (req instanceof CancelRequest) {
            return new CancelResponse();
        }
        if (req instanceof SaveParamRequest) {
            return new SaveParamResponse();
        }
        if (req instanceof ReadParamRequest) {
            return new ReadParamResponse();
        }
        if (req instanceof RetryRequest) {
            return new RetryResponse();
        }
        if (req instanceof LastTopUpRequest) {
            return new PrepaidResponse();
        }
        if (req instanceof ReportRequest) {
            return new ReportResponse();
        }
        if (req instanceof AnnouncementRequest) {
            return new AnnouncementResponse();
        }
        if (Service.logger.isDebugEnabled()) {
            Service.logger.error("-->Unknown request = " +
                    req);
        }
        return null;
        
    }
    
    
    private String getErrorResponseCode(AbstractRequest req){
        if (req instanceof PrepaidRequest) {
            return PrepaidResponse.SYSTEM_FAILURE_ERROR;
        }
        if (req instanceof CancelRequest) {
            return CancelResponse.SYSTEM_FAILURE_ERROR;
        }
        if (req instanceof AnnouncementRequest) {
            return AnnouncementResponse.NO_ANNOUNCEMENT_ERROR;
        }
        if (req instanceof ReportRequest) {
            return ReportResponse.UNKNOWN_ERROR;
        }
        if (req instanceof RetryRequest) {
            return RetryResponse.SYSTEM_FAILURE_ERROR;
        }
        if (Service.logger.isDebugEnabled()) {
            Service.logger.error("Request system error is not supported");
        }
        return ReportResponse.UNKNOWN_ERROR;
    }




    //  private class KillThread
    //      implements Runnable {
    //
    //    private int _sleepInSekInKill = 20;
    //
    //    private BufferedWriter _outForKill = null;
    //
    //    public KillThread(BufferedWriter out){
    //      _outForKill = out;
    //    }
    //
    //    public void run() {
    //      waitInSec(_sleepInSekInKill);
    //      if (Service.logger.isDebugEnabled()) {
    //        Service.logger.info("Try to sen kill after " + _sleepInSekInKill + " sec");
    //
    //      }
    //      try {
    //        _outForKill.write(KILL);
    //        _outForKill.flush();
    //      }
    //      catch (IOException ioe) {
    //        if (Service.logger.isDebugEnabled()) {
    //          Service.logger.error("--> Unable to write message " + KILL +
    //                               " details: " + ioe.getMessage());
    //        }
    //      }
    //    }
    //  }
    //
    //  public void waitInSec(int timeInSec) {
    //    try {
    //      Thread.sleep(timeInSec * 1000);
    //    }
    //    catch (InterruptedException ie) {
    //      if (Service.logger.isDebugEnabled()) {
    //        Service.logger.error("Sleep in clean for " + timeInSec +
    //                             " sec interrupted ");
    //
    //      }
    //    }
    //  }
    //
    //  //public static final String KILL = "kill";


public static void main(String[] args){
    byte[] duzina_poruke_bytes = new byte[]{0x00, 0x00, 0x00, 0x48};
    int duzina_poruke = java.nio.ByteBuffer.wrap(duzina_poruke_bytes).getInt();
    
    System.out.println("duzina poruke = " + duzina_poruke);
    
    
    byte[] broj_racuna = new byte[]{0x00, 0x00, 0x00, 0x05};
    
    int broj_racuna_int = java.nio.ByteBuffer.wrap(broj_racuna).getInt();
    System.out.println("broj_racuna_int = " + broj_racuna_int);
    
    
    byte[] sha1 = {0x39, (byte)0xf7, 0x2d, 0x14, 0x2f, 0x53, 0x58, 0x6c, 0x57, 0x37, 
            (byte)0xce, 0x54, (byte)0xe1, 0x1a, (byte)0xb3, (byte)0xf2, (byte)0xf5, 0x04, (byte)0xa4, 0x31
    };

org.apache.commons.codec.binary.Base32 base32 = new org.apache.commons.codec.binary.Base32();
byte[] b32 = base32.encode(sha1);
//HH3S2FBPKNMGYVZXZZKOCGVT6L2QJJBR
//HH3S2FBPKNMGYVZXZZKOCGVT6L2QJJBR
System.out.println("b32 length = " + b32.length);
System.out.println(new String( b32));
    
    byte[] buf_start_byte = {0x31};
    int tip_poruke = Integer.parseInt("" + (char)buf_start_byte[0]);
    
    System.out.println("tip_poruke = " + tip_poruke);
    
    
    int jedan_ascii = (int)0xff;
    
    //System.out.println(Integer.parseInt("" + (char)jedan_ascii));
    System.out.println((int)jedan_ascii);
    
    byte[] term_id = {0x12, 0x34, 0x56};
    int term_id_2 = (int)(0x0F & term_id[0]);
    int term_id_1 = (int)((0xF0 & term_id[0])>> 4);
    int term_id_4 = (int)(0x0F & term_id[1]);
    int term_id_3 = (int)((0xF0 & term_id[1])>> 4);
    int term_id_6 = (int)(0x0F & term_id[2]);
    int term_id_5 = (int)((0xF0 & term_id[2])>> 4);
    
    System.out.println("" + term_id_1 + term_id_2 + term_id_3 + term_id_4 + term_id_5 + term_id_6);
    byte[] duz_por = {(byte)0x01, (byte)0x00, 0x00, 0x00};
    duzina_poruke = java.nio.ByteBuffer.wrap(duz_por).getInt();
    System.out.println("duzina poruke = " + duzina_poruke);
}


}
