class CSVInputException extends Exception
{
	CSVInputException(String message, Throwable t)
	{
		super(message, t);
	}
	
	CSVInputException(String message)
	{
		super(message);
	}
}