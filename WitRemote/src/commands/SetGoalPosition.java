package commands;

public class SetGoalPosition extends SetGoal {
	
	private int x, y;

	public SetGoalPosition(int x, int y){
		super(x +" " + y, "Setting new target position: x=" + x +"mm, y=" +y+"mm.", "hcommand.move");
		this.x=x;
		this.y=y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
}
