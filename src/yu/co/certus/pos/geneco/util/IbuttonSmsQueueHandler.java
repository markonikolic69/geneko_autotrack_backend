package yu.co.certus.pos.geneco.util;


import yu.co.certus.pos.lanus.service.Service;

import org.apache.log4j.Logger;

public class IbuttonSmsQueueHandler implements Runnable{

    Logger logger = Service.logger;
    private String _content;

    public void run()
    {

        while (!Thread.currentThread().isInterrupted()) {
            try {

                _content = Service.ibuttonSmsQueue.take();

                String arr[]=_content.split("_");
                String phone=arr[0];
                String message=arr[1];

                logger.info(
                        "sms take from queue - "
                        + phone);

                ComtradeSmsSender smsSender=new ComtradeSmsSender(phone, message);
                smsSender.send();



            } 
            catch (InterruptedException itre) {
                if (logger.isDebugEnabled()) {
                    logger.error(
                            "'InterruptedException' - while taking mesage from queue - "
                            + itre.getMessage());
                }
            }catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.error(
                            "'Exception' - when handling sms message - "
                            + e.getMessage());
                }
            }catch(Throwable t){
                if (logger.isDebugEnabled()) {
                    logger.error("throwable on sms  handler - " + t.getMessage());
                }

            }


        }     


    }


}
