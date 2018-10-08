class Vessel extends UserInput
{
	private long mmsi;
	private Flag flag;
	private String name;
	private VesselType type;
	private double length;
	private double width;
	private double weight;
	private double maximumSpeed;
	private double minimumSpeed;
	private double acceleration;
	private double deceleration;
	
	
	Vessel(long mmsi, Flag flag, String name, VesselType type, double length, double width, double weight, double maximumSpeed, double minimumSpeed, double acceleration, double deceleration)
	{
		this.mmsi = mmsi;
		this.flag = flag;
		this.name = name;
		this.type = type;
		this.length = length;
		this.width = width;
		this.weight = weight;
		this.maximumSpeed = maximumSpeed;
		this.minimumSpeed = minimumSpeed;
		this.acceleration = acceleration;
		this.deceleration = deceleration;
	}
	
	public long getMmsi(){return mmsi;}
	public Flag getFlag(){return flag;}
	public String getName(){return name;}
	public VesselType getType(){return type;}
	public double getLength(){return length;}
	public double getWidth(){return width;}
	public double getWeight(){return weight;}
	public double getMaximumSpeed(){return maximumSpeed;}
	public double getMinimumSpeed() {return minimumSpeed;}
	public double getAcceleration(){return acceleration;}
	public double getDeceleration() {return deceleration;}
}