class TripParameters extends UserInput
{
	private double initialSpeed;
	private double ballast;
	private double fuel;
	private double payload;
	
	TripParameters(double initialSpeed, double ballast, double fuel, double payload)
	{
		this.initialSpeed = initialSpeed;
		this.ballast = ballast;
		this.fuel = fuel;
		this.payload = payload;
	}
	
	public double getInitialSpeed(){return initialSpeed;}
	public double getBallast(){return ballast;}
	public double getFuel(){return fuel;}
	public double getPayload(){return payload;}
}