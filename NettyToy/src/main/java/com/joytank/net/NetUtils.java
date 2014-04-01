package com.joytank.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public final class NetUtils {

  private static final Logger LOGGER = Logger.getLogger(NetUtils.class);

  /**
   * Get the external IP (i.e. router IP)
   * 
   * @return
   */
  public static String getExternalIp() {
    String ip = StringUtils.EMPTY;
    String url = "http://checkip.amazonaws.com/";
    try {
      URL u = new URL(url);
      BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
      ip = reader.readLine();
      reader.close();
    } catch (Exception e) {
      LOGGER.warn("Exception: ", e);
    }
    return ip;
  }
  
  private NetUtils() {
  }
}
