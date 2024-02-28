package yu.co.certus.pos.lanus.util;

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
public abstract class PaymentNumber {
  private String code;
        public PaymentNumber(String number)
        {
                code = number;
        }
        
        public String getCode(){
            return code;
        }
        /**
         * @return Returns the code.
         */
//        public String getCode()
//        {
//            return code;
//        }
        /**
         * @param code The code to set.
         */
//        public void setCode(String code)
//        {
//            this.code = code;
//        }
//        public abstract void parseNumber(String number);
//        public static PaymentNumber parseCode(String number)
//        {
//                PaymentNumber paymentNumber = null;
//                if (number.startsWith("0"))
//                        paymentNumber = new Phone();
//
//                paymentNumber.parseNumber(number);
//                return paymentNumber;
//        }

}
