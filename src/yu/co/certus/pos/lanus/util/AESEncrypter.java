package yu.co.certus.pos.lanus.util;


import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


//import javax.crypto.Cipher;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Formatter;
import java.util.Map;

import java.security.*;
 
public class AESEncrypter {
 
//    private static final byte[] SALT = {
//        (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
//        (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
//    };
//    private static final int ITERATION_COUNT = 65536;
//    private static final int KEY_LENGTH = 256;
//    private Cipher ecipher;
//    private Cipher dcipher;
    
    
    private static final String ALGORITHM = "AES";
//    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] keyValue =
            new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f };
    
    public static String encryptSimple(String valueToEnc) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        byte[] first_16 = {
                encValue[0], encValue[1], encValue[2], encValue[3],
                encValue[4], encValue[5], encValue[6], encValue[7],
                encValue[8], encValue[9], encValue[10], encValue[11],
                encValue[12], encValue[13], encValue[14], encValue[15]};
        String encryptedValue = new BASE64Encoder().encode(first_16);
        return encryptedValue;
    }
    
    public static String encryptSimple(byte[] valueToEnc) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = c.doFinal(valueToEnc);
System.out.println(toHexString(encValue));
byte[] first_16 = {
        encValue[0], encValue[1], encValue[2], encValue[3],
        encValue[4], encValue[5], encValue[6], encValue[7],
        encValue[8], encValue[9], encValue[10], encValue[11],
        encValue[12], encValue[13], encValue[14], encValue[15]};
        String encryptedValue = new BASE64Encoder().encode(first_16);
        return encryptedValue;
    }
    
    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }
 
    public static String decryptSimple(String encryptedValue) throws Exception {
        System.out.println(encryptedValue);
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue 
                = new BASE64Decoder().decodeBuffer(encryptedValue);
        System.out.println(toHexString(decordedValue));
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
    
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        // SecretKeyFactory keyFactory 
        //              = SecretKeyFactory.getInstance(ALGORITHM);
        // key = keyFactory.generateSecret(new DESKeySpec(keyValue));
        return key;
    }
   
//    AESEncrypter(String passPhrase) throws Exception {
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
//        SecretKey tmp = factory.generateSecret(spec);
//        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
// 
//        ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        ecipher.init(Cipher.ENCRYPT_MODE, secret);
//       
//        dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        byte[] iv = ecipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
//        dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
//    }
    
//    AESEncrypter(char[] passPhrase) throws Exception {
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        KeySpec spec = new PBEKeySpec(passPhrase, SALT, ITERATION_COUNT, KEY_LENGTH);
//        SecretKey tmp = factory.generateSecret(spec);
//        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
// 
//        ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        ecipher.init(Cipher.ENCRYPT_MODE, secret);
//       
//        dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        byte[] iv = ecipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
//        dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
//    }
 
//    public String encrypt(String encrypt) throws Exception {
//        byte[] bytes = encrypt.getBytes("UTF8");
//        byte[] encrypted = encrypt(bytes);
//        return new BASE64Encoder().encode(encrypted);
//    }
 
//    public byte[] encrypt(byte[] plain) throws Exception {
//        return ecipher.doFinal(plain);
//    }
 
//    public String decrypt(String encrypt) throws Exception {
//        byte[] bytes = new BASE64Decoder().decodeBuffer(encrypt);
//        byte[] decrypted = decrypt(bytes);
//        return new String(decrypted, "UTF8");
//    }
 
//    public byte[] decrypt(byte[] encrypt) throws Exception {
//        return dcipher.doFinal(encrypt);
//    }
    
    
//    public static void fixKeyLength() {
//        String errorString = "Failed manually overriding key-length permissions.";
//        int newMaxKeyLength;
//        try {
//            if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
//                Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
//                Constructor con = c.getDeclaredConstructor();
//                con.setAccessible(true);
//                Object allPermissionCollection = con.newInstance();
//                Field f = c.getDeclaredField("all_allowed");
//                f.setAccessible(true);
//                f.setBoolean(allPermissionCollection, true);
//
//                c = Class.forName("javax.crypto.CryptoPermissions");
//                con = c.getDeclaredConstructor();
//                con.setAccessible(true);
//                Object allPermissions = con.newInstance();
//                f = c.getDeclaredField("perms");
//                f.setAccessible(true);
//                ((Map) f.get(allPermissions)).put("*", allPermissionCollection);
//
//                c = Class.forName("javax.crypto.JceSecurityManager");
//                f = c.getDeclaredField("defaultPolicy");
//                f.setAccessible(true);
//                Field mf = Field.class.getDeclaredField("modifiers");
//                mf.setAccessible(true);
//                mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
//                f.set(null, allPermissions);
//
//                newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
//            }
//        } catch (Exception e) {        
//            throw new RuntimeException(errorString, e);
//        }
//        if (newMaxKeyLength < 256)
//            throw new RuntimeException(errorString); // hack failed
//    }
    
    
    public static void main(String[] args) throws Exception {
 //fixKeyLength();
        String message = "OFDC00A123456";
        //byte[] messageChar = {0x4f, 0x46, 0x44, 0x43, 0x30, 0x30, 0x41, 0x12, 0x34, 0x56, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00};
        
        byte[] messageChar = {0x4f, 0x46, 0x44, 0x43, 0x30, 0x30, 0x31, 0x12, 0x34, 0x56, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00};
        
        String password = "PASSWORD";
        System.out.println("passphreze length = " + password.toCharArray().length);
        char[] key = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f};
        System.out.println("passphreze length 2 = " + key.length);
        //AESEncrypter encrypter = new AESEncrypter(password);
        AESEncrypter encrypter = new AESEncrypter();
//        AESEncrypter encrypter = new AESEncrypter(key);
//        String encrypted = encrypter.encrypt(message);
//        String decrypted = encrypter.decrypt(encrypted);
        
        
        String encrypted = encryptSimple(messageChar);
  //      String decrypted = decryptSimple(encrypted);
        
 
        System.out.println("Encrypt(\"" + message + "\", \"" + password + "\") = \"" + encrypted + "\"");
 //       System.out.println("Decrypt(\"" + encrypted + "\", \"" + password + "\") = \"" + decrypted + "\"");
    }
}
