class LessThan2WaypointsException extends CSVItineraryException
{
	LessThan2WaypointsException(String message, Throwable t)
	{
		super(message, t);
	}
	
	LessThan2WaypointsException(String message)
	{
		super(message);
	}
}