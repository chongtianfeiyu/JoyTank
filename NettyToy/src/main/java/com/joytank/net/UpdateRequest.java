package com.joytank.net;

import java.io.Serializable;

/**
 *  
 */
public final class UpdateRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  private final int clientId;

  public int getClientId() {
    return clientId;
  }

  private UpdateRequest(Builder builder) {
    this.clientId = builder.clientId;
  }

  public static class Builder {

    private int clientId;

    public Builder withClientId(int clientId) {
      this.clientId = clientId;
      return this;
    }

    public UpdateRequest build() {
      validate();
      return new UpdateRequest(this);
    }

    private void validate() {
    }
  }
}
