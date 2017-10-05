import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

public class App
{
	public static void main(String[] args)
	{
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("EdMMMyyyy__H-m-s");
		String directoryName = sdf.format(now);
		
		File outDir = new File("output");
		File runDir = new File(outDir, directoryName);
		File logDir = new File(runDir, "logs");
		logDir.mkdirs();
		
		
		File l1 = new File(logDir, "1.log");
		File l2 = new File(logDir, "2.log");
		try{
			l1.createNewFile();
			l2.createNewFile();
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(l1), "utf-8"))){
			writer.write("11");
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(l2), "utf-8"))){
			writer.write("22");
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
}