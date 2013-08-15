package umass.socketsInterface.server;
/*
 * Should handle all of the behind-the-scenes management of the working thread pool.
 * 
 * 	(for example, it should dynamically resize the pool when needed)
 * 
 */
import java.util.Vector;

public class ThreadPool {

	Vector<ServerThread> threadVec;
	
	ThreadPool(int numThreads){
		threadVec = new Vector<ServerThread>(numThreads);
		for(int i = 0; i < numThreads; i++){
			threadVec.add(i, new ServerThread());
			new Thread(threadVec.get(i)).start(); //create the thread and start it.
		}
	}
	
	//is a signal to the threadpool that most/all threads are busy; is a plea to add more threads.
	synchronized void notifyLowOnThreads(){
		if(threadVec.size() < 1000){
			ServerThread newThread = new ServerThread();
			threadVec.add(newThread);
		}
		else{
			;//do nothing; don't spawn more than 1000 threads!
		}
	}
	
	/*
	 * Attempts to join all threads in the thread pool. 
	 */
	synchronized void joinThreads(){
		for(int i = 0; i < threadVec.size(); i++){
			try {
				threadVec.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}//end ThreadPool