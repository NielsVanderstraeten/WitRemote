package commands;

public class SetGoalHeight extends SetGoal{

	public SetGoalHeight(int height){
		super("gotoheight " + height, "Setting new target height: " + height + "mm");
	}
}
