package commands;

public class SetGoalHeight extends SetGoal{

	public SetGoalHeight(int height){
		super("setgoalheight " + height/10, "Setting new target height: " + height/10 + "cm");
	}
}
