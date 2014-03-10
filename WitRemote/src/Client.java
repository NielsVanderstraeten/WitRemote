
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import commands.Command;

public class Client
{
	private String serverName, path;
	private String namePicture = "recv.jpg";
	private int port;
	private String previousCommand = "";

	public Client(String serverName, int port, String path){
		this.serverName = serverName;
		this.port = port;
		this.path = path; //Path waar foto's opgeslagen moeten worden
	}

	public int getPort(){
		return port;
	}

	public String getServerName(){
		return serverName;
	}
	
	public String getNamePicture() {
		return namePicture;
	}

	
	/*
	public void executeCommand(Command cmd){
		executeCommand(cmd.toString());
	}
	*/
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
				namePicture = "recv.jpg"; //Kan eventueel vervangen worden door dynamische bestandsnaam. Nu wordt telkens de laatste foto overschreven door een nieuwe
				File file = new File(path+namePicture);
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
	
	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[8192];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
	}
}
