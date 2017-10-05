import java.io.File;
import java.util.LinkedList;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JFrame;

public class App
{
	public static void main(String[] args) throws CSVInputException
	{
		Sea aegean = new Sea("Aegean");
		Island i;
		LinkedList<Island> overlappingIslands;
		File islandDirectory = new File("../../inputFiles/islands/");
		for(File csv : islandDirectory.listFiles()){
			//try{
				i = new CSVIsland(csv).getData(true);
			/*} catch(Exception ex){
				ex.printStackTrace();
			}*/
			if(i != null){
				//if((overlappingIslands = aegean.overlapsWithCurrentIslands(i)) == null){
					aegean.addIsland(i);
					//System.out.println("Created island: " + i.getName() + " and added it to " + aegean.getName() + " sea:");
				/*} else{
					System.out.println("Island " + i.getName() + " overlaps with the following islands in the " + aegean.getName() + " sea:");
					for(Island isle : overlappingIslands){
						System.out.println(isle.getName());
					}
				}*/
			} else{
				System.out.println("Invalid island decribed in file " + csv.getName());
			}
		}
		
		System.out.println("Number of islands created: " + aegean.getIslands().size());//////////////////////////////////
		for(Island is : aegean.getIslands()){////////////////////////////////////////////////////////////////////////////
			System.out.println(is.getName());////////////////////////////////////////////////////////////////////////////
		}////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// GUI
		// DRAW MAP
		
		int MAP_BORDER = 0;
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth() - MAP_BORDER;
		double screenHeight = screenSize.getHeight() - MAP_BORDER;
		
		double mapWidth = Math.ceil(aegean.getWestEnd().getLongtitude() - aegean.getEastEnd().getLongtitude());
		double mapHeight = Math.ceil(aegean.getNorthEnd().getLatitude() - aegean.getSouthEnd().getLatitude());
		
		double screenRatio = screenWidth / screenHeight;
		double mapRatio = mapWidth / mapHeight;
		
		//scale map, keep aspect ratio
		double scale;
		if(screenRatio > mapRatio){
			scale = screenHeight / mapHeight;
			//mapHeight = screenHeight;
			//mapWidth *= scale;
		} else{
			scale = screenWidth / mapWidth;
			//mapWidth = screenWidth;
			//mapHeight *= scale;
		}
		
		int screenWidthPixels = (int)Math.round(screenWidth);
		int screenHeightPixels = (int)Math.round(screenHeight);
		JFrame f = new JFrame("Aegean");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		//f.setSize(1024, 768);
		f.setSize(screenWidthPixels, screenHeightPixels);
		Map p = new Map(aegean, scale);
		f.add(p); //add the JPanel to the JFrame
	}
}

