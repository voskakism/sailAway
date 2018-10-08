class NegativeNumberException extends CSVInputException
{
	NegativeNumberException(String message, Throwable t)
	{
		super(message, t);
	}
	
	NegativeNumberException(String message)
	{
		super(message);
	}
}