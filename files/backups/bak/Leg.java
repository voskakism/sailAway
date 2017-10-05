/*class Leg extends LinearSegment
{
	private Waypoint origin;
	private Waypoint destination;
	
	Leg(Waypoint origin, Waypoint destination)
	{
		this.origin = origin;
		this.destination = destination;
	}
}*/

/*class Leg // extends LinearSegment
{
	private Waypoint origin;
	private Waypoint destination;
	private Azimuth course;
	private double length;
	//private double maximumBrakingDistance;///////////////////////////////////////////////////////////////////
	//braking distance should be the maximum counted distance needed to decelerate from maxSpeed to the leg's destination's cap
	
	Leg(Waypoint origin, Waypoint destination)
	{
		this.origin = origin;
		this.destination = destination;
		length = Coordinates.calculateDistance(origin, destination);
		course = Coordinates.calculateBearing(origin, destination);
		//maximumBrakingDistance = 0; ///////////////derived
	}
	
	public Waypoint getOrigin(){return origin;}
	public Waypoint getDestination(){return destination;}
	public Azimuth getCourse(){return course;}
	public double getLength(){return length;}	
	//public double getMaximumBrakingDistance(){return maximumBrakingDistance;}//////////////////////////////////////
	
	public double getMaximumEntrySpeed(double decelerationCoefficient, double broadcastIntervalMillis)
	{
		double c = decelerationCoefficient;
		double cap = this.getDestination().getSpeedCap();
		double speed = cap;
		double distance = 0;
		double step;
		while(distance < length){
			speed = (speed - cap * c) / (1 - c);
			step = speed * broadcastIntervalMillis / 1000;
			distance += step;
		}
		return speed;
	}
}*/
	
class Leg extends LinearSegment
{
	private Azimuth course;
	private double length;
	
	public Azimuth getCourse() {return course;}
	public double getLength() {return length;}
	
	Leg(Waypoint origin, Waypoint destination)
	{
		super(origin, destination);
		this.course = Coordinates.calculateBearing(origin, destination);
		this.length = 0; ///////////////////////////////////////////////////////////////////////////////////
	}
	
	@Override
	public String toString()
	{
		return ("Leg with endpoints: A: " + getPointA().toString() + " and B: " + getPointB().toString());
	}
}