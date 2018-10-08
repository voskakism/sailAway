import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class CSVVessel implements CSVParsable
{
	private File inputFile;
	
	CSVVessel(File inputFile)
	{
		this.inputFile = inputFile;
	}
	
	public Vessel getData(boolean omitFirstLine) throws CSVInputException
	{
		String line;
		String[] parts;
		long mmsi;
		Flag flag;
		String name;
		VesselType type;
		double length;
		double width;
		double weight;
		double maximumSpeed;
		double minimumSpeed;
		double acceleration;
		double deceleration;
	
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))){
			try{
				if(omitFirstLine){
					bufferedReader.readLine(); // throws IOException
				}
				line = bufferedReader.readLine(); // throws IOException
				line = line.replaceAll("\\s+", ""); // eat whitespace. This throws PatternSyntaxException, impossible here
				parts = line.split(","); // throws PatternSyntaxException, impossible here
				mmsi = Long.parseLong(parts[0]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(mmsi < 0) throw new NegativeNumberException("MMSI should be a non negative number.");
				try{
					flag = Flag.valueOf(parts[1]); // throws IllegalArgumentException (quite possible) when given an undefined value, ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				} catch(IllegalArgumentException iaex){
					throw new UnknownFlagException("Flag not among listed.", iaex);
				}
				name = parts[2]; // throws ArrayIndexOutOfBoundsException (quite possible)
				try{
					if(isPositiveInteger(parts[3], 10)){ // throws ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						type = VesselType.getVesselTypeForCode(Integer.parseInt(parts[3])); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						if(type == null){
							throw new UnknownVesselTypeException("No Vessel type with code " + parts[3] + " listed.");
						}
					} else{
						type = VesselType.valueOf(parts[3].toUpperCase()); // throws IllegalArgumentException (quite possible) when given an undefined value, ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
					}
				} catch(IllegalArgumentException iaex){
					throw new UnknownVesselTypeException("Vessel type not among listed.", iaex);
				}
				length = Double.parseDouble(parts[4]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(length <= 0) throw new NotAPositiveNumberException("Length should be a positive number.");
				width = Double.parseDouble(parts[5]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(width <= 0) throw new NotAPositiveNumberException("Width should be a positive number.");
				weight = Double.parseDouble(parts[6]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(weight <= 0) throw new NotAPositiveNumberException("Weight should be a positive number.");
				maximumSpeed = Double.parseDouble(parts[7]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(maximumSpeed <= 0) throw new NotAPositiveNumberException("Maximum Speed should be a positive number.");
				minimumSpeed = Double.parseDouble(parts[8]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(minimumSpeed <= 0) throw new NotAPositiveNumberException("Minimum Speed should be a positive number.");
				acceleration = Double.parseDouble(parts[9]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(acceleration <= 0) throw new NotAPositiveNumberException("Acceleration should be a positive number.");
				deceleration = Double.parseDouble(parts[10]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(deceleration <= 0) throw new NotAPositiveNumberException("Deceleration should be a positive number.");
			} catch(NegativeNumberException | NotAPositiveNumberException ex){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Invalid value.", ex);
			} catch(ArrayIndexOutOfBoundsException aioobe){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Fields could be missing from the file, be situated in a line other than the 2nd, or perhaps a wrong delimiter is used.", aioobe);
			} catch(NullPointerException | IllegalArgumentException iae){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Bad input.", iae);
			} catch(CSVVesselException ve){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Unknown Vessel description", ve);
			}
			return new Vessel(mmsi, flag, name, type, length, width, weight, maximumSpeed, minimumSpeed, acceleration, deceleration);
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
	
	private static boolean isPositiveInteger(String input, int radix)
	{
		if(input == null) return false;
		if(input.isEmpty()) return false;
		for(int i = 0; i < input.length(); i++){
			char c = input.charAt(i);
			if(Character.digit(c, radix) < 0) return false;
		}
		return true;
	}
}