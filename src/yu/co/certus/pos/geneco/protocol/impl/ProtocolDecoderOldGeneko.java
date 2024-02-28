package yu.co.certus.pos.geneco.protocol.impl;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;


import org.json.simple.JSONObject;

import io.netty.buffer.ByteBuf;

import yu.co.certus.pos.geneco.protocol.DeviceProperty;
import yu.co.certus.pos.geneco.protocol.ProtocolDecoder;
import yu.co.certus.pos.geneco.protocol.Protocols;
import yu.co.certus.pos.geneco.protocol.message.GPSMessage;
import yu.co.certus.pos.lanus.service.Service;

public class ProtocolDecoderOldGeneko implements ProtocolDecoder {
 
    final String protocol = Protocols.OLD_GENEKO;
    
    private Map<String, String> message=null;
    
    @Override
    public GPSMessage getGPSMessage(){
        return new GPSMessage(message);
    }
    
    
    @Override
    public  String getProtocol() {
        // TODO Auto-generated method stub
        return protocol;
    }

    
    @Override
    public String getID(ByteBuf in) {
        try {
            if ( in.getUnsignedByte(in.readerIndex()) == 0x5C) {
                
    //          byte b[]=new byte[4];
    //          b[0]=(byte) in.getUnsignedByte(in.readerIndex()+BIN_V_ID);
    //          b[1]=(byte) in.getUnsignedByte(in.readerIndex()+BIN_V_ID+1);
    //          b[2]=(byte) in.getUnsignedByte(in.readerIndex()+BIN_V_ID+2);
    //          b[3]=(byte) in.getUnsignedByte(in.readerIndex()+BIN_V_ID+3);
    //          
    //          int tmp = ByteBuffer.wrap(b).getInt(0);
                Service.logger.debug("0x5C size: "+in.readableBytes());
                return String.valueOf(in.getInt(in.readerIndex() +BIN_V_ID));
                
            } else if (in.getUnsignedByte(in.readerIndex()) == 0x2) {
                
                int size=in.readableBytes();
    
                for(int i=2;i<size;i++)
                {
                    if(in.getUnsignedByte(in.readerIndex()+i) == 0x3)
                    {
                        byte[] by= new byte[i+1];
                        Service.logger.info("0x3 size:"+size);
                        in.getBytes(0, by, in.readerIndex(), i+1);
    
                        return getIdFromCommand(by);
                    }
                }
                
            //  String ss =String.valueOf((in.readerIndex() +BIN_V_ID)) 
                
                    
            }
        }catch(IndexOutOfBoundsException x) {
            Service.logger.error("get ID IndexOutOfBoundsException");
        }
        
        return null;
    }
    
    


    @Override
    public String decodeToJSONString(ByteBuf in) {
//        OldListenerHandler.EVENT event = getEvent(in);
        int n = in.readableBytes();
        byte array[] = new byte[n];
        in.readBytes(array);
        
        
        
//        if(event == OldListenerHandler.EVENT.SEND)
//        {
            message = parseMessage(array);
//        }
//        else if(event == OldListenerHandler.EVENT.COMMAND)
//        {
//            message = new LinkedHashMap<String,String> ();
//            message.put(DeviceProperty.VEHICLE_ID,getIdFromCommand(array));
//        }
        
        JSONObject obj = new JSONObject();
        for (Map.Entry<String, String> entry : message.entrySet()) {
            obj.put(entry.getKey(), entry.getValue());
        }
        
        return obj.toJSONString();
    }
    
      public static String getIdFromCommand(byte[] b)
      {
         String ret = null;
         try {
            ret = new String(Arrays.copyOfRange(b, 1,b.length-1), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         return ret;
      }
      
      public static Map<String, String> parseMessage(byte[] b){

          
        Map <String,String> message = new LinkedHashMap<String,String> ();
        int msLen = (int)b[BIN_LEN] & 0xff;
        int cellLen = 0;
        int tmp,tmp2;
        int idStatus;
          
        tmp = ByteBuffer.wrap(b).getInt(BIN_V_ID);
        message.put(DeviceProperty.VEHICLE_ID, Integer.toString(tmp));

        idStatus = ByteBuffer.wrap(b).getShort(BIN_MS_ID);
        message.put(DeviceProperty.ID_STATUS, Integer.toString(idStatus));

        if ((b[BIN_YEAR] & 0x80) == 0)
             message.put(DeviceProperty.VALID_POSITION, "A"); //Good position
        else
            message.put(DeviceProperty.VALID_POSITION, "V");
     
        
        message.put(DeviceProperty.DAY,  Integer.toString(b[BIN_DAY]));
        message.put(DeviceProperty.MONTH,  Integer.toString(b[BIN_MONTH]));


        tmp = b[BIN_YEAR] & 0x7f;
        tmp += 2000;
        message.put(DeviceProperty.YEAR,  Integer.toString(tmp));
        message.put(DeviceProperty.HOUR,  Integer.toString(b[BIN_HOUR]));
        message.put(DeviceProperty.MINUTE,  Integer.toString(b[BIN_MINUTE]));
        message.put(DeviceProperty.SECOND,  Integer.toString(b[BIN_SECUND]));
        String inputString = tmp + "-" + b[BIN_MONTH] + "-" + b[BIN_DAY] + " " + b[BIN_HOUR]
                + ":" + b[BIN_MINUTE] + ":" + b[BIN_SECUND];
        
        Long vreme=0L;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = sdf.parse(inputString);

                 vreme = date.getTime();
            } catch (Exception e) {
            }

        message.put(DeviceProperty.TIMESTAMP, Long.toString(vreme));
        
        
        tmp2 = (b[BIN_LAT] & 0x7f) * 256 + ((int)b[BIN_LAT + 1] & 0xff);
        message.put(DeviceProperty.LAT_SIROVI, Integer.toString(tmp2));
        tmp = tmp2 / 60; //stepeni
        tmp2 = tmp2 % 60; //minuti


        double latCeoDeo = (double)tmp;
        tmp = (b[BIN_LAT_DEC]) * 256 + ((int)b[BIN_LAT_DEC + 1] & 0xff);
        double tmp2Doub = (double)tmp2 + ((double)tmp) / 10000;
        double doubLat = latCeoDeo + tmp2Doub / 60;
        message.put(DeviceProperty.LAT, Double.toString(doubLat));
        if ((b[BIN_LAT] & 0x80) == 0)
            message.put(DeviceProperty.NORTHING, "N");
        else
            message.put(DeviceProperty.NORTHING, "S");
        

        tmp2 = (b[BIN_LONG] & 0x7f) * 256 + ((int)b[BIN_LONG + 1] & 0xff);
        message.put(DeviceProperty.LON_SIROVI, Integer.toString(tmp2));
        tmp = tmp2 / 60; //stepeni
        tmp2 = tmp2 % 60; //minuti
        double longCeoDeo = (double)(tmp);
        tmp = (b[BIN_LONG_DEC]) * 256 + ((int)b[BIN_LONG_DEC + 1] & 0xff);
        tmp2Doub = (double)tmp2 + ((double)tmp) / 10000;
        double doubLong = longCeoDeo + tmp2Doub / 60;
        message.put(DeviceProperty.LON, Double.toString(doubLong));
        if ((b[BIN_LONG] & 0x80) == 0)
            message.put(DeviceProperty.EASTING, "E");
        else
            message.put(DeviceProperty.EASTING, "W");    
     
        tmp =  ByteBuffer.wrap(b).getShort(BIN_SPEED);
        message.put(DeviceProperty.SPEED, Integer.toString(tmp));

        tmp =  ByteBuffer.wrap(b).getShort(BIN_DIR);
        if (tmp < 0) tmp += 360;
        message.put(DeviceProperty.DIRECTION, Integer.toString(tmp));

        cellLen = ((int)b[BIN_RESERVED]) & 0xff;
        //System.out.println("cellLen: " + cellLen);
        tmp = 1;
        StringBuilder baznaStanica = new StringBuilder();
        
        for(int i = 1; i<=cellLen;i++)
        {
            //System.out.print(" "+ (b[BIN_RESERVED + i]&0xff));
            
            
            baznaStanica.append((char)b[BIN_RESERVED + i]);
        }
        //long ex_time = ((b[BIN_RESERVED+1]&0xff)<<0) + ((b[BIN_RESERVED+2]&0xff)<<8) + ((b[BIN_RESERVED+3]&0xff)<<16) + ((b[BIN_RESERVED+4]&0xff)<<24);
        //System.out.println(" time_t: " +  ex_time );
        //System.out.println("gps_flags_t: " + b[BIN_RESERVED+5] );
        if(cellLen>15)
        {
            message.put(DeviceProperty.GPS_FLAGS, ""+((int)b[BIN_RESERVED+5]) );
            
            //System.out.println("satellites: " + b[BIN_RESERVED+6] );
            message.put(DeviceProperty.SATELLITES, ""+((int)b[BIN_RESERVED+6]) );
            
            //System.out.println("hdop: "     +    ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(BIN_RESERVED+7));
            message.put(DeviceProperty.HDOP, ""+ ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(BIN_RESERVED+7));
            
            //System.out.println("altitude: "  +  ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(BIN_RESERVED+11));
            message.put(DeviceProperty.ALT, ""+ ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(BIN_RESERVED+11));
            
            //System.out.println("gsm_signal: " +  b[BIN_RESERVED+15]);
            message.put(DeviceProperty.GSM_SIGNAL, ""+((int)b[BIN_RESERVED+15]));
            
            //System.out.println("voltage_internal_battery: " +  ((b[BIN_RESERVED+17]&0x7f)*256+((int)b[BIN_RESERVED+16]&0xff)));//treba inverzno
            //System.out.println("voltage_external: " + ((b[BIN_RESERVED+19]&0x7f)*256+((int)b[BIN_RESERVED+18]&0xff)));//treba inverzno
        }
        else
        {
            message.put(DeviceProperty.CELL_INFO, baznaStanica.toString());
            // message.put(DeviceProperty.CELL_INFO,""); //external data using field of CELL_INFO
        }

        //status
        int statusLen = ((int)b[BIN_RESERVED + 1 + cellLen]) & 0xff;
        if (statusLen == 16)
        { //primarni format statusa je dugacak 16 bajtova, inace ignorisi
            StringBuilder tmpStr = new StringBuilder();
            byte tmpB = b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_INL]; //INPUT BITI L
            for (int i = 0; i < 8; i++)
            {
                //tmpStr = tmpStr + ((tmpB >> i) & 0x01);
                tmpStr.append(((tmpB >> i) & 0x01));
            }
            tmpB = b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_INH]; //INPUT BITI H
            for (int i = 0; i < 8; i++)
            {
                //tmpStr = tmpStr + ((tmpB >> i) & 0x01);
                tmpStr.append(((tmpB >> i) & 0x01));
            }
            //Console.WriteLine("Input: " + tmpStr);
           // pozicijaVozila.input = tmpStr;
            message.put(DeviceProperty.INPUT, tmpStr.toString());
           // tmpStr = tmpStr + " ";
            tmp = ((int)b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_VEXT] & 0xff) * 256;
            tmp = tmp + ((int)b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_VEXT + 1] & 0xff);
           // tmpStr = tmpStr + tmp + " "; //externo napajanje
            //Console.WriteLine("Main power: "+tmp);
            //pozicijaVozila.mainPower = tmp;
            message.put(DeviceProperty.MAIN_POWER, Integer.toString(tmp));
            tmp = ((int)b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_USER] & 0xff) * 256;
            tmp = tmp + ((int)b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_USER + 1] & 0xff);
            //tmpStr = tmpStr + tmp + " "; //korisnicki AD ulaz
            //pozicijaVozila.backUpPower = tmp;
            message.put(DeviceProperty.BACKUP_POWER, Integer.toString(tmp));
            //tmpStr=tmpStr+"0 "; //korisnicki AD ulaz
            tmp = ((int)b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_RPM] & 0xff) * 256;
            tmp = tmp + ((int)b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_RPM + 1] & 0xff);
           // tmpStr = tmpStr + tmp + " "; //RMP
            //pozicijaVozila.roundPerMinute = tmp;
            message.put(DeviceProperty.RPM, Integer.toString(tmp));
            tmp = ((int)b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_FUEL] & 0xff);
            //pozicijaVozila.gasLevel = tmp;
            message.put(DeviceProperty.GAS_LEVEL, Integer.toString(tmp));
           // tmpStr = tmpStr + tmp + " "; //FUEL
           // tmpStr = tmpStr + "0 0 "; //shock senzor x i y ne prenose se vise
           // pozicijaVozila.xAxis = 0;
           // pozicijaVozila.yAxis = 0;
            tmpB = b[BIN_RESERVED + 1 + cellLen + 1 + BIN_S_OUT]; //OUTPUT BITI
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < 8; i++)
                output.append(((tmpB >> i) & 0x01));
            //pozicijaVozila.output = output;
            message.put(DeviceProperty.OUTPUT, output.toString());
        }
        
        int eventDataLen = ((int)b[BIN_RESERVED + 1 + cellLen + 1 + statusLen]) & 0xff;
        if (eventDataLen > 0)
        {
            int eventDataFirstIndex = BIN_RESERVED + 1 + cellLen + 1 + statusLen + 1;
            
            switch(idStatus)
            {
              case STATUS_REGULAR_GPS_DATA:
                  StringBuilder tmpStr2 = new StringBuilder();
                  if (eventDataLen >= 4)
                  {
                      tmp = ((int)b[eventDataFirstIndex + 0] & 0xff) * 16777216;
                      tmp = tmp + ((int)b[eventDataFirstIndex + 1] & 0xff) * 65536;
                      tmp = tmp + ((int)b[eventDataFirstIndex + 2] & 0xff) * 256;
                      tmp = tmp + ((int)b[eventDataFirstIndex + 3] & 0xff);
                      //tmpStr2 = tmp.ToString();
                      tmpStr2.append(tmp);
                  }
                  if (eventDataLen >= 6)
                  {
                      //tmpStr2 += " ";
                      tmpStr2.append(" ");
                      tmp = (int)(b[eventDataFirstIndex + 4] & 0xff);
                      //tmpStr2 += tmp.ToString();
                      tmpStr2.append(tmp);
                      tmp = (int)(b[eventDataFirstIndex + 5] & 0xff);
                      tmpStr2.append("." + tmp);
                      //tmpStr2 += "." + tmp.ToString();
                  }
                  message.put(DeviceProperty.EVENT_DATA, tmpStr2.toString());
                  //pozicijaVozila.eventData = tmpStr2;
                  break;
              case STATUS_RPM_OVER_LIMIT_EVENT: 
              case STATUS_EXTERNAL_POWER_SUPPLY_BELLOW_LIMIT_EVENT: 
              case STATUS_AUXILIAR_POWER_SUPPLY_BELLOW_LIMIT_EVENT:
                  tmp = ((int)b[eventDataFirstIndex] & 0xff) * 256;
                  tmp = tmp + ((int)b[eventDataFirstIndex + 1] & 0xff);
                  //pozicijaVozila.eventData = tmp.ToString();
                  message.put(DeviceProperty.EVENT_DATA, Integer.toString(tmp));
                  break;
              case STATUS_DALLAS_KEY_EVENT:
              case STATUS_IBUTTON_LOGIN_MESSAGE:
              case STATUS_IBUTTON_LOGOUT_MESSAGE:
                  StringBuilder tmpStr = new StringBuilder();
                  for (int i = eventDataFirstIndex; i < msLen - 1; i++)
                  {
                      int tmpInt2 = ((int)b[i] & 0xff);
                      tmpStr.append(String.format("%02X", tmpInt2));
                  }
                  //pozicijaVozila.eventData = tmpStr;
                  message.put(DeviceProperty.EVENT_DATA, tmpStr.toString());
                  break;
              case STATUS_FUEL_DATA_PACKET:
              case STATUS_FUEL_DATA_PACKET_FIRST_FUEL_SENSOR:
              case STATUS_FUEL_DATA_PACKET_SECOND_FUEL_SENSOR:
                  StringBuilder tmpIbutton = new StringBuilder();
                  for (int i = 0; i < 32; i++)
                  {
                      tmp = ((int)b[eventDataFirstIndex + i * 2] & 0xff) * 256;
                      tmp = tmp + ((int)b[eventDataFirstIndex + i * 2 + 1] & 0xff);
                      tmpIbutton.append(tmp);
                      if (i < 31)
                          tmpIbutton.append(" ");
                  }
                  message.put(DeviceProperty.EVENT_DATA, tmpIbutton.toString());
                  break;
              case STATUS_FUEL_FLOW_DATA_PACKET:
                  StringBuffer tmpFuel = new StringBuffer();
                  for (int i = 0; i < 20; i++)
                  {
                      tmp = ((int)b[eventDataFirstIndex + i] & 0xff);
                      tmpFuel.append(tmp);
                      if (i < 19)
                        tmpFuel.append(" ");
                  }
                  message.put(DeviceProperty.EVENT_DATA, tmpFuel.toString());
                  break;
              case STATUS_GREEN_DRIVE_PACKET:
                  if (eventDataLen > 0 && eventDataLen % 8 == 0)
                  {
                      StringBuilder tmpCdrive = new StringBuilder();
                      for (int i = 0; i < eventDataLen; i += 2)
                      {
                          tmp = ((int)b[eventDataFirstIndex + i] & 0xff) * 256;
                          tmp = tmp + ((int)b[eventDataFirstIndex + i + 1] & 0xff);
                          // ako nije RPM polje onda je broj u komplementu dvojke
                          if (i % 8 != 6 && tmp > 32767) tmp -= 65536;
                          tmpCdrive.append((i > 0 ? " " : "") + tmp);
                      }
                      message.put(DeviceProperty.EVENT_DATA, tmpCdrive.toString());
                  }
                  break;
              case STATUS_CANGO_DATA:
                  
                  if (eventDataLen == 1)
                  {
                      tmp = ((int)b[eventDataFirstIndex] & 0xff);
                      message.put(DeviceProperty.EVENT_DATA, Integer.toString(tmp));

                      if (tmp == 33)
                      {
                          int canGoDataLen = (int)(b[eventDataFirstIndex + eventDataLen]);

                          tmp = 0;
                          byte[] canGoData = new byte[canGoDataLen];
                          while (tmp < canGoDataLen)
                          {
                              canGoData[tmp] = b[eventDataFirstIndex + eventDataLen + 1 + tmp];
                              tmp++;
                          }
                          //pozicijaVozila.CAN_Data = AvlServer.CANBus.convertToCANfileds(canGoData);
                         // message.put(DeviceProperty.can, tmp);
                          parseCanFileds(message,canGoData);
     
                      }
                  }
                  break;
                  
              case STATUS_RFID_LOGIN:
              case STATUS_RFID_LOGOUT:
                  StringBuilder rfid = new StringBuilder();
                  for (int i = eventDataFirstIndex; i < eventDataFirstIndex + b[eventDataFirstIndex-1]; i++)
                  {
                      int tmpInt2 = ((int)b[i] & 0xff);
                      rfid.append(String.format("%02X", tmpInt2));
                  }
                  message.put(DeviceProperty.EVENT_DATA, rfid.toString());

                  break;
              case STATUS_FW_UPDATE:
                  tmp = ((int)b[eventDataFirstIndex] & 0xff);
                  //pozicijaVozila.eventData = tmp.ToString();
                  message.put(DeviceProperty.EVENT_DATA, Integer.toString(tmp));
                  break;
              case STATUS_RESPONSE_ON_PARAM_CHANGE:
              case STATUS_DELETE_LOG_FILE_RESPONSE:
              case STATUS_IBUTTON_REMOTE_CHANGE_RESPONSE:
              case STATUS_NET_LIST_CHANGE_RESPONSE:
              case STATUS_RESPONSE_ON_PARAMETER_READ_COMMAND:
                  tmp = 0;
                  StringBuilder evData1 = new StringBuilder();
                  while (tmp < eventDataLen)
                  {
                      //xmlData.Append((char)b[BIN_RESERVED + 1 + cellLen + 1 + statusLen + 1 + tmp]);
                      //evData += (char)b[eventDataFirstIndex + tmp];
                      evData1.append((char)b[eventDataFirstIndex + tmp]);
                      tmp++;
                  }
                  //pozicijaVozila.eventData = evData;
                  message.put(DeviceProperty.EVENT_DATA, evData1.toString());
                  break;
              case STATUS_RESPONSE_CONTACT_KEY_OFF:
              case STATUS_RESPONSE_CONTACT_KEY_ON:
                  //budzevina
                  StringBuilder evData2 = new StringBuilder();
                  boolean isBinary = false;
                  for (int i = 0; i < eventDataLen; i++)
                  {
                      byte tmpB = b[eventDataFirstIndex + i];
                      if (tmpB < 40 || tmpB > 126)
                      {
                          isBinary = true;
                          break;
                      }
                      //evData += (char)tmpB;
                      evData2.append((char)tmpB);
                  }

                  if (isBinary && (eventDataLen == 4 || eventDataLen == 6))
                  {
                      long tmp3;
                      tmp3 = ((int)b[eventDataFirstIndex + 0] & 0xff) * 16777216;
                      tmp3 = tmp3 + ((int)b[eventDataFirstIndex + 1] & 0xff) * 65536;
                      tmp3 = tmp3 + ((int)b[eventDataFirstIndex + 2] & 0xff) * 256;
                      tmp3 = tmp3 + ((int)b[eventDataFirstIndex + 3] & 0xff);
                      evData2 = new StringBuilder();
                     // evData = tmp.ToString();
                      evData2.append(tmp3);

                      if (eventDataLen == 6)
                      {
                         // evData += " ";
                          evData2.append(" ");
                          tmp = (int)(b[eventDataFirstIndex + 4] & 0xff);
                          //evData += tmp.ToString();
                          evData2.append(tmp);
                          tmp = (int)(b[eventDataFirstIndex + 5] & 0xff);
                          //evData += "." + tmp.ToString();
                          evData2.append("."+tmp);
                      }
                  }
                  //pozicijaVozila.eventData = evData;
                  message.put(DeviceProperty.EVENT_DATA, evData2.toString());
                  break;
              case STATUS_RESPONSE_ON_REMOTE_IBUTTON_CHANGE:
              case STATUS_LIST_OF_NEWLY_ADDED_IBUTTON_UIDS:
              case STATUS_LIST_OF_DELETED_IBUTTON_UIDS:
                  tmp = 0;
                  StringBuilder iBut = new StringBuilder();
                  int idBanke = b[eventDataFirstIndex + tmp] & 0xff;
                  tmp++;
                  int brojButtona = b[eventDataFirstIndex + tmp] & 0xff;
                  tmp++;
                  while (tmp < eventDataLen)
                  {
                      int tmpInt3 = (b[eventDataFirstIndex + tmp] & 0xff);
                      //String iBtnHexByte = tmpInt3.ToString("X").PadLeft(2, '0');
                      //iBut += iBtnHexByte;
                      iBut.append(String.format("%02X", tmpInt3));
                      tmp++;
                  }
                 // pozicijaVozila.eventData = idBanke + "*" + brojButtona + ":" + iBut;
                  message.put(DeviceProperty.EVENT_DATA, iBut.toString());
                  break;
              case STATUS_GEO_FENCE_IN_EVENT:
              case STATUS_GEO_FENCE_OUT_EVENT:
                  //string tmpStr2 = "";
                  tmp = ((int)b[eventDataFirstIndex + 0] & 0xff) * 16777216;
                  tmp = tmp + ((int)b[eventDataFirstIndex + 1] & 0xff) * 65536;
                  tmp = tmp + ((int)b[eventDataFirstIndex + 2] & 0xff) * 256;
                  tmp = tmp + ((int)b[eventDataFirstIndex + 3] & 0xff);
                  //tmpStr2 = tmp.ToString();
                  //pozicijaVozila.eventData = tmpStr2;
                  message.put(DeviceProperty.EVENT_DATA, Integer.toString(tmp));
                  break;
              case STATUS_RESPONSE_ON_GEO_FENCE_INSERTION:
              case STATUS_RESPONSE_ON_GE_FENCE_DELETION:
                  StringBuffer tmpStr3 = new StringBuffer();
                  tmp = ((int)b[eventDataFirstIndex + 0] & 0xff) * 16777216;
                  tmp = tmp + ((int)b[eventDataFirstIndex + 1] & 0xff) * 65536;
                  tmp = tmp + ((int)b[eventDataFirstIndex + 2] & 0xff) * 256;
                  tmp = tmp + ((int)b[eventDataFirstIndex + 3] & 0xff);
                  //tmpStr2 = tmp.ToString() + ",";
                  tmpStr3.append(tmp+",");

                  int kodGreske = ((int)b[eventDataFirstIndex + 4] & 0xff);
                  if (kodGreske > 127)
                  {
                      kodGreske = kodGreske - 256;
                  }
                  //tmpStr2 += kodGreske;
                  tmpStr3.append(kodGreske);
                  //pozicijaVozila.eventData = tmpStr2;
                  message.put(DeviceProperty.EVENT_DATA, tmpStr3.toString());
                  break;
              case STATUS_RESPONSE_ON_GE_FENCE_READ:
                  StringBuilder tmpStr4 = new StringBuilder();
                  int brojZona = ((int)b[eventDataFirstIndex + 0] & 0xff);
                  //tmpStr2 = brojZona + ":";
                  tmpStr4.append(brojZona + ":");
                  for (int i = 1; i <= brojZona * 4; i += 4)
                  {
                      tmp = ((int)b[eventDataFirstIndex + 0 + i] & 0xff) * 16777216;
                      tmp = tmp + ((int)b[eventDataFirstIndex + 1 + i] & 0xff) * 65536;
                      tmp = tmp + ((int)b[eventDataFirstIndex + 2 + i] & 0xff) * 256;
                      tmp = tmp + ((int)b[eventDataFirstIndex + 3 + i] & 0xff);
                      //tmpStr2 += tmp + ",";
                      tmpStr4.append(tmp);
                  } 
                  //pozicijaVozila.eventData = tmpStr2.Remove(tmpStr2.Length - 1);  )
                  message.put(DeviceProperty.EVENT_DATA, tmpStr4.substring(0,tmpStr4.length()-1));
                  break;
              case STATUS_RESPONSE_ON_REMOTE_PHONEBOOKREAD:
              case STATUS_LIST_OF_NEWLY_ADDED_PHONEBOOKITEMS:
              case STATUS_LIST_OF_DELETED_PHONEBOOKITEMS:
                  
                  tmp = 0;
                  StringBuffer phoneBnk = new StringBuffer();
                  int idBanke2 = b[eventDataFirstIndex + tmp] & 0xff;
                  phoneBnk.append(idBanke2 + "*");
                  tmp++;
                  int brojPhonova = b[eventDataFirstIndex + tmp] & 0xff;
                  phoneBnk.append(brojPhonova + ":");
                  tmp++;
                  //xmlData.Append("*");
                  while (tmp < eventDataLen)
                  {
                      int tmpInt3 = (b[eventDataFirstIndex + tmp] & 0xff);
                      //String iPhoneHexByte = tmpInt3.ToString("X").PadLeft(2, '0');
                      //phoneBnk += iPhoneHexByte;
                      phoneBnk.append(String.format("%02X", tmpInt3));

                      tmp++;
                  }
                  //pozicijaVozila.eventData = idBanke2 + "*" + brojPhonova + ":" + phoneBnk;
                  message.put(DeviceProperty.EVENT_DATA, phoneBnk.toString());
                  break;
              case STATUS_GARMIN_DEVICE_LOGIN:
                  
                  int passPos = b[eventDataFirstIndex];
                  StringBuffer k = new StringBuffer();
                  //pozicijaVozila.eventData = System.Text.Encoding.ASCII.GetString(b, eventDataFirstIndex + 1, eventDataLen - 1);
                  try {
                    k.append(new String(Arrays.copyOfRange(b, eventDataFirstIndex + 1, eventDataLen - 1), "UTF-8"));
                  } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                  
                  
                  if (passPos > 0)
                  {
                      //pozicijaVozila.eventData.Insert(passPos, " ");
                      k.append(" ");
                      message.put(DeviceProperty.EVENT_DATA,k.toString());
                  }
                  break;
              case STATUS_GARMIN_DEVICE_ARRIVAL:
              
                  if (eventDataLen > 0)
                  {
                      StringBuilder garmin = new StringBuilder();
                      //pozicijaVozila.eventData = BitConverter.ToUInt16(b, eventDataFirstIndex).ToString();
                      garmin.append(ByteBuffer.wrap(b).getShort(eventDataFirstIndex));
                      int eventDataLastIndex = eventDataFirstIndex + eventDataLen;
                      for (int i = eventDataFirstIndex + 2; i < eventDataLastIndex; i += 2)
                      {
                          //pozicijaVozila.eventData += " " + BitConverter.ToInt16(b, i).ToString();
                          garmin.append(" " + ByteBuffer.wrap(b).getShort(i));
                      }
                      message.put(DeviceProperty.EVENT_DATA, garmin.toString());
                  }
                  break;
              case STATUS_GARMIN_DEVICE_REMOVAL:
                  break;
              case STATUS_GARMIN_DEVICE_DATA:
                  
                  if (eventDataLen > 0)
                  {
                      //pozicijaVozila.garminData = new byte[eventDataLen];
                     // Array.Copy(b, eventDataFirstIndex, pozicijaVozila.garminData, 0, eventDataLen);
                  }
                  break;
            }
        }
        return message;
      }
      
      private static void parseCanFileds(Map <String,String> message,byte [] canGoData)
      {
          try
          {
              long tmp = ((long)canGoData[0] & 0xff);
              tmp = tmp + ((long)canGoData[1] & 0xff) * 256;
              tmp = tmp + ((long)canGoData[2] & 0xff) * 65536;
              tmp = tmp + ((long)canGoData[3] & 0xff) * 16777216;

              int pos = 4;
              //BitArray mask = new BitArray(tmp);
              for (int bit = 0; bit < 32; bit++)
              {
                  if (((tmp >> bit) & 1) == 1)
                  {
                      switch (bit)
                      {
                          case 0: // Average and Maximal Speed
                             // ret.Add(CANcodes.AVERAGE_SPEED, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                             // ret.Add(CANcodes.MAXIMAL_SPEED, (uint)canGoData[pos + 2] + (uint)canGoData[pos + 3] * 256);
                              message.put(DeviceProperty.SPEED_AVERAGE, Integer.toString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256));
                              message.put(DeviceProperty.SPEED_MAXIMAL, Integer.toString(((int)canGoData[pos + 2]&0xff) + ((int)canGoData[pos + 3]&0xff) * 256));
                              pos += 4;
                              break;

                          case 1: // Average and Maximal RPM
                              //ret.Add(CANcodes.AVERAGE_RPM, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                              //ret.Add(CANcodes.MAXIMAL_RPM, (uint)canGoData[pos + 2] + (uint)canGoData[pos + 3] * 256);
                              message.put(DeviceProperty.RPM_AVERAGE,  Integer.toString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256) );
                              message.put(DeviceProperty.RPM_MAXIMAL,  Integer.toString(((int)canGoData[pos + 2]&0xff) + ((int)canGoData[pos + 3]&0xff) * 256));
                              pos += 4;
                              break;

                          case 2: // Average and Maximal Fuel Consumption
                              //ret.Add(CANcodes.AVERAGE_FUEL_CONSUMPTION, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                              //ret.Add(CANcodes.MAXIMAL_FUEL_CONSUMPTION, (uint)canGoData[pos + 2] + (uint)canGoData[pos + 3] * 256);
                              
                              message.put(DeviceProperty.FUEL_AVERAGE_CONSUMPTION, Integer.toString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256));                           
                              message.put(DeviceProperty.FUEL_MAXIMAL_CONSUMPTION, Integer.toString(((int)canGoData[pos + 2]&0xff) + ((int)canGoData[pos + 3]&0xff) * 256));
                              
                              pos += 4;
                              break;

                          case 3: // Axel Weight 1
                              //ret.Add(CANcodes.AXEL_WEIGHT_1, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                              message.put(DeviceProperty.AXEL_WEIGHT_1, Integer.toString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256));
                              pos += 2;
                              break;

                          case 4: // Axel Weight 2
                              //ret.Add(CANcodes.AXEL_WEIGHT_2, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                              message.put(DeviceProperty.AXEL_WEIGHT_2, Integer.toString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256));
                              pos += 2;
                              break;

                          case 5: // Axel Weight 3
                              //ret.Add(CANcodes.AXEL_WEIGHT_3, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                              message.put(DeviceProperty.AXEL_WEIGHT_3, Integer.toString(((int)canGoData[pos]&0xff) + (int)canGoData[pos + 1] * 256));
                              pos += 2;
                              break;

                          case 6: // Axel Weight 4
                              //ret.Add(CANcodes.AXEL_WEIGHT_4, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                              message.put(DeviceProperty.AXEL_WEIGHT_4, Integer.toString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256));
                              pos += 2;
                              break;

                          case 7: // Average and Maximal Turbo Pressure
                              //ret.Add(CANcodes.AVERAGE_TURBO_PRESSURE, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                             // ret.Add(CANcodes.MAXIMAL_TURBO_PRESSURE, (uint)canGoData[pos + 2] + (uint)canGoData[pos + 3] * 256);
                              message.put(DeviceProperty.TURBO_PRESSURE_AVERAGE, Integer.toString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256));
                              message.put(DeviceProperty.TURBO_PRESSURE_MAXIMAL, Integer.toString(((int)canGoData[pos + 2]&0xff) + ((int)canGoData[pos + 3]&0xff) * 256));
                              pos += 4;
                              break;

                          case 8: // Minimal and Maximal Engine Temp
                              //ret.Add(CANcodes.MINIMAL_ENGINE_TEMP, (uint)canGoData[pos]);
                              //ret.Add(CANcodes.MAXIMAL_ENGINE_TEMP, (uint)canGoData[pos + 1]);
                              message.put(DeviceProperty.ENGINE_TEMP_MINIMAL,  Integer.toString((int)canGoData[pos]&0xff));
                              message.put(DeviceProperty.ENGINE_TEMP_MAXIMAL,  Integer.toString((int)canGoData[pos + 1]&0xff));
                              pos += 2;
                              break;

                          case 9: // Average and Maximal Accel. Pedal
                              //ret.Add(CANcodes.AVERAGE_ACCEL_PEDAL, (uint)canGoData[pos]);
                              //ret.Add(CANcodes.MAXIMAL_ACCEL_PEDAL, (uint)canGoData[pos + 1]);
                              message.put(DeviceProperty.ACCEL_PEDAL_AVERAGE, Integer.toString((int)canGoData[pos]&0xff));
                              message.put(DeviceProperty.ACCEL_PEDAL_MAXIMAL, Integer.toString((int)canGoData[pos + 1]&0xff));
                              pos += 2;
                              break;

                          case 10: // Average and Maximal Torque
                              //ret.Add(CANcodes.AVERAGE_TORQUE, (uint)canGoData[pos]);
                              //ret.Add(CANcodes.MAXIMAL_TORQUE, (uint)canGoData[pos + 1]);
                              message.put(DeviceProperty.TORQUE_AVERAGE, Integer.toString(((int)canGoData[pos]&0xff)));
                              message.put(DeviceProperty.TORQUE_MAXIMAL, Integer.toString((int)canGoData[pos + 1]&0xff));
                              pos += 2;
                              break;

                          case 11: // Total Fuel Used
                              //ret.Add(CANcodes.TOTAL_FUEL_USED, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //   + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              
                              message.put(DeviceProperty.FUEL_TOTAL_USED, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 12: // Fuel Used at Cruise
                              
//                            ret.Add(CANcodes.FUEL_USED_AT_CRUISE, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
//                                    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              
                              message.put(DeviceProperty.FUEL_USED_AT_CRUISE, Long.toString((long)(canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));      
                              pos += 4;
                              break;

                          case 13: // Fuel Used at Drive
                              //ret.Add(CANcodes.FUEL_USED_AT_DRIVE, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.FUEL_USED_AT_DRIVE, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));  
                              pos += 4;
                              break;

                          case 14: // Idle Longer than 5min
                              //ret.Add(CANcodes.IDLE_LONGER_THAN_5MIN, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.IDLE_LONGER_THAN_5MIN, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));  
                              pos += 4;
                              break;

                          case 15: // Idle Longer than 10min
                              //ret.Add(CANcodes.IDLE_LONGER_THAN_10MIN, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.IDLE_LONGER_THAN_10MIN, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216)); 
                              pos += 4;
                              break;

                          case 16: // Total Idle Time
                              //ret.Add(CANcodes.TOTAL_IDLE_TIME, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.TOTAL_IDLE_TIME, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 17: // Total Time PTO
                              //ret.Add(CANcodes.TOTAL_TIME_PTO, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //   + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.TOTAL_TIME_PTO, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 18: // Time Cruise
                              //ret.Add(CANcodes.TIME_CRUISE, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.TIME_CRUISE, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 19: // RPM > treshold_RPM1
                              //ret.Add(CANcodes.RPM_TRESHOLD_1, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //   + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.RPM_TRESHOLD_1, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 20: // RPM > treshold_RPM2
                              //ret.Add(CANcodes.RPM_TRESHOLD_2, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //        + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.RPM_TRESHOLD_2, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              
                              pos += 4;
                              break;

                          case 21: // Speed > treshold_Speed1
                              //ret.Add(CANcodes.SPEED_TRESHOLD_1, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.SPEED_TRESHOLD_1, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 22: // Speed > treshold_Speed2
                              //ret.Add(CANcodes.SPEED_TRESHOLD_2, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.SPEED_TRESHOLD_2, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 23: // Speed > treshold_Speed3
                              //ret.Add(CANcodes.SPEED_TRESHOLD_3, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.SPEED_TRESHOLD_3, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 24: // Brake Applications
                              //ret.Add(CANcodes.BRAKE_APPLICATIONS, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.BRAKE_APPLICATIONS, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 25: // Clutch Applications
                              //ret.Add(CANcodes.CLUTCH_APPLICATIONS, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.CLUTCH_APPLICATIONS, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 26: // Engine On
                              //ret.Add(CANcodes.ENGINE_ON, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.ENGINE_ON, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 27: // Time Torque > 90%
                              //ret.Add(CANcodes.TIME_TORQUE_90PERCENT, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.TORQUE_90PERCENT_TIME, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 28: // Milage
                              //ret.Add(CANcodes.MILAGE, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256
                              //    + (uint)canGoData[pos + 2] * 65536 + (uint)canGoData[pos + 3] * 16777216);
                              message.put(DeviceProperty.MILAGE, Long.toString(((long)canGoData[pos]&0xff) + ((long)canGoData[pos + 1]&0xff) * 256
                                      + ((long)canGoData[pos + 2]&0xff) * 65536 + ((long)canGoData[pos + 3]&0xff) * 16777216));
                              pos += 4;
                              break;

                          case 29: // Tachograph Data
                              //ret.Add(CANcodes.TACHO_OUTPUT_SHAFT_SPEED_AVG, (uint)canGoData[pos] + (uint)canGoData[pos + 1] * 256);
                              message.put(DeviceProperty.TACHO_OUTPUT_SHAFT_SPEED_AVG, Integer.toBinaryString(((int)canGoData[pos]&0xff) + ((int)canGoData[pos + 1]&0xff) * 256));
                              //ret.Add(CANcodes.TACHO_OUTPUT_SHAFT_SPEED_MAX, (uint)canGoData[pos + 2] + (uint)canGoData[pos + 3] * 256);
                              message.put(DeviceProperty.TACHO_OUTPUT_SHAFT_SPEED_MAX, Integer.toBinaryString(((int)canGoData[pos + 2]&0xff) + ((int)canGoData[pos + 3]&0xff) * 256));
                              //ret.Add(CANcodes.TACHO_VEHICLE_SPEED_AVG, (uint)canGoData[pos + 4] + (uint)canGoData[pos + 5] * 256);
                              message.put(DeviceProperty.TACHO_VEHICLE_SPEED_AVG, Integer.toBinaryString(((int)canGoData[pos + 4]&0xff) + ((int)canGoData[pos + 5]&0xff) * 256));
                              //ret.Add(CANcodes.TACHO_VEHICLE_SPEED_MAX, (uint)canGoData[pos + 6] + (uint)canGoData[pos + 7] * 256);
                              message.put(DeviceProperty.TACHO_VEHICLE_SPEED_MAX, Integer.toBinaryString(((int)canGoData[pos + 6]&0xff) + ((int)canGoData[pos + 7]&0xff) * 256));
                              //ret.Add(CANcodes.TACHO_INFO, (uint)canGoData[pos + 8]);
                              message.put(DeviceProperty.TACHO_INFO, Integer.toBinaryString(((int)canGoData[pos + 8]&0xff)));
                              //ret.Add(CANcodes.TACHO_DRIVER1_DATA, (uint)canGoData[pos + 9]);
                              message.put(DeviceProperty.TACHO_DRIVER1_DATA, Integer.toBinaryString(((int)canGoData[pos + 9]&0xff)));
                              //ret.Add(CANcodes.TACHO_DRIVER2_DATA, (uint)canGoData[pos + 10]);
                              message.put(DeviceProperty.TACHO_DRIVER2_DATA, Integer.toBinaryString(((int)canGoData[pos + 10]&0xff)));
                              pos += 11;
                              break;
                      }
                  }
              }
          }
          catch (Exception ex)
          {
              String tmp = "Cannot decode CANGO bytes! INFO: " + ex;
              Service.logger.error(tmp);
          }
      }
        
      //Status ID
      public static final int STATUS_REGULAR_GPS_DATA = 0;
      public static final int STATUS_WDT_START = 11;
      private static final int STATUS_RPM_OVER_LIMIT_EVENT = 18;
      private static final int STATUS_EXTERNAL_POWER_SUPPLY_BELLOW_LIMIT_EVENT = 27;
      private static final int STATUS_AUXILIAR_POWER_SUPPLY_BELLOW_LIMIT_EVENT = 28;
      private static final int STATUS_DALLAS_KEY_EVENT = 29;
      public static final int STATUS_FUEL_DATA_PACKET = 31;
      public static final int STATUS_FUEL_DATA_PACKET_FIRST_FUEL_SENSOR = 53;
      public static final int STATUS_FUEL_DATA_PACKET_SECOND_FUEL_SENSOR = 54;
      private static final int STATUS_FUEL_FLOW_DATA_PACKET = 32;
      private static final int STATUS_GREEN_DRIVE_PACKET = 39;
      private static final int STATUS_CANGO_DATA = 41;
      /* ovo sam ja dodao - nije bilo u originalnom source0u*/
      public static final int STATUS_GSM_ANTENNA = 43;
      public static final int STATUS_IBUTTON_LOGIN_MESSAGE = 51;
      public static final int STATUS_IBUTTON_LOGOUT_MESSAGE = 52;
      private static final int STATUS_RFID_LOGIN = 61;
      private static final int STATUS_RFID_LOGOUT = 62;
      private static final int STATUS_RESPONSE_ON_PARAM_CHANGE = 80;
      private static final int STATUS_DELETE_LOG_FILE_RESPONSE = 84;
      private static final int STATUS_IBUTTON_REMOTE_CHANGE_RESPONSE = 85;
      private static final int STATUS_FW_UPDATE = 88;
      private static final int STATUS_NET_LIST_CHANGE_RESPONSE = 98;
      private static final int STATUS_RESPONSE_ON_PARAMETER_READ_COMMAND = 100;
      private static final int STATUS_RESPONSE_ON_REMOTE_IBUTTON_CHANGE = 110;
      private static final int STATUS_LIST_OF_NEWLY_ADDED_IBUTTON_UIDS = 111;
      private static final int STATUS_LIST_OF_DELETED_IBUTTON_UIDS = 112;
      private static final int STATUS_GEO_FENCE_IN_EVENT = 120;
      private static final int STATUS_GEO_FENCE_OUT_EVENT = 121;
      private static final int STATUS_RESPONSE_ON_GEO_FENCE_INSERTION = 122;
      private static final int STATUS_RESPONSE_ON_GE_FENCE_DELETION = 123;
      private static final int STATUS_RESPONSE_ON_GE_FENCE_READ = 124;
      private static final int STATUS_RESPONSE_ON_REMOTE_PHONEBOOKREAD = 210;
      private static final int STATUS_LIST_OF_NEWLY_ADDED_PHONEBOOKITEMS = 211;
      private static final int STATUS_LIST_OF_DELETED_PHONEBOOKITEMS = 212;
      private static final int STATUS_GARMIN_DEVICE_LOGIN = 380;
      private static final int STATUS_GARMIN_DEVICE_ARRIVAL = 383;
      private static final int STATUS_GARMIN_DEVICE_REMOVAL = 384;
      private static final int STATUS_GARMIN_DEVICE_DATA = 385;
      public static final int STATUS_RESPONSE_CONTACT_KEY_ON = 1092;
      public static final int STATUS_RESPONSE_CONTACT_KEY_OFF = 1091;
      public static final int STATUS_13_UNKNOWN = 13;
      
      //pozicije u binarnom protokolu
      private static final int BIN_LEN = 2; //pozicije duzine poruke
      private static final int BIN_MS_TYPE = 3;
      private static final int BIN_V_ID = 4;
      private static final int BIN_MS_ID = 8;
      private static final int BIN_YEAR = 10;
      private static final int BIN_MONTH = 11;
      private static final int BIN_DAY = 12; 
      private static final int BIN_HOUR = 13;
      private static final int BIN_MINUTE = 14;
      private static final int BIN_SECUND = 15;
      private static final int BIN_LAT = 16;
      private static final int BIN_LAT_DEC = 18;
      private static final int BIN_LONG = 20;
      private static final int BIN_LONG_DEC = 22;
      private static final int BIN_SPEED = 24;
      private static final int BIN_DIR = 26;
      private static final int BIN_RESERVED = 28;
      private static final int BIN_S_INL = 0;
      private static final int BIN_S_INH = 1;
      private static final int BIN_S_OUT = 2;
      private static final int BIN_S_VEXT = 3;
      private static final int BIN_S_RPM = 5;
      private static final int BIN_S_FUEL = 7;
      private static final int BIN_S_TEMP = 8;
      private static final int BIN_S_USER = 10;
      private static final int BIN_S_DIST = 12;

      //pozicije za drugu verziju protokola
      private static final int POS_LEN = 2;
      private static final int POS_SESSION = 5;
      private static final int POS_MSG_TYPE = 6;
      private static final int POS_VEHICLE_ID = 7;
      private static final int POS_IDCHECK1 = 15;
      private static final int POS_IDCHECK2 = 16;
      private static final int POS_TAG = 17;
      private static final int POS_MSG_STATUS_ID = 27;
      private static final int POS_GPS_DATA = 29;
        //private static final int POS_RESERVED = 47;

        //pozicije u data delu (binV1 ili binV2)
      private static final int POS_STATUS = 0;
      private static final int POS_YEAR = 2;
      private static final int POS_LAT = 8;
      private static final int POS_LONG = 12;
      private static final int POS_SPEED = 16;
      private static final int POS_DIRECTION = 18;
      private static final int POS_RESERVED = 20;

}