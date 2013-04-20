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
 * Connection is a structure that holds all information that is related to a
 * connection this includes the socket, producer, consumer and a nickname.
 */
public class Connection {
  // Public access due to this just being a structure.
  // Would be pointless to set private access and then supply getters/setters
  // for all variables.

  public Socket     socket    = null;
  public Producer   producer  = null;
  public Consumer   consumer  = null;
  public String     nickname  = null;

  public void close() {
    try {
      producer.close();
      consumer.close();
      socket.close();
    } catch(Exception e) { }
  }
}