/**
-* This project is our own work. We have not recieved assistance beyond what is
-* normal, and we have cited any sources from which we have borrowed. We have
-* not given a copy of our work, or a part of our work, to anyone. We are aware
-* that copying or giving a copy may have serious consequences.
-*/

import java.net.*;

/**
 * Connection is a structure that holds all information that is related to a
 * connection this includes the socket, producer, consumer and a nickname.
 *
 * @author Ian Duffy, 11356066
 * @author Richard Kavanagh, 11482928
 * @author Darren Brogan, 11424362
 */
public class Connection {
  // Public access due to this just being a structure.
  // Would be pointless to set private access and then supply getters/setters
  // for all variables.

  /// Socket to the clients connection.
  public Socket     socket    = null;
  
  /// Producer for listen for messages from the client.
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
    } catch(Exception e) { }
  }
}