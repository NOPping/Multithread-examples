import java.util.*;
import java.io.*;
import java.net.*;


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
      // Display the error message and end.
      System.out.println(e.getMessage());
      return;
    }
  }
  
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
    
    synchronized String getMessage() {
      while(occupied == 0) {
        try {
          wait();
        } catch(InterruptedException e) {
          System.out.println(e.getMessage());
        }
      }
      
      String contents = buffer[nextOut];
      nextOut = (nextOut+1)%size;
      occupied--;
      outs++;
      
      notifyAll();
      
      return contents;
    }
    
    synchronized void insertMessage(String message) {
      while(occupied == size) {
        try{ 
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
    
      notifyAll();
    }
    
  }
  
  private static class Consumer extends Thread {
    private Vector<Connection> connections;
    private BoundedBuffer buffer;
    
    Consumer(BoundedBuffer buffer, Vector<Connection> connections) {
      this.buffer = buffer;
      this.connections = connections;
    }
    
    public void run() {
      while(true) {
        String message = buffer.getMessage();
        sendAll(message);
      }
    }
    
    private void sendAll(String message) {
      for(int i=connections.size()-1;i>=0;i--) {
        Connection connection = connections.get(i);
        if(!connection.write(message)) {
          connections.remove(i);
        }
      }
    }
  }
  
  private static class Connection extends Thread {
    private Socket socket;
    private BoundedBuffer buffer;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private String nick;
    
    Connection(Socket socket,BoundedBuffer buffer) {
      try {
        this.socket = socket;
        this.buffer = buffer;
        inputStream = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
        outputStream = new PrintWriter(socket.getOutputStream(), true);
      } catch(IOException e) {
        System.out.println(e.getMessage());
      }
    }
    
    public void run() {
      try {
          setNick(inputStream.readLine());
          buffer.insertMessage(nick + " Just joined the chatroom...");
          
          String input;
          while((input = inputStream.readLine()) != null) {
            buffer.insertMessage(nick + " says: " + input);
          }
          
          buffer.insertMessage(nick + " Just left the chatroom...");
          close();
        } catch(IOException e) {
          System.out.println(e.getMessage());
          return;
        }
    }
    
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
    
    public String getNick() {
      return nick;
    }

    public void setNick(String nick) {
      this.nick = nick;
    }
    
    public boolean write(String message) {
      outputStream.println(message);
      if(outputStream.checkError()) {
        close();
        return false;
      } else {
        return true;
      }
    }
  }
}