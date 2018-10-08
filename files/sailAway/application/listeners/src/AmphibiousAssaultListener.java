import java.io.File;
import java.util.Date;
import com.espertech.esper.client.*;

public class AmphibiousAssaultListener extends AISEventListener implements UpdateListener
{
	AmphibiousAssaultListener(File eventReport)
	{
		super(eventReport);
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents)
	{
		if(newEvents != null){
			for(EventBean event : newEvents){
				Date time = (Date)event.get("time");
				Vessel ship = (Vessel)event.get("ship");
				Coordinates site = (Coordinates)event.get("site");
				Double ld = (Double)event.get("ld");
				
				long mmsi = ship.getMmsi();
				String name = ship.getName();
				double landDistance = (double)ld;
				
				writeToOutputFile("EVENT: Possible imminent amphibious assault by a foreign Navy," + lineBreak +
								"due to their unauthorized sail within National waters." + lineBreak +
								"Details:" + lineBreak +
								"	Date and time: " + time + lineBreak +
								"	Offending foreign military vessel:" + lineBreak +
								"		MMSI: " + mmsi + lineBreak +
								"		Name: " + name + lineBreak +
								"	Position: " + site + lineBreak +
								"	Distance from nearest landmass: " + landDistance + lineBreak);
			}
		}
	}
}