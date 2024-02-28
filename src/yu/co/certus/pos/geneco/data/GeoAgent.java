package yu.co.certus.pos.geneco.data;

public class GeoAgent
{
    private String _streetNumber;
    private String _community;
    private String _city;
    private String _state;
    private double _distance;
    private double _direction;
    private double _angleRt;
    private double _latitude;
    private double _longitude;
    private int _timeZone;
    private String _fix;



    public GeoAgent()
    {

    }

    public void setDistance(double distance)
    {


        //distancu postavljamo u kilometrima
        _distance= distance;

    }

    public double getDistance()
    {
        return _distance;
    }

    public void setStreetNumber(String streetNumber)
    {
        _streetNumber=streetNumber;
    }

    public String getStreetNumber()
    {
        return _streetNumber;
    }

    public void setCommunity(String community)
    {
        _community=community;
    }

    public String getCommunity()
    {
        return _community;
    }

    public void setCity(String city)
    {
        _city=city;
    }

    public String getCity()
    {
        return _city;
    }

    public void setState(String state)
    {
        _state=state;
    }

    public String getState()
    {
        return _state;
    }

    public void setLatitude(double latitude)
    {
        _latitude= latitude;   
    }

    public double getLatitude()
    {
        return _latitude;
    }

    public void setLongitude(double longitude)
    {
        _longitude= longitude;   
    }

    public double getLongitude()
    {
        return _longitude;
    }

    public void setAngleRt(double angle)
    {
        _angleRt= angle;   
    }

    public double getAngleRt()
    {
        return _angleRt;
    }


    public void setTimeZone(int zone)
    {
        _timeZone= zone;   
    }

    public int getTimeZone()
    {
        return _timeZone;
    }

    public void setDirection(double direction)
    {
        _direction= direction;   
    }

    public double getDirection()
    {
        return _direction;
    }

    public void setFix(String fix)
    {
        _fix=fix;
    }

    public String getFix()
    {
        return _fix;
    }

    public String toString() {
        return 

        "_streetNumber = " + _streetNumber + ", " +
        "_community = " + _community + ", " +
        "_city = " + _city + ", " +
        "_state = " + _state + ", " +
        "_distance = " + _distance + ", " +
        "_direction = " + _direction + ", " +
        "_angleRt = " + _angleRt + ", " +
        "_latitude = " + _latitude + ", " +
        "_longitude = " + _longitude + ", " +
        "_timeZone = " + _timeZone + ", " +
        "_fix = " + _fix;

    }


}

