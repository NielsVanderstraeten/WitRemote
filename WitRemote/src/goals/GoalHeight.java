package goals;

public class GoalHeight extends Goal {

	private int height;
	public GoalHeight(int height){
		this.height = height;
	}
	
	public int getTargetHeight(){
		return height;
	}
	
	public void print(){
		System.out.println("Height: " + height);
	}
}
