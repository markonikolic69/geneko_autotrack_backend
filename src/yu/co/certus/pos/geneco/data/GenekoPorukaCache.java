package yu.co.certus.pos.geneco.data;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



public class GenekoPorukaCache {
    
    private static Map<String, Calendar> _cache = new HashMap<String, Calendar>();
    
    public static Calendar getHashedPorukaGsmDateTime(String unit_id){
        return _cache.get(unit_id) == null ? null : (Calendar)_cache.get(unit_id);
    }
    
    public static void put(String unit_id, Calendar date){
        _cache.put(unit_id, date);
    }

}
