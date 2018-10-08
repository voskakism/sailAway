class Azimuth
{
	private static final double DEFAULT = 0;
	private double value = 0;
	
	private static double normalize(double input) //able to implicitly convert int passed from constructor to double
	{
		if(input < 0){
			input = input % 360;
			input += 360;
		} else if(input == 0){
			return 360;
		} else if(input > 360){
			input = input % 360;
		}
		return input;
	}
	
	public static Azimuth calculateRelativeBearing(Azimuth heading, Azimuth absoluteBearing)
	{
		double magneticNorthOffset = heading.getAzimuth();
		double targetAbsoluteBearing = absoluteBearing.getAzimuth();
		return new Azimuth(targetAbsoluteBearing - magneticNorthOffset);
	}
	
	public static Double angleFinder(Azimuth a, Azimuth b) // the angle diff in degrees between two Azimuths // referenced only by Esper queries
	{
		if((a != null) && (b != null)){
			Azimuth turn = a.relativeTo(b);
			double angle = turn.getAzimuth();
			if(angle > 180){
				angle = 360 - angle;
			}
			return angle;
		}
		return null;
	}
	
	public Azimuth()
	{
		value = DEFAULT;
	}
	
	public Azimuth(int value)
	{
		this.value = normalize(value);
	}
	
	public Azimuth(double value)
	{
		this.value = normalize(value);
	}
	
	public double getAzimuth()
	{
		return value;
	}
	
	public void setAzimuth(int value)
	{
		this.value = normalize(value);
	}
	
	public void setAzimuth(double value)
	{
		this.value = normalize(value);
	}
	
	public void rotateClockwise(int turn)
	{
		value += turn;
		value = normalize(value);
	}
	
	public void rotateClockwise(double turn)
	{
		value += turn;
		value = normalize(value);
	}
	
	public void rotateCounterClockwise(int turn)
	{
		value -= turn;
		value = normalize(value);
	}
	
	public void rotateCounterClockwise(double turn)
	{
		value -= turn;
		value = normalize(value);
	}
	
	public void turnTo(Azimuth bearing, double ratio)
	{
		Azimuth relativeBearing = this.relativeTo(bearing);
		double rb = relativeBearing.getAzimuth();
		if(rb > 180){
			rotateCounterClockwise((360 - rb) * ratio);
		} else{
			rotateClockwise(rb * ratio);
		}
	}
	
	public Azimuth relativeTo(Azimuth target)
	{
		double magneticNorthOffset = this.getAzimuth();
		double targetAbsoluteBearing = target.getAzimuth();
		return new Azimuth(targetAbsoluteBearing - magneticNorthOffset);
	}
}