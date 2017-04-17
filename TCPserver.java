package icepod.FlightGuide;

//import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;

@SuppressLint("NewApi")
public class TCPserver extends Fragment {
	
	private ServerSocket serverSocket;
	
	Thread serverThread = null;
	
	public static final int SERVERPORT = 6000;
	
	class ServerThread implements Runnable {
		public void run() {
			Socket socket = null;
			
			try {
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			while (!Thread.currentThread().isInterrupted()) {
				try {
					socket = serverSocket.accept();
					CommunicationThread commThread = new CommunicationThread(socket);
					new Thread(commThread).start();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} // function run
	} // class ServerThread
	
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
		} // constructor
		
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					String read = input.readLine();
				
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} // function run
		
	} // class CommunicationThread
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.serverThread = new Thread(new ServerThread());
		this.serverThread.start();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
} // class TCPserver