package commands;

public class SetPosition extends Command{
	

	public SetPosition(int x, int y){
		super("setposition " + x + " " + y, "Reporting position to the zeppelin. Poisition is: x=" + x + "mm, y=" + y + "mm.");
	}
}
