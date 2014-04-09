package com.joytank;

import java.io.File;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.joytank.game.AbstractApplication;
import com.joytank.game.ClientApplication;
import com.joytank.game.DefaultClientApplication;
import com.joytank.game.DefaultServerApplication;
import com.joytank.game.GameConfig;
import com.joytank.net.UdpServer;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class App {

	private static final Logger logger = Logger.getLogger(App.class);

	private static final String CONFIG_PATH = "config.json";

	@Option(name = "-server", required = false)
	private boolean isServer;

	public App(String[] args) {
		GameConfig config = parseArgs(args);
		if (config == null) {
			logger.warn("Cannot find config file 'config.json', now exit.");
			System.exit(0);
		}
		if (isServer) {
			AbstractApplication app = new DefaultServerApplication(config.getServerPort());
			app.start(Type.Headless);
		} else {
			AbstractApplication app = new DefaultClientApplication(config.getServerHost(), config.getServerPort());
			AppSettings settings = new AppSettings(true);
			settings.setResolution(config.getScreenWidth(), config.getScreenHeight());
			settings.setSamples(config.getSamples());
			settings.setFullscreen(config.isFullscreen());
			settings.setVSync(config.isVSync());
			settings.setTitle("ROFL");
			app.setSettings(settings);
			app.start();
		}
	}

	private GameConfig parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			File configFile = new File(CONFIG_PATH);
			if (configFile.exists() && configFile.isFile()) {
				GameConfig config = new ObjectMapper().readValue(configFile, GameConfig.class);
				return config;
			}
		} catch (Exception e) {
			logger.warn("Exception: ", e);
		}
		return null;
	}

	public static void main(String[] args) {
		new App(args);
	}
}
