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

import java.net.*;

/**
 * ChatServer starts the application.
 * It is responsible for:
 *  - Setting up the bind socket.
 *  - Starting the MessageServer thread.
 *  - Accepting new connections.
 *  - Creating listener and sender threads for all connections.
 *
 * MessageServer, Listener, and Sender all extend the thread class.
 * This means that they can be executed in parallel. All shared resources,
 * Within the classes are marked synchronized as necessary.
 */
public class ChatServer {
  public static void main(String[] args) {
    // Set the bind port.
    final int PORT = 7777;

    // Set the maxium amount of clients. Don't wish to eat up server resources
    // By spawning an infinte amount of threads.
    final int MAXCLIENTS = 200;

    // Initalize a socket for binding.
    ServerSocket serverSocket = null;

    try {
      // Attempt to bind the socket to the given port.
      serverSocket = new ServerSocket(PORT);
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
        Listener listener = new Listener(connection, messageServer);
        connection.listener = listener;

        // Create a thread that will allow the connection to send messages.
        Sender sender = new Sender(connection, messageServer);
        connection.sender = sender;

        // Start the sender and listener.
        sender.start();
        listener.start();
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
}