/*
 * Created on Jun 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 *
 *
 * Marko Nikolic - Bug fix (Shomy found) - method fromInt, nedostaje case 6 =
 * SERVICE_PREPAID_DOPUNA_MOBTEL
 */
package yu.co.certus.pos.lanus.util;




/**
 * @author Aleksa
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServiceData {
    public static final ServiceData SERVICE_PREPAID_DOPUNA_TELEKOM = new ServiceData(1, "Prepaid dopuna - Telekom");
    //public static final ServiceData SERVICE_DOPUNA063=new ServiceData(1,"dopuna063");
    public static final ServiceData SERVICE_ROBE_I_USLUGE = new ServiceData(2, "Placanje roba i usluga");
    public static final ServiceData SERVICE_CACHE_PLACANJE = new ServiceData(3, "Cache placanje");
    public static final ServiceData SERVICE_MOBILNO_PLACANJE = new ServiceData(4, "Mobilno placanje");
    public static final ServiceData SERVICE_DINERS = new ServiceData(5, "Diners");
    public static final ServiceData SERVICE_PREPAID_DOPUNA_MOBTEL = new ServiceData(6, "Prepaid dopuna - Mobtel");

    public static final ServiceData SERVICE_Q_PAY_SPOT_INTERNET = new ServiceData(8, "QPay Spot - Internet");
    public static final ServiceData SERVICE_Q_PAY_SPOT_INFOSTAN = new ServiceData(9, "QPay Spot - Infostan");
    public static final ServiceData SERVICE_Q_PAY_VIRTUAL_CARD = new ServiceData(10, "QPay Spot - Virtual Card");

    public static final ServiceData SERVICE_DINA_ROBE_I_USLUGE = new ServiceData(11, "Dina card - Placanje roba i uluga");
    public static final ServiceData SERVICE_DINA_PODIZANJE_GOTOVINE = new ServiceData(12, "Dina card - Podizanje gotovine");

    public static final ServiceData SERVICE_PREPAID_DOPUNA_MOBILKOM = new ServiceData(13, "Prepaid dopuna - Mobilkom");

    public static final ServiceData SERVICE_TERMINAL_RENT = new ServiceData(35, "TERMINAL RENT");
    
    public static final ServiceData SERVICE_INTERNET_ABATEL = new ServiceData(16, "ABATEL - Internet");
    
    public static final ServiceData SERVICE_MUNDIO_DOPUNA = new ServiceData(24, "Prepaid dopuna - Mundio");
    
    public static final ServiceData SERVICE_GLOBALTEL_DOPUNA = new ServiceData(25, "Prepaid dopuna - Globaltel");

    
    private int serviceId;
    private String serviceName;
    private ServiceData(int id, String name){
        serviceId=id;
        serviceName=name;
    }

    public ServiceData fromInt(int id) {
      switch (id) {
        case 1:
          return ServiceData.SERVICE_PREPAID_DOPUNA_TELEKOM;
        case 2:
          return ServiceData.SERVICE_ROBE_I_USLUGE;
        case 3:
          return ServiceData.SERVICE_CACHE_PLACANJE;
        case 4:
          return ServiceData.SERVICE_MOBILNO_PLACANJE;
        case 5:
          return ServiceData.SERVICE_DINERS;
        case 6: //bug fix, Shomy found
          return ServiceData.SERVICE_PREPAID_DOPUNA_MOBTEL;
        case 8:
          return ServiceData.SERVICE_Q_PAY_SPOT_INTERNET;
        case 9:
          return SERVICE_Q_PAY_SPOT_INFOSTAN;
        case 10:
          return SERVICE_Q_PAY_VIRTUAL_CARD;
        case 11:
          return SERVICE_DINA_ROBE_I_USLUGE;
        case 12:
          return SERVICE_DINA_PODIZANJE_GOTOVINE;
        case 13:
          return SERVICE_PREPAID_DOPUNA_MOBILKOM;
        case 16:
            return SERVICE_INTERNET_ABATEL;

        default:
          System.out.println("Unknown service code received, " +
                             "received " + id +
                             ", must be between 1 and 16 , 7 not");
      }
      return null;
    }

    public static Integer integervalue(ServiceData serviceData){
        return new Integer(serviceData.getServiceId());
    }
    /**
     * @return Returns the serviceId.
     */
    public int getServiceId() {
        return serviceId;
    }
    /**
     * @param serviceId The serviceId to set.
     */
//    public void setServiceId(int serviceId) {
//        this.serviceId = serviceId;
//    }
    /**
     * @return Returns the servicename.
     */
    public String getServiceName() {
        return serviceName;
    }
    /**
     * @param servicename The servicename to set.
     */
//    public void setServicename(String serviceName) {
//        this.serviceName = serviceName;
//    }

    public String toString(){
        return "serviceName = " + serviceName + " serviceId = " + serviceId;
    }

    public boolean equals(Object o){
        if(o instanceof ServiceData){
            return ((ServiceData)o).getServiceId() == getServiceId();
        }else{
            return false;
        }
    }

}
