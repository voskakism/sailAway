import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Random;
import com.espertech.esper.client.EPServiceProvider;
import java.util.concurrent.atomic.AtomicInteger;

class Lnav implements Runnable
{
	//private boolean underWay = true;
	private final int BROADCAST_INTERVAL = AppConfiguration.getBroadcastInterval(); // milliseconds
	private final int LANE_WIDTH = AppConfiguration.getLaneWidth();
	private final int COLLIDER_RADIUS = (LANE_WIDTH / 2);
	private final int DESTINATION_RADIUS = 50;
	private final double SECONDS = (double)BROADCAST_INTERVAL / 1000;
	private final double TURBULENCE = AppConfiguration.getTurbulence();
	private final boolean DEBUG = AppConfiguration.getDebugStatus();
	
	private final double CHECK_SPEED_DISTANCE =  550; //17500 / COLLIDER_RADIUS; // until we come up with a better solution for slowing down before a turn...
	
	private Vessel vessel;
	private TripParameters parameters;
	private Itinerary route;
	private Terrain map;
	private EPServiceProvider engine;
	private File aisLogFile;
	private AtomicInteger tripsReady;
	private Object lock;
	
	//Trip Info
	private Azimuth heading;
	private double speed;
	private double maxSpeed;
	private double minSpeed;
	private double laneWidth;
	
	private Random random;
	
	Lnav(Vessel vessel, TripParameters parameters, Itinerary route, Terrain map, EPServiceProvider engine, File aisLogFile, AtomicInteger tripsReady, Object lock)
	{
		this.vessel = vessel;
		this.parameters = parameters;
		this.route = route;
		this.map = map;
		this.engine = engine;
		this.aisLogFile = aisLogFile;
		this.tripsReady = tripsReady;
		this.lock = lock;
		
		random = new Random(System.currentTimeMillis());
		
		// total mass calculation
		double weight = vessel.getWeight();
		double ballast = parameters.getBallast();
		double fuel = parameters.getFuel();
		double payload = parameters.getPayload();
		double mass = weight + ballast + fuel + payload; // assuming constant gravity acceleration (let g = 10 m/s^2) at sea level (at the Aegean and devoid of lunar tide), same weights correspond to equal masses.
		
		// set speed caps for turns.
		LinkedList<Leg> legs = route.getLegs();
		ListIterator<Leg> li = legs.listIterator();
		// 1st pass: calculate speed limits of each turn 'myopically' based the turn alone.
		// The ships follow uniform circular motion when taking a turn. Consequently, angular acceleration is zero, angular velocity, speed (linear, tangential), circle's center (a.k.a. "turnpoint") and turn radius are all constant for each turn.
		// In such motion, the formula for centripetal acceleration is given by: ac = v^2 / r , where: ac = centripetal acceleration, v = speed, r = turn radius. Multiplying the equation with mass,
		// Fc  = v^2 * m / r , where Fc = centripetal force, m = mass (Newton's 2nd Law of Motion). We solve for speed. Fc is an app-wide extra-systemic constant.
		// The speed limit refers to the (maximum for the turn) linear speed of the ship and it is as a scalar, not a vector.
		double centripetalForce = AppConfiguration.getCentripetalForce(); /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		do{
			Leg currentLeg = li.next();
			Waypoint wb = (Waypoint)currentLeg.getPointB();
			Turn t = wb.getTurn();
			if(t == null) continue;
			double turnRadius = t.getTurnRadius();
			double cap = Math.sqrt(centripetalForce * turnRadius / mass);
			t.setSpeedCap(cap);
		} while(li.hasNext());
		/*System.out.println("1st pass caps:");
		for(int i = 0; i < route.getLegs().size() - 1; i++){
			Waypoint bb = (Waypoint)route.getLegs().get(i).getPointB();
			System.out.println(bb.getTurn().getSpeedCap());
		}*/
		
		// 2nd pass: where needed, update the speed limits of turns so that they take into consideration the speed limits of other turns ahead that are very close, as in an "S" pair of turns.
		// Starting from the last turn (that is the B point of the next-to-last Leg; Note that the process of slowing down to an almost stop at destination is quite different),
		// we get the speed limit calculated earlier and perform a "reverse counting" procedure, where moving backwards on the leg, we re-create the ship's steps on it as it could possibly decelerate before the turn.
		// In each iteration (backstep), we calculate the covered distance and speed, in a reverse fashion (that is, increasing the speed as we move in reverse, in the same rate that the forward-moving
		// ship would reduce it). The "counting" stops when we reach the begining of the leg at collider radius distance (since that is the closest to the begining of the Leg where speed can change),
		// OR if the speed "count" reaches the vessels max speed. In any case, the speed found, is compared to the speed limit of point B of the previous Leg (the previous Turn) if there is one.
		// If the previous turn's limit is higher than the speed "count", a.k.a. "carryOver", it is replaced by it. Obviously, the count for the next Leg starts from the (possibly) updated speed limit.
		// It is also apparent that if carryOver reaches max speed, it cannot affect the speed limit of the previous turn.
		// In a cascading manner, the "carried over" speed limit updates the speed caps (if needed), all the way to the beginning of the trip.
		/*double carryOverSpd = -1;
		int round = 0;
		double decel = vessel.getDeceleration();
		double maxSpd = vessel.getMaximumSpeed();
		Iterator<Leg> di = legs.descendingIterator(); // REVERSE iterator: The first element to be gotten here, is the route's last leg
		di.next(); // skip last leg, it has no Turn, just the destination of the trip
		while(di.hasNext()){
			round++;
			Leg currentLeg = di.next();
			Waypoint wb = (Waypoint)currentLeg.getPointB();
			Turn t = wb.getTurn();
			if(t == null) break; // unlikely. all Legs but the last have a Turn within their B Waypoint
			double legLength = currentLeg.getLength();
			double straightlength = legLength - (2 * COLLIDER_RADIUS); // the part of the leg where the ship sails on a straight line and speed can change
			double spd = t.getSpeedCap();
			if((spd > carryOverSpd) && (round > 1)){ // skip last turn (round == 1), as there is no carryOverSpd to compare it against.
				spd = carryOverSpd;
				t.setSpeedCap(spd);
			}
			double dist = 0;
			do{
				dist += spd * SECONDS;
				spd += decel * SECONDS;
				if(spd >= maxSpd){
					spd = maxSpd;
					currentLeg.setBrakingDistance(dist);
					break;
				}
			} while(dist + (spd * SECONDS) <= straightlength); // nextStep = speed * SECONDS;
			spd -= decel * SECONDS; // the previous speed value is the one sought after, since starting the deceleration from the value last assigned in the loop is, marginally, too fast to meet the speed cap.It is the speed value nextStep is calculated from
			carryOverSpd = spd;
		} */ //alternatively, the calculations of the first pass can be incorporated in the second loop, with rather minor modifications
		/*System.out.println("2nd pass caps:");
		for(int i = 0; i < route.getLegs().size() - 1; i++){
			Waypoint bb = (Waypoint)route.getLegs().get(i).getPointB();
			System.out.println(bb.getTurn().getSpeedCap());
		}*/
		// calculate maximum entry speed
		//Leg firstLeg = legs.peek();
		//double firstLegLength = firstLeg.getLength();
		//double startToFirstApex = firstLegLength - COLLIDER_RADIUS;
		//Waypoint firstWB = (Waypoint)firstLeg.getPointB();
		//Turn firstTurn = firstWB.getTurn(); ////////////////////////////// if null ???
		//double firstCap = firstTurn.getSpeedCap();
		//double speed = firstCap;
		//double distance = 0;
		//do{
		//	distance += speed * SECONDS;
		//	speed += decel * SECONDS;
		//	/*if(speed >= maxSpd){
		//		speed = maxSpd;
		//		// 
		//		//currentLeg.setCheckSpeedDistance(dist);
		//		break;
		//	}*/
		//} while(distance + (speed * SECONDS) <= startToFirstApex); // nextStep = speed * SECONDS;
		//speed -= decel * SECONDS;
		//if(the above condition became true after just one iteration, then we need to bring down the step, by adjusting the B.I.
		/*if(initialSpeed >= maxSpeed){
				// WARNING !!
				initialSpeed = maxSpeed;
		}*/
		/*if(initialSpeed >= speed){
			// WARNING !!
			initialSpeed = speed;
			if(initialSpeed >= maxSpeed)
		}*/
	}
	
	public void run()
	{
		double speed = parameters.getInitialSpeed();
		LinkedList<Leg> legs = route.getLegs();
		ListIterator<Leg> li = legs.listIterator();
		Leg firstLeg = legs.peek();
		Leg currentLeg;
		
		double vmax = vessel.getMaximumSpeed();
		double vmin = vessel.getMinimumSpeed();
		double acc = vessel.getAcceleration();
		Coordinates origin = firstLeg.getPointA();
		
		Coordinates position;
		Azimuth bearing;
		Azimuth heading;
		double distanceToWaypoint;
		double distanceToApex;
		
		boolean firstStepOfArc = false;
		Waypoint wb;
		
		// turn information. Initializations are arbitrary, to silence the compiler's "...might not have been initialized"
		Turn t;
		TurnDirection turnDirection = TurnDirection.RIGHT;
		Coordinates turnPoint = new Coordinates();
		double turnRadius = 0;
		double turnAngle;
		Coordinates turnEntryApex = new Coordinates();
		double turnSpeedCap = 0;
		double turnCirclePerimeter = 0;
		double angleStep = 0;
		Azimuth centralAngleAbsoluteAzm = new Azimuth();
		Coordinates positionFinder;
		double firstArcStep;
		double firstAngleStep;
		double lastAngleStep;
		double turnAngleRemainder = 0;
		double step = 0;
		double firstStraightComponent;
		double firstCurvedComponent;
		double lastStraightComponent;
		double lastCurvedComponent;
		boolean lastLeg = false;
		double offCourse;
		double correctiveTurnAngle;
		long calculationStart = 0;
		long calculationEnd = 0;
		int timeTaken = 0;
		int legInitialDelay = 0;
		Azimuth course;
		position = new Coordinates(origin.getLongitude(), origin.getLatitude());
		heading = new Azimuth(firstLeg.getCourse().getAzimuth());
		
		// synchronization point
		synchronized(lock){
			tripsReady.incrementAndGet();
			try{
				lock.wait(25000);
			} catch(InterruptedException ie){
				ie.printStackTrace();
			}
		}
		
		do{
			// per leg initial calculations
			calculationStart = System.currentTimeMillis();
			currentLeg = li.next();
			course = currentLeg.getCourse();
			wb = (Waypoint)currentLeg.getPointB();
			t = wb.getTurn();
			if(t == null){ // on last Leg
				lastLeg = true;
			} else{ // get turn details
				turnDirection = t.getTurnDirection();
				turnPoint = t.getTurnPoint();
				turnRadius = t.getTurnRadius();
				turnAngle = t.getTurnAngle();
				turnEntryApex = t.getTurnEntryApex();
				turnSpeedCap = t.getSpeedCap();
				// calculate further turn information
				turnCirclePerimeter = 2 * Math.PI * turnRadius;
				centralAngleAbsoluteAzm = Coordinates.calculateBearing(turnPoint, turnEntryApex);
				/*
				System.out.println("wb.toString()" + wb.toString());///////////////////////////////////////////////////////////////////////////////////////////
				System.out.println("currentLeg.getCourse()" + currentLeg.getCourse().getAzimuth());////////////////////////////////////////////////////////////
				System.out.println("turnEntryApex: " + turnEntryApex);/////////////////////////////////////////////////////////////////////////////////////////
				System.out.println("centralAngleAbsoluteAzm: " + centralAngleAbsoluteAzm.getAzimuth());////////////////////////////////////////////////////////
				System.out.println("turnCirclePerimeter: " + turnCirclePerimeter);/////////////////////////////////////////////////////////////////////////////
				*/
				turnAngleRemainder = turnAngle;
				firstStepOfArc = true;
			}
			calculationEnd = System.currentTimeMillis();
			legInitialDelay = (int)(calculationEnd - calculationStart);
			if(AppConfiguration.getDebugStatus()) System.out.println("Leg initial Delay. timeTaken: " + legInitialDelay + " milliseconds");
			
			// sail straight on Leg
			// we need these calculated here as well, otherwise we won't be able to compare them against the CHECK_SPEED_DISTANCE in the first iteration
			distanceToWaypoint = Coordinates.calculateDistance(position, wb);
			distanceToApex = distanceToWaypoint - COLLIDER_RADIUS;
			do{
				calculationStart = System.currentTimeMillis();
				if(!lastLeg){
					if(distanceToApex <= CHECK_SPEED_DISTANCE){
						if(speed > turnSpeedCap){
							speed = speed * (distanceToApex / CHECK_SPEED_DISTANCE);
							if(speed < turnSpeedCap) speed = turnSpeedCap;
						} else{
							//speed += ((vmax - speed) / vmax * acc) * SECONDS; // speed up
						}
					} else{
						speed += ((vmax - speed) / vmax * acc) * SECONDS; // speed up
					}
				} else{
					speed += ((vmax - speed) / vmax * acc) * SECONDS; // speed up
				}
				step = speed * SECONDS;
				// waveHit and Corrective measures
				position = waveHit(position);
				bearing = Coordinates.calculateBearing(position, wb);
				offCourse = currentLeg.distanceFromCarrierLineOfThisSegment(position);
				if(offCourse > (LANE_WIDTH / 2)){ // take more aggressive corrective measures, aiming to get back on the leg
					if(AppConfiguration.getDebugStatus()) System.out.println("AGGRESSIVE CORRECTION MODE: " + offCourse + " meters off course");
					
					// corrective turn
					// 1: angle
					if(step > offCourse){
						correctiveTurnAngle = Math.toDegrees(Math.asin(offCourse/step));
						if(AppConfiguration.getDebugStatus()) System.out.println("  CORRECTIVE ANGLE  --->  " + correctiveTurnAngle);
					} else{
						correctiveTurnAngle = 90;
					}
					// 2: direction
					Azimuth divergence = bearing.relativeTo(course);
					double div = divergence.getAzimuth();
					if(div <= 90){
						heading.setAzimuth(course.getAzimuth()); // will fail if position overshoots the leg
						heading.rotateCounterClockwise(correctiveTurnAngle);
					} else{
						heading.setAzimuth(course.getAzimuth());
						heading.rotateClockwise(correctiveTurnAngle);
					}
				} else{ // or simply aim for the next waypoint
					heading.turnTo(bearing, 0.95);
				}
				broadcastAIS(position, heading, speed);
				position.move(heading, step);
				distanceToWaypoint = Coordinates.calculateDistance(position, wb);
				distanceToApex = distanceToWaypoint - COLLIDER_RADIUS;
				calculationEnd = System.currentTimeMillis();
				timeTaken = (int)(calculationEnd - calculationStart);
				if(AppConfiguration.getDebugStatus()){
					System.out.println("distanceToWaypoint = " + distanceToWaypoint);
					System.out.println("distanceToApex = " + distanceToApex);
					System.out.println("timeTaken: " + timeTaken + " milliseconds");
				}
				if((timeTaken + legInitialDelay) < BROADCAST_INTERVAL){ // sleep time cannot be negative
					sleep(BROADCAST_INTERVAL - timeTaken - legInitialDelay);
				}
				legInitialDelay = 0;
			} while(distanceToApex >= 0);
			
			// take the turn or slow down to destination
			if(lastLeg){ // on last Leg
				do{ // don't turn, just slow down to almost a stop (minimum vessel speed), very close to destination ("DESTINATION_RADIUS")
					calculationStart = System.currentTimeMillis();
					broadcastAIS(position, heading, speed);
					distanceToWaypoint = Coordinates.calculateDistance(position, wb);
					if(AppConfiguration.getDebugStatus()) System.out.println("distanceToWaypoint = " + distanceToWaypoint);
					speed *= (distanceToWaypoint / COLLIDER_RADIUS);
					if(speed < vmin) speed = vmin;
					if(speed > vmax) speed = vmax;
					bearing = Coordinates.calculateBearing(position, wb);
					heading.turnTo(bearing, 0.95);
					step = speed * SECONDS;
					position.move(heading, step);
					calculationEnd = System.currentTimeMillis();
					timeTaken = (int)(calculationEnd - calculationStart);
					if(AppConfiguration.getDebugStatus()) System.out.println("timeTaken: " + timeTaken + " milliseconds");
					if(timeTaken < BROADCAST_INTERVAL){ // sleep time cannot be negative
						sleep(BROADCAST_INTERVAL - timeTaken);
					}
				} while(distanceToWaypoint > DESTINATION_RADIUS);
			} else{
				if(AppConfiguration.getDebugStatus()) System.out.println("In turn !!");
				angleStep = (step / turnCirclePerimeter) * 360;
				// first step: partially straight on current Leg, partially curved on turn
				if(firstStepOfArc){
					firstStepOfArc = false;
					firstCurvedComponent = COLLIDER_RADIUS - distanceToWaypoint; // step == firstStraightComponent + firstCurvedComponent
					firstStraightComponent = step - firstCurvedComponent;
					firstAngleStep = (firstCurvedComponent / turnCirclePerimeter) * 360;
					if(firstAngleStep < turnAngleRemainder){
						calculationStart = System.currentTimeMillis();
						if(AppConfiguration.getDebugStatus()) System.out.println("firstAngleStep: " + firstAngleStep);
						turnAngleRemainder -= firstAngleStep;
						if(turnDirection == TurnDirection.LEFT){
							centralAngleAbsoluteAzm.rotateCounterClockwise(firstAngleStep);
							heading.rotateCounterClockwise(firstAngleStep);
						} else{
							centralAngleAbsoluteAzm.rotateClockwise(firstAngleStep);
							heading.rotateClockwise(firstAngleStep);
						}
						positionFinder = new Coordinates(turnPoint);
						positionFinder.move(centralAngleAbsoluteAzm, turnRadius);
						position = positionFinder;
						position = waveHit(position);
						broadcastAIS(position, heading, speed);
						calculationEnd = System.currentTimeMillis();
						timeTaken = (int)(calculationEnd - calculationStart);
						if(AppConfiguration.getDebugStatus()) System.out.println("timeTaken: " + timeTaken + " milliseconds");
						if(timeTaken < BROADCAST_INTERVAL){ // sleep time cannot be negative
							sleep(BROADCAST_INTERVAL - timeTaken);
						}
					}
				}
				// subsequent steps: completely curved (fully situated on turn's curve)
				while(turnAngleRemainder > angleStep){
					calculationStart = System.currentTimeMillis();
					if(AppConfiguration.getDebugStatus()) System.out.println("angleStep: " + angleStep);
					turnAngleRemainder -= angleStep;
					if(turnDirection == TurnDirection.LEFT){
						centralAngleAbsoluteAzm.rotateCounterClockwise(angleStep);
						heading.rotateCounterClockwise(angleStep);
					} else{
						centralAngleAbsoluteAzm.rotateClockwise(angleStep);
						heading.rotateClockwise(angleStep);
					}
					positionFinder = new Coordinates(turnPoint);
					positionFinder.move(centralAngleAbsoluteAzm, turnRadius);
					position = positionFinder;
					position = waveHit(position);
					broadcastAIS(position, heading, speed);
					calculationEnd = System.currentTimeMillis();
					timeTaken = (int)(calculationEnd - calculationStart);
					if(AppConfiguration.getDebugStatus()) System.out.println("timeTaken: " + timeTaken + " milliseconds");
					if(timeTaken < BROADCAST_INTERVAL){ // sleep time cannot be negative
						sleep(BROADCAST_INTERVAL - timeTaken);
					}
				}
				// last step: partially curved on turn, partially straight on next Leg
				calculationStart = System.currentTimeMillis();
				if(AppConfiguration.getDebugStatus()){
					System.out.println("angleStep" + angleStep);
					System.out.println("turnAngleRemainder: " + turnAngleRemainder);
				}
				lastAngleStep = turnAngleRemainder;
				lastCurvedComponent = (lastAngleStep / 360) * turnCirclePerimeter; // calculate arc length
				lastStraightComponent = step - lastCurvedComponent;
				if(turnDirection == TurnDirection.LEFT){
					centralAngleAbsoluteAzm.rotateCounterClockwise(lastAngleStep);
					heading.rotateCounterClockwise(lastAngleStep);
				} else{
					centralAngleAbsoluteAzm.rotateClockwise(lastAngleStep);
					heading.rotateClockwise(lastAngleStep);
				}
				positionFinder = new Coordinates(turnPoint);
				positionFinder.move(centralAngleAbsoluteAzm, turnRadius);
				position = positionFinder;
				position.move(heading, lastStraightComponent); // move straight for the last straight fragment to complete the step and then broadcast + sleep
				position = waveHit(position);
				broadcastAIS(position, heading, speed);
				calculationEnd = System.currentTimeMillis();
				timeTaken = (int)(calculationEnd - calculationStart);
				if(AppConfiguration.getDebugStatus()){
					System.out.println("Out of turn, already sailing on the next Leg !!");
					System.out.println("timeTaken: " + timeTaken + " milliseconds");
				}
				if(timeTaken < BROADCAST_INTERVAL){ // sleep time cannot be negative
					sleep(BROADCAST_INTERVAL - timeTaken);
				}
			}
		} while(li.hasNext());
		if(AppConfiguration.getDebugStatus()) System.out.println("Reached destination !");
	}
	
	private void sleep(int milliseconds)
	{
		try{
			Thread.sleep(milliseconds);
		} catch(InterruptedException ie){
			ie.printStackTrace();
		}
	}
	
	private void broadcastAIS(Coordinates position, Azimuth heading, double speed)
	{
		// Call copy constructor of Coordinates instead of using the 'position' arg as-is, to make a new AISBroadcast.
		// Necessary step, if previous broadcast positions are to be retrieved (for instance, see insertToDeltaWinEPL).
		Coordinates currentPosition = new Coordinates(position);
		AISBroadcast ais = new AISBroadcast(System.currentTimeMillis(), this.vessel, currentPosition, heading, speed);
		engine.getEPRuntime().sendEvent(ais);
		if(map != null) map.beep(ais);
		ais.toConsole();
		ais.toFile(aisLogFile);
	}
	
	private Coordinates waveHit(Coordinates position) // both the incidence of a wavehit and its potential severity are determined by the turbulence setting
	{
		if(TURBULENCE > 0){
			double chance = random.nextDouble();
			if(TURBULENCE > chance){
				Azimuth windDirection = new Azimuth(random.nextInt(360));
				double windSpeed = random.nextDouble() * 15 * SECONDS * TURBULENCE;
				if(AppConfiguration.getDebugStatus()){
					System.out.println("windDirection: " + windDirection.getAzimuth());
					System.out.println("windSpeed: " + windSpeed);
				}
				position.move(windDirection, windSpeed);
			}
		}
		return position;
	}
}