package gui;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Rooster.Vector;
import au.com.bytecode.opencsv.CSVReader;

public class MapMaker extends JPanel {
	private String[] code;
	private int columns, rows, width, height;
	private static final double standardScale = 10;
	private static final double standardTotalScale = 20;
	private double scaleX, scaleY;
	private final double zeppScale = 0.7;	
	private final double targetScale = 2;
	private final double tabletScale = 1.5;
	
	public MapMaker(int width, int height, int colums, int rows, String[] code){
		this.columns = colums;
		this.rows = rows;
		this.height = height;
		this.width = width;
		this.code = code;
		createShapes();
		setUpMap();
		createAndroid();
	}
	
	public MapMaker(int width, int height){
		this.height = height;
		this.width = width;
		parseCSV();
		createShapes();
		setUpMap();
		createAndroid();
	}
	
	private void setTemps(){		
		foundFigures = new ArrayList<Integer>();
		widthPerPiece = (int) (width/(columns + 0.5));
		heightPerPiece = (int) (height/rows);
		scaleX = widthPerPiece/standardTotalScale;
		scaleY = heightPerPiece/standardTotalScale;
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
		Graphics2D g2 = (Graphics2D)g;
		Image resizedImg = tabletPhoto.getScaledInstance(100, 100, Image.SCALE_DEFAULT);
		for(Vector pos: tablets){
			g2.drawImage(resizedImg, (int) pos.getX(), (int) pos.getY(), (int) (30*tabletScale), (int) (30*tabletScale), null);
		}
		
		for(PaintComp toPaint: toPaintAreas){
			int position = toPaint.getPosition();
			Area toPaintArea = (Area) toPaint.getArea().clone();
			//Als er figuren herkent zijn (en anderen kleiner moeten voorgesteld worden)
			//Als de figuur NIET herkend is, geen positie -1 heeft (voor de zeppelins) en foundfigures niet leeg is (er zijn wel figures herkend)
			if(!foundFigures.contains(position) && position != -1){
				double scale = 0.5;
				double x = toPaintArea.getBounds().getX();
				double y = toPaintArea.getBounds().getY();
				
				AffineTransform at = new AffineTransform();
				at.scale(scale, scale); //Kleiner maker
				at.translate((x + heightPerPiece/4) * (1/scale - 1), (y +heightPerPiece/4) * (1/scale - 1)); //Terug naar originele positie brengen.
				toPaintArea.transform(at);
			}
			
			g2.setColor(toPaint.getColor());
			g2.draw(toPaintArea);
			g2.fill(toPaintArea);
			
		}
		
		
		if(displayTarget){
			g2.setColor(Color.RED);
			g2.draw(target);
			g2.fill(target);
		}
	}
	
	private ArrayList<PaintComp> toPaintAreas = new ArrayList<PaintComp>();
	private ArrayList<Vector> tablets = new ArrayList<Vector>();
	private boolean startsWithIndent = false;
	private int widthPerPiece, heightPerPiece;
	
	private void setUpMap(){
		Color color;
		Area toPaint = null;
		boolean indent = startsWithIndent;
		
		widthPerPiece = (int) (width/(columns + 0.5));
		heightPerPiece = (int) (height/rows);
		scaleX = widthPerPiece/standardTotalScale;
		scaleY = heightPerPiece/standardTotalScale;
		
		int x = 0;	int y = 0; int position = 0; //positie in het rooster, gewoon de hoeveelste.
		if(indent)
			x = (int) widthPerPiece/2;
		
		x += scaleX*(standardTotalScale - standardScale)/2;
		y += scaleY*(standardTotalScale - standardScale)/2;
		
		if(code != null && code.length > 0){
			for(int i = 0; i < code.length; i++){
				String info = code[i];
				color = Color.pink; //debug color. Pink stuff are evil.
				if( y > height){
					System.err.println("Te veel symbolen om weer te geven in de figuur.\n@MapMaker - Paint: rows= " + rows + " ,colums= " + columns + "code.length= " + code.length);
					return;
				}
				if(info !=null && info.length() == 2 && !info.equalsIgnoreCase("XX")){
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
						toPaint = drawError(x,y);
					
					toPaintAreas.add(new PaintComp(toPaint, color, position));
				}
				//Volgende coordinaten berekenen
				//We gebruiken tempwidth om juist de plaatsen te berekenen. Omdat we alle figuren plaatsen in hun linksbovenhoek en met een standaardwaarde werken
				//moeten we colums - 1 gebruiken.
				//Omdat we met een inspringing werken, gebruiken we 2*colums, zodat de andere kan inspringen in de helft.
				x = x + widthPerPiece;
				position++;
				//Wanneer de linksbovenhoek groter wordt dan de breedtte - de marge - de grootte van het beeldje, moeten we de volgende lijn beginnen.
				if(x > width - widthPerPiece/2){
					//Als we de vorige keer geen indent hebben gemaakt, moet dit nu wel.
					indent = !indent;
					if(indent)
						x = (int) widthPerPiece/2;
					else
						x = 0;
					x += (standardTotalScale - standardScale)/2*scaleX;
					y = y + heightPerPiece;
				}
			}
		}
		toPaintAreas.add(new PaintComp(oppZepp, Color.magenta, -1));
		toPaintAreas.add(new PaintComp(ownZepp, Color.cyan, -1));
		toPaintAreas.add(new PaintComp(firstZepp, Color.black, -1));
	}
	
	private Area ownZepp, oppZepp, firstZepp;
	private double ownZeppX, ownZeppY, oppZeppX, oppZeppY, targetX, targetY;
	
	public void moveOwnZeppelin(double realX, double realY){
		double ownX = changeRealToPixel(realX, true);
		double ownY = changeRealToPixel(realY, false);
		
		double diffX = ownX - ownZeppX;
		double diffY = ownY - ownZeppY;
		ownZeppX = ownX;
		ownZeppY = ownY;

		AffineTransform at = new AffineTransform();
		at.translate(diffX, diffY);
		ownZepp.transform(at);
		firstZepp.transform(at);
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
	
	public void moveOppZeppelin(int realX, int realY){
		double oppX = changeRealToPixel(realX, true);
		double oppY = changeRealToPixel(realY, false);
		
		double diffX = oppX - oppZeppX;
		double diffY = oppY - oppZeppY;
		oppZeppX = oppX; oppZeppY = oppY;
		
		AffineTransform at = new AffineTransform();
		at.translate(diffX, diffY);
		oppZepp.transform(at);
	}
	
	private boolean displayTarget;
	
	public void setTarget(int realX, int realY){
		double newX = changeRealToPixel(realX, true);
		double newY = changeRealToPixel(realY, false);
		
		double diffX = newX - targetX;
		double diffY = newY - targetY;
		targetX = newX;
		targetY = newY;

		AffineTransform at = new AffineTransform();
		at.translate(diffX, diffY);
		target.transform(at);
		
		displayTarget = true;
	}
	
	public void removeTarget(){
		displayTarget = false;
	}
	
	private ArrayList<Integer> foundFigures;
	
	public void setFoundFigures(ArrayList<Integer> array){
		foundFigures = array;
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
	
	private Area drawError(int x, int y){
		AffineTransform at = new AffineTransform();
		at.scale(scaleX, scaleY);
		at.translate(x/scaleX, y/scaleY);
		at.rotate(Math.PI/4);

		Area newError = (Area) square.clone();
		newError.transform(at);
		
		return newError;
	}
	
	private Area square, circle, star, heart, target, tablet;
	
	private void createShapes(){
		//Scource http://www.cs.bham.ac.uk/~szh/teaching/graphics/sourcecode/DrawHeart.java
		
		//De shapes worden gemaakt als in een 10 op 10 vierkant. Dit is de standaardschaling.
		Ellipse2D circle1 = new Ellipse2D.Double(0, 0, 7, 7);
		Ellipse2D circle2 = new Ellipse2D.Double(5, 0, 7, 7);
		Polygon circlePolygon = new Polygon();
		circlePolygon.addPoint(1, 6);		
		circlePolygon.addPoint(6, 11);
		circlePolygon.addPoint(11,6);
		circlePolygon.addPoint(6, 4);
		Area area1 = new Area(circle1); // circle 1
		Area area2 = new Area(circle2); // circle 1
		Area area3 = new Area(circlePolygon); // diamond
		heart = new Area(area2);
		heart.add(area1);
		heart.add(area3);
		AffineTransform at = new AffineTransform();
		at.translate(1,0);
		heart.transform(at);
		
		Rectangle2D rectangle = new Rectangle2D.Double(0, 0, 10, 10);
		square = new Area(rectangle);
		
		Ellipse2D circleForm = new Ellipse2D.Double(0, 0, 10, 10);
		circle = new Area(circleForm);
		
		Polygon starPolygon = new Polygon();
		starPolygon.addPoint(5,0);
		starPolygon.addPoint(8,9);
		starPolygon.addPoint(0,3);
		starPolygon.addPoint(10,3);
		starPolygon.addPoint(2,9);
		star = new Area(starPolygon);
			
		Ellipse2D zeppelinShape = new Ellipse2D.Double(0, 0, 40*zeppScale, 60*zeppScale);
		ownZepp = new Area(zeppelinShape);
		oppZepp = new Area(zeppelinShape);
		ownZeppX = 20*zeppScale; ownZeppY = 30*zeppScale;
		oppZeppX = 20*zeppScale; oppZeppY = 30*zeppScale;
		
		Polygon one = new Polygon();
		one.addPoint((int) (6*zeppScale), (int) (21*zeppScale)); one.addPoint((int) (16*zeppScale), (int) (8*zeppScale)); 
		one.addPoint((int) (27*zeppScale), (int) (8*zeppScale)); one.addPoint((int) (27*zeppScale), (int) (53*zeppScale));
		one.addPoint((int) (16*zeppScale), (int) (53*zeppScale)); one.addPoint((int) (16*zeppScale), (int) (21*zeppScale));
		firstZepp = new Area(one);
		
		Ellipse2D outerCircle = new Ellipse2D.Double(5*targetScale,5*targetScale,30*targetScale,30*targetScale);
		Ellipse2D innerCircle = new Ellipse2D.Double(7.5*targetScale, 7.5*targetScale, 25*targetScale, 25*targetScale);
		RoundRectangle2D line = new RoundRectangle2D.Double(1*targetScale, 19*targetScale, 12*targetScale, 2*targetScale, 1*targetScale, 1*targetScale);
		Area generalLine = new Area(line);
		AffineTransform targetAT = new AffineTransform();
		targetAT.rotate(Math.PI/2, 20*targetScale, 20*targetScale);
		
		target = new Area(outerCircle);
		target.subtract(new Area(innerCircle));
		//Left
		target.add((Area) generalLine.clone());
		//Top
		generalLine.transform(targetAT);
		target.add((Area) generalLine.clone());
		//Right
		generalLine.transform(targetAT);
		target.add((Area) generalLine.clone());
		//Bottom
		generalLine.transform(targetAT);
		target.add((Area) generalLine.clone());
		
		
		targetX = 20*targetScale; targetY = 20*targetScale;
	}
	
	private boolean simulator = false;
	
	public void setSimulator(){
		simulator = true;
	}
	
	private Image tabletPhoto;
	
	public void createAndroid(){
		 tabletPhoto = null;
		try{
			tabletPhoto = ImageIO.read(new File("src/gui/resources/android1.png"));
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("Cannot find image or invalid resource type.");
		}
	}
	public static void main(String[] args) {
		String[] code = MapMaker.testcode(7, 7);
		code[41] = "XX";
		
		MapMaker heart = new MapMaker(1000, 800);
		//MapMaker heart = new MapMaker(1200, 800, 12, 7, code);
		heart.addMouse();
		JFrame f = new JFrame("Heart");
		f.setBounds(4, 4, 816,  818);
		f.getContentPane().add( heart, "Center" );
		
		heart.setTarget(1000, 1000);

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
	
	private int numberoftablets = 3;
	
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
			rows = myEntries.size() - numberoftablets;
			columns = myEntries.get(0).length;
			code = new String[rows*columns];
			int i = 0;
			for(int r = 0; r < rows; r++){
				String[] array = myEntries.get(r);
				for(String something: array){
					something = something.replaceAll("\\s","");
					code[i] = something;
					i++;
				}
			}
			setTemps();
			for(int t = 0; t < numberoftablets; t++){
				String[] array = myEntries.get(rows + t);
				int tabX = Integer.parseInt(array[0]);
				tabX = changeRealToPixel(tabX, true);
				int tabY = Integer.parseInt(array[1]);
				tabY = changeRealToPixel(tabY, false);
				tablets.add(new Vector(tabX, tabY));
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
			x = changePixelToReal(x, true);
			y = changePixelToReal(y, false);
			if(SwingUtilities.isLeftMouseButton(e) ){
				moveOwnZeppelin(x, y);
				repaint();
			} else if(SwingUtilities.isRightMouseButton(e) ){
				moveOppZeppelin(x, y);
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
	
	public int changePixelToReal(double pos, boolean horizontal){
		int direction;
		if(horizontal)
			direction = widthPerPiece;
		else
			direction = heightPerPiece;
		pos = pos - direction/2;
		pos = pos * 400/direction;
		if(!horizontal)
			pos = (int) (pos/2*Math.sqrt(3));
		return (int) pos;
	}
	
	public int changeRealToPixel(double pos, boolean horizontal){
		int direction;
		if(horizontal)
			direction = widthPerPiece;
		else
			direction = heightPerPiece;
		
		pos = pos/400*direction;
		if(!horizontal)
			pos = pos/Math.sqrt(3)*2;
		pos = pos + direction/2;
		
		return (int) pos;
	}
}
