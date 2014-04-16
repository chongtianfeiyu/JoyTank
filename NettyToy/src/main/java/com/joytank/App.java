package com.joytank;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.joytank.game.AbstractApplication;
import com.joytank.game.DefaultClientApplication;
import com.joytank.game.DefaultServerApplication;
import com.joytank.game.GameConfig;
import com.joytank.net.game.Utils;
import com.joytank.net.lobby.LobbyClient;
import com.joytank.net.lobby.LobbyServer;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class App {

	private static final Logger logger = Logger.getLogger(App.class);

	@Option(name = "-server", required = false)
	private boolean isServer;

	public App(String[] args) {
		parseArgs(args);
		GameConfig config = Utils.getGameConfig();
		if (isServer) {
			AbstractApplication app = new DefaultServerApplication(config.getLobbyPort());
			app.start(Type.Headless);
			
//			new LobbyServer(config).run();
		} else {
			AbstractApplication app = new DefaultClientApplication(config.getLobbyHost(), config.getLobbyPort());
			AppSettings settings = new AppSettings(true);
			settings.setResolution(config.getScreenWidth(), config.getScreenHeight());
			settings.setSamples(config.getSamples());
			settings.setFullscreen(config.isFullscreen());
			settings.setVSync(config.isVSync());
			settings.setTitle("ROFL");
			app.setSettings(settings);
			app.setShowSettings(false);
			app.setPauseOnLostFocus(false);
			app.start();
			
//			new LobbyClient();
		}
	}

	private void parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
	    parser.parseArgument(args);
    } catch (CmdLineException e) {
	    logger.warn("CmdLineException: ", e);
    }
	}

	public static void main(String[] args) {
		new App(args);
	}
}
