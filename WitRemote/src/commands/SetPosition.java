package commands;

public class SetPosition extends Command{
	
	int x, y;
	
	public SetPosition(int x, int y){
		super("setposition " + x + " " + y, "Reporting position to the zeppelin. Poisition is: x=" + x + "mm, y=" + y + "mm.");
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
