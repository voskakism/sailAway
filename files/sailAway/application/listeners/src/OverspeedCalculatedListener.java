import java.io.File;
import com.espertech.esper.client.*;

public class OverspeedCalculatedListener extends AISEventListener implements UpdateListener
{
	OverspeedCalculatedListener(File eventReport)
	{
		super(eventReport);
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents)
	{
		if(newEvents != null){
			for(EventBean event : newEvents){
				Long mmsi = (Long)event.get("mmsi");
				String name = (String)event.get("name");
				Double distanceCovered = (Double)event.get("distanceCovered");
				Double timeTaken = (Double)event.get("timeTaken");
				Double calculatedSpeed = (Double)event.get("calculatedSpeed");
				writeToOutputFile("EVENT: Calculated speed is above the legal limit of " + AppConfiguration.getGlobalSpeedLimit() + " m/s." + lineBreak +
								"	Vessel:" + lineBreak +
								"		MMSI: " + mmsi + lineBreak +
								"		Name: " + name + lineBreak +
								"	Distance Covered: " + distanceCovered + " meters." + lineBreak +
								"	Time Taken: " + timeTaken + " seconds." + lineBreak +
								"	Calculated speed: " + calculatedSpeed + " m/s." + lineBreak);
			}
		}
	}
}