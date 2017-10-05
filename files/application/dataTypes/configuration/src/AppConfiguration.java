class AppConfiguration
{
	private static final double defaultTurbulence = 0.05;
	private static final double defaultCentripetalForce = 2650000;
	private static final GuiMode defaultGuiMode = GuiMode.FIXED;
	private static final int defaultTrailLength = 50;
	private static final int defaultLaneWidth = 300;
	private static final int defaultBroadcastInterval = 2000;
	private static final boolean defaultDebugStatus = false;
	private static final double defaultImmobileSpeedThreshold = 4;
	private static final double defaultGlobalSpeedLimit = 100;
	
	private static double turbulence;
	private static double centripetalForce;
	private static GuiMode guiMode;
	private static int trailLength;
	private static int laneWidth;
	private static int broadcastInterval;
	private static boolean debugStatus;
	private static double immobileSpeedThreshold;
	private static double globalSpeedLimit;
	
	static
	{
		turbulence = defaultTurbulence;
		centripetalForce = defaultCentripetalForce;
		guiMode = defaultGuiMode;
		trailLength = defaultTrailLength;
		laneWidth = defaultLaneWidth;
		broadcastInterval = defaultBroadcastInterval;
		debugStatus = defaultDebugStatus;
		immobileSpeedThreshold = defaultImmobileSpeedThreshold;
		globalSpeedLimit = defaultGlobalSpeedLimit;
	}
	
	private AppConfiguration(){}
	
	public static void setTurbulence(double t) {turbulence = t;}
	public static void setCentripetalForce(double cf) {centripetalForce = cf;}
	public static void setGuiMode(GuiMode gm) {guiMode = gm;}
	public static void setTrailLength(int tl) {trailLength = tl;}
	public static void setLaneWidth(int lw) {laneWidth = lw;}
	public static void setBroadcastInterval(int bi) {broadcastInterval = bi;}
	public static void setDebugStatus(boolean ds) {debugStatus = ds;}
	public static void setImmobileSpeedThreshold(double ist) {immobileSpeedThreshold = ist;}
	public static void setGlobalSpeedLimit(double gsl) {globalSpeedLimit = gsl;}
	
	public static double getTurbulence() {return turbulence;}
	public static double getCentripetalForce() {return centripetalForce;}
	public static GuiMode getGuiMode() {return guiMode;}
	public static int getTrailLength() {return trailLength;}
	public static int getLaneWidth() {return laneWidth;}
	public static int getBroadcastInterval() {return broadcastInterval;}
	public static boolean getDebugStatus() {return debugStatus;}
	public static double getImmobileSpeedThreshold() {return immobileSpeedThreshold;}
	public static double getGlobalSpeedLimit() {return globalSpeedLimit;}
	
	public static double getDefaultTurbulence() {return defaultTurbulence;}
	public static double getDefaultCentripetalForce() {return defaultCentripetalForce;}
	public static GuiMode getDefaultGuiMode() {return defaultGuiMode;}
	public static int getDefaultTrailLength() {return defaultTrailLength;}
	public static int getDefaultLaneWidth() {return defaultLaneWidth;}
	public static int getDefaultBroadcastInterval() {return defaultBroadcastInterval;}
	public static boolean getDefaultDebugStatus() {return defaultDebugStatus;}
	public static double getDefaultImmobileSpeedThreshold() {return defaultImmobileSpeedThreshold;}
	public static double getDefaultGlobalSpeedLimit() {return defaultGlobalSpeedLimit;}
}