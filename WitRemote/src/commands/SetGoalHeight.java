package commands;

public class SetGoalHeight extends SetGoal{

	public SetGoalHeight(int height){
		super("" + (int) height/10, "Setting new target height: " + height/10 + "cm", "hcommand.elevate" );
	}
}
