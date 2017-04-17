package icepod.FlightGuide;

//import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;

public class TCPclient extends Activity {
	private static final String TAG = TCPclient.class.getSimpleName();
	
	private Socket socket;
	private static final int SERVERPORT = 5000;
	private static final String SERVER_IP = "10.0.0.2";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		new Thread(new ClientThread()).start();
		
	}
	
	class ClientThread implements Runnable {
		@Override
		public void run() {
			try {
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
				socket = new Socket(serverAddr,SERVERPORT);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	} // class ClientThread
	
	class CommunicationThread implements Runnable {
		private Socket clientSocket;
		private BufferedReader input;
		
		public CommunicationThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
			
			try {
				this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					String read = input.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	} // class CommsThread
	
}