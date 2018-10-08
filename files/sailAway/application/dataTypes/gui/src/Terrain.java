import java.awt.Color;
import javax.swing.JPanel;
import java.util.LinkedList;
import java.util.Random;
import java.util.HashMap;

abstract class Terrain extends JPanel
{
	protected abstract void beep (AISBroadcast ais);
	
	// Window bezels' thickness in pixels, also an added margin where islands are not drawn
	protected static final int leftBezel;
	protected static final int rightBezel;
	protected static final int topBezel;
	protected static final int bottomBezel;
	protected static final int margin;		// margin around the islands' containing rectangle
	
	// The ship icons appearing on screen
	protected static final double[] shipTriangle;
	
	// Colours for sea and land
	protected static final Color seaColor;
	protected static final Color landColor;
	
	static
	{
		// Bezels' thickness for JFrame on Windows 7
		/*leftBezel = 3;
		rightBezel = 3;
		topBezel = 26;
		bottomBezel = 3;*/
		
		// Bezels' thickness for - maximized - JFrame on Windows 10
		leftBezel = 0;
		rightBezel = 0;
		topBezel = 23;
		bottomBezel = 0;
		
		margin = 40;
		shipTriangle = new double[]{0, 13, 3, 0, 6, 13}; // pixel coordinates {x1, y1, x2, y2, ..., xN, yN} // pixel (3,9) is the centroid of this isosceles triangle.
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
	
	protected HashMap<Long, Ship> ships; // HashMap<> is NOT thread-safe, but thread safety is not required in this scenario, as each trip writes only to its dedicated MapEntry.
	
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
		//Step 4: Flip y-axis
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

/*
There is also another approach to the implementation of graphical output, which would grant
the user further abilities, the trade-off being a slightly increased strain on system resourses.

The modification:

A) In Application.main():
	1) the size of the JFrame would be set to approximately the two thirds of the screen resolution:
		int width = (int)screenSize.getWidth();
		int height = (int)screenSize.getHeight();
		width = width * 2 / 3;
		height = height * 2 / 3;
		f.setSize(width, height);
	2) the frame would be resizable:
		f.setResizable(true);
	So far after the above adjustments, the Window starts maximized, however not overlapping the taskbar.
	If the maximize button is hit, the window shrinks to a reasonable size and it can be resized at will.
	3) the JPanels would be sent only a Sea argument, not the screen resolution:
		map = new FixedTerrain(sea); or,
		map = new ResizableTerrain(sea);
	
B) In the classes of the JPanel extenders (FixedTerrain/ResizableTerrain):
	The Jpanels do not need the screen resolution to be passed as arguments from Application.main().
	Their constructors are then modified accordingly while they should be able to deduce their own
	current size via a call to the inheritted java.awt.Component.getSize(), at any time.
	That method returns a java.awt.Dimension object, having the
		int width, int height
	fields, both publicly accessible.

These measurements correspond to the "viewport" size currently in code, eliminating the need for the
subtraction of bezel thickness from the monitor's resolution.
Furthermore, the need for separate bezel thickness profiles (one per OS) is also eliminated,
uniform fit of the maximized window to the screen, regardless of the host machine, now granted.

More importantly, depending on wether the viewport is calculated only at the constructor of each JPanel,
or as a part of an update procedure, the graphical output can become responsive to the window being resized
(and not only to the ships sailing out of the boundaries of the archipelago, as in the case of ResizableTerrain).

To clarify the meaning of the misleading term "responsive" (popularized by webpage designers):
Designating the frame as such, it is implied that its contents are automatically scaled to fit inside it
when the frame is resized, rather than being cropped, hidden / partially drawn.

The update procedure mentioned, is - ideally - the Jpanel sending events to an attached
java.awt.event.ComponentListener, which would then handle them in its void componentResized(ComponentEvent e) method.
See: https://docs.oracle.com/javase/tutorial/uiswing/events/componentlistener.html
*/