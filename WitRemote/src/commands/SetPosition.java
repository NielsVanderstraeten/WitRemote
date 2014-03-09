package commands;

public class SetPosition extends Command{
	
	private int x, y;
	private double rotation;
	
	public SetPosition(int x, int y, double rotation){
		super("setposition " + x + " " + y +" " + rotation, "Reporting position to the zeppelin. Poisition is: x=" + x + "mm, y=" + y + "mm. Rotation is: " + rotation);
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
