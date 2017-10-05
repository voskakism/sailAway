class IslandOverlapException extends CSVIslandException
{
	IslandOverlapException(String message, Throwable t)
	{
		super(message, t);
	}
	
	IslandOverlapException(String message)
	{
		super(message);
	}
}