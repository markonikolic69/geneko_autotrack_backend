package yu.co.certus.pos.lanus.data;

import java.text.DecimalFormat;

import java.util.Date;


public class TransactionData
{
        private int id;
        private int sellerId;
        private String pointOfSaleName;


        ////////////////////////////INTERNET_IZMENE////////////////////////////
        //Podaci id, sellerId, pointOfSaleId, postId, ispMerchantId upisuju se
        //u tabelu internet_transactions.
        //ispMerchantId odgovara 'id' polju iz tebele 'contractor' i nazvan je
        //ovako samo zbog koriscenja slicnih naziva za Mobtel, Diners i pcoo
        private int _pointOfSaleId;
        private int postId;
        private String contractorName;
        private int ispMerchantId;
        //////////////////////INTERNET_IZMENE-kraj bloka//////////////////////


        private long dinersMerchantId;
        private int mobtelMerchantId;
        private int pcooMerchantId;

        private String mccCode = "";

        private int _status_id = 0;
        private String _stop_time = "";

        private Date _stop_time_as_date = new Date();
        private double _amount = 0;
        private long _qPaySpotNumber = 0;

        private int _contractor_id = 0;

         private String _point_of_sale_town = "Beograd";
         private String _point_of_sale_address = "";
         private String _point_of_sale_zip = "";

         private String _seller_name = "Administrator";

         private String _serial_number = "";


         private int _change_counter = 1;
         
         private boolean _additional_info_flag = false;
         
         private boolean _annoincement_exist_flag = false;
         
         
         private String _firmware_update_version = "";
         
         private String _cs_contractor_sifra_kupca = "0";
         public String get_cs_contractor_sifra_kupca() {
             return _cs_contractor_sifra_kupca;
         }

         public void set_cs_contractor_sifra_kupca(String _cs_contractor_sifra_kupca) {
             this._cs_contractor_sifra_kupca = _cs_contractor_sifra_kupca;
         }

         public boolean is_is_cs_subcontractor() {
             return !this._cs_contractor_sifra_kupca.equals("0");
         }


         private boolean _is_cs_subcontractor_in_debt = false;
         public boolean is_is_cs_subcontractor_in_debt() {
             return _is_cs_subcontractor_in_debt;
         }

         public void set_is_cs_subcontractor_in_debt(boolean _is_cs_subcontractor_in_debt) {
             this._is_cs_subcontractor_in_debt = _is_cs_subcontractor_in_debt;
         }

         private boolean _is_cs_subcontractor_valid = false;


         public boolean is_is_cs_subcontractor_valid() {
             return _is_cs_subcontractor_valid;
         }

         public void set_is_cs_subcontractor_valid(boolean _is_cs_subcontractor_valid) {
             this._is_cs_subcontractor_valid = _is_cs_subcontractor_valid;
         }




        public String get_firmware_update_version() {
            return _firmware_update_version;
        }
        public void set_firmware_update_version(String _firmware_update_version) {
            this._firmware_update_version = _firmware_update_version;
        }
        public boolean is_annoincement_exist_flag() {
            return _annoincement_exist_flag;
        }
        public void set_annoincement_exist_flag(boolean _annoincement_exist_flag) {
            this._annoincement_exist_flag = _annoincement_exist_flag;
        }
        public boolean is_additional_info_flag() {
            return _additional_info_flag;
        }
        public void set_additional_info_flag(boolean _additional_info_flag) {
            this._additional_info_flag = _additional_info_flag;
        }
        public void setDinersMerchantId(int merchantId)
        {
                dinersMerchantId = merchantId;
        }
        public long getDinersMerchantId()
        {
                return dinersMerchantId;
        }

        public void setChangeCounter(int counter){
          _change_counter = counter;
        }

        public int getChangeCounter(){
          return _change_counter;
        }
        public void setId(int transactionId)
        {
                this.id = transactionId;
        }
        public int getId()
        {
                return id;
        }

        public void setSerialNumber(String serNum){
          _serial_number = serNum;
        }

        public String getSerialNumber(){
          return _serial_number;
        }

        public String getFormatedId(){
            return new DecimalFormat("00000000000").format(id);
        }

        public void setQPaySpotNumber(long qPaySpotNumber){
            _qPaySpotNumber = qPaySpotNumber;
        }

        public long getQPaySpotNumber(){
            return _qPaySpotNumber;
        }

        public void setAmount(double amount){
            _amount = amount;
        }

        public double getAmount(){
            return _amount;
        }

        /**
         * @return Returns the sellerId.
         */
        public int getSellerId()
        {
            return sellerId;
        }

        /**
         * @return Returns the pointOfSaleName.
         */
        public String getPointOfSaleName()
        {
            return pointOfSaleName;
        }

        public void setPcooMerchantId(int id)
        {
                this.pcooMerchantId = id;
        }
        public int getPcooMerchantId()
        {
                return pcooMerchantId;
        }

        public void setMccCode(String code)
        {
                this.mccCode = code;
        }

        public String getMccCode()
        {
                return mccCode;
        }


        public void setMobtelMerchantId(int mobtelMerchantId)
        {
                this.mobtelMerchantId = mobtelMerchantId;
        }

        public int getMobtelMerchantId()
        {
                return mobtelMerchantId;
        }


        ////////////////////////////INTERNET_IZMENE////////////////////////////
        public TransactionData(int sellerId, String pointOfSaleName, int postId, String contractorName, long dinersMerchantId,
                int pointOfSaleId)
        {
                this.sellerId = sellerId;
                this.pointOfSaleName = pointOfSaleName;
                this.postId = postId;
                this.contractorName = contractorName;
                this.dinersMerchantId = dinersMerchantId;
                _pointOfSaleId = pointOfSaleId;
        }

        public TransactionData(int sellerId, String pointOfSaleName, int postId,
                               String contractorName,
                               int pointOfSaleId) {
            this(sellerId, pointOfSaleName, postId, contractorName, 0,
                 pointOfSaleId);
        }


        public int getPointOfSaleId() {
            return _pointOfSaleId;
        }


        /**
         * @return Returns the postId.
         */
        public int getPostId()
        {
                return postId;
        }

        public String getContractorName()
        {
                return contractorName;
        }



        public void setIspMerchantId(int ispMerchantId) {
            this.ispMerchantId = ispMerchantId;
        }

        public int getIspMerchantId() {
            return ispMerchantId;
        }

        public void setStopTime(String stop_time){
            _stop_time = stop_time;
        }

        public void setStopTimeAsDate(Date stop_time){
          _stop_time_as_date = stop_time;
        }

        public void setStatus_Id(int status_id){
            _status_id = status_id;
        }

        public String getStopTime(){
            return _stop_time;
        }

        public Date getStopTimeAsDate(){
          return _stop_time_as_date;
        }

        public int getStatusId(){
            return _status_id;
        }

        public int getContractorId(){
            return _contractor_id;
        }

        public void setContractorId(int contractorId){
            _contractor_id = contractorId;
        }

        public void setPointOfSaleTown(String town){
            _point_of_sale_town = town;
        }

        public void setPointOfSaleAddress(String address) {
          _point_of_sale_address = address;
        }

        public void setPointOfSaleZip(String zip_code) {
          _point_of_sale_zip = zip_code;
        }

        public void setSellerName(String sel_name){
          _seller_name = sel_name;
        }


        public String getPointOfSaleAddress() {
            if(_point_of_sale_address.length() > 25){
                return _point_of_sale_address.substring(0,24);
            }else{
                return _point_of_sale_address;
            }
            //return _point_of_sale_address;
        }

        public String getPointOfSaleTown() {
            return _point_of_sale_town;
        }

        public String getPointOfSaleZipCode() {
          return _point_of_sale_zip;
        }

        public String getSellerName(){
          return _seller_name;
        }

        public String toString() {
            return

                    " id = " + id +
                    " sellerId = " + sellerId +
                    " pointOfSaleId = " + _pointOfSaleId +
                    " pointOfSaleAddress = " + _point_of_sale_address +
                    " pointOfSaleTown = " + _point_of_sale_town +
                    " pointOfSaleZip = " + _point_of_sale_zip +
                    " pointOfSaleName = " + pointOfSaleName +
                    " postId = " + postId +
                    " seller_name = " + _seller_name +
                    " serial_number = " + _serial_number +
                    " change_counter = " + _change_counter +
                    " stop_time = " + _stop_time +
                    " contractorName = " + contractorName +
                    " dinersMerchantId = " + dinersMerchantId +
                    " mobtelMerchantId = " + mobtelMerchantId +
                    " pcooMerchantId = " + pcooMerchantId +
                    " ispMerchantId = " + ispMerchantId +
                    " is_is_cs_subcontractor = " + is_is_cs_subcontractor() +
                    " _is_cs_subcontractor_in_debt = " + _is_cs_subcontractor_in_debt +
                    " _is_cs_subcontractor_valid = " + _is_cs_subcontractor_valid +
                    " mccCode = " + mccCode;
        }
        //////////////////////INTERNET_IZMENE-kraj bloka//////////////////////


        private static final int MAX_POINT_OF_SALE_ADDRESS_LENGTH = 25;
}
