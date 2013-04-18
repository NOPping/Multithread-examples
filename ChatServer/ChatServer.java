import java.util.*;
import java.net.*;
import java.io.*;

public class ChatServer {
  public static void main(String[] args) {
    // Port to listen on.
    final int PORT = 7777;

    // Open socket for listening on.
    ServerSocket serverSocket = null;

    try {
      // Attempt to bind to socket on given port.
      serverSocket = new ServerSocket(PORT);
      System.out.println("Chat server listening " + PORT + ".");
    } catch(IOException e) {
      System.out.println("Cannot listen on " + PORT + ".");
      System.out.println(e.getMessage());
      System.exit(-1);
    }

    // Start messageServer thread for serving all messages.
    MessageServer messageServer = new MessageServer();
    messageServer.start();

    // Accept connections.
    while (true) {
      try {
        // Wait for a connection.
        Socket socket = serverSocket.accept();

        // Save the connection.
        Connection connection = new Connection(socket);

        // Create a listener and sender for the connection.
        Listener listener = new Listener(connection, messageServer);
        Sender sender = new Sender(connection, messageServer);

        // Save the references in the connection.
        connection.setListener(listener);
        connection.setSender(sender);

        // start the listener and sender threads.
        listener.start();
        sender.start();
      } catch(IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}