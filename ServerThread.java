package icepod.FlightGuide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class ServerThread extends Thread {
	private static final String TAG = ServerThread.class.getSimpleName();
	
	private ServerSocket serverSocket;
	private int SERVERPORT = 6000;
	private String lastMessage;
	
	public ServerThread(int port) {
		super();
		SERVERPORT = port;
	}
	
	public void run() {
		Log.d(TAG,"Server running...");
		
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
					lastMessage = read;
				
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} // function run
	} // class
	
	public String getLastMessage() {
		return lastMessage;
	}
} // class ServerThread