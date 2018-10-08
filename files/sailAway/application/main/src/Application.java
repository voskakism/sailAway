import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.io.File;
import javax.swing.JFrame;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Frame;
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
		File aisOutputPath = new File(".." + sep + ".." + sep + ".." + sep + "outputFiles" + sep + runInstanceFolder);
		File eventReportOutputPath = new File(aisOutputPath + sep + "Event Reports");
		eventReportOutputPath.mkdirs();
		File aisLogFile = new File(aisOutputPath + sep + "AIS Broadcasts.txt");
		
		/////////////////////////////////////////////////////////////////////////////////////////////// ISLAND CREATION
		Sea sea = new Sea();
		Island i;
		for(File csv : islandDirectory.listFiles()){ // returns files AND directories
			try{
				if(csv.isDirectory()){ // ignore directories
					continue;
				}
				if(AppConfiguration.getDebugStatus()) System.out.println("Found file: " + csv.getName());
				i = new CSVIsland(csv).getData(true);
				sea.addIsland(i);
			} catch(CSVInputException e){
				Throwable t = e;
				investigateException(t);
			}
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println("Number of islands created: " + sea.getIslands().size());
			for(Island is : sea.getIslands()){
				System.out.println(is.getName());
			}
		}
		
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
			f.setExtendedState(Frame.MAXIMIZED_BOTH);
		}
		
		//////////////////////////////////////////////////////////////////////// ESPER CONFIGURATION AND EPL STATEMENTS
		File overspeedReportedFile = new File(eventReportOutputPath + sep + "Reported Overspeed Events.txt");
		File overspeedCalculatedFile = new File(eventReportOutputPath + sep + "Calculated Overspeed Events.txt");
		File sharpTurnFile = new File(eventReportOutputPath + sep + "Sharp Turn Events.txt");
		File packageDeliveryFile = new File(eventReportOutputPath + sep + "Package Delivery Events.txt");
		File imminentCollisionFile = new File(eventReportOutputPath + sep + "Imminent Collision Events.txt");
		File amphibiousAssaultFile = new File(eventReportOutputPath + sep + "Amphibious Assault Events.txt");
		
		EPServiceProvider esperEngine = EPServiceProviderManager.getDefaultProvider();
		EPAdministrator esperAdministrator = esperEngine.getEPAdministrator();
		ConfigurationOperations esperConfiguration = esperAdministrator.getConfiguration();
		esperConfiguration.addEventType(AISBroadcast.class);
		//esperConfiguration.addImport(Broadcast.class);
		//esperConfiguration.addImport(Vessel.class);
		//esperConfiguration.addImport(Coordinates.class);
		esperConfiguration.addVariable("speedLimit", Double.class, new Double(AppConfiguration.getGlobalSpeedLimit()));
		esperConfiguration.addVariable("broadcastInterval", Integer.class, new Integer(AppConfiguration.getBroadcastInterval()));
		esperConfiguration.addVariable("sea", Sea.class, sea);
		esperConfiguration.addPlugInSingleRowFunction("angle", Azimuth.class.getName(), "angleFinder");
		esperConfiguration.addPlugInSingleRowFunction("distance", Coordinates.class.getName(), "calculateDistance");
		esperConfiguration.addPlugInSingleRowFunction("futurePosition", Application.class.getName(), "esper_predictFuturePosition");
		esperConfiguration.addPlugInSingleRowFunction("timeDifference", Application.class.getName(), "esper_findTimeDifference");
		esperConfiguration.addPlugInSingleRowFunction("landDistance", Application.class.getName(), "esper_findDistanceFromLand");
		
		// Reported Overspeed
		String overspeedReportedEPL = 	"SELECT vessel, speed " +
										"FROM AISBroadcast((vessel.type NOT IN (" +
											"VesselType.MILITARY, " +
											"VesselType.LAWENFORCEMENT, " +
											"VesselType.SEARCHANDRESCUE, " +
											"VesselType.MEDICAL)) OR (vessel.flag NOT IN (" +
											"Flag.GR))) " +
										"WHERE speed > speedLimit";
		EPStatement overspeedReportedStatement = esperAdministrator.createEPL(overspeedReportedEPL);
		OverspeedReportedListener overspeedReportedListener = new OverspeedReportedListener(overspeedReportedFile);
		overspeedReportedStatement.addListener(overspeedReportedListener);
		
		// Calculated Overspeed
		String broadcastDeltaEPL =		"CREATE SCHEMA DeltaAIS(mmsi long, name String, distanceCovered double, angleRotated double, timeTaken double)";
		String deltaWindowEPL =			"CREATE WINDOW DeltaEvents.win:length(3) AS DeltaAIS";
		String insertToDeltaWinEPL =	"INSERT INTO DeltaEvents " +
										"SELECT " +
										"	vessel.mmsi AS mmsi, " +
										"	vessel.name AS name, " +
										"	distance(position, prev(position)) AS distanceCovered, " +
										"	angle(heading, prev(heading)) AS angleRotated, " +
										"	timeDifference(timeStamp, prev(timeStamp)) AS timeTaken " +
										"FROM AISBroadcast#groupwin(vessel.mmsi)#length(2)";
		String overspeedCalculatedEPL =	"SELECT mmsi, name, distanceCovered, timeTaken, (distanceCovered / timeTaken) AS calculatedSpeed " +
										"FROM DeltaEvents " +
										"WHERE (distanceCovered / timeTaken) > speedLimit";
		EPStatement broadcastDeltaStatement = esperAdministrator.createEPL(broadcastDeltaEPL);
		EPStatement deltaWindowStatement = esperAdministrator.createEPL(deltaWindowEPL);
		EPStatement insertToDeltaWinStatement = esperAdministrator.createEPL(insertToDeltaWinEPL);
		EPStatement overspeedCalculatedStatement = esperAdministrator.createEPL(overspeedCalculatedEPL);
		OverspeedCalculatedListener overspeedCalculatedListener = new OverspeedCalculatedListener(overspeedCalculatedFile);
		overspeedCalculatedStatement.addListener(overspeedCalculatedListener);
		
		// Sharp Turn
		String sharpTurnEPL =	"SELECT mmsi, name, angleRotated, timeTaken, (angleRotated / timeTaken) AS rateOfTurn " +
								"FROM DeltaEvents " +
								"WHERE (angleRotated / timeTaken) > 40"; // angle in degrees, time in seconds.
		/* Sharp Turn: an alternative approach (requires listener modification, does not depend on DeltaAIS window):
		String sharpTurnEPL = 	"SELECT * " +
								"FROM AISBroadcast " +
								"MATCH_RECOGNIZE(" +
								"PARTITION BY vessel.mmsi " +
								"MEASURES A AS a, B AS b " +
								"PATTERN (A B) " +
								"DEFINE" +
								" B AS (B.speed * angle(B.heading, A.heading) > 60))";*/ // angle in degrees, speed in m/s.
		EPStatement sharpTurnStatement = esperAdministrator.createEPL(sharpTurnEPL);
		SharpTurnListener sharpTurnListener = new SharpTurnListener(sharpTurnFile);
		sharpTurnStatement.addListener(sharpTurnListener);
		
		// Package Delivery
		String SimpleBroadcastEPL = "CREATE SCHEMA SimpleBroadcast(time Date, velocity double, ship Vessel, site Coordinates)";
		String stoppagesWindowEPL =	"CREATE WINDOW Stoppages#length(25) AS SimpleBroadcast";
		String insertToStopWinEPL =	"INSERT INTO Stoppages" +
									" SELECT" +
									" b.lastOf().timeStamp AS time," +
									" b.lastOf().speed AS velocity," +
									" cast(b.lastOf().vessel, Vessel) AS ship," +
									" cast(b.lastOf().position, Coordinates) AS site " +
									"FROM AISBroadcast.win:time(900 sec) " +
									"MATCH_RECOGNIZE(" +
									"PARTITION BY (vessel.mmsi) " +
									"MEASURES A AS a, B AS b " +
									// De-comment the following 2 lines to detect overlapping patterns:
									//"ALL MATCHES " +
									//"AFTER MATCH SKIP TO CURRENT ROW " + // [ after match skip (past last row | to next row | to current row) ]
									"PATTERN (A{4,} B+ A{4,}) " +
									"DEFINE" +
									" A AS (A.speed > A.vessel.minimumSpeed)," +
									" B AS (B.speed <= B.vessel.minimumSpeed))";
									// Due to the "one-or-more" (+) quantifier in the PATTERN clause, B / b become de jure collections,
									// whose properties can be accessed by [index], or Esper's Enumeration Methods (chapter 11).
									// Depending on granularity / nesting level of said properties, the latter approach may not always work:
									// b.firstOf().speed <==> b[0].speed	// both work
									// b[0].vessel.mmsi						// works
									// b.firstOf().vessel.mmsi				// doesn't work
		String packageDeliveryEPL =	"SELECT " +
									"	a('time') AS timeOfDispense, " +
									"	c('time') AS timeOfRecovery, " +
									"	a('ship') AS dispenserVessel, " +
									"	c('ship') AS collectorVessel, " +
									"	a('site') AS siteOfDispense, " +
									"	c('site') AS siteOfRecovery, " +
									"	distance(cast(a('site'), Coordinates), cast(c('site'), Coordinates)) AS dist " +
									"FROM Stoppages " +
									"MATCH_RECOGNIZE(" +
									"MEASURES A AS a, C AS c " +
									"PATTERN (A B* C) " +
									"DEFINE" +
									" C AS (distance(C.site, A.site) <= 100) AND" +
									" (timeDifference(C.time, A.time) <= 600) AND" +
									" (C.ship.mmsi != A.ship.mmsi))";
		/* Package Delivery: an alternative approach (requires listener modification, does not depend on Stoppages events):
		String packageDeliveryEPL =	"SELECT * " +
									"FROM AISBroadcast.win:time(900 sec) " +
									"MATCH_RECOGNIZE(" +
									"MEASURES A AS a, B AS b, C AS c " +
									"ALL MATCHES " +
									"PATTERN (A B* C) " +
									"DEFINE" +
									" A AS (A.speed <= A.vessel.minimumSpeed)," +
									" C AS (C.speed <= C.vessel.minimumSpeed) AND" +
									" (distance(A.position, C.position) <= 100) AND" +
									" (C.timeStamp.getTime() - A.timeStamp.getTime() <= 600000) AND" +
									" (C.vessel.mmsi != A.vessel.mmsi))";*/
		EPStatement simpleBroadcastStatement = esperAdministrator.createEPL(SimpleBroadcastEPL);
		EPStatement stoppagesWindowStatement = esperAdministrator.createEPL(stoppagesWindowEPL);
		EPStatement insertToStopWinStatement = esperAdministrator.createEPL(insertToStopWinEPL);
		EPStatement packageDeliveryStatement = esperAdministrator.createEPL(packageDeliveryEPL);
		PackageDeliveryListener packageDeliveryListener = new PackageDeliveryListener(packageDeliveryFile);
		packageDeliveryStatement.addListener(packageDeliveryListener);
		
		// Imminent Collision
		String imminentCollisionEPL =	"SELECT *, current_timestamp() AS time " +
										"FROM AISBroadcast.win:length(250) " +
										"MATCH_RECOGNIZE(" +
										"MEASURES A AS a, B AS b, C AS c " +
										"PATTERN (A B* C) " +
										"DEFINE" +
										" C AS (C.vessel.mmsi != A.vessel.mmsi) AND" +
										" (Math.abs(C.timeStamp.getTime() - A.timeStamp.getTime()) < broadcastInterval) AND " +
										" (distance(C.position, A.position) < 200) AND" +
										" ((distance(futurePosition(1, C), futurePosition(1, A)) < 70) OR" +
										" (distance(futurePosition(2, C), futurePosition(2, A)) < 70)))";
		/* Imminent Collision: an alternative approach (requires listener modification, uses a self-join):
		String imminentCollisionEPL = 	"SELECT * " +
										"FROM AISBroadcast a1, AISBroadcast a2 " +
										"WHERE " +
											"(a1.vessel.mmsi != a2.vessel.mmsi) AND " +
											"(Math.abs(a1.timeStamp.getTime() - a2.timeStamp.getTime()) < broadcastInterval) AND " +
											"(distance(a1.position, a2.position) < 200) AND " +
											"((distance(futurePosition(1, a1), futurePosition(1, a2) < 70)) OR " +
											"(distance(futurePosition(2, a1), futurePosition(2, a2) < 70)))";*/
		EPStatement imminentCollisionStatement = esperAdministrator.createEPL(imminentCollisionEPL);
		ImminentCollisionListener imminentCollisionListener = new ImminentCollisionListener(imminentCollisionFile);
		imminentCollisionStatement.addListener(imminentCollisionListener);
		
		// Amphibious Assault
		String foreignNavyWindowEPL =	"CREATE WINDOW ForeignMilitaryBroadcasts.win:length(50) AS SimpleBroadcast";
		String foreignNavyBeepsEPL =	"INSERT INTO ForeignMilitaryBroadcasts " +
										"SELECT" +
										" timeStamp AS time," +
										" speed AS velocity," +
										" cast(vessel, Vessel) AS ship," +
										" cast(position, Coordinates) AS site " +
										"FROM AISBroadcast#length(10) " +
										"WHERE ((vessel.flag != Flag.GR) AND (vessel.type = VesselType.MILITARY))";
		String amphibiousAssaultEPL =	"SELECT time, ship, site, landDistance(site, sea) AS ld " +
										"FROM ForeignMilitaryBroadcasts " +
										"HAVING landDistance(site, sea) < 2000";
		EPStatement foreignNavyWindowStatement = esperAdministrator.createEPL(foreignNavyWindowEPL);
		EPStatement foreignNavyBeepsStatement = esperAdministrator.createEPL(foreignNavyBeepsEPL);
		EPStatement amphibiousAssaultStatement = esperAdministrator.createEPL(amphibiousAssaultEPL);
		AmphibiousAssaultListener amphibiousAssaultListener = new AmphibiousAssaultListener(amphibiousAssaultFile);
		amphibiousAssaultStatement.addListener(amphibiousAssaultListener);
		
		////////////////////////////////////////////////////////////////////////////////////////// TRIP THREAD CREATION
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
				if(AppConfiguration.getDebugStatus()) System.out.println(fileEntry.getName());
				
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
					if(AppConfiguration.getDebugStatus()) System.out.println(csvFileName);
					switch(csvFileName){
						case "parameters.csv":
							parametersFound = true;
							try{
								tripParameters = new CSVTripParameters(csv).getData(true);
								validParameters = true;
							} catch(CSVInputException e){
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
						trips.add(new Thread(new Lnav(vessel, tripParameters, route, map, esperEngine, aisLogFile, tripsReady, lock)));
						if(AppConfiguration.getDebugStatus()) System.out.println("creating Lnav.");
						lnavCount++;
					} catch(Exception e){
						e.getMessage();
						e.printStackTrace();
					}
				} else{
					if(AppConfiguration.getDebugStatus()){
						if(!parametersFound){System.out.println("Missing file \"parameters.csv\"");}
						if(!vesselFound){System.out.println("Missing file \"vessel.csv\"");}
						if(!routeFound){System.out.println("Missing file \"waypoints.csv\"");}
					}
				}
				System.out.println();
			}
		}
		
		// Spawn illicit trips
		for(File fileEntry : illegalTripDir.listFiles()){
			if(fileEntry.isDirectory()){
				if(AppConfiguration.getDebugStatus()) System.out.println(fileEntry.getName());
				
				Vessel vessel = null;
				BroadcastSequence sequence = null;
				boolean vesselFound = false;
				boolean sequenceFound = false;
				boolean validVessel = false;
				boolean validSequence = false;
				for(File csv : fileEntry.listFiles()){
					String csvFileName = csv.getName();
					if(AppConfiguration.getDebugStatus()) System.out.println(csvFileName);
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
						trips.add(new Thread(new BroadcastPlayer(vessel, sequence, map, esperEngine, aisLogFile, tripsReady, lock)));
						if(AppConfiguration.getDebugStatus()) System.out.println("creating Bandit.");
						banditCount++;
					} catch(Exception e){
						e.getMessage();
						e.printStackTrace();
					}
				} else{
					if(AppConfiguration.getDebugStatus()){
						if(!vesselFound){System.out.println("Missing file \"vessel.csv\"");}
						if(!sequenceFound){System.out.println("Missing file \"broadcasts.csv\"");}
					}
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
		for(Thread t : trips){
			try{
				t.join();
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		
		// Terminate Esper
		esperEngine.destroy();
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
	
	public static Coordinates esper_predictFuturePosition(Integer numberOfPeriods, AISBroadcast ais) // ...based on ***current*** speed and heading // used by Esper queries
	{
		int periods = numberOfPeriods.intValue();
		if(periods <= 0){
			System.out.println("Invalid argument! Setting number of periods to 1");
			periods = 1;
		}
		Coordinates currentPosition = ais.getPosition();
		Azimuth currentHeading = ais.getHeading();
		double currentSpeed = ais.getSpeed();
		
		Coordinates futurePosition = new Coordinates(currentPosition);
		double step = currentSpeed * (double)AppConfiguration.getBroadcastInterval() / 1000;
		double totalDistance = step * periods;
		futurePosition.move(currentHeading, totalDistance);
		return futurePosition;
	}
	
	public static Double esper_findTimeDifference(Date lastDate, Date firstDate)
	{
		if((lastDate != null) && (firstDate != null)){
			double millisecondsInAsecond = 1000;
			long lastDateMilliseconds = lastDate.getTime();
			long firstDateMilliseconds = firstDate.getTime();
			long timeDifferenceInMilliseconds = lastDateMilliseconds - firstDateMilliseconds;
			double timeDifferenceInSeconds = (double)timeDifferenceInMilliseconds / millisecondsInAsecond;
			return timeDifferenceInSeconds;
		}
		return null;
	}
	
	public static double esper_findDistanceFromLand(Coordinates position, Sea sea)
	{
		LinkedList<Island> islands = sea.getIslands();
		LinkedList<Double> islandDistances = new LinkedList<Double>();
		double distanceFromIsland = 0;
		double minimumDistance = 0;
		double distance = 0;
		
		for(Island island : islands){
			distanceFromIsland = island.distanceFrom(position);
			islandDistances.add(new Double(distanceFromIsland));
		}
		minimumDistance = islandDistances.peekFirst();
		for(Double dist : islandDistances){
			distance = (double)dist;
			if(distance < minimumDistance) minimumDistance = distance;
		}
		return minimumDistance;
	}
}