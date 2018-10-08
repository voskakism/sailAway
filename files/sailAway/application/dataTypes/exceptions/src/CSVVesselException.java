class CSVVesselException extends CSVInputException
{
	CSVVesselException(String message, Throwable t)
	{
		super(message, t);
	}
	
	CSVVesselException(String message)
	{
		super(message);
	}
}