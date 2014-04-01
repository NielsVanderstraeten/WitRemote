import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import commands.*;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.*;

/**
 * Klasse die een QR-code inleest uit een opgegeven pad, die verwerkt en opsplitst in verschillende
 * basiscommando's en die toevoegt aan een opgegeven queue. 
 * 
 * @author Niels Vanderstraeten
 *
 */

public class QRcode implements Runnable {
	
	private boolean foundCorrectQRCode;
	
	private Result[] QRcodes;
	private String CorrectQRCode;
	private LinkedList<Command> queue;
	private ControlManager cm;

	/**
	 * Constructor die eveneens onmiddellijk de in te lezen afbeelding verwerkt naar tekst.
	 * 
	 * @param imagePath
	 * 			Plaats waar het in te lezen bestand zich bevindt. Dit kan nog aangepast worden naar een inputstream van de Pi
	 * @param queue
	 * 			Het object van de CommandsQueue-klasse waar alle commando's verzameld zullen worden
	 */
	//public QRcode(ControlManager cm, LinkedList<Command> queue, PiState pistate, String imagePath, int vereistVolgnummer) {
	public QRcode(ControlManager cm, LinkedList<Command> queue, String imagePath) {
		this.cm = cm;
		this.queue = queue;
		
		try {
			File file = new File(imagePath);
			
			BufferedImage in = ImageIO.read(file);
			
			QRCodeMultiReader reader = new QRCodeMultiReader();

			BinaryBitmap image = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(
					in)));
			
			//Toegevoegd om analyse van QR-code te verbeteren
		    Hashtable<DecodeHintType, Object> hint = new Hashtable<DecodeHintType, Object>();
		    hint.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		    
			QRcodes = reader.decodeMultiple(image, hint);
			System.out.println("-> "+QRcodes.length+" QRcodes found.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("-> No QRcode found.");
			CorrectQRCode = "No QRcode found.";
		}
	}
	
	/**
	 * Methode die de ingelezen QR-code opsplitst in commando's en die toevoegt aan de queue
	 */
	public synchronized void run() {		//TODO: synchronised?
		
		foundCorrectQRCode = false;
		ArrayList<Command> toAddToQueue = new ArrayList<Command>();

		if (!(QRcodes == null)) {

			for (int i = 0; i < QRcodes.length && foundCorrectQRCode == false; i++) {

				String QRcode = QRcodes[i].getText();

				List<String> strings = Arrays.asList(QRcode.split("\\s*;\\s*"));

				toAddToQueue = new ArrayList<Command>();

				for (String s : strings) {
					Matcher m;
//					Command c;
					boolean found = false;

					m = Pattern.compile("S:(\\d*)").matcher(s);
					if (m.find()) {
//						c = new Climb(Integer.parseInt(m.group(1))); //Groep 1 = getal x in "S:x"
//						toAddToQueue.add(c);
						found = true;
					}

					m = Pattern.compile("D:(\\d*)").matcher(s);
					if (!found && m.find()) {
//						c = new Descend(Integer.parseInt(m.group(1)));
//						toAddToQueue.add(c);
						found = true;
					}

					m = Pattern.compile("V:(\\d*)").matcher(s);
					if (!found && m.find()) {
//						c = new Forward((long) (TIJD_PER_CM_VOORUIT * Integer.parseInt(m.group(1))));
//						toAddToQueue.add(c);
						found = true;
					}

					m = Pattern.compile("A:(\\d*)").matcher(s);
					if (!found && m.find()) {
//						c = new Backward((long) (TIJD_PER_CM_ACHTERUIT * Integer.parseInt(m.group(1))));
//						toAddToQueue.add(c);
						found = true;
					}

					m = Pattern.compile("L:(\\d*)").matcher(s);
					if (!found && m.find()) {
//						c = new TurnLeft((int) (TIJD_PER_GRADEN_LINKS * Integer.parseInt(m.group(1))));
//						toAddToQueue.add(c);
						found = true;
					}

					m = Pattern.compile("R:(\\d*)").matcher(s);
					if (!found && m.find()) {
//						c = new TurnRight((int) (TIJD_PER_GRADEN_RECHTS * Integer.parseInt(m.group(1))));
//						toAddToQueue.add(c);
						found = true;
					}		

					//Volgnummer
					m = Pattern.compile("N:(\\d*)").matcher(s);
					if (!found && m.find()) {
						CorrectQRCode = QRcode;
						foundCorrectQRCode = true;
						found = true;
					}	
				}
			}
		} else
			CorrectQRCode = null;
		
		if (foundCorrectQRCode){
			//TODO boodschap decrypten
			//TODO: nieuwe doellocatie toevoegen aan queue (queue.add(command))

			cm.foundQRCode();
			//cm.increaseVolgnummer();
			//System.out.println("-> Volgnummer wordt verhoogd naar " + (vereistVolgnummer + 1));
		} 
		System.out.println("---> Gevonden QRcode: " +CorrectQRCode);
	}
	
	public boolean foundCorrectQRCode() {
		return foundCorrectQRCode;
	}
	
	public String getCommandString(){
		String returnString = "";
		for(Command c: queue)
			returnString += c.getConsole();
		return returnString;
	}
	
}