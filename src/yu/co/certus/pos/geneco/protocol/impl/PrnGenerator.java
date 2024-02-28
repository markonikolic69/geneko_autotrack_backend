package yu.co.certus.pos.geneco.protocol.impl;

public class PrnGenerator
{

    byte[] value;
    int pointer;
    int[] feedback;
    int size;
    /** Creates a new instance of PsnGenerator */
    public PrnGenerator(byte[] v, int s, int[] nizF)
    {
        size = s;
        value = new byte[size];
        for (int i = 0; i < size; i++)
        {
            value[i] = v[i];
        }
        feedback = nizF;
        pointer = 0;
    }

    public byte getByte()
    {
        int wrtPtr = (pointer - 1);
        if (wrtPtr < 0) wrtPtr += size;
        byte tmp = 0x00;
        int a = 0;
        for (int i = 0; i < feedback.length; i++)
        {
            a = pointer - feedback[i] - 1;
            if (a < 0) a += size;
            // System.out.println("vrednost: "+value[a]);
            tmp = (byte)(tmp ^ value[a]);
        }
        value[wrtPtr] = tmp;
        pointer = (pointer - 1);
        if (pointer < 0) pointer += size;
        return tmp;

    }
}
