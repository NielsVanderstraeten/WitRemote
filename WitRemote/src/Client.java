
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
	private int numberOfPicture = 0;
	private int port, bufferSize;
	private String previousCommand = "";
	private ControlManager cm;

	public Client(String serverName, int port, String path, ControlManager cm){
		this.serverName = serverName;
		this.port = port;
		this.path = path; //Path waar foto's opgeslagen moeten worden
		
		setUpSocket(port);
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

	
	/*
	public void executeCommand(Command cmd){
		executeCommand(cmd.toString());
	}
	*/
	//De bedoeling is dat de socket connectie enkel de foto's ontvangt. Daarom zal de executeCommand vervangen worden door de executeCommand van RabbitClient
	@Deprecated
	public String executeCommand(Command c){
		String str = c.getPiCommand();
		String returnMsg = "";
		try{
			Socket client = new Socket(getServerName(), getPort());
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(str);
			System.out.println("Sent commando: " + str); //Debug: print verstuurd commando af
			
			if(str.equals("takepicture")){
				InputStream inFromServer = client.getInputStream();
				
				numberOfPicture++;
				if(numberOfPicture > 9){
					numberOfPicture = 1;
				}
				File file = new File(path+getNamePicture());
				System.out.println("************"+path+getNamePicture());
				OutputStream outFile = new FileOutputStream(file, false); //Schrijft nu over eventueel bestaand bestand
				copy(inFromServer, outFile);
				outFile.close();
				inFromServer.close();
				System.out.println("-> Picture saved at " + path+namePicture);
			}
			else if(str.equals("getheight")){
				DataInputStream in = new DataInputStream(client.getInputStream());
				returnMsg = in.readUTF();
				System.out.println("-> PiState:" + returnMsg);
			}
			else
				System.out.println("-> Sent."); //Voor debug
			System.out.println("--------------------------");
						
			previousCommand = str;
			client.close();	
		}
		catch (ConnectException e) {
			System.out.println("Connectie nog niet aanvaard.");
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return returnMsg + 5;
	}
	
	//Vanaf hier is de nieuwe manier :D
	private DataInputStream dis;
	private Socket socket;
	private ServerSocket serverSocket;
	private DataOutputStream outToClient;
	
	private void setUpSocket(int port){
		try{
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			bufferSize = socket.getReceiveBufferSize();
			dis = new DataInputStream(socket.getInputStream());
			outToClient = new DataOutputStream(socket.getOutputStream());
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	public void run(){
		try{ 
			boolean listening = true;
			while(listening){
				String requestString = dis.readLine();
				System.out.println(requestString);
				if(requestString.equals("QUIT")){
					System.out.println("quiting...");
					listening = false;
					socket.close();
				}
				else if(requestString.equals("IMG")){
					long fileSize = Long.parseLong(dis.readLine());
					numberOfPicture++;
					if(numberOfPicture > 9){
						numberOfPicture = 1;
					}
					File file = new File(path+getNamePicture());
					System.out.println("************"+path+getNamePicture());
					OutputStream outFile = new FileOutputStream(file, false); //Schrijft nu over eventueel bestaand bestand
					copy(dis, outFile, fileSize);
					outFile.close();
					System.out.println("-> Picture saved at " + path+namePicture);
					cm.analysePicture();
					outToClient.writeBytes("done\r\n");
					System.out.println("Picture saved");
				}
			}
			serverSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
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
