package autoTabletControl;


import java.io.IOException;
import java.util.ArrayList;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class Forwarder implements Runnable{

	private QueueingConsumer consumer;
	private Channel channel;
	private String queueName, exchangeName;
	private String forwardFrom;
	private String forwardTo;
	private String color;
	private boolean colorIsEnemy;
	private ForwardClient myClient;

	public Forwarder(String forwardFrom, String forwardTo, String exchangeName, String color, boolean isOwnColor) throws SecurityException, IOException {
		this.forwardFrom = forwardFrom;
		this.forwardTo = forwardTo;
		this.color = color;
		this.exchangeName = exchangeName;
		this.colorIsEnemy = !isOwnColor;
		System.out.println("color = " + color + " and enemy = " + colorIsEnemy);
		myClient = new ForwardClient(forwardTo,exchangeName,color);
	}

	private void setUpConnection(){
		System.out.println("setting up conn");
		Connection connection = null;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUsername("wit");
			factory.setPassword("wit");
			factory.setHost(forwardFrom);
			factory.setPort(5672);			
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
			e.printStackTrace();
		}
	}

	private boolean running = true;
	public synchronized void run() {
		setUpTopics();
		setUpConnection();
		try{
			QueueingConsumer.Delivery delivery;
			String message;
			String topic;
			
			//TODO wa geprul voor tabor-tabor testen
			int count = 0;
			
			while(running) {
				topic = ""; message = ""; delivery = null;
				delivery = consumer.nextDelivery();
				message = new String(delivery.getBody(),"UTF-8");
				topic = delivery.getEnvelope().getRoutingKey();
				
				if (topic.startsWith(color + ".tablets.") && !colorIsEnemy && count <1) {
					myClient.sendMessage(message, topic);
					System.out.println("forwarded from: " + forwardFrom + " "+ "to: " + forwardTo + ". the next message:");
					System.out.println(message);
					if (forwardFrom.equals(forwardTo)) {
						count++;
					}	
				}
				else if(topic.equals(color +".info.location") && count < 2){
					myClient.sendMessage(message, topic);
					System.out.println("forwarded from: " + forwardFrom + " "+ "to: " + forwardTo + ". the next message:");
					System.out.println(message);
					if (forwardFrom.equals(forwardTo)) {
						count++;
					}	
				} else {
					System.out.println("did not forward but listened on: " + topic + " to: " + message);
				}			
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
		topics.add(color + ".info.location");
		topics.add(color + ".hcommand.elevate");
		topics.add(color + ".hcommand.move");
		topics.add(color + ".private.terminate");
		topics.add(color + ".private.sendPicture");
		topics.add(color + ".private.rotation");
		topics.add(color + ".tablets.tablet1");
		topics.add(color + ".tablets.tablet2");
		topics.add(color + ".tablets.tablet3");
		topics.add(color + ".tablets.tablet4");
	}

	public void stopRunning(){
		running = false;
	}

	private void declareTopicBinds() throws IOException{
		for(String topic: topics)
			channel.queueBind(queueName, exchangeName, topic);
	}
	
	public static void main(String[] args) throws SecurityException, IOException{
		TabletControlListener listener = new TabletControlListener("localhost", "server");
		listener.run();
	}
}