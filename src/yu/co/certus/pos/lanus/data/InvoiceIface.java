package yu.co.certus.pos.lanus.data;






import java.util.Date;

import java.sql.SQLException;

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
public interface InvoiceIface extends BaseDBIface{

    public InvoiceData getInvoice(int pointId, Date invoiceDateFrom,
                                  Date invoiceDateTo) throws NoInvoiceDataException,
            SQLException;


}
