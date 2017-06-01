import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class NotificationThread extends Thread {
	
	static int port_note = 6667;
	static ServerSocket serverSocket_note;
	static Socket socket_note;
	static OutputStream out_note;
	public InputStream in_note;
	public byte p;
	public int myNotifId;
	public boolean sendNotif = false;
	public boolean continue_sending = false;

	public NotificationThread(){
		try {
			serverSocket_note = new ServerSocket(port_note);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(String.format("problem2"));
		}
		
	}
	@Override
	public void run() {
		while(true){
			if(sendNotif){
				try {
					while(continue_sending){
					socket_note = serverSocket_note.accept();
					out_note = socket_note.getOutputStream();
					in_note = socket_note.getInputStream();
					out_note.write(p);
					out_note.flush();
					if (p==1) System.out.println("1st notif sent..........................");
					if (p==2) System.out.println("2nd vdo generated notif sent.......................");
					int q = in_note.read();
					if(q==9)continue_sending = false;
					DataOutputStream dout_note = new DataOutputStream(out_note);
					dout_note.writeInt(myNotifId);
					dout_note.flush();
					sendNotif = false;
					socket_note.close();
					}
					continue_sending = true;
					// System.out.println(String.format("connected"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(String.format("connection_prob2"));
					e.printStackTrace();
				}
			}else{
				try {
					Thread.sleep(0,10000);
				} catch (InterruptedException e1){
					e1.printStackTrace();
				}
			}
	}
	}
}
