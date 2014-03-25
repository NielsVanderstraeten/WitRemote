package gui;

import goals.*;

import java.util.LinkedList;

import commands.*;

public class TextParser {

	private LinkedList<Command> queue;
	private LinkedList<Goal> goals;
	private LinkedList<String> commands;
	
	public TextParser(LinkedList<Command> queue, LinkedList<Goal> goals){
		this.goals = goals;
		this.queue = queue;
		populateCommands();
	}
	
	private void populateCommands(){
		commands = new LinkedList<String>();
		commands.add("setposition");
		commands.add("addgoal");
		commands.add("addgoalposition");
		commands.add("addgoalheight");
		commands.add("getheight");
		commands.add("help");
		commands.add("text");
	}
	
	public String parse(String command){
		String returnString = "";
		if(command == null || command.equals(""))
			throw new IllegalArgumentException("Wrong agrument @ parser");
		String[] commandWords = command.split("[ ]+");
		
		if(commandWords.length == 0)
			returnString = "Please enter something else than just space.";
		else if(commandWords[0].equalsIgnoreCase(commands.get(0)))
			returnString = setPosition(commandWords);
		else if(commandWords[0].equalsIgnoreCase(commands.get(1)))
			returnString = addGoal(commandWords);
		else if(commandWords[0].equalsIgnoreCase(commands.get(2)))
			returnString = addGoalPosition(commandWords);
		else if(commandWords[0].equalsIgnoreCase(commands.get(3)))
			returnString = addGoalHeight(commandWords);
		else if(commandWords[0].equalsIgnoreCase(commands.get(4)))
			returnString = getHeight(commandWords);
		else if(commandWords[0].equalsIgnoreCase(commands.get(5)))
			returnString = help(commandWords);
		else if(commandWords[0].equalsIgnoreCase(commands.get(6)))
			returnString = text(commandWords);
		else if(commandWords[0].equalsIgnoreCase("terminate"))
			returnString = terminate();
		else
			returnString = "Command not found. Please try again.\nYou entered: " + command;

		return returnString;
	}
	
	private String setPosition(String[] command){
		if(command.length != 4)
			return "Please enter exactly 4 words. First the command then the x and y poisition on the grid in mm and finally the rotation of the zeppelin.\n"
					+ "For example: setposition 1000 2000 3.14";
		else{
			int x, y;
			double rotation;
			try{
				x = Integer.parseInt(command[1]); y = Integer.parseInt(command[2]);
				rotation = Double.parseDouble(command[3]);
				}
			catch(NumberFormatException ne){
				return "Please enter valid numbers as positions. Only positive integers are allowed.";
			}
			if(x < 0 || y < 0)
				return "Please enter positive amounts.";
			else{
				queue.add(new SetPosition(x, y, rotation));
				return "Setting the position to: x= " + x + "mm, y= " +y + "mm.";
			}
		}
	}
	
	private String addGoal(String[] command){
		return "You can add goals to the goal queue with commands.\n"+
					"You can add a position with: 'addgoalposition x y' or a height by: 'addgoalheight z'.";
	}
	
	private String addGoalPosition(String[] command){
		if(command.length != 3)
			return "Please enter exactly 3 words. First the command then the goal x and y poisition on the grid in mm.\n"
					+ "For example: addgoalposition 1000 2000";
		else{
			int x, y;
			try{
				x = Integer.parseInt(command[1]); y = Integer.parseInt(command[2]); }
			catch(NumberFormatException ne){
				return "Please enter valid numbers as positions. Only positive integers are allowed.";
			}
			if(x < 0 || y < 0)
				return "Please enter positive amounts.";
			else{
				goals.add(new GoalPosition(x, y));
				return "Adding a new goal position to: x= " + x + "mm, y= " +y + "mm.";
			}
		}
	}
	
	private String addGoalHeight(String[] command){
		if(command.length != 2)
			return "Please enter exactly 2 words. First the command then the target goal height in mm.\n"
					+ "For example: addgoalheight 1000";
		else{
			int height;
			try{
				height = Integer.parseInt(command[1]); }
			catch(NumberFormatException ne){
				return "Please enter a valid number as height. Only positive integers are allowed.";
			}
			if(height < 0)
				return "Please enter a positive amount.";
			else{
				queue.add(new SetGoalHeight(height));
				return "Adding a new goal height: height = "+ height + "mm.";
			}
		}
	}
	
	private String getHeight(String[] command){
		if(command.length != 1)
			return "Please do not enter anything after the getheight command.";
		else{
			queue.add(new GetHeight());
			return "Updating the height momentarily.";
		}
	}
	
	private String help(String[] command){
		String returnString = "NYI";
		if(command.length == 1){
			returnString = "Available commands are:";
			for(String temp: commands){
				returnString += "\n" + temp;
			}
			returnString += "\nCommands are not case sensitive. \nFor more information about each command type: help + 'command'. \n"
					+ "You can type 'help command' for more information about the commands.";
		}
		return returnString;
	}
	
	private String text(String[] command){
		if(command.length == 1)
			return "You can print text if you type 'text YOUR_TEXT_HERE'";
		String returnString = "";
		for(int i = 1; i < command.length; i++)
			returnString += command[i] + " ";
		return returnString;
	}
	
	private String terminate(){
		queue.add(new Terminate());
		return "Terminating program. Bye bye";
	}
	
}
