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

	private List<Command> queue;
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
		this.queue = cm.getQueue();
		this.grid = cm.getGrid();
		this.command = command;
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
				queue.add(new SetGoalPosition(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))));
				cm.setTabletNumber(-1); //-1 betekent landen
				cm.foundQRCode();
				foundCorrectQRCode = true;
			}
			//geval tablet
			m = Pattern.compile("tablet:(\\d+)").matcher(command);
			if (!foundCorrectQRCode && m.find()) {
				Vector targetPosition = grid.getTabletPosition(Integer.parseInt(m.group(1)));
				queue.add(new SetGoalPosition((int) targetPosition.getX(), (int) targetPosition.getX()));
				cm.setTabletNumber(Integer.parseInt(m.group(1)));
				cm.foundQRCode();
				foundCorrectQRCode = true;				
			}
		
		}
		
		System.out.println("---> Decoded QRcode: " + command);
	}
}