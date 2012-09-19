package com.iflytek.udp;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Holds information about a Boxee server which announced itself in response to
 * a discovery request.
 */
class BoxeeServer {
  private String mVersion;
  private String mName;
  private boolean mAuthRequired;
  private int mPort;
  private InetAddress mAddr;

  public BoxeeServer(HashMap<String, String> attributes, InetAddress address) {
    mAddr = address;
    mVersion = attributes.get("version");
    mName = attributes.get("name");
    try {
      mPort = Integer.parseInt(attributes.get("httpPort"));
    } catch (NumberFormatException e) {
      //mPort = Remote.BAD_PORT;
    }

    String auth = attributes.get("httpAuthRequired");
    mAuthRequired = auth != null && auth.equals("true");
  }

  public boolean valid() {
   // return mPort != Remote.BAD_PORT && mAddr != null;
	  return true;
  }

  public String version() {
    return mVersion;
  }

  public String name() {
    return mName;
  }

  public boolean authRequired() {
    return mAuthRequired;
  }

  public int port() {
    return mPort;
  }

  public InetAddress address() {
    return mAddr;
  }

  public String toString() {
    return String.format("%s at %s:%d %s", mName, mAddr.getHostAddress(), mPort, valid() ? "" : "(broken?)");
  }
}
