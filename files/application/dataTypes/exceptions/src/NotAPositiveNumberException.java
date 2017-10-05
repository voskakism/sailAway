class NotAPositiveNumberException extends CSVInputException
{
	NotAPositiveNumberException(String message, Throwable t)
	{
		super(message, t);
	}
	
	NotAPositiveNumberException(String message)
	{
		super(message);
	}
}