package commands;

@Deprecated
public class SetDimensions extends Command {

	private int height, width;
	
	public SetDimensions(int width, int height){
		super(width + " " + height, "Setting up the dimension of the grid. Width: "+ width + "mm, height: " + height + "mm.", "info.dimensions");
		this.width = width;
		this.height = height;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getWidth(){
		return width;
	}
}
