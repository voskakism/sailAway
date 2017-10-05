import java.awt.Color;
import javax.swing.JPanel;

abstract class Terrain extends JPanel
{
	protected abstract void beep (AISBroadcast ais);
	
	//Window bezels' thickness in pixels, also an added margin where islands are not drawn
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