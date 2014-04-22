import gui.KirovAirship;

import java.io.IOException;
import java.util.ArrayList;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitRecv implements Runnable{

	private QueueingConsumer consumer;
	private Channel channel;
	private String queueName, exchangeName;
	private KirovAirship gui;
	
	public RabbitRecv(String host, String exchangeName, KirovAirship gui) {
		setUpTopics();
		setUpConnection(host, exchangeName);
		this.gui = gui;
	}
	
	private void setUpConnection(String host, String exchangeName){
		Connection connection = null;
		try {
			this.exchangeName = exchangeName;
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUsername("wit");
			factory.setPassword("wit");
			factory.setHost(host);
			factory.setPort(5673);
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			channel.exchangeDeclare(exchangeName, "topic");
			queueName = channel.queueDeclare().getQueue();
			
			declareTopicBinds();
			
			channel.basicQos(1);
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			System.out.println("[x] Awaiting RPC requests");
		}
		catch(Exception e){
			System.err.println("Error in TestServer constructor");
		}
	}
	
	public void run() {
		try{
			QueueingConsumer.Delivery delivery;
			String message;
			String topic;
			boolean terminated = false;
			while(!terminated) {
				topic = ""; message = ""; delivery = null;
				delivery = consumer.nextDelivery();
				message = new String(delivery.getBody(),"UTF-8");
				topic = delivery.getEnvelope().getRoutingKey();
				
				if(topic.equals("wit.info.height"))
					gui.updateZeppHeightMM(Integer.parseInt(message));
				
				else if(topic.equals("wit.private.terminate")){
					System.out.println(message);
					if(message.equalsIgnoreCase("true"))
						terminated = true;
				}
				System.out.println("[.] " + topic + ": " + message);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("terminate");
	}
	private ArrayList<String> topics;
	private void setUpTopics(){
		topics = new ArrayList<String>();
		topics.add("wit.info.height");
		topics.add("wit.private.#");
	}
	
	private void declareTopicBinds() throws IOException{
		for(String topic: topics)
			channel.queueBind(queueName, exchangeName, topic);
	}
	
	public static void main(String[] args){
		RabbitRecv recv = new RabbitRecv("localhost", "tobar", new KirovAirship());
		recv.run();
	}

}