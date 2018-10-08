class CSVBroadcastException extends CSVInputException
{
	CSVBroadcastException(String message, Throwable t)
	{
		super(message, t);
	}
	
	CSVBroadcastException(String message)
	{
		super(message);
	}
}