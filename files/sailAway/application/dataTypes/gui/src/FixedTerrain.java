import java.awt.Color;
import java.util.Map;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.Iterator;
import java.awt.geom.AffineTransform;
import java.util.HashMap;

class FixedTerrain extends Terrain
{
	private LinkedList<Isle> landmass;
	
	{
		landmass = new LinkedList<Isle>();
		ships = new HashMap<Long, Ship>();
		setBackground(seaColor);
	}
	
	public FixedTerrain(int width, int height, Sea sea)
	{
		double scale;
		int clipWidth;
		int clipHeight;
		int leftPadding;
		int topPadding;
		int viewportWidth = width - leftBezel - rightBezel;
		int viewportHeight = height - topBezel - bottomBezel;
		int canvasWidth = viewportWidth - 2 * margin;
		int canvasHeight = viewportHeight - 2 * margin;
		double canvasRatio = (double)canvasWidth / (double)canvasHeight;
		double archipelagoWidth = sea.getEastEnd().getLongitude() - sea.getWestEnd().getLongitude();
		double archipelagoHeight = sea.getNorthEnd().getLatitude() - sea.getSouthEnd().getLatitude();
		double archipelagoRatio = archipelagoWidth / archipelagoHeight;
		if(canvasRatio >= archipelagoRatio){
			scale = (double)canvasHeight / archipelagoHeight;
			clipWidth = (int)(Math.round(archipelagoWidth * scale));
			clipHeight = canvasHeight; // (or "clipHeight = archipelagoHeight * scale", rounded to closest int)
			leftPadding = (canvasWidth - clipWidth) / 2;
			topPadding = 0;
			seaRatioLessThanScreenRatio = true; // debug
		} else{
			scale = (double)canvasWidth / archipelagoWidth;
			clipHeight = (int)(Math.round(archipelagoHeight * scale));
			clipWidth = canvasWidth; // (or "clipWidth = archipelagoWidth * scale", rounded to closest int)
			topPadding = (canvasHeight - clipHeight) / 2;
			leftPadding = 0;
			seaRatioLessThanScreenRatio = false; // debug
		}
		
		this.scale = scale;
		this.topPadding = topPadding;
		this.leftPadding = leftPadding;
		this.clipHeight = clipHeight;
		this.clipWidth = clipWidth;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.southWestCorner = new Coordinates(sea.getWestEnd().getLongitude(), sea.getSouthEnd().getLatitude());
		
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
			landmass.add(new Isle(title, x, y));
		}
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		FontMetrics fm = g2.getFontMetrics();
		for(Isle i : landmass){
			g2.setColor(landColor);
			g2.fillPolygon(i.getLons(), i.getLats(), i.getLons().length);
			g2.setColor(Color.GREEN);
			IntPair p = i.getTitlePosition();
			String t = i.getTitle();
			Rectangle2D rect = fm.getStringBounds(t, g2);
			int titleWidth = (int)(rect.getWidth());
			int titleHeight = (int)(rect.getHeight());
			g2.drawString(t, p.getX() - titleWidth / 2, p.getY() + titleHeight / 2 - 3);
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
				System.out.println("Archipelago's containing rectangle's side ratio is LESS than canvas' side ratio.");
			} else{
				g2.drawRect(margin, margin, clipWidth, topPadding);
				g2.drawRect(margin, margin + canvasHeight - topPadding, clipWidth, topPadding);
				System.out.println("Archipelago's containing rectangle's side ratio is GREATER than canvas' side ratio.");
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
			LinkedList<IntPair> trail = s.getTrail();
			Iterator<IntPair> di = trail.descendingIterator(); // REVERSE iterator: The first element to be gotten here, is the Ship's current (latest) position
			double[] shipIcon = new double[6];
			startPoint = null;
			endPoint = null;
			boolean firstRun = true;
			int x1, y1, x2, y2;
			while(di.hasNext()){
				endPoint = startPoint;
				startPoint = di.next();
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
						at.translate(-3, -9); // rotate the triangle around its centroid by translating the triangle so that its centroid coincides with the pixel axis' start,
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
	
	public void beep(AISBroadcast ais)
	{
		// 1: Dissect AISBroadcast to the more compact local data type Beep
		Vessel v = ais.getVessel();
		Long mmsi = new Long(v.getMmsi());
		String name = v.getName();
		Coordinates p = ais.getPosition();
		IntPair position = cartesianToMonitor(p);
		Azimuth h = ais.getHeading();
		double heading = h.getAzimuth();
		double speed = ais.getSpeed();
		
		Beep beep = new Beep<IntPair>(mmsi, name, position, heading, speed);
		
		// 2: Identify the beeping ship via MMSI. If not mapped in ships, create the entry and add the beep's info. If mapped, simply update its trail and hdg.
		if(ships.containsKey(mmsi)){
			Ship s = ships.get(mmsi);
			s.update(beep, IntPair.class);
		} else{
			Ship s = new Ship(beep.getName());
			s.update(beep, IntPair.class);
			ships.put(mmsi, s);
		}
		
		// 3: call repaint to re-draw graphics on screen
		repaint();
	}
	
	//------------------------------  Local datatypes  ------------------------------//
	private class Isle
	{
		private String title;
		private int[] lons;
		private int[] lats;
		private IntPair titlePosition;
		
		Isle(String title, int[] lons, int[] lats)
		{
			this.title = title;
			this.lons = lons;
			this.lats = lats;
			calculateTitlePosition();
		}
		
		String getTitle() {return title;}
		int[] getLons() {return lons;}
		int[] getLats() {return lats;}
		IntPair getTitlePosition() {return titlePosition;}
		
		private void calculateTitlePosition()
		{
			int xmin = lons[0];
			int xmax = lons[0];
			for(int i = 0; i < lons.length; i++){
				if(lons[i] < xmin){
					xmin = lons[i];
				}
				if(lons[i] > xmax){
					xmax = lons[i];
				}
			}
			int ymin = lats[0];
			int ymax = lats[0];
			for(int j = 0; j < lats.length; j++){
				if(lats[j] < ymin){
					ymin = lats[j];
				}
				if(lats[j] > ymax){
					ymax = lats[j];
				}
			}
			int posX = (xmin + xmax) / 2;
			int posY = (ymin + ymax) / 2;
			this.titlePosition = new IntPair(posX, posY);
		}
	}
}