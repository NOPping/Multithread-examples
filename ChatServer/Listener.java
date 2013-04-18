import java.util.*;
import java.net.*;
import java.io.*;

class Listener extends Thread {
  private MessageServer messageServer;

  private Connection connection;

  private BufferedReader inputStream;

  Listener() {
    // Default constructor
  }

  Listener(Connection connection, MessageServer messageServer) {
    this.connection = connection;
    this.messageServer = messageServer;

    try {
      Socket socket = connection.getSocket();
      inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch(IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public void run() {
    try {
      String message;
      while(connection.getNickname() == null) {
        message = inputStream.readLine();
        if(message == null || message.equals("")) {
          close();
          return;
        }
        connection.setNickname(message);
        messageServer.addConnection(connection);
      }

      messageServer.addMessage(connection.getNickname() + " has joined the chatroom...");
      while(!isInterrupted()) {
        Thread.sleep(100);
        message = inputStream.readLine();
        if(message == null) {
          break;
        } else if(!message.trim().equals("")) {
          message = connection.getNickname() + " says: " + message;
          messageServer.addMessage(message);
        }
      }
    } catch(Exception e) {
    }
    close();
    messageServer.deleteConnection(connection);
  }

  private void close() {
    try {
      inputStream.close();
      connection.getSocket().close();
      connection.getSender().interrupt();
    } catch(IOException e) {
    }
  }
}