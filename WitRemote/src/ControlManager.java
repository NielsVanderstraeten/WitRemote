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

import commands.Command;
import commands.SetGoalHeight;
import commands.SetGoalPosition;
import commands.SetPosition;
import commands.TakePicture;
import commands.Terminate;


public class ControlManager implements Runnable{

	private final static int columnReal = 12;
	private final static int rowReal = 14;
	private final static int REAL_WIDTH = 400*columnReal;
	private final static int REAL_HEIGHT = (int) (400*Math.sqrt(3)/2)*rowReal;

	private int tabletNumber = 1; //-1 indien zeppelin moet landen

	public static void main(String[] args){
		Simulator simulator;
		ControlManager cm ;
		Thread t = null;
		if(args.length == 0 || args[0].equals("-lazymode")){
			cm = new ControlManager();
			t = new Thread(cm);
		}
		else if(args[0].equals("-simulate")){
			simulator = new Simulator("localhost");
			t = new Thread(simulator);
		}
		else {
			cm = new ControlManager(args[0]);
			t = new Thread(cm);
		}
		//		else 
		//			throw new IllegalArgumentException("Cannot start main method in Control Manager");
		t.start();
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
			channel2.setCommand("cd ZeppelinPi/WitPi/WitPi/src; sudo java -classpath pi4j-0.0.5/lib/pi4j-core.jar:rabbitmq-client.jar:. pi/Pi 6066 " + REAL_WIDTH + " " + REAL_HEIGHT);
			channel2.setInputStream(null);
			channel2.setErrStream(System.err);
			channel2.connect();


		}
		catch (Exception e){ 
			System.out.println("Fout bij SSH connectie met Pi");
			e.printStackTrace();
		}		
	}

	private KirovAirship gui;
	//	private Client photoClient;
	private RabbitClient client;
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
	private final static int QR_PICTURES_TO_ANALYSE = 20;

	private String IPadressPi = "";


	public ControlManager(String IPadressPi){
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
		client = new RabbitClient(host, exchangeName);
		//rabbitRecv om de hoogte die de Pi doorstuurt, te ontvangen.
		rabbitRecv = new RabbitRecv(host, exchangeName, gui, this);
		(new Thread(rabbitRecv)).start();
		//	queue.add(new SetDimensions(REAL_WIDTH,REAL_HEIGHT));
		grid = new Grid("plaats van CSV-bestand");
	}

	public ControlManager(){
		this("192.168.2.100");
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
		goals.add(new GoalFindQRCode());
		gui.setTargetHeight(800);
		
		//TODO: commando om QR te lezen op bepaald moment & oude goal negeert
	}

	public KirovAirship getGUI() {
		return gui;
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
					client.executeCommand(c);
					gui.updateOwnPosition(((SetPosition) c).getX(), ((SetPosition) c).getY(), ((SetPosition) c).getRotation());
					gui.setFoundFigures(grid.getLastFigures());
				}
				else if (c instanceof Terminate) {
					client.executeCommand(c);
					terminate();
				}
				else{
					client.executeCommand(c);
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

	//RabbitRecv moet dit oproepen wanneer een foto ontvangen wordt
	public void analysePicture(String realPath) {		
		if (isThreadStillAlive(analyserThread) || isThreadStillAlive(qrThread))
			return;
		
		System.out.println(goals.toString()); //TODO debug
		
		boolean analyseNextPictureForQR = false;
		boolean analyseNextPictureForShapes = false;
		NewShapeRecognition recog = new NewShapeRecognition(realPath, gui, grid, queue);
		
		System.out.println("Analysing picture...");
		if (findQRcode) {
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
		System.out.println("-"+goals.toString()); //TODO debug
		findQRcode = false;
		addNextGoal();
		System.out.println("*"+goals.toString()); //TODO debug
		System.out.println("**"+nextGoal);
	}

	private void startFindingQRCode() {
		findQRcode = true;
		analysedQRPictures = 0;
		client.sendMessage(QRcode.getPublicKey(), "wit.tablet.tablet" + tabletNumber);
	}

	public void setTabletNumber(int number) {
		tabletNumber = number;
	}

	private boolean isThreadStillAlive(Thread t) {
		return (t != null && t.isAlive());
	}	

	//NextGoal is al uit de list verwijderd
	private Goal nextGoal;
	private boolean checkGoalReached(){
		if(nextGoal instanceof GoalHeight){
			double height = gui.getZeppHeight();
			double target = ((GoalHeight) nextGoal).getTargetHeight();
			return closeEnough(height, target);
		}
		else if(nextGoal instanceof GoalPosition){
			int ownX = gui.getOwnX(); int ownY = gui.getOwnY();
			int targetX = ((GoalPosition) nextGoal).getX(); int targetY = ((GoalPosition) nextGoal).getY();
			boolean result = closeEnough(ownX, targetX) && closeEnough(ownY, targetY);
			if (tabletNumber == -1) { //Landen als doel bereikt is en er geen volgende tablet meer is
				client.sendMessage("true", "wit.private.terminate");
				System.exit(0);
			}
			return result;
		} else if (nextGoal instanceof GoalFindQRCode) {
			return findQRcode == false;
		}
		return false;
	}

	

	private void addNextGoal(){
		if(!goals.isEmpty()){
			nextGoal = goals.poll();
			//TODO: GoalHeight verwijderen (anton weet hoe)
			//TODO: cancelnextgoal bij commands toevoegen.
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
			else
				throw new IllegalStateException("Niet bestaande goal @ addNextGoal @ ControlManager");
		} else {
			nextGoal = new GoalFindQRCode();
			startFindingQRCode();
		}
	}

	private boolean closeEnough(double current, double target){
		//We werken in mm ok? - NEIN
		double absoluteMarge = 100;
		if(Math.abs(current - target) < absoluteMarge)
			return true;
		return false;
	}

	public List<Command> getQueue() {
		return queue;
	}

	public Grid getGrid() {
		return grid;
	}
}
