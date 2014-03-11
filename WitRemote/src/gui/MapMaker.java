package gui;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.FileReader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import au.com.bytecode.opencsv.CSVReader;

public class MapMaker extends JPanel {
	private String[] code;
	private int colums, rows, width, height;
	private static final double standardScale = 42.5;
	private static final double standardTotalScale = 71;
	private double scaleX, scaleY;
	private final double zeppScale = 1.2;	
	private int tempwidth, tempheight;
	
	public MapMaker(int width, int height, int colums, int rows, String[] code){
		this.colums = colums;
		this.rows = rows;
		this.height = height;
		this.width = width;
		this.code = code;
		createShapes();
		setTemps();
		//setUpMap();
	}
	
	public MapMaker(int width, int height){
		this.height = height;
		this.width = width;
		parseCSV();
		setTemps();
		createShapes();
		//setUpMap();
	}
	
	private void setTemps(){
		//WHY? CUZ IT WORKS!
		tempwidth = (int) (width + (standardTotalScale - standardScale)/standardTotalScale*width/colums);
		tempheight = (int) (height + (standardTotalScale - standardScale)/2.1/standardTotalScale*height/rows);
		scaleX = 2*tempwidth/(2*colums+1)/standardTotalScale; 
		scaleY = tempheight/rows/standardTotalScale;
	}
	
	@Override
	public int getWidth(){
		return width;
	}
	
	@Override
	public int getHeight(){
		return height;
	}
	
	@Override
	public void paintComponent (Graphics g){
		int x = 0;
		int y = 0;
		// de temp vars die we mogen gebruiken is:
		
		Color color;
		Area toPaint = null;
		Graphics2D g2 = (Graphics2D)g;
		boolean indent = true;
		
		if (code != null && code.length > 0){
			for(int i = 0; i < code.length; i++){
				String info = code[i];
				color = Color.pink;
				if( y > height){
					System.err.println("Te veel symbolen om weer te geven in de figuur.\n@MapMaker - Paint: rows= " + rows + " ,colums= " + colums + "code.length= " + code.length);
					System.exit(1);
				}
				if(info != null && info.length()==2 && !info.equalsIgnoreCase("xx")){
					String[] letters = info.split("(?!^)");
					//Kleur toewijzen
					if(letters[0].equals("G"))
						color = Color.green;
					else if(letters[0].equals("R"))
						color = Color.red;
					else if(letters[0].equals("Y"))
						color = Color.yellow;
					else if(letters[0].equals("B"))
						color = Color.blue;
					else if(letters[0].equals("W"))
						color = Color.white;
					else 
						color = Color.pink;
					
					//De figuur maken
					if(letters[1].equals("R"))
						toPaint = drawSquare(x,y);
					else if(letters[1].equals("C"))
						toPaint = drawCircle(x,y);
					else if(letters[1].equals("H"))
						toPaint = drawHeart(x,y);
					else if(letters[1].equals("S"))
						toPaint = drawStar(x,y);
					else
						toPaint = drawRetard(x,y);
					
					g2.setColor(color);
					g2.draw(toPaint);
					g2.fill(toPaint);
				}
				//Volgende coordinaten berekenen
				//We gebruiken tempwidth om juist de plaatsen te berekenen. Omdat we alle figuren plaatsen in hun linksbovenhoek en met een standaardwaarde werken
				//moeten we colums - 1 gebruiken.
				//Omdat we met een inspringing werken, gebruiken we 2*colums, zodat de andere kan inspringen in de helft.
				x = x + 2*tempwidth/(2*colums+1);
				//Wanneer de linksbovenhoek groter wordt dan de breedtte - de marge - de grootte van het beeldje, moeten we de volgende lijn beginnen.
				if(x > width - tempwidth/(2*colums+1)){
					//Als we de vorige keer geen indent hebben gemaakt, moet dit nu wel.
					if(indent)
						x = (int) Math.floor(tempwidth/(2*colums+1));
					else
						x = 0;
					y = y + tempheight/rows;
					indent = !indent;
				}
			}
		}
		g2.setColor(Color.cyan);
		g2.draw(ownZepp);
		g2.fill(ownZepp);
		
		g2.setColor(Color.magenta);
		g2.draw(oppZepp);
		g2.fill(oppZepp);
		
		g2.setColor(Color.black);
		g2.draw(firstZepp);
		g2.fill(firstZepp);
	}
	
	private Area drawSquare(int x, int y){
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		at.translate(x/scaleX,y/scaleY);
		
		Area newSquare = (Area) square.clone();
		newSquare.transform(at);
		
		return newSquare;
	}
	
	private Area drawCircle(int x, int y){
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		at.translate(x/scaleX, y/scaleY);
		
		Area newCircle = (Area) circle.clone();
		newCircle.transform(at);
		
		return newCircle;
	}
	
	private Area drawHeart(int x, int y){
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		at.translate((x-5)/scaleX,y/scaleY);
		
		Area newHeart = (Area) heart.clone();
		newHeart.transform(at);
		
		return newHeart;
	}
	
	private Area drawStar(int x, int y){
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		//Scaling telt ook voor de translatie, dus die moeten we er terug uit halen.
		at.translate(x/scaleX,y/scaleY);
		
		Area newStar = (Area) star.clone();
		newStar.transform(at);
		
		return newStar;
	}
	
	private Area drawRetard(int x, int y){
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		at.translate(x/scaleX, y/scaleY);
		at.rotate(Math.PI/4);

		Area newRetard = (Area) square.clone();
		newRetard.transform(at);
		
		return newRetard;
	}
	
	private Area square, circle, star, heart;
	
	private void createShapes(){
		//Scource http://www.cs.bham.ac.uk/~szh/teaching/graphics/sourcecode/DrawHeart.java
		
		//De shapes worden gemaakt als in een 41 op 41 vierkant. Dit is de standaardschaling.
		Ellipse2D circle1 = new Ellipse2D.Double(0, 0, 30, 30);
		Ellipse2D circle2 = new Ellipse2D.Double(20, 0, 30, 30);
		Polygon circlePolygon = new Polygon();
		circlePolygon.addPoint(4, 25);		
		circlePolygon.addPoint(25, 45);
		circlePolygon.addPoint(46,25);
		circlePolygon.addPoint(25, 15);
		Area area1 = new Area(circle1); // circle 1
		Area area2 = new Area(circle2); // circle 1
		Area area3 = new Area(circlePolygon); // diamond
		heart = new Area(area2);
		heart.add(area1);
		heart.add(area3);
		AffineTransform at = new AffineTransform();
		at.translate(4,0);
		at.scale(0.85,0.85);
		heart.transform(at);
		
		Rectangle2D rectangle = new Rectangle2D.Double(0, 0, 41, 41);
		square = new Area(rectangle);
		
		Ellipse2D circleForm = new Ellipse2D.Double(0, 0, 41, 41);
		circle = new Area(circleForm);
		
		Polygon starPolygon = new Polygon();
		starPolygon.addPoint(21,0);
		starPolygon.addPoint(33,37);
		starPolygon.addPoint(1,14);
		starPolygon.addPoint(40,14);
		starPolygon.addPoint(8,37);
		star = new Area(starPolygon);
			
		Ellipse2D zeppelinShape = new Ellipse2D.Double(0, 0, 40*zeppScale, 60*zeppScale);
		ownZepp = new Area(zeppelinShape);
		oppZepp = new Area(zeppelinShape);
		ownZeppX =(int) (20*zeppScale); ownZeppY =(int) (30*zeppScale);
		oppZeppX =(int) (20*zeppScale); oppZeppY =(int) (30*zeppScale);
		
		Polygon one = new Polygon();
		one.addPoint((int) (6*zeppScale), (int) (21*zeppScale)); one.addPoint((int) (16*zeppScale), (int) (8*zeppScale)); 
		one.addPoint((int) (27*zeppScale), (int) (8*zeppScale)); one.addPoint((int) (27*zeppScale), (int) (53*zeppScale));
		one.addPoint((int) (16*zeppScale), (int) (53*zeppScale)); one.addPoint((int) (16*zeppScale), (int) (21*zeppScale));
		firstZepp = new Area(one);
	}
	
	private Area ownZepp, oppZepp, firstZepp;
	private int ownZeppX, ownZeppY, oppZeppX, oppZeppY;
	private int timeToRedraw, drawThreshhold;
	
	public void moveOwnZeppelin(double ownX, double ownY){
		//if(timeToRedraw < drawThreshhold){
			double diffX = ownX - ownZeppX;
			double diffY = ownY - ownZeppY;
			ownZeppX = (int) ownX;
			ownZeppY = (int) ownY;
			
			AffineTransform at = new AffineTransform();
			at.translate(diffX, diffY);
			ownZepp.transform(at);
			firstZepp.transform(at);
		//	timeToRedraw++;
		//} else{
		//	firstZepp.
	}
	
	private double ownRotation = 0;
	public void rotateOwnZeppelin(double rotation){
		double diffRot = (rotation - ownRotation);
		ownRotation = rotation;
		
		AffineTransform at = new AffineTransform();
		at.rotate(diffRot, ownZeppX, ownZeppY);
		ownZepp.transform(at);
		firstZepp.transform(at);
	}
	
	public void moveOppZeppelin(int oppX, int oppY){
		double diffX = oppX - oppZeppX;
		double diffY = oppY - oppZeppY;
		oppZeppX = oppX; oppZeppY = oppY;
		
		AffineTransform at = new AffineTransform();
		at.translate(diffX, diffY);
		oppZepp.transform(at);
	}
	
	public static void main(String[] args) {
		String[] code = MapMaker.testcode(7, 7);
		code[41] = "XX";
		
		//MapMaker heart = new MapMaker(800, 800);
		MapMaker heart = new MapMaker(1200, 800, 12, 7, code);
		heart.addMouse();
		JFrame f = new JFrame("Heart");
		f.setBounds(4, 4, 816,  818);
		f.getContentPane().add( heart, "Center" );

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle("Zeppelin Group White");
		f.setVisible(true);
		f.requestFocus();
		f.setSize(heart.getWidth()+16, heart.getHeight()+18);
		f.setVisible(true);
	}
	
	private static String[] testcode(int rows, int colums){
		String[] code = new String[rows*colums];
		int random;
		for(int i = 0; i < rows*colums; i++){
			random = (int )(Math.random() * 5 + 1);
			if(random == 1)
				code[i] = "G";
			if(random == 2)
				code[i] = "R";
			if(random == 3)
				code[i] = "Y";
			if(random == 4)
				code[i] = "B";
			if(random == 5)
				code[i] = "W";
			
			random = (int )(Math.random() * 4 + 1);
			if(random == 1)
				code[i] += "R";
			if(random == 2)
				code[i] += "C";
			if(random == 3)
				code[i] += "S";
			if(random == 4)
				code[i] += "H";
		}
		return code;
	}
	
	//Gebruikt OpenCSV: http://opencsv.sourceforge.net/
	public void parseCSV() {
		CSVReader reader;
		java.util.List<String[]> myEntries = null;
		try {
			reader = new CSVReader(new FileReader("src/gui/resources/map.csv"), ',');
			myEntries = (java.util.List<String[]>) reader.readAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(myEntries != null){
			rows = myEntries.size();
			colums = myEntries.get(0).length;
			code = new String[rows*colums];
			int i = 0;
			for(String[] array: myEntries){
				for(String something: array){
					something = something.replaceAll("\\s","");
					code[i] = something;
					i++;
				}
			}
		}
	}
	
	public void addMouse(){
		this.addMouseListener(new ZeppelinMouse());
	}
	
	private class ZeppelinMouse implements MouseListener{
		@Override
		public void mousePressed(MouseEvent e){
			int x = (int) e.getX();
			int y = (int) e.getY();
			if(SwingUtilities.isLeftMouseButton(e) ){
				System.out.println("x: " + x + "\ny: " +  y);
				moveOwnZeppelin(e.getX(), e.getY());
				repaint();
			}
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}
	}
}
