import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.io.File;
import javax.swing.JFrame;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import java.util.concurrent.atomic.AtomicInteger;
import com.espertech.esper.client.*;

public class Application
{
	public static void main(String[] args)
	{
		String sep = System.getProperty("file.separator");
		
		///////////////////////////////////////////////////////////////////////////////////// APPLICATION CONFIGURATION
		File appConfigFile = new File(".." + sep + ".." + sep + ".." + sep + "inputFiles" + sep + "configuration.csv");
		new AppConfigurationCSVParser(appConfigFile).getData(true);
		
		////////////////////////////////////////////////////////////////////////////////////////////// USER INPUT FILES
		File islandDirectory = new File (".." + sep + ".." + sep + ".." + sep + "inputFiles" + sep + "islands");
		File legitTripDir = new File(".." + sep + ".." + sep + ".." + sep + "inputFiles" + sep + "trips" + sep + "legitimate");
		File illegalTripDir = new File(".." + sep + ".." + sep + ".." + sep + "inputFiles" + sep + "trips" + sep + "illicit");
		
		////////////////////////////////////////////////////////////////////////////////////////////////// OUTPUT FILES
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE d MMMM yyyy HH_mm_ss");
		String formattedDate = sdf.format(date);
		String[] dateFragments = formattedDate.split(" ", 3);
		String weekDay = dateFragments[0];
		String monthDay = dateFragments[1];
		String restOfDate = dateFragments[2];
		String monthDaySuffix = "th";
		switch(monthDay){
			case "1":
			case "21":
			case "31":
				monthDaySuffix = "st";
				break;
			case "2":
			case "22":
				monthDaySuffix = "nd";
				break;
			case "3":
			case "23":
				monthDaySuffix = "rd";
				break;
		}
		String runInstanceFolder = weekDay + " the " + monthDay + monthDaySuffix + " of " + restOfDate;
		File instanceOutputPath = new File(".." + sep + ".." + sep + ".." + sep + "outputFiles" + sep + runInstanceFolder + sep);
		instanceOutputPath.mkdirs();
		File eventReport = new File(instanceOutputPath + sep + "Event Report.txt");
		
		//////////////////////////////////////////////////////////////////////// ESPER CONFIGURATION AND EPL STATEMENTS
		EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
		engine.getEPAdministrator().getConfiguration().addEventType(AISBroadcast.class);
		engine.getEPAdministrator().getConfiguration().addVariable("speedLimit", Double.class, new Double(AppConfiguration.getGlobalSpeedLimit()));
		engine.getEPAdministrator().getConfiguration().addPlugInSingleRowFunction("angle", Application.class.getName(), "esper_angleFinder");
		
		String overspeedReportedEPL = 	"select vessel, speed " +
										"from AISBroadcast " +
										"where speed > speedLimit";
		EPStatement overspeedReportedStatement = engine.getEPAdministrator().createEPL(overspeedReportedEPL);
		OverspeedReportedListener overspeedReportedListener = new OverspeedReportedListener(eventReport);
		overspeedReportedStatement.addListener(overspeedReportedListener);
		
		/*String epl = 	"insert into RoTStreamselect vessel, position, speed " +
						"from AISBroadcast#win:5" +
						"where speed > speedLimit";
		EPStatement statement = engine.getEPAdministrator().createEPL(epl);
		MyListener ml = new MyListener(eventReport);
		statement.addListener(ml);*/
		
	
	
		/*select * from TemperatureEvent "
		match_recognize (
		measures A as temp1, B as temp2
		pattern (A B)
		define 
		A as A.temperature > 400,
		B as B.temperature > 400)
		
		select * from TemperatureEvent
		match_recognize (
		measures A as temp1, B as temp2, C as temp3, D as temp4
		pattern (A B C D)
		define
		A as A.temperature > 100,
		B as (A.temperature < B.value),
		C as (B.temperature < C.value),
		D as (C.temperature < D.value) and D.value >
		(A.value * 1.5))*/
		
		String epl = 	"select * from AISBroadcast " +
						"match_recognize( " +
						"partition by vessel.mmsi " +
						"measures A as a, B as b " +
						"pattern (A B)" +
						"define" +
						" B as ((angle(B.heading, A.heading) / B.speed) > 5))"; // angle in degrees, speed in m/s
		EPStatement myStatement = engine.getEPAdministrator().createEPL(epl);
		MyListener ml = new MyListener(eventReport);
		myStatement.addListener(ml);
			
		/*
		String epl_1 = "insert into RoTStream from AISBroadcast where vessel.speed = esper_angleFinder()";
		EPStatement myStatement = engine.getEPAdministrator().createEPL(epl_1);
		/*MyListener ml = new MyListener(eventReport);
		myStatement.addListener(ml);*/
		/*
		String epl2 = "select mmsi from RoTStream where ((rot / speed) > 5)";
		EPStatement myStatement = engine.getEPAdministrator().createEPL(epl2);
		MyListener ml = new MyListener(eventReport);
		myStatement.addListener(ml);*/
		
		/////////////////////////////////////////////////////////////////////////////////////////////// ISLAND CREATION
		Sea sea = new Sea();
		Island i;
		for(File csv : islandDirectory.listFiles()){ // returns files AND directories
			try{
				if(csv.isDirectory()){ // ignore directories
					continue;
				}
				System.out.println("Found file: " + csv.getName());
				i = new CSVIsland(csv).getData(true);
				sea.addIsland(i);
			} catch(CSVInputException e){
				Throwable t = e;
				investigateException(t);
			}
		}
		
		System.out.println("Number of islands created: " + sea.getIslands().size());///////////////////////////////////
		for(Island is : sea.getIslands()){/////////////////////////////////////////////////////////////////////////////
			System.out.println(is.getName());//////////////////////////////////////////////////////////////////////////
		}//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		///////////////////////////////////////////////////////////////////////////////////////////// GUI CONFIGURATION
		Terrain map = null;
		if(AppConfiguration.getGuiMode() != GuiMode.NOGUI){
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int width = (int)screenSize.getWidth();
			int height = (int)screenSize.getHeight();
			JFrame f = new JFrame("Sail Away");
			f.setIconImage(new ImageIcon(".." + sep + ".." + sep + ".." + sep + "media" + sep + "icons" + sep + "anchor.png").getImage());
			f.setSize(width, height);
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.setResizable(false);
			if(sea.islandCount() > 0){
				if(AppConfiguration.getGuiMode() == GuiMode.FIXED){
					map = new FixedTerrain(width, height, sea);
				} else{
					map = new ResizableTerrain(width, height, sea);
				}
			} else{
				if(AppConfiguration.getGuiMode() != GuiMode.RESIZABLE_TO_TRAILS) System.out.println("No islands on sea, setting GUI Mode to RESIZABLE_TO_TRAILS");
				AppConfiguration.setGuiMode(GuiMode.RESIZABLE_TO_TRAILS);
				map = new ResizableTerrain(width, height, sea);
			}
			f.add(map);
			f.setVisible(true);
		}
		
		LinkedList<Thread> trips = new LinkedList<Thread>();
		Fleet fleet = new Fleet();
		Object lock = new Object();
		int lnavCount = 0;
		int banditCount = 0;
		int tripCount = 0;
		AtomicInteger tripsReady = new AtomicInteger(0);
		
		// Spawn legitimate trips
		for(File fileEntry : legitTripDir.listFiles()){
			if(fileEntry.isDirectory()){
				System.out.println(fileEntry.getName());////////////////////////////////////////////////////////////////////
				
				TripParameters tripParameters = null;
				Vessel vessel = null;
				Itinerary route = null;
				boolean parametersFound = false;
				boolean vesselFound = false;
				boolean routeFound = false;
				boolean validParameters = false;
				boolean validVessel = false;
				boolean validRoute = false;
				for(File csv : fileEntry.listFiles()){
					String csvFileName = csv.getName();
					System.out.println(csvFileName); ////////////////////////////////////////////////////////////////////////
					switch(csvFileName){
						case "parameters.csv":
							parametersFound = true;
							try{
								tripParameters = new CSVTripParameters(csv).getData(true);
								validParameters = true;
							} catch(CSVInputException e){
								System.out.println("Error reading file \"parameters.csv\". Using default parameters.");
								Throwable t = e;
								investigateException(t);
							}
							break;
						case "vessel.csv":
							vesselFound = true;
							try{
								vessel = new CSVVessel(csv).getData(true);
								fleet.registerVessel(vessel);
								validVessel = true;
							} catch(CSVInputException e){
								Throwable t = e;
								investigateException(t);
							}
							break;
						case "waypoints.csv":
							routeFound = true;
							try{
								route = new CSVWaypoints(csv, sea).getData(true);
								validRoute = true;
							} catch(CSVInputException e){
								Throwable t = e;
								investigateException(t);
							}
							break;
					}
				}
				if(validParameters && validVessel && validRoute){
					try{
						trips.add(new Thread(new Lnav(vessel, tripParameters, route, map, engine, tripsReady, lock)));
						System.out.println("creating Lnav.");//////////////////////////////////////////////////////////
						lnavCount++;
					} catch(Exception e){
						e.getMessage();
						e.printStackTrace();
					}
				} else{
					if(!parametersFound){System.out.println("Missing file \"parameters.csv\"");}
					if(!vesselFound){System.out.println("Missing file \"vessel.csv\"");}
					if(!routeFound){System.out.println("Missing file \"waypoints.csv\"");}
				}
				System.out.println();
			}
		}
		
		// Spawn illicit trips
		for(File fileEntry : illegalTripDir.listFiles()){
			if(fileEntry.isDirectory()){
				System.out.println(fileEntry.getName());/////////////////////////////////////////////////////////////////////
				
				Vessel vessel = null;
				BroadcastSequence sequence = null;
				boolean vesselFound = false;
				boolean sequenceFound = false;
				boolean validVessel = false;
				boolean validSequence = false;
				for(File csv : fileEntry.listFiles()){
					String csvFileName = csv.getName();
					System.out.println(csvFileName); ////////////////////////////////////////////////////////////////////////
					switch(csvFileName){
						case "vessel.csv":
							vesselFound = true;
							try{
								vessel = new CSVVessel(csv).getData(true);
								fleet.registerVessel(vessel);
								validVessel = true;
							} catch(CSVInputException e){
								Throwable t = e;
								investigateException(t);
							}
							break;
						case "broadcasts.csv":
							sequenceFound = true;
							try{
								sequence = new CSVBroadcasts(csv).getData(true);
								validSequence = true;
							} catch(CSVInputException e){
								Throwable t = e;
								investigateException(t);
							}
							break;
					}
				}
				if(validVessel && validSequence){
					try{
						trips.add(new Thread(new BroadcastPlayer(vessel, sequence, map, engine, tripsReady, lock)));
						System.out.println("creating Bandit.");//////////////////////////////////////////////////////////
						banditCount++;
					} catch(Exception e){
						e.getMessage();
						e.printStackTrace();
					}
				} else{
					if(!vesselFound){System.out.println("Missing file \"vessel.csv\"");}
					if(!sequenceFound){System.out.println("Missing file \"broadcasts.csv\"");}
				}
				System.out.println();
			}
		}
		tripCount = lnavCount + banditCount;
		
		for(Thread t : trips){
			t.start();
		}
		
		do{
			try{
				Thread.sleep(50);
			} catch(InterruptedException ie){
				ie.printStackTrace();
			}
		} while(tripsReady.get() < tripCount);
		synchronized(lock){
			lock.notifyAll();
		}
		
		// no need to .join() Threads, apparently the main thread waits for them to finish. Anyway, this would be the code:
		/*for(Thread t : trips){
			try{
				t.join();
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}*/
	}
	
	public static double esper_angleFinder(Azimuth a, Azimuth b)
	{
		Azimuth turn = a.relativeTo(b);
		double angle = turn.getAzimuth();
		if(angle > 180){
			angle = 360 - angle;
		}
		return (angle);
	}
	
	private static void investigateException(Throwable t)
	{
		String arrowHead = "> ";
		String shaft = "--";
		System.out.println(shaft + arrowHead + t.toString());
		while((t = t.getCause()) != null){
			shaft += "--";
			System.out.println(shaft + arrowHead + "Caused by:");
			System.out.println(shaft + arrowHead + t.toString());
		}
	}
}