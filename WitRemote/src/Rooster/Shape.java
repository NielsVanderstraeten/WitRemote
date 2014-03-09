package Rooster;



public class Shape {
	private Vector position;
	private String code;
	private int gridPosition;
	
	public Shape(double centerXCoord, double centerYCoord, String code) {
		this.position = new Vector(centerXCoord, centerYCoord);
		this.code = code;
	}
	
	public Shape(Vector center, String color, String shape){
		this.position = center;
		this.code = getCode(color, shape);
	}
	
	private String getCode(String color, String shape) {
		return color.substring(0,1) + shape.substring(0, 1);
	}

	public Vector getPosition() {
		return position;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setGridPosition(int position) {
		gridPosition = position;
	}
	
	public int getGridPosition() {
		return gridPosition;
	}
}
