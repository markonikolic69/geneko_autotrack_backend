package yu.co.certus.pos.geneco.protocol;

import yu.co.certus.pos.geneco.protocol.message.GPSMessage;
import io.netty.buffer.ByteBuf;



public interface ProtocolDecoder {
    
    public String getProtocol();
    public String decodeToJSONString(ByteBuf in);
    public String getID(ByteBuf in);
    public GPSMessage getGPSMessage();

    
}
