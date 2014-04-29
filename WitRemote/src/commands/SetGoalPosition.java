package commands;

public class SetGoalPosition extends SetGoal {

	public SetGoalPosition(int x, int y){
		super(x +"," + y, "Setting new target position: x=" + x +"mm, y=" +y+"mm.", "hcommand.move");
	}
}
