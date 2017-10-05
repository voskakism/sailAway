class Leg extends LinearSegment
{
	private Azimuth course;
	private double length;
	private double brakingDistance;
	
	public Azimuth getCourse() {return course;}
	public double getLength() {return length;}
	public double getBrakingDistance() {return brakingDistance;}
	
	Leg(Waypoint origin, Waypoint destination)
	{
		super(origin, destination);
		this.course = Coordinates.calculateBearing(origin, destination);
		this.length = Coordinates.calculateDistance(origin, destination);
		brakingDistance = length - AppConfiguration.getLaneWidth();
	}
	
	public void setBrakingDistance(double distance) {brakingDistance = distance;}
	
	@Override
	public String toString()
	{
		return ("Leg with endpoints: A: " + getPointA().toString() + " and B: " + getPointB().toString());
	}
}