/**
-* This project is our own work. We have not recieved assistance beyond what is
-* normal, and we have cited any sources from which we have borrowed. We have
-* not given a copy of our work, or a part of our work, to anyone. We are aware
-* that copying or giving a copy may have serious consequences.
-*/

import java.net.*;

/**
 * ChatServer starts the application.
 * It is responsible for:
 *  - Setting up the bind socket.
 *  - Starting the MessageServer thread.
 *  - Accepting new connections.
 *  - Creating producer and consumer threads for all connections.
 *
 * MessageServer, Producer, and Consumer all extend the thread class.
 * This means that they can be executed in parallel. All shared resources,
 * Within the classes are marked synchronized as necessary.
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
        Consumer consumer = new Consumer(connection, messageServer);
        connection.consumer = consumer;

        // Start the consumer and producer.
        consumer.start();
        producer.start();
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
}