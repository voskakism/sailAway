import java.util.LinkedList;

class Sea
{
	private LinkedList<Island> islands;
	
	private Coordinates northEnd;
	private Coordinates southEnd;
	private Coordinates westEnd;
	private Coordinates eastEnd;
	
	public Coordinates getNorthEnd(){return northEnd;}
	public Coordinates getSouthEnd(){return southEnd;}
	public Coordinates getWestEnd(){return westEnd;}
	public Coordinates getEastEnd(){return eastEnd;}
	public LinkedList<Island> getIslands(){return islands;}
	
	public Sea()
	{
		islands = new LinkedList<Island>();
	}
	
	public void addIsland(Island isle) throws IslandOverlapException
	{
		LinkedList<Island> overlappingIsles = new LinkedList<Island>();
		for(Island isl : islands){
			if(isle.overlapsWith(isl)){
				overlappingIsles.add(isl);
			}
		}
		if(overlappingIsles.isEmpty()){
			islands.add(isle);
			reCalculateNSWEExtremes(isle);
		} else{
			String isles = new String();
			for(Island isla : overlappingIsles){
				isles += isla.getName();
				isles += ", ";
			}
			try{
				isles = isles.substring(0, isles.length() - 2);
			} catch (IndexOutOfBoundsException e){
				e.printStackTrace();
			}
			throw new IslandOverlapException(isle.getName() + " overlaps with previously placed " + isles);
		}
	}
	
	private void reCalculateNSWEExtremes(Island isle)
	{
		if(islands.size() == 1){
			northEnd = isle.getCapeNorth();
			southEnd = isle.getCapeSouth();
			westEnd = isle.getCapeWest();
			eastEnd = isle.getCapeEast();
		} else{
			for(Island i : islands)
			{
				if(isle.getCapeNorth().getLatitude() > northEnd.getLatitude()){
					northEnd = isle.getCapeNorth();
				}
				if(isle.getCapeSouth().getLatitude() < southEnd.getLatitude()){
					southEnd = isle.getCapeSouth();
				}
				if(isle.getCapeWest().getLongitude() < westEnd.getLongitude()){
					westEnd = isle.getCapeWest();
				}
				if(isle.getCapeEast().getLongitude() > eastEnd.getLongitude()){
					eastEnd = isle.getCapeEast();
				}
			}
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println("The compass extremes of the sea, are:");
			System.out.println("North: " + northEnd.toString());
			System.out.println("South: " + southEnd.toString());
			System.out.println("West: " + westEnd.toString());
			System.out.println("East: " + eastEnd.toString());
		}
	}
	
	public int islandCount()
	{
		return islands.size();
	}
}