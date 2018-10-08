import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

public abstract class AISEventListener
{
	private File eventReport;
	protected String lineBreak;
	
	AISEventListener(File eventReport)
	{
		this.eventReport = eventReport;
		lineBreak = System.getProperty("line.separator");
	}
	
	public synchronized void writeToOutputFile(String input)
	{
		try(BufferedWriter bW = new BufferedWriter(new FileWriter(eventReport, true))){ // true to append to an existing file, preserving its contents
			bW.write(input);
			bW.newLine();
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
}