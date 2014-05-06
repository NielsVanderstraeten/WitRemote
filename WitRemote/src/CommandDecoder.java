import goals.Goal;
import goals.GoalPosition;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Rooster.Grid;
import Rooster.Vector;

import commands.Command;
import commands.SetGoalPosition;

/**
 * Klasse die een QR-code inleest uit een opgegeven pad, die verwerkt en opsplitst in verschillende
 * basiscommando's en die toevoegt aan een opgegeven queue. 
 * 
 * @author Niels Vanderstraeten
 *
 */

public class CommandDecoder {

	private List<Goal> goals;
	private ControlManager cm;
	private Grid grid;
	private String command;

	/**
	 * Constructor die eveneens onmiddellijk de in te lezen afbeelding verwerkt naar tekst.
	 * 
	 * @param imagePath
	 * 			Plaats waar het in te lezen bestand zich bevindt. Dit kan nog aangepast worden naar een inputstream van de Pi
	 * @param queue
	 * 			Het object van de CommandsQueue-klasse waar alle commando's verzameld zullen worden
	 */
	public CommandDecoder(ControlManager cm, String command) {
		this.cm = cm;
		this.grid = cm.getGrid();
		this.goals = cm.getGoals();
		this.command = command;
	}
	
	//Voor tests, wordt niet gebruikt
	@Deprecated
	public CommandDecoder(List<Goal> goals, Grid grid, String command) {
		this.goals = goals;
		this.command = command;
		this.grid = grid;
	}

	/**
	 * Methode die de ingelezen QR-code opsplitst in commando's en die toevoegt aan de queue
	 */
	public void decodeCommand() {
		boolean foundCorrectQRCode = false;

		if (! command.isEmpty()) {		
			Matcher m;
			
			//geval position
			m = Pattern.compile("position:(\\d+),(\\d+)").matcher(command);
			if (m.find()) {
				System.out.println("---> Decoded QRcode: position:"+Integer.parseInt(m.group(1))+","+Integer.parseInt(m.group(2)));
				if (cm != null) {
					//queue.add(new SetGoalPosition(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))));
					cm.setTabletNumber(-1); //-1 betekent landen
					cm.foundQRCode();
					System.out.println("Found correct QR code!"); //TODO debug
				}
				
				goals.add(new GoalPosition(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))));
				
				foundCorrectQRCode = true;
			}
			//geval tablet
			m = Pattern.compile("tablet:(\\d+)").matcher(command);
			if (!foundCorrectQRCode && m.find()) {
				System.out.println("---> Decoded QRcode: tablet:" + Integer.parseInt(m.group(1)));	
				Vector targetPosition = grid.getTabletPosition(Integer.parseInt(m.group(1)));
				
				goals.add(new GoalPosition((int) targetPosition.getX(), (int) targetPosition.getY()));
				
				if (cm != null) {
					cm.setTabletNumber(Integer.parseInt(m.group(1)));
					cm.foundQRCode();
					System.out.println("Found correct QR code!"); //TODO debug
				}
				foundCorrectQRCode = true;				
			}
		
		}	
	}
}