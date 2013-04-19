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
import java.net.*;
import java.io.*;

/**
 * Recives messages from MessageServer and sends them to the connection.
 */
public class Sender extends Thread {
  // Reference to MessageServer.
  private MessageServer messageServer;

  // Reference to Connection.
  private Connection connection;

  // Buffer of messages.
  private ArrayList<String> buffer;

  // Output stream.
  private PrintWriter outstream;

  Sender(Connection connection, MessageServer messageServer) {
    // Setup necessary references.
    this.connection = connection;
    this.messageServer = messageServer;

    // Setup a message buffer.
    this.buffer = new ArrayList<String>();

    // Setup outstream.
    try {
      this.outstream = new PrintWriter(connection.socket.getOutputStream());
    } catch(Exception e) { }
  }

  /// Adds a message to the buffer.
  public synchronized void addMessage(String message) {
    buffer.add(message);

    // Notify getNextMessage() that the message list is no longer empty.
    notify();
  }

  /// Waits until the buffer is not empty and then returns the first message.
  public synchronized String getNextMessage() throws InterruptedException {
    while(buffer.size() == 0) {
      wait();
    }

    // Get the message from the buffer.
    String message = buffer.get(0);

    // Remove the message from the bufffer.
    buffer.remove(0);

    return message;
  }

  /// Sends a message to the client.
  public void sendMessage(String message) {
    outstream.println(message);
    outstream.flush();
  }

  /// Send messages from the buffer to clients.
  public void run() {
    try {
      while(!isInterrupted()) {
        // Sleep for 100 milliseconds, don't want to hog the CPU.
        sleep(100);

        // Get message from buffer.
        String message = getNextMessage();

        // Send to Client.
        sendMessage(message);
      }
    } catch(Exception e) { }
  }

  /// Closes the outstream.
  public void close() {
    try {
      outstream.close();
    } catch(Exception e) { }
  }
}