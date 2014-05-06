import goals.Goal;
import goals.GoalFindQRCode;
import goals.GoalHeight;
import goals.GoalPosition;
import gui.KirovAirship;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import Rooster.Grid;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import commands.CancelCurrentGoal;
import commands.Command;
import commands.SetEnemy;
import commands.SetGoalHeight;
import commands.SetGoalPosition;
import commands.SetPosition;
import commands.Terminate;


public class ControlManager {

	private final static int columnReal = 12;
	private final static int rowReal = 14;
	private final static int REAL_WIDTH = 400*columnReal;
	private final static int REAL_HEIGHT = (int) (400*Math.sqrt(3)/2)*rowReal;
	
	private static String IPaddressPI = "192.168.43.180";

	private int tabletNumber = -1; //-1 betekent geen nieuwe tablet als doel
	
	private boolean foundPosition = true;
	
	public void setFoundPosition() {
		foundPosition = true;
	}

	public static void main(String[] args){
		//		Simulator simulator;
		ControlManager cm ;
		if(args.length == 0 || args[0].equals("-lazymode")){
			cm = new ControlManager();
		}
		//		else if(args[0].equals("-simulate")){
		//			simulator = new Simulator("localhost");
		//			t = new Thread(simulator);
		//		}
		else {
			cm = new ControlManager(args[0]);
		}
		//		else 
		//			throw new IllegalArgumentException("Cannot start main method in Control Manager");
		cm.run();
	}

	public void setUpFirstConnection(String IPadressPi){
		try{
			JSch jsch = new JSch();
			Session session = jsch.getSession("pi", IPadressPi, 22);
			session.setPassword("raspberry");
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect(30000000);

			ChannelExec channel2= (ChannelExec) session.openChannel("exec");			
			channel2.setCommand("cd ZeppelinPi/WitPi2/WitPi/WitPi/src; sudo java -classpath pi4j-0.0.5/lib/pi4j-core.jar:rabbitmq-client.jar:. pi/Pi 6066 " + REAL_WIDTH + " " + REAL_HEIGHT);
			channel2.setInputStream(null);
			channel2.setErrStream(System.err);
			channel2.connect();


		}
		catch (Exception e){ 
			System.out.println("Fout bij SSH connectie met Pi");
			System.out.println(" -> Waarschijnlijk is de Pi uitgevallen");
			System.out.println(" -> Of Putty / SSH op pi staat nog niet op");
			System.out.println(" -> Of IP vd pi is fout");
			e.printStackTrace();
		}		
	}

	private KirovAirship gui;
	//	private Client photoClient;
	private RabbitClient rabbitClient;
	private RabbitRecv rabbitRecv;
	private LinkedList<Command> queue;
	private long lastCheck;
	private LinkedList<Goal> goals;
	//	private String path = "src/images/";
	private final String host = "localhost";
	private final String exchangeName = "server";
	private Grid grid;
	private boolean findQRcode = false;
	private int analysedQRPictures;
	private final static int QR_PICTURES_TO_ANALYSE = 5;

	private String IPadressPi = "";


	public ControlManager(String IPadressPi){
		long start = System.currentTimeMillis();
		this.IPadressPi = IPadressPi;
		setUpFirstConnection(IPadressPi);
		queue = new LinkedList<Command>();
		goals = new LinkedList<Goal>();
		//-500 zodat er direct wordt gevraagd naar hoogte.
		lastCheck = System.currentTimeMillis()-500;
		setUpGui();
		setUpGoals();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Client voor dingen door te sturen.
		rabbitClient = new RabbitClient(host, exchangeName);
		//rabbitRecv om de hoogte die de Pi doorstuurt, te ontvangen.
		rabbitRecv = new RabbitRecv(host, exchangeName, gui, this);
		(new Thread(rabbitRecv)).start();
		//	queue.add(new SetDimensions(REAL_WIDTH,REAL_HEIGHT));
		grid = new Grid("plaats van CSV-bestand");
		System.out.println("CM started in: " + (System.currentTimeMillis() - start));
	}

	public ControlManager(){
		this(IPaddressPI);
	}

	public void setUpGui(){
		gui = new KirovAirship(1200, 650, REAL_WIDTH, REAL_HEIGHT, queue, goals);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("Zeppelin Group White");
		gui.setSize(gui.getWidth(), gui.getHeight());
		gui.setVisible(true);
		gui.requestFocus();
	}

	public void setUpGoals(){
		//Standaard hoogte van 1m invoeren als targethoogte.
		//nextGoal = new GoalHeight(800);
//		goals.add(new GoalFindQRCode()); //TODO: wegdoen
		gui.setTargetHeight(800);
	}

	public KirovAirship getGUI() {
		return gui;
	}
	
	public List<Goal> getGoals() {
		return goals;
	}

	private boolean terminate = false;

	public void terminate(){
		terminate = true;
	}

	private Thread analyserThread = null;
	private Thread qrThread = null;
	private Thread photoClient = null;

	public synchronized void run(){

		if(!goals.isEmpty())
			nextGoal = goals.getFirst();

		QRcode.initialiseKeys(); //Nodig om public en private key voor QR-codes te genereren

		while(!terminate){
			//PositionAnalyser analyser = new PositionAnalyser();

			/*
			if(analyser.isReady()){
				gui.updateOwnPosition(analyser.getX(), analyser.getY());
				analyserPicture = false;
			}
			 */
			while(!queue.isEmpty()){
				Command c = queue.poll();		
				if(c instanceof SetPosition){
					rabbitClient.executeCommand(c);
					gui.updateOwnPosition(((SetPosition) c).getX(), ((SetPosition) c).getY(), ((SetPosition) c).getRotation());
					gui.setFoundFigures(grid.getLastFigures());
//				} else if (c instanceof SetGoalHeight) {
//					rabbitClient.executeCommand(c);
//					int height = ((SetGoalHeight)c).getHeight();
//					goals.add(new GoalHeight(height));
//					gui.setTargetHeight(height);
//				} else if (c instanceof SetGoalPosition) {
//					rabbitClient.executeCommand(c);
//					int x = ((SetGoalPosition)c).getX();
//					int y = ((SetGoalPosition)c).getY();
//					goals.add(new GoalPosition(x, y));
//					gui.setGoalPosition(x, y);
				} else if(c instanceof CancelCurrentGoal) {
					nextGoal = null;
				} else if (c instanceof Terminate) {
					rabbitClient.executeCommand(c);
					terminate();
				} else if (c instanceof SetEnemy){
					rabbitRecv.setEnemy(((SetEnemy) c).getEnemy());
				} else if (c instanceof SetGoalHeight) {
					gui.setTargetHeight(((SetGoalHeight)c).getHeight());
					rabbitClient.executeCommand(c);
				} else{
					rabbitClient.executeCommand(c);
				}
				gui.updateLastCommand(c.getConsole());
			}

			gui.updateGui();

			if(System.currentTimeMillis() - lastCheck > 3000 && !isThreadStillAlive(photoClient)) {
				lastCheck = System.currentTimeMillis();
				photoClient = new Thread(new PhotoClient(IPadressPi, 6066, this));
				photoClient.start();
			}

			if(checkGoalReached()){
				addNextGoal(); //Haal eerste goal uit goal-lijst en zet in programma
			}
		}
	}

	//TODO: analysing pictures gaat nog traag?

	//RabbitRecv moet dit oproepen wanneer een foto ontvangen wordt
	public void analysePicture(String realPath) {		
		if (isThreadStillAlive(analyserThread) || isThreadStillAlive(qrThread))
			return;

		boolean analyseNextPictureForQR = false;
		boolean analyseNextPictureForShapes = false;
		NewShapeRecognition recog = new NewShapeRecognition(realPath, this);

		System.out.println("Analysing picture...");
		if (findQRcode && foundPosition) {
			analyseNextPictureForQR = true;
			analysedQRPictures++;
			gui.updateLastCommand("Analysing picture for QR Code");
		} else {
			analyseNextPictureForShapes = true;
			gui.updateLastCommand("Analysing picture for shapes");
		}			

		if (analyseNextPictureForQR) {
			if (analysedQRPictures > QR_PICTURES_TO_ANALYSE) {
				findQRcode = false;
				foundPosition = false;
				goals.add(new GoalPosition(gui.getGoalX(), gui.getGoalY()));
			} else {
				qrThread = new Thread(new QRcode(this, realPath));
				qrThread.start();
			}
			analyseNextPictureForQR = false;
		} else if(analyseNextPictureForShapes){
			recog.setFile(realPath);
			analyserThread = new Thread(recog);
			analyserThread.start();
			analyseNextPictureForShapes = false;
		}
	}

	public void foundQRCode() {
		findQRcode = false;
		addNextGoal();
	}

	private void startFindingQRCode() {
		findQRcode = true;
		analysedQRPictures = 0;
		rabbitClient.sendMessage(QRcode.getPublicKey(), "wit.tablets.tablet" + tabletNumber);
	}

	public void setTabletNumber(int number) {
		tabletNumber = number;
	}

	private boolean isThreadStillAlive(Thread t) {
		//		System.gc(); //TODO: aanzetten?
		return (t != null && t.isAlive());
	}	

	//NextGoal is al uit de list verwijderd
	private Goal nextGoal;
	private boolean checkGoalReached(){
		if(nextGoal instanceof GoalHeight){
//			double height = gui.getZeppHeight();
//			double target = ((GoalHeight) nextGoal).getTargetHeight();
//			return closeEnough(height, target);
			return true;
		}
		else if(nextGoal instanceof GoalPosition){
			int ownX = gui.getOwnX(); int ownY = gui.getOwnY();
			int targetX = ((GoalPosition) nextGoal).getX(); int targetY = ((GoalPosition) nextGoal).getY();
			boolean result = closeEnough(ownX, targetX) && closeEnough(ownY, targetY);
//			if (tabletNumber == -1 && result) {		
			if (result) {
				gui.printToConsole("TARGET POSITION REACHED!");
				
				//Wanneer doel bereikt is, nieuwe QRcode zoeken.

				goals.add(new GoalFindQRCode());
			}

			return result;
		} else if (nextGoal instanceof GoalFindQRCode) {
			return findQRcode == false;
		} else if (nextGoal == null)
			return true;

		return false;
	}

	private void addNextGoal(){
		if(goals.isEmpty())
			nextGoal = null;

		nextGoal = goals.poll();
		
		if(nextGoal instanceof GoalHeight){
			int targetheight = ((GoalHeight) nextGoal).getTargetHeight();
			gui.setTargetHeight(targetheight);
			queue.offerFirst(new SetGoalHeight(targetheight));
		}
		else if(nextGoal instanceof GoalPosition){
			int goalX = ((GoalPosition) nextGoal).getX();
			int goalY = ((GoalPosition) nextGoal).getY();
			gui.setGoalPosition(goalX, goalY);			
			queue.offerFirst(new SetGoalPosition(goalX, goalY));
		} else if (nextGoal instanceof GoalFindQRCode) {
			startFindingQRCode();
		}
		else if (nextGoal != null)
			throw new IllegalStateException("Niet bestaande goal @ addNextGoal @ ControlManager");
	}

	private boolean closeEnough(double current, double target){
		return (Math.abs(current - target) < 500);
	}

	public List<Command> getQueue() {
		return queue;
	}

	public Grid getGrid() {
		return grid;
	}
}
