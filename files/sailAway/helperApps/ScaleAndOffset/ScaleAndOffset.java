import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.util.InputMismatchException;

public class ScaleAndOffset
{
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);
		System.out.println("\"Scale and Offset\" for arrays of Coordinates.");
		System.out.println("..a helper program for the \"sailAway\" application.");
		System.out.println();
		System.out.println("Enter \"1\" for instructions, or anything else to skip.");
		String choice = scanner.nextLine();
		if(choice.equals("1")){
			System.out.println();
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("This helper application is to be used for manipulating certain types of input files used by the application \"sailAway\".");
			System.out.println("Those types of files are .csv ones, each containing an array of Coordinates, i.e (longitude,latitude) pairs, having at most one such pair per line.");
			System.out.println("Typically those are the \"waypoints.csv\" files representing ship routes, or the island files, representing island polygons.");
			System.out.println();
			System.out.println("The app scales the points on their x and y axes by multiplying their respective components by coefficients provided by the user.");
			System.out.println("Remember that such coefficients need to be positive numbers.");
			System.out.println("After scaling, the points are offset in both axes by different offsets, also provided by the user.");
			System.out.println("Offsets can be zero or numbers of either sign, depending on the desired direction of shift.");
			System.out.println("Note that scaling itself produces an \"offset effect\", as the points' components are multiplied, thus moving either away or towards their axes.");
			System.out.println("Effectively, the only point that keeps its position after scaling is the origin (0,0).");
			System.out.println();
			System.out.println("The app can perform the described operations for multiple files per run, namely every compatible file found in the \"input\" folder.");
			System.out.println("The original files remain unmodified in their directory, while the program writes their modified, homonymous version in the \"output\" folder.");
			System.out.println();
			System.out.println("This program can detect and ignore empty / whitespace-only lines and commented lines in the input files.");
			System.out.println("It cannot however detect wether your files use column header lines, so it asks the user during runtime.");
			System.out.println("Note in case you input multiple files, they all need to have the same status regarding their use of column header lines,");
			System.out.println("as the program asks once, and extends the response for all input files.");
			System.out.println("Disclaimer: User input validation regarding the files in NOT performed in this helper app, as it is meant to edit files already valid with \"sailAway\".");
			System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
		}
		System.out.println();
		
		double xScale = getScale(scanner, "Please input x axis scale");
		double yScale = getScale(scanner, "Please input y axis scale. If not sure, input the same you entered for x axis, " + xScale);
		double xOffset = getOffset(scanner, "Please input x axis offset");
		double yOffset = getOffset(scanner, "Please input y axis offset");
		boolean omitFirstLine = columnHeaders(scanner, "Does the first line in each of your input files, represent column headers? (true / false)");
		
		String sep = System.getProperty("file.separator");
		File inputDirectory = new File ("input");
		File outputDirectory = new File ("output");
		for(File inCsv : inputDirectory.listFiles()){ //returns files AND directories
			if(inCsv.isDirectory()){ //ignore directories
				continue;
			}
			
			File outCsv = new File(outputDirectory + sep + inCsv.getName());
			try(BufferedWriter bW = new BufferedWriter(new FileWriter(outCsv))){
				String inLine;
				String outLine;
				String[] parts;
				
				try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inCsv))){
					if(omitFirstLine){
						inLine = bufferedReader.readLine();
						bW.write(inLine);
						bW.newLine();
					}
					while((inLine = bufferedReader.readLine()) != null){
						inLine = inLine.replaceAll("\\s+", ""); // eat whitespace.
						if(inLine.isEmpty()){
							bW.newLine();
						} else if(inLine.charAt(0) == '#'){
							bW.write(inLine);
							bW.newLine();
						} else{
							parts = inLine.split(",");
							double x = Double.parseDouble(parts[0]); 
							double y = Double.parseDouble(parts[1]); 
							x *= xScale;
							y *= yScale;
							x += xOffset;
							y += yOffset;
							outLine = x + "," + y;
							
							bW.write(outLine);
							bW.newLine();
						}
					}
				}
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	private static double getScale(Scanner scanner, String message)
	{
		double scale = 1;
		boolean inputErr = false;
		do{
			inputErr = false;
			System.out.println(message);
			try{
				scale = scanner.nextDouble();
				if(scale <= 0){
					inputErr = true;
					System.out.println("Scale must be positive, try again.");
				}
			} catch(InputMismatchException ime){
				scanner.next(); // to clear garbage input
				inputErr = true;
				System.out.println("Not a number, try again.");
			}
			System.out.println();
		}while(inputErr);
		return scale;
	}
	
	private static double getOffset(Scanner scanner, String message)
	{
		double offset = 0;
		boolean inputErr = false;
		do{
			inputErr = false;
			System.out.println(message);
			try{
				offset = scanner.nextDouble();
			} catch(InputMismatchException ime){
				scanner.next(); // to clear garbage input
				inputErr = true;
				System.out.println("Not a number, try again.");
			}
			System.out.println();
		}while(inputErr);
		return offset;
	}
	
	private static boolean columnHeaders(Scanner scanner, String message)
	{
		boolean headers = false;
		boolean inputErr = false;
		do{
			inputErr = false;
			System.out.println(message);
			try{
				headers = scanner.nextBoolean();
			} catch(InputMismatchException ime){
				scanner.next(); // to clear garbage input
				inputErr = true;
				System.out.println("Not a boolean value, try again. Input either \"true\" or \"false\".");
			}
			System.out.println();
		}while(inputErr);
		return headers;
	}
}