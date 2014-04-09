package com.joytank.net;

public final class Consts {

	/**
	 * 
	 */
  public static final int CONN_TIME_LMT_SEC = 2;
  
  /**
   * How often a Ping packet will be sent
   */
  public static final int PING_INTERVAL_MILLISEC = 5000;
  
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

  private Consts() {
  }
}
