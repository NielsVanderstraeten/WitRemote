

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import commands.*;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitClient implements Runnable{

	//Naam van het kanaal waaar we een 1 op 1 verbinding met de server krijgen.
	private String replyQueueName;
	//Naam van de verbinding waar we naartoe moeten.
	private String requestQueueName = "server";
	private String exchangeName;
	private Connection connection;
	private Channel channel;
	private QueueingConsumer consumer;
	private int port;
	private String server, path;
	private String namePicture = "recv";
	private int numberOfPicture = 0;
	
	private InputStream inFromServer;
	private Socket socket;
	
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
	
	public String getNamePicture() {
		return namePicture + numberOfPicture + ".jpg";
	}
	
	private void setUpConnection(String host, String exchangeName){
		try{
			//Setting up connection
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(host);
			connection = factory.newConnection();	
			channel = connection.createChannel();
			channel.exchangeDeclare(exchangeName, "topic"); //todo server en variabele maken
//			//Setting up reply
//			replyQueueName = channel.queueDeclare().getQueue();
//			consumer = new QueueingConsumer(channel);
//			channel.basicConsume(replyQueueName, true, consumer);
		} catch(IOException ex){
			System.out.println("Error in setUpConnection");
		}
	}
	
	public String sendMessage(String message, String topic){
		String response = null;
		try{
			//Sending the message
			channel.basicPublish(exchangeName, topic, null, message.getBytes());
			System.out.println("We have a lift-off: Sent '" + message+"'. Topic: " + topic);
			
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
			String topic = "white." + command.getTopic();
			
			sendMessage(str, topic);
			
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
		client.sendMessage("spam", "white.private.terminate");
		client.sendMessage("true", "white.private.terminate");
		client.closeChannel();
	}
	
	public void run(){
		System.out.println("Running Client");
	}
}
