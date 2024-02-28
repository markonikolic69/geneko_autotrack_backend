package yu.co.certus.pos.lanus.concurrent;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.InputStream;
import java.io.OutputStream;

import java.io.IOException;


import yu.co.certus.pos.geneco.data.PostGreAgent;
import yu.co.certus.pos.geneco.data.UnitData;
import yu.co.certus.pos.geneco.protocol.ProtocolDecoder;
import yu.co.certus.pos.geneco.protocol.ProtocolDecoderFactory;
import yu.co.certus.pos.geneco.protocol.UnknownProtocolException;
import yu.co.certus.pos.geneco.protocol.message.GPSMessage;
import yu.co.certus.pos.lanus.util.ByteConverter;


import yu.co.certus.pos.lanus.service.Service;




public class ProcessingTaskGeneco implements Runnable{

    private Socket _socket = null;

    //  private boolean _send = true;

    //private TransactionData _tData = null;
    private String _vehicleId = "";
    
    private boolean _is_genecodev_direct_client = true;
    



    public ProcessingTaskGeneco(Socket socket) {
        _socket = socket;

    }
    
    public ProcessingTaskGeneco(Socket socket, boolean is_genecodev_direct_client) {
        _socket = socket;
        _is_genecodev_direct_client = is_genecodev_direct_client;
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
            _socket.setSoTimeout(300000);
            is = _socket.getInputStream();
            os = _socket.getOutputStream();

            // read data 
            ///////////////////////header
            byte[] buf_start_byte_1 = new byte[1];
            byte[] buf_start_byte_2 = new byte[1];
            byte[] duzina_poruke = new byte[1];

            //boolean budi_u_petlji = true;


            //while(budi_u_petlji) {

                Service.logger.debug(" reading from socket: ");
                is.read(buf_start_byte_1);
                Service.logger.debug("recived start byte 1 hex: " + ByteConverter.hexify(buf_start_byte_1));

                is.read(buf_start_byte_2);
                Service.logger.debug("recived start byte 2 hex: " + ByteConverter.hexify(buf_start_byte_2));

                is.read(duzina_poruke);
                Service.logger.debug("recived duzina poruke hex: " + ByteConverter.hexify(duzina_poruke));

                int duzina_poruke_int = (int)duzina_poruke[0];
                Service.logger.debug("duzina_poruke_int = " + duzina_poruke_int);




                if(duzina_poruke_int > 0) {

                    byte[] ostatak_poruke = new byte[duzina_poruke_int - 3];
                    is.read(ostatak_poruke);
                    
//////////////////////////////////////////////sto pre ocistiti socket i deblokirati klijenta/////////////////////////////                    
                    try{
                        if(is != null)
                            is.close();
                        if(os != null)
                            os.close();
                        if(_socket != null)
                            _socket.close();
                    }catch(Exception ex){
                        if (Service.logger.isDebugEnabled()) {
                            Service.logger.error(
                                    "Error when try to close input, output or connection, details: " + ex.getMessage(), ex);
                        }
                    }
                    if (Service.logger.isDebugEnabled()) {
                        Service.logger.info("<-- socket finished, all resources closed" );
                    }
/////////////////////////////////////////////kraj ciscenja socket-a///////////////////////////////////////////////////////////////                    
                    
                    
                    
                    Service.logger.info("poruja: " + ByteConverter.hexify(buf_start_byte_1) + ByteConverter.hexify(buf_start_byte_2) + 
                            ByteConverter.hexify(duzina_poruke) + ByteConverter.hexify(ostatak_poruke));

                    if(buf_start_byte_1[0] == 0x02 /*&& buf_start_byte_2[0] == 0x39*/){
                        Service.logger.info("recived check for command poruka, vracamo salji poziciju: " + ByteConverter.hexify(duzina_poruke));
//                        byte[] mnemonic_p_send_position = "P:".getBytes();//salji poziciju
//                        byte[] pocetak = {0x02};
//                        byte[] kraj = {0x03};
//
//                        os.write(pocetak);
//                        os.write(mnemonic_p_send_position);
//                        os.write(kraj);
//                        os.flush();
//                        Service.logger.debug("poslali nazad pocetak poruke hex: " + ByteConverter.hexify(pocetak));
//                        Service.logger.debug("poslali nazad poruku salji poziciju hex: " + ByteConverter.hexify(mnemonic_p_send_position));
//                        Service.logger.debug("poslali nazad kraj poruke hex: " + ByteConverter.hexify(kraj));
                        //continue;
                    }else{ 

                        ByteBuf poruka = Unpooled.wrappedBuffer(buf_start_byte_1, buf_start_byte_2, duzina_poruke, ostatak_poruke);
                        //ovde parsiramo
                        //..buf_start_byte_1.

                        ProtocolDecoder decoder = ProtocolDecoderFactory.createProtocolDecoder(poruka);

                        decoder.decodeToJSONString(poruka);

                        GPSMessage gps_message = decoder.getGPSMessage();
                        
                        

                        if (Service.logger.isDebugEnabled()) {
                            Service.logger.info(
                                    "PArsed Message, gps_message = " + gps_message);
                        }

                        PostGreAgent agent = null;
                        try{
                            agent = new PostGreAgent();
                            String geneko_unit_id = gps_message.getSerialNumber();
                            UnitData unit_data = agent.getSerialNumber(gps_message.getSerialNumber());
                            gps_message.setSerialNumber(unit_data.get_serialNumber());
                            if(unit_data.get_current_driver_id() != null){
                                gps_message.setDriver(unit_data.get_current_driver());
                                gps_message.setDriverId(unit_data.get_current_driver_id());
                            }
                            
                            if(!gps_message.getiButton().equals("")){
                                DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                //ovo mozda promeniti sa primljenim gps vremenom
                                String timestampString = gps_message.getLocalDateTime();//timestampFormatter.format(new java.util.Date());
                                agent.saveIButtonRequest(geneko_unit_id, gps_message.getiButton(), timestampString);
                            }else{
                                agent.saveMessage(gps_message, geneko_unit_id);
                            }
                        }catch(Throwable ex){
                            if (Service.logger.isDebugEnabled()) {
                                Service.logger.error(
                                        "Unable to insert gps data from vehicle in database, details: " + ex.getMessage(), ex);
                            }
                        }finally{
                            if(agent != null){
                                try{
                                    agent.close();
                                }catch(Exception e){
                                    if (Service.logger.isDebugEnabled()) {
                                        Service.logger.error(
                                                "Unable to close database connection, details: " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
//                        if(!_is_genecodev_direct_client){
//                            os.write("OK\n".getBytes());
//                            os.flush();
//                        }
                    }


                }else{
                    //duzina poruke 0
                    //ovo se pojavljuje iz meni nepoznatih razloga
                    //izaci iz petlje, nema slusanja, ubiti socket i oslboditi resurse
                    if (Service.logger.isDebugEnabled()) {
                        Service.logger.error(
                                "#####detektovana duzina poruke 0");
                    }
                    if(buf_start_byte_1[0] == 0x00){
                        //budi_u_petlji = false;
                        if (Service.logger.isDebugEnabled()) {
                            Service.logger.error(
                                    "#####Prvi bajt je 0x00, izlazi iz petlje, brise resurse");
                        }
                    }
                }


            //}//while endless

        }
        catch (UnknownProtocolException upe) {
            if (Service.logger.isDebugEnabled()) {
                Service.logger.error(
                        "UnknownProtocolException, connection closed, details: " + upe.getMessage(), upe);
            }

        }
        catch (IOException e) {
            if (e instanceof java.net.SocketTimeoutException &&
                    e.getMessage().contains("Read")) {
                if (Service.logger.isDebugEnabled()) {
                    Service.logger.info(
                    "Socket more then 30 seconds idle, connection closed");
                }
                //                try{
                //                    is.close();
                //                    os.close();
                //                    _socket.close();
                //                }catch(Exception ex){
                //                    if (Service.logger.isDebugEnabled()) {
                //                        Service.logger.error(
                //                                "Error when try to close input, output or connection, details: " + ex.getMessage(), ex);
                //                    }
                //                }
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
//            try{
//                if(is != null)
//                    is.close();
//                if(os != null)
//                    os.close();
//                if(_socket != null)
//                    _socket.close();
//            }catch(Exception ex){
//                if (Service.logger.isDebugEnabled()) {
//                    Service.logger.error(
//                            "Error when try to close input, output or connection, details: " + ex.getMessage(), ex);
//                }
//            }
//            if (Service.logger.isDebugEnabled()) {
//                Service.logger.info("<-- thread finished, all resources closed" );
//            }

        }
    }






    public static void main(String[] args){
        byte[] duzina_poruke_bytes = new byte[]{0x00, 0x00, 0x00, 0x48};

    }


}
