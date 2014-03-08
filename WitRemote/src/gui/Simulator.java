package gui;

import java.util.LinkedList;

import javax.swing.JFrame;

import commands.*;
import goals.*;

public class Simulator implements Runnable{

	private LinkedList<Command> queue;
	private LinkedList<Goal> goals;
	private Goal nextGoal;
	public Simulator(String host){
		queue = new LinkedList<Command>();
		goals = new LinkedList<Goal>();
		createGUI();
	}
	private KirovAirship gui;
	
	private void createGUI(){
		gui = new KirovAirship(1280, 780, 4000, 4000, queue, goals);;
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("Fancy");
		gui.setSize(gui.getWidth(), gui.getHeight());
		gui.setVisible(true);
		gui.requestFocus();
	}
	public static void main(String[] argvs) throws InterruptedException{
		Simulator simulator = new Simulator("localhost");
		simulator.run();
	}
	
	public void run(){
		int i = 0;
		int maxvalue = 100;
		while(i<maxvalue){
			try {
				Thread.sleep(5000/maxvalue); }
			catch(Exception e){
				System.err.println("Da werkt ni..... stoeme thread sleep");
			}
			getDestination();
			goToDestination();
		}
	}
	
	int goalX, goalY;
	double ownX, ownY;
	double oppX, oppY;
	
	private void getDestination(){
		ownX = gui.getOwnX(); ownY = gui.getOwnY();
		oppX = gui.getOpponentX(); oppY = gui.getOpponentY();			
	}
	
	private void goToDestination(){
		if(!(fuzzyEquals(ownY, goalY) && fuzzyEquals(ownX, goalX)) && !(fuzzyEquals(oppY, goalY) && fuzzyEquals(oppX, goalX))){
			double distanceOwn = Math.sqrt(Math.pow(goalY - ownY, 2) + Math.pow(goalX - ownX, 2));
			ownX = ownX + 5/distanceOwn*(goalX - ownX);
			ownY = ownY + 5/distanceOwn*(goalY - ownY);
			double distanceOpp = Math.sqrt(Math.pow(goalY - oppY, 2) + Math.pow(goalX - oppX, 2));
			oppX = oppX + 5/distanceOpp*(goalX - oppX);
			oppY = oppY + 5/distanceOpp*(goalY - oppY);
		}
		gui.updateOwnPosition((int) ownX, (int) ownY);
		gui.updateOpponentPosition((int) oppX, (int) oppY);
		gui.updateGui();
	}
	
	private boolean fuzzyEquals(double first, double second){
		if(Math.abs(first - second) < 5)
			return true;
		return false;
	}
}
