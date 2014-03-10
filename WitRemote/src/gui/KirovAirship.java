package gui;

import goals.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import commands.*;

public class KirovAirship extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLayeredPane totalPane;
	//Height en width van het scherm
	private final int height, width;
	//Coordinaten worden bijgehouden in millimeter.
	private final int heightMeters, widthMeters;
	public LinkedList<Command> queue;
	private LinkedList<Goal> goals;
	
	public KirovAirship(int width, int height, int heightMeters, int widthMeters, LinkedList<Command> queue, LinkedList<Goal> goals){
		if(height > 0)
			this.height = height;
		else throw new IllegalArgumentException("Height smaller than zero.");
		if(width > 0)
			this.width = width;
		else throw new IllegalArgumentException("Width smaller than zero.");
		if(heightMeters > 0)
			this.heightMeters = heightMeters;
		else throw new IllegalArgumentException("Height(real) smaller than zero.");
		if(widthMeters > 0)
			this.widthMeters = widthMeters;
		else throw new IllegalArgumentException("Width(real) smaller than zero.");
		
		this.queue = queue;
		this.goals = goals;
		this.parser = new TextParser(this.queue, this.goals);
		totalPane = new JLayeredPane();
		totalPane.setOpaque(true);
		totalPane.setBackground(new Color(193,193,193));
		totalPane.setBounds(0, 0, width - 16, height - 18);
		getContentPane().add(totalPane);
		addKeyListener(new EventKey());
		
		setUpConsole();
		setUpMission();
		setUpInformation();
		setUpPhoto();
		
		//Map moet laatste
		setUpMap();
	}
	
	public KirovAirship(LinkedList<Command> queue, LinkedList<Goal> goals){
		this(1280, 780, 4000, 4000, queue, goals);
	}
	
	public KirovAirship(){
		this(new LinkedList<Command>(), new LinkedList<Goal>());
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getWidth(){
		return width;
	}
	
	private ImageIcon getImageIcon(String source, int width, int height){
		assert (source != null && width > 0 && height > 0);
		BufferedImage srcImg = null;
		try{
			srcImg = ImageIO.read(new File(source));
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("Cannot find image or invalid resource type.");
		}
		BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, width, height, null);
		g2.dispose();
		return new ImageIcon(resizedImg);
	}
	
	
	private TextParser parser;
	/**
	 * De methode die de text in de console pakt en doorstuurt naar de parser.
	 * Deze methode scrollt ook naar beneden en reset de input.
	 * @param text
	 */
	private void textEntered(String text){
		if(text != null && text != ""){
			String printer = parser.parse(text);
			outputConsole.append(printer + "\n");
			inputConsole.setText("");
			outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
		}
	}
	
	public static void main(String[] args){
		KirovAirship gui = new KirovAirship();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("Kirov Airship Reporting.");
		gui.setSize(gui.getWidth(), gui.getHeight());
		gui.setVisible(true);
		gui.requestFocus();
	}
	
	private JLayeredPane consolePane;
	private JScrollPane consoleScroller;
	private JTextArea outputConsole;
	private JTextField inputConsole;
	private JButton consoleButton;
	
	/**
	 * Deze methode maakt de input en outputconsole aan. Hij maakt ook een enterknop aan naast de input.
	 * Dit zorgt er ook voor dat er kan gescrolld worden in de tekst.
	*/
	private void setUpConsole(){
		consolePane = new JLayeredPane();
		consolePane.setLocation(10, 422);
		consolePane.setSize(358, 309);
		totalPane.add(consolePane);
		
		consoleScroller = new JScrollPane();
		consoleScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		consoleScroller.setBounds(0, 0, consolePane.getWidth(), consolePane.getHeight()-31);
		consolePane.add(consoleScroller);
		
		outputConsole = new JTextArea();
		outputConsole.setFont(new Font("Tahoma", Font.PLAIN, 13));
		outputConsole.setFocusable(false);
		outputConsole.setEditable(false);
		outputConsole.setLineWrap(true);
		consoleScroller.setViewportView(outputConsole);
		
		inputConsole = new JTextField();
		inputConsole.setBounds(0, consolePane.getHeight()-20, consolePane.getWidth()-104, 20);
		inputConsole.addKeyListener(new EventKey());
		consolePane.add(inputConsole);
		
		consoleButton = new JButton("Enter");
		consoleButton.setOpaque(false);
		consoleButton.setBackground(Color.WHITE);
		consoleButton.setBounds(consolePane.getWidth()-100, consolePane.getHeight()-20,100,20);
		consolePane.add(consoleButton);
		consoleButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				textEntered(inputConsole.getText());
			}
		});
		
	}
	
	private JLayeredPane missionPane;
	private JLabel missionText;
	
	/**
	 * Deze methode zet het panel op waarin de missietekst zal staan.
	 */
	private void setUpMission(){
		missionPane = new JLayeredPane();
		missionPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		missionPane.setBounds(10, 10, 358, 29);
		totalPane.add(missionPane);
		
		missionText = new JLabel();
		missionText.setBounds(0,0,missionPane.getWidth(), missionPane.getHeight());
		missionText.setHorizontalAlignment(SwingConstants.CENTER);
		missionText.setBackground(new Color(224,224,224));
		missionText.setOpaque(true);
		missionPane.add(missionText);
		
		missionText.setText("This will state the mission to execute.");
	}
	
	private JLayeredPane mapPane;
	private MapMaker mapMaker;
	
	/**
	 * Deze methode zet de map op. Hij genereert een MapMaker die dan het veld genereert.
	 */
	private void setUpMap(){
		mapPane = new JLayeredPane();
		mapPane.setBackground(new Color(175,123,167));
		mapPane.setOpaque(true);
		mapPane.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		mapPane.setBounds(378, 10, 876, 721);
		totalPane.add(mapPane);
		
		//TODO Dit wegdoen
		int colums = 9; int rows = 9;
		String[] code = testcode(rows, colums);
		
		mapMaker = new MapMaker(mapPane.getWidth() - 8, mapPane.getHeight() - 8);
		//mapMaker = new MapMaker(mapPane.getWidth() - 8, mapPane.getHeight() - 8, 9, 9, code);
		mapMaker.setBounds(4, 4, mapMaker.getWidth(), mapPane.getHeight());
		mapPane.add(mapMaker);
		mapMaker.addMouseListener(new ZeppelinMouse());
		
		//TODO wegdoen
		updateOwnPosition((int) (widthMeters*0.1), 	(int) (heightMeters*0.1), 0);
		updateOpponentPosition((int) (widthMeters*0.9), (int) (heightMeters*0.9));
		updateGui();
	}

	private JLayeredPane informationPane;
	private JLabel targetHeightLabel, currentHeightLabel, ownXPosLabel, ownYPosLabel, opponentXPosLabel, opponentYPosLabel;
	
	private void setUpInformation(){
		informationPane = new JLayeredPane();
		informationPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		informationPane.setBounds(10, 50, 358, 72);
		informationPane.setBackground(new Color(145,145,145));
		informationPane.setOpaque(true);
		totalPane.add(informationPane);
		informationPane.setLayout(new GridLayout(3, 5, 0, 0));
		
		// Eerst rij
		JLabel emptyLabel = new JLabel("");
		informationPane.add(emptyLabel);
		
		JLabel heightTextLabel = new JLabel("Height (cm)");
		heightTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(heightTextLabel);
		
		JLabel positionTextLabel = new JLabel("Position");
		positionTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(positionTextLabel);
		
		JLabel xTextLabel = new JLabel("X");
		xTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		xTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(xTextLabel);
		
		JLabel yTextLabel = new JLabel("Y");
		yTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		yTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(yTextLabel);
		
		//Tweede rij
		JLabel currentTextLabel = new JLabel("Current");
		currentTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(currentTextLabel);
		
		currentHeightLabel = new JLabel("/");
		informationPane.add(currentHeightLabel);
		
		JLabel thisTextLabel = new JLabel("You");
		thisTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(thisTextLabel);
		
		ownXPosLabel = new JLabel("/");
		ownXPosLabel.setHorizontalAlignment(SwingConstants.CENTER);
		informationPane.add(ownXPosLabel);
		
		ownYPosLabel = new JLabel("/");
		ownYPosLabel.setHorizontalAlignment(SwingConstants.CENTER);
		informationPane.add(ownYPosLabel);
		
		//Derde rij
		JLabel targetTextLabel = new JLabel("Target");
		targetTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(targetTextLabel);
		
		targetHeightLabel = new JLabel("/");
		informationPane.add(targetHeightLabel);
		
		JLabel opponentTextLabel = new JLabel("Opponent");
		opponentTextLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		informationPane.add(opponentTextLabel);
		
		opponentXPosLabel = new JLabel("/");
		opponentXPosLabel.setHorizontalAlignment(SwingConstants.CENTER);
		informationPane.add(opponentXPosLabel);
		
		opponentYPosLabel = new JLabel("/");
		opponentYPosLabel.setHorizontalAlignment(SwingConstants.CENTER);
		informationPane.add(opponentYPosLabel);
	}
	
	private JLayeredPane photoPane;
	private JLabel photoLabel;
	private void setUpPhoto(){
		photoPane = new JLayeredPane();
		photoPane.setLocation(10, 133);
		photoPane.setSize(358, 278);
		totalPane.add(photoPane);
		
		photoLabel = new JLabel();
		photoLabel.setSize(photoPane.getWidth(), photoPane.getHeight());
		photoPane.add(photoLabel);
		
		updatePhoto();
	}

	private void moveZeppelins(){
		mapMaker.moveOwnZeppelin(ownX * mapMaker.getWidth() / widthMeters, ownY * mapMaker.getHeight() / heightMeters);
		mapMaker.rotateOwnZeppelin(ownRotation);
		mapMaker.moveOppZeppelin(opponentX * mapMaker.getWidth() / widthMeters, opponentY * mapMaker.getHeight() / heightMeters);
		repaint();
	}
	
	public void updateGui(){
		moveZeppelins();
		updatePhoto();
	}
	
	private int ownX, ownY;
	private double ownRotation = 0;
	public void updateOwnPosition(int x, int y, double rotation){
		ownX = x; 
		ownY = y;
		ownRotation = rotation;
		ownXPosLabel.setText(x+" mm"); ownYPosLabel.setText(y+" mm");
	}
	
	private int opponentX, opponentY;
	public void updateOpponentPosition(int x, int y){
		opponentX = x; 
		opponentY = y;
		opponentXPosLabel.setText(x+" mm"); opponentYPosLabel.setText(y+" mm");
	}
	
	public int getOwnX() {
		return ownX;
	}

	public int getOwnY() {
		return ownY;
	}

	public double getOwnRotation(){
		return ownRotation;
	}
	
	public int getOpponentX() {
		return opponentX;
	}

	public int getOpponentY() {
		return opponentY;
	}
	
	public void updateLastCommand(String command){
		outputConsole.append(command + "\n");
		outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
	}
	
	public void updateZeppHeight(int newheight){
		zeppHeight = newheight;
		currentHeightLabel.setText(newheight + "mm");
	}
	
	public void updatePhoto(){
		ImageIcon photo = getImageIcon("C:/analyse.png", photoLabel.getWidth(), photoLabel.getHeight());
		photoLabel.setIcon(photo);
	}
	
	int zeppHeight;
	public int getZeppHeight(){
		return zeppHeight;
	}
	
	public void setTargetHeight(int height){
		targetHeightLabel.setText(height + "mm");
		missionText.setText("Change height to: "+ height+"mm.");
	}
	
	public void setGoalPosition(int x, int y){
		missionText.setText("We have to go to: " + x + "mm, " + y +"mm");
	}
	//TODO Testcode: wegdoen uiteindelijk
	private String[] testcode(int rows, int colums){
		String[] code = new String[rows*colums];
		int random;
		for(int i = 0; i < rows; i++){
			for(int j = 0; j<colums; j++){
				random = (int )(Math.random() * 5 + 1);
				if(random == 1)
					code[i*colums + j] = "G";
				else if(random == 2)
					code[i*colums + j] = "R";
				else if(random == 3)
					code[i*colums + j] = "Y";
				else if(random == 4)
					code[i*colums + j] = "B";
				else if(random == 5)
					code[i*colums + j] = "W";
				
				random = (int )(Math.random() * 4 + 1);
				if(random == 1)
					code[i*colums + j] += "R";
				else if(random == 2)
					code[i*colums + j] += "C";
				else if(random == 3)
					code[i*colums + j] += "S";
				else if(random == 4)
					code[i*colums + j] += "H";
				
				if(j != colums - 1)
					code[i*colums + j] += ", ";
				else
					code[i*colums + j] += "\n";
			}
		}
		//Dit is om een nieuw grid af te printen zodat je deze in de map.csv kan zetten.
//		String test = "";
//		for(int i = 0; i < code.length; i++)
//			test += code[i];
//		System.out.println(test);
		
		return code;
	}
	
	//TODO dit weghalen, of toch alleen doel bijhouden.
	private class ZeppelinMouse implements MouseListener{
		@Override
		public void mousePressed(MouseEvent e){
			int x = (int) e.getX();
			int y = (int) e.getY();
			if(SwingUtilities.isLeftMouseButton(e) ){
				goals.offer(new GoalPosition(x, y));
			}
			else if(SwingUtilities.isRightMouseButton(e) ){
				updateOwnPosition(x * widthMeters / mapMaker.getWidth(), y * heightMeters / mapMaker.getHeight(), ownRotation);
			}
			else if(SwingUtilities.isMiddleMouseButton(e) ){
				updateOpponentPosition(x * widthMeters / mapMaker.getWidth(), y * heightMeters / mapMaker.getHeight());
			}
			updateGui();
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}
	}

	private class EventKey implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
				textEntered(inputConsole.getText());
			}
			if(arg0.getKeyCode() == KeyEvent.VK_T){
				ownRotation += Math.PI/6;
				moveZeppelins();
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
