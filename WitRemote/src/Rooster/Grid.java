package Rooster;


import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;

public class Grid {
	
	private ArrayList<String> myMap;
	private int height;
	private int width;
	private PositionCalculator myCalculator;
	private double pictureDistance;
	private Vector lastZepPosition;
	private double lastRotation;
	//toegelaten afwijking in percenten bij afstandsverglijking
	private final double approx = 8;
	//laatste afstand van 2 naast elkaar liggende punten
	
	
	/*constructor for an object of Grid
	 * 
	 * width = aantal kolommen
	 * height = aantal rijen
	 * map = lijst van String objecten dat de Map voorsteld (vb: "BO", "WH", "YR",...)
	 */
	public Grid(int width, int height, String csvReference) {
		this.width = width;
		this.height = height;
		myCalculator = new PositionCalculator(width, height);
		lastZepPosition = new Vector(0,0);
		lastRotation = 0;
		
	}
	
	public Grid(String plaatsVanCSV) {
		parseCSV(plaatsVanCSV);
		myCalculator = new PositionCalculator(width, height);
		lastZepPosition = new Vector(0,0);
		lastRotation = 0;
	}

	public void setMap(ArrayList<String> list) {
		this.myMap = list;
	}
	
	//Gebruikt OpenCSV: http://opencsv.sourceforge.net/
	private void parseCSV(String csvRef) {
		CSVReader reader;
		java.util.List<String[]> myEntries = null;
		try {
			reader = new CSVReader(new FileReader("src/gui/resources/map.csv"), ',');
			myEntries = (java.util.List<String[]>) reader.readAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(myEntries != null){
			height = myEntries.size();
			width = myEntries.get(0).length;
			myMap = new ArrayList<String>();
			for(String[] array: myEntries){
				for(String something: array){
					myMap.add(something);
				}
			}
		}
	}

	//returns de rotatie in graden (x-as naar rechts, y-as naar onder)
	public double getRotation(ArrayList<Shape> figures) {
		if (figuresContainTriangle(figures)) {
			ArrayList<Shape> rightFigures = this.getRightTriangle(figures);
			ArrayList<Integer> points = getPoints(figures);
			int a = points.get(0);
			int b = points.get(1);
			if (points.size() == 3) {
				int c = points.get(2);
				Shape compare = null;
				for (int i = 0; i<3; i++) {
					boolean test = false;
					for (int j = 0; j<3; j++) {
						if (i !=j) {
							test = (rightFigures.get(i).getCode() == rightFigures.get(j).getCode());
						}
					}
					if (!test) {
						compare = rightFigures.get(i);
					}
				}
				Vector triCenter = myCalculator.calculateTriple(a, b, c);
				double gridAngle = myCalculator.getVector(compare.getGridPosition()).getAngle(triCenter);
//				System.out.println("gridAngle = " + gridAngle);
				Vector picCenter = myCalculator.calculateTriple(rightFigures.get(0).getPosition(), rightFigures.get(1).getPosition(), rightFigures.get(2).getPosition());
				double pictureAngle = compare.getPosition().getAngle(picCenter);
//				System.out.println("pictureAngle = " + pictureAngle);
				lastRotation = (gridAngle - pictureAngle + 270)%360;
			}
		}
		
		return lastRotation;
	}
	
	public Vector getPosition(ArrayList<Shape> figures) {
		ArrayList<Integer> points = getPoints(figures);
		//TODO: steeds size 0, dus IndexOutOFBounsdException
		int a = points.get(0);
		int b = points.get(1);
		if (points.size() == 3) {
			int c = points.get(2);
			lastZepPosition = myCalculator.calculateTriple(a, b, c);
		}
		else if (points.size() == 2) {
			lastZepPosition = myCalculator.calculateDouble(a, b);
		}
		else {
			//nieuwe foto maken -> nog programmeren
		}
		return lastZepPosition;		
	}
	
	private boolean figuresContainTriangle(ArrayList<Shape> figures) {
		return !(this.getRightTriangle(figures) == null);
	}
	
	private boolean figuresContainNeighbours(ArrayList<Shape> figures) {
		return !(this.getRightNeighbours(figures) == null);
	} 
	
	public ArrayList<Integer> getPoints(ArrayList<Shape> figures) {
		if (figures.size() > 2) {
			if (figuresContainTriangle(figures)) {
				ArrayList<Shape> rightFigures = this.getRightTriangle(figures);
				ArrayList<Integer> points0 = new ArrayList<Integer>();
				ArrayList<Integer> points1 = new ArrayList<Integer>();
				ArrayList<Integer> points2 = new ArrayList<Integer>();
				for (int i = 0; i < myMap.size(); i++) {
					if (rightFigures.get(0).getCode().equals(myMap.get(i))) {
						points0.add(i);
//						System.out.println("added to points0: " + i);
					}
					if (rightFigures.get(1).getCode().equals(myMap.get(i))) {
						points1.add(i);
//						System.out.println("added to points1: " + i);
					}
					if (rightFigures.get(2).getCode().equals(myMap.get(i))) {
						points2.add(i);
//						System.out.println("added to points2: " + i);
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
									rightFigures.get(0).setGridPosition(points0.get(i));
//									System.out.println("gridPosition setted: " + points0.get(i));
									returnList.add(points1.get(j));
									rightFigures.get(1).setGridPosition(points1.get(j));
//									System.out.println("gridPosition setted: " + points1.get(j));
									returnList.add(points2.get(k));
									rightFigures.get(2).setGridPosition(points2.get(k));
//									System.out.println("gridPosition setted: " + points2.get(k));
									return returnList;
								}
							}
						}
					}				
				}
			}
			else if (figuresContainNeighbours(figures)) {
				
			}
			
		}
		if (figures.size() == 2) {
			
			
		}
		
		if (figures.size() == 1) {
			
		}
		return new ArrayList<Integer>();
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
	private ArrayList<Shape> getRightTriangle(ArrayList<Shape> figures) {
//		HashSet<String> keys = new HashSet<String>(figures.keySet());
		
		ArrayList<String> codes = new ArrayList<String>();
		for (Shape shape: figures) {
			codes.add(shape.getCode());
//			System.out.println(shape.getCode());
		}
		ArrayList<Shape> returnList = new ArrayList<Shape>();
		for (int i=0;i<codes.size()-2;i++) {
			for (int j = (i+1); j<codes.size()-1;j++) {
				for(int k = (j+1); k<codes.size();k++) {
					if ( isTriangle(figures.get(i).getPosition(), figures.get(j).getPosition(), figures.get(k).getPosition()) ) {
						returnList.add(figures.get(i));
						returnList.add(figures.get(j));
						returnList.add(figures.get(k));
//						System.out.println("in returnList: " );
//						for (Shape shape: returnList) {
//							System.out.println(shape.getCode());
//						}
						return returnList;
					}
				}
			}
		}
		
		System.out.println("geen driehoek gevonden");
		return null;
	}
	
	//methode dat uit een gegeven hashmap twee buren haalt.
	public ArrayList<Shape> getRightNeighbours(ArrayList<Shape> figures) {
		return new ArrayList<Shape>();
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
