package gui;

import goals.Goal;
import goals.GoalPosition;

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
import java.util.ArrayList;
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

import Rooster.Shape;

import commands.Command;

public class KirovAirship extends JFrame {
	
	private final static int columnReal = 7;
	private final static int rowReal = 7;
	public final static int REAL_WIDTH = 400*columnReal;
	public final static int REAL_HEIGHT = (int) (400*Math.sqrt(3)/2)*rowReal;
	private static final long serialVersionUID = 1L;
	private JLayeredPane totalPane;
	//Height en width van het scherm
	private final int height, width;
	//Coordinaten worden bijgehouden in millimeter.
	private final int heightMeters, widthMeters;
	public LinkedList<Command> queue;
	private LinkedList<Goal> goals;
	
	/**
	 * Full parameter constructor. Sets height, width, gives the size of the real grid. Finally also gives the queue and the goals list to the GUI.
	 * @param width The width of the gui in pixels
	 * @param height The height if the gui in pixels
	 * @param widthMeters The width of the real grid in mm.
	 * @param heightMeters The height of the real grid in mm
	 * @param queue	The queue of commands
	 * @param goals	The list of goals
	 */
	public KirovAirship(int width, int height, int widthMeters, int heightMeters, LinkedList<Command> queue, LinkedList<Goal> goals){
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
		setResizable(false);
		
		setUpConsole();
		setUpMission();
		setUpInformation();
		setUpPhoto();
		setUpHeightGraph();
		
		//Map moet laatste
		setUpMap();
	}
	
	/**
	 * Small parameter constructor. Gives default values to width (1280), height (780), realWdith (4000), realHeight (4000)
	 * @param queue
	 * @param goals
	 */
	public KirovAirship(LinkedList<Command> queue, LinkedList<Goal> goals){
		this(1200, 650, REAL_WIDTH, REAL_HEIGHT, queue, goals);
	}
	
	/**
	 * Default constructor. Uses the short constructor but also makes a new queue list and goals list.
	 */
	public KirovAirship(){
		this(new LinkedList<Command>(), new LinkedList<Goal>());
	}
	
	/**
	 * returns height in pixels
	 */
	public int getHeight(){
		return height;
	}
	
	/**
	 * returns width in pixels
	 */
	public int getWidth(){
		return width;
	}
	
	/**
	 * Takes a source string, finds the image, rescales to give width and height, and returns the imageIcon.
	 * @param source
	 * @param width
	 * @param heightf
	 * @return
	 */
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
	
	public void printToConsole(String text){
		outputConsole.append(text + "\n");
	}
	
	private TextParser parser;
	/**
	 * De methode die de text in de console pakt en doorstuurt naar de parser.
	 * Deze methode scrollt ook naar beneden en reset de input.
	 * @param text
	 */
	private void textEntered(String text){
		if(text != null && !text.isEmpty()){
			String printer = parser.parse(text);
			printToConsole(printer);
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
		consolePane.setLocation(10, 374);
		consolePane.setSize(300, 237);
		totalPane.add(consolePane);
		
		consoleScroller = new JScrollPane();
		consoleScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		consoleScroller.setBounds(0, 0, consolePane.getWidth(), consolePane.getHeight()-25);
		consolePane.add(consoleScroller);
		
		outputConsole = new JTextArea();
		outputConsole.setFont(new Font("Tahoma", Font.PLAIN, 13));
		outputConsole.setFocusable(false);
		outputConsole.setEditable(false);
		outputConsole.setLineWrap(true);
		consoleScroller.setViewportView(outputConsole);
		
		inputConsole = new JTextField();
		inputConsole.setBounds(0, consolePane.getHeight() - 20, consolePane.getWidth() - 85, 20);
		inputConsole.addKeyListener(new EventKey());
		consolePane.add(inputConsole);
		
		consoleButton = new JButton("Enter");
		consoleButton.setOpaque(false);
		consoleButton.setBackground(Color.WHITE);
		consoleButton.setBounds(consolePane.getWidth() - 85, consolePane.getHeight() - 20 ,85 ,20);
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
		missionPane.setBounds(10, 10, 300, 22);
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
		mapPane.setBackground(new Color(153, 153, 204));
		mapPane.setOpaque(true);
		mapPane.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		mapPane.setBounds(324, 11, 830, 600);
		totalPane.add(mapPane);
		
		mapMaker = new MapMaker(mapPane.getWidth() - 18, mapPane.getHeight() - 18);
		mapMaker.setBounds(9, 9, mapMaker.getWidth(), mapPane.getHeight());
		mapPane.add(mapMaker);
		mapMaker.addMouseListener(new ZeppelinMouse());
		
		updateOwnPosition((int) (widthMeters*0.5), 	(int) (heightMeters*0.5), 0);
		updateOpponentPosition((int) (widthMeters*0.9), (int) (heightMeters*0.9));
		updateGui();
	}

	private JLayeredPane informationPane;
	private JLabel targetHeightLabel, currentHeightLabel, ownXPosLabel, ownYPosLabel, opponentXPosLabel, opponentYPosLabel;
	
	/**
	 * Deze methode maakt het paneel om de positie in mm weer te geven alsook de hoogte.
	 */
	private void setUpInformation(){
		informationPane = new JLayeredPane();
		informationPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		informationPane.setBounds(10, 43, 300, 60);
		informationPane.setBackground(Color.WHITE);
		informationPane.setOpaque(true);
		totalPane.add(informationPane);
		informationPane.setLayout(new GridLayout(3, 5, 0, 0));
		
		// Eerst rij
		JLabel emptyLabel = new JLabel("");
		informationPane.add(emptyLabel);
		
		JLabel heightTextLabel = new JLabel("Height");
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
	private JLabel photoLabel, recognShapeLabel;
	/**
	 * Maakt het fotopaneel.
	 */
	private void setUpPhoto(){
		photoPane = new JLayeredPane();
		photoPane.setLocation(10, 114);
		photoPane.setSize(300, 256);
		totalPane.add(photoPane);
		
		photoLabel = new JLabel();
		photoLabel.setSize(photoPane.getWidth(), photoPane.getHeight() - 20);
		photoPane.add(photoLabel);
		ImageIcon photo = getImageIcon("src/gui/resources/startphoto.jpg", photoLabel.getWidth(), photoLabel.getHeight());
		photoLabel.setIcon(photo);
		
		recognShapeLabel = new JLabel();
		recognShapeLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		recognShapeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		recognShapeLabel.setText("Found shapes");
		recognShapeLabel.setBounds(0, photoPane.getHeight() - 20, photoPane.getWidth(), 20);
		photoPane.add(recognShapeLabel);
	}

	private JLayeredPane heightGraphPanel;
	private JLabel targetGraphHeightLabel, currentGraphHeightLabel, heightColorLabel;
	
	private void setUpHeightGraph(){
		heightGraphPanel = new JLayeredPane();
		heightGraphPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		heightGraphPanel.setBounds(1160, 10, 30, 601);
		heightGraphPanel.setOpaque(true);
		heightGraphPanel.setBackground(new Color(135, 206, 235));
		totalPane.add(heightGraphPanel);
		heightGraphPanel.setLayout(null);
		
		targetGraphHeightLabel = new JLabel();
		targetGraphHeightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		heightGraphPanel.add(targetGraphHeightLabel);
		
		currentGraphHeightLabel  = new JLabel();
		currentGraphHeightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		currentGraphHeightLabel.setBounds(0, heightGraphPanel.getHeight() - 20, heightGraphPanel.getWidth(), 20);
		heightGraphPanel.add(currentGraphHeightLabel);
		
		heightColorLabel = new JLabel();
		heightColorLabel.setOpaque(true);
		heightGraphPanel.add(heightColorLabel);
		
	}
	
	/**
	 * Beweegt de zeppelins naar de coordinaten die in de gui zitten.
	 */
	private void moveZeppelins(){
		mapMaker.moveOwnZeppelin(ownX, ownY);
		mapMaker.rotateOwnZeppelin(ownRotation);
		mapMaker.moveOppZeppelin(opponentX, opponentY);
		repaint();
	}
	
	/**
	 * Update de relevante waarden van de gui.
	 */
	public void updateGui(){
		moveZeppelins();
	}
	
	private int ownX, ownY;
	private double ownRotation = 0;
	/**
	 * Verandert de coordinaten van de eigen zeppelin. Waardes moeten ingegeven worden in mm, of radialen.
	 * @param x
	 * @param y
	 * @param rotation
	 */
	public void updateOwnPosition(int x, int y, double rotation){
		ownX = x; 
		ownY = y;
		ownRotation = rotation;
		ownXPosLabel.setText(x+" mm"); ownYPosLabel.setText(y+" mm");
	}
	
	private int opponentX, opponentY;
	/**
	 * Verandert de coordinaten van de andere zeppelin. Waardes moeten ingegeven worden in mm.
	 * @param x
	 * @param y
	 */
	public void updateOpponentPosition(int x, int y){
		opponentX = x; 
		opponentY = y;
		opponentXPosLabel.setText(x+" mm"); opponentYPosLabel.setText(y+" mm");
		
	}
	
	/**
	 * Returnt de x-positie van de eigen zeppelin in mm. 
	 * @return
	 */
	public int getOwnX() {
		return ownX;
	}

	/**
	 * Returnt de y-positie van de eigen zeppelin in mm. 
	 * @return
	 */
	public int getOwnY() {
		return ownY;
	}

	/**
	 * Returnt de rotatie van de eigen zeppelin in mm. 
	 * @return
	 */
	public double getOwnRotation(){
		return ownRotation;
	}
	
	/**
	 * Returnt de x-positie van de andere zeppelin in mm. 
	 * @return
	 */
	public int getOpponentX() {
		return opponentX;
	}

	/**
	 * Returnt de y-positie van de andere zeppelin in mm. 
	 * @return
	 */
	public int getOpponentY() {
		return opponentY;
	}
	
	/**
	 * Print deze zin in de parser.
	 * @param command
	 */
	public void updateLastCommand(String command){
		outputConsole.append(command + "\n");
		outputConsole.setCaretPosition(outputConsole.getDocument().getLength());
	}
	
	/**
	 * Verandert de zeppelinoogte naar de huidige waarde. Waardes moeten ingegeven worden in cm.
	 * @param newheight
	 */
	public void updateZeppHeight(int newheight){
		updateZeppHeightMM(newheight * 10);
	}
	
	public void updateZeppHeightMM(int newheight){
		zeppHeight = newheight;
		currentHeightLabel.setText(newheight + "mm");
		currentGraphHeightLabel.setText(newheight + "");
		updateHeightGraph();
	}
	
	/**
	 * Zorgt voor de uitbeelding van de geanalyseerde foto in de gui.
	 */
	public void updatePhoto(){
		updatePhoto("src/images/analyse.jpg");
	}
	
	public void updatePhoto(String path){
		ImageIcon photo = getImageIcon(path, photoLabel.getWidth(), photoLabel.getHeight());
		photoLabel.setIcon(photo);
	}
	
	public void setSimulatorPhoto(){
		photoPane.remove(recognShapeLabel);
		ImageIcon photo = getImageIcon("src/gui/resources/simulator.jpg", photoLabel.getWidth(), photoLabel.getHeight());
		photoLabel.setSize(photoPane.getWidth(), photoPane.getHeight());
		photoLabel.setIcon(photo);
//		ImageIcon photo = getImageIcon("src/images/analyse.jpg", photoLabel.getWidth(), photoLabel.getHeight());
//		photoLabel.setIcon(photo);
//		recognShapeLabel.setText("RR, GC, WC, BH, BH");
	}
	
	public void updateRecognisedShapes(ArrayList<Shape> shapes){
		String shapeString = "";
		for(int i = 0; i < shapes.size(); i++){
			Shape shape = shapes.get(i);
			shapeString += shape.getCode();
			if(!(i == shapes.size() - 1))
				shapeString += ", ";
		}
		recognShapeLabel.setText(shapeString);
	}
	
	private int zeppHeight;
	/**
	 * Returnt de hoogte van de eigen zeppelin in mm. 
	 * @return
	 */
	public int getZeppHeight(){
		return (int) zeppHeight/10;
	}
	
	public int getZeppHeightMM(){
		return zeppHeight;
	}
	
	private int targetheight;
	/**
	 * Zet een nieuwe targethoogte. In mm.
	 * @param height
	 */
	public void setTargetHeight(int height){
		targetheight = height;
		targetHeightLabel.setText(height + "mm");
		missionText.setText("Change height to: "+ height+"mm.");
		targetGraphHeightLabel.setText(height + "");
		
		updateHeightGraph();
	}
	
	private void updateHeightGraph(){
		int highest = Math.max(getZeppHeightMM(), getTargetHeight());
		highest  = Math.min((int) (highest*1.3), highest+300);
		if(highest == 0)
			highest = 1;
		
		double currPos = getZeppHeightMM()*heightGraphPanel.getHeight()/highest;
		heightColorLabel.setBounds(1,heightGraphPanel.getHeight() - (int) currPos, heightGraphPanel.getWidth() - 2,(int) currPos - 1);
		if(Math.abs(getZeppHeightMM() - getTargetHeight()) > 400)
			heightColorLabel.setBackground(Color.RED);
		else if(Math.abs(getZeppHeightMM() - getTargetHeight()) > 200)
			heightColorLabel.setBackground(Color.ORANGE);
		else 
			heightColorLabel.setBackground(Color.GREEN);
		
		double tarPos = getTargetHeight()*heightGraphPanel.getHeight()/highest;
		targetGraphHeightLabel.setBounds(0,heightGraphPanel.getHeight() - (int) tarPos - 10, heightGraphPanel.getWidth(),20);
	}
	
	public int getTargetHeight(){
		return targetheight;
	}
	
	private int goalX = REAL_WIDTH/2;
	private int goalY = REAL_HEIGHT/2;
	/**
	 * Zet een nieuwe doelhoogte. In mm
	 * @param x
	 * @param y
	 */
	public void setGoalPosition(int x, int y){
		missionText.setText("We have to go to: " + x + "mm, " + y +"mm");
		goalX = x;
		goalY = y;
		mapMaker.setTarget(x, y);
	}
	
	/**
	 * Returnt de X waarde van het doel.
	 * @return
	 */
	public int getGoalX(){
		return goalX;
	}
	
	/**
	 * returnt de Y waarde van het doel;
	 * @return
	 */
	public int getGoalY(){
		return goalY;
	}
	
	public void setFoundFigures(ArrayList<Integer> array){
		mapMaker.setFoundFigures(array);
	}
	
	public void setDebug(){
		
	}
	
	@Deprecated
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
	
	@Deprecated
	private class ZeppelinMouse implements MouseListener{
		int mouseX, mouseY;
		@Override
		public void mousePressed(MouseEvent e){
			mouseX = (int) e.getX();
			mouseY = (int) e.getY();
			changePixelToReal();
			if(SwingUtilities.isLeftMouseButton(e) ){
				goals.addFirst(new GoalPosition(mouseX ,mouseY));
				//setGoalPosition(mouseX,mouseY);
			}
			else if(SwingUtilities.isRightMouseButton(e) ){
				updateOwnPosition(mouseX, mouseY, ownRotation);
			}
			else if(SwingUtilities.isMiddleMouseButton(e) ){
				updateOpponentPosition(mouseX, mouseY);
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
		private void changePixelToReal(){
			mouseX = mapMaker.changePixelToReal(mouseX, true);
			mouseY = mapMaker.changePixelToReal(mouseY, false);
		}
	}

	private class EventKey implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
				textEntered(inputConsole.getText());
			} else if(arg0.getKeyCode() == KeyEvent.VK_Y)
				updatePhoto();
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
