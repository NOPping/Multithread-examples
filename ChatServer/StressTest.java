import java.io.*;
import java.net.*;

public class StressTest {
    public static void main(String[] args) {
        Socket s;
        PrintWriter pw;
        BufferedReader br;
        int count = 0;
        String host = "localhost";
        int port = 7777;
        String nickname = "test";
        while(true) {
            try {
                count++;
                Thread.sleep(300);
                s = new Socket(host, port);
                pw = new PrintWriter(s.getOutputStream(), true);
                br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                /* Send nickname to chat server */
                pw.println(nickname);
                pw.println("Sup?");
                System.out.println(count);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            } catch(InterruptedException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }
}
