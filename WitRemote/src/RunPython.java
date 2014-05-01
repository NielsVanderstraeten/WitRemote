import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class RunPython {

	
	public void run(){
		try {
			Runtime.getRuntime().exec("cmd /c python keys.py");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String key = read("public");
		//SEND KEY TO RABBITMQ
		
		//READ QR CODE
		String text = QR_Decoder.decodeQR("image.jpg");
		write("encrypted", text);
		
		try {
			Runtime.getRuntime().exec("cmd /c python decryption.py");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		read("result");
		
	}
	
	
	public String read(String fileName){
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader("result"));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			result = sb.toString();
			br.close();
		} catch(IOException e) {
		}
		return result;
	}
	
	public void write(String fileName, String text){
		PrintWriter writer;
		try {
			writer = new PrintWriter("encrypted", "UTF-8");
			writer.println(text);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
		}
	}
}
