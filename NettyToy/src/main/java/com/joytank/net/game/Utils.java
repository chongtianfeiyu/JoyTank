package com.joytank.net.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.joytank.game.GameConfig;

/**
 * 
 * @author lizhaoliu
 * 
 */
public final class Utils {

	private static final Logger logger = Logger.getLogger(Utils.class);

	private static final String LOOPBACK_LOCALHOST = "127.0.0.1";

	private static final Pattern IPV4_PATTERN = Pattern.compile("^[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}$");

	/**
	 * Get the external IP (i.e. router IP)
	 * 
	 * @return
	 */
	public static String getExternalAddress() {
		String ip = null;
		String url = "http://checkip.amazonaws.com/";
		try {
			URL u = new URL(url);
			BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
			ip = reader.readLine();
			reader.close();
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
	public static String getLocalAddress() {
		String host = null;
		Enumeration<NetworkInterface> netInterfaces;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();

				Enumeration<InetAddress> a = ni.getInetAddresses();
				while (a.hasMoreElements()) {
					host = a.nextElement().getHostAddress();
					if (IPV4_PATTERN.matcher(host).matches() && !LOOPBACK_LOCALHOST.equals(host)) {
						return host;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return host;
	}

	/**
	 * Generate a random valid port number
	 * 
	 * @return
	 */
	public static int generateRandomPort() {
		int port = 0;
		while (port < Consts.PORT_MIN || port > Consts.PORT_MAX) {
			port = new Random().nextInt(Consts.PORT_MAX);
		}
		return port;
	}

	/**
	 * 
	 * @return
	 */
	public static GameConfig getGameConfig() {
		try {
			return new ObjectMapper().readValue(ClassLoader.getSystemResourceAsStream(""), GameConfig.class);
		} catch (IOException e) {
			logger.warn("IOException: ", e);
		}
		return null;
	}

	private Utils() {}
}
