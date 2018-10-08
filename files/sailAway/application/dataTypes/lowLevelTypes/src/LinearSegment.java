// Due to the fact that Leg which extends LinearSegment, has Waypoints instead of Coordinates, and Waypoints extends Coordinates, this class could be re-written in as a generic one
class LinearSegment//<T extends Coordinates>
{
	//private T a;
	//private T b;
	private Coordinates a;
	private Coordinates b;
	
	//private T northEnd;
	//private T southEnd;
	//private T westEnd;
	//private T eastEnd;
	private Coordinates northEnd;
	private Coordinates southEnd;
	private Coordinates westEnd;
	private Coordinates eastEnd;
	
	private double minX;
	private double minY;
	private double maxX;
	private double maxY;

	private double slope;
	private double intercept;
	private boolean isVertical;
	
	//protected <T extends Coordinates> T getPointA(Class<T> type){return type.cast(a);}
	//protected <T extends Coordinates> T getPointB(Class<T> type){return type.cast(b);}
	protected Coordinates getPointA(){return a;}
	protected Coordinates getPointB(){return b;}
	
	//protected <T extends Coordinates> T getNorthEnd(Class<T> type){return type.cast(northEnd);}
	//protected <T extends Coordinates> T getSouthEnd(Class<T> type){return type.cast(southEnd);}
	//protected <T extends Coordinates> T getWestEnd(Class<T> type){return type.cast(westEnd);}
	//protected <T extends Coordinates> T getEastEnd(Class<T> type){return type.cast(eastEnd);}
	protected Coordinates getNorthEnd(){return northEnd;}
	protected Coordinates getSouthEnd(){return southEnd;}
	protected Coordinates getWestEnd(){return westEnd;}
	protected Coordinates getEastEnd(){return eastEnd;}
	
	LinearSegment(Coordinates /*T*/ a, Coordinates /*T*/ b)
	{
		this.a = a;
		this.b = b;
		
		determineExtremes();
		determineSlope();
		determineIntercept();
	}
	
	private void determineExtremes()
	{
		if(a.getLongitude() <= b.getLongitude()){
			westEnd = a;
			eastEnd = b;
		}else{
			westEnd = b;
			eastEnd = a;
		}
		if(a.getLatitude() <= b.getLatitude()){
			southEnd = a;
			northEnd = b;
		}else{
			southEnd = b;
			northEnd = a;
		}
		
		if(AppConfiguration.getDebugStatus()){
			System.out.println("Segment NSWE extremes are:");
			System.out.println("N-end: " + northEnd.toString());
			System.out.println("S-end: " + southEnd.toString());
			System.out.println("W-end: " + westEnd.toString());
			System.out.println("E-end: " + eastEnd.toString());
		}
		
		minX = westEnd.getLongitude();
		maxX = eastEnd.getLongitude();
		minY = southEnd.getLatitude();
		maxY = northEnd.getLatitude();
	}
	
	private void determineSlope()
	{
		if(a.getLongitude() == b.getLongitude()){
			isVertical = true;
			// Slope is infinite
		}else{
			isVertical = false;
			slope = (a.getLatitude() - b.getLatitude()) / (a.getLongitude() - b.getLongitude());
		}
		
		if(AppConfiguration.getDebugStatus()){
			if(isVertical){
				System.out.println("LinearSegment is vertical");
			} else{
				System.out.println("LinearSegment's slope is " + slope);
			}
			if(isFlat()) System.out.println("LinearSegment is flat");
		}
	}
	
	private void determineIntercept()
	{
		if(isVertical){
			// Intercept not applicable
		}else{
			intercept = a.getLatitude() - slope * a.getLongitude();
		}
	}
	
	public boolean isFlat()
	{
		if(isVertical) return false;
		if(slope == 0) return true;
		return false;
	}
	
	// As we need this method to return two variables, we wrapped those in a local datatype to be returned. Wrapped there, we have a boolean indicating wether two linear segments share any common points,
	// and a Coordinates obj representing their intersection point. This combination of variables might seem to convey redundant information about an inersection.
	// For instance, one might think: "Since the intersection point is not null, it must mean that the segments do in fact intersect". Such a statement however might be wrong in some cases.
	// To understand this choice of return datatype, we should first understand the way this method works:
	// First, the method calculates the intersection point of the segments' carrier lines that are non-parallel (and that means non-colinear as well) and registers that within the return variable.
	// The point is null for parallels.
	// Second, it checks the boundaries of the two linear segments to determine wether that point, if not null,	is in fact a mutual point. And sets the boolean accordingly.
	// If the point is null, a check for colinearity is performed. If not colinear, the boolean gets set to false, as the segments are parallel.
	// If colinear, a further check for length overlap is done. The boolean get set accordingly.
	// It now may be clear that there are four combinations at play, each one depicting a distinct relation between the segments:
	// 1: ( true  , (x,y) )  ::  The two linear segments intersect. (...at their only common point, which is within both their boundaries)
	// 2: ( true  ,  null )  ::  The two linear segments are colinear and have multiple common points, as they overlap
	// 3: ( false , (x,y) )  ::  The two linear segments do not intersect, but they will eventually if extended appropriately.
	// 4: ( false ,  null )  ::  The two linear segments do not have any common points. They are parallel.
	//		In the special case they are also colinear, they will have common points if extended appropriately. If not colinear, they won't ever have any points in common, despite any length extension.
	public IntersectionStatus getIntersectionWith(LinearSegment s)
	{
		if(!((this.isVertical) || (s.isVertical))){
			if(this.slope == s.slope){
				if(this.intercept == s.intercept){ // colinear segments i.e., on the same carrier line
					/* If the sum of their respective lengths is bigger than the length of the linear segment that is defined by endpoints
					 * M: (xm, ym = slope * xn + intercept) and 
					 * N: (xn, yn = slope * xn + intercept) ##NOTE: in this case, both slope and intercept are common for the two segments##, where
					 * M is the western-most point and
					 * N is the eastern-most point of the union set of the points in the two segments,
					 * then the segments intersect, since they have a mutually overlapping section.
					 *
					 * Note that since the slope is common, we need not calculate length, but simply get either the two vertical or two horizontal
					 * components of the segments (here, the horizontals are selected arbitrarily). All three compared lengths remain proportional, since:
					 * φ = Arctan(slope) and
					 * horizontal component (i.e adjacent) = hypotenuse (i.e segment length) / cos(φ).
					 * in the other segment's triangle, the horizontal component is calculated by dividing with the exact same number, cos(φ).
					 * The same applies to the "triangle of unions" discussed earlier.
					 */
					double westernmostLon = this.minX;
					if(s.minX < westernmostLon){
						westernmostLon = s.minX;
					}
					double easternmostLon = this.maxX;
					if(s.maxX > easternmostLon){
						easternmostLon = s.maxX;
					}
					double combinedHorizontalSpan = easternmostLon - westernmostLon;
					if(combinedHorizontalSpan > ((this.maxX - this.minX) + (s.maxX - s.minX))){
						return new IntersectionStatus(false, null);
					} else{
						return new IntersectionStatus(true, null);
					}
				} else{
					return new IntersectionStatus(false, null); // parallel segments and not colinear
				}
			} else{ // Different slope
				// Find intersection point
				double longitudeOfIntersection = (s.intercept - this.intercept) / (this.slope - s.slope);
				double latitudeOfIntersection = this.slope * longitudeOfIntersection + this.intercept;
				// Check that the intersection point is within both the linear segments' boundaries.
				/* Note that since these linear funtions are genuinely monotonous and thus 1-1 functions,
				 * if A and B the endpoints of a linear segment, with coordinates:
				 * A: (xA, f(xA) = yA)
				 * B: (xB, f(xB) = yB)
				 * and there is a point Z that belongs to the segment's line with coordinates:
				 * Z: (xZ, f(xZ) = yZ)
				 * and (xA <= xZ <= xB) <=> (yA <= yZ <= yB).
				 * So we need not check both the horizontal (longitudinal) and vertical (latitudinal) boundaries of a segment.
				 * We have chosen to examine the horizontal boundaries for both linear segments.
				 */
				if(!((this.isFlat()) || (s.isFlat()))){
					if((this.minX <= longitudeOfIntersection) && (this.maxX >= longitudeOfIntersection)){
						if((s.minX <= longitudeOfIntersection) && (s.maxX >= longitudeOfIntersection)){
							/*if((this.minY <= latitudeOfIntersection) && (this.maxY >= latitudeOfIntersection)){ // The latitude checks are redundant, but we were getting false positives for an unknown reason (perhaps implicit number conversions / rounding).
								if((s.minY <= latitudeOfIntersection) && (s.maxY >= latitudeOfIntersection)){   // Different intersection check procedure for segments known to be adjacent, has been an effective work - around.
									return true;
								} else{
									return false;
								}
							} else {
								return false;
							}*/
							return new IntersectionStatus(true, new Coordinates(longitudeOfIntersection, latitudeOfIntersection));
						} else{
							return new IntersectionStatus(false, new Coordinates(longitudeOfIntersection, latitudeOfIntersection));
						}
					} else{
						return new IntersectionStatus(false, new Coordinates(longitudeOfIntersection, latitudeOfIntersection));
					}
				} else if(((!(this.isFlat())) && (s.isFlat())) || ((this.isFlat()) && (!(s.isFlat())))){
					boolean intersects = false;
					// it isn't really necessary to differentiate the flat and sloped arguments when calling flatAndSlopedIntersection(), as it will return the same result regardless.
					if(this.isFlat()){
						intersects = flatAndSlopedIntersection(this, s, longitudeOfIntersection);
					} else{
						intersects = flatAndSlopedIntersection(s, this, longitudeOfIntersection);
					}
					return new IntersectionStatus(intersects, new Coordinates(longitudeOfIntersection, latitudeOfIntersection));
				}
			}
		}else if(((this.isVertical) && (!(s.isVertical))) || ((!(this.isVertical)) && (s.isVertical))){
			if(this.isVertical){
				return verticalAndSlopedIntersection(this, s);
			} else{
				return verticalAndSlopedIntersection(s, this);
			}
		} else{
			if(this.a.getLongitude() == s.a.getLongitude()){ // colinear segments i.e., on the same (vertical) carrier line
				/* We determine occurence of overlap by comparing lengths as done earlier in the method.
				 * Note that in this case, the vertical component is the same as length and horizontal is zero.
				 */
				double southernmostLat = this.minY;
				if(s.minY < southernmostLat){
					southernmostLat = s.minY;
				}
				double northernmostLat = this.maxY;
				if(s.maxY > northernmostLat){
					northernmostLat = s.maxY;
				}
				double combinedVerticalSpan = northernmostLat - southernmostLat;
				if(combinedVerticalSpan > ((this.maxY - this.minY) + (s.maxY - s.minY))){
					return new IntersectionStatus(false, null);
				} else{
					return new IntersectionStatus(true, null);
				}
			} else{
				return new IntersectionStatus(false, null); // parallel segments and not colinear
			}
		}
		System.out.println("This should never print.");
		return null; // Ureachable line, but needed to silence the compiler...
	}
	
	public boolean intersectsAdjacent(LinearSegment s)
	{
		if(!((this.isVertical) || (s.isVertical))){
			if(this.slope == s.slope){
				double westernmostLon = this.minX;
				if(s.minX < westernmostLon){
					westernmostLon = s.minX;
				}
				double easternmostLon = this.maxX;
				if(s.maxX > easternmostLon){
					easternmostLon = s.maxX;
				}
				double combinedHorizontalSpan = easternmostLon - westernmostLon;
				if((combinedHorizontalSpan + 0.001) > ((this.maxX - this.minX) + (s.maxX - s.minX))){ // we have to cheat a little here, by adding an offset...
					return false;
				} else{
					return true;
				}
			} else{
				return false;
			}
		} else if(((this.isVertical) && (!(s.isVertical))) || ((!(this.isVertical)) && (s.isVertical))){
			return false;
		} else{
			double southernmostLat = this.minY;
			if(s.minY < southernmostLat){
				southernmostLat = s.minY;
			}
			double northernmostLat = this.maxY;
			if(s.maxY > northernmostLat){
				northernmostLat = s.maxY;
			}
			double combinedVerticalSpan = northernmostLat - southernmostLat;
			if((combinedVerticalSpan + 0.001) > ((this.maxY - this.minY) + (s.maxY - s.minY))){
				return false;
			} else{
				return true;
			}
		}
	}
	
	private static IntersectionStatus verticalAndSlopedIntersection(LinearSegment v, LinearSegment s)
	{
		// find intersection
		double longitudeOfIntersection = v.a.getLongitude();
		double latitudeOfIntersection = s.slope * v.a.getLongitude() + s.intercept;
		
		// and check boundaries
		if((s.minX <= longitudeOfIntersection) && (s.maxX >= longitudeOfIntersection)){
			if((v.minY <= latitudeOfIntersection) && (v.maxY >= latitudeOfIntersection)){
				return new IntersectionStatus(true, new Coordinates(longitudeOfIntersection, latitudeOfIntersection));
			} else{
				return new IntersectionStatus(false, new Coordinates(longitudeOfIntersection, latitudeOfIntersection));
			}
		} else{
			return new IntersectionStatus(false, new Coordinates(longitudeOfIntersection, latitudeOfIntersection));
		}
	}
	
	// This method is a bit different, functionally, from the similar verticalAndSlopedIntersection(), as it does not calculate the intersection point,
	// but it receives it as an argument (double lonOfIntersection) from its caller.
	private static boolean flatAndSlopedIntersection(LinearSegment f, LinearSegment s, double lonOfIntersection)
	{
		if((f.minX <= lonOfIntersection) && (f.maxX >= lonOfIntersection)){
			if((s.minX <= lonOfIntersection) && (s.maxX >= lonOfIntersection)){
				return true;
			} else{
				return false;
			}
		} else{
			return false;
		}
	}
	
	public String toString()
	{
		return ("Linear segment with endpoints: A: " + a.toString() + " and B: " + b.toString());
	}
	
	public double distanceFromCarrierLineOfThisSegment(Coordinates position)
	{
		return Coordinates.distanceOfPointPFromLineAB(position, this.a, this.b);
	}
	
	/*protected void determineLength()
	{
		if(isVertical){
			length = northEnd.getLatitude() - southEnd.getLatitude();
		} else{
			double horizontalDiff = b.getLongitude() - a.getLongitude();
			double verticalDiff = b.getLatitude() - a.getLatitude();
			length = Math.sqrt(Math.pow(horizontalDiff, 2) + Math.pow(verticalDiff, 2));
		}
	}*/
	
	public double distanceFrom(Coordinates point)
	{
		// get x, y coordinates of parameter "point" and of endpoints a, b of this linear segment as well:
		double xa = a.getLongitude();
		double ya = a.getLatitude();
		double xb = b.getLongitude();
		double yb = b.getLatitude();
		double xp = point.getLongitude();
		double yp = point.getLatitude();
		// move the two vectors ((a -> b) and (a -> point)) to origin (0, 0):
		double abX = (xb - xa);
		double abY = (yb - ya);
		double apX = (xp - xa);
		double apY = (yp - ya);
		
		// calculate the inner product of vectors (a -> b) and (a -> point):
		double dotProduct = ((abX * apX) + (abY * apY));
		// calculate the square of this linear segment's length (based on the Pythagorean):
		double lengthOfThisLinearSegmentSquared = (Math.pow(abX, 2) + Math.pow(abY, 2));
		
		/*The absolute value of the inner product (a.k.a. dot product) of 2D vectors a, b equals the norm (a.k.a. length) of either vector multiplied by the norm of the of the other
		vector's projection on it. In any case, the two norms multiplied represent colinear vectors. If the contained angle of a, b is obtuse (>90 degrees), the projection of one to
		the other is still colinear but opposed, yielding a negative dot product.
		By diving the calculated dot product by the norm of the vector being cast to, the quotient is the norm of the projection to that vector. By dividing that result again by the
		vector's norm, the final quotient represents the length of the aforementioned projection expressed as a fraction of the vector's length it is projected on. The two divisions
		are carried out in a single step, since the algorithm divides by the square of the norm of the original vector being cast to.
		This method adapts the above concepts to this linear segment and an arbitrary point on the plane: The segment is represented by a vector with startpoint a and endpoint b, and
		the alien point "p", by a vector with the same startpoint (a) and endpoint the point in question itself. The vectors are moved to origin and then (a->p) is projected onto
		(a->b).
		A negative quotient means that the contained angle between (a->p) and (a->b) is obtuse, or in other words, (a->b) and the projection of (a->p) on (a->b) are opposite. In that
		case, the (shortest) distance of p to the linear segment ab is its straight distance from point a. Similarly, for quotients 1 or more the distance or p to ab is represented by
		the segment that couples b with p. Finally, if ( 0 <= quotient <= 1), the endpoint of the associated projection falls between the points a and b on their common carrier line and
		the the shortest distance of p to ab is just the distance p to the carrier line of ab.
		To avoid dividing by zero, which would be the case for zero-length linear segments, the algorithm just returns the distance of p from a in such circumstances.*/
		if(lengthOfThisLinearSegmentSquared > 0){
			double ratio = dotProduct / lengthOfThisLinearSegmentSquared;
			if(ratio < 0){
				return (double)Coordinates.calculateDistance(a, point);
			} else if(ratio > 1){
				return (double)Coordinates.calculateDistance(b, point);
			} else{
				return distanceFromCarrierLineOfThisSegment(point);
			}
		} else{
			return (double)Coordinates.calculateDistance(a, point);
		}
	}
	
	//------------------------------  Local datatypes  ------------------------------//
	static class IntersectionStatus
	{
		private boolean intersecting;
		private Coordinates intersection;
		
		private IntersectionStatus (boolean intersecting, Coordinates intersection)
		{
			this.intersecting = intersecting;
			this.intersection = intersection;
		}
		
		public boolean intersecting()
		{
			return intersecting;
		}
		
		public Coordinates getIntersection()
		{
			return intersection;
		}
	}
}