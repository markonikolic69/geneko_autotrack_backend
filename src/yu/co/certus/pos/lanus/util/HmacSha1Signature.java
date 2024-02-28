package yu.co.certus.pos.lanus.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class HmacSha1Signature {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static byte[] calculateRFC2104HMAC(String data, String key)
        throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return mac.doFinal(data.getBytes());
    }
    
    public static byte[] calculateRFC2104HMAC(byte[] data, String key)
    throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
{
    SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
    Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
    mac.init(signingKey);
    return mac.doFinal(data);
}
    
    public static boolean checkSignature(String data, String signaure, String key)
    throws SignatureException, NoSuchAlgorithmException, InvalidKeyException
    {
        return signaure.equals(calculateRFC2104HMAC(data,key));
    }

    public static void main(String[] args) throws Exception {
        //String data = "H,13/155NS,MRC01123456,2,999999999,081217145219A,�Knjaz Milos 1.5l�,53.50,2,4,107.00T,0,0,0,9.73,0,0,0,0,0,0,9.73E,0,0,0,107.00,0,0,0,0,0,0,107.00P,107.00,0,0L,123456789012345";
        String data = "";
        byte[] data_byte = data.getBytes("UTF-8");
        String key = "1234567890";
        byte[] mac_bane = calculateRFC2104HMAC(data, key);
        byte[] hmac_bane_byte = calculateRFC2104HMAC(data_byte, key);
        byte[] hmac = calculateRFC2104HMAC("data", "key");

        System.out.println(hmac);
        assert hmac.equals("104152c5bfdca07bc633eebd46199f0255c9f49d");
//        assert hmac_bane.equals("39f72d142f53586c5737ce54e11ab3f2f504a431");
        assert hmac_bane_byte.equals("39f72d142f53586c5737ce54e11ab3f2f504a431");
 //       System.out.println("hmac_bane" + hmac_bane);
        System.out.println("hmac_bane" + hmac_bane_byte);
        System.out.println( checkSignature("data", "104152c5bfdca07bc633eebd46199f0255c9f49d", "key") );
    }
}
