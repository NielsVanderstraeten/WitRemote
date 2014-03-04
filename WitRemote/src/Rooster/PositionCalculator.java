package Rooster;


public class PositionCalculator {
	
	private int x;
	private int y;
	private int width;
	private int height;
	private final int distance = 400; // in mm
	
	public PositionCalculator(int width, int height) {
		x = 0;
		y = 0;
		this.width = width;
		this.height = height;
	}
	
	public Vector calculateTriple(Vector vectorA, Vector vectorB, Vector vectorC) {
		double xcoord = (vectorA.getX() + vectorB.getX() + vectorC.getX())/3;
		double ycoord = (vectorA.getY() + vectorB.getY() + vectorC.getY())/3;
		
		return new Vector(xcoord, ycoord);
	}
	public Vector calculateTriple(int a, int b, int c) {
		double xcoord = (getX(a) + getX(b) + getX(c))/3;
		double ycoord = (getY(a) + getY(b) + getY(c))/3;
		
		return new Vector(xcoord, ycoord);
	}
	
	public Vector getVector(int point) {
		double xcoord = getX(point);
		double ycoord = getY(point);
		
		return new Vector(xcoord,ycoord);
	}
	
	public int getX(int point) {
		int line = point/width +1; 
		int column = point - (line-1) * width + 1;
		if (line%2 == 0) {
			return (distance/2) + distance*(column - 1);
		}
		else {
			return distance*(column-1);
		}
	}
	
	// Y-as naar boven gericht!!!
	public double getY2(int point) {
		int line = point/width +1;
		double totalHeight = ((height-1)*distance*Math.sqrt(3)/2);
		if (line%2 == 0) {
			return totalHeight - (line-1)*distance*Math.sqrt(3)/2;
		}
		else {
			return totalHeight - (line-1)*distance*Math.sqrt(3)/2;
		}
	}
	
	// Y-as naar beneden gericht!!!
	public double getY(int point) {
		int line = point/width +1;
		if (line%2 == 0) {
			return (line-1)*distance*Math.sqrt(3)/2;
		}
		else {
			return (line-1)*distance*Math.sqrt(3)/2;
		}
	}
	
}
