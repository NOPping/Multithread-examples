/**
-* This project is our own work. We have not recieved assistance beyond what is
-* normal, and we have cited any sources from which we have borrowed. We have
-* not given a copy of our work, or a part of our work, to anyone. We are aware
-* that copying or giving a copy may have serious consequences.
-*/

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ChatServer starts the application.
 * It is responsible for:
 *  - Setting up the bind socket.
 *  - Starting the MessageServer thread.
 *  - Accepting new connections.
 *  - Creating producer and consumer threads for all connections.
 *
 * MessageServer, Producer, and Consumer all extend the thread class.
 * This means that they can be executed in parallel.
 *
 * @author Ian Duffy, 11356066
 * @author Richard Kavanagh, 11482928
 * @author Darren Brogan, 11424362
 */
public class ChatServer {
  /// Main function.
  public static void main(String[] args) {
    // Set the bind port.
    final int PORT = 7777;

    // Set bind IP
    final String ADDRESS = "127.0.0.1";

    // Initialize a IP for binding.
    InetAddress address = null;

    // Set the maxium amount of clients. Don't wish to eat up server resources
    // By spawning an infinte amount of threads.
    final int MAXCLIENTS = 2000;

    // Initalize a socket for binding.
    ServerSocket serverSocket = null;


    // Attempt to resolve and set the bind IP.
    try {
      address = InetAddress.getByName(ADDRESS);
    } catch(Exception e) {
      System.out.println(e.getMessage());
      System.exit(-1);
    }

    try {
      // Attempt to bind the socket to the given port.
      serverSocket = new ServerSocket(PORT,0,address);
      System.out.println("Waiting for connections on " + PORT + ".");
    } catch(Exception e) {
      System.out.println("Failed to bind to " + PORT + ".");
      System.out.println(e.getMessage());
      System.exit(-1);
    }

    // Start the messageServer thread.
    MessageServer messageServer = new MessageServer(MAXCLIENTS);
    messageServer.start();

    // Accept connections.
    while(true) {
      try {
        // Wait for a connection.
        Socket socket = serverSocket.accept();

        // Save the connection.
        Connection connection = new Connection();
        connection.socket = socket;

        // Create a thread that will allow the connection to listen for
        // messages.
        Producer producer = new Producer(connection, messageServer);
        connection.producer = producer;

        // Create a thread that will allow the connection to receive messages.
        Consumer consumer = new Consumer(connection);
        connection.consumer = consumer;

        // Start the consumer and producer.
        consumer.start();
        producer.start();
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /**
   * MessageServer is responsible for recieving and sending all of the clients
   * messages.
   *
   * MessageServer holds:
   * - A buffer of all messages.
   * - A list of all connections.
   *
   * @author Ian Duffy, 11356066
   * @author Richard Kavanagh, 11482928
   * @author Darren Brogan, 11424362
   */
  private static class MessageServer extends Thread {
    /// Message buffer.
    private ArrayList<String> buffer;

    /// Connection list.
    private ArrayList<Connection> connections;

    /// Maximum about of connections.
    private final int MAXCONNECTIONS;

    MessageServer(int MAXCONNECTIONS) {
      this.MAXCONNECTIONS = MAXCONNECTIONS;
      this.buffer = new ArrayList<String>();
      this.connections = new ArrayList<Connection>();
    }

    /// Adds a new connection to the connections list.
    public synchronized void addConnection(Connection connection) {
      // Adds the connection.
      connections.add(connection);
      // Inform the server operator of a change in the amount of clients.
      System.out.println("Clients: " + connections.size());
    }

    /// Deletes a connection from the connections list.
    public synchronized void deleteConnection(Connection connection) {
      // Find the connection in the list.
      int connectionIndex = connections.indexOf(connection);

      // If it was found remove it.
      if(connectionIndex != -1) {
        connections.remove(connectionIndex);

        // Inform the server operator of a change in the amount of clients.
        System.out.println("Clients: " + connections.size());

        // Inform all other connections.
        addMessage(connection.nickname + " has left the chatroom...");
      }
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

    /// Passes the message onto all the connections.
    private synchronized void sendMessageToAll(String message) {
      for(Connection connection : connections) {
        connection.consumer.addMessage(message);
      }
    }

    /// Reports whether or not the server is at maximum connections.
    public synchronized boolean isFull() {
      return (connections.size() >= MAXCONNECTIONS);
    }

    /// Send messages from the buffer to clients.
    @Override
    public void run() {
      while(true) {
        try {
          // Sleep for 100 milliseconds, don't want to hog the CPU.
          sleep(100);

          // Get message from buffer.
          String message = getNextMessage();

          // Send to Client.
          sendMessageToAll(message);
        } catch(Exception e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }

  /**
   * Connection is a structure that holds all information that is related to a
   * connection this includes the socket, producer, consumer and a nickname.
   *
   * @author Ian Duffy, 11356066
   * @author Richard Kavanagh, 11482928
   * @author Darren Brogan, 11424362
   */
  private static class Connection {
    // Public access due to this just being a structure.
    // Would be pointless to set private access and then supply getters/setters
    // for all variables.

    /// Socket to the clients connection.
    public Socket     socket    = null;

    /// Producer to listen for messages from the client.
    public Producer   producer  = null;

    /// Consumer to send messages to the client.
    public Consumer   consumer  = null;

    /// The clients nickname.
    public String     nickname  = null;

    /// Closes all the streams involved in the connection.
    public void close() {
      try {
        producer.close();
        consumer.close();
        socket.close();
      } catch(IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  /**
   * Recieves messages from MessageServer and stores them in a buffer which is
   * eventually sent to the client.
   *
   * @author Ian Duffy, 11356066
   * @author Richard Kavanagh, 11482928
   * @author Darren Brogan, 11424362
   */
  private static class Consumer extends Thread {
    /// Buffer of messages.
    private ArrayList<String> buffer;

    /// Output stream.
    private PrintWriter outstream;

    Consumer(Connection connection) {
      // Setup a message buffer.
      this.buffer = new ArrayList<String>();

      // Setup outstream.
      try {
        this.outstream = new PrintWriter(
          new OutputStreamWriter(connection.socket.getOutputStream(), "UTF-8"),
          true
        );
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
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
    @Override
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
      } catch(InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    /// Closes the outstream.
    public void close() {
        outstream.close();
    }
  }

  /**
   * Recieves messages from the Connection and sends them to MessageServer.
   *
   * @author Ian Duffy, 11356066
   * @author Richard Kavanagh, 11482928
   * @author Darren Brogan, 11424362
   */
  private static class Producer extends Thread {
    /// Reference to MessageServer.
    private MessageServer messageServer;

    /// Reference to Connection.
    private Connection connection;

    /// Input stream.
    private BufferedReader instream;

    Producer(Connection connection, MessageServer messageServer) {
      // Setup necessary references.
      this.connection = connection;
      this.messageServer = messageServer;

      // Setup the instream.
      try {
        instream = new BufferedReader(
          new InputStreamReader(connection.socket.getInputStream(), "UTF-8")
        );
      } catch(IOException e) {
        System.out.println(e.getMessage());
      }
    }

    /// Listen for messages.
    public void run() {
      try {
        // Set the nickname.
        connection.nickname = instream.readLine();

        // Check that the server isn't full.
        if(messageServer.isFull()) {
          connection.consumer.addMessage("Server is at maximum capacity.");
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
              connection.consumer.addMessage("You attempted to send an empty "
                                             + "message");
            } else {
              messageServer.addMessage(connection.nickname + " says : "
                                       + message);
            }
          }
        }
      } catch(IOException e) {
        System.out.println(e.getMessage());
      } catch(InterruptedException e) {
        System.out.println(e.getMessage());
      }
      interrupt();
    }

    /// Overrides the default thread interrupt to
    ///   - Remove the connection from the messageServer list of connections.
    ///   - Close all the streams involved with the connection.
    ///   - Interrupt the consumer for the connection.
    @Override
    public void interrupt() {
      messageServer.deleteConnection(connection);
      connection.close();

      try {
        connection.consumer.interrupt();
        connection.consumer.join();
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }

      super.interrupt();
    }

    /// Closes the instream.
    public void close() {
      try {
        instream.close();
      } catch(IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}