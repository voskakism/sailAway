import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

class AppConfigurationCSVParser
{
	private File inputFile;
	
	AppConfigurationCSVParser(File inputFile)
	{
		this.inputFile = inputFile;
	}
	
	public void getData(boolean omitFirstLine)
	{
		String line;
		String[] parts;
		
		double turbulence;
		double centripetalForce;
		GuiMode guiMode;
		int trailLength;
		int laneWidth;
		int broadcastInterval;
		boolean debugStatus;
		double immobileSpeedThreshold;
		double globalSpeedLimit;
	
		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))){
			try{
				if(omitFirstLine){
					bufferedReader.readLine(); // throws IOException
				}
			} catch(IOException ioex){
				System.out.println("I/O error. Check the first line in " + inputFile.getName());
			}
			line = bufferedReader.readLine(); // throws IOException
			line = line.replaceAll("\\s+", ""); // eat whitespace. This throws PatternSyntaxException, impossible here
			parts = line.split(","); // throws PatternSyntaxException, impossible here
			// Turbulence
			try{
				turbulence = Double.parseDouble(parts[0]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(turbulence < 0){
					System.out.println("Turbulence has to be within [0, 1]. Setting Turbulence to 0.");
					AppConfiguration.setTurbulence(0);
				} else if(turbulence > 1){
					System.out.println("Turbulence has to be within [0, 1]. Setting Turbulence to 1.");
					AppConfiguration.setTurbulence(1);
				} else{
					AppConfiguration.setTurbulence(turbulence);
				}
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Turbulence setting of " + AppConfiguration.getDefaultTurbulence());
			} catch(NumberFormatException npe){
				System.out.println("Bad input. Check configuration file. Preserving the default Turbulence setting of " + AppConfiguration.getDefaultTurbulence());
			}
			// Centripetal Force
			try{
				centripetalForce = Double.parseDouble(parts[1]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(centripetalForce <= 0) throw new NotAPositiveNumberException("Centripetal Force should be a positive number.");
				AppConfiguration.setCentripetalForce(centripetalForce);
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Centripetal Force setting of " + AppConfiguration.getDefaultCentripetalForce() + " Newtons.");
			} catch(NumberFormatException npe){
				System.out.println("Bad input. Check configuration file. Preserving the default Centripetal Force setting of " + AppConfiguration.getDefaultCentripetalForce() + " Newtons.");
			} catch(NotAPositiveNumberException npne){
				System.out.println(npne.toString() + " Preserving the default Centripetal Force setting of " + AppConfiguration.getDefaultCentripetalForce() + " Newtons.");
			}
			// GUI Mode
			try{
				guiMode = GuiMode.valueOf(parts[2].toUpperCase()); // throws IllegalArgumentException (quite possible) when given an undefined value, ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				AppConfiguration.setGuiMode(guiMode);
			} catch(IllegalArgumentException iaex){
				System.out.println("Gui Mode not among listed. Preserving the default GUI Mode setting: " + AppConfiguration.getDefaultGuiMode());
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default GUI Mode setting: " + AppConfiguration.getDefaultGuiMode());
			}
			// Trail Length
			try{
				trailLength = Integer.parseInt(parts[3]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(trailLength < 0) throw new NegativeNumberException("Trail Length cannot be negative.");
				AppConfiguration.setTrailLength(trailLength);
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Trail Length setting of " + AppConfiguration.getDefaultTrailLength() + " steps.");
			} catch(NumberFormatException npe){
				System.out.println("Bad input. Check configuration file. Preserving the default Trail Length setting of " + AppConfiguration.getDefaultTrailLength() + " steps.");
			} catch(NegativeNumberException nne){
				System.out.println(nne.toString() + " Preserving the default Trail Length setting of " + AppConfiguration.getDefaultTrailLength() + " steps.");
			}
			// Lane Width
			try{
				laneWidth = Integer.parseInt(parts[4]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(laneWidth <= 0) throw new NotAPositiveNumberException("Lane Width should be a positive number.");
				AppConfiguration.setLaneWidth(laneWidth);
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Lane Width setting of " + AppConfiguration.getDefaultLaneWidth() + " meters.");
			} catch(NumberFormatException npe){
				System.out.println("Bad input. Check configuration file. Preserving the default Lane Width setting of " + AppConfiguration.getDefaultLaneWidth() + " meters.");
			} catch(NotAPositiveNumberException npne){
				System.out.println(npne.toString() + " Preserving the default Lane Width setting of " + AppConfiguration.getDefaultLaneWidth() + " meters.");
			}
			// Broadcast Interval
			try{
				broadcastInterval = Integer.parseInt(parts[5]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(broadcastInterval <= 0) throw new NotAPositiveNumberException("Broadcast Interval should be a positive number.");
				AppConfiguration.setBroadcastInterval(broadcastInterval);
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Broadcast Interval setting of " + AppConfiguration.getDefaultBroadcastInterval() + " milliseconds.");
			} catch(NumberFormatException npe){
				System.out.println("Bad input. Check configuration file. Preserving the default Broadcast Interval setting of " + AppConfiguration.getDefaultBroadcastInterval() + " milliseconds.");
			} catch(NotAPositiveNumberException npne){
				System.out.println(npne.toString() + " Preserving the default Broadcast Interval setting of " + AppConfiguration.getDefaultBroadcastInterval() + " milliseconds.");
			}
			// Debug Status
			try{
				debugStatus = Boolean.parseBoolean(parts[6]); // throws ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				AppConfiguration.setDebugStatus(debugStatus);
				if(!((parts[6].toUpperCase().equals("TRUE")) || (parts[6].toUpperCase().equals("FALSE")))){
					System.out.println("Bad input. Check configuration file. Debug Status set to \"false\".");
				}
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Debug Status setting of " + AppConfiguration.getDefaultDebugStatus());
			}
			// Immobile Speed Threshold
			try{
				immobileSpeedThreshold = Double.parseDouble(parts[7]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(immobileSpeedThreshold < 0) throw new NegativeNumberException("Immobile Speed Threshold cannot be negative.");
				AppConfiguration.setImmobileSpeedThreshold(immobileSpeedThreshold);
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Immobile Speed Threshold setting of " + AppConfiguration.getDefaultImmobileSpeedThreshold());
			} catch(NumberFormatException npe){
				System.out.println("Bad input. Check configuration file. Preserving the default Immobile Speed Threshold setting of " + AppConfiguration.getDefaultImmobileSpeedThreshold());
			} catch(NegativeNumberException nne){
				System.out.println(nne.toString() + " Preserving the default Immobile Speed Threshold setting of " + AppConfiguration.getDefaultImmobileSpeedThreshold());
			}
			// Global Speed Limit
			try{
				globalSpeedLimit = Double.parseDouble(parts[8]); // throws NumberFormatException (quite possible), ArrayIndexOutOfBoundsException (quite possible), NullPointerException (impossible: AIOoBE will be thrown before NPE)
				if(globalSpeedLimit <= 0) throw new NotAPositiveNumberException("Global Speed Limit should be a positive number.");
				AppConfiguration.setGlobalSpeedLimit(globalSpeedLimit);
			} catch(ArrayIndexOutOfBoundsException bex){
				System.out.println("Bad input. Check number of variables in configuration file. Preserving the default Global Speed Limit setting of " + AppConfiguration.getDefaultGlobalSpeedLimit());
			} catch(NumberFormatException npe){
				System.out.println("Bad input. Check configuration file. Preserving the default Global Speed Limit setting of " + AppConfiguration.getDefaultGlobalSpeedLimit());
			} catch(NotAPositiveNumberException npne){
				System.out.println(npne.toString() + " Preserving the default Global Speed Limit setting of " + AppConfiguration.getDefaultGlobalSpeedLimit());
			}
		} catch(IOException ioex){
			System.out.println("I/O error in configuration file. Using default settings.");
		}
	}
}