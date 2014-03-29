package Rooster;


public class PositionCalculator {
	
//	private int x;
//	private int y;
	private int width;
	private int height;
	private final int distance = 400; // in mm
	private final boolean startLeft;
	private final Vector pictureSize;
	private final Vector pictureMiddle;
	
	public PositionCalculator(int width, int height, boolean startLeft, Vector pictureSize) {
//		x = 0;
//		y = 0;
		this.width = width;
		this.height = height;
		this.startLeft = startLeft;
		this.pictureSize = pictureSize;
		this.pictureMiddle = new Vector(pictureSize.getX()/2, pictureSize.getY()/2);
	}
	
	//returns a vector that is the center of three given vectors
	public Vector calculateTriple(Vector vectorA, Vector vectorB, Vector vectorC) {
		double xcoord = (vectorA.getX() + vectorB.getX() + vectorC.getX())/3;
		double ycoord = (vectorA.getY() + vectorB.getY() + vectorC.getY())/3;
		
		return new Vector(xcoord, ycoord);
	}
	
	//returns a vector that is the center of three given points (position in the arraylist that describes the grid)
	public Vector calculateTriple(int a, int b, int c) {
		double xcoord = (getX(a) + getX(b) + getX(c))/3;
		double ycoord = (getY(a) + getY(b) + getY(c))/3;
		
		return new Vector(xcoord, ycoord);
	}
	
	//returns the vector that is the center of two given vectors
	public Vector calculateDouble(Vector vectorA, Vector vectorB) {
		double xcoord = (vectorA.getX() + vectorB.getX())/2;
		double ycoord = (vectorA.getY() + vectorB.getY())/2;
		
		return new Vector(xcoord, ycoord);
	}
	
	//returns a vector that is the center of tow given points (position in the arraylist that describes the grid)
	public Vector calculateDouble(int a, int b) {
		double xcoord = (getX(a) + getX(b))/2;
		double ycoord = (getY(a) + getY(b))/2;
		
		return new Vector(xcoord, ycoord);
	}
	
	//returns the positionvector of a given point from the grid.
	public Vector getVector(int point) {
		double xcoord = getX(point);
		double ycoord = getY(point);
		
		return new Vector(xcoord,ycoord);
	}
	
	public Vector getPictureMiddle(Vector vector1, int point1, Vector vector2, int point2) {
		double delta1to2Picture = vector1.getDistance(vector2);
		double delta1to2Real = getVector(point1).getDistance(getVector(point2));
		double delta1toMiddlePicture = vector1.getDistance(pictureMiddle);
				
		double realDistance = delta1to2Real/delta1to2Picture*delta1toMiddlePicture;
		
		double deltaAngle = vector2.getAngle(vector1)-getVector(point2).getAngle(getVector(point1));
		double angleMiddle = pictureMiddle.getAngle(vector1);
		//rotation staat in radialen!
		//graden omzetten naar radialen! 
		double totalAngle = (angleMiddle-deltaAngle + 720)%360*Math.PI/180;
		double middleX = getVector(point1).getX() + realDistance*Math.cos(totalAngle);
		double middleY = getVector(point1).getY() + realDistance*Math.sin(totalAngle);
		
		return new Vector(middleX, middleY);
	}
	
	// X-as naar rechts gericht!!
	public int getX(int point) {
		int line = point/width +1;
		boolean evenLine = (line%2 == 0);		 
		int column = point - (line-1) * width + 1;
		if (startLeft == evenLine) {
			return (distance/2) + distance*(column - 1);
		} else {
			return distance*(column-1);
		}		
	}
	
	// Y-as naar beneden gericht!!!
	public double getY(int point) {
		int line = point/width +1;
		return (line-1)*distance*Math.sqrt(3)/2;
	}
	
	// Y-as naar boven gericht!!!
		public double getY2(int point) {
			int line = point/width +1;
			double totalHeight = ((height-1)*distance*Math.sqrt(3)/2);
			return totalHeight - (line-1)*distance*Math.sqrt(3)/2;
		}
	
}
