package yu.co.certus.pos.lanus.concurrent;

import EDU.oswego.cs.dl.util.concurrent.BoundedLinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

import java.net.Socket;

import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;

import yu.co.certus.pos.lanus.service.Service;

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
public class ProcessTaskEngine {
  private BoundedLinkedQueue channel = null;

  private PooledExecutor executor = null;

  private BoundedLinkedQueue _overloadChannel = null;
  private PooledExecutor _overloadExecutor = null;



  private OverloadHisteresis _overHist = new OverloadHisteresis();
  
  private boolean _is_genecodev_direct_client = true;

  public ProcessTaskEngine(int numberOfThreads, boolean is_genecodev_direct_client) {
      _is_genecodev_direct_client = is_genecodev_direct_client;
    channel = new BoundedLinkedQueue();

//        executor = new PooledExecutor(channel, numberOfThreads);
    executor = new PooledExecutor(channel, Math.max(600, 2 * numberOfThreads));
    executor.setMaximumPoolSize(Math.max(600, 2 * numberOfThreads));
    executor.setMinimumPoolSize(numberOfThreads);
//        executor.setKeepAliveTime( -1);

//        executor.createThreads(numberOfThreads);

    _overloadChannel = new BoundedLinkedQueue();
    _overloadExecutor = new PooledExecutor(_overloadChannel, 5);
    _overloadExecutor.setMaximumPoolSize(5);
    _overloadExecutor.setMinimumPoolSize(5);


  }

  public OverloadHisteresis getOverHist() {
    return _overHist;
  }

  public void processRequest(Socket socket) {

    if (Service.logger.isDebugEnabled()) {
      Service.logger.debug("--> socket = " + socket);
    }
    try {

      if (channel.size() > 0) {
        if (Service.logger.isDebugEnabled()) {
          Service.logger.info("Thread queue size is " + channel.size());
        }
      }
      if (Service.logger.isDebugEnabled()) {
        Service.logger.info("Pool size is " + executor.getPoolSize());
      }







 //     ProcessingTask currentTask = new ProcessingTask(socket);
      
      ProcessingTaskGeneco currentTask = new ProcessingTaskGeneco(socket, _is_genecodev_direct_client);

      //override operation type to OperationType.systemOverload if
      // overloaded condition is fullfiled
      if (isSystemOverloaded()) {

        //execute with overload executor
        _overloadExecutor.execute(currentTask);
      }
      else {

         executor.execute(currentTask);
      }
    }
    catch (InterruptedException ie) {
      if (Service.logger.isDebugEnabled()) {
        Service.logger.error("InterruptedException , details = " +
                             ie.getMessage(), ie);
      }
    }
    if (Service.logger.isDebugEnabled()) {
      Service.logger.debug("<--");
    }
  }

  private boolean isSystemOverloaded() {
//        System.out.println("CHANNEL SIZE = " + channel.size() +
//                ", IS_OVERLOADED = " + _overHist.shouldBeBlocked(channel.size()));
//    return _overHist.shouldBeBlocked(channel.size());
      return false;
  }

  public class OverloadHisteresis {

    private int _threashold = 0;
    private int _histeresis_loop_width = 0;

    private boolean _previousStateIsBlocked = false;
    private boolean _stateIsBlocked = false;

    public OverloadHisteresis() {

      Properties properties = new Properties();
      try {
        FileInputStream stream = new FileInputStream(
            "application.properties");
        properties.load(stream);
        _threashold = Integer.parseInt(properties.getProperty(
            "histeresis.threashold", "200"));
        _histeresis_loop_width = Integer.parseInt(properties.getProperty(
            "histeresis.loop.width", "20"));
        stream.close();
      }
      catch (IOException e) {
        if (Service.logger.isDebugEnabled()) {
          Service.logger.info(
              "IOException when try to read application properties, details : " +
              e.getMessage() +
              ", histeresis will be set on default values");
        }

      }
      catch (Throwable e) {
        if (Service.logger.isDebugEnabled()) {
          Service.logger.info(
              "Unknown exception when try to read application properties, details :" +
              e.getMessage() +
              ", histeresis will be set on default values");
        }

      }

    }

    public boolean shouldBeBlocked(int input) {
      boolean toReturn = _previousStateIsBlocked;
      setStates(input);
      return toReturn;
    }

    private void setStates(int input) {
      //boolean tmp = _stateIsBlocked;

      if (_previousStateIsBlocked) {
        //previous state is blocked
        _stateIsBlocked = input >= _threashold - _histeresis_loop_width;
      }
      else {
        //previous state is not blocked
        _stateIsBlocked = input >= _threashold + _histeresis_loop_width;
      }
      _previousStateIsBlocked = _stateIsBlocked;
    }

  }

}
