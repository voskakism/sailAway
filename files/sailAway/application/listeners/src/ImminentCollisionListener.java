import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.espertech.esper.client.*;

public class ImminentCollisionListener extends AISEventListener implements UpdateListener
{
	ImminentCollisionListener(File eventReport)
	{
		super(eventReport);
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents)
	{
		if(newEvents != null){
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			for(EventBean event : newEvents){
				long timeOfHLEmillis = (long)event.get("time");
				Date timeOfHLE = new Date(timeOfHLEmillis);
				
				Date timeStampA = (Date)event.get("a.timeStamp");
				Date timeStampC = (Date)event.get("c.timeStamp");
				String timeA = sdf.format(timeStampA);
				String timeC = sdf.format(timeStampC);
				
				
				Vessel vesselA = (Vessel)event.get("a.vessel");
				Vessel vesselC = (Vessel)event.get("c.vessel");
				long mmsiA = vesselA.getMmsi();
				long mmsiC = vesselC.getMmsi();
				String nameA = vesselA.getName();
				String nameC = vesselC.getName();
				Coordinates positionA = (Coordinates)event.get("a.position");
				Coordinates positionC = (Coordinates)event.get("c.position");
				Azimuth azimuthA = (Azimuth)event.get("a.heading");
				Azimuth azimuthC = (Azimuth)event.get("c.heading");
				double headingA = azimuthA.getAzimuth();
				double headingC = azimuthC.getAzimuth();
				double speedA = (double)event.get("a.speed");
				double speedC = (double)event.get("c.speed");
				writeToOutputFile("EVENT: Ιmminent Collision of vessels. Alert received at " + timeOfHLE + lineBreak +
								"At " + timeA + ", vessel of:" + lineBreak +
								"	MMSI: " + mmsiA + lineBreak +
								"	Name: " + nameA + lineBreak +
								"	Reported:" + lineBreak +
								"	Position: " + positionA + lineBreak +
								"	Heading: " + headingA + " degrees." + lineBreak +
								"	Speed: " + speedA + " m/s." + lineBreak +
								"At " + timeC + ", vessel of:" + lineBreak +
								"	MMSI: " + mmsiC + lineBreak +
								"	Name: " + nameC + lineBreak +
								"	Reported:" + lineBreak +
								"	Position: " + positionC + lineBreak +
								"	Heading: " + headingC + " degrees." + lineBreak +
								"	Speed: " + speedC + " m/s." + lineBreak);
			}
		}
	}
}