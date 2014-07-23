package com.joytank.net.game;

/**
 * Some useful constants
 */
public final class Consts {

  /**
   * The time limit to try to connect to a remote end-point
   */
  public static final int CONN_TIME_LMT_SEC = 2;

  /**
   * How often a Ping packet will be sent from client
   */
  public static final int PING_INTERVAL_MILLIS = 1000;

  /**
   * How often the server will check clients disconnections
   */
  public static final int DC_DETECTION_INTERVAL_SEC = 1;

  /**
   * Server decides a client is disconnected if it doesn't receive a Ping in this period of time
   */
  public static final int DISCONNECT_THRESHOLD_MILLIS = 5000;

  /**
   * How often game state will be broadcasted to clients
   */
  public static final int GAME_STATE_BROADCAST_INTERVAL_MILLIS = 40;

  /**
   * How often client retries to join
   */
  public static final int JOIN_REQUEST_RETRY_INTERVAL_MILLIS = 3000;

  /**
   * The minimum port number
   */
  public static final int PORT_MIN = 1024;

  /**
   * The maximum port number
   */
  public static final int PORT_MAX = 65535;

  /**
   * Up to 32 KB UDP packet size
   */
  public static final int UDP_PACKET_SIZE_MAX = (1 << 15);

  /**
   * The default invalid client ID
   */
  public static final int INVALID_CLIENT_ID = 0;

  /**
   * The "assets" folder URL
   */
  public static final String ASSETS_BASE_URL = ClassLoader.getSystemResource("assets/").toString();

  private Consts() {}
}
