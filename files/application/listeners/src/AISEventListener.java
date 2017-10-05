import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class AISEventListener
{
	private File eventReport;
	
	AISEventListener(File eventReport)
	{
		this.eventReport = eventReport;
	}
	
	public void writeToOutputFile(String input)
	{
		try(BufferedWriter bW = new BufferedWriter(new FileWriter(eventReport, true))){ // true to append to an existing file, preserving its contents
			bW.write(input);
			bW.newLine();
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
}