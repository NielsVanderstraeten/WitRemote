package goals;

import Rooster.Vector;

public class GoalPosition extends Goal {

	public Vector position;
	
	public GoalPosition(int x, int y){
		position = new Vector(x ,y);
	}
	
	public int getX(){
		return (int) position.getX();
	}
	
	public int getY(){
		return (int) position.getY();
	}
	
	public void print(){
		System.out.println("X :" + position.getX() +", Y : " + position.getY());
	}
}
