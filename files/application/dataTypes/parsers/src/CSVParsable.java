interface CSVParsable
{
	UserInput getData(boolean omitFirstLine) throws CSVInputException;
}