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
public class Phone extends PaymentNumber{
  private String number;
  
  //private static boolean _isMTS = false;
  //private static boolean _isVIP = false;
  //private static boolean _isStartedWithPrefix = false;
  
        private String getNumber()
        {
                return number;
        }
        private String areaPrefix = "64";
        private void setAreaPrefix(String prefix)
        {
                if (prefix.startsWith("0"))
                        areaPrefix = prefix.substring(1);
                else
                        areaPrefix = prefix;
        }
        private String countryPrefix = "381";
        private void setCountryPrefix(String prefix)
        {
                if (prefix.startsWith("+"))
                        countryPrefix = prefix.substring(1);
                countryPrefix = prefix;
        }
        public String getAreaPrefix()
        {
                return "0" + areaPrefix;
        }
        private Phone(String number)
        {
                super(number);
                this.number = number;
        }
//        public Phone()
//        {
//                super(null);
//        }
        private void parseNumber(String number)
        {
                if (number.startsWith("+"))
                {
                        setNumber(number.substring(6));
                        setAreaPrefix(number.substring(4, 6));
                        setCountryPrefix(number.substring(1, 4));
                }
                if (number.startsWith("381"))
                {
                        setNumber(number.substring(5));
                        setAreaPrefix(number.substring(3, 5));
                        setCountryPrefix(number.substring(0, 3));
                }
                else if (number.startsWith("0"))
                {
                        setNumber(number.substring(3));
                        setAreaPrefix(number.substring(0, 3));
                }
        }
        private void setNumber(String number)
        {
                this.number = number;
        }
        public static Phone parse(String number)
        {
            String nmbr = number;
            if(number.startsWith("1")){
                //_isMTS = true;
                nmbr = number.substring(1);
                //_isStartedWithPrefix = true;
            }
            if(number.startsWith("2")){
                //_isVIP = true;
                nmbr = number.substring(1);
                //_isStartedWithPrefix = true;
            }
                Phone phone = new Phone(nmbr);
                phone.parseNumber(nmbr);
                return phone;
        }
        /**
         * @return number with area prefix
         */
        public String getAreaNumber()
        {
                return getAreaPrefix() + getNumber();
        }
        /**
         * @return full number, without prepending '+'
         */
        public String getFull()
        {
                return getFull(false);
        }
        /**
         * @param prependPlus
         * @return full number
         */
        public String getFull(boolean prependPlus)
        {
                if (prependPlus)
                        return "+" + getCountryPrefix() + getAreaNumber().substring(1);
                return getCountryPrefix() + getAreaNumber().substring(1);
        }
        /**
         * @return
         */
        public String getCountryPrefix()
        {
                return countryPrefix;
        }
        /**
         * @return
         */
        public boolean isTest()
        {
                return getAreaNumber().equals("0640000000");
        }

        public boolean equals(Object o){
            if(o instanceof Phone){
                return ((Phone)o).getNumber().equals(getNumber());
            }else{
                return false;
            }
        }

//        public boolean isMTS(){
//            if(_isStartedWithPrefix){
//                return _isMTS;
//            }else{
//                return getAreaPrefix().equals(MTS_PREFIX_1) || getAreaPrefix().equals(MTS_PREFIX_2)
//                 || getAreaPrefix().equals(MTS_PREFIX_3);
//            }
//            
//        }

//        public boolean isVIP(){
//            if(_isStartedWithPrefix){
//                return _isVIP;
//            }else{
//                return getAreaPrefix().equals(VIP_PREFIX_1) || getAreaPrefix().equals(VIP_PREFIX_2);
//            }
//        }



//        private static final String MTS_PREFIX_1 = "064";
//        private static final String MTS_PREFIX_2 = "065";
//        private static final String MTS_PREFIX_3 = "066";
//
//
//        private static final String VIP_PREFIX_1 = "060";
//        private static final String VIP_PREFIX_2 = "061";

        public String toString(){
          return getFull(false) /*+ ", isMts = " + isMTS() +
              ", isVIP = " + isVIP()*/;
        }

}
