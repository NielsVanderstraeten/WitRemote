

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
	
	// afwijking ten opzichte van horizontale as naar rechts in radialen!
	public double getAnglePicture(Vector vector) {
		double deltaX = (vector.getX() - this.getX());
		double deltaY = (vector.getY() - this.getY());
		double slope = deltaX/deltaY;
		double deviation;
		if (deltaX > 0) {
			deviation = Math.PI/2 - Math.atan(slope);
		}
		else {
			deviation = -(Math.PI/2 + Math.atan(slope));
		}
		return deviation;
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
}
