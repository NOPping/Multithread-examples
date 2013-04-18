import java.io.*;
import java.net.*;

public class StressTest {
  public static void main(String[] args) {
    while(true) {
      try {
        StressClient client = new StressClient();
        client.start();
      } catch (Exception e) { }
    }
  }
}

class StressClient extends Thread {
  private Socket s = null;
  private PrintWriter pw = null;
  private BufferedReader br = null;

  StressClient() {
    try {
      s = new Socket("localhost",7777);
      pw = new PrintWriter(s.getOutputStream(), true);
      br = new BufferedReader(new InputStreamReader(s.getInputStream()));
      pw.println(""+((int)(Math.random()*1000)+1000)+"user");

    } catch(Exception e) { }
  }

  public void run() {
    try {
      while(true) {
        pw.println("Hey!");
        sleep(1000);
      }
    } catch(Exception e) { }
  }
}
