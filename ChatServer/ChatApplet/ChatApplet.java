/* Our chat client */
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

class ChatClient extends Panel implements Runnable {
  /* Display */
  private TextField textfield = new TextField();
  private TextArea textarea = new TextArea("", 4, 30, TextArea.SCROLLBARS_VERTICAL_ONLY);
  private Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

  /* Communication */
  private Socket s;
  private PrintWriter pw;
  private BufferedReader br;

  public ChatClient(String host, int port, String nickname) {

    /* Set up display */
    setLayout(new BorderLayout());
    textarea.setFont(font);
    textarea.setEditable(false);
    
    textfield.setFont(font);
    add(BorderLayout.SOUTH, textfield);
    add(BorderLayout.CENTER, textarea);

    /* Associate sendChat with textfield callback */
    textfield.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sendChat(e.getActionCommand());
      }
    });

    try {
      s = new Socket(host, port);
      pw = new PrintWriter(s.getOutputStream(), true);
      br = new BufferedReader(new InputStreamReader(s.getInputStream()));

      /* Send nickname to chat server */
      pw.println(nickname);

      /* Become a thread */
      new Thread(this).start();
    } catch (IOException e) {
      System.out.println(e);
    }
  }

  /* Called whenever user hits return in the textfield */
  private void sendChat(String message) {
    pw.println(message);
    textfield.setText("");
  }

  /* Add strings from chat server to the textarea */
  public void run() {

    String message;

    try {
      while (true) {
        message = br.readLine();
        if(message == null) {
          break;
        }
        textarea.append(message + "\n");
      }
    } catch (IOException e) {
      System.out.println(e);
    }
    
    textarea.append("Connection to server was lost." + "\n");
  }
}

public class ChatApplet extends Applet {
  public void init() {

    /* Retrieve parameters */
    int port = Integer.parseInt(getParameter("port"));
    String host = getParameter("host");
    String nickname = getParameter("nickname");

    /* Set up display */
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new ChatClient(host, port, nickname));
  }
}
