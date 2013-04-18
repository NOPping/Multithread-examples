import java.util.*;
import java.net.*;
import java.io.*;


public class Connection {
  private Socket socket = null;
  private Listener listener = null;
  private Sender sender = null;
  private String nickname = null;

  Connection() {
    // default constructor
  }

  Connection(Socket socket) {
    this.socket = socket;
  }

  // Set methods

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void setSender(Sender sender) {
    this.sender = sender;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  // Get methods
  public Socket getSocket() {
    return socket;
  }

  public Listener getListener() {
    return listener;
  }

  public Sender getSender() {
    return sender;
  }

  public String getNickname() {
    return nickname;
  }
}