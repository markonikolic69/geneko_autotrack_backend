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


public class ProtocolDecoderNewBinGeneko implements ProtocolDecoder {

    final String protocol = Protocols.NEW_BIN_GENEKO;
    
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
        
        if ( in.getUnsignedByte(in.readerIndex()) == 0x5C) {
            return String.valueOf(in.getInt(in.readerIndex() +POS_VEHICLE_ID));
        } else if (in.getUnsignedByte(in.readerIndex()) == 0x2) {
            
            int size=in.readableBytes();

            for(int i=2;i<size;i++)
            {
                if(in.getUnsignedByte(in.readerIndex()+i) == 0x3)
                {
                    byte[] by= new byte[i+1];
                    in.getBytes(0, by, in.readerIndex(), i+1);

                    return getIdFromCommand(by);
                }
            }
        //  String ss =String.valueOf((in.readerIndex() +BIN_V_ID)) 
        }
        return null;
    }
    
    


    @Override
    public String decodeToJSONString(ByteBuf in) {
 //       OldListenerHandler.EVENT event = getEvent(in);
        int n = in.readableBytes();
        byte array[] = new byte[n];
        in.readBytes(array, 0, n);
        
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
      
      private boolean idCheck (byte[]binaryData)
      { 
          //provera idCheck1 i idCheck2
          int sumBajt = 0x6c;
          int xorBajt = 0xb4;
          for (int i = POS_VEHICLE_ID; i < POS_VEHICLE_ID + 8; i++)
          {
              sumBajt += binaryData[i];
              xorBajt ^= binaryData[i];
          }
          sumBajt %= 256;
          if (binaryData[POS_IDCHECK1] != mag[sumBajt] || binaryData[POS_IDCHECK2] != mag[xorBajt])
              return false; //ukoliko nije dobar id vrati false
          else
              return true; //inace sve je ok. vrati true
      }
      private static byte[] dekriptujData(byte[] binaryData, byte[] sk)
      {

          int lenOfData = binaryData.length - 28; //duzina data dela
          byte[] data = new byte[lenOfData]; //inicijalizacija data

          byte[] key = {mag[sk[3]&0xff], mag[sk[2]&0xff], mag[sk[1]&0xff], mag[sk[0]&0xff],
                                  mag[binaryData[POS_VEHICLE_ID+7]&0xff],mag[binaryData[POS_VEHICLE_ID+6]&0xff],mag[binaryData[POS_VEHICLE_ID+5]&0xff],mag[binaryData[POS_VEHICLE_ID+4]&0xff]};
          int[] povratnaSprega = { 6, 5, 4, 0 };

          PrnGenerator prn = new PrnGenerator(key, 8, povratnaSprega);
          for (int i = 0; i < 22; i++)
          {
              prn.getByte();
          }
          byte[] bKey = new byte[uk.length];
          for (int i = 0; i < uk.length; i++)
              bKey[i] = (byte)(uk[i] ^ prn.getByte());

          int[] povratnaSprega2 = { 9, 0 };
          prn = new PrnGenerator(bKey, uk.length, povratnaSprega2);

          for (int i = 0; i < 60; i++)
          {
              prn.getByte();
          }

          for (int i = 0; i < lenOfData; i++)
          {
              data[i] = (byte)(binaryData[POS_MSG_STATUS_ID + i] ^ prn.getByte());//otkljucavanje data dela
          }
          return data;

      }
      
      
      /// <summary>
      /// Proverava FoxIdTag
      /// </summary>
      /// <param name="binaryData">podaci, odnosno niz bajtova dobijen od Fox-a</param>
      /// <returns>
      /// niz bajtova koji predstavljaju id tag - sve ok
      /// null - nesto nije dobro
      /// </returns>
      private static byte[] proveriTag(byte[] binaryData)
      {
          byte[] tag = new byte[10];
          for (int i = 0; i < tag.length; i++)
          {
              tag[i] = binaryData[POS_TAG + i];
          }

          //cheksum
          int chkSum = 0;
          for (int i = 0; i < 9; i++)
          {
              chkSum += binaryData[POS_TAG + i]&0xff;
          }
          if (chkSum % 256 == (tag[9]&0xff))
          {
              return tag;
          }
          else
          {
              return null;
          }
      }
      
      
      public static Map<String, String> parseMessage(byte[] b){

          
        Map <String,String> message = new LinkedHashMap<String,String> ();
        int msLen = (int)b[BIN_LEN] & 0xff;
        int cellLen = 0;
        int tmp,tmp2;
        int idStatus;
        
        byte[] sk = new byte[4]; //spoljasnji kljuc
        for (int i = 0; i < 4; i++)
        {
            sk[i] = b[POS_TAG + i];
        }
        
        
        int lenOfData = b.length - 28; //duzina data dela
        byte[] data = new byte[lenOfData]; //niz u koji ce biti smesteni bajtovi data dela

        //provera tipa poruke, da li treba desifrovati, punjenje data dela
        int msgType = b[POS_MSG_TYPE]&0xff;
        switch (msgType)
        {
            case 0xa3: //kriptovana poruka
                {
                    data = dekriptujData(b, sk);
                    break;
                }
            case 0x36: //obicni GPS podaci
                {
                    for (int i = 0; i < lenOfData; i++)
                    {
                        data[i] = b[POS_MSG_STATUS_ID + i];
                    }
                    break;
                }
            case 0x56: //obicni GPS podaci
                {
                    for (int i = 0; i < lenOfData; i++)
                    {
                        data[i] = b[POS_MSG_STATUS_ID + i];
                    }
                    break;
                }
            case 0x3a: //citanje parametara
                {
                    //sta ovde?
                    break;
                }
            default:
                {
                    break;
                }
        }
          
        tmp = ByteBuffer.wrap(b).getInt(POS_VEHICLE_ID);
        message.put(DeviceProperty.VEHICLE_ID, Integer.toString(tmp));
        
        
        byte[] tag = proveriTag(b);
        
        if(tag==null)
            return null;
        
        if( (tag[4] & 0x80) == 0x80)
        {
            //proracun pomeraja
            byte[] uk_v2 = { (byte)202, (byte)87, (byte)195, (byte)94 };
            byte[] startKey = {sk[0],(byte)(uk_v2[0]^b[POS_VEHICLE_ID+4]),(byte)(uk_v2[1]^b[POS_VEHICLE_ID+5]),(byte)(uk_v2[2]^b[POS_VEHICLE_ID+6]),
                        sk[1], sk[2], (byte)(uk_v2[3]^b[POS_VEHICLE_ID+7]),sk[3]};
            int[] povratnaSprega = { 6, 5, 4, 0 };
            PrnGenerator prnG = new PrnGenerator(startKey, 8, povratnaSprega);
            for (int i = 0; i < 17; i++)
            {
                prnG.getByte();
            }

            //otkljucavanje id statusa
            data[0] = (byte)((data[0] & 0xff) ^ (prnG.getByte() & 0xff));
            data[1] = (byte)((data[1] & 0xff) ^ (prnG.getByte() & 0xff));

            //otkljucavanje latitude
            int tmpLat = ((int)data[POS_LAT] & 0xff) * 256 + ((int)data[POS_LAT + 1] & 0xff) - ((int)prnG.getByte() & 0xff);//konvertuj decimalu minuta
            data[POS_LAT] = (byte)(tmpLat / 256);
            data[POS_LAT + 1] = (byte)(tmpLat % 256);

            //otkljucavanje longitude
            int tmpLong = ((int)data[POS_LONG] & 0xff) * 256 + ((int)data[POS_LONG + 1] & 0xff) - ((int)prnG.getByte() & 0xff);
            data[POS_LONG] = (byte)(tmpLong / 256);
            data[POS_LONG + 1] = (byte)(tmpLong % 256);

            //otkljucavanje pravca
            int tmpDir = ((int)data[POS_DIRECTION] & 0xff) * 256 + ((int)data[POS_DIRECTION + 1] & 0xff) - ((int)prnG.getByte() & 0xff);
            if (tmpDir < 0) //vrati u opseg od 0 do 360
            {
                tmpDir += 360;
            }
            data[POS_DIRECTION] = (byte)(tmpDir / 256);
            data[POS_DIRECTION + 1] = (byte)(tmpDir % 256);

            //status
            int gsmDataLen = data[POS_RESERVED] & 0xff;
            int statusLen = data[POS_RESERVED + gsmDataLen + 1] & 0xff;
            for (int i = 0; i < statusLen; i++)
            {
                data[POS_RESERVED + 2 + gsmDataLen + i] ^= prnG.getByte();
            }
            tag[4] = (byte)(tag[4] & 0x7f); //prepravi bit koji pokazuje da li postoji pomeraj, zato sto je pomeraj uklonjen
        }


        idStatus = ByteBuffer.wrap(data).getShort(POS_STATUS);
        message.put(DeviceProperty.ID_STATUS, Integer.toString(idStatus));

        if ((data[POS_YEAR] & 0x80) == 0)
             message.put(DeviceProperty.VALID_POSITION, "A"); //Good position
        else
            message.put(DeviceProperty.VALID_POSITION, "V");

        message.put(DeviceProperty.DAY,  Integer.toString(data[POS_YEAR +2]));
        message.put(DeviceProperty.MONTH,  Integer.toString(data[POS_YEAR+1]));

        tmp = data[POS_YEAR] & 0x7f;
        tmp += 2000;
        message.put(DeviceProperty.YEAR,  Integer.toString(tmp));
        message.put(DeviceProperty.HOUR,  Integer.toString(data[POS_YEAR+3]));
        message.put(DeviceProperty.MINUTE,  Integer.toString(data[POS_YEAR+4]));
        message.put(DeviceProperty.SECOND,  Integer.toString(data[POS_YEAR+5]));
        String inputString = tmp + "-" + data[POS_YEAR+1] + "-" + data[POS_YEAR+2] + " " + data[POS_YEAR+3]
                + ":" + data[POS_YEAR+4] + ":" + data[POS_YEAR+5];
        
        Long vreme=0L;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = sdf.parse(inputString);

                 vreme = date.getTime();
            } catch (Exception e) {
            }

        message.put(DeviceProperty.TIMESTAMP, Long.toString(vreme));
        
        
        tmp2 = (data[POS_LAT] & 0x7f) * 256 + ((int)data[POS_LAT + 1] & 0xff);
        tmp = tmp2 / 60; //stepeni
        tmp2 = tmp2 % 60; //minuti


        double latCeoDeo = (double)tmp;
        tmp = (data[POS_LAT+2]) * 256 + ((int)data[POS_LAT+3] & 0xff);
        double tmp2Doub = (double)tmp2 + ((double)tmp) / 10000;
        double doubLat = latCeoDeo + tmp2Doub / 60;
        message.put(DeviceProperty.LAT, Double.toString(doubLat));
        if ((data[POS_LAT] & 0x80) == 0)
            message.put(DeviceProperty.NORTHING, "N");
        else
            message.put(DeviceProperty.NORTHING, "S");
        

        tmp2 = (data[POS_LONG] & 0x7f) * 256 + ((int)data[POS_LONG + 1] & 0xff);
        tmp = tmp2 / 60; //stepeni
        tmp2 = tmp2 % 60; //minuti
        double longCeoDeo = (double)(tmp);
        tmp = (data[POS_LONG+2]) * 256 + ((int)data[POS_LONG + 3] & 0xff);
        tmp2Doub = (double)tmp2 + ((double)tmp) / 10000;
        double doubLong = longCeoDeo + tmp2Doub / 60;
        message.put(DeviceProperty.LON, Double.toString(doubLong));
        if ((data[POS_LONG] & 0x80) == 0)
            message.put(DeviceProperty.EASTING, "E");
        else
            message.put(DeviceProperty.EASTING, "W");    
     
        tmp =  ByteBuffer.wrap(data).getShort(POS_SPEED);
        message.put(DeviceProperty.SPEED, Integer.toString(tmp));

        tmp =  ByteBuffer.wrap(data).getShort(POS_DIRECTION);
        if (tmp < 0) tmp += 360;
        message.put(DeviceProperty.DIRECTION, Integer.toString(tmp));

        cellLen = ((int)data[POS_RESERVED]) & 0xff;
        System.out.println("cellLen: " + cellLen);
        tmp = 1;
        StringBuilder baznaStanica = new StringBuilder();
        
        for(int i = 1; i<=cellLen;i++)
        {
            baznaStanica.append((char)data[POS_RESERVED + 1 + i]);
        }
        message.put(DeviceProperty.CELL_INFO, baznaStanica.toString());

        //status
        int statusLen = ((int)data[POS_RESERVED + 1 + cellLen]) & 0xff;
        if (statusLen == 16)
        { //primarni format statusa je dugacak 16 bajtova, inace ignorisi
            StringBuilder tmpStr = new StringBuilder();
            byte tmpB = data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_INL]; //INPUT BITI L
            for (int i = 0; i < 8; i++)
            {
                //tmpStr = tmpStr + ((tmpB >> i) & 0x01);
                tmpStr.append(((tmpB >> i) & 0x01));
            }
            tmpB = data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_INH]; //INPUT BITI H
            for (int i = 0; i < 8; i++)
            {
                //tmpStr = tmpStr + ((tmpB >> i) & 0x01);
                tmpStr.append(((tmpB >> i) & 0x01));
            }
            //Console.WriteLine("Input: " + tmpStr);
           // pozicijaVozila.input = tmpStr;
            message.put(DeviceProperty.INPUT, tmpStr.toString());
           // tmpStr = tmpStr + " ";
            tmp = ((int)data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_VEXT] & 0xff) * 256;
            tmp = tmp + ((int)data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_VEXT + 1] & 0xff);
           // tmpStr = tmpStr + tmp + " "; //externo napajanje
            //Console.WriteLine("Main power: "+tmp);
            //pozicijaVozila.mainPower = tmp;
            message.put(DeviceProperty.MAIN_POWER, Integer.toString(tmp));
            
            tmp = ((int)data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_USER] & 0xff) * 256;
            tmp = tmp + ((int)data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_USER + 1] & 0xff);
            //tmpStr = tmpStr + tmp + " "; //korisnicki AD ulaz
            //pozicijaVozila.backUpPower = tmp;
            message.put(DeviceProperty.BACKUP_POWER, Integer.toString(tmp));
            //tmpStr=tmpStr+"0 "; //korisnicki AD ulaz
            tmp = ((int)data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_RPM] & 0xff) * 256;
            tmp = tmp + ((int)data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_RPM + 1] & 0xff);
           // tmpStr = tmpStr + tmp + " "; //RMP
            //pozicijaVozila.roundPerMinute = tmp;
            message.put(DeviceProperty.RPM, Integer.toString(tmp));
            tmp = ((int)data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_FUEL] & 0xff);
            //pozicijaVozila.gasLevel = tmp;
            message.put(DeviceProperty.GAS_LEVEL, Integer.toString(tmp));
           // tmpStr = tmpStr + tmp + " "; //FUEL
           // tmpStr = tmpStr + "0 0 "; //shock senzor x i y ne prenose se vise
           // pozicijaVozila.xAxis = 0;
           // pozicijaVozila.yAxis = 0;
            tmpB = data[POS_RESERVED + 1 + cellLen + 1 + BIN_S_OUT]; //OUTPUT BITI
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < 8; i++)
                output.append(((tmpB >> i) & 0x01));
            //pozicijaVozila.output = output;
            message.put(DeviceProperty.OUTPUT, output.toString());
        }
        
        int eventDataLen = ((int)data[POS_RESERVED + 1 + cellLen + 1 + statusLen]) & 0xff;
        
        int eventDataFirstIndex = POS_RESERVED + 1 + cellLen + 1 + statusLen + 1;
        
        if(eventDataLen==0)
            eventDataLen=data.length-eventDataFirstIndex;
        
//      System.out.println("data.lenght "+data.length + "eventDataFirstIndex: " +eventDataFirstIndex);
        if(idStatus==STATUS_REGULAR_GPS_DATA)
        {
            StringBuilder tmpStr2 = new StringBuilder();
            if (eventDataLen >= 4)
            {
                tmp = ((int)data[eventDataFirstIndex + 0] & 0xff) * 16777216;
                tmp = tmp + ((int)data[eventDataFirstIndex + 1] & 0xff) * 65536;
                tmp = tmp + ((int)data[eventDataFirstIndex + 2] & 0xff) * 256;
                tmp = tmp + ((int)data[eventDataFirstIndex + 3] & 0xff);
                //tmpStr2 = tmp.ToString();
                tmpStr2.append(tmp);
            }
            if (eventDataLen >= 6)
            {
                //tmpStr2 += " ";
                tmpStr2.append(" ");
                tmp = (int)(data[eventDataFirstIndex + 4] & 0xff);
                //tmpStr2 += tmp.ToString();
                tmpStr2.append(tmp);
                tmp = (int)(data[eventDataFirstIndex + 5] & 0xff);
                tmpStr2.append("." + tmp);
                //tmpStr2 += "." + tmp.ToString();
            }
            message.put(DeviceProperty.EVENT_DATA, tmpStr2.toString());
        }
        else if(idStatus==STATUS_RESPONSE_CONTACT_KEY_ON || idStatus==STATUS_RESPONSE_CONTACT_KEY_OFF)
        {
            StringBuilder evData2 = new StringBuilder();
            boolean isBinary = false;
            for (int i = 0; i < eventDataLen; i++)
            {
                byte tmpB = data[eventDataFirstIndex + i];
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
              tmp3 = ((int)data[eventDataFirstIndex + 0] & 0xff) * 16777216;
              tmp3 = tmp3 + ((int)data[eventDataFirstIndex + 1] & 0xff) * 65536;
              tmp3 = tmp3 + ((int)data[eventDataFirstIndex + 2] & 0xff) * 256;
              tmp3 = tmp3 + ((int)data[eventDataFirstIndex + 3] & 0xff);
              //  evData2 = new StringBuilder();
               // evData = tmp.ToString();
               evData2.append(tmp3);

                if (eventDataLen == 6)
                {
                   // evData += " ";
                    evData2.append(" ");
                    tmp = (int)(data[eventDataFirstIndex + 4] & 0xff);
                    //evData += tmp.ToString();
                    evData2.append(tmp);
                    tmp = (int)(data[eventDataFirstIndex + 5] & 0xff);
                    //evData += "." + tmp.ToString();
                    evData2.append("."+tmp);
                }
            }
            //pozicijaVozila.eventData = evData;
            message.put(DeviceProperty.EVENT_DATA, evData2.toString());
            
        }

        
        
        if (eventDataLen > 0)
        {
            switch(idStatus)
            {
              case STATUS_REGULAR_GPS_DATA:

                  //pozicijaVozila.eventData = tmpStr2;
                  break;
              case STATUS_RPM_OVER_LIMIT_EVENT: 
              case STATUS_EXTERNAL_POWER_SUPPLY_BELLOW_LIMIT_EVENT: 
              case STATUS_AUXILIAR_POWER_SUPPLY_BELLOW_LIMIT_EVENT:
                  tmp = ((int)data[eventDataFirstIndex] & 0xff) * 256;
                  tmp = tmp + ((int)data[eventDataFirstIndex + 1] & 0xff);
                  //pozicijaVozila.eventData = tmp.ToString();
                  message.put(DeviceProperty.EVENT_DATA, Integer.toString(tmp));
                  break;
              case STATUS_DALLAS_KEY_EVENT:
              case STATUS_IBUTTON_LOGIN_MESSAGE:
              case STATUS_LOGOUT_MESSAGE:
                  StringBuilder tmpStr = new StringBuilder();
                  for (int i = eventDataFirstIndex; i < msLen - 1; i++)
                  {
                      int tmpInt2 = ((int)data[i] & 0xff);
                      tmpStr.append(String.format("%02X", tmpInt2));
                  }
                  //pozicijaVozila.eventData = tmpStr;
                  message.put(DeviceProperty.EVENT_DATA, tmpStr.toString());
                  break;
              case STATUS_FUEL_DATA_PACKET:
                  StringBuilder tmpIbutton = new StringBuilder();
                  for (int i = 0; i < 32; i++)
                  {
                      tmp = ((int)data[eventDataFirstIndex + i * 2] & 0xff) * 256;
                      tmp = tmp + ((int)data[eventDataFirstIndex + i * 2 + 1] & 0xff);
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
                      tmp = ((int)data[eventDataFirstIndex + i] & 0xff);
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
                          tmp = ((int)data[eventDataFirstIndex + i] & 0xff) * 256;
                          tmp = tmp + ((int)data[eventDataFirstIndex + i + 1] & 0xff);
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
                      tmp = ((int)data[eventDataFirstIndex] & 0xff);
                      message.put(DeviceProperty.EVENT_DATA, Integer.toString(tmp));

                      if (tmp == 33)
                      {
                          int canGoDataLen = (int)(data[eventDataFirstIndex + eventDataLen]);

                          tmp = 0;
                          byte[] canGoData = new byte[canGoDataLen];
                          while (tmp < canGoDataLen)
                          {
                              canGoData[tmp] = data[eventDataFirstIndex + eventDataLen + 1 + tmp];
                              tmp++;
                          }
                          //pozicijaVozila.CAN_Data = AvlServer.CANBus.convertToCANfileds(canGoData);
                         // message.put(DeviceProperty.can, tmp);
                          parseCanFileds(message,canGoData);
     
                      }
                  }
                  break;
              case STATUS_FW_UPDATE:
                  tmp = ((int)data[eventDataFirstIndex] & 0xff);
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
                      evData1.append((char)data[eventDataFirstIndex + tmp]);
                      tmp++;
                  }
                  //pozicijaVozila.eventData = evData;
                  message.put(DeviceProperty.EVENT_DATA, evData1.toString());
                  break;

              case STATUS_RESPONSE_ON_REMOTE_IBUTTON_CHANGE:
              case STATUS_LIST_OF_NEWLY_ADDED_IBUTTON_UIDS:
              case STATUS_LIST_OF_DELETED_IBUTTON_UIDS:
                  tmp = 0;
                  StringBuilder iBut = new StringBuilder();
                  int idBanke = data[eventDataFirstIndex + tmp] & 0xff;
                  tmp++;
                  int brojButtona = data[eventDataFirstIndex + tmp] & 0xff;
                  tmp++;
                  while (tmp < eventDataLen)
                  {
                      int tmpInt3 = (data[eventDataFirstIndex + tmp] & 0xff);
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
                  tmp = ((int)data[eventDataFirstIndex + 0] & 0xff) * 16777216;
                  tmp = tmp + ((int)data[eventDataFirstIndex + 1] & 0xff) * 65536;
                  tmp = tmp + ((int)data[eventDataFirstIndex + 2] & 0xff) * 256;
                  tmp = tmp + ((int)data[eventDataFirstIndex + 3] & 0xff);
                  //tmpStr2 = tmp.ToString();
                  //pozicijaVozila.eventData = tmpStr2;
                  message.put(DeviceProperty.EVENT_DATA, Integer.toString(tmp));
                  break;
              case STATUS_RESPONSE_ON_GEO_FENCE_INSERTION:
              case STATUS_RESPONSE_ON_GE_FENCE_DELETION:
                  StringBuffer tmpStr3 = new StringBuffer();
                  tmp = ((int)data[eventDataFirstIndex + 0] & 0xff) * 16777216;
                  tmp = tmp + ((int)data[eventDataFirstIndex + 1] & 0xff) * 65536;
                  tmp = tmp + ((int)data[eventDataFirstIndex + 2] & 0xff) * 256;
                  tmp = tmp + ((int)data[eventDataFirstIndex + 3] & 0xff);
                  //tmpStr2 = tmp.ToString() + ",";
                  tmpStr3.append(tmp+",");

                  int kodGreske = ((int)data[eventDataFirstIndex + 4] & 0xff);
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
                  int brojZona = ((int)data[eventDataFirstIndex + 0] & 0xff);
                  //tmpStr2 = brojZona + ":";
                  tmpStr4.append(brojZona + ":");
                  for (int i = 1; i <= brojZona * 4; i += 4)
                  {
                      tmp = ((int)data[eventDataFirstIndex + 0 + i] & 0xff) * 16777216;
                      tmp = tmp + ((int)data[eventDataFirstIndex + 1 + i] & 0xff) * 65536;
                      tmp = tmp + ((int)data[eventDataFirstIndex + 2 + i] & 0xff) * 256;
                      tmp = tmp + ((int)data[eventDataFirstIndex + 3 + i] & 0xff);
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
                  int idBanke2 = data[eventDataFirstIndex + tmp] & 0xff;
                  phoneBnk.append(idBanke2 + "*");
                  tmp++;
                  int brojPhonova = data[eventDataFirstIndex + tmp] & 0xff;
                  phoneBnk.append(brojPhonova + ":");
                  tmp++;
                  //xmlData.Append("*");
                  while (tmp < eventDataLen)
                  {
                      int tmpInt3 = (data[eventDataFirstIndex + tmp] & 0xff);
                      //String iPhoneHexByte = tmpInt3.ToString("X").PadLeft(2, '0');
                      //phoneBnk += iPhoneHexByte;
                      phoneBnk.append(String.format("%02X", tmpInt3));

                      tmp++;
                  }
                  //pozicijaVozila.eventData = idBanke2 + "*" + brojPhonova + ":" + phoneBnk;
                  message.put(DeviceProperty.EVENT_DATA, phoneBnk.toString());
                  break;
              case STATUS_GARMIN_DEVICE_LOGIN:
                  
                  int passPos = data[eventDataFirstIndex];
                  StringBuffer k = new StringBuffer();
                  //pozicijaVozila.eventData = System.Text.Encoding.ASCII.GetString(b, eventDataFirstIndex + 1, eventDataLen - 1);
                  try {
                    k.append(new String(Arrays.copyOfRange(data, eventDataFirstIndex + 1, eventDataLen - 1), "UTF-8"));
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
                      garmin.append(ByteBuffer.wrap(data).getShort(eventDataFirstIndex));
                      int eventDataLastIndex = eventDataFirstIndex + eventDataLen;
                      for (int i = eventDataFirstIndex + 2; i < eventDataLastIndex; i += 2)
                      {
                          //pozicijaVozila.eventData += " " + BitConverter.ToInt16(b, i).ToString();
                          garmin.append(" " + ByteBuffer.wrap(data).getShort(i));
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
      
      
      private static byte[] mag = {(byte)136,(byte) 215,(byte) 199,(byte) 23,(byte) 4,(byte) 126,(byte) 129,(byte) 233,(byte) 115,(byte) 193,(byte) 220,(byte) 76,(byte) 106,(byte) 47,(byte) 123,(byte) 224,(byte) 77,(byte) 117,(byte) 10,(byte) 148,(byte) 88,(byte) 196,(byte) 189,(byte) 232,(byte) 108,(byte) 228,(byte)
              51,(byte) 244,(byte) 227,(byte) 231,(byte) 153,(byte) 24,(byte) 241,(byte) 130,(byte) 67,(byte) 159,(byte) 211,(byte) 185,(byte) 150,(byte) 237,(byte) 13,(byte) 64,(byte) 53,(byte) 63,(byte) 171,(byte) 243,(byte) 55,(byte) 138,(byte) 98,(byte) 14,(byte) 234,(byte) 217,(byte)
              45,(byte) 206,(byte) 41,(byte) 176,(byte) 168,(byte) 89,(byte) 219,(byte) 152,(byte) 7,(byte) 212,(byte) 109,(byte) 251,(byte) 22,(byte) 27,(byte) 91,(byte) 110,(byte) 81,(byte) 250,(byte) 9,(byte) 62,(byte) 180,(byte) 214,(byte) 216,(byte) 50,(byte) 235,(byte) 198,(byte)
               8,(byte) 33,(byte) 145,(byte) 57,(byte) 96,(byte) 187,(byte) 35,(byte) 36,(byte) 240,(byte) 157,(byte) 102,(byte) 112,(byte) 107,(byte) 48,(byte) 94,(byte) 15,(byte) 245,(byte) 252,(byte) 194,(byte) 118,(byte) 160,(byte) 120,(byte) 74,(byte) 161,(byte) 103,(byte) 111,(byte)
              78,(byte) 223,(byte) 230,(byte) 134,(byte) 61,(byte) 30,(byte) 58,(byte) 202,(byte) 87,(byte) 49,(byte) 65,(byte) 42,(byte) 26,(byte) 68,(byte) 75,(byte) 190,(byte) 66,(byte) 128,(byte) 246,(byte) 86,(byte) 46,(byte) 100,(byte) 197,(byte) 162,(byte) 205,(byte) 131,(byte)
              92,(byte) 186,(byte) 60,(byte) 1,(byte) 31,(byte) 37,(byte) 239,(byte) 184,(byte) 137,(byte) 200,(byte) 226,(byte) 248,(byte) 188,(byte) 247,(byte) 73,(byte) 11,(byte) 139,(byte) 125,(byte) 43,(byte) 5,(byte) 97,(byte) 164,(byte) 6,(byte) 203,(byte) 72,(byte) 20,(byte)
              174,(byte) 146,(byte) 147,(byte) 140,(byte) 169,(byte) 70,(byte) 254,(byte) 119,(byte) 191,(byte) 93,(byte) 165,(byte) 25,(byte) 238,(byte) 167,(byte) 172,(byte) 39,(byte) 90,(byte) 113,(byte) 116,(byte) 21,(byte) 177,(byte) 183,(byte) 0,(byte) 124,(byte) 52,(byte) 32,(byte)
              142,(byte) 28,(byte) 143,(byte) 3,(byte) 170,(byte) 236,(byte) 18,(byte) 101,(byte) 218,(byte) 135,(byte) 34,(byte) 59,(byte) 213,(byte) 114,(byte) 222,(byte) 249,(byte) 163,(byte) 210,(byte) 166,(byte) 179,(byte) 2,(byte) 181,(byte) 201,(byte) 253,(byte) 221,(byte) 83,(byte)
              79,(byte) 192,(byte) 195,(byte) 105,(byte) 133,(byte) 151,(byte) 242,(byte) 40,(byte) 175,(byte) 141,(byte) 182,(byte) 99,(byte) 17,(byte) 207,(byte) 54,(byte) 149,(byte) 71,(byte) 225,(byte) 82,(byte) 80,(byte) 229,(byte) 44,(byte) 209,(byte) 12,(byte) 95,(byte) 16,(byte)
              208,(byte) 19,(byte) 122,(byte) 255,(byte) 104,(byte) 154,(byte) 178,(byte) 29,(byte) 144,(byte) 38,(byte) 69,(byte) 84,(byte) 155,(byte) 173,(byte) 56,(byte) 127,(byte) 158,(byte) 204,(byte) 156,(byte) 121,(byte) 85,(byte) 132};
        
      public static byte[] uk = {(byte)41,(byte)164,(byte) 53,(byte) 242,(byte)65,(byte)182,(byte)31,(byte)111,(byte)164,(byte)119,(byte)24,(byte)238,(byte)136,(byte)47,(byte)58,(byte)89,(byte)1,(byte)208,(byte)71,(byte)218,(byte)61,(byte)
              135,(byte)105,(byte)63,(byte)63,(byte)204,(byte)226,(byte)226,(byte)143,(byte)137,(byte)17,(byte)3,(byte)127,(byte)11,(byte)214,(byte)161,(byte)120,(byte)12,(byte)245,(byte)188,(byte)34,(byte)122,(byte)
               166,(byte)108,(byte)63,(byte)21,(byte)241,(byte)142,(byte)123};
      //Status ID
      private static final int STATUS_REGULAR_GPS_DATA = 0;
      private static final int STATUS_WDT_START = 11;
      private static final int STATUS_RPM_OVER_LIMIT_EVENT = 18;
      private static final int STATUS_EXTERNAL_POWER_SUPPLY_BELLOW_LIMIT_EVENT = 27;
      private static final int STATUS_AUXILIAR_POWER_SUPPLY_BELLOW_LIMIT_EVENT = 28;
      private static final int STATUS_DALLAS_KEY_EVENT = 29;
      private static final int STATUS_FUEL_DATA_PACKET = 31;
      private static final int STATUS_FUEL_FLOW_DATA_PACKET = 32;
      private static final int STATUS_GREEN_DRIVE_PACKET = 39;
      private static final int STATUS_CANGO_DATA = 41;
      private static final int STATUS_IBUTTON_LOGIN_MESSAGE = 51;
      private static final int STATUS_LOGOUT_MESSAGE = 52;
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
      private static final int STATUS_RESPONSE_CONTACT_KEY_ON = 1092;
      private static final int STATUS_RESPONSE_CONTACT_KEY_OFF = 1091;
      
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