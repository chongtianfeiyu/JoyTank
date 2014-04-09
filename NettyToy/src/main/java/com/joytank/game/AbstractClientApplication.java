package com.joytank.game;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.joytank.net.Consts;
import com.joytank.net.JoinResponse;
import com.joytank.net.PingMsg;

/**
 * 
 */
public abstract class AbstractClientApplication extends AbstractApplication {

  private static final Logger logger = Logger.getLogger(AbstractClientApplication.class);

  protected final SocketAddress serverAddress;
  protected AtomicBoolean isPingServer = new AtomicBoolean(false);
  protected int clientId;
  
  protected int pingValue;

  public AbstractClientApplication(String serverHost, int serverPort) {
    super();
    Preconditions.checkState(!StringUtils.isBlank(serverHost), "serverHost is unexpectedly blank or null.");
    serverAddress = new InetSocketAddress(serverHost, serverPort);
  }

  @Override
  protected void initAll() {
  }

  @Override
  protected void handleMessage(Object msg) {
    if (msg instanceof JoinResponse) {

    }
    if (msg instanceof PingMsg) {
      handlePingMsg((PingMsg) msg);
    }
  }

  private void handlePingMsg(PingMsg msg) {
    long dTime = System.nanoTime() - msg.getTimestamp();
    pingValue = (int) (dTime / 1000000);
  }

  /**
   * 
   */
  public void stopPingingServer() {
    isPingServer.set(false);
  }

  /**
   * 
   */
  public void startPingingServer() {
    if (isPingServer.get()) {
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

  private class PingTask implements Runnable {
    @Override
    public void run() {
      try {
        isPingServer.set(true);
        while (isPingServer.get()) {
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
