package yu.co.certus.pos.geneco.protocol;

import yu.co.certus.pos.geneco.protocol.impl.ProtocolDecoderNewBinGeneko;
import yu.co.certus.pos.geneco.protocol.impl.ProtocolDecoderOldGeneko;
import io.netty.buffer.ByteBuf;

public class ProtocolDecoderFactory {

    public static ProtocolDecoder createProtocolDecoder(ByteBuf from_vehicle) throws UnknownProtocolException
    {
        
        
        int num = from_vehicle.readableBytes();

        if(num < 2)
            throw new UnknownProtocolException();
          
        final int magic1 = from_vehicle.getUnsignedByte(from_vehicle.readerIndex());

        final int magic2 = from_vehicle.getUnsignedByte(from_vehicle.readerIndex() + 1);
        
        
        if(magic1 == 0x5C && magic2 == 0x72) //bin
            return new ProtocolDecoderOldGeneko();
    
        if(magic1 == 0x3C && magic2 == 0x63) //XML
            return new ProtocolDecoderOldGeneko();
    
        if(magic1 == 0x2)
        {
            for(int i=1;i<num;i++)
            {
                int k = from_vehicle.getUnsignedByte(from_vehicle.readerIndex() + i);
            
                if((k>='0' && k<='9') || k==3)
                {
                    if(k==3 && i>1)
                    {
                        return new ProtocolDecoderOldGeneko();
                    }
                }
                else return new ProtocolDecoderNewBinGeneko();
            }
        }
        
        if(magic1 == 0x5C && magic2 == 0x65) //new bin
            return new ProtocolDecoderNewBinGeneko();
        
        
        throw new UnknownProtocolException();
    
                    
    }
}
