public class App
{
	public static void main(String[] args)
	{
		/*Azimuth b = Coordinates.calculateBearing(new Coordinates(0, 0), new Coordinates(1, 10));
		System.out.println(b.getAzimuth());*/
		
		/*Coordinates a = new Coordinates(4, 10);
		Coordinates b = new Coordinates(5, 10);*/
		
		/*Coordinates point = new Coordinates(0, 0);
		Azimuth direction = new Azimuth(315);
		point.move(direction, 10);
		System.out.println(point.toString());*/
		
		/*Coordinates a = new Coordinates(2, 2);
		Coordinates b = new Coordinates(4, 4);
		Coordinates point = new Coordinates(-4, 4);
		double distance = Coordinates.distanceOfPointPFromLineAB(point, a, b);
		System.out.println(distance);*/
		Azimuth a;
		double azm;
		//int q = 1;
		//int angleOffset = (q - 1) * 90;
		int angleOffset = 0;
		
		Azimuth bearing = new Azimuth(355 + angleOffset);
		Azimuth course = new Azimuth(5 + angleOffset);
		
		a = bearing.relativeTo(course);
		azm = a.getAzimuth();
		System.out.println(azm);
		
		a = course.relativeTo(bearing);
		azm = a.getAzimuth();
		System.out.println(azm);
	}
}