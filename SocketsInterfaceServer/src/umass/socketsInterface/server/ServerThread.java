package umass.socketsInterface.server;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.BufferedReader;

/*
 * Thread class for the thread pool used by the server to communicate
 * 	with clients after the main thread has accept()'ed their connections
 * 	and placed their sockets into ProdConsStructures.
 * 
 */

public class ServerThread extends Thread{
	private Socket currSock;
	private BufferedWriter outStream;
	private BufferedReader inStream;
	private ProdConsStructures prodCons;
	private Commands commands;
	private PipedInputStream interThreadStream; //used by other threads to write to this threads' connected client.
	
	public ServerThread(){
		//get singleton instance of ProdConsStructures, which handles message passing between threads.s
		this.prodCons = ProdConsStructures.getInstance();
		this.commands = Commands.getInstance();
		this.interThreadStream = new PipedInputStream();
		
		//For debugging, mainly.
		System.out.println("ServerThread: Thread " + this.getId() + " Started.");
	}
	
	public void run(){
		
		currSock = prodCons.getPendingConn(interThreadStream);
		
		if(currSock != null){
			try {
				System.out.println("ServerThread: Server Thread Has Picked Up Client with IP: " + currSock.getInetAddress().getHostAddress() + " and port: " + currSock.getPort());
				//initiate streams
				inStream = new BufferedReader(new InputStreamReader(currSock.getInputStream()) );
				outStream = new BufferedWriter(new OutputStreamWriter(currSock.getOutputStream()) );
				
				//write to client - temporarily commented out
				//outStream.write("You have connected to the server!");
				//outStream.newLine();
				//outStream.flush();
				
				//receive from client
				String inFromClient;
				String inFromThread;
				while(true){
					//process data sent from this thread's connected client.
					if(inStream.ready()){
						inFromClient = inStream.readLine();
						System.out.println("ServerThread " + this.getId() + ": read from client: " + inFromClient);
						processString(inFromClient);
					}
					sleep(10); //greatly reduces CPU consumption
					//process data sent to this thread from other threads (on behalf of their respective connected clients.)
					if(interThreadStream.available() > 0){ //if there are bytes available to be read from the socket, then read them.
						byte[] inBuf = new byte[10240];
						interThreadStream.read(inBuf);
						inFromThread = new String(inBuf).trim();
						//System.out.println("DEBUG TEMP: " + inFromThread + " " + this.getId());
						System.out.println("ServerThread " + this.getId() + ": read from another thread: " + inFromThread);
						processString(inFromThread);
					}
				}
			} catch (IOException e) {
				System.out.println("ServerThread " + this.getId() + ": Exception in thread.");
				e.printStackTrace();
			} catch (InterruptedException e) {
				//do nothing. It's fine if the sleep is interrupted.
			}
		}
		else{
			System.out.println("ServerThread " + this.getId() + "pulled a null socket.");
		}
	}
	
	/*
	 * Checks the destination of the command (if applicable) and
	 * 	forwards the command appropriately. This means that
	 * 	it is either forwarded to this thread's connected client,
	 * 	or piped to a special input stream of the thread which is
	 * 	currently connected to the destination client.
	 */
	private void processString(String inStr){
		//parse command string from the input string
		String cmd = this.commands.parseCommand(inStr);
		
		try{
			OutputStream myStream = this.getMyStream(); // hold onto this so we can use it below
			//detect commands and take action; either forward it to the connected client, or to another thread.
			
				if(cmd.equals("CONNECT")){ 
					System.out.println("ServerThread " + this.getId() + ": received CONNECT message.");
					InetSocketAddress peerAddr = commands.parseAddr_Conn(inStr);
					OutputStream threadStream = prodCons.streamMapLookup(peerAddr); //stream to send data to thread connected to peerAddr
					
					if(!threadStream.equals(myStream)){ //if this thread isn't responsible for the client (it probably isn't)
						BufferedWriter outStreamToThread = new BufferedWriter(new OutputStreamWriter(threadStream));
						outStreamToThread.write(inStr); //then forward the message over to the right server thread
						outStreamToThread.newLine();
						outStreamToThread.flush();
					}
					else{ //else, a client may want to connect to itself for some reason, or we received a request from another server thread.
						outStream.write(inStr);
						outStream.newLine();
						outStream.flush();
					}
				}
				else if(cmd.equals("CONNECT_ACCEPT")){
					System.out.println("ServerThread " + this.getId() + ": received CONNECT_ACCEPT message.");
					InetSocketAddress destAddr = commands.parseAddr_ConnAccept(inStr);
					OutputStream destThreadStream = prodCons.streamMapLookup(destAddr); //echo the message to its destination.
					
					if(!destThreadStream.equals(myStream)){ //forward the message to the appropriate server thread
						BufferedWriter outStreamToOtherThread = new BufferedWriter(new OutputStreamWriter(destThreadStream));
						outStreamToOtherThread.write(inStr); //then forward the message over to the right server thread
						outStreamToOtherThread.newLine();
						outStreamToOtherThread.flush();
					}
					else{ //pass the message on to the client connected to this server thread
						outStream.write(inStr);
						outStream.newLine();
						outStream.flush();
					}
				}
				else if(cmd.equals("DATA")){
					System.out.println("ServerThread " + this.getId() + ": received DATA message.");
					InetSocketAddress dataDestAddr = commands.parseAddr_Data(inStr);
					OutputStream destDataStream = prodCons.streamMapLookup(dataDestAddr); //echo the message to its destination server thread
					
					if(!destDataStream.equals(myStream)){ //forward the message to the appropriate server thread
						BufferedWriter outStreamToOtherThread = new BufferedWriter(new OutputStreamWriter(destDataStream));
						outStreamToOtherThread.write(inStr); //then forward the message over to the right server thread
						outStreamToOtherThread.newLine();
						outStreamToOtherThread.flush();
					}
					else{ //pass the message on to the client connected to this server thread
						outStream.write(inStr);
						outStream.newLine();
						outStream.flush();
					}
				}
				else{
					System.out.println("ServerThread " + this.getId() + ": received unrecognized command '" + inStr + "' from connected client.");
				}
			
		}catch (IOException e) {
			System.out.println("ServerThread " + this.getId() + ": Exception in thread.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Returns the OutputStream associated with this particular thread
	 */
	private OutputStream getMyStream(){
		InetAddress myIP = currSock.getInetAddress();
		int myPort = currSock.getPort();
		InetSocketAddress myAddr = new InetSocketAddress(myIP, myPort);
		OutputStream myStream = prodCons.streamMapLookup(myAddr);
		return myStream;
	}
}
