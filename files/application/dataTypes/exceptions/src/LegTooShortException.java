class LegTooShortException extends CSVItineraryException
{
	LegTooShortException(String message, Throwable t)
	{
		super(message, t);
	}
	
	LegTooShortException(String message)
	{
		super(message);
	}
}