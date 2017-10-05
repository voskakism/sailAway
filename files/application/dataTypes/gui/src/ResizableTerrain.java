import java.awt.Color;
import java.util.Map;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.geom.AffineTransform;
import java.util.HashMap;

class ResizableTerrain extends Terrain implements ActionListener
{
	private Sea sea;
	private Timer t;
	private boolean noIslandsMode_firstBeep;
	
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;
	
	private double seaMinX;
	private double seaMinY;
	private double seaMaxX;
	private double seaMaxY;
	
	{
		t = new Timer(AppConfiguration.getBroadcastInterval(), this);
		ships = new HashMap<Long, Ship>();
		setBackground(seaColor);
		noIslandsMode_firstBeep = false;
	}
	
	public ResizableTerrain(int width, int height, Sea sea)
	{
		this.sea = sea;
		
		int viewportWidth = width - leftBezel - rightBezel;
		int viewportHeight = height - topBezel - bottomBezel;
		int canvasWidth = viewportWidth - 2 * margin;
		int canvasHeight = viewportHeight - 2 * margin;
		
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		
		if(sea.islandCount() > 0){
			seaMinX = sea.getWestEnd().getLongitude();
			seaMaxX = sea.getEastEnd().getLongitude();
			seaMinY = sea.getSouthEnd().getLatitude();
			seaMaxY = sea.getNorthEnd().getLatitude();
		} else{ // a sea without islands also means "trails mode" regardless of user settings, see "Application.java".
			seaMinX = 0;
			seaMaxX = 0;
			seaMinY = 0;
			seaMaxY = 0;
			noIslandsMode_firstBeep = true;
		}
		resetTheatreExtremes();
		calculateScaleOriginOffset();
		t.start();
	}
	
	private void resetTheatreExtremes()
	{
		minX = seaMinX;
		maxX = seaMaxX;
		minY = seaMinY;
		maxY = seaMaxY;
	}
	
	private void calculateScaleOriginOffset()
	{
		double scale;
		int clipWidth;
		int clipHeight;
		int leftPadding;
		int topPadding;
		double canvasRatio = (double)canvasWidth / (double)canvasHeight;
		double theatreWidth = maxX - minX;
		double theatreHeight = maxY - minY;
		double theatreRatio = theatreWidth / theatreHeight;
		if(canvasRatio >= theatreRatio){
			scale = (double)canvasHeight / theatreHeight;
			clipWidth = (int)(Math.round(theatreWidth * scale));
			clipHeight = canvasHeight; // (or "clipHeight = theatreHeight * scale", rounded to closest int)
			leftPadding = (canvasWidth - clipWidth) / 2;
			topPadding = 0;
			seaRatioLessThanScreenRatio = true; // debug
		} else{
			scale = (double)canvasWidth / theatreWidth;
			clipHeight = (int)(Math.round(theatreHeight * scale));
			clipWidth = canvasWidth; // (or "clipWidth = theatreWidth * scale", rounded to closest int)
			topPadding = (canvasHeight - clipHeight) / 2;
			leftPadding = 0;
			seaRatioLessThanScreenRatio = false; // debug
		}
		this.scale = scale;
		this.topPadding = topPadding;
		this.leftPadding = leftPadding;
		this.clipHeight = clipHeight;
		this.clipWidth = clipWidth;
		this.southWestCorner = new Coordinates(minX, minY);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		FontMetrics fm = g2.getFontMetrics();
		
		for(Island i : sea.getIslands()){
			String title = i.getName();
			int count = i.getShores().size();
			int round = 0;
			int[] x = new int[count];
			int[] y = new int[count];
			for(Shore s : i.getShores()){
				Coordinates apex = s.getPointA();
				IntPair pair = cartesianToMonitor(apex);
				x[round] = pair.getX();
				y[round]= pair.getY();
				round++;
			}
			g2.setColor(landColor);
			g2.fillPolygon(x, y, count);
			g2.setColor(Color.GREEN);
			
			IntPair capeNorth = cartesianToMonitor(i.getCapeNorth());
			IntPair capeSouth = cartesianToMonitor(i.getCapeSouth());
			IntPair capeWest = cartesianToMonitor(i.getCapeWest());
			IntPair capeEast = cartesianToMonitor(i.getCapeEast());
			
			IntPair titlePosition = new IntPair((capeEast.getX() + capeWest.getX()) / 2, (capeNorth.getY() + capeSouth.getY()) / 2);
			
			Rectangle2D rect = fm.getStringBounds(title, g2);
			int titleWidth = (int)(rect.getWidth());
			int titleHeight = (int)(rect.getHeight());
			g2.drawString(title, titlePosition.getX() - titleWidth / 2, titlePosition.getY() + titleHeight / 2 - 3);
		}
		
		// debug
		if(AppConfiguration.getDebugStatus()){
			g2.setColor(Color.BLACK);
			g2.drawRect(0, 0, margin, margin);
			g2.drawRect(margin, margin, canvasWidth, canvasHeight);
			g2.drawRect(margin + canvasWidth, margin + canvasHeight, margin, margin);
			if(seaRatioLessThanScreenRatio){
				g2.drawRect(margin, margin, leftPadding, clipHeight);
				g2.drawRect(margin + canvasWidth - leftPadding, margin, leftPadding, clipHeight);
				System.out.println("Theatre's containing rectangle's side ratio is LESS than canvas' side ratio, a.k.a vertical");
			} else{
				g2.drawRect(margin, margin, clipWidth, topPadding);
				g2.drawRect(margin, margin + canvasHeight - topPadding, clipWidth, topPadding);
				System.out.println("Theatre's containing rectangle's side ratio is MORE than canvas' side ratio, a.k.a horizontal");
			}
		}
		
		// paint Ships with their trails
		double angle;
		IntPair startPoint;
		IntPair endPoint;
		AffineTransform at;
		for(Map.Entry<Long, Ship> entry : ships.entrySet()){
			Ship s = entry.getValue();
			g2.setColor(s.getColor());
			LinkedList<Coordinates> trail = s.getTrail();
			Iterator<Coordinates> di = trail.descendingIterator(); // REVERSE iterator: The first element to be gotten here, is the Ship's current (latest) position
			double[] shipIcon = new double[6];
			startPoint = null;
			endPoint = null;
			boolean firstRun = true;
			int x1, y1, x2, y2;
			while(di.hasNext()){
				endPoint = startPoint;
				startPoint = cartesianToMonitor(di.next());
				if(firstRun){
					endPoint = startPoint;
					firstRun = false;
					if(s.getSpeed() <= AppConfiguration.getImmobileSpeedThreshold()){
						g2.fillOval(endPoint.getX() - 5, endPoint.getY() - 5, 9, 9);
					} else{
						angle = s.getHeading();
						at = new AffineTransform();
						at.translate(endPoint.getX(), endPoint.getY());
						at.rotate(Math.toRadians(angle), 0, 0); // and then rotating around the axis' intersection i.e. monitor coordinates (0,0) at the given angle
						at.translate(-3, -8); // rotate the triangle around its centroid by translating the triangle so that its centroid coincides with the pixel axis' start,
						at.transform(shipTriangle, 0, shipIcon, 0, 3);
					
						int[] xPoints = new int[]{(int)shipIcon[0], (int)shipIcon[2], (int)shipIcon[4]};
						int[] yPoints = new int[]{(int)shipIcon[1], (int)shipIcon[3], (int)shipIcon[5]};
				
						g2.fillPolygon(xPoints, yPoints, 3);
					}
				} else{
					x1 = startPoint.getX();
					y1 = startPoint.getY();
					x2 = endPoint.getX();
					y2 = endPoint.getY();
					g2.drawLine(x1, y1, x2, y2);
				}
			}
		}
	}
	
	private void updateTheatreExtremes(AISBroadcast ais)
	{
		if(AppConfiguration.getGuiMode() == GuiMode.RESIZABLE_TO_TRAILS){
			Coordinates c = ais.getPosition();
			double x = c.getLongitude();
			double y = c.getLatitude();
			if(noIslandsMode_firstBeep){		 // This block will run ONLY when there are no Islands created. No Islands also means RESIZABLE_TO_TRAILS mode, regardless of user setting.
				minX = x;						 // Also, this code block runs only once, at the very first Beep ever received by this ResizableTerrain. It does not matter which Vessel that Beep comes from.
				maxX = x;						 // The block sets the theatre extemes to that Beep's position, which is meant to be included to the theatre anyway. This could not be done any earlier (see constructor)
				minY = y;						 // as no beeps were submitted at that time, and there are no Islands for spatial reference. Having to set theatre exteme values for the initial calculation (see constructor),
				maxY = y;						 // we arbitrarily set those to the origin (0,0). However, there is no guarantee, that the origin will be contained in the trails' rectangle. Purely for better visuals,
				noIslandsMode_firstBeep = false; // this block runs at the first Beep received, discarding the origin and replacing it with the Beep's position, which will be contained anyway.
			}
			boolean changed = false;
			if(x < minX){
				minX = x;
				changed = true;
			}
			if(x > maxX){
				maxX = x;
				changed = true;
			}
			if(y < minY){
				minY = y;
				changed = true;
			}
			if(y > maxY){
				maxY = y;
				changed = true;
			}
			if(changed) calculateScaleOriginOffset();
		} else{
			resetTheatreExtremes();
			for(Map.Entry<Long, Ship> entry : ships.entrySet()){
				Ship s = entry.getValue();
				Coordinates c = (Coordinates)s.getTrail().peekLast();
				double x = c.getLongitude();
				double y = c.getLatitude();
				if(x < minX) minX = x;
				if(x > maxX) maxX = x;
				if(y < minY) minY = y;
				if(y > maxY) maxY = y;
			}
			calculateScaleOriginOffset();
		}
	}
	
	public void beep(AISBroadcast ais)
	{
		// 1: Dissect AISBroadcast to the more compact local data type Beep
		Vessel v = ais.getVessel();
		Long mmsi = new Long(v.getMmsi());
		String name = v.getName();
		Coordinates pos = ais.getPosition();
		Coordinates position = new Coordinates(pos.getLongitude(), pos.getLatitude()); // The position object is disected and remade here as a new object. Otherwise, in case the Lnav Threads simply update their position object instead of spawning a new one with every AIS broadcast, the location history (trail) entries will all be references to that same Coordinates object, and all contain the ship's latest position.
		Azimuth h = ais.getHeading();
		double heading = h.getAzimuth();
		double speed = ais.getSpeed();
		
		Beep beep = new Beep<Coordinates>(mmsi, name, position, heading, speed);
		
		// 2: Identify the beeping ship via MMSI. If not mapped in ships, create the entry and add the beep's info. If mapped, simply update its trail and hdg.
		if(ships.containsKey(mmsi)){
			Ship s = ships.get(mmsi);
			s.update(beep, Coordinates.class);
		} else{
			Ship s = new Ship(beep.getName());
			s.update(beep, Coordinates.class);
			ships.put(mmsi, s);
		}
		
		// 3: Useful in case a ship broadcasts from a position outside the archipelago's containing rectangle
		updateTheatreExtremes(ais);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		repaint();
	}
}