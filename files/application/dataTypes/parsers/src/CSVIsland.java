import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class CSVIsland implements CSVParsable
{
	private File inputFile;
	
	CSVIsland(File inputFile)
	{
		this.inputFile = inputFile;
	}
	
	public Island getData(boolean omitFirstLine) throws CSVInputException
	{
		int lineNumber = 0;
		String line;
		String[] parts;
		double longitude;
		double latitude;
		LinkedList<Coordinates> apexes = new LinkedList<Coordinates>();
		String[] islandFileName;
		String islandName;
		String[] islandNameWords;
		String islandNamePhrase = new String();
		int indexOfLastChar;
		String finalName;
		Island isl;
		
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
						apexes.add(new Coordinates(longitude, latitude));
					} else{
						badInputMessage(lineNumber);
					}
				} catch(IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e){ // just to be on the safe side
					badInputMessage(lineNumber);
				}
			}
			try{
				islandFileName = inputFile.getName().split("\\."); // this throws PatternSyntaxException, impossible here
				islandName = islandFileName[0]; // drop the filename extension and its dot.
				islandNameWords = islandName.split("_"); // this throws PatternSyntaxException, impossible here
				for(String islandNameWord : islandNameWords){
					islandNamePhrase += islandNameWord.substring(0, 1).toUpperCase() + islandNameWord.substring(1).toLowerCase(); // capitalize 1st letter, lower-case the rest. This throws IndexOutOfBoundsException, impossible here
					islandNamePhrase += " "; // add a space between words
				}
				indexOfLastChar = islandNamePhrase.length() - 1;
				finalName = islandNamePhrase.substring(0, indexOfLastChar); //remove the trailing space
			} catch(PatternSyntaxException | IndexOutOfBoundsException e){ // just to be on the safe side
				e.printStackTrace();
				finalName = inputFile.getName();
			}
			try{
				isl = Island.createIsland(apexes, finalName);
				return isl;
			} catch(CSVIslandException ie){
				throw new CSVInputException(trimFilePath(this.inputFile.getPath()) + ": Invalid sequence of island apexes.", ie);
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