import java.io.IOException;


public class TestQRCode {

	public static void main(String[] args) throws IOException {
		QRcode.initialiseKeys();

		try {
			Process tr = Runtime.getRuntime().exec("cmd /c C:/Python27/python.exe encrypt.py");
			tr.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		String text = QRcode.read("encrypted");

		GenerateEncryptedQR generator = new GenerateEncryptedQR(text);
		generator.run();

		QRcode qrcoder = new QRcode(null, "src/gui/resources/testQRcode.jpg");

		try {
			qrcoder.run();
		} catch (NullPointerException e) {
		}

		String encrypted = QRcode.read("encrypted");
		System.out.println("Encrypted: \"" + encrypted + "\"");

		String decrypted = QRcode.read("result");
		System.out.println("Decrypted: \"" + decrypted + "\"");	
	}

}
