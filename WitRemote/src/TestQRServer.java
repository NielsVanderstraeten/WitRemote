
public class TestQRServer {

	public static void main(String[] args) {
		RabbitClient rabbitClient = new RabbitClient("localhost", "server");
		rabbitClient.sendMessage(QRcode.getPublicKey(), "wit.tablets.tablet" + 1);
	}

}
