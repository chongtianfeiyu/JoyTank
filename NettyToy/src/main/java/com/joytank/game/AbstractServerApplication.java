package com.joytank.game;

public abstract class AbstractServerApplication extends AbstractApplication {

  public AbstractServerApplication(int localPort) {
    super(localPort);
  }

  @Override
  protected void initAll() {
  }

  @Override
  protected void handleMessage(Object msg) {
  }
}
