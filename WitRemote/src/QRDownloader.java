
import goals.Goal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

import Rooster.Grid;

public class QRDownloader {
	private String host, path;
	private int port, bufferSize;
	private LinkedList<Goal> goals;
	private Grid grid;

	public QRDownloader(String host, int port, String path, LinkedList<Goal> goals, Grid grid){
		this.host = host;
		this.port = port;
		this.goals = goals;
		this.path = path; //Path waar foto's opgeslagen moeten worden
		this.grid = grid;
		setUpSocket(host, port);
		
	}

	public int getPort(){
		return port;
	}

	public String getHost(){
		return host;
	}

	
	//Vanaf hier is de nieuwe manier :D
	private InputStream inFromServer;
	private Socket socket;
	private DataOutputStream outToServer;
	
	private void setUpSocket(String host, int port){
		try{
			socket = new Socket(host, port);
			bufferSize = socket.getReceiveBufferSize();
			inFromServer = socket.getInputStream();
			outToServer = new DataOutputStream(socket.getOutputStream());
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	public void getPhoto(int tabletNumber){
		try{ 
			int length = 0;
			//TODO juiste string
			String toServer = "GET static/wit" + tabletNumber + ".png HTTP/1.1\nHost: " + host + ":" + port;
			outToServer.writeUTF(toServer);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(inFromServer));
			String nextLine = in.readLine();
			while(!nextLine.isEmpty() && nextLine != null){
				if(nextLine.startsWith("Content-Length")){
					String[] words = nextLine.split("[ ]+");
					length = Integer.parseInt(words[1]);
				}
				nextLine = in.readLine();
			}
			
			OutputStream toFile = new FileOutputStream(path);
			copy(inFromServer, toFile, length);
			System.out.println("Received QR code from tablet " + tabletNumber +".");
			
			(new QRcode(goals, grid, path)).run();
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
