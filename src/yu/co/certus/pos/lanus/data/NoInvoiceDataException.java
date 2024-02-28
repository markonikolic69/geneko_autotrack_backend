package yu.co.certus.pos.lanus.data;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author marko nikolic (marko.nikolic@certus.co.yu)
 * @version 1.0
 */
public class NoInvoiceDataException extends Exception {
    public NoInvoiceDataException(int pointId, String from, String to) {
        super("No invoice data for point of sale = " + pointId + " from date " +
              from + " to date " + to);
    }
}
