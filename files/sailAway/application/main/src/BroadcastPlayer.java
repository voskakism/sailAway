import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import com.espertech.esper.client.EPServiceProvider;
import java.util.concurrent.atomic.AtomicInteger;

class BroadcastPlayer implements Runnable
{
	private Vessel vessel;
	private BroadcastSequence sequence;
	private Terrain map;
	private EPServiceProvider engine;
	private File aisLogFile;
	private AtomicInteger tripsReady;
	private Object lock;
	
	BroadcastPlayer(Vessel vessel, BroadcastSequence sequence, Terrain map, EPServiceProvider engine, File aisLogFile, AtomicInteger tripsReady, Object lock)
	{
		this.vessel = vessel;
		this.sequence = sequence;
		this.map = map;
		this.engine = engine;
		this.aisLogFile = aisLogFile;
		this.tripsReady = tripsReady;
		this.lock = lock;
	}
	
	public void run()
	{
		LinkedList<Broadcast> broadcasts = sequence.getBroadcasts();
		ListIterator<Broadcast> li = broadcasts.listIterator();
		AISBroadcast ais;
		
		// synchronization point
		synchronized(lock){
			tripsReady.incrementAndGet();
			try{
				lock.wait(25000);
			} catch(InterruptedException ie){
				ie.printStackTrace();
			}
		}
		
		while(li.hasNext()){
			Broadcast broadcast = li.next();
			ais = new AISBroadcast(System.currentTimeMillis(), this.vessel, broadcast);
			engine.getEPRuntime().sendEvent(ais);
			if(map != null) map.beep(ais);
			ais.toConsole();
			ais.toFile(aisLogFile);
			try{
				Thread.sleep(AppConfiguration.getBroadcastInterval());
			} catch(InterruptedException ie){
				ie.printStackTrace();
			}
		}
	}
}