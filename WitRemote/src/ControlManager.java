import goals.Goal;
import goals.GoalHeight;
import goals.GoalPosition;
import gui.KirovAirship;
import gui.Simulator;

import java.io.InputStream;
import java.util.LinkedList;

import javax.swing.JFrame;

import Rooster.Grid;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import commands.Command;
import commands.GetHeight;
import commands.SetDimensions;
import commands.SetGoalHeight;
import commands.SetGoalPosition;
import commands.SetPosition;
import commands.TakePicture;


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
			InputStream in = channel2.getInputStream();
			channel2.connect();
		}
		catch (Exception e){ }
		
	}
	
	private KirovAirship gui;
	private Client client;
	private LinkedList<Command> queue;
	private long lastCheck;
	private LinkedList<Goal> goals;
	private String path = "src/images/";
	private Grid grid;
	private boolean findQRcode = true; //TODO: terug op false zetten
	
	public ControlManager(String serverName, int port){
		queue = new LinkedList<Command>();
		client = new Client(serverName, port, path);
		goals = new LinkedList<Goal>();
		//-500 zodat er direct wordt gevraagd naar hoogte.
		lastCheck = System.currentTimeMillis()-500;
		setUpGui();
		setUpGoals();
		queue.add(new SetDimensions(REAL_WIDTH,REAL_HEIGHT));
		grid = new Grid("plaats van CSV-bestand");
	}
	
	public ControlManager(){
		this("192.168.43.233", 6066);
	}
	
	public void setUpGui(){
		gui = new KirovAirship(1280, 780, REAL_WIDTH, REAL_HEIGHT, queue, goals);
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
		//TODO HIER MOETEN DE GOALS KOMEN.
		//WE KUNNEN MISSCHIEN AUTOMATISEREN, MAAR DAS LASTIG :D
	}
	
	private boolean terminate = false;
	public void terminate(){
		terminate = true;
	}
	public synchronized void run(){ //TODO: synchronised?
		//Wachttijd nodig om connectie te initialiseren
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		if(!goals.isEmpty())
			nextGoal = goals.getFirst();
		
		NewShapeRecognition recog = new NewShapeRecognition(path + client.getNamePicture(), gui, grid, queue);
		boolean analyseQR = false;
		
		while(!terminate){
			Command c = null;
			boolean analysePicture = false;
			Thread analyserThread = null;
			Thread qrThread = null;
			//PositionAnalyser analyser = new PositionAnalyser();
			
			/*
			if(analyser.isReady()){
				gui.updateOwnPosition(analyser.getX(), analyser.getY());
				analyserPicture = false;
			}
			*/
			while(!queue.isEmpty()){
				c = queue.poll();		
				if(c instanceof TakePicture && !isThreadStillAlive(analyserThread)) {
					if (findQRcode && !isThreadStillAlive(qrThread))
						analyseQR = true;
					analysePicture = true;
					gui.updateLastCommand(c.getConsole());
					client.executeCommand(c);
				}
				else if(c instanceof GetHeight){
					String recv = client.executeCommand(c);
					gui.updateZeppHeight((int) Double.parseDouble(recv));
					gui.updateLastCommand(c.getConsole());
				}
				else if(c instanceof SetPosition){
					client.executeCommand(c);
					gui.updateOwnPosition(((SetPosition) c).getX(), ((SetPosition) c).getY(), ((SetPosition) c).getRotation());
					gui.setFoundFigures(grid.getLastFigures());
				}
				else{
					client.executeCommand(c);
					gui.updateLastCommand(c.getConsole());
				}
			}
			
			if (analyseQR) {
				qrThread = new Thread(new QRcode(this, queue, path + client.getNamePicture()));
				qrThread.start();
				analyseQR = false;
			}
			
			if(analysePicture){
				recog.setFile(path + client.getNamePicture());
				analyserThread = new Thread(recog);
				analyserThread.start();
				analysePicture = false;
				//TODO: update GUI
				//TODO: nog getMethode om locatie van gevonden shapes te
			}
			
			gui.updateGui();
			
			if(System.currentTimeMillis() - lastCheck > 2000){
				lastCheck = System.currentTimeMillis();
				
				boolean stillEmpty = false;
				if (queue.isEmpty())
					stillEmpty = true;
				
				nextGoal.print();
				queue.offer(new GetHeight());
				Command nextCommand = queue.getFirst();
				if(!(nextCommand instanceof GetHeight) && !(nextCommand instanceof TakePicture))
					System.out.println(nextCommand.getConsole());
				
				//Enkel nieuwe foto nemen indien queue nog steeds leeg was, en de laatst genomen foto reeds verwerkt is
				if (stillEmpty && ! isThreadStillAlive(analyserThread))
					queue.add(new TakePicture());
			}
			
			if(checkGoalReached()){
				addNextGoal();
			}
		}
	}
	
	public void foundQRCode() {
		findQRcode = false;
	}
	
	//TODO: ergens moet dit gebruikt worden in client (vanaf dat bericht binnen komt dat we voor Qr codes moeten kijken)
	public void startFindingQRCode() {
		findQRcode = true;
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
			return closeEnough(height, target);
		}
		else if(nextGoal instanceof GoalPosition){
			int ownX = gui.getOwnX(); int ownY = gui.getOwnY();
			int targetX = ((GoalPosition) nextGoal).getX(); int targetY = ((GoalPosition) nextGoal).getY();
			return closeEnough(ownX, targetX) && closeEnough(ownY, targetY);
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
			}
			else
				throw new IllegalStateException("Niet bestaande goal @ addNextGoal @ ControlManager");
		}
	}
	
	private boolean closeEnough(double current, double target){
		//We werken in mm ok?
		double absoluteMarge = 100;
		if(Math.abs(current - target) < absoluteMarge)
			return true;
		return false;
	}
}
