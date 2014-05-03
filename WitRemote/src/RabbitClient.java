

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import commands.Command;
import commands.SetPosition;

public class RabbitClient implements Runnable{

	private String exchangeName;
	private Connection connection;
	private Channel channel;
	private int port;
	private String server;
//	private String namePicture = "recv";
//	private int numberOfPicture = 0;
	
	public RabbitClient(String host, String exchangeName){
		setUpConnection(host, exchangeName);
		this.exchangeName = exchangeName;
	}
	
	public int getPort(){
		return port;
	}

	public String getServerName(){
		return server;
	}
	
//	public String getNamePicture() {
//		return namePicture + numberOfPicture + ".jpg";
//	}
	
	private void setUpConnection(String host, String exchangeName){
		try{
			//Setting up connection
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUsername("wit");
			factory.setPassword("wit");
			factory.setHost(host);
			factory.setPort(5672);
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(exchangeName, "topic"); 
		} catch(IOException ex){
			System.out.println("Error in setUpConnection");
			ex.printStackTrace();
		}
	}
	
	public String sendMessage(String message, String topic){
		String response = null;
		try{
			//Sending the message
			channel.basicPublish(exchangeName, topic, null, message.getBytes());
			System.out.println("[X] Sent '" + message+"'. Topic: " + topic);
			
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return response;
	}
	
	public void receiveMessage(){
		
	}
	
	public String executeCommand(Command command){
		String returnMessage = "";
//		try{
		
			String str = command.getPiCommand();
			String topic = "wit." + command.getTopic();
			sendMessage(str, topic);
			
			//FIRSTWORLDANARCHISTS
			if(command instanceof SetPosition){
				double rot = ((SetPosition) command).getRotation();
				topic = "wit.private.rotation";
				sendMessage(rot + "", topic);
			}
//			if(str.equals("takepicture")){
//				sendMessage(str, false);
//				numberOfPicture++;
//				if(numberOfPicture > 9){
//					numberOfPicture = 1;
//				}
//				File file = new File(path+getNamePicture());
//				System.out.println("[PICT] "+path+getNamePicture());
//				OutputStream outFile = new FileOutputStream(file, false); //Schrijft nu over eventueel bestaand bestand
//				copy(inFromServer, outFile);
//				outFile.close();
//				inFromServer.close();
//				System.out.println("-> Picture saved at " + path+namePicture);
//				
//				returnMessage = "Succesfully received picture " + getNamePicture();
//			} else 
//			if(str.equals("getheight")){
//				returnMessage = sendMessage(str, "white." + topic);
//			} else
//				sendMessage(str, false);
//		} catch(IOException ioe){
//			System.err.println("Error in executeCommand");
//		}
		return returnMessage;
	}

	private void closeChannel(){
		try{
			channel.close();
			connection.close();
		} catch(IOException ex){
			System.out.println("Error in closeChannel");
		}
	}
		
	public static void main(String[] argvs) throws InterruptedException{
		RabbitClient client = new RabbitClient("localhost", "server");
		//client.sendMessage("1000", "white.info.height");
		client.sendMessage("2000 2000", "wit.hcommand.move");
		client.sendMessage("5000", "wit.hcommand.elevate");
		client.sendMessage("1000 1000", "wit.info.position");
		client.sendMessage("true", "wit.private.terminate");
		client.closeChannel();
	}
	
	public void run(){
		System.out.println("Running Client");
	}
}
