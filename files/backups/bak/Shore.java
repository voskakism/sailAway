class Shore extends LinearSegment
{
	Shore(Coordinates a, Coordinates b)
	{
		super(a, b);
	}
	
	@Override
	public String toString()
	{
		return ("Shore with endpoints: A: " + getPointA().toString() + " and B: " + getPointB().toString());
	}
}