package Rooster;


public class Vector {
	
	private double x;
	private double y;
	
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getDistance(Vector vector) {
		return Math.sqrt(Math.pow(this.getX()-vector.getX(), 2) + Math.pow(this.getY()-vector.getY(), 2));
	}
	
	// afwijking ten opzichte van horizontale as naar rechts en verticale as naar onder in GRADEN!
	public double getAngle(Vector vector) {
		double deltaX = (this.getX() - vector.getX());
		double deltaY = (this.getY() - vector.getY());
		double slope = deltaY/deltaX;
		double deviation;
		
		if (deltaX == 0) {
			if (deltaY >= 0) {
				deviation = Math.PI/2;
			} else {
				deviation = 3*Math.PI/2;
			}
		}
		else if (deltaX > 0) {
			//deviation = Math.PI/2 - Math.atan(slope);
			deviation = Math.atan(slope);
			System.out.println(deviation);
		}
		else {
			//deviation = -(Math.PI/2 + Math.atan(slope));
			deviation = Math.atan(slope) + Math.PI;
		}
		deviation = deviation/Math.PI*180 + 360;
		return (deviation%360);
	}
	
	public boolean positiveX(Vector vector) {
		boolean positive;
		if ((vector.getX() - this.getX()) < 0) {
			positive = false;
		} else {
			positive = true;
		}
		return positive;
	}
	
	public String toString() {
		return (getX() + ", " + getY());
	}
}
