import java.util.LinkedList;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

class Map extends JPanel
{
	private Sea archipelago;
	private double scale;
	//private LinkedList<Polygon> polygons;
	
	public Map(Sea archipelago, double scale)
	{
		this.archipelago = archipelago;
		this.scale = scale;
		//setLayout(new GridBagLayout());
		//setBorder(new TitledBorder(archipelago.getName()));
	}
	
	public void paintComponent(Graphics g)
	{
		Color seaColor = new Color(65, 105, 225);
		Color landColor = new Color(205, 133, 63);
		
		super.paintComponent(g); //see http://stackoverflow.com/questions/28724609/what-does-super-paintcomponentg-do
		this.setBackground(seaColor);
		
		g.setColor(landColor);
		
		for(Island i : archipelago.getIslands()){
			Polygon p = new Polygon();
			for(Shore s : i.getShores()){
				//Coordinates xy = convertToMonitorCoordinates(s.getPointA());
				Coordinates xy = s.getPointA();////////////////////////////////////////////////////////////////
				int x = (int)(Math.round(xy.getLongtitude() * 10 /*scale*/));
				int y = (int)(Math.round(xy.getLatitude() * 10 /*scale*/));
				//int x = (int)xy.getLongtitude();////////////////////////////////////////////////////////////
				//int y = (int)xy.getLatitude();//////////////////////////////////////////////////////////////
				p.addPoint(x, y);
			}
			g.fillPolygon(p);
			
			//also add label//////////////////////////////////////////////////
			Rectangle rec = p.getBounds();
			Point location = rec.getLocation();
			double x = location.getX();
			double y = location.getY();
			double w = rec.getWidth();
			double h = rec.getHeight();
			//JLabel label = new JLabel(i.getName());
			int textX = (int)Math.round(x + w/2);
			int textY = (int)Math.round(y + h/2);
			g.setColor(Color.BLACK);
			g.drawString(i.getName(), textX, textY);
			g.setColor(landColor);
		}
		
		/*Polygon p = new Polygon();
		p.addPoint(40, 40);
		p.addPoint(40, 440);
		p.addPoint(440, 440);
		p.addPoint(440, 40);
		g.fillPolygon(p);*/
	}
	
	private Coordinates convertToMonitorCoordinates(Coordinates c)
	{
		double height = 1080;//Math.ceil(archipelago.getNorthEnd().getLatitude() - archipelago.getSouthEnd().getLatitude());
		return new Coordinates(c.getLongtitude(), (height - c.getLatitude()));
	}
}

//todo: scale, captions, shipBlip