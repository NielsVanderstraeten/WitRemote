import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;


public class TestQRCode {

	@Before
	public void initialise() throws IOException {
		QRcode.initialiseKeys();

		String encrypted = encrypt("position:15,60");

		System.out.println("Encrypted: " + encrypted);

		String decrypted = decrypt(encrypted);

		System.out.println("Decrypted: " + decrypted);

		System.out.println("======================");

		ByteArrayOutputStream byteout = QRCode.from(encrypted).to(ImageType.JPG).stream();
		FileOutputStream fileout = new FileOutputStream(new File("src/gui/resources/testQRcode.jpg"));
		fileout.write(byteout.toByteArray());
		fileout.close();		
	}

	@Test
	public void test() {
		QRcode test = new QRcode(null, "src/gui/resources/testQRcode.jpg");
		test.run();
	}

	private String encrypt(String text) {
		byte[] cipherText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA");
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, QRcode.getPublicKey());
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Base64.encodeBase64String(cipherText);
	}

	private String decrypt(String text64) {
		//TODO: kijken welke String-codering de scheidsrechtercommissie beslist
		byte[] text = Base64.decodeBase64(text64);		
		byte[] decryptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA");
			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, QRcode.getPrivateKey());	      
			decryptedText = cipher.doFinal(text);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(decryptedText);
	}

}
