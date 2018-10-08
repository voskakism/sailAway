class Waypoint extends Coordinates
{
	private Turn turn;
	
	Waypoint(Coordinates point)
	{
		super(point.getLongitude(), point.getLatitude());
	}
	
	Waypoint(double longitude, double latitude)
	{
		super(longitude, latitude);
	}
	
	public void setTurn(Turn turn)
	{
		this.turn = turn;
	}
	
	public Turn getTurn()
	{
		return turn;
	}
}