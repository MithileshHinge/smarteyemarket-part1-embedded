import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SendingFrame extends Thread{
	static int port = 6666;
	static ServerSocket serverSocket;
	static Socket socket;
	public static boolean flag = false; 
	  public void run() {
		 
          while (true) {
        	  
				try {
					socket = serverSocket.accept();
					Main.out= socket.getOutputStream();
					flag = true;
				} catch (IOException e) {
					System.out.println(String.format("connection_prob"));
					e.printStackTrace();
				}
              }
          }
}
