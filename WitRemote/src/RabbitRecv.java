import gui.KirovAirship;

import java.io.IOException;
import java.util.ArrayList;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class RabbitRecv implements Runnable{

	private QueueingConsumer consumer;
	private Channel channel;
	private String queueName, exchangeName;
	private KirovAirship gui;
	private final String enemy = "zwart";
	private boolean simulator;
	
	public RabbitRecv(String host, String exchangeName, KirovAirship gui) {
		setUpTopics();
		setUpConnection(host, exchangeName);
		this.gui = gui;
		simulator = false; 
	}
	
	public RabbitRecv(String host, String exchangeName, KirovAirship gui, boolean sim) {
		setUpTopics();
		setUpConnection(host, exchangeName);
		this.gui = gui;
		simulator = sim;
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
			System.err.println("Error in RabbitRecv setUpConnection");
			e.printStackTrace();
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
				
				if(topic.equals("wit.info.height") && !simulator)
					gui.updateZeppHeightMM(Integer.parseInt(message));
				else if(topic.equals("wit.private.terminate")){
					System.out.println(message);
					if(message.equalsIgnoreCase("true"))
						terminated = true;
				} else if(topic.equals(enemy +".info.position")){
					String[] words = message.split("[ ]+");
					gui.updateOpponentPosition(Integer.parseInt(words[0]), Integer.parseInt(words[1]));
				}
				System.out.println("[.] " + topic + ": " + message);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("terminate");
		System.exit(0);
	}
	private ArrayList<String> topics;
	private void setUpTopics(){
		topics = new ArrayList<String>();
		topics.add("wit.info.height");
		topics.add("wit.private.#");
		topics.add(enemy + ".info.position");
	}
	
	private void declareTopicBinds() throws IOException{
		for(String topic: topics)
			channel.queueBind(queueName, exchangeName, topic);
	}
	
	public static void main(String[] args){
		RabbitRecv recv = new RabbitRecv("localhost", "tabor", new KirovAirship());
		recv.run();
	}

}