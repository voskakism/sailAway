import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;

public class TesterProgram
{
	//private static Coordinates pos;

	public static void main(String[] args)
	{
		//pos = new Coordinates(0, 0);
		//pos.move(Coordinates.calculateBearing(pos, new Coordinates(4, -3)), Coordinates.calculateDistance(pos, new Coordinates(4, -3)));
		//printPos();
		//Coordinates destination = new Coordinates(-4, -3);
		//System.out.println("bearing: " + Coordinates.calculateBearing(origin, destination).getAzimuth());
		//System.out.println("distance: " + Coordinates.calculateDistance(origin, destination));
		//pos.move(new Azimuth(120.9635), 5);
		//printPos();
		
		//pos.move(new Azimuth(102.53), 9.22);
		//printPos();
		
		/*System.out.println("Cosine of 90 degrees is: " + Math.cos(Math.toRadians(90)));
		System.out.println("Cosine of 0 degrees is: " + Math.cos(0 * Math.PI / 180));
		System.out.println("Cosine of 180 degrees is: " + Math.cos(Math.PI));*/
		/*Azimuth currentBearing = new Azimuth(350);
		System.out.println("Current Bearing = " + currentBearing.getAzimuth());
		Azimuth nextBearing = new Azimuth(10);//Coordinates.calculateBearing(new Coordinates(0, 0), new Coordinates(0, 2));
		System.out.println("Next Bearing = " + nextBearing.getAzimuth());
		System.out.println(nextBearing.getAzimuth() - currentBearing.getAzimuth());//-340
		double turn = Math.abs(nextBearing.getAzimuth() - currentBearing.getAzimuth());//340
		if(turn > 180){
			turn = 360 - turn;
		}
		System.out.println("Next turn is angled: " + turn);//20*/
		/*Coordinates n0 = new Coordinates(0, 0);
		Coordinates n1 = new Coordinates(10, 10);
		Coordinates n2 = new Coordinates(20, 20);
		Coordinates n3 = new Coordinates(30, 30);
		Coordinates n4 = new Coordinates(40, 40);
		Coordinates n5 = new Coordinates(50, 50);
		List<Coordinates> ll = new LinkedList<Coordinates>();
		ll.add(n0);
		ll.add(n1);
		ll.add(n2);
		ll.add(n3);
		ll.add(n4);
		ll.add(n5);
		ListIterator<Coordinates> li = ll.listIterator();*/
		
		
		//System.out.println(li.nextIndex());
		//printPos(li.next());
		//System.out.println(li.nextIndex());
		//printPos(li.previous());
		//System.out.println(li.nextIndex());
		//printPos(li.previous());
		
		//next() returns the element pointed by the cursor and then advances the cursor
		//previous() recedes the cursor and then returns the element pointed at by the cursor
		
		/*Azimuth heading = new Azimuth(170);
		Azimuth bearing = new Azimuth(351);
		heading.turnTo(bearing, 0.5);
		heading.turnTo(bearing, 0.5);
		System.out.println(heading.getAzimuth());*/
		//Azimuth relative = heading.relativeTo(bearing);
		//System.out.println("Relative bearing of " + heading.getAzimuth() + " to " + bearing.getAzimuth() + " is: " + relative.getAzimuth());
		
		Coordinates c = new Coordinates(10, 11);
		Waypoint w1 = new Waypoint(10, 11);
		//w1.mius();
		
		Waypoint w2 = new Waypoint(c);
		//w2.mius();
	}
	
	private static void printPos(Coordinates pos)
	{
		System.out.print("LON " + pos.getLongtitude());
		System.out.println(" LAT " + pos.getLatitude());		
	}
}