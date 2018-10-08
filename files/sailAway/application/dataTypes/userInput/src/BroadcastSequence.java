import java.util.LinkedList;

class BroadcastSequence extends UserInput
{
	private LinkedList<Broadcast> broadcasts;
	
	public LinkedList<Broadcast> getBroadcasts() {return broadcasts;}
	
	private BroadcastSequence(LinkedList<Broadcast> broadcasts)
	{
		this.broadcasts = broadcasts;
	}
	
	public static BroadcastSequence createBroadcastSequence(LinkedList<Broadcast> broadcasts) throws CSVBroadcastException
	{
		if(broadcasts.size() >= 2){
			return new BroadcastSequence(broadcasts);
		} else{
			throw new LessThan2BroadcastsException("Not enough broadcasts.");
		}
	}
}