package autoTabletControl;


import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ForwardClient implements Runnable{

	private String exchangeName;
	private Connection connection;
	private Channel channel;
	private int port;
	private String server;
	private String colorToForward;
	
	public ForwardClient(String host, String exchangeName, String colorToForward,int toPort){
		this.port = toPort;
		this.exchangeName = exchangeName;
		this.colorToForward = colorToForward;
		setUpConnection(host, exchangeName);		
	}
	
	public int getPort(){
		return port;
	}

	public String getServerName(){
		return server;
	}
	
	private void setUpConnection(String host, String exchangeName){
		try{
			//Setting up connection
			System.out.println("setting up connection");
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
	
	public void sendMessage(String message, String topic){
		try{
			//Sending the message
			channel.basicPublish(exchangeName, topic, null, message.getBytes());
			System.out.println("[X] Sent '" + message+"'. Topic: " + topic);
			
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	

	private void closeChannel(){
		try{
			channel.close();
			connection.close();
		} catch(IOException ex){
			System.out.println("Error in closeChannel");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
