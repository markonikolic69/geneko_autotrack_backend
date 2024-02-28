package yu.co.certus.pos.lanus.service;

import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;

///////////////////logging imports/////////////////////////////////////////
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import yu.co.certus.pos.geneco.util.IbuttonSmsQueueHandler;
import yu.co.certus.pos.lanus.concurrent.ProcessTaskEngine;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Service extends Thread{

  public static final Logger logger = Logger.getLogger("mainLogger");

  //////////////////changes for mutithreading ///////////////////////////////////
  private int _numberOfThreads = 0;
  /**
   * do not reference directly
   * use getTaskEngineSingleton() local method
   */
  private ProcessTaskEngine taskEngine = null;

/////////////////////end of changes////////////////////////////////////////////

  private ServerSocket server = null;

  private static Properties properties = new Properties();
  
  
  private boolean _is_geneco_dev_direct_client = true;
  
  public static final int ibuttonSmsQueueSize = 100;
  public static ArrayBlockingQueue<String> ibuttonSmsQueue;

  public Service() throws ServiceException {

    //System.out.print("Starting service on localhost. ");
    if (logger.isDebugEnabled()) {
      logger.debug("-->Service() - starting service on localhost");
    }

    initialize();

    if (logger.isDebugEnabled()) {
      logger.debug("<--Service()");
    }

  }

  protected void initialize() throws ServiceException {
    if (logger.isDebugEnabled()) {
      logger.debug("-->initialize() - initializing resources for Service: properties, socket, timer, and seting number of threads");
    }
    //initializeLogger();
    initializeProperties();
    initializeSocket();
    _is_geneco_dev_direct_client = Boolean.parseBoolean(properties.getProperty("is.geneco.device.direct.client", "true"));
//        initializeMaintenance();
//        initializeTimer();
    //set number of threads param and isThreaded flag
    setNumberOfThreads();
//		initializeResponces();

    ibuttonSmsQueue=new ArrayBlockingQueue<String>(ibuttonSmsQueueSize);
    
    try{
        Thread thread = new Thread(new IbuttonSmsQueueHandler());
        thread.start();
    }catch(Throwable e){
        logger.error("Unable to run IbuttonSmsQueueHandler thread, details: " + e.getMessage(), e);
    }
    //setFrame();
    if (logger.isDebugEnabled()) {
      logger.debug("<--initialize()");
    }
  }

  /**
   * @throws ServiceException
   */
  protected void initializeProperties() throws ServiceException {
    if (logger.isDebugEnabled()) {
      logger.debug("-->initializeProperties() - initializing app. properties from the property file");
    }

    try {
      FileInputStream stream = new FileInputStream(
          "application.properties");
      properties.load(stream);

      stream.close();
    }
    catch (java.io.FileNotFoundException fnfe) {
      if (logger.isDebugEnabled()) {
        logger.error("'FileNotFound' exception caught while trying to open application property file - " +
                     fnfe.getMessage());
      }
      System.exit( -1);
    }
    catch (IOException e) {
      if (logger.isDebugEnabled()) {
        logger.error(
            "'IOException' caught while reading application property file - " +
            e.getMessage());
      }
      throw new ServiceException(
          "Couldn't find or read from a configuration file");
    }

    if (logger.isDebugEnabled()) {
      logger.debug("<--initializeProperties()");
    }
  }

  /**
   * opens the port which is used to receive Datagram sockets from pos
   * terminals
   * @throws ServiceException
   */
  protected void initializeSocket() throws ServiceException {

    System.out.println("Host = " + properties.
            getProperty("Service.address"));

    if (logger.isDebugEnabled()) {
      logger.debug(
          "-->initializeSocket() - initializing socket for the port " +
          Integer.parseInt(properties.getProperty("Service.port")) + ", " +
          properties.
            getProperty("Service.address"));
    }



    if (server == null) {
      try {

        //open the socket
        server = new ServerSocket(Integer.parseInt(properties.
            getProperty("Service.port")),
                                  1000,
                                  InetAddress.getByName(
                                      properties.
            getProperty("Service.address")));
      }
      catch (java.net.UnknownHostException uhe) {
        if (logger.isDebugEnabled()) {
          logger.error(
              "Unknown host");
        }

        throw new ServiceException("Couldn't resolve the host");
      }

      catch (IOException e) {
        if (logger.isDebugEnabled()) {
          logger.error(
              "IOException when try to open a server socket, details:" +
              e.getMessage() + ", server will be shutdown");
        }

        System.exit( -1);
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("<--initializeSocket()");
    }
  }

  /**
   * main class function, listens the port on a local machine infinitelly and
   * initialise process of the request. Request contains information received
   * from the pos terminal. Information is stored in the DatagramPacket object
   *
   * @see java.net.DatagramPacket
   *
   * @throws PosException
   */
  private void listen() {

    if (logger.isDebugEnabled()) {
      logger.debug("-->listen() - accepting terminal requests");
    }

    Socket fromClient = null;

    if (server == null) {
      return;
    }

    while (true) {

      try {
        //server listens for the client sockets
        fromClient = server.accept();
        getTaskEngineSingleton().processRequest(fromClient);
      }
      catch (IOException e) {
        if (logger.isDebugEnabled())
          logger.error(
              "'IOException' caught while receiving socket, details:" +
              e.getMessage());

      }

    }
  }

  /**
     * starts the port listening
     */
    public void run() {

        if (logger.isDebugEnabled()) {
            logger.debug("-->");
        }

        try {

            listen();

        }  catch (Throwable te) {
            if (logger.isDebugEnabled()) {
                logger.error("Unknown exception (Throwable type) while starting the thread for a new terminal",
                             te);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("<--");
        }

    }


  protected ProcessTaskEngine getTaskEngineSingleton() {
    if (this.taskEngine == null) {
      taskEngine = new ProcessTaskEngine(
          _numberOfThreads, _is_geneco_dev_direct_client);
    }
    return taskEngine;
  }

  protected void setNumberOfThreads() {
    String threadProperty = System.getProperty("service.thread.number");

    if (threadProperty != null) {
      try {
        int threadNum = Integer.parseInt(threadProperty);
        if (threadNum > 200) {
          System.out.println(
              "usage: -Dservice.thread.number must be integer not greater then 200" +
              ", certus service is not threaded");
        }
        else {
          this._numberOfThreads = threadNum;
        }
      }
      catch (NumberFormatException nfe) {
        System.out.println(
            "usage: -Dservice.thread.number must be integer between 1 and 50 if present " +
            ", certus service is not threaded");
      }

    }
  }



  public static void main(String[] args) throws ServiceException {
    if (args.length == 1)
            PropertyConfigurator.configure(args[0]);

        if (logger.isDebugEnabled()) {
            logger.info("Program started");
        }

        Service service = new Service();

        service.start();

  }
}
