package com.joytank;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.joytank.net.UdpClient;
import com.joytank.net.UdpServer;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class App {

  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  @Option(name = "-server", required = false)
  private boolean isServer;

  @Option(name = "-server_host", required = false)
  private String serverHostName;

  @Option(name = "-server_port", required = false)
  private int serverPort;

  public App(String[] args) {
    parseArgs(args);
    if (isServer) {
      new UdpServer(serverPort).run();
    } else {
      new UdpClient(serverHostName, serverPort).run();
    }
  }

  private void parseArgs(String[] args) {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
      validateArgs();
    } catch (CmdLineException e) {
      LOGGER.warn("CmdLineException: ", e);
    }
  }

  private void validateArgs() {
    if (isServer) {
      Preconditions.checkState(serverHostName == null);
    } else {
      Preconditions.checkState(serverHostName != null);
    }
  }

  public static void main(String[] args) {
    new App(args);
  }
}
