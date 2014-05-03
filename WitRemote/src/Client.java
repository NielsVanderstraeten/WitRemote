
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import commands.Command;


public class Client implements Runnable {
	private String serverName, path;
	private String namePicture = "recv";
	private static int numberOfPicture = 0;
	private int port, bufferSize;
	private String previousCommand = "";
	private ControlManager cm;

	public Client(String serverName, int port, ControlManager cm){
		this.serverName = serverName;
		this.port = port;
		this.cm = cm;
		this.path = "src/images/"; //Path waar foto's opgeslagen moeten worden
		
//		setUpSocket(serverName, port);
	}

	public int getPort(){
		return port;
	}

	public String getServerName(){
		return serverName;
	}
	
	public String getNamePicture() {
		return namePicture + numberOfPicture + ".jpg";
	}

	
	public synchronized void run(){
		try {
			Socket client = new Socket(getServerName(), getPort());			
			InputStream inFromServer = client.getInputStream();

			numberOfPicture++;
			
			if(numberOfPicture > 9){
				numberOfPicture = 1;
			}

			File file = new File(path + namePicture + numberOfPicture + ".jpg");
			OutputStream outFile = new FileOutputStream(file, false);
			copy(inFromServer, outFile);
			outFile.close();
			inFromServer.close();
			System.out.println("-> Picture saved at " + path + namePicture + numberOfPicture + ".jpg");


			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
//	//Vanaf hier is de nieuwe manier :D
//	private DataInputStream dis;
//	private Socket socket;
//	private ServerSocket serverSocket;
//	private DataOutputStream outToClient;
//	
//	private void setUpSocket(String host, int port){
//		try{
//			socket = new Socket(host, port);
//			bufferSize = socket.getReceiveBufferSize();
//			dis = new DataInputStream(socket.getInputStream());
//			outToClient = new DataOutputStream(socket.getOutputStream());
//		} catch(IOException ioe){
//			ioe.printStackTrace();
//		}
//	}
//	
//	public void run(){
//		try{ 
//			boolean listening = true;
//			while(listening){
//				String requestString = dis.readLine();
//				System.out.println(requestString);
//				if(requestString.equals("QUIT")){
//					System.out.println("quiting...");
//					listening = false;
//					socket.close();
//				}
//				else if(requestString.equals("IMG")){
//					long fileSize = Long.parseLong(dis.readLine());
//					numberOfPicture++;
//					if(numberOfPicture > 9){
//						numberOfPicture = 1;
//					}
//					File file = new File(path+getNamePicture());
//					System.out.println("************"+path+getNamePicture());
//					OutputStream outFile = new FileOutputStream(file, false); //Schrijft nu over eventueel bestaand bestand
//					copy(dis, outFile, fileSize);
//					outFile.close();
//					System.out.println("-> Picture saved at " + path+namePicture);
//					cm.analysePicture(path+namePicture);
//					outToClient.writeBytes("done\r\n");
//					System.out.println("Picture saved");
//				}
//			}
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	
	private void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[8192];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
	}
	
	private void copy(InputStream in, OutputStream out, long contentLength) throws IOException {
		byte[] buf = new byte[bufferSize];
		int n = 0;
		while( contentLength > 0 ){
			n = in.read(buf, 0, (int)Math.min(buf.length, contentLength));
			if( n == -1 ){
				break;
			}
			out.write(buf,0,n);
			contentLength -= n;
		}
	}
}
