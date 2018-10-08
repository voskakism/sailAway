import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class CSVBroadcasts implements CSVParsable
{
	private File inputFile;
	
	CSVBroadcasts(File inputFile)
	{
		this.inputFile = inputFile;
	}
	
	public BroadcastSequence getData(boolean omitFirstLine) throws CSVInputException
	{
		int lineNumber = 0;
		String line;
		String[] parts;
		double longitude;
		double latitude;
		Coordinates position;
		double h;
		Azimuth heading;
		double speed;
		LinkedList<Broadcast> broadcasts = new LinkedList<Broadcast>();
	
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
					if(parts.length >= 4){
						longitude = Double.parseDouble(parts[0]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						latitude = Double.parseDouble(parts[1]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						position = new Coordinates(longitude, latitude);
						h = Double.parseDouble(parts[2]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						heading = new Azimuth(h);
						speed = Double.parseDouble(parts[3]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (impossible due to length check), NullPointerException (impossible: AIOoBE will be thrown before NPE)
						// any further parts[] beyond [3] will be ignored
						broadcasts.add(new Broadcast(position, heading, speed));
					} else{
						badInputMessage(lineNumber);
					}
				} catch(IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e){ // just to be on the safe side
					badInputMessage(lineNumber);
				}
			}
			try{
				return BroadcastSequence.createBroadcastSequence(broadcasts);
			} catch(CSVBroadcastException be){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Invalid sequence of broadcasts.", be);
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