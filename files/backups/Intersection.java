private class Intersection
{
	private boolean intersectionStatus;
	private Coordinates intersection;
	
	private Intersection (boolean intersectionStatus, Coordinates intersection)
	{
		this.intersectionStatus = intersectionStatus;
		this.intersection = intersection;
	}
	
	public boolean getIntersectionStatus()
	{
		return intersectionStatus;
	}
	
	public Coordinates getIntersection()
	{
		return intersection;
	}
}