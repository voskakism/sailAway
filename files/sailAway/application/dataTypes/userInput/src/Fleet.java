import java.util.LinkedList;

class Fleet extends UserInput
{
	private LinkedList<Vessel> vessels;
	
	public Fleet()
	{
		vessels = new LinkedList<Vessel>();
	}
	
	public void registerVessel(Vessel v) throws CSVVesselException
	{
		long candidateMmsi = v.getMmsi();
		for(Vessel vessel : vessels){
			if(vessel.getMmsi() == candidateMmsi){
				throw new VesselAlreadyExistsException("The vessel with MMSI: " + candidateMmsi + " appears to be currently underway elsewhere on the sea.");
			}
		}
		vessels.add(v);
	}
}