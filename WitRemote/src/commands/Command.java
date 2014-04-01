package commands;

public abstract class Command {

	private String piCommand;
	private String guiCommand;
	private String topic;
	
	public Command(String pi, String gui, String topic){
		this.piCommand = pi;
		this.guiCommand = gui;
		this.topic = topic;
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
	
	public String getTopic(){
		return topic;
	}
}
