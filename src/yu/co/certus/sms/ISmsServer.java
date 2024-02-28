package yu.co.certus.sms;

import java.rmi.*;

public interface ISmsServer extends Remote {

	public void sendMessage(String number, String message) throws RemoteException;
	public void exit() throws RemoteException;
	
}
