import java.io.File;
import com.espertech.esper.client.*;

public class OverspeedReportedListener extends AISEventListener implements UpdateListener
{
	OverspeedReportedListener(File eventReport)
	{
		super(eventReport);
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents)
	{
		if(newEvents != null){
			for(EventBean event : newEvents){
				Double speed = (Double)event.get("speed");
				Long mmsi = (Long)event.get("vessel.mmsi");
				String name = (String)event.get("vessel.name");
				Flag flag = (Flag)event.get("vessel.flag");
				VesselType type = (VesselType)event.get("vessel.type");
				writeToOutputFile("EVENT: Reported speed is above the legal limit of " + AppConfiguration.getGlobalSpeedLimit() + " m/s." + lineBreak +
								"	Vessel:" + lineBreak +
								"		MMSI: " + mmsi + lineBreak +
								"		Name: " + name + lineBreak +
								"		Flag: " + flag + lineBreak +
								"		Type: " + type + lineBreak +
								"	Reported speed: " + speed + " m/s." + lineBreak);
			}
		}
	}
}