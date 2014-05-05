package commands;

public class SetPosition extends Command{
	
	private int x, y;
	private double rotation;
	
	public SetPosition(int x, int y, double rotation){
		super(x + "," + y, "Reporting position to the zeppelin. Poisition is: x=" + x + "mm, y=" + y + "mm. Rotation is: " + rotation, "info.location");
		this.x = x;
		this.y = y;
		this.rotation = rotation;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public double getRotation(){
		return rotation;
	}
}
