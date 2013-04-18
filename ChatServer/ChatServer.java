/**
 * This project is our own work. We have not recieved assistance beyond what is
 * normal, and we have cited any sources from which we have borrowed. We have
 * not given a copy of our work, or a part of our work, to anyone. We are aware
 * that copying or giving a copy may have serious consequences.
 *
 * @author Ian Duffy, 11356066
 * @author Richard Kavanagh, 11482928
 * @author Darren Brogan, 11424362
 */

import java.util.*;
import java.io.*;
import java.net.*;

/// Basic implementation of a chat server.
public class ChatServer {
  public static void main(String[] args) {
    try {
      // Setup a socket to listen on.
      final int PORT = 7777;
      ServerSocket serverSocket = new ServerSocket(PORT);

      // Buffer to hold the messages.
      final int BUFFERSIZE = 100;
      BoundedBuffer buffer = new BoundedBuffer(BUFFERSIZE);

      // Vector of connections.
      Vector<Connection> connections = new Vector<Connection>();

      // Monitor to monitor the amount of connections.
      Monitor monitor = new Monitor(connections);
      monitor.start();

      // Consumer to transfer the messages to all connections.
      Consumer consumer = new Consumer(buffer,connections);
      consumer.start();

      // Inform the server operator that the server is ready to accept
      // connections.
      System.out.println("Waiting for connections on port " + PORT + ".");

      // Infinite loop to accept connections.
      while(true) {
        Socket clientSocket = serverSocket.accept();

        // Create a new thread for the socket.
        Connection connection = new Connection(clientSocket,buffer);

        // Add it to the vector of connections.
        connections.add(connection);

        // Start the thread.
        connection.start();
      }
    } catch(IOException e) {
      System.out.println(e.getMessage());
      return;
    }
  }

  /// Bounded buffer of a fixed length.
  private static class BoundedBuffer {
    private String[] buffer;
    private int nextIn, nextOut, size, occupied, ins, outs;

    BoundedBuffer(int size) {
      // Setup the buffer.
      this.size   = size;
      buffer      = new String[size];

      // Initialize the required variables.
      nextIn      = 0;
      nextOut     = 0;
      occupied    = 0;
      ins         = 0;
      outs        = 0;
    }

    /// Removes a message from the buffer.
    synchronized String getMessage() {
      // Wait until the buffer isn't empty.
      while(occupied == 0) {
        try {
          wait();
        } catch(InterruptedException e) {
          System.out.println(e.getMessage());
          return "";
        }
      }

      String contents = buffer[nextOut];
      nextOut = (nextOut+1)%size;
      occupied--;
      outs++;

      // Notify all other threads of completition.
      notifyAll();

      return contents;
    }

    /// Adds a message to the buffer.
    synchronized void insertMessage(String message) {
      // Wait until the buffer isn't full.
      while(occupied == size) {
        try {
          wait();
        } catch(InterruptedException e) {
          System.out.println(e.getMessage());
          return;
        }
      }

      buffer[nextIn] = message;
      nextIn=(nextIn+1)%size;
      occupied++;
      ins++;

      // Notify all other threads of competition.
      notifyAll();
    }

  }

  /// The consumer gets messages from the buffer and sends them out to all
  /// connections.
  private static class Consumer extends Thread {
    // Vector of connections.
    private Vector<Connection> connections;
    
    // Buffer of messages.
    private BoundedBuffer buffer;

    Consumer(BoundedBuffer buffer, Vector<Connection> connections) {
      // Setup the buffer and connection references.
      this.buffer = buffer;
      this.connections = connections;
    }

    public void run() {
      while(true) {
        try {
          // Check for messages to send every 100 milliseconds.
          Thread.sleep(100);
          
          // Get a message from the buffer.
          String message = buffer.getMessage();
          
          // End it to all connections.
          sendAll(message);
        } catch(Exception e) {
          System.out.println(e.getMessage());
          return;
        }
      }
    }

    private void sendAll(String message) {
      for(int i=0; i<connections.size(); i++) {
        // Get the ith connection.
        Connection connection = connections.get(i);

        // Send a message to it.
        if(!connection.write(message)) {
          // If the message failed to send to it assume that its dead and
          // remove it from the vector.
          connections.remove(i);
        }
      }
    }
  }

  /// Represents a connection, will be given messages by the consumer.
  private static class Connection extends Thread {
    //Users socket.
    private Socket socket;
    
    // Buffer of messages.
    private BoundedBuffer buffer;
    
    // Input and Output streams.
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    
    // Nick of the user.
    private String nick;

    Connection(Socket socket,BoundedBuffer buffer) {
      try {
        this.socket = socket;
        this.buffer = buffer;
        inputStream = new BufferedReader(
          new InputStreamReader(socket.getInputStream()));
        outputStream = new PrintWriter(socket.getOutputStream(), true);
        nick="";
      } catch(IOException e) {
        System.out.println(e.getMessage());
      }
    }

    public void run() {
      try {
        // Set the nick
        while(nick.equals("")) nick=inputStream.readLine().trim();

        // Adds a message to inform of the users join to the buffer.
        buffer.insertMessage(nick + " Just joined the chatroom...");

        // Gets the users messages and inserts them into the buffer.
        String input;
        while((input = inputStream.readLine()) != null) {
          if(!input.trim().equals("")) {
            buffer.insertMessage(nick + " says: " + input);
          } else {
            write("You attempted to send an empty message");
          }
        }

        // Adds a message to inform of the users departure.
        buffer.insertMessage(nick + " just left the chatroom...");

        // Close all the streams and socket.
        close();
      } catch(IOException e) {
        System.out.println(e.getMessage());
        return;
      }
    }

    /// Closes all the streams and socket.
    private void close() {
      try {
        socket.close();
        inputStream.close();
        outputStream.close();
      } catch(IOException e) {
        System.out.println(e.getMessage());
        return;
      }
    }

    /// Returns the connections nick
    public String getNick() {
      return nick;
    }

    /// Writes a message to the connection.
    public boolean write(String message) {
      outputStream.println(message);
      if(outputStream.checkError()) {
        // Close the socket and streams if writing caused an error.
        close();
        return false;
      } else {
        return true;
      }
    }
  }
  
  /// Provides a count of clients to the server operator.
  private static class Monitor extends Thread {
    // Last known amount of clients.
    private int lastAmount;
    
    // Vector of connections.
    private Vector<Connection> connections;
    
    Monitor(Vector<Connection> connections) {
      // Setup the connections reference.
      this.connections = connections;
      // Set the lastAmount to 0.
      this.lastAmount = 0;
    }
    
    public void run() {
      try{ 
        while(true) {
          // Check client count every 300 milliseconds.
          Thread.sleep(300);
          if(lastAmount != connections.size()) {
            // If the size has changed update print out the new count.
            System.out.println("Clients: " + connections.size());
            
            // Update last amount.
            lastAmount = connections.size();
          }
        }
      } catch(Exception e) {
        System.out.println(e.getMessage());
        return;
      }
    }
  }
}