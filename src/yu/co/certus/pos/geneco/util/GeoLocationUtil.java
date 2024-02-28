package yu.co.certus.pos.geneco.util;

import java.math.BigDecimal;

public class GeoLocationUtil {
    
    public static double getNonFormatedValue(double format_value){
        
      //correct long non formatted
        BigDecimal bigDecimalLon = new BigDecimal(String.valueOf(format_value));
         
        int intValueLon = bigDecimalLon.intValue();

        String decimalPartLon= bigDecimalLon.subtract(new BigDecimal(intValueLon)).toPlainString();
        
        int negativeLongitude=(int) (format_value/Math.abs(format_value));
         
        String lonPrefix="";
        if(Math.abs(new Double(decimalPartLon)*new Double(60))<10){
            lonPrefix="0";
        }
        
        return new Double(negativeLongitude) *new Double(Math.abs(intValueLon) + lonPrefix + "" + 
                String.format("%.12f", Math.abs(new Double(decimalPartLon)*new Double(60))));     
        
        
//        BigDecimal bigDecimalLat = new BigDecimal(String.valueOf(_gpsLat));
//        int intValueLat = bigDecimalLat.intValue();
//
//        logger.debug("intValueLat " + intValueLat );
//        int negativeLatitude=(int) (_gpsLat/Math.abs(_gpsLat));
//         
//          logger.debug("negative " + negativeLatitude );
//        
//
//        String decimalPartLat= bigDecimalLat.subtract(new BigDecimal(intValueLat)).toPlainString();
//         String latPrefix="";
//        if(Math.abs(new Double(decimalPartLat)*new Double(60))<10){
//            latPrefix="0";
//        }
//        
//        
//        
//        
//        _gpsLatNonFormated=new Double(negativeLatitude) *new Double(Math.abs(intValueLat) + latPrefix + "" + String.format("%.12f",Math.abs(new Double(decimalPartLat)*new Double(60))));     


        
    }
    
    public static void main(String[] args){
        System.out.println(getNonFormatedValue(20.465995));
    }

}
