class Turn
{
	private TurnDirection turnDirection;
	private Coordinates turnPoint;
	private double turnRadius;
	private double turnAngle;
	private Coordinates turnEntryApex;
	private double speedCap;
	
	public Turn(TurnDirection turnDirection, Coordinates turnPoint, double turnRadius, double turnAngle, Coordinates turnEntryApex)
	{
		this.turnDirection = turnDirection;
		this.turnPoint = turnPoint;
		this.turnRadius = turnRadius;
		this.turnAngle = turnAngle;
		this.turnEntryApex = turnEntryApex;
		speedCap = -1;
	}
	
	public Turn(TurnDirection turnDirection, Coordinates turnPoint, double turnRadius, double turnAngle, Coordinates turnEntryApex, double speedCap)
	{
		this.turnDirection = turnDirection;
		this.turnPoint = turnPoint;
		this.turnRadius = turnRadius;
		this.turnAngle = turnAngle;
		this.turnEntryApex = turnEntryApex;
		this.speedCap = speedCap;
	}
	
	public void setSpeedCap(double cap) {speedCap = cap;}
	
	public TurnDirection getTurnDirection() {return turnDirection;}
	public Coordinates getTurnPoint() {return turnPoint;}
	public double getTurnRadius() {return turnRadius;}
	public double getTurnAngle() {return turnAngle;}
	public Coordinates getTurnEntryApex() {return turnEntryApex;}
	public double getSpeedCap() {return speedCap;}
}