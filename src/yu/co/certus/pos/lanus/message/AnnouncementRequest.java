package yu.co.certus.pos.lanus.message;

public class AnnouncementRequest extends AbstractRequest {
    
    private boolean _isOld = false;
    
    public boolean is_isOld() {
        return _isOld;
    }

    public void set_isOld(boolean old) {
        _isOld = old;
    }

    public String toString(){
        return "ANNOUNCEMENT REQUEST: terminal_id = " + getUserId() ;
    }

}
