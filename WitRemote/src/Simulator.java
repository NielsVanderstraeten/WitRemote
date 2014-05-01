

import goals.Goal;
import goals.GoalHeight;
import goals.GoalPosition;
import gui.KirovAirship;

import java.util.LinkedList;

import javax.swing.JFrame;

import Rooster.Grid;
import commands.Command;

public class Simulator implements Runnable{

	private LinkedList<Command> queue;
	private LinkedList<Goal> goals;
	private Goal nextGoal = null;
	private RabbitClient client;
	private RabbitRecv rabbitRecv;
	private final String host = "localhost";
	private final String exchangeName = "tabor";
	private final static String qrservername = "192.168.2.115";
	private final static int qrportnumber = 5000;
	private final static String qrsimpath = "src/images/qrimsulator.jpg";
	private int lastTablet;
	private Grid grid;
	private QRDownloader qrdl;
	
	public Simulator(String host){
		queue = new LinkedList<Command>();
		goals = new LinkedList<Goal>();
		grid = new Grid("test");
		createGUI();
		setUpConnection();
		goals.addLast(new GoalHeight(100));
		ownX = gui.getOwnX();
		ownY = gui.getOwnY();
		qrdl = new QRDownloader(qrservername, qrportnumber, qrsimpath, goals, grid);
	}
	
	private KirovAirship gui;
	
	private void createGUI(){
		gui = new KirovAirship(1200, 650, KirovAirship.REAL_WIDTH, KirovAirship.REAL_HEIGHT, queue, goals);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("Genious Label Analyser and Distancesensor Operating Zeppelin (G.L.A.D.O.Z.)");
		gui.setSize(gui.getWidth(), gui.getHeight());
		gui.setVisible(true);
		gui.requestFocus();
		gui.setSimulatorPhoto();
	}
	
	private void setUpConnection(){
		client = new RabbitClient(host, exchangeName);
		rabbitRecv = new RabbitRecv(host, exchangeName, gui, true);
		Thread recv = new Thread(rabbitRecv);
		recv.start();
	}
	
	public static void main(String[] argvs) throws InterruptedException{
		Simulator simulator = new Simulator("localhost");
		if(argvs.length > 0 && argvs[0].equals("-debug"))
			simulator.setDebug();
		simulator.run();
	}
	
	public void run(){
		lastCalc = System.currentTimeMillis();
		setNextGoal();
		while(true){
			try {
				Thread.sleep(250); 
			} catch(Exception e){
				System.err.println("Da werkt ni..... stoeme thread sleep");
			}
			if(goalReached())
				setNextGoal();
			getDestination();
			goToDestination();
		}
	}
	
	private int goalX, goalY;
	private double ownX, ownY;
	
	private void getDestination(){
		goalX = gui.getGoalX(); goalY = gui.getGoalY();
		targetHeight = gui.getTargetHeight();
	}

	private double rotation = 0;
	private long lastCalc = 0;
	
	private void goToDestination(){
		long newCalc = System.currentTimeMillis();
		long time = newCalc - lastCalc;
		lastCalc = newCalc;
		calcNextPosition(time);
		calcNextHeight(time);
		gui.updateGui();
	}
	
	private boolean fuzzyEquals(double first, double second){
		if(Math.abs(first - second) < 50)
			return true;
		return false;
	}
	
	private boolean fuzzyEqualsParam(double first, double second, double range){
		if(Math.abs(first - second) < range)
			return true;
		return false;
	}
	
	private double speedX, speedY;
	private static double accelX = 0.00002; //  mm*(ms)^-2
	private static double accelY = 0.00002; //  mm*(ms)^-2
	private static double maxSpeedX = 0.02; // mm/ms
	private static double maxSpeedY = 0.02; // mm/ms
	
	private void calcNextPosition(long time){
		double diffX = goalX - ownX;
		double diffY = goalY - ownY;
		int directionX = (int) Math.signum(diffX);
		int directionY = (int) Math.signum(diffY);
		
		if(goalX == 0 && goalY == 0)
			return;
		if(fuzzyEquals(ownX, goalX)){ //If almost there
			if(speedX == 0)
				;//Do nothing
			else if(fuzzyEqualsParam(speedX, 0, accelX*time)) //If speed is almost 0, we set to 0
				speedX = 0;
			else // If speed is less than 0, we decrease the speed by the acceleration * time, in the opposite direction of the speed (deceleration).
				speedX = speedX - Math.signum(speedX)*accelX*time;
		} else{ // If not there
			if(!isSameDirection(diffX, speedX)) // If we aren't going the right way, we try to accelerate the right way.
				speedX = speedX + directionX * accelX * time;
			else {
				int accel = canSlowDown(diffX, speedX, -directionX*accelX);
				if(accel == 1) //If we can slow down fast enough to get there
					speedX = speedX + directionX * accelX * time; //We go faster
				else if(accel == -1) //If we can't slow down, we should really start deceleration some time soon.
					speedX = speedX - directionX * accelX * time; // we go slower
				//If accel = 0, we do not change the speed.
				if(Math.abs(speedX) > Math.abs(maxSpeedX)) // If we are faster than the maximum speed, then we slow down to max speed.
					speedX = maxSpeedX * directionX;
			}
		}
		
		//Same for Y.
		if(fuzzyEquals(ownY, goalY)){ //If almost there
			if(speedY == 0)
				;//Do nothing
			else if(fuzzyEqualsParam(speedY, 0, accelY*time)) //If speed is almost 0, we set to 0
				speedY = 0;
			else // If speed is less than 0, we decrease the speed by the acceleration * time, in the opposite direction of the speed (deceleration).
				speedY = speedY - Math.signum(speedY)*accelY*time;
		} else{ // If not there
			if(!isSameDirection(diffY, speedY)) // If we aren't going the right way, we try to accelerate the right way.
				speedY = speedY + directionY * accelY * time;
			else {
				int accel = canSlowDown(diffY, speedY, -directionY*accelY);
				if(accel == 1) //If we can slow down fast enough to get there
					speedY = speedY + directionY * accelY * time; //We go faster
				else if(accel == -1) //If we can't slow down, we should really start deceleration some time soon.
					speedY = speedY - directionY * accelY * time; // we go slower
				//If accel = 0, we do not change the speed.
				if(Math.abs(speedY) > Math.abs(maxSpeedY)) // If we are faster than the maximum speed, then we slow down to max speed.
					speedY = maxSpeedY * directionY;
			}
		}
		if(speedX != 0 || speedY != 0){
			ownX = ownX + speedX * time;
			ownY = ownY + speedY * time;
			rotation = Math.atan((goalY - ownY)/(goalX - ownX)) + Math.PI/2;
			if(goalX < ownX)
				rotation += Math.PI;
			
			gui.updateOwnPosition((int) ownX, (int) ownY, rotation);
			client.sendMessage(ownX + " " + ownY, "wit.info.position");
		}
	}
	
	private double height = 0;
	private double targetHeight;
	private double heightSpeed = 0;
	private static double maxSpeedHeight = 0.05;
	private static double accelHeight = 0.00002;
	
	/**
	 * Calculates the next height.
	 * This is based on a parabole flight, with a saved position, speed and a constant acceleration.
	 */
	private void calcNextHeight(long time){
		double diff = targetHeight - height;
		int direction = (int) Math.signum(diff);
		int accel = 0;
		
		if(fuzzyEqualsParam(height, targetHeight, 10)){ //If almost there
			if(heightSpeed == 0)
				;//Do nothing
			else if(fuzzyEqualsParam(heightSpeed, 0, accelHeight*time)) //If speed is almost 0, we set to 0
				heightSpeed = 0;
			else // If speed is less than 0, we decrease the speed by the acceleration * time, in the opposite direction of the speed (deceleration).
				heightSpeed = heightSpeed - Math.signum(heightSpeed)*accelHeight*time;
		} else{ // If not there
			if(!isSameDirection(diff, heightSpeed)) // If we aren't going the right way, we try to accelerate the right way.
				heightSpeed = heightSpeed + direction * accelHeight * time;
			else {
				accel = canSlowDown(diff, heightSpeed, -direction*accelHeight);
				if(accel == 1) //If we can slow down fast enough to get there
					heightSpeed = heightSpeed + direction * accelHeight * time; //We go faster
				else if(accel == -1) //If we can't slow down, we should really start deceleration some time soon.
					heightSpeed = heightSpeed - direction * accelHeight * time; // we go slower
				//If accel = 0, we do not change the speed.
				if(Math.abs(heightSpeed) > Math.abs(maxSpeedHeight)) // If we are faster than the maximum speed, then we slow down to max speed.
					heightSpeed = maxSpeedHeight * direction;
			}
		}
		if(heightSpeed != 0){
			height = height + heightSpeed*time;
			gui.updateZeppHeightMM((int) (height));
			client.sendMessage((int) height + "", "wit.info.height");
		}
	}
	
	private boolean isSameDirection(double position, double speed){
		if(position * speed > 0)
			return true;
		return false;
	}
	
	/**
	 * This method will be able to calculate if we are able to decelerate fast enough to stand still at the goal position.
	 * @param position
	 * @param speed
	 * @param accel
	 * @return int 1 if we have to accelerate. 0 if we have to stay at the same speed. -1 if we have to slow down.
	 */
	private int canSlowDown(double position, double speed, double accel){
		if((speed > 0 && accel > 0) || (speed < 0 && accel < 0)) //If the acceleration and the speed go in the same direction, then we cannot slow down to zero in any amount of time.
			return -1;
		if((position > 0 && speed < 0 && accel > 0) || (position < 0 && speed > 0 && accel < 0)) 
			// If we have to go forward, and the speed is backwards. Then we must reach speed of zero before we got to the right position.
			return 1;
		double timeToTop = -speed/accel;		//Time where the speed = 0, also the top of a parabole. Speed and accel have a different sign, so it has to be different sign.
		double posAtTop = accel*timeToTop*timeToTop/2 + speed*timeToTop; // The position at the top, calculated by a*t²/2 + v*t = 0;
		if(Math.abs(posAtTop) > Math.abs(position)) //If the position were the speed is zero if further than the give position, then we we cannot slow down fast enough
			return -1;
		else if(Math.abs(posAtTop) > Math.abs(position)* 0.7) // If our margin is small, we signal to stay at the same speed. (This also means posAtTop < position so no check is needed)
			return 0;
		else // Else we will be able to slow down fast enough. We can signal to accelerate further.
			return 1;
	}
	
	private boolean goalReached(){
		if(nextGoal instanceof GoalPosition){
			if(fuzzyEquals(ownX, goalX) && fuzzyEquals(ownY, goalY) && speedX == 0 && speedY == 0){
				gui.updateLastCommand("We reached our goal position; x:" + goalX + ", y:" + goalY + ".");
				lastTablet = grid.getTabletNumber(goalX, goalY);
				return true;
			}
		} else if(nextGoal instanceof GoalHeight){
			if(fuzzyEqualsParam(height, targetHeight, 10) && heightSpeed == 0){
				gui.updateLastCommand("We reached our goal height; height:" + targetHeight + ".");
				return true;
			}	
		} else if(nextGoal == null)
			return true;
		return false;
	}
	
	private void setNextGoal(){
		if(goals.isEmpty())
			nextGoal = null;
		nextGoal = goals.poll();
		if(nextGoal instanceof GoalHeight)
			gui.setTargetHeight(((GoalHeight) nextGoal).getTargetHeight());
		else if(nextGoal instanceof GoalPosition)
			gui.setGoalPosition(((GoalPosition) nextGoal).getX(), ((GoalPosition) nextGoal).getY());
		else if(nextGoal == null && lastTablet != -1){
			//qrdl.getPhoto(lastTablet);
		} else if(nextGoal != null)
			System.err.println("Error bij addnextgoal");
	}
	
	public void setDebug(){
		gui.setDebug();
	}
}
