package yu.co.certus.pos.geneco.data;

public class UnitData {
    
    
    private String _serialNumber = "";
    private String _current_driver = "";
    private Integer _current_driver_id = null;
    public String get_serialNumber() {
        return _serialNumber;
    }
    public void set_serialNumber(String number) {
        _serialNumber = number;
    }
    public String get_current_driver() {
        return _current_driver;
    }
    public void set_current_driver(String _current_driver) {
        this._current_driver = _current_driver;
    }
    public Integer get_current_driver_id() {
        return _current_driver_id;
    }
    public void set_current_driver_id(Integer _current_driver_id) {
        this._current_driver_id = _current_driver_id;
    }
    
    
    public String toString(){
        return "_serialNumber = " + _serialNumber + ", " +
        "_current_driver = " + _current_driver + ", " +
        "_current_driver_id = " + _current_driver_id;
    }
    

}
