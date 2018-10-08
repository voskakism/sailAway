import java.util.LinkedList;
import java.util.ListIterator;

class Itinerary extends UserInput
{
	private LinkedList<Leg> legs;
	
	public LinkedList<Leg> getLegs() {return legs;}
	
	private Itinerary(LinkedList<Leg> legs)
	{
		// Instead of sharply turning to follow the next Leg, a Vessel sails on the arc of a circle that is tangent to both Legs at exactly colliderRadius distance from their common point.
		// Collider Radius is always set as half the Lane Width, which is an app-wide setting (see configuration.csv). It's the distance from the leg junction (their common point),
		// at which the ship effectively responds (heading-wise, not nessecarily speed-wise) by begining to turn. When the juction is left behind at again the same distance,
		// the ship is expected to have completed the turn, and started to sail in a straight line on the next Leg.
		for(int i = 0; i < (legs.size() - 1); i++){ // the B Waypoint of the last Leg does not have Turn, it is the trip's destination.
			/*if(i == 0){
				Leg firstLeg = legs.get(i);
				double brakingDistance = firstLeg.getLength() - (AppConfiguration.getLaneWidth() / 2);
				firstLeg.setBrakingDistance(brakingDistance);
			}*/
			Leg currentLeg = legs.get(i);
			Leg nextLeg = legs.get(i + 1);
			
			//angle
			Azimuth turn = currentLeg.getCourse().relativeTo(nextLeg.getCourse());
			//left or right
			double turnAngle = turn.getAzimuth();
			double turnAngleUnsigned; // "sign" (left/right) being depicted by turnDirection instead
			double containedAngle;
			
			TurnDirection turnDirection;
			if(turnAngle > 180){
				turnDirection = TurnDirection.LEFT;
				turnAngleUnsigned = 360 - turnAngle;
			} else{
				turnDirection = TurnDirection.RIGHT;
				turnAngleUnsigned = turnAngle;
				
			}
			containedAngle = 180 - turnAngleUnsigned;
			
			//turn radius
			double halfContainedAngle = (containedAngle / 2);
			double colliderRadius = (AppConfiguration.getLaneWidth() / 2);
			double turnRadius = colliderRadius / Math.tan(Math.toRadians(turnAngleUnsigned / 2));
			
			//turn point
			double hypotenuse = Math.sqrt(Math.pow(colliderRadius, 2) + Math.pow(turnRadius, 2)); // Pythagorean.
			Coordinates turnpointPosition = new Coordinates(currentLeg.getPointB().getLongitude(), currentLeg.getPointB().getLatitude());
			Azimuth turnpointDirection = new Azimuth(currentLeg.getCourse().getAzimuth());
			if(turnDirection == TurnDirection.LEFT){
				turnpointDirection.rotateCounterClockwise(turnAngleUnsigned + halfContainedAngle); 
			} else{
				turnpointDirection.rotateClockwise(turnAngleUnsigned + halfContainedAngle);
			}
			turnpointPosition.move(turnpointDirection, hypotenuse);
			
			if(AppConfiguration.getDebugStatus()){
				System.out.println("turnAngle: " + turnAngle);
				System.out.println("turnAngleUnsigned: " + turnAngleUnsigned);
				System.out.println("containedAngle: " + containedAngle);
				System.out.println("halfContainedAngle: " + halfContainedAngle);
				System.out.println("Col radius: " + colliderRadius);
				System.out.println("Turn radius: " + turnRadius);
				System.out.println("hypotenuse: " + hypotenuse);
				System.out.println("turnpointDirection: " + turnpointDirection.getAzimuth());
				System.out.println("turnpointPosition" + turnpointPosition);
			}
			
			Coordinates apexFinder = new Coordinates(currentLeg.getPointA());
			apexFinder.move(currentLeg.getCourse(), (currentLeg.getLength() - (AppConfiguration.getLaneWidth() / 2)));
			
			Turn t = new Turn(turnDirection, turnpointPosition, turnRadius, turnAngleUnsigned, apexFinder);
			((Waypoint)(currentLeg.getPointB())).setTurn(t);
			
			// In case we decide to use the generic version of LinearSegment, something like this should work:
			//Waypoint w = currentLeg.getPointB(Waypoint.class);
			//Waypoint w = (Waypoint)currentLeg.getPointB(Waypoint.class);
			//w.setTurn(t);
		}
		this.legs = legs;
	}
	
	public static Itinerary createRoute(LinkedList<Waypoint> waypoints, Sea sea) throws CSVItineraryException
	{
		LinkedList<Leg> potentialLegs;
		ListIterator<Waypoint> li;
		Waypoint origin;
		Waypoint previous;
		Waypoint next;
		Leg leg;
		int remainingWaypoints = waypoints.size();
		
		if(remainingWaypoints >= 2){
			potentialLegs = new LinkedList<Leg>();
			li = waypoints.listIterator();
			previous = li.next();
			remainingWaypoints--;
			//check that the route's origin is in water
			origin = previous;
			if(!(pointIsOnWater(origin, sea))) throw new LandCollisionException("A marine route cannot begin on land.");
			while(remainingWaypoints > 0){
				next = li.next();
				leg = new Leg(previous, next);
				if(AppConfiguration.getDebugStatus()){
					System.out.println("Leg created:");
					System.out.println(leg.toString());
				}
				if(leg.getLength() <= AppConfiguration.getLaneWidth()){
					throw new LegTooShortException(leg.toString() + " has insufficient length. Currently, legs shorter than Lane Width are not supported.");
				}
				if(legIsOnWater(leg, sea)){
					potentialLegs.add(leg);
				}else{
					throw new LandCollisionException(leg.toString() + " runs aground.");
				}
				previous = next;
				remainingWaypoints--;
			}
			return new Itinerary(potentialLegs);
		}else {
			throw new LessThan2WaypointsException("Route has only " + remainingWaypoints + " waypoints, while a valid route has at least an origin and a destination.");
		}
	}
	
	private static boolean legIsOnWater(Leg leg, Sea sea)
	{
		if(sea.islandCount() <= 0) return true;
		
		// get leg's extremes
		Coordinates ne = leg.getNorthEnd();
		Coordinates se = leg.getSouthEnd();
		Coordinates we = leg.getWestEnd();
		Coordinates ee = leg.getEastEnd();
		double legMinX = we.getLongitude();
		double legMaxX = ee.getLongitude();
		double legMinY = se.getLatitude();
		double legMaxY = ne.getLatitude();
		
		// get sea's extremes
		Coordinates sne = sea.getNorthEnd();
		Coordinates sse = sea.getSouthEnd();
		Coordinates swe = sea.getWestEnd();
		Coordinates see = sea.getEastEnd();
		double seaMinX = swe.getLongitude();
		double seaMaxX = see.getLongitude();
		double seaMinY = sse.getLatitude();
		double seaMaxY = sne.getLatitude();
		
		if(!((legMaxX < seaMinX) || (legMinX > seaMaxX) || (legMaxY < seaMinY) || (legMinY > seaMaxY))){
			LinkedList<Island> islands = sea.getIslands();
			for(Island island : islands){
				// get island's extemes
				Coordinates cn = island.getCapeNorth();
				Coordinates cs = island.getCapeSouth();
				Coordinates cw = island.getCapeWest();
				Coordinates ce = island.getCapeEast();
				double isleMinX = cw.getLongitude();
				double isleMaxX = ce.getLongitude();
				double isleMinY = cs.getLatitude();
				double isleMaxY = cn.getLatitude();
	
				if(!((legMaxX < isleMinX) || (legMinX > isleMaxX) || (legMaxY < isleMinY) || (legMinY > isleMaxY))){
					LinkedList<Shore> shores = island.getShores();
					for(Shore shore : shores){
						if(leg.getIntersectionWith(shore).intersecting()) return false;
					}
				}
			}
		}
		return true;
	}
	
	private static boolean pointIsOnWater(Coordinates point, Sea sea)
	{
		if(sea.islandCount() <= 0) return true;
		
		// get sea's extremes
		Coordinates sne = sea.getNorthEnd();
		Coordinates sse = sea.getSouthEnd();
		Coordinates swe = sea.getWestEnd();
		Coordinates see = sea.getEastEnd();
		double seaMinX = swe.getLongitude();
		double seaMaxX = see.getLongitude();
		double seaMinY = sse.getLatitude();
		double seaMaxY = sne.getLatitude();
		
		double x = point.getLongitude();
		double y = point.getLatitude();
		
		if((x < seaMinX) || (x > seaMaxX) || (y < seaMinY) || (y > seaMaxY)){
			return true;
		} else{
			LinkedList<Island> islands = sea.getIslands();
			for(Island island : islands){
				// get island's extemes
				Coordinates cn = island.getCapeNorth();
				Coordinates cs = island.getCapeSouth();
				Coordinates cw = island.getCapeWest();
				Coordinates ce = island.getCapeEast();
				double isleMinX = cw.getLongitude();
				double isleMaxX = ce.getLongitude();
				double isleMinY = cs.getLatitude();
				double isleMaxY = cn.getLatitude();
				
				if((x < isleMinX) || (x > isleMaxX) || (y < isleMinY) || (y > isleMaxY)) continue;
				if(island.contains(point)) return false;
			}
		}
		return true;
	}
}