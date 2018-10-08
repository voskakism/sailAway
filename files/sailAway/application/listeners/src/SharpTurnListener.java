import java.io.File;
import com.espertech.esper.client.*;

public class SharpTurnListener extends AISEventListener implements UpdateListener
{
	SharpTurnListener(File eventReport)
	{
		super(eventReport);
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents)
	{
		if(newEvents != null){
			for(EventBean event : newEvents){
				Long mmsi = (Long)event.get("mmsi");
				String name = (String)event.get("name");
				Double angleRotated = (Double)event.get("angleRotated");
				Double timeTaken = (Double)event.get("timeTaken");
				Double rateOfTurn = (Double)event.get("rateOfTurn");
				writeToOutputFile("EVENT: Sharp turn taken. " + lineBreak +
								"	Vessel:" + lineBreak +
								"		MMSI: " + mmsi + lineBreak +
								"		Name: " + name + lineBreak +
								"	Rotation: " + angleRotated + " degrees." + lineBreak +
								"	Time Taken: " + timeTaken + " seconds." + lineBreak +
								"	Rate of Turn: " + rateOfTurn + " degrees per second." + lineBreak);
			}
		}
	}
}