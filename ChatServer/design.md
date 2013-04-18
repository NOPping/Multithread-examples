Design
=========

Our chat server is made up of the following:

  - A Vector for holding all the connections.
  - A Buffer that stores the clients messages.
  - A Consumer that takes messages from the buffer and sends them to each 
    connection/client.
  - A Connection that represents each client. It inserts messages into the
    buffer.
  - A Monitor thread that outputs a client count to the server operator.

We used a vector for storing the connections as a vector is a dynamic and
synchronized datastore. This means our multiple threads can read and modify it
without any issues.

The Buffer is taken from the last CA216 assignment. The only modification to it
is that it holds Strings insteads of Integers. The Buffer is used to queue the
clients messages while the consumer outputs them.

The Consumer takes messages from the buffer and sends them to each client in
the connections vector. Should if fail to send a message to a connection it will
assume that the connection no longer exists and remove the connection from the
vector.

The Connection simply represenets a client. It is made up of their socket, an
input and output stream to that socket, and their nick. It waits for first valid
input and sets this as the users nick, after that all input is assumed to be
messages so they are inserted into the buffer.

The monitor is just used to supply a count of the amount of users currently on
the server to the server operator.
