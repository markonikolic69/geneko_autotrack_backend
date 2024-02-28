package yu.co.certus.pos.lanus.data;

/**
 * @author naum
 *
 */
public class BalanceData
{
        private int daily;
        private int weekly;
        private int monthly;
        private boolean alertSent;

        private int dailyLimit;
        private int weeklyLimit;
        private int monthlyLimit;

        private String pointOfSalePhone;
        private boolean sendToTelekom;

        private int terminalId;
        public BalanceData(int terminalId, int daily, int weekly, int monthly, boolean alertSent, int dailyLimit, int weeklyLimit, int monthlyLimit, String pointOfSalePhone, boolean sendToTelekom)
        {
                this.terminalId = terminalId;

                this.daily = daily;
                this.weekly = weekly;
                this.monthly = monthly;
                this.alertSent = alertSent;

                this.dailyLimit = dailyLimit;
                this.weeklyLimit = weeklyLimit;
                this.monthlyLimit = monthlyLimit;

                this.pointOfSalePhone = pointOfSalePhone;
                this.sendToTelekom = sendToTelekom;
        }
        /**
         * @return Returns the daily.
         */
        public int getDaily()
        {
                return daily;
        }
        /**
         * @return Returns the weekly.
         */
        public int getWeekly()
        {
                return weekly;
        }
        /**
         * @return Returns the monthly.
         */
        public int getMonthly()
        {
                return monthly;
        }
        /**
         * @return Returns the alertSent.
         */
        public boolean isAlertSent()
        {
                return alertSent;
        }
        /**
         * @return Returns the dailyLimit.
         */
        public int getDailyLimit()
        {
                return dailyLimit;
        }
        /**
         * @return Returns the weeklyLimit.
         */
        public int getWeeklyLimit()
        {
                return weeklyLimit;
        }
        /**
         * @return Returns the monthlyLimit.
         */
        public int getMonthlyLimit()
        {
                return monthlyLimit;
        }
        /**
         * @return Returns the pointOfSalePhone.
         */
        public String getPointOfSalePhone()
        {
                return pointOfSalePhone;
        }
        /**
         * @return Returns the sendToTelekom.
         */
        public boolean isSendToTelekom()
        {
                return sendToTelekom;
        }
        /**
         * @return Returns the terminalId.
         */
        public int getTerminalId()
        {
                return terminalId;
        }

        public String toString() {
            return "daily = " + daily +
                    " weekly = " + weekly +
                    " monthly = " + monthly +
                    " alertSent = " + alertSent +

                    " dailyLimit = " + dailyLimit +
                    " weeklyLimit = " + weeklyLimit +
                    " monthlyLimit = " + monthlyLimit +

                    " pointOfSalePhone = " + pointOfSalePhone +
                    " sendToTelekom = " + sendToTelekom +

                    " terminalId = " + terminalId;

        }
}
