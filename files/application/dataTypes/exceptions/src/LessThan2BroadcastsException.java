class LessThan2BroadcastsException extends CSVBroadcastException
{
	LessThan2BroadcastsException(String message, Throwable t)
	{
		super(message, t);
	}
	
	LessThan2BroadcastsException(String message)
	{
		super(message);
	}
}