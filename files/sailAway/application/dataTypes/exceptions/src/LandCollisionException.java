class LandCollisionException extends CSVItineraryException
{
	LandCollisionException(String message, Throwable t)
	{
		super(message, t);
	}
	
	LandCollisionException(String message)
	{
		super(message);
	}
}