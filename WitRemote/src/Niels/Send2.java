package Niels;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Send2 {
	private final static String QUEUE_NAME = "wit.private.terminate";
	public static void main(String[] argv)
			throws java.io.IOException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("wit");
		factory.setPassword("wit");
		factory.setHost("localhost");
		factory.setPort(5672);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		String message = "Niels is geil.";
		channel.basicPublish("server", QUEUE_NAME, null, message.getBytes());
		System.out.println(" [x] Sent '" + message + "'");
		// channel.close();
		connection.close();
	}
}