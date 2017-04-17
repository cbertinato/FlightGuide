package icepod.FlightGuide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class ClientThread extends Thread {
	
	private static final String TAG = TCPclient.class.getSimpleName();
	
	private Socket socket = null;
	private int SERVERPORT = 5000;
	private String SERVER_IP;
	private boolean connectionStatus = false;
	CommunicationThread commsThread;
	
	public ClientThread(String ip) {
		SERVER_IP = ip;
	}
	
	public ClientThread(String ip, int port) {
		SERVER_IP = ip;
		SERVERPORT = port;
	}
	
	public void run() {
		
		Log.d(TAG,"Network client started...");
		
		while (true) {
			while (socket == null) {
				try {
					InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
					socket = new Socket(serverAddr,SERVERPORT);
				
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
					Log.d(TAG,"Unknown host");
				
				} catch (IOException e2) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					
					connectionStatus = false;
					Log.d(TAG,"Trying again...");
				}
			}
			
			// DEBUG
			Log.d(TAG,"Connection made.");
			
			commsThread = new CommunicationThread(socket);
			// Connection status inside run comms thread
			commsThread.run();
			
			Log.d(TAG,"Connection died.");
			socket = null;
		}
			
	}
	
	public String[] getOutput() {	
		String[] output = parseGPS();
		
		// DEBUG
		//Log.d(TAG,"Lat=" + output[0] + ", Lon=" + output[1]);
		
		return output;
	}
	
	public boolean getStatus() {
		return connectionStatus;
	}
	
	public void setStatus(boolean connected) {
		connectionStatus = connected;
	}
	
	private String[] parseGPS() {
		
		String input = commsThread.getOutput();
		//Log.d(TAG,"input=" + input);
		
		// Get lat and lon
		int i = input.indexOf("Lat");
		String inter = input.substring(i);
		String[] parts1 = inter.split("\\s");
		String[] parts2 = parts1[0].split("=");
		String[] parts3 = parts1[1].split("=");
		
		// Get heading (azimuth)
		i = input.indexOf("azimuth=");
		inter = input.substring(i);
		parts1 = inter.split("\\s");
		
		String[] pos = new String[3];
		pos[0] = parts2[1];
		pos[1] = parts3[1];
		pos[2] = parts1[0];
		
		return pos;
	}

	class CommunicationThread implements Runnable {
		private Socket clientSocket;
		private BufferedReader input;
		private String output;
		
		public CommunicationThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
			
			try {
				this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			
			Log.d(TAG,"Connection up.");
			
			while(!Thread.currentThread().isInterrupted()) {
				try {
					
					// If the input is null, the connection has died.
					if ((output = input.readLine()) == null) {
						connectionStatus = false;
						break;
					}
					
					connectionStatus = true;
					
					// DEBUG
					//Log.d(TAG,"Comm thread output=" + output);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public String getOutput() {
			return output;
		}
	} // class CommunicationThread
} // class ClientThread