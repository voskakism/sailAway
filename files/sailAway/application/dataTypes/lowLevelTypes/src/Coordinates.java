class Coordinates
{
	private double longitude;
	private double latitude;
	
	Coordinates(){}
	
	Coordinates(Coordinates point) // copy constructor: create a new object mimicking current state of an existing one.
	{
		this.longitude = point.getLongitude();
		this.latitude = point.getLatitude();
	}
	
	Coordinates(double longitude, double latitude)
	{
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public void set(double longitude, double latitude)
	{
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}
	
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}
	
	public double getLongitude()
	{
		return longitude;
	}
	
	public double getLatitude()
	{
		return latitude;
	}
	
	public void moveNorth(double distance)
	{
		latitude += distance;
	}
	
	public void moveSouth(double distance)
	{
		latitude -= distance;
	}
	
	public void moveEast(double distance)
	{
		longitude += distance;
	}
	
	public void moveWest(double distance)
	{
		longitude -= distance;
	}
	
	@Override
	public String toString()
	{
		return ("LON: " + this.longitude + " LAT: " + this.latitude);
	}
	
	public void move(Azimuth heading, double distance)
	{
		double horizontalComponent;
		double verticalComponent;
		if((heading.getAzimuth() == 0.0) || (heading.getAzimuth() == 360.0)){
			moveNorth(distance);
		} else if((heading.getAzimuth() > 0) && (heading.getAzimuth() < 90)){
			//NorthEast
			verticalComponent = distance * Math.cos(Math.toRadians(heading.getAzimuth()));
			horizontalComponent = distance * Math.cos(Math.toRadians(90 - heading.getAzimuth()));
			moveNorth(verticalComponent);
			moveEast(horizontalComponent);
		} else if(heading.getAzimuth() == 90){
			moveEast(distance);
		} else if((heading.getAzimuth() > 90) && (heading.getAzimuth() < 180)){
			//SouthEast
			verticalComponent = distance * Math.cos(Math.toRadians(180 - heading.getAzimuth()));
			horizontalComponent = distance * Math.cos(Math.toRadians(heading.getAzimuth() - 90));
			moveSouth(verticalComponent);
			moveEast(horizontalComponent);
		} else if(heading.getAzimuth() == 180){
			moveSouth(distance);
		} else if((heading.getAzimuth() > 180) && (heading.getAzimuth() < 270)){
			//SouthWest
			verticalComponent = distance * Math.cos(Math.toRadians(heading.getAzimuth() - 180));
			horizontalComponent = distance * Math.cos(Math.toRadians(270 - heading.getAzimuth()));
			moveSouth(verticalComponent);
			moveWest(horizontalComponent);
		} else if(heading.getAzimuth() == 270){
			moveWest(distance);
		} else{
			//NorthWest
			verticalComponent = distance * Math.cos(Math.toRadians(360 - heading.getAzimuth()));
			horizontalComponent = distance * Math.cos(Math.toRadians(heading.getAzimuth() - 270));
			moveNorth(verticalComponent);
			moveWest(horizontalComponent);
		}
	}
	
	public static Azimuth calculateBearing(Coordinates origin, Coordinates destination)
	{
		double x1 = origin.getLongitude();
		double y1 = origin.getLatitude();
		double x2 = destination.getLongitude();
		double y2 = destination.getLatitude();
		
		// distance, or leg, vector (dv)
		double horizontalDiff = x2 - x1;
		double verticalDiff = y2 - y1;
		
		// The "North" normal vector (nv)
		// its norm is 1
		double x = 0;
		double y = 1;
		
		// Calculation of the cosine of the angle between the vectors
		// via their inner product and their norms
		double innerProduct = verticalDiff; //because: (horizontalDiff * 0 + verticalDiff * 1) = verticalDiff
		double normOfDv = calculateDistance((new Coordinates(0, 0)), (new Coordinates(horizontalDiff, verticalDiff)));
		double cosOfAngle = innerProduct / normOfDv; //because: cos(angle) = innerProduct(v1, v2) / (norm(v1) * norm(v2))
		double angle = Math.acos(cosOfAngle) * 180 / Math.PI; //Math.acos() returns Radians. Converting to degrees.
		if(horizontalDiff < 0){
			angle = 360 - angle;
		}
		return (new Azimuth(angle));
	}
	
	public static Double calculateDistance(Coordinates a, Coordinates b)
	{
		if((a != null) && (b != null)){
			double horizontal = Math.abs(a.getLongitude() - b.getLongitude());
			double vertical = Math.abs(a.getLatitude() - b.getLatitude());
			double distance = Math.sqrt(Math.pow(horizontal, 2) + Math.pow(vertical, 2));
			return distance;
		}
		return null;
	}
	
	public static double distanceOfPointPFromLineAB(Coordinates p, Coordinates a, Coordinates b) // https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line http://ebooks.edu.gr/modules/ebook/show.php/DSGL-B100/491/3189,12934/
	{
		double Xp = p.longitude;
		double Yp = p.latitude;
		double Xa = a.longitude;
		double Ya = a.latitude;
		double Xb = b.longitude;
		double Yb = b.latitude;
		
		double distance = Math.abs((Yb - Ya) * Xp - (Xb - Xa) * Yp + Xb * Ya - Yb * Xa) / Math.sqrt(Math.pow((Yb - Ya), 2) + Math.pow((Xb - Xa), 2));
		return distance;
	}
	
	@Override
	public int hashCode()
	{
		long result = 7;
		long lon = Double.doubleToLongBits(this.longitude);
		long lat = Double.doubleToLongBits(this.latitude);
		result = 17 * result + (lon + lat);
		result = result % 1000000000;
		result *= -1;
		return (int)result;
	}
	
	@Override
	public boolean equals(Object o)
	{
		Coordinates c = (Coordinates)o;
		if((c.longitude == this.longitude) && (c.latitude == this.latitude)){
			return true;
		} else{
			return false;
		}
	}
}