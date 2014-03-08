package commands;

public abstract class Command {

	private String piCommand;
	private String guiCommand;
	
	public Command(String pi, String gui){
		this.piCommand = pi;
		this.guiCommand = gui;
	}
	
	public Command(){
		piCommand = "";
		guiCommand = "";
	}
	
	public String getPiCommand(){
		return piCommand;
	}
	
	public String getConsole(){
		return guiCommand;
	}
}
