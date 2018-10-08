class CSVItineraryException extends CSVInputException
{
	CSVItineraryException(String message, Throwable t)
	{
		super(message, t);
	}
	
	CSVItineraryException(String message)
	{
		super(message);
	}
}