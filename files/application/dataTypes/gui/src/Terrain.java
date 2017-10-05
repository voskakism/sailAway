import java.awt.Color;
import javax.swing.JPanel;
import java.util.LinkedList;
import java.util.Random;
import java.util.HashMap;

abstract class Terrain extends JPanel
{
	protected abstract void beep (AISBroadcast ais);
	
	// Window bezels' thickness in pixels, also an added margin where islands are not drawn
	protected static final int leftBezel;    // total vertical bezels' thickness in pixels
	protected static final int rightBezel;   // total vertical bezels' thickness in pixels
	protected static final int topBezel;     // total horizontal bezels' thickness in pixels
	protected static final int bottomBezel;  // total horizontal bezels' thickness in pixels
	protected static final int margin;       // margin around the islands' containing rectangle, in pixels
	
	// The ship icons appearing on screen
	protected static final double[] shipTriangle;
	
	// Colours for sea and land
	protected static final Color seaColor;
	protected static final Color landColor;
	
	static
	{
		leftBezel = 3;
		rightBezel = 3;
		topBezel = 26;
		bottomBezel = 3;
		margin = 50;
		shipTriangle = new double[]{0, 13, 3, 0, 6, 13}; // pixel coordinates {x1, y1, x2, y2, ..., xN, yN} // pixel (3,8) is the centroid of this isosceles triangle.
		seaColor = new Color(65, 105, 225);
		landColor = new Color(205, 133, 63);
	}
	
	protected double scale;
	protected int topPadding;
	protected int leftPadding;
	protected int clipHeight;
	protected int clipWidth; 
	protected int canvasWidth;
	protected int canvasHeight;
	protected Coordinates southWestCorner;
	protected boolean seaRatioLessThanScreenRatio; // debug
	
	protected HashMap<Long, Ship> ships; // HashMap<> is NOT thread-safe, but thread safety is not required in this scenario, as each Lnav writes only to its dedicated MapEntry.
	
	protected IntPair cartesianToMonitor(Coordinates c) // a utility method that converts geographical coordinates to pixel coordinates, scales and centers
	{
		double lon = c.getLongitude();
		double lat = c.getLatitude();
		// Step 1: Move the southwest corner of the theatre's containing rectangle to origin.
		lon -= southWestCorner.getLongitude();
		lat -= southWestCorner.getLatitude();
		// Step 2: Scale to fit to monitor
		lon = lon * scale;
		lat = lat * scale;
		//Step 3: Round to closest integer
		int x = (int)(Math.round(lon));
		int y = (int)(Math.round(lat));
		//Step 4: Flip ψ-axis
		y = clipHeight - y;
		//Step 5: Offset to center to monitor
		x +=  margin + leftPadding;
		y +=  margin + topPadding;
		return new IntPair(x, y);
	}
	
	//------------------------------  Local datatypes  ------------------------------//
	protected class Ship<T>
	{
		private String name;
		private LinkedList<T> trail;
		private double heading;
		private double speed;
		private Color color;
		private Random r;
		
		Ship(String name)
		{
			r = new Random();
			this.name = name;
			this.trail = new LinkedList<T>();
			this.color = new Color(16 + r.nextInt(112), 16 + r.nextInt(96), 16 + r.nextInt(80)); // The sea is light-blue, so we need dark colours, but maybe not too dark. Also, we should avoid overly blue-ish / green-ish colours for ship icons, for the sake of contrast.
		}
		
		String getName() {return name;}
		LinkedList<T> getTrail() {return trail;}
		double getHeading() {return heading;}
		double getSpeed() {return speed;}
		Color getColor() {return color;}
		
		void update(Beep b, Class<T> type)
		{
			if (trail.size() >= AppConfiguration.getTrailLength()) trail.poll();
			this.trail.add(type.cast(b.getPosition()));
			this.heading = b.getHeading();
			this.speed = b.getSpeed();
		}
	}
	
	protected class Beep<T>
	{
		private Long mmsi; 
		private String name;
		private T position;
		private double heading;
		private double speed;
		
		Beep(Long mmsi, String name, T position, double heading, double speed)
		{
			this.mmsi = mmsi;
			this.name = name;
			this.position = position;
			this.heading = heading;
			this.speed = speed;
		}
		
		Long getMmsi() {return mmsi;}
		String getName() {return name;}
		T getPosition() {return position;}
		double getHeading() {return heading;}
		double getSpeed() {return speed;}
	}
	
	protected class IntPair
	{
		private int x;
		private int y;
		
		IntPair(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		int getX() {return x;}
		int getY() {return y;}
	}
}