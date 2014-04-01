package commands;

public class SetGoalHeight extends SetGoal{

	public SetGoalHeight(int height){
		super("" + height, "Setting new target height: " + height/10 + "cm", "hcommand.elevate" );
	}
}
