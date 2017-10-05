import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class CSVTripParameters implements CSVParsable
{
	private File inputFile;
	
	CSVTripParameters(File inputFile)
	{
		this.inputFile = inputFile;
	}
	
	public TripParameters getData(boolean omitFirstLine) throws CSVInputException
	{
		String line;
		String[] parts;
		double initialSpeed;
		double ballast;
		double fuel;
		double payload;
	
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))){
			try{
				if(omitFirstLine){
					bufferedReader.readLine(); // throws IOException
				}
				line = bufferedReader.readLine(); // throws IOException
				line = line.replaceAll("\\s+", ""); // eat whitespace. This throws PatternSyntaxException, impossible here
				parts = line.split(","); // throws PatternSyntaxException, impossible here
				initialSpeed = Double.parseDouble(parts[0]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(initialSpeed < 0) throw new NegativeNumberException("Speed cannot be negative.");
				ballast = Double.parseDouble(parts[1]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(ballast < 0) throw new NegativeNumberException("Ballast weight cannot be negative.");
				fuel = Double.parseDouble(parts[2]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(fuel < 0) throw new NegativeNumberException("Fuel weight cannot be negative.");
				payload = Double.parseDouble(parts[3]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(payload < 0) throw new NegativeNumberException("Payload weight cannot be negative.");
			} catch(NegativeNumberException nne){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Invalid value.", nne);
			} catch(ArrayIndexOutOfBoundsException bex){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Fields could be missing from the file, be situated in a wrong line, or perhaps a wrong delimiter is used.", bex);
			} catch(NullPointerException | IllegalArgumentException rte){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Bad input.", rte);
			}
			return new TripParameters(initialSpeed, ballast, fuel, payload);
		} catch(IOException ioex){
			throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": I/O error.", ioex);
		}
	}
	
	private String trimFilePath(String filePath)
	{
		String pattern = "^((\\.\\.\\\\)|(\\.\\.\\/))+";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(filePath);
		return m.replaceFirst("");
	}
}