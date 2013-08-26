package umass.socketsInterface.server;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	public static void main(String[] args){

		int port = -1;
		int numThreads = -1;
		int backlog = -1; //max number of queued connections; will reject further connections.

		//Ensure that the correct number of arguments have been passed.
		if(!(args.length == 2 || args.length == 3)){
			System.out.println("Usage: <port> <numThreads> [<backlog>]");
			System.exit(-1);
		}
		
		//Logic for setting the input arguments correctly.
		switch(args.length){
			case 2: port = Integer.parseInt(args[0]);
					numThreads = Integer.parseInt(args[1]);
					backlog = 50; //50 is the default for TCP Sockets.
					break;
			case 3: port = Integer.parseInt(args[0]);
					numThreads = Integer.parseInt(args[1]);
					backlog = Integer.parseInt(args[2]);
					break;
			default: System.out.println("Error parsing input arguments.");
		}
		
		
		//get the number of threads to use in the thread pool.
		System.out.println("System running with thread pool of size: " + numThreads);//debug
		
		//start up socket for accepting incoming connections.
		try {
			ServerSocket incomingSocket = new ServerSocket(port, backlog); //bind to 0.0.0.0 aka >all interfaces<
			incomingSocket.setReuseAddress(true);
			
			//get a copy of the instantiation of ProdConsStructures
			ProdConsStructures prodConsStruct = ProdConsStructures.getInstance();
			
			//initialize thread pool - creates and starts all threads.
			ThreadPool myThreadPool = new ThreadPool(numThreads);
			
			//print out status message
			System.out.println("Server listening on port: " + incomingSocket.getLocalPort());
			System.out.println("Server address is: " + incomingSocket.getInetAddress().getHostAddress());
			System.out.println("Server receive buffer size is: " + incomingSocket.getReceiveBufferSize());
			
			//accept a connection, add it to the producer-consumer queue to feed the threads.
			while(true){
				Socket newConn = incomingSocket.accept();
				System.out.println("Main: Accepted a new connection.");
				prodConsStruct.insertPendingConn(newConn);
			}
			
			//wait for all threads to end - not important, since the kernel will kill these threads.
			//myThreadPool.joinThreads();
			
			//clean up - not important, since SO_REUSEADDR is used.
			//incomingSocket.close();
			
		} catch (IOException e) {
			System.out.println("IOException in main thread.");
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}	
}
