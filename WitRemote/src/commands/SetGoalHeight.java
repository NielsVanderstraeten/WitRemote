package commands;

public class SetGoalHeight extends SetGoal{
	
	private int height;

	public SetGoalHeight(int height){
		super("" + height, "Setting new target height: " + height/10 + "cm", "hcommand.elevate" );
		this.height=height;
	}
	
	public int getHeight() {
		return height;
	}
}
