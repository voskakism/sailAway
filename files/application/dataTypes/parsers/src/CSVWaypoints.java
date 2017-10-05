import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class CSVWaypoints implements CSVParsable
{
	private File inputFile;
	private Sea sea;
	
	CSVWaypoints(File inputFile, Sea sea)
	{
		this.inputFile = inputFile;
		this.sea = sea;
	}
	
	public Itinerary getData(boolean omitFirstLine) throws CSVInputException
	{
		int lineNumber = 0;
		String line;
		String[] parts;
		double longitude;
		double latitude;
		LinkedList<Waypoint> waypoints = new LinkedList<Waypoint>();
		Itinerary itin;
	
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))){
			if(omitFirstLine){
				lineNumber++;
				bufferedReader.readLine(); // throws IOException
			}
			while((line = bufferedReader.readLine()) != null){ // throws IOException
				try{
					lineNumber++;
					line = line.replaceAll("\\s+", ""); // eat whitespace. This throws PatternSyntaxException, impossible here
					if(line.isEmpty()){ // ... or just only whitespace (see above),
						break;
					}
					if(line.charAt(0) == '#'){ // ... "if the first non-whitespace char is # ..." (see above). This throws IndexOutOfBoundsException, impossible here as it would have broken anyway, see earlier "break" statement
						continue;
					}
					parts = line.split(","); // throws PatternSyntaxException, impossible here
					if(parts.length >= 2){
						longitude = Double.parseDouble(parts[0]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						latitude = Double.parseDouble(parts[1]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						// any further parts[] beyond [1] will be ignored
						waypoints.add(new Waypoint(longitude, latitude));
					} else{
						badInputMessage(lineNumber);
					}
				} catch(IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e){ // just to be on the safe side
					badInputMessage(lineNumber);
				}
			}
			try{
				itin = Itinerary.createRoute(waypoints, sea);
				return itin;
			} catch(CSVItineraryException ie){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Invalid sequence of waypoints.", ie);
			}
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
	
	private void badInputMessage(int line)
	{
		System.out.println("Bad input, file \"" + inputFile.getName() + "\". Ignoring line " + line);
	}
}