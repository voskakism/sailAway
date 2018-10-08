class VesselAlreadyExistsException extends CSVVesselException
{
	VesselAlreadyExistsException(String message, Throwable t)
	{
		super(message, t);
	}
	
	VesselAlreadyExistsException(String message)
	{
		super(message);
	}
}