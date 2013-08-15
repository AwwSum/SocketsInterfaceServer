package umass.socketsInterface.server;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	public static void main(String[] args){

		int port = -1;
		int numThreads = -1;
		int maxNumConnections = -1;
		int backlog = -1; //max number of queued connections; will reject further connections.

		//Ensure that the correct number of arguments have been passed.
		if(!(args.length == 3 || args.length == 4)){
			System.out.println("Usage: <port> <numThreads> <maxNumConnections> [<backlog>]");
			System.exit(-1);
		}
		
		//Logic for setting the input arguments correctly.
		switch(args.length){
			case 3: port = Integer.parseInt(args[0]);
					numThreads = Integer.parseInt(args[1]);
					maxNumConnections = Integer.parseInt(args[2]);
					backlog = 10;
					break;
			case 4: port = Integer.parseInt(args[0]);
					numThreads = Integer.parseInt(args[1]);
					maxNumConnections = Integer.parseInt(args[2]);
					backlog = Integer.parseInt(args[3]);
					break;
			default: System.out.println("Error parsing input arguments.");
		}
		
		
		//get the number of threads to use in the thread pool.
		System.out.println("System running with thread pool of size: " + numThreads);//debug
		
		//start up socket for accepting incoming connections.
		try {
			InetAddress localAddress = InetAddress.getByName("localhost");
			ServerSocket incomingSocket = new ServerSocket(port, backlog, localAddress);
			incomingSocket.setReuseAddress(true);
			//incomingSocket.setSoTimeout(60000); 
			
			//get a copy of the instantiation of ProdConsStructures
			ProdConsStructures prodConsStruct = ProdConsStructures.getInstance();
			
			//initialize thread pool
			ThreadPool myThreadPool = new ThreadPool(numThreads);
			
			//print out status message
			System.out.println("Server listening on port: " + incomingSocket.getLocalPort());
			System.out.println("Server address is: " + incomingSocket.getInetAddress().getHostAddress());
			System.out.println("Server receive buffer size is: " + incomingSocket.getReceiveBufferSize());
			
			//accept a connection, add it to the producer-consumer queue to feed the threads.
			//TODO: make this some kind of infinite loop - it is currently just a few connections.
			for(int i = 0; i < maxNumConnections; i++){
				Socket newConn = incomingSocket.accept();
				System.out.println("Main: Accepted a new connection.");
				prodConsStruct.insertPendingConn(newConn);
			}
			
			//wait for all threads to end
			myThreadPool.joinThreads();
			
			//clean up
			incomingSocket.close();
			
		} catch (IOException e) {
			System.out.println("IOException in main thread.");
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		/*
		while(true){
			
			
		}*/
		

			
	}

	
	
}
