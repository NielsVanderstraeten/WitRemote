import goals.Goal;
import goals.GoalFindQRCode;
import goals.GoalHeight;
import goals.GoalPosition;
import gui.KirovAirship;
import gui.Simulator;

import java.util.LinkedList;

import javax.swing.JFrame;

import Rooster.Grid;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import commands.Command;
import commands.SetGoalHeight;
import commands.SetGoalPosition;
import commands.SetPosition;


public class ControlManager implements Runnable{

	private final static int columnReal = 7;
	private final static int rowReal = 7;
	private final static int REAL_WIDTH = 400*columnReal;
	private final static int REAL_HEIGHT = (int) (400*Math.sqrt(3)/2)*rowReal;
	
	public static void main(String[] args){
		Simulator simulator;
		ControlManager cm ;
		Thread t = null;
		if(args.length == 0){
			cm = new ControlManager();
			cm.setUpFirstConnection();
			t = new Thread(cm);
		}
		else if(args[0].equals("-simulate")){
			simulator = new Simulator("localhost");
			t = new Thread(simulator);
		}
		else if(args[0].equals("-lazymode")){
			cm = new ControlManager();
			t = new Thread(cm);
		}
		else if(args.length >= 2){
			cm = new ControlManager(args[0], Integer.parseInt(args[1]));
			cm.setUpFirstConnection();
			t = new Thread(cm);
		}
		else 
			throw new IllegalArgumentException("Cannot start main method in Control Manager");
		t.start();
	}
	
	public void setUpFirstConnection(){
		try{
			JSch jsch=new JSch();
			Session session=jsch.getSession("pi", "192.168.43.233", 22);
			session.setPassword("raspberry");
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect(30000000);
			
			ChannelExec channel2= (ChannelExec)session.openChannel("exec");
			
			channel2.setCommand("cd ZeppelinPi/WitPi/WitPi/src; sudo java -cp pi4j-0.0.5/lib/pi4j-core.jar:. pi/Pi 6066 " + REAL_WIDTH + " " + REAL_HEIGHT);
			channel2.setInputStream(null);
			channel2.setErrStream(System.err);
//			InputStream in = channel2.getInputStream();
			channel2.connect();
		}
		catch (Exception e){ }
		
	}
	
	private KirovAirship gui;
	private Client photoClient;
	private RabbitClient client;
	private RabbitRecv rabbitRecv;
	private LinkedList<Command> queue;
	private long lastCheck;
	private LinkedList<Goal> goals;
	private String path = "src/images/";
	private final String host = "tabor";
	private final String exchangeName = "server";
	private Grid grid;
	private boolean findQRcode = false; //TODO: op true zetten indien we willen testen zonder server
	private int analysedQRPictures;
	private final static int QR_PICTURES_TO_ANALYSE = 5;
	
	public ControlManager(String serverName, int port){
		queue = new LinkedList<Command>();
		goals = new LinkedList<Goal>();
		//-500 zodat er direct wordt gevraagd naar hoogte.
		lastCheck = System.currentTimeMillis()-500;
		setUpGui();
		setUpGoals();
		//Client voor dingen door te sturen.
		client = new RabbitClient(host, exchangeName);
		//photoClient voor foto's te ontvangen.
		photoClient = new Client(serverName, port, path);
		(new Thread(photoClient)).run();
		//rabbitRecv om de hoogte die de Pi doorstuurt, te ontvangen.
		rabbitRecv = new RabbitRecv(host, exchangeName, gui);
		(new Thread(rabbitRecv)).run();
	//	queue.add(new SetDimensions(REAL_WIDTH,REAL_HEIGHT));
		grid = new Grid("plaats van CSV-bestand");
	}
	
	public ControlManager(){
		this("192.168.43.233", 5672);
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
		nextGoal = new GoalHeight(1000);
		gui.updateZeppHeight(1500);
	}
	
	private boolean terminate = false;
	public void terminate(){
		terminate = true;
	}
	
	private Thread analyserThread = null;
	private Thread qrThread = null;
	
	public synchronized void run(){
		
		if(!goals.isEmpty())
			nextGoal = goals.getFirst();
			
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
				else{
					client.executeCommand(c);
				}
				gui.updateLastCommand(c.getConsole());
			}
			
			gui.updateGui();
			
			if(System.currentTimeMillis() - lastCheck > 2000){
				lastCheck = System.currentTimeMillis();
				
//				boolean stillEmpty = false;
//				if (queue.isEmpty())
//					stillEmpty = true;
				
				nextGoal.print();
//				Command nextCommand = queue.getFirst();
//				System.out.println(nextCommand.getConsole());
			}
			
			if(checkGoalReached()){
				addNextGoal(); //Haal eerste goal uit goal-lijst en zet in programma
			}
		}
	}
	
	//RabbitRecv moet dit oproepen wanneer een foto ontvangen wordt
	public void analysePicture() {
		boolean analyseNextPictureForQR = false;
		boolean analyseNextPictureForShapes = false;
		NewShapeRecognition recog = new NewShapeRecognition(path + client.getNamePicture(), gui, grid, queue);
		
		if(!isThreadStillAlive(analyserThread) && !isThreadStillAlive(qrThread)) {
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
					qrThread = new Thread(new QRcode(this, queue, path + client.getNamePicture()));
					qrThread.start();
				}
				analyseNextPictureForQR = false;
			} else if(analyseNextPictureForShapes){
				recog.setFile(path + client.getNamePicture());
				analyserThread = new Thread(recog);
				analyserThread.start();
				analyseNextPictureForShapes = false;
			}
		}
	}
	
	public void foundQRCode() {
		findQRcode = false;
	}
	
	private void startFindingQRCode() {
		findQRcode = true;
		analysedQRPictures = 0;
	}
	
	private boolean isThreadStillAlive(Thread t) {
		return (! ((t == null) || t.isAlive()) );
	}	

	//NextGoal is al uit de list verwijderd
	private Goal nextGoal;
	private boolean checkGoalReached(){
		if(nextGoal instanceof GoalHeight){
			double height = gui.getZeppHeight();
			double target = ((GoalHeight) nextGoal).getTargetHeight();
				if (target == 0)
					System.exit(0);
			return closeEnough(height, target);
		}
		else if(nextGoal instanceof GoalPosition){
			int ownX = gui.getOwnX(); int ownY = gui.getOwnY();
			int targetX = ((GoalPosition) nextGoal).getX(); int targetY = ((GoalPosition) nextGoal).getY();
			return closeEnough(ownX, targetX) && closeEnough(ownY, targetY);
		} else if (nextGoal instanceof GoalFindQRCode) {
			return findQRcode == false;
		}
		return false;
	}
	
	private void addNextGoal(){
		if(!goals.isEmpty()){
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
}
