class UnknownVesselTypeException extends CSVVesselException
{
	UnknownVesselTypeException(String message, Throwable t)
	{
		super(message, t);
	}
	
	UnknownVesselTypeException(String message)
	{
		super(message);
	}
}