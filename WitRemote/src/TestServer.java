

import java.io.IOException;
import java.util.ArrayList;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
  
public class TestServer implements Runnable{
	
	private QueueingConsumer consumer;
	private Channel channel;
	private String queueName, exchangeName;
	
	public TestServer(String host, String exchangeName) {
		setUpTopics();
		setUpConnection(host, exchangeName);
	}
	
	private void setUpConnection(String host, String exchangeName){
		Connection connection = null;
		try {
			this.exchangeName = exchangeName;
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(host);
			
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			channel.exchangeDeclare(exchangeName, "topic");
			queueName = channel.queueDeclare().getQueue();
			
			declareTopicBinds();
			
			channel.basicQos(1);
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			System.out.println(" [x] Awaiting RPC requests");
		}
		catch(Exception e){
			System.err.println("Error in TestServer constructor");
		}
	}
	
	public void run() {
		try{
			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody(),"UTF-8");
				String topic = delivery.getEnvelope().getRoutingKey();
				
				System.out.println("[.] " + topic + ": " + message);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private ArrayList<String> topics;
	private void setUpTopics(){
		topics = new ArrayList<String>();
		topics.add("white.#");
	}
	
	private void declareTopicBinds() throws IOException{
		for(String topic: topics)
			channel.queueBind(queueName, exchangeName, topic);
	}
	
	private String parse(String message){
		
		return null;
	}
	
	public static void main(String[] args){
		TestServer server = new TestServer("localhost", "server");
		server.run();
	}
  }

