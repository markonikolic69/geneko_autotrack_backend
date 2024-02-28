package yu.co.certus.pos.lanus.data;

public class MobtelResponse {
    private String responseCode;

        private String authIdentResponse;

        private String _telenor_transaction_id ;

        /**
         * @return Returns the authIdentResponse.
         */
        public String getAuthIdentResponse() {
                return authIdentResponse;
        }

        /**
         * @param authIdentResponse
         *            The authIdentResponse to set.
         */
        public void setAuthIdentResponse(String authIdentResponse) {
                this.authIdentResponse = authIdentResponse;
        }

        /**
         * @return Returns the responseCode.
         */
        public String getResponseCode() {
                return responseCode;
        }

        /**
         * @param responseCode
         *            The responseCode to set.
         */
        public void setResponseCode(String responseCode) {
                this.responseCode = responseCode;
        }

        public String getTelenorTransactionId(){
            return _telenor_transaction_id;
        }

        public void setTelenorTransactionId(String transactionId){
            _telenor_transaction_id = transactionId;
        }



        public String toString(){

            return "responseCode = " + responseCode +
                    ", authIdentResponse = " + authIdentResponse +
                    ", _telenor_transaction_id = " + _telenor_transaction_id;
        }

}
