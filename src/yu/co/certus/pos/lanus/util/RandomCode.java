package yu.co.certus.pos.lanus.util;

import java.util.Random;

public class RandomCode {

  public static void main(String[] args) {
    System.out.println(generateCode(32));
  }
/*
  public static String generateCode(int length) {
    String randomCode = null;
    Random random = new Random();
    long r1 = random.nextLong();
    long r2 = random.nextLong();
    String hash1 = Long.toHexString(r1);
    String hash2 = Long.toHexString(r2);

    randomCode = hash1 + hash2;
    randomCode = randomCode.substring(0, length);

    return randomCode;
  }
*/
  public static String generateCode(int length) {
                Random random = new Random();
                int len=0;
                long r=0;
                String hash="";
                while(len<length) {
                        r = random.nextLong();
                        hash+=Long.toHexString(r);
                        len = hash.length();
                }
                hash = hash.substring(0, length);

                return hash;
        }

}
