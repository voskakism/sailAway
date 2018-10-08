class UnknownFlagException extends CSVVesselException
{
	UnknownFlagException(String message, Throwable t)
	{
		super(message, t);
	}
	
	UnknownFlagException(String message)
	{
		super(message);
	}
}