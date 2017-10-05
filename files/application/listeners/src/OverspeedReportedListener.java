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
		EventBean event = newEvents[0];
		Double speed = (Double)event.get("speed");
		Long mmsi = (Long)event.get("vessel.mmsi");
		String name = (String)event.get("vessel.name");
		writeToOutputFile("EVENT: Reported speed (through AIS broadcast) is above the legal limit of " + AppConfiguration.getGlobalSpeedLimit() + " m/s.");
		writeToOutputFile("Vessel:");
		writeToOutputFile("		MMSI: " + mmsi);
		writeToOutputFile("		Name: " + name);
		writeToOutputFile("Reported speed: " + speed.toString() + " m/s.");
		writeToOutputFile("\n");
	}
}