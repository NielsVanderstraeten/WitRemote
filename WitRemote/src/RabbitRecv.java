import gui.KirovAirship;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
	private int numberOfPicture = 0;
	private ControlManager cm;
	private String path = "src/images/";
	private String namePicture;
	
	public RabbitRecv(String host, String exchangeName, KirovAirship gui, ControlManager cm) {
		setUpTopics();
		setUpConnection(host, exchangeName);
		this.gui = gui;
		simulator = false;
		this.cm = cm;
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
					gui.updateZeppHeightMM((int) (Double.parseDouble(message)*10));
				else if(topic.equals("wit.private.terminate")){
					System.out.println(message);
					if(message.equalsIgnoreCase("true"))
						terminated = true;
				} else if(topic.equals(enemy +".info.location")){
					String[] words = message.split("[ ]+");
					gui.updateOpponentPosition(Integer.parseInt(words[0]), Integer.parseInt(words[1]));
//				} else if(topic.equalsIgnoreCase("wit.private.recvPicture")){
//					numberOfPicture++;
//					if(numberOfPicture > 9){
//						numberOfPicture = 1;
//					}
//					namePicture = "recv" + numberOfPicture + ".jpg";
//					File file = new File(path+namePicture);
//					
//					byte[] data = delivery.getBody();
//					
//					OutputStream outFile = new FileOutputStream(file, false); //Schrijft nu over eventueel bestaand bestand
//					BufferedOutputStream bout = new BufferedOutputStream(outFile);
//					long done = 0;
////					System.out.println(size +"");
//					
//					String test = new String(data, "UTF-8");
//					while(! test.equals("end")){
//						bout.write(data);
//						done = done + data.length;
//						delivery = consumer.nextDelivery();
//						data = delivery.getBody();
//						test = new String(data, "UTF-8");
//					}
//					outFile.close();
					
//					System.out.println("[.] New picture downloaded: " + path+namePicture);
//					cm.analysePicture(path+namePicture);
				}
				
				//Hoogte receiven afgezet //TODO
//				if(topics.contains(topic) && !topic.equals("wit.private.recvPicture"))
//					System.out.println("[.] " + topic + ": " + message);
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
		topics.add("wit.private.recvPicture");
	}
	
	private void declareTopicBinds() throws IOException{
		for(String topic: topics)
			channel.queueBind(queueName, exchangeName, topic);
	}
	
	public static void main(String[] args){
		RabbitRecv recv = new RabbitRecv("localhost", "server", new KirovAirship(), null);
		recv.run();
	}

}