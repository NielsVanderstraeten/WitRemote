import goals.Goal;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import Rooster.Grid;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class QRcode implements Runnable {
	
	private ControlManager cm;
	private Grid grid;
	private List<Goal> goals;
	private String imagePath;
	private static boolean initialisedKeys = false;
	
	public QRcode(ControlManager cm, String imagePath) {
		this.cm = cm;
		this.imagePath = imagePath;
	}
	
	public QRcode(List<Goal> goals, Grid grid, String imagePath) {
		this.goals = goals;
		this.grid = grid;
		this.imagePath = imagePath;
	}
	
	public static void initialiseKeys() {
		if (initialisedKeys)
			return;
		
		try {
			Process tr = Runtime.getRuntime().exec("cmd /c C:/Python27/python.exe keys.py");
			tr.waitFor();
			initialisedKeys = true;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static String getPublicKey() {
		if (! initialisedKeys)
			initialiseKeys();
		
		return read("public");
		//SEND KEY TO RABBITMQ
	}

	
	public synchronized void run(){	
		long start = System.currentTimeMillis();
		
		//READ QR CODE
		String text = "";
		
		//READ QR CODE
		try {
			text = readQR(); //text = ingelezen, geëncrypteerde boodschap
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		write("encrypted", text);
		
		try {
			Process tr = Runtime.getRuntime().exec("cmd /c C:/Python27/python.exe decription.py");
			tr.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		String command = read("result");
		
		if (cm != null) {
			CommandDecoder decoder = new CommandDecoder(cm, command);
			decoder.decodeCommand();
		}
		if (goals != null && grid != null) {
			CommandDecoder decoder = new CommandDecoder(goals, grid, command);
			decoder.decodeCommand();
		}
		
		cm.getGUI().printToConsole(command);
		cm.getGUI().updatePhoto(imagePath);
		System.out.println("-> Time QR: " + (System.currentTimeMillis() - start));
	}
	
	
	public static String read(String fileName){
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			result = sb.toString();
			br.close();
		} catch(IOException e) {
		}
		return result;
	}
	
	public static void write(String fileName, String text){
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.println(text);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
		}
	}
	
	private String readQR() throws IOException {
		File file = new File(imagePath);

		BufferedImage in = ImageIO.read(file);

		QRCodeReader reader = new QRCodeReader();

		BinaryBitmap image = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(
				in)));

		//Toegevoegd om analyse van QR-code te verbeteren
		Hashtable<DecodeHintType, Object> hint = new Hashtable<DecodeHintType, Object>();
		hint.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

		try {
			return reader.decode(image, hint).getText();
		} catch (NotFoundException | ChecksumException | FormatException e) {
			e.printStackTrace();
			return "";
		}
	}
	
//	private void waitNow() {
//		long start = System.currentTimeMillis();
//		while (System.currentTimeMillis() - start < 500)
//			;
//	}
}
