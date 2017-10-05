import java.util.Date;
import java.text.SimpleDateFormat;

class AISBroadcast extends Broadcast
{
	Date timeStamp;
	private Vessel vessel;
	
	AISBroadcast(long t, Vessel vessel, Coordinates position, Azimuth heading, double speed)
	{
		super(position, heading, speed);
		timeStamp = new Date(t);
		this.vessel = vessel;
	}
	
	AISBroadcast(long t, Vessel vessel, Broadcast broadcast)
	{
		super(broadcast.getPosition(), broadcast.getHeading(), broadcast.getSpeed());
		timeStamp = new Date(t);
		this.vessel = vessel;
	}
	
	public Date getTimeStamp() {return timeStamp;}
	public Vessel getVessel() {return vessel;}
	
	public void toConsole()
	{
		
		SimpleDateFormat sdf = new SimpleDateFormat("EE d MMM yyyy HH:mm:ss");
		String formattedDate = sdf.format(timeStamp);
	
		String name = vessel.getName();
		double x = position.getLongitude();
		double y = position.getLatitude();
		double hdg = heading.getAzimuth();
		System.out.print(formattedDate);
		System.out.print("   ");
		System.out.printf("Vessel: %-24s", name);
		System.out.format("LON: %8.2f", x);
		System.out.print("   ");
		System.out.format("LAT: %8.2f", y);
		System.out.print("\t");
		System.out.print("\t");
		System.out.format("HDG: %8.2f", hdg);
		System.out.print("   ");
		System.out.format("SPD: %8.2f", speed);
		System.out.println();
	}
}