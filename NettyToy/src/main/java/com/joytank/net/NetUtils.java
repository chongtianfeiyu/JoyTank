package com.joytank.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public final class NetUtils {

	private static final Logger logger = Logger.getLogger(NetUtils.class);

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
	 * @return local address @Nullable
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

		try {
			Socket socket = new Socket("www.google.com", 80);
			String hostName = socket.getLocalAddress().getHostAddress();
			socket.close();
			return hostName;
		} catch (Exception e) {
			logger.warn("Exception: ", e);
		}
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.warn("UnknownHostException: ", e);
		}
		
		return host;
	}

	private NetUtils() {
	}
}
