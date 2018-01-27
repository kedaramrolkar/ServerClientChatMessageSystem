
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;

public class Client  {

	private ObjectInputStream inputStream;		// to read from the socket
	private ObjectOutputStream outputStream;		// to write on the socket
	private Socket socket;

	private String username, server;
	private int port;

	public Client(int p, String u) {
		port = p;
		username = u;
		server = "127.0.0.1";
	}
	
	
	/**
	 * Method used to close all client resources after logout
	 */
	private void disconnect() {
		try { 
			if(inputStream != null) {
				inputStream.close();
			}
		} catch(Exception e) {
			System.out.println("Error in closing input");
		}
		
		try {
			if(outputStream != null) {
				outputStream.close();
			}
		} catch(Exception e) {
			System.out.println("Error in closing output");			
		}
        
		try {
			if(socket != null) {
				socket.close();
			}
		} catch(Exception e) {
			System.out.println("Error in closing socket");			
		}		
	}


	/**
	 * Initialization - 1) Start the listener thread that listens to the server
	 * 					2) Writes username once 
	 * @return false if any errors
	 */
	
	public boolean start() {

		try {
			socket = new Socket(server, port);
		} catch(Exception e) {
			System.out.println("Error connecting to server:" + e);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		System.out.println(msg);
	
		//input output streams
		try {
			inputStream  = new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			outputStream.flush();
		} catch (IOException eIO) {
			System.out.println("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		//client thread listening to server to receive data
		new ListenFromServer().start();
		
		//announce new client name for server
		try {
			outputStream.writeObject(username);
			outputStream.flush();
		} catch (IOException e) {
			System.out.println("Login exception : " + e);
			disconnect();
			return false;
		}
		return true;
	}

	/**
	 * Client will write to the out stream of the socket with the server
	 * @param msg is a ChatMessage object
	 */
	void sendMessage(ChatMessage msg) {
		
		//if simple text message, write it as is 
		if(msg.getOperation()==false){
			try {
				outputStream.writeObject(msg);
				outputStream.flush();
			} catch(IOException e) {
				System.out.println("Exception writing to server: " + e);
			}
		} else {	//read a file and fill the byte array
			FileInputStream fileStream = null;
		    BufferedInputStream bufferedStream = null;
		    
		    //message field will contain filename
		    String filePath = msg.getMessage();
		    File toSend = new File(filePath);	
			Path p = Paths.get(filePath);
			String fileName = p.getFileName().toString();
			msg.setMessage(fileName);
	        msg.fileBytes  = new byte [(int)toSend.length()];

	        //read the file into byte array of ChatMessage 
	        try {
				fileStream = new FileInputStream(toSend);
				bufferedStream = new BufferedInputStream(fileStream);
				bufferedStream.read(msg.fileBytes, 0, msg.fileBytes.length);
				outputStream.writeObject(msg);
				outputStream.flush();
			} catch (Exception e) {
				System.out.println("Error in sending file contents" + e);
			}
		}
	}
	
	/**
	 * Main driver program providing the options available
	 * @param args port to connect and name of the client
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		// port to connect
		int portNumber = Integer.parseInt(args[1]);
		String userName = args[0];

		Client client = new Client(portNumber, userName);
		if(!client.start()){
			return;			
		}
		
		Scanner scan = new Scanner(System.in);

		boolean continueProcess = true;
		
		while(continueProcess) {
			System.out.println("Options 1.Broadcast 2.Unicast 3.Blockcast 4.Logout");			
			int castChoice=0;
			try{
				castChoice = Integer.parseInt(scan.nextLine());
			} catch(Exception ex){
				System.out.println("Error reading option");
			}
			
			//logout
			if(castChoice==4){		
				client.sendMessage(new ChatMessage(ChatMessage.Logout, userName, "", false));
				continueProcess = false;
			} else if(castChoice==1 || castChoice==2 || castChoice==3) {
				String username = null;				
				if(castChoice==2 || castChoice==3) {
					System.out.println((castChoice==2 ? "Enter Recipient Username " : "Enter Username to be left out"));
					username = scan.nextLine();
				}
				else{
					username = userName;
				}
				System.out.println("Operation Type : 1.Text 2.File");
				int MsgFileChoice = 0;
				try {
						MsgFileChoice = Integer.parseInt(scan.nextLine());
				} catch(Exception ex){
					System.out.println("Error reading type");
				}
					
				if(MsgFileChoice==1){
						System.out.print("Enter Text: ");
						String message = scan.nextLine();
						client.sendMessage(new ChatMessage(castChoice, username, message, false));
				} else if(MsgFileChoice==2){
						System.out.println("Enter filepath: ");
						String filePath = scan.nextLine();
						client.sendMessage(new ChatMessage(castChoice, username, filePath, true));						
				} else{
						System.out.println("Invalid Entry..\n");
				} 
			} else {
				System.out.println("Invalid Entry. Please enter a valid number\n");
			}
		}
		scan.close();
		client.disconnect();
	}

	class ListenFromServer extends Thread {
		
		public final static int FILE_SIZE = 6022386; // file size temporary hard coded
		
		public void run() {
			FileOutputStream outputStream = null;
		    BufferedOutputStream bufferedStream = null;

			while(true) {
				try {
					ChatMessage msg = (ChatMessage) inputStream.readObject();
					if(!msg.getOperation()) {	//if not file operation
						System.out.println(msg.getMessage());
					} else {					// write file						
					    outputStream = new FileOutputStream(msg.getMessage());	  //has file name
					    bufferedStream = new BufferedOutputStream(outputStream);					    
					    bufferedStream.write(msg.fileBytes, 0 , msg.fileBytes.length);						
					    bufferedStream.flush();
					}
				} catch(Exception e) {
					System.out.println("Successfully Logged Out" +  e);
					break;
				} finally {
					if (outputStream != null) {
						try {
							outputStream.close();
						} catch (IOException e) {
							System.out.println("Error closing output stream" + e);
						}
					}
					if (bufferedStream != null){
						try {
							bufferedStream.close();
						} catch (IOException e) {
							System.out.println("Error closing buffered stream" + e);
						}
					}
				}
			}
		}
	}
}

