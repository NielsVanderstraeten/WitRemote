package autoTabletControl;


public class Regulator implements Runnable {

	//// OUDE RUN MEHTODE
//	private boolean running;
//	private TabletControlListener autoListener;
//	private int tablet;
//	private boolean listening;
//	private Forwarder myForwarder;
	
	
	
	//// NIEUWE RUN METHODE ////
	//----------------------////
	
	private String exchangeName = "server";
	// EERSTE FORWARDER
	//Declare the two hosts to forward from and to:
	private String forwardFrom1 = "localhost";
	private String forwardTo1 = "localhost";
	
	//Declare the color to forward from
	private String color1 = "wit";
	private boolean isOwnColor1 = true;
	
	// TWEEDE FORWARDER
	boolean playToEnemy = true;
	//Declare the two hosts to forward from and to
	private String forwardFrom2 = "localhost";
	private String forwardTo2 = "localhost";
	
	//Declare the color to forward from
	private String color2 = "appelblauwzeegroen";
	private boolean isOwnColor2 = false;
	
	
	
	public Regulator() {
//		running = true;
	}
	
	public static void main(String[] args){
		Regulator reg = new Regulator();
		reg.run();
	}
	
	@Override
	public synchronized void run() {
		try {
			Forwarder forwarder1 = new Forwarder(forwardFrom1,forwardTo1,exchangeName,color1,isOwnColor1);
			Thread forw1 = new Thread(forwarder1);
			forw1.start();
			if (playToEnemy) {
				System.out.println("made forwarder 2");
				Forwarder forwarder2 = new Forwarder(forwardFrom2,forwardTo2,exchangeName,color2,isOwnColor2);
				Thread forw2 = new Thread(forwarder2);
				forw2.start();
			}
			
		} catch (Exception e) {
			
		}
	}
	
//	@Override
//	public synchronized void run(){
//		try {
//			while(running) {
//				autoListener = new TabletControlListener("localhost", "server");
//				System.out.println("ok");
//				autoListener.run();
//				System.out.println("ok");
//				listening = true;
//				while (listening) {
//					if (autoListener.getCloseEnough()) {
//						listening = false;
//						tablet = autoListener.getTabletNumber();
//					}
//				}
//				System.out.println("uit listeningLoop");
//				autoListener.stopRunning();
//				System.out.println("stoppedRunning!!!!");
//				//TODO: host veranderen!
//				TabletControlClient autoClient = new TabletControlClient("localhost", "server");
//				autoClient.sendMessage(QRcode.getPublicKey(), "wit.tablets.tablet" + tablet);
//			}
//		} catch (Exception e) {
//			
//		}
//		
//	}
}
