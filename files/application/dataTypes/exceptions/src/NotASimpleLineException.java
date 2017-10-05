class NotASimpleLineException extends CSVIslandException
{
	NotASimpleLineException(String message, Throwable t)
	{
		super(message, t);
	}
	
	NotASimpleLineException(String message)
	{
		super(message);
	}
}