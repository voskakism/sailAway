import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

class AISBroadcast extends Broadcast
{
	private Date timeStamp;
	private Vessel vessel;
	private String message;
	
	AISBroadcast(long t, Vessel vessel, Coordinates position, Azimuth heading, double speed)
	{
		super(position, heading, speed);
		timeStamp = new Date(t);
		this.vessel = vessel;
		formatOutputString();
	}
	
	AISBroadcast(long t, Vessel vessel, Broadcast broadcast)
	{
		super(broadcast.getPosition(), broadcast.getHeading(), broadcast.getSpeed());
		timeStamp = new Date(t);
		this.vessel = vessel;
		formatOutputString();
	}
	
	@Override
	public String toString() {return message;}
	public Vessel getVessel() {return vessel;}
	public Date getTimeStamp() {return timeStamp;}
	
	public void toConsole()
	{
		System.out.println(message);
	}
	
	public void toFile(File aisLogFile)
	{
		try(BufferedWriter bW = new BufferedWriter(new FileWriter(aisLogFile, true))){ // true to append to an existing file, preserving its contents
			bW.write(message + System.getProperty("line.separator")); // <-- thread-safe method
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void formatOutputString()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("EE d MMM yyyy HH:mm:ss");
		String formattedDate = sdf.format(timeStamp);
		
		String name = vessel.getName();
		double x = position.getLongitude();
		double y = position.getLatitude();
		double hdg = heading.getAzimuth();
	
		String s3 = "   "; // just 3 spaces
		this.message = formattedDate + s3 + String.format("Vessel: %-24s", name) + String.format("LON: %8.2f", x) + s3 +
		String.format("LAT: %8.2f", y) + "\t" + "\t" + String.format("HDG: %8.2f", hdg) + s3 + String.format("SPD: %8.2f", speed);
	}
}