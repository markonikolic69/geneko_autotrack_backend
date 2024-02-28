package yu.co.certus.pos.lanus.message;

public class AnnouncementResponse extends AbstractResponse{
    

    
    
    public boolean isSuccessful(){
        return getResponseCode().startsWith(ANN_SUCCESSFUL);
    }
    
    
    public static final String ANN_SUCCESSFUL = "19 ";
    public static final String NO_ANNOUNCEMENT_ERROR = "29 Announcement does not exist";
    public static final String MPOS_NOT_REGISTERED_ERROR = "28 mPOS is not registered";




}
