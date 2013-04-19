/*
-* This project is our own work. We have not recieved assistance beyond what is
-* normal, and we have cited any sources from which we have borrowed. We have
-* not given a copy of our work, or a part of our work, to anyone. We are aware
-* that copying or giving a copy may have serious consequences.
-*
-* @author Ian Duffy, 11356066
-* @author Richard Kavanagh, 11482928
-* @author Darren Brogan, 11424362
-*/

import java.util.*;
import java.io.*;

/**
 * Listen for messages from the client. Forward them to the MessageServer.
 */
class Listener extends Thread {
  // Reference to MessageServer.
  private MessageServer messageServer;

  // Reference to Connection.
  private Connection connection;

  // Input stream.
  private BufferedReader instream;

  Listener(Connection connection, MessageServer messageServer) {
    // Setup necessary references.
    this.connection = connection;
    this.messageServer = messageServer;

    // Setup the instream.
    try {
      instream = new BufferedReader(
        new InputStreamReader(connection.socket.getInputStream()));
    } catch(Exception e) { }
  }

  /// Listen for messages.
  public void run() {
    try {
      // Set the nickname.
      connection.nickname = instream.readLine();

      // Check that the server isn't full.
      if(messageServer.isFull()) {
        connection.sender.addMessage("Server is at maximum capacity.");
        sleep(100);
        connection.close();
      } else {

        // Add the connection to the connections list
        messageServer.addConnection(connection);
        messageServer.addMessage(connection.nickname
                                 + " has joined the chatroom...");

        while(!isInterrupted()) {
          // Sleep for 100 milliseconds, don't want to hog the CPU.
          sleep(100);

          // Read in input.
          String message = instream.readLine();

          // If input is null the client has disconnected.
          if(message == null) {
            break;
          } else {
            message = message.trim();
          }

          // Check that the message isn't empty.
          if(message.equals("")) {
            connection.sender.addMessage("You attempted to send an empty "
                                         + "message");
          } else if(message.charAt(0) == '/') {
            String [] splitMessage = message.split(" ", 2);
            String command = splitMessage[0].toLowerCase();
            String arguments = splitMessage[1];
            if(command.equals("/nick")) {
              messageServer.addMessage(connection.nickname + " has changed "
                                       + "their nickname to " + arguments);
              connection.nickname = arguments;
            }
          } else {
            message = connection.nickname + " says : " + message;
            messageServer.addMessage(message);
          }
        }
      }
    } catch(Exception e) { }
    interrupt();
  }

  public void interrupt() {
    System.out.println("Attempting to end Listener");
    messageServer.deleteConnection(connection);

    try {
      connection.sender.interrupt();
      connection.sender.join();
    } catch(Exception e) { }

    System.out.println("Ended Sender");

    connection.close();
    super.interrupt();
  }

  /// Closes the instream.
  public void close() {
    try {
      instream.close();
    } catch(Exception e) { }
  }
}