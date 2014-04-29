import java.awt.image.BufferedImage;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import Rooster.Grid;
import Rooster.Vector;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import commands.Command;
import commands.SetGoalPosition;

/**
 * Klasse die een QR-code inleest uit een opgegeven pad, die verwerkt en opsplitst in verschillende
 * basiscommando's en die toevoegt aan een opgegeven queue. 
 * 
 * @author Niels Vanderstraeten
 *
 */

public class QRcode implements Runnable {

	private static PublicKey publicKey;
	private static PrivateKey privateKey;

	private Result QRcode;
	private String QRcodeString;
	private List<Command> queue;
	private ControlManager cm;
	private Grid grid;

	/**
	 * Constructor die eveneens onmiddellijk de in te lezen afbeelding verwerkt naar tekst.
	 * 
	 * @param imagePath
	 * 			Plaats waar het in te lezen bestand zich bevindt. Dit kan nog aangepast worden naar een inputstream van de Pi
	 * @param queue
	 * 			Het object van de CommandsQueue-klasse waar alle commando's verzameld zullen worden
	 */
	public QRcode(ControlManager cm, String imagePath) {
		this.cm = cm;
		this.queue = cm.getQueue();
		this.grid = cm.getGrid();

		try {
			File file = new File(imagePath);

			BufferedImage in = ImageIO.read(file);

			QRCodeReader reader = new QRCodeReader();

			BinaryBitmap image = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(
					in)));

			//Toegevoegd om analyse van QR-code te verbeteren
			Hashtable<DecodeHintType, Object> hint = new Hashtable<DecodeHintType, Object>();
			hint.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

//			QRcode = reader.decodeMultiple(image, hint);
			QRcode = reader.decode(image, hint);
		} catch (Exception e) {
			e.printStackTrace();
			QRcodeString = "None.";
		}
	}

	/**
	 * Methode die de ingelezen QR-code opsplitst in commando's en die toevoegt aan de queue
	 */
	public synchronized void run() {
		boolean foundCorrectQRCode = false;

		if (QRcode != null) {
			QRcodeString = decrypt(QRcode.getText());		
			Matcher m;
			
			//geval position
			m = Pattern.compile("position:(\\d+),(\\d+)").matcher(QRcodeString);
			if (m.find()) {
				queue.add(new SetGoalPosition(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))));
				cm.setTabletNumber(-1); //-1 betekent landen
				cm.foundQRCode();
				foundCorrectQRCode = true;
			}
			//geval tablet
			m = Pattern.compile("tablet:(\\d+)").matcher(QRcodeString);
			if (!foundCorrectQRCode && m.find()) {
				Vector targetPosition = grid.getTabletPosition(Integer.parseInt(m.group(1)));
				queue.add(new SetGoalPosition((int) targetPosition.getX(), (int) targetPosition.getX()));
				cm.setTabletNumber(Integer.parseInt(m.group(1)));
				cm.foundQRCode();
				foundCorrectQRCode = true;				
			}
		
		}
		
		System.out.println("---> Gevonden QRcode: " + QRcodeString);
	}

//	public String getCommandString(){
//		String returnString = "";
//		for(Command c: queue)
//			returnString += c.getConsole();
//		return returnString;
//	}
	
	public static void initialiseKeys() {
		if (privateKey != null && publicKey != null)
			return;
		
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		keyGen.initialize(1024);
		final KeyPair key = keyGen.generateKeyPair();

		publicKey = key.getPublic();
		privateKey = key.getPrivate();
	}
	
	public static String getPublicKeyString() {
		if (publicKey == null && privateKey == null)
			initialiseKeys();
		
		return Base64.encodeBase64String(publicKey.getEncoded());
	}
	
	//Gebruikt voor test
	static PublicKey getPublicKey() {
		if (publicKey == null && privateKey == null)
			initialiseKeys();
		
		return publicKey;
	}
	
	//Gebruikt voor test
	static PrivateKey getPrivateKey() {
		if (publicKey == null && privateKey == null)
			initialiseKeys();
		
		return privateKey;
	}

	private String decrypt(String text64) {
		//TODO: kijken welke String-codering de scheidsrechtercommissie beslist
		byte[] text = Base64.decodeBase64(text64);		
		byte[] decryptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA");
			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, privateKey);	      
			decryptedText = cipher.doFinal(text);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(decryptedText);
	}


}