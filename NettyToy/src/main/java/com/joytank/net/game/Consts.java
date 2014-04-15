package com.joytank.net.game;

public final class Consts {

	/**
	 * 
	 */
  public static final int CONN_TIME_LMT_SEC = 2;
  
  /**
   * How often a Ping packet will be sent from client
   */
  public static final int PING_INTERVAL_MILLIS = 5000;
  
  /**
   * How often a heart beat packet will be sent from server
   */
  public static final int HEART_BEAT_INTERVAL_MILLIS = 1000;
  
  /**
   * 
   */
  public static final int DISCONNECT_THRESHOLD_MILLIS = 5000;
  
  /**
   * 
   */
  public static final int PORT_MIN = 1024;
  
  /**
   * 
   */
  public static final int PORT_MAX = 65535;
  
  /**
   * Up to 32 KB UDP packet size
   */
  public static final int UDP_PACKET_SIZE_MAX = (1 << 15);
  
  /**
   * 
   */
  public static final int INVALID_CLIENT_ID = -1;

  /**
   * 
   */
  public static final String ASSETS_BASE_URL = ClassLoader.getSystemResource("assets/").toString();
  
  private Consts() {
  }
}
