import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

class Island extends UserInput
{
	private String name;
	private LinkedList<Shore> shores;
	
	private Coordinates capeNorth;
	private Coordinates capeEast;
	private Coordinates capeWest;
	private Coordinates capeSouth;
	
	public String getName(){return name;}
	public LinkedList<Shore> getShores(){return shores;}
	
	public Coordinates getCapeNorth(){return capeNorth;}
	public Coordinates getCapeSouth(){return capeSouth;}
	public Coordinates getCapeWest(){return capeWest;}
	public Coordinates getCapeEast(){return capeEast;}
	
	private Island(LinkedList<Shore> shores, String name)
	{
		this.name = name;
		this.shores = shores;
		findNSWEExtremes();
	}
	
	public static Island createIsland(LinkedList<Coordinates> apexes, String name) throws CSVIslandException
	{
		LinkedList<Shore> potentialShores;
		ListIterator<Coordinates> li;
		Coordinates start;
		Coordinates previous;
		Coordinates next;
		Shore s;
		int remainingApexes;
		boolean lastApex = false;
		
		remainingApexes = apexes.size();
		if(AppConfiguration.getDebugStatus()){
			System.out.println(name + ": " + remainingApexes + " apexes");
		}
		if(remainingApexes >= 3){
			potentialShores = new LinkedList<Shore>();
			li = apexes.listIterator();
			start = li.next();
			previous = start;
			remainingApexes--;
			while(remainingApexes >= 0){
				if(remainingApexes == 0){
					next = start;
					lastApex = true;
				}else{
					next = li.next();
				}
				s = new Shore(previous, next);
				if(AppConfiguration.getDebugStatus()){
					System.out.println("Shore created:");
					System.out.println(s.toString());
				}
				if(lineRemainsSimple(s, potentialShores, lastApex)){
					potentialShores.add(s);
				}else{
					throw new NotASimpleLineException(name + " island's shoreline intersects itself, i.e. it is not a simple line. Check: " + s.toString());
				}
				previous = next;
				remainingApexes--;
			}
			return new Island(potentialShores, name);
		}else {
			throw new LessThan3ApexesException("Island " + name + " has only " + remainingApexes + " apexes, while a valid island has at least 3.");
		}
	}
	
	private static boolean lineRemainsSimple(Shore s, LinkedList<Shore> potentialShores, boolean lastApex)
	{
		Shore shore, lastShore;
		for(int i = 0; i < (potentialShores.size() - 1); i++){ // The last Shore of the line is separately checked as an adjacent to the candidate shore. Adjacents tend to produce some false positives due to their common point.
			shore = potentialShores.get(i);
			if((lastApex) && (i == 0)){ // To the last candidate Shore of the island, which closes the polygon, the very first Shore is also an adjacent.
				if(AppConfiguration.getDebugStatus()){
					System.out.println("Leading");
				}
				if(s.intersectsAdjacent(shore)){
					return false;
				}
				continue; // The rest of the iteration must be skipped in this case, to avoid a false positive, as the adjacent should not be checked as an independent, "regular", Shore.
			}
			if(s.getIntersectionWith(shore).intersecting()){ // perhaps check containing rectangles' overlap first to save resources?
				if(AppConfiguration.getDebugStatus()){
					System.out.println(s.toString() + " intersects with " + shore.toString());
				}
				return false;
			}else{
				if(AppConfiguration.getDebugStatus()){
					System.out.println(s.toString() + " does not intersect with " + shore.toString());
				}
			}
		}
		try{
			lastShore = potentialShores.getLast();
		} catch(NoSuchElementException nse){ // potentialShores will be empty at the fist Shore's run, so we can be certain the line is still simple at this time.
			return true;
		}
		if(AppConfiguration.getDebugStatus()){
			System.out.println("Trailing");
		}
		if(s.intersectsAdjacent(lastShore)){
			return false;
		}
		return true;
	}
	
	private void findNSWEExtremes()
	{
		ListIterator<Shore> si = shores.listIterator();
		Shore s = si.next();
		capeNorth = s.getNorthEnd();
		capeSouth = s.getSouthEnd();
		capeWest = s.getWestEnd();
		capeEast = s.getEastEnd();
		
		while(si.hasNext()){
			s = si.next();
			if(s.getNorthEnd().getLatitude() > capeNorth.getLatitude()){capeNorth = s.getNorthEnd();}
			if(s.getSouthEnd().getLatitude() < capeSouth.getLatitude()){capeSouth = s.getSouthEnd();}
			if(s.getWestEnd().getLongitude() < capeWest.getLongitude()){capeWest = s.getWestEnd();}
			if(s.getEastEnd().getLongitude() > capeEast.getLongitude()){capeEast = s.getEastEnd();}
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println(name + " island NSWE extremes are:");
			System.out.println("Cape North: " + capeNorth.toString());
			System.out.println("Cape South: " + capeSouth.toString());
			System.out.println("Cape West: " + capeWest.toString());
			System.out.println("Cape East: " + capeEast.toString());
		}
	}
	
	public boolean overlapsWith(Island i)
	{
		if((this.capeNorth.getLatitude() <= i.capeSouth.getLatitude()) || (this.capeSouth.getLatitude() >= i.capeNorth.getLatitude())){
			return false;
		}
		
		if((this.capeWest.getLongitude() >= i.capeEast.getLongitude()) || (this.capeEast.getLongitude() <= i.capeWest.getLongitude())){
			return false;
		}
		
		if((this.capeWest.getLongitude() < i.capeWest.getLongitude()) && (this.capeEast.getLongitude() > i.capeEast.getLongitude()) && (this.capeSouth.getLatitude() < i.capeSouth.getLatitude()) && (this.capeNorth.getLatitude() > i.capeNorth.getLatitude())){
			if(AppConfiguration.getDebugStatus()){
				System.out.println("stretches outside");
				// "this" island's containing rectangle stretches outside "i"'s in all 4 directions.
				// ... where "containing rectangle" is a rectangle whose sides are meridians and parallels,
				// and it fully covers the island's surface while itself having the minimum possible (minimal) area.
			}
			if(shoreIntersectionWith(i)){
				return true;
			} else{
				// since the two polygons do not intersect, if any point of "i" is outside the polygon of "this", the entire polygon "i" is outside "this".
				if(this.contains(i.getCapeNorth())){ // we need any apex of "i" for this check, Cape North is selected arbitrarily
					return true;
				} else{
					return false;
				}
			}
		}
		
		if((this.capeWest.getLongitude() > i.capeWest.getLongitude()) && (this.capeEast.getLongitude() < i.capeEast.getLongitude()) && (this.capeSouth.getLatitude() > i.capeSouth.getLatitude()) && (this.capeNorth.getLatitude() < i.capeNorth.getLatitude())){
			if(AppConfiguration.getDebugStatus()){
				System.out.println("contained");
				// "this"'s box is inside "i"'s box
			}
			if(shoreIntersectionWith(i)){
				return true;
			} else{
				if(i.contains(this.getCapeNorth())){
					return true;
				} else{
					return false;
				}
			}
		}
		return shoreIntersectionWith(i);
	}
	
	private boolean shoreIntersectionWith(Island i)
	{
		for(Shore s : this.shores){
			for(Shore si : i.shores){
				if(s.getIntersectionWith(si).intersecting()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean contains(Coordinates apex) // point-in-polygon algorithm, zero-slope, a.k.a flat (ψ = latitude of apex) ray-casting implementation. Stubborn, deterministic approach. Somewhat inefficient.
	{
		// Count of intersections between the polygon's sides, and the flat (zero-slope) linear segment that connects the apex (see method params) to the east boundary (arbitrary choice) of the polygon's containing rectangle.
		// If even, the point is outside the polygon. If odd, it is contained in it.
		
		// If one of those intersections happens to be an apex of the polygon, further investigation is required, as we should not count an intersection if the ray is a "tangent" to the polygon's line.
		// In such situations, we only count intersections where the ray is "secant" to the polygon's line.
		// Trying to rephrase the last two sentences with more suited to the context description, the word "tangent" should absolutely be substituted with a correct term, as a tangent to a polygon's apex cannot be defined.
		// Rather, instead of "tangent" we can describe the ray as intersecting the apex in a manner that the other endpoints (not the coinciding ones that belong to the apex and simultaneously to the ray intersection)
		// of the polygon's involved sides, are situated on the same semi-plane of the two that the ray divides the cartesian plane to.
		// Similarly "secant" corresponds to an analogous situation where those "other endpoints" would be each on a different semi-plane.
		
		// The above special case has a special case of its own. It occurs when the ray intersects one or more, albeit adjacent to each other (sequential), same-slope-to-ray polygon sides.
		// In our approach those would be flat (zero-slope).
		// In other words, special care is needed when the ray "overlaps" with a flat side, or a string of them, in succession.
		// The resolution would be to consider that "flat patch" (i.e one or more flat sides one after another) as simply one polygon apex, as if facing the "simpler" special case (see above).
		// We just need to find the non-flat sides encasing the flat side(s), and compare the latitudes of their "other endpoints", i.e. those above or below the ray, as done before.
		
		// Before going any further, the reader must have a good understanding of the way LinearSegment.getIntersectionWith(LinearSegment) method works, as well as its return type LinearSegment.IntersectionStatus.
		// What is important for the present method, is keeping in mind that the ray does not ever get an intersection point (x,y) with a flat side, even if they are on the same carrier line, and LinearSegment.IntersectionStatus.intersecting() returns true.
		// The two hits registered when a ray overlaps with a "flat patch" are actually the endpoints of its enclosing non-flat sides.
		// When processing such an intersection, special care must be taken in order to somehow flag the other endpoint of the "flat patch" in question, so it does not get count twice (if indeed "secant").
		// Remember: all of the "patch" is regarded as just an apex.
		
		
		// Constructing the ray in the form of a LinearSegment parallel to the equator. Endpoints:
		// a: the parameter "apex"
		// b: the point with latitude equal to that of "apex" and longitude equal to surrounding island's Cape East.
		// To be precise, "b" endpoint is moved just a bit further to the east, to ensure segment's intersection with the surrounding island's shores.
		LinearSegment ray = new LinearSegment(apex, new Coordinates((this.getCapeEast().getLongitude() + 10), apex.getLatitude()));
		
		// the collection of ray's hits
		HashMap<Coordinates, LinkedList<Shore>> hits = new HashMap<Coordinates, LinkedList<Shore>>();
		
		int count = 0;
		int remainder = 0;
		
		LinkedList<Shore> shores = this.getShores();
		
		// get intersection points of ray with polygon without any duplicates
		// Things to remember: 
		// usually most sides do not intersect with the ray (which is flat btw),
		// the flat-with-flat intersection returns true in intersecting() but a null intersection point,
		// the intersection of ray with a polygon apex registers twice (each for the respective side, if they are both non-flat)
		for(Shore s : shores){
			LinearSegment.IntersectionStatus lin = s.getIntersectionWith(ray);
			if(lin.intersecting()){
				Coordinates intersection = lin.getIntersection();
				if(intersection != null){
					if(!(hits.containsKey(intersection))){
						hits.put(intersection, new LinkedList<Shore>());
					}
				}
			}
		}
		
		// go through all the sides of the polygon, and check wether their endpoints happen to be a ray intersection point.
		// if yes, map those sides to the intersection point (which is also a polygon apex)
		for(Shore s : shores){
			Coordinates aApex = s.getPointA();
			if(hits.containsKey(aApex)){
				hits.get(aApex).add(s);
			}
			Coordinates bApex = s.getPointB();
			if(hits.containsKey(bApex)){
				hits.get(bApex).add(s);
			}
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println("PNPOLY HIT COUNT: " + hits.size());
			System.out.println("count :::: " + count);
			System.out.println(hits);
			for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
				Coordinates inte = entry.getKey();
				double lo = inte.getLongitude();
				double la = inte.getLatitude();
				LinkedList<Shore> shorez = entry.getValue();
				System.out.println(lo + " " + la + ":: " + shorez.size());
			}
		}
		
		// now all intersections are mapped to either zero or exactly two sides in the "hits" HashMap
		// there are three different scenarios, as described earlier:
		
		// 1---------------------------------------------------------------------------------------
		// those with zero sides are ray hits that cut a (non-flat obviously) side mid-length, and should be considered valid secants.
		// we increment the counter by one for each such mapping, and then we remove the entry altogether from the map,
		// so we may focus on the intersections mapped to two sides, as these entries require further investigation.
		LinkedList<Coordinates> plainSecantsMarkedForRemoval = new LinkedList<Coordinates>();
		for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
			Coordinates key = entry.getKey();
			LinkedList<Shore> sho = entry.getValue();
			if(sho.size() == 0){
				count++;
				plainSecantsMarkedForRemoval.add(key);
			}
		}
		for(Coordinates key : plainSecantsMarkedForRemoval){
			hits.remove(key);
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println("PNPOLY HIT COUNT: " + hits.size());
			System.out.println("count :::: " + count);
			System.out.println(hits);
			for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
				Coordinates inte = entry.getKey();
				double lo = inte.getLongitude();
				double la = inte.getLatitude();
				LinkedList<Shore> shorez = entry.getValue();
				System.out.println(lo + " " + la + ":: " + shorez.size());
			}
		}
		
		// 2---------------------------------------------------------------------------------------
		// the "simple special case", ray intersects a polygon apex being the common endpoint of two non-flat sides
		LinkedList<Coordinates> slopedPairMarkedForRemoval = new LinkedList<Coordinates>();
		for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
			Coordinates key = entry.getKey();
			LinkedList<Shore> shorePair = entry.getValue();
			int nonFlatCount = 0;
			for(Shore sho : shorePair){
				if(!(sho.isFlat())){
					nonFlatCount++;
				}
			}
			if(nonFlatCount == 2){
				double d_maxY_0 = shorePair.get(0).getNorthEnd().getLatitude();
				double d_maxY_1 = shorePair.get(1).getNorthEnd().getLatitude();
				double d_minY_0 = shorePair.get(0).getSouthEnd().getLatitude();
				double d_minY_1 = shorePair.get(1).getSouthEnd().getLatitude();
				
				double d_maxY = d_maxY_0 > d_maxY_1 ? d_maxY_0 : d_maxY_1;
				double d_minY = d_minY_0 < d_minY_1 ? d_minY_0 : d_minY_1;
				
				// we subtract a vertical offset equal to the ray's latitude from both the extreme latitudes of the shore pair.
				// after that, if they are both positive or both negative, the ray is a "tangent" to their common point.
				// else, if they have a different sign, we can regard the ray as being a secant to the "bent" line formed by these 2 shores.
				// we count only secants and in any case, we remove the hits from the collection, since they have been processed.
				d_maxY -= apex.getLatitude(); 
				d_minY -= apex.getLatitude();
				
				if((d_maxY * d_minY) < 0){ // sign discordance
					count++;
				}
				slopedPairMarkedForRemoval.add(key);
			}
		}
		for(Coordinates key : slopedPairMarkedForRemoval){
			hits.remove(key);
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println("PNPOLY HIT COUNT: " + hits.size());
			System.out.println("count :::: " + count);
			System.out.println(hits);
			for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
				Coordinates inte = entry.getKey();
				double lo = inte.getLongitude();
				double la = inte.getLatitude();
				LinkedList<Shore> shorez = entry.getValue();
				System.out.println(lo + " " + la + ":: " + shorez.size());
			}
		}
		
		// 3---------------------------------------------------------------------------------------
		// the "complex special case", ray overlaps with a "flat patch" of the a polygon.
		int step = 0;
		for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
			LinkedList<Shore> ll = entry.getValue();
			Coordinates key = entry.getKey();
			if(ll.size() == 0){ // "if flagged" (see below)
				continue;
			}
			Shore flat = ll.get(0); // or "...might not have been initialized."
			Shore sloped = ll.get(1); // or "...might not have been initialized."
			for(Shore s : ll){
				if(s.isFlat()){
					flat = s;
				}
				if(!(s.isFlat())){
					sloped = s;
				}
			}
			
			//if next is slopped, go the other way
			int indexOfFlat = shores.indexOf(flat);
			int indexOfSloped = shores.indexOf(sloped);
			int direction = indexOfFlat - indexOfSloped;
			if ((direction == 1) || (direction == ((shores.size() - 1) * (-1)))){ // go forward
				step = 1;
			} else{
				step = -1;
			}
			
			int index = indexOfFlat;
			Shore next;
			do{
				index += step;
				//if it reaches either end of List, start over
				if(index >= shores.size()) index = 0;
				if(index < 0) index = (shores.size() - 1);
				next = shores.get(index);
			} while(next.isFlat()); // this could theoretically get stuck in an inf loop, practically impossible due to all the restrictions / checks during island creation
			ll.add(next); // this shore is needed for the "other endpoints" latitude comparison.
			
			//get the entry next belongs to and flag it
			Coordinates pA = next.getPointA();
			Coordinates pB = next.getPointB();
			
			Coordinates otherKey = pA;
			if (pA.getLatitude() != apex.getLatitude()) otherKey = pB;
			
			hits.get(otherKey).clear();
			// as previously explained, when an endpoint of a flat patch is processed, the other end needs to be get flagged. A way to do that would be to empty its mapped shores, since it must be ignored anyway.
		}
		
		for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
			LinkedList<Shore> shoreTriple = entry.getValue();
			Coordinates key = entry.getKey();
			if(shoreTriple.size() == 3){
				
				double[] t_maxY = new double[3];
				double[] t_minY = new double[3];
				t_maxY[0] = shoreTriple.get(0).getNorthEnd().getLatitude();
				t_maxY[1] = shoreTriple.get(1).getNorthEnd().getLatitude();
				t_maxY[2] = shoreTriple.get(2).getNorthEnd().getLatitude();
				t_minY[0] = shoreTriple.get(0).getSouthEnd().getLatitude();
				t_minY[1] = shoreTriple.get(1).getSouthEnd().getLatitude();
				t_minY[2] = shoreTriple.get(2).getSouthEnd().getLatitude();
				
				double tmaxY = t_maxY[0];
				double tminY = t_minY[0];
				for(int i = 1; i < 3; i++){
					if(t_maxY[i] > tmaxY){
						tmaxY = t_maxY[i];
					}
					if(t_minY[i] < tminY){
						tminY = t_minY[i];
					}
				}
				
				tmaxY -= apex.getLatitude(); 
				tminY -= apex.getLatitude();
				
				if((tmaxY * tminY) < 0){ // sign discordance
					count++;
				}
			}
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println("PNPOLY HIT COUNT: " + hits.size());
			System.out.println("count :::: " + count);
			System.out.println(hits);
			for(Map.Entry<Coordinates, LinkedList<Shore>> entry : hits.entrySet()){
				Coordinates inte = entry.getKey();
				double lo = inte.getLongitude();
				double la = inte.getLatitude();
				LinkedList<Shore> shorez = entry.getValue();
				System.out.println(lo + " " + la + ":: " + shorez.size());
			}
		}
		
		remainder = (count % 2);
		if(remainder == 0){
			return false;
		} else{
			return true;
		}
	}
}