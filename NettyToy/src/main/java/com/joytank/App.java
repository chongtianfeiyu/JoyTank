package com.joytank;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

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
		if (isServer) {
			new LobbyServer(Utils.getGameConfig()).run();
//			AbstractApplication app = new DefaultServerApplication(config.getServerUdpPort());
//			app.start(Type.Headless);
		} else {
		  new LobbyClient();
//			AbstractApplication app = new DefaultClientApplication(config.getServerHost(), config.getServerUdpPort());
//			AppSettings settings = new AppSettings(true);
//			settings.setResolution(config.getScreenWidth(), config.getScreenHeight());
//			settings.setSamples(config.getSamples());
//			settings.setFullscreen(config.isFullscreen());
//			settings.setVSync(config.isVSync());
//			settings.setTitle("ROFL");
//			app.setSettings(settings);
//			app.setShowSettings(false);
//			app.setPauseOnLostFocus(false);
//			app.start();
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
