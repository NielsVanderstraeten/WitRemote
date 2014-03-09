package Rooster;

public class Shape {
	private Vector position;
	private String code;
	
	public Shape(double xcoord, double ycoord, String code) {
		position = new Vector(xcoord, ycoord);
		this.code = code;
	}
	
	public Vector getPosition() {
		return position;
	}
	
	public String getCode() {
		return code;
	}
}
