import java.io.File;
import java.util.Date;
import com.espertech.esper.client.*;

public class PackageDeliveryListener extends AISEventListener implements UpdateListener
{
	PackageDeliveryListener(File eventReport)
	{
		super(eventReport);
	}
	
	public void update(EventBean[] newEvents, EventBean[] oldEvents)
	{
		if(newEvents != null){
			for(EventBean event : newEvents){
				Date timeOfDispense = (Date)event.get("timeOfDispense");
				Date timeOfRecovery = (Date)event.get("timeOfRecovery");
				Vessel dispenserVessel = (Vessel)event.get("dispenserVessel");
				Vessel collectorVessel = (Vessel)event.get("collectorVessel");
				long dispenserVesselMMSI = dispenserVessel.getMmsi();
				String dispenserVesselName = dispenserVessel.getName();
				long collectorVesselMMSI = collectorVessel.getMmsi();
				String collectorVesselName = collectorVessel.getName();
				Coordinates siteOfDispense = (Coordinates)event.get("siteOfDispense");
				Coordinates siteOfRecovery = (Coordinates)event.get("siteOfRecovery");
				double distance = (Double)event.get("dist");
				
				long dispenseMillis = timeOfDispense.getTime();
				long recoveryMillis = timeOfRecovery.getTime();
				long millisOnWater = recoveryMillis - dispenseMillis;
				double millisInASec = 1000;
				double secondsOnWater = ((double)millisOnWater / millisInASec);
				
				writeToOutputFile("EVENT: Package Delivery: Vessel " + dispenserVesselName + " discarded a package, subsequently collected by vessel " + collectorVesselName + "," + lineBreak +
								secondsOnWater + " seconds later, " + distance + " meters away." + lineBreak +
								"	Dispense details:" + lineBreak +
								"		Vessel:" + lineBreak +
								"			MMSI: " + dispenserVesselMMSI + lineBreak +
								"			Name: " + dispenserVesselName + lineBreak +
								"		Time: " + timeOfDispense + lineBreak +
								"		Location: " + siteOfDispense + lineBreak +
								"	Recovery details:" + lineBreak +
								"		Vessel:" + lineBreak +
								"			MMSI: " + collectorVesselMMSI + lineBreak +
								"			Name: " + collectorVesselName + lineBreak +
								"		Time: " + timeOfRecovery + lineBreak +
								"		Location: " + siteOfRecovery + lineBreak);
			}
		}
	}
}