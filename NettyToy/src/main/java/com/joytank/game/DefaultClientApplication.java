package com.joytank.game;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.joytank.net.Consts;
import com.joytank.net.JoinRequest;
import com.joytank.net.JoinResponse;
import com.joytank.net.PingMsg;

/**
 * 
 */
public class DefaultClientApplication extends AbstractApplication {

  private static final Logger logger = Logger.getLogger(DefaultClientApplication.class);

  protected final SocketAddress serverAddress;
  
  protected int clientId;
  protected int pingValue;
  
  protected volatile boolean isPingServer = false;

  /**
   * 
   * 
   * @param serverHost @Nonnull server host name
   * @param serverPort server port number
   */
  public DefaultClientApplication(String serverHost, int serverPort) {
    super();
    Preconditions.checkState(!StringUtils.isBlank(serverHost), "serverHost is unexpectedly blank or null.");
    this.serverAddress = new InetSocketAddress(serverHost, serverPort);
  }

  @Override
  protected void initAll() {
  }

  @Override
  protected void handleMessage(Object msg) {
    if (msg instanceof PingMsg) {
      handlePingMsg((PingMsg) msg);
    }
    if (msg instanceof JoinRequest) {
    	handleJoinResponse((JoinResponse) msg);
    }
  }

  private void handlePingMsg(PingMsg msg) {
    long dTime = System.nanoTime() - msg.getTimestamp();
    pingValue = (int) (dTime / 1000000);
  }

  private void handleJoinResponse(JoinResponse msg) {
  	
  }
  
  /**
   * Stop pinging the server
   */
  public void stopPingingServer() {
    isPingServer = false;
  }

  /**
   * Start to ping the server on a timely basis
   */
  public void startPingingServer() {
    if (isPingServer) {
      logger.info("Pinging daemon thread is already running.");
      return;
    }

    ExecutorService exec = Executors.newFixedThreadPool(1, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      }
    });
    exec.execute(new PingTask());
  }

  /**
   * 
   */
  private class PingTask implements Runnable {
    @Override
    public void run() {
      try {
        isPingServer = true;
        while (isPingServer) {
          PingMsg pingMsg = new PingMsg(clientId, System.nanoTime());
          udpComponent.sendMsg(pingMsg, serverAddress);
          Thread.sleep(Consts.PING_INTERVAL_MILLISEC);
        }
      } catch (InterruptedException e) {
        logger.warn("InterruptedException", e);
      }
    }

  }
}
