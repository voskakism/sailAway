public boolean intersects(LinearSegment s)
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
				 * f = Arctan(slope) and
				 * horizontal component (i.e adjacent) = hypotenuse (i.e segment length) / cos(f).
				 * in the other segment's triangle, the horizontal component is calculated by dividing with the exact same number, cos(f).
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
					return false;
				} else{
					return true;
				}
			} else{
				return false; // parallel segments and not colinear
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
							/*if((s.minY <= latitudeOfIntersection) && (s.maxY >= latitudeOfIntersection)){ // Checking further with latitude seems to have gotten it fixed.
								return true;
							} else{
								return false;
							}
						} else {
							return false;
						}*/
						return true;
					} else{
						return false;
					}
				} else{
					return false;
				}
			} else if(((!(this.isFlat())) && (s.isFlat())) || ((this.isFlat()) && (!(s.isFlat())))){
				if(this.isFlat()){
					return flatAndSlopedIntersection(this, s, longitudeOfIntersection);
				} else{
					return flatAndSlopedIntersection(s, this, longitudeOfIntersection);
				}
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
				return false;
			} else{
				return true;
			}
		} else{
			return false; // parallel segments and not colinear
		}
	}
	return true;
}