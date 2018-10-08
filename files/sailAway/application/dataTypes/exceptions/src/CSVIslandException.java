class CSVIslandException extends CSVInputException
{
	CSVIslandException(String message, Throwable t)
	{
		super(message, t);
	}
	
	CSVIslandException(String message)
	{
		super(message);
	}
}