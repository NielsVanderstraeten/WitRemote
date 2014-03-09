package Rooster;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Grid {
	
	private ArrayList<String> myMap;
	private int height;
	private int width;
	private PositionCalculator myCalculator;
	private double pictureDistance;
	private double lastZepPosition;
	//toegelaten afwijking in percenten bij afstandsverglijking
	private final double approx = 10;
	//laatste afstand van 2 naast elkaar liggende punten
	
	public Grid(int width, int height) {
		//myPoints = new ArrayList<String>(120);
		this.width = width;
		this.height = height;
		myCalculator = new PositionCalculator(width, height);
		
	}
	
	public void setMap(ArrayList<String> list) {
		this.myMap = list;
	}
	

	//returns de rotatie in graden (x-as naar rechts, y-as naar onder)
	public double getRotation(HashMap<String,Vector> figures) {
		HashMap<String,Vector> rightFigures = this.getRightTriangle(figures);
		ArrayList<Integer> points = getPoints(figures);
		int a = points.get(0);
		int b = points.get(1);
		if (points.size() == 3) {
			int c = points.get(2);
			Vector triCenter = myCalculator.calculateTriple(a, b, c);
			double gridAngle = myCalculator.getVector(a).getAngle(triCenter);
			System.out.println("gridAngle = " + gridAngle);
			Vector picCenter = myCalculator.calculateTriple(rightFigures.get(myMap.get(a)), rightFigures.get(myMap.get(b)), rightFigures.get(myMap.get(c)));
			double pictureAngle = rightFigures.get(myMap.get(a)).getAngle(picCenter);
			System.out.println("pictureAngle = " + pictureAngle);
			return (gridAngle - pictureAngle + 270)%360;
		}
		return 0;
	}
	
	public Vector getPosition(HashMap<String,Vector> figures) {
		ArrayList<Integer> points = getPoints(figures);
		int a = points.get(0);
		int b = points.get(1);
		if (points.size() == 3) {
			int c = points.get(2);
			return myCalculator.calculateTriple(a, b, c);
		}
		else if (points.size() == 2) {
			return myCalculator.calculateDouble(a, b);
		}
		else {
			//nieuwe foto maken -> nog programmeren
			return null;
		}
		
	}
	
	private boolean figuresContainTriangle(HashMap<String,Vector> figures) {
		return !(this.getRightTriangle(figures) == null);
	}
	
	private boolean figuresContainNeighbours(HashMap<String,Vector> figures) {
		return !(this.getRightNeighbours(figures) == null);
	} 
	
	public ArrayList<Integer> getPoints(HashMap<String,Vector> figures) {
		if (figures.size() >= 3) {
			if (figuresContainTriangle(figures)) {
				HashMap<String,Vector> rightFigures = this.getRightTriangle(figures);
				HashSet<String> keys = new HashSet<String>(rightFigures.keySet());
				ArrayList<String> listkeys = new ArrayList<String>(keys);
				ArrayList<Integer> points0 = new ArrayList<Integer>();
				ArrayList<Integer> points1 = new ArrayList<Integer>();
				ArrayList<Integer> points2 = new ArrayList<Integer>();
				for (int i = 0; i < myMap.size(); i++) {
					if (listkeys.get(0) == myMap.get(i)) {
						points0.add(i);
					}
					else if (listkeys.get(1) == myMap.get(i)) {
						points1.add(i);
					}
					else if (listkeys.get(2) == myMap.get(i)) {
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
			}
			
			return null;
		}
		if (figures.size() == 2) {
			HashSet<String> keys = new HashSet<String>(figures.keySet());
			ArrayList<String> listkeys = new ArrayList<String>(keys);
			Vector vector0 = figures.get(listkeys.get(0));
			Vector vector1 = figures.get(listkeys.get(1));
			vector0.getDistance(vector1);
			
		}
		
		if (figures.size() == 1) {
			
		}
		return null;
	}
	
	// return the hexagon around a given point (if the complete hexagon exists, otherwise only the existing surrounding points)
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
	
	//Methode om een gelijkzijdige driehoek uit de ontvangen hashmap van de fotoanalyse te verkrijgen
	private HashMap<String, Vector> getRightTriangle(HashMap<String,Vector> figures) {
		HashSet<String> keys = new HashSet<String>(figures.keySet());
		ArrayList<String> listkeys = new ArrayList<String>(keys);
		HashMap<String,Vector> returnMap = new HashMap<String,Vector>();
		for (int i=0;i<listkeys.size()-2;i++) {
			for (int j = (i+1); j<listkeys.size()-1;j++) {
				for(int k = (j+1); k<listkeys.size();k++) {
					String key1 = listkeys.get(i);
					String key2 = listkeys.get(j);
					String key3 = listkeys.get(k);
					if (isTriangle(figures.get(key1), figures.get(key2), figures.get(key3))) {
						returnMap.put(key1,figures.get(key1));
						returnMap.put(key2,figures.get(key2));
						returnMap.put(key3,figures.get(key3));
						return returnMap;
					}
				}
			}
		}
		
		System.out.println("geen driehoek gevonden");
		return null;
	}
	
	//methode dat uit een gegeven hashmap twee buren haalt.
	public HashMap<String,Vector> getRightNeighbours(HashMap<String, Vector> figures) {
		return null;
	}
	
	//boolean ofdat 3 ontvangen vectoren in een gelijkzijdige driehoek liggen
	private boolean isTriangle(Vector vector1, Vector vector2, Vector vector3) {
		double distance1 = vector1.getDistance(vector2);
		double distance2 = vector1.getDistance(vector3);
		double distance3 = vector2.getDistance(vector3);
		if (fuzzyEquals(distance1,distance2) && fuzzyEquals(distance1,distance3)) {
			pictureDistance = (distance1 + distance2 + distance3)/3;
			return true;
		} else {
			return false;
		}
		
	}
	
	// gelijk bij benadering
	private boolean fuzzyEquals(double a, double b) {
		return (a*(1+approx/100) > b && a*(1-approx/100) < b);
	}
}
