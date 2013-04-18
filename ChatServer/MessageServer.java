import java.util.*;
import java.net.*;
import java.io.*;

class MessageServer extends Thread {
  private ArrayList<String> messages = new ArrayList<String>();
  private ArrayList<Connection> connections = new ArrayList<Connection>();

  MessageServer() {
    // Default constructor.
  }

  public synchronized void addConnection(Connection connection) {
    connections.add(connection);
    System.out.println("Clients: " + connections.size());
  }

  public synchronized void deleteConnection(Connection connection) {
    int connectionIndex = connections.indexOf(connection);
    if(connectionIndex != -1) {
      connections.remove(connectionIndex);
      System.out.println("Clients: " + connections.size());
      addMessage(connection.getNickname() + " has left the chatroom...");
    }
  }

  public synchronized void addMessage(String message) {
    messages.add(message);
    notify();
  }

  private synchronized String getNextMessage() {
    while(messages.size () == 0) {
      try {
        wait();
      } catch(InterruptedException e) {
      }
    }

    String message = messages.get(0);
    messages.remove(0);

    return message;
  }

  private synchronized void sendMessageToAllConnections(String message) {
    for(int i=0; i<connections.size(); i++) {
      Connection connection = connections.get(i);
      connection.getSender().sendMessage(message);
    }
  }

  public void run() {
    while(true) {
      try {
        Thread.sleep(100);
        String message = getNextMessage();
        sendMessageToAllConnections(message);
      } catch(Exception e) {
      }
    }
  }
}