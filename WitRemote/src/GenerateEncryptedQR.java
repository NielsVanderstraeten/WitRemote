import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;


public class GenerateEncryptedQR {
	
	private String text; 
	
	public GenerateEncryptedQR(String text) {
		this.text = text;
	}
	
	public void run() {
		try {
			ByteArrayOutputStream byteout = QRCode.from(text).to(ImageType.JPG).withSize(500, 500).stream();
			FileOutputStream fileout = new FileOutputStream(new File("src/gui/resources/testQRcode.jpg"));
			fileout.write(byteout.toByteArray());
			fileout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
