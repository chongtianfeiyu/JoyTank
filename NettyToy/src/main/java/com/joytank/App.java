package com.joytank;

import java.io.File;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.joytank.game.ClientGui;
import com.joytank.net.UdpServer;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class App {

	private static final Logger LOGGER = Logger.getLogger(App.class);

	private static final String CONFIG_PATH = "config.json";

	@Option(name = "-server", required = false)
	private boolean isServer;

	public App(String[] args) {
		Config config = parseArgs(args);
		if (config == null) {
			LOGGER.warn("Cannot find config file 'config.json', now exit.");
			System.exit(0);
		}
		if (isServer) {
			new UdpServer(config.getServerPort()).run();
		} else {
			new ClientGui(config.getServerHost(), config.getServerPort()).run();
		}
	}

	private Config parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			File configFile = new File(CONFIG_PATH);
			if (configFile.exists() && configFile.isFile()) {
				Config config = new ObjectMapper().readValue(configFile, Config.class);
				return config;
			}
		} catch (Exception e) {
			LOGGER.warn("Exception: ", e);
		}
		return null;
	}

	public static void main(String[] args) {
		new App(args);
	}
}
