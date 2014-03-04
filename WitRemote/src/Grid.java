

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Grid {
	
	private ArrayList<String> myPoints;
	private int height;
	private int width;
	private PositionCalculator myCalculator;
	
	public Grid(int width, int height) {
		myPoints = new ArrayList<String>(120);
		this.width = width;
		this.height = height;
		myCalculator = new PositionCalculator(width, height);
		
	}
	
	public Vector getPosition(HashMap<String,ArrayList<Integer>> figures) {
		ArrayList<Integer> points = getPoints(figures);
		int a = points.get(0);
		int b = points.get(1);
		if (points.size() == 3) {
			int c = points.get(2);
			return myCalculator.calculateTriple(a, b, c);
		}
		else {
			return null;
		}
		
	}
	
	public ArrayList<Integer> getPoints(HashMap<String,ArrayList<Integer>> figures) {
		if (figures.size() == 3) {
			HashSet<String> keys = new HashSet<String>(figures.keySet());
			ArrayList<String> listkeys = new ArrayList<String>(keys);
			ArrayList<Integer> points0 = new ArrayList<Integer>();
			ArrayList<Integer> points1 = new ArrayList<Integer>();
			ArrayList<Integer> points2 = new ArrayList<Integer>();
			for (int i = 0; i < myPoints.size(); i++) {
				if (listkeys.get(0) == myPoints.get(i)) {
					points0.add(i);
				}
				else if (listkeys.get(1) == myPoints.get(i)) {
					points1.add(i);
				}
				else if (listkeys.get(2) == myPoints.get(i)) {
					points2.add(i);
				}
			}			
			for (int i = 0; i < points0.size(); i++) {
				for (int j = 0; j < points1.size(); j++) {
					ArrayList<Integer> compareList = getHexagon(points0.get(i));
					boolean found = false;
					int j2 = 0;
					while (j2 < compareList.size() && !found) {
						if (compareList.get(j2) == points1.get(j)) {
							found = true;
						}
						j2++;
					}
					if (found) {
						for (int k = 0; k < points2.size(); k++) {
							ArrayList<Integer> compareList2 = getTriangle(points0.get(i), points1.get(j));
							boolean found2 = false;
							int k2 = 0;
							while (k2 < compareList2.size() && !found2) {
								if (compareList2.get(k2) == points2.get(k)) {
									found2 = true;
								}
								k2++;
							}
							if (found2) {
								ArrayList<Integer> returnList = new ArrayList<Integer>();
								returnList.add(points0.get(i));
								returnList.add(points1.get(j));
								returnList.add(points2.get(k));
								return returnList;
							}
						}
					}
				}
				
			}
			return null;
		}
		if (figures.size() == 2) {
			
		}
		
		if (figures.size() == 1) {
			
		}
		return null;
	}
	
	public int getRotation(ArrayList<String> points) {
		
		
		return 0;
	}
	
	private ArrayList<Integer> getTriangles() {
		
		return null;
	}
	
	// return the hexagon around a given point (if the complet hexagon exists, otherwise les points)
	private ArrayList<Integer> getHexagon(int point) {
		int line = point/width +1; /// width ipv height
		int column = point - (line-1) * width + 1;
		ArrayList<Integer> newPoints = new ArrayList<Integer>();
		// 6, 5 of 3 punten afhankelijk van positie op EVEN lijn
		if ( line%2 == 0) {	
			// sowieso toevoegen
			newPoints.add(point-width);							
			newPoints.add(point+width);				
			// indien niet het laatste punt van een lijn
			if (!((point+1)%width == 0)) {	
				newPoints.add(point+1);
				newPoints.add(point-width+1);
				newPoints.add(point+width+1);				
			}
			// indien niet het eerste punt van een lijn
			if (!((point)%width == 0)) { 
				newPoints.add(point-1);
			}
		}
		// 6, 5 of 3 punten afhankelijk van positie op ONEVEN lijn
		else {
			// sowieso toevoegen
			newPoints.add(point-width);							
			newPoints.add(point+width);				
			// indien niet het laatste punt van een lijn.
			if (!((point+1)%width == 0)) {	
				newPoints.add(point+1);								
			}
			// indien niet het eerste punt van een lijn
			if (!((point)%width == 0)) {
				newPoints.add(point-1);
				newPoints.add(point-width-1);
				newPoints.add(point+width-1);
			}
		}
		return newPoints;
	}
	
	//return the two last points when 2 NEIGHBOUR points are given.
	private ArrayList<Integer> getTriangle(int point1, int point2) {
		int firstpoint;
		int secondpoint;
		if (point1<point2) {
			firstpoint = point1;
			secondpoint = point2;
		}
		else {
			firstpoint = point2;
			secondpoint = point1;
		}
		int line1 = firstpoint/width +1; 
		int column1 = firstpoint - (line1-1) * width +1; 
		int line2 = secondpoint/width +1; 
		int column2 = secondpoint - (line2-1) * width +1; 
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		// first < second
		if (firstpoint < secondpoint) {
			if (line1 == line2) {
				// EVEN rij
				if (line1%2 == 0) {
					returnList.add(secondpoint-width);
					returnList.add(secondpoint+width);
				} 
				// ONEVEN rij
				else {
					returnList.add(firstpoint-width);
					returnList.add(firstpoint+width);
				}
			}
			else if ( column1 == column2) {
				// 1 op EVEN rij
				if (line1%2 == 0) {
					//als niet 2 punten aan rechterzijkant
					if (!(column1 == width)) {
						returnList.add(secondpoint+1);
						}					
					// als niet 2 punten aan de linkerzijkant
					if (!(column1 == 1)) {
						returnList.add(firstpoint-1);
					}
				} 
				// 1 op ONEVEN rij
				else {
					// als niet 2 punten aan rechterzijkant
					if (!(column1 == width)) {
						returnList.add(firstpoint+1);
					}
					if (!(column1 == 1)) {
						returnList.add(secondpoint-1);
					}					
				}
			}			
			else {
				// 1 op EVEN rij
				if (line1%2 == 0) {
					returnList.add(firstpoint+1);
					returnList.add(secondpoint-1);
				}
				// 1 op ONEVEN rij
				else {
					returnList.add(firstpoint-1);
					returnList.add(secondpoint+1);
				}
			}
		}
		
		return returnList;
	}
}
