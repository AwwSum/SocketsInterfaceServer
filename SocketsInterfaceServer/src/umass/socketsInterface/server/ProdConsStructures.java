package umass.socketsInterface.server;
/*	
 * Contains synchronized producer-consumer data structures and encapsulates their behavior.
 * 
 * 	Functions as a means of passing messages between threads
 */
import java.util.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


//TODO: separate queues for sender/receiver client connections. 
public class ProdConsStructures {
	//for singleton pattern
	private static boolean hasBeenInstantiated = false;
	private static ProdConsStructures theInstance;
	
	//locks
	private static Lock constructorLock = new ReentrantLock();
	private static Lock pendingConnLock = new ReentrantLock();
	private static Lock streamMapLock = new ReentrantLock();
	
	//condition variables
	private static Condition hasPendingConn = pendingConnLock.newCondition();
	
	//data structures
	//queues client-side sockets of clients connecting to the server.
	private static Queue<Socket> pendingServerConnections;
	//maps an IP/port tuple to the OutputStream of the thread handling that connection.
	private static Map<InetSocketAddress, OutputStream> streamMap;
	
	//private constructor due to singleton pattern
	private ProdConsStructures(){
		pendingServerConnections = new ConcurrentLinkedQueue<Socket>();
		streamMap = new LinkedHashMap<InetSocketAddress, OutputStream>();
	}
	
	//singleton "constructor"
	public static ProdConsStructures getInstance(){
		constructorLock.lock();
		if(hasBeenInstantiated == false){
			theInstance = new ProdConsStructures();
			hasBeenInstantiated = true;
		}
		constructorLock.unlock();
		return theInstance;
	}
	
	public void insertPendingConn(Socket conn){
		pendingConnLock.lock();
		pendingServerConnections.add(conn);
		hasPendingConn.signal();
		pendingConnLock.unlock();
	}
	
	//pick up a socket, and update the mapping appropriately.
	public Socket getPendingConn(PipedInputStream threadPipe){
		pendingConnLock.lock();
		//return null if no elements in pendingConnections; like a better version of remove();
		while( pendingServerConnections.size() == 0){
			try {
				hasPendingConn.await();
			} catch (InterruptedException e) {
				System.out.println("ProdConsStructures::getPendingConn() Interrupted.");
				e.printStackTrace();
			}
		}
		Socket sock = pendingServerConnections.poll(); //removes element and returns value.
		//update the "routing table" map of ip/port tuples to PipedOutputStreams
		streamMapLock.lock();
		try {
			InetSocketAddress addrTuple = new InetSocketAddress(sock.getInetAddress(), sock.getPort());
			PipedOutputStream threadStream = new PipedOutputStream(threadPipe); //create a new output stream that pipes to the calling thread's input stream
			streamMap.put(addrTuple, threadStream);
		} catch (IOException e) {
			System.out.println("ProdCons: Problem associating address with internal stream");
			e.printStackTrace();
		}
		streamMapLock.unlock();
		pendingConnLock.unlock();
		return sock;
	}
	
	//Looks up the stream associated with the thread associated with the InetSocketAddress specified.
	public OutputStream streamMapLookup(InetSocketAddress peerAddr){
		streamMapLock.lock();
		OutputStream retStream = streamMap.get(peerAddr);
		streamMapLock.unlock();
		return retStream;
	}
}
