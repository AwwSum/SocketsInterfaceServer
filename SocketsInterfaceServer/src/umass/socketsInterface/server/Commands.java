package umass.socketsInterface.server;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * Singleton utility class for defining and parsing command messages from strings.
 * 
 */


public class Commands {
	//holds the list of valid commands/command prefixes.
	private static Vector<String> commandList = new Vector<String>();
	
	//lock and instance for singleton pattern.
	private static Lock constructorLock = new ReentrantLock();
	private static Commands instance = null;
	
	//instance retrieval method for singleton pattern.
	public static Commands getInstance(){
		constructorLock.lock();
		if(instance == null){
			instance = new Commands();
		}
		constructorLock.unlock();
		return instance;
	}
	
	//add new commands and command prefixes here
	private Commands(){
		commandList.add("CONNECT_ACCEPT");
		commandList.add("CONNECT");
		commandList.add("DATA");
	}
	
	/*
	 * Parses the command prefix of a string, and returns it.
	 * 
	 * Returns null if the string does not contain a command prefix.
	 * 
	 * Returns the first match.
	 */
	public String parseCommand(String input){
		input = input.trim(); //remove leading/trailing whitespace.
		for(int i = 0; i < commandList.size(); i++){
			if(input.indexOf(commandList.get(i)) != -1){
				return commandList.get(i);
			}
		}
		return "UNRECOGNIZED";
	}
	
	//given a CONNECT <DEST_IPADDR> <DEST_PORT> <SRC_IPADDR> <SRC_PORT> request, returns an InetSocketAddress from <DEST_IPADDR>, <DEST_PORT>
	public InetSocketAddress parseAddr_Conn(String connectReqStr){
		String inString = connectReqStr.trim(); //clean the input string
		String[] args = inString.split(" ");
	
		InetSocketAddress IP;
		try {
			IP = new InetSocketAddress(InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
			return IP;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	//given a CONNECT_ACCEPT <DEST_IPADDR> <DEST_PORT> <SRC_IPADDR> <SRC_PORT> request, returns an InetSocketAddress from <SRC_IPADDR>, <SRC_PORT>
	public InetSocketAddress parseAddr_ConnAccept(String connectReqStr){
		String inString = connectReqStr.trim(); //clean the input string
		String[] args = inString.split(" ");
	
		InetSocketAddress IP;
		try {
			IP = new InetSocketAddress(InetAddress.getByName(args[3]), Integer.parseInt(args[4]));
			return IP;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	//given a CONNECT_ACCEPT <DEST_IPADDR> <DEST_PORT> <SRC_IPADDR> <SRC_PORT> request, returns an InetSocketAddress from <DEST_IPADDR>, <DEST_PORT>
	public InetSocketAddress parseAddr_Data(String datagramStr){
		String inString = datagramStr.trim(); //clean the input string
		String[] args = inString.split(" ");
	
		InetSocketAddress IP;
		try {
			IP = new InetSocketAddress(InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
			return IP;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
