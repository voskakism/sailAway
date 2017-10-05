class Broadcast
{
	protected Coordinates position;
	protected Azimuth heading;
	protected double speed;
	
	Broadcast(Coordinates position, Azimuth heading, double speed)
	{
		this.position = position;
		this.heading = heading;
		this.speed = speed;
	}
	
	public Coordinates getPosition() {return position;}
	public Azimuth getHeading() {return heading;}
	public double getSpeed() {return speed;}
}