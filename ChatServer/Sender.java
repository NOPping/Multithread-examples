import java.util.*;
import java.net.*;
import java.io.*;

public class Sender extends Thread {
  private MessageServer messageServer;
  private Connection connection;
  private PrintWriter outputStream;
  private ArrayList<String> messages = new ArrayList<String>();

  Sender() {
    // Default constructor.
  }

  Sender(Connection connection, MessageServer messageServer) {
    this.connection = connection;
    this.messageServer = messageServer;

    try {
      Socket socket = connection.getSocket();
      outputStream = new PrintWriter(socket.getOutputStream(),true);
    } catch(IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public synchronized void sendMessage(String message) {
    messages.add(message);
    notify();
  }

  private synchronized String getNextMessage() {
    while(messages.size() == 0) {
      try {
        wait();
      } catch(InterruptedException e) { }
    }

    String message = messages.get(0);
    messages.remove(0);
    return message;
  }

  public void sendMessageToConnection(String message) {
    outputStream.println(message);
    outputStream.flush();
  }

  public void run() {
    try {
      while(!isInterrupted()) {
        Thread.sleep(100);
        String message = getNextMessage();
        sendMessageToConnection(message);
      }
    } catch(Exception e) {
      e.getMessage();
    }
  }

  private void close() {
    try {
      outputStream.close();
      connection.getSocket().close();
      connection.getListener().interrupt();
    } catch(IOException e) { }
  }
}