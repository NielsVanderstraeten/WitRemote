import gui.KirovAirship;
import gui.Simulator;

import java.io.InputStream;
import java.util.LinkedList;

import javax.swing.JFrame;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import commands.*;
import goals.*;


public class ControlManager implements Runnable{

	public static void main(String[] args){
		Simulator simulator;
		ControlManager cm ;
		Thread t = null;
		if(args.length == 0){
			ControlManager.setUpFirstConnection();
			cm = new ControlManager();
			t = new Thread(cm);
		}
		else if(args[0].equals("-simulate")){
			simulator = new Simulator("localhost");
			t = new Thread(simulator);
		}
		else if(args.length >= 2){
			ControlManager.setUpFirstConnection();
			cm = new ControlManager(args[0], Integer.parseInt(args[1]));
			t = new Thread(cm);
		}
		else 
			throw new IllegalArgumentException("Cannot start main method in Control Manager");
		t.start();
	}
	
	public static void setUpFirstConnection(){
		try{
			JSch jsch=new JSch();
			Session session=jsch.getSession("pi", "192.168.43.233", 22);
			session.setPassword("raspberry");
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect(30000000);
			
			ChannelExec channel2= (ChannelExec)session.openChannel("exec");
			
			channel2.setCommand("cd ZeppelinPi/ZeppelinPi/src; sudo java -cp pi4j-0.0.5/lib/pi4j-core.jar:. pi/Pi 6066");
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
	
	public ControlManager(String serverName, int port){
		queue = new LinkedList<Command>();
		client = new Client(serverName, port, "src/resources");
		goals = new LinkedList<Goal>();
		setUpGui();
		setUpGoals();
	}
	
	public ControlManager(){
		this("192.168.43.233", 6066);
	}
	
	public void setUpGui(){
		gui = new KirovAirship(1280, 780, 1000, 1000, queue, goals);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("Zeppelin Group White");
		gui.setSize(gui.getWidth(), gui.getHeight());
		gui.setVisible(true);
		gui.requestFocus();
	}
	
	public void setUpGoals(){
		goals.add(new GoalHeight(1000));
		//TODO HIER MOETEN DE GOALS KOMEN.
		//WE KUNNEN MISSCHIEN AUTOMATISEREN, MAAR DAS LASTIG :D
	}
	
	private boolean terminate = false;
	public void terminate(){
		terminate = true;
	}
	public void run(){
		//Wachttijd nodig om connectie te initialiseren
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		nextGoal = goals.getFirst();
		
		while(!terminate){
			Command c = null;
			boolean analysePicture = false;
			Thread analyserThread = null;
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
					analysePicture = true;
					gui.updateLastCommand(c.getConsole());
					client.executeCommand(c);
				}
				else if(c instanceof GetHeight){
					String recv = client.executeCommand(c);
					gui.updateZeppHeight(Integer.parseInt(recv));
					gui.updateLastCommand(c.getConsole());
				}
				else{
					client.executeCommand(c);
					gui.updateLastCommand(c.getConsole());
				}
				queue.offer(new GetHeight());
			}
			
			if(analysePicture){
				//TODO Fotoanalyse klasse toevoegen.
				//positionAnalyser = new Thread()
			}
			
			if(System.currentTimeMillis() - lastCheck > 500){
				lastCheck = System.currentTimeMillis();
				
				boolean stillEmpty = false;
				if (queue.isEmpty())
					stillEmpty = true;
				
				queue.offer(new GetHeight());
				
				//Enkel nieuwe foto nemen indien queue nog steeds leeg was, en de laatst genomen foto reeds verwerkt is
				if (stillEmpty && ! isThreadStillAlive(analyserThread))
					queue.add(new TakePicture());
			}
			
			if(checkGoalReached()){
				goals.removeFirst();
				nextGoal = goals.getFirst();
			}
		}
	}
	
	private boolean isThreadStillAlive(Thread t) {
		return (! ((t == null) || t.isAlive()) );
	}	

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
	
	private boolean closeEnough(double current, double target){
		//We werken in mm ok?
		double absoluteMarge = 100;
		if(current - target < absoluteMarge)
			return true;
		return false;
	}
}
