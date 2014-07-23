package com.joytank.net.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import javax.annotation.Nullable;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.joytank.game.GameConfig;

/**
 * Utility class
 */
public final class Utils {

	private static final Logger logger = Logger.getLogger(Utils.class);

	private static final String LOOPBACK_LOCALHOST = "127.0.0.1";
	
	private static final String CHECK_IP_URL = "http://checkip.amazonaws.com/";

	/**
	 * Get the external IP (i.e. router IP)
	 * 
	 * @return
	 */
	@Nullable
	public static String getExternalAddress() {
		String ip = null;
		try {
			URL u = new URL(CHECK_IP_URL);
			BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
			ip = reader.readLine();
		} catch (Exception e) {
			logger.warn("Exception: ", e);
		}
		return ip;
	}

	/**
	 * Find the non-loopback local address
	 * 
	 * @return local address {@link Nullable}
	 */
	@Nullable
	public static String getLocalAddress() {
		Enumeration<NetworkInterface> netInterfaces;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();

				Enumeration<InetAddress> a = ni.getInetAddresses();
				while (a.hasMoreElements()) {
					InetAddress hostAddr = a.nextElement();
					if (hostAddr instanceof Inet4Address && !LOOPBACK_LOCALHOST.equals(hostAddr.getHostAddress())) {
						return hostAddr.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a random valid port number
	 * 
	 * @return
	 */
	public static int generateRandomPort() {
		int port = 0;
		while (port < Consts.PORT_MIN || port > Consts.PORT_MAX) {
			port = RandomUtils.nextInt(Consts.PORT_MAX);
		}
		return port;
	}

	/**
	 * Read the game config
	 * 
	 * @return Game config object {@link Nullable} 
	 */
	@Nullable
	public static GameConfig getGameConfig() {
		try {
			return new ObjectMapper().readValue(ClassLoader.getSystemResourceAsStream("gameConfig.json"), GameConfig.class);
		} catch (IOException e) {
			logger.warn("IOException: ", e);
		}
		return null;
	}

	private Utils() {}
}
