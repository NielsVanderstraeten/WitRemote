//import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawCircle;
//import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvFont;
import static com.googlecode.javacv.cpp.opencv_core.cvGet2D;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint2D32f;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_UNCHANGED;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CLOCKWISE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvConvexHull2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetCentralMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetSpatialMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMoments;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvPointPolygonTest;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;



import gui.KirovAirship;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;

import Rooster.Grid;
import Rooster.Shape;
import Rooster.Vector;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_core.IplImageArray;
import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;

import commands.Command;
import commands.SetPosition;

/**
 * VORMEN: 1) Harten 2) Cirkels 3) Rechthoeken 4) Sterren
 * KLEUREN: 1) Blauw 2) Wit 3) Rood 4) Geel 5) Groen
 */

public class NewShapeRecognition implements Runnable {
	
	/**
	 * Lijst met alle vormen. 
	 */
	private ArrayList<String> shapes = new ArrayList<String>();
	
	/**
	 * Lijst met de kleuren. Indexen komen overeen met shapes.
	 */
	private ArrayList<String> colors = new ArrayList<String>();

	/**
	 * Aantal punten die de benaderende polygonaal heeft. Indexen komen overeen met shapes.
	 */
	private ArrayList<Integer> points = new ArrayList<Integer>();

	/**
	 * Vectors die de centrums/zwaartepunten van de figuren bijhoudt.Indexen komen overeen met shapes.
	 */
	private ArrayList<Vector> centers = new ArrayList<Vector>();

	/**
	 * Gevonden RGB codes. Indexen komen overeen met shapes.
	 */
	private ArrayList<String> foundColorCodesRGB = new ArrayList<String>();
	
	/**
	 * list van shapes
	 */
	private ArrayList<Shape> shapeList = new ArrayList<Shape>();
	
	/**
	 * iplimages
	 */
	private IplImage imgOrg, imgHSV, imgSmooth, imgThresholdWhite, 
	imgThresholdWhiteCanny, imgThresholdHSVDarkColors;
	
	@SuppressWarnings("unused")
	private int stars,rectangles,hearts,circles,unidentifiedShapes,unidentifiedColors;
	
	
	private String originalImagePath;

	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	
	private KirovAirship gui;
	private Grid grid;
	private LinkedList<Command> queue;
	
	private CvScalar minWhite = cvScalar(170, 170, 170, 0);
    private CvScalar maxWhite = cvScalar(255, 255, 255, 0);	 
    private CvScalar minHSV = cvScalar(0, 50, 50, 0); //TODO mss aanpassen
    private CvScalar maxHSV = cvScalar(255, 255, 255, 0);
    private CvScalar colorScalar;
	
	public static void main(String args[]){
		
		Grid grid = new Grid("");
		NewShapeRecognition shapeRecog = new NewShapeRecognition(
				"C:/Users/Jeroen/Desktop/Pics/TestB11.jpg", null, grid, null);
		//NewShapeRecognition shapeRecog = new NewShapeRecognition("pic1.jpg");
		Thread t = new Thread(shapeRecog);
		t.start();
	}	

	public NewShapeRecognition(String path, KirovAirship gui, Grid grid, LinkedList<Command> queue){
		this.gui = gui;
		this.grid = grid;
		this.queue = queue;

		originalImagePath = path;
	}
	
	public synchronized void run(){ //TODO: synchronised?
		Long start = System.currentTimeMillis();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		emptyAllParameters();

		createImagesAndFindContours();
		System.out.println();
		System.out.println("Time: " + (System.currentTimeMillis() - start));
		gui.updatePhoto();
		
		/*System.out.println("Unidentified shapes: " + " " + unidentifiedShapes + " --- Unidentified colors: " + unidentifiedColors);
		System.out.println("Number of shapes found: " + shapes.size());
		for(int i = 0; i <= shapes.size() - 1; i++){
			System.out.println((i+1) + ") "  + colors.get(i) + " " + shapes.get(i) + " - Center: ("+ centers.get(i) +") "
					+ foundColorCodesRGB.get(i));
		}*/
		
		shapeList = makeShapeList();
		
		Vector position = grid.getPositionNew(shapeList);
		double rotation = grid.getRotationNew(shapeList);
	    
		if (position.getX() != -1 && position.getY() != -1) {
			queue.add(new SetPosition((int) position.getX(), (int) position.getY(), rotation));
			gui.updateRecognisedShapes(shapeList);
			gui.updateOwnPosition((int) position.getX(), (int) position.getY(), rotation);
		}
	}
	
	private void createImagesAndFindContours() {
		imgOrg = cvLoadImage(originalImagePath, CV_LOAD_IMAGE_UNCHANGED);
		
		imgHSV = IplImage.create(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
//	    imgHSV = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
//		imgHSV = null;
	    cvCvtColor(imgOrg, imgHSV, CV_BGR2HSV);
	    //cvSaveImage("C:/Users/Jeroen/Desktop/Original.jpg", imgOrg);
	    //cvSaveImage("C:/Users/Jeroen/Desktop/HSV.jpg", imgHSV);
	    
	    imgSmooth = IplImage.create(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
//	    imgSmooth = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
	    cvSmooth(imgOrg, imgSmooth, CV_GAUSSIAN, 5);
	    //cvSaveImage("C:/Users/Jeroen/Desktop/WhiteGaussian.jpg", imgSmooth);

	    // WIT   
	    imgThresholdWhite = IplImage.create(cvGetSize(imgOrg), 8, 1);
//	    imgThresholdWhite = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    cvInRangeS(imgSmooth, minWhite, maxWhite, imgThresholdWhite);
	    imgThresholdWhiteCanny = IplImage.create(cvGetSize(imgOrg), 8, 1);
//	    imgThresholdWhiteCanny = cvCreateImage(cvGetSize(imgOrg), 8, 1);
        cvCanny(imgThresholdWhite, imgThresholdWhiteCanny, 0, 255, 3);
        //TODO cvSaveImage van hieronder commenten
       // cvSaveImage("C:/Users/Jeroen/Desktop/ThresholdWhite.jpg", imgThresholdWhite);
        //TODO wit dmv hsv

	    // HSV => DONKEREKLEUREN
        imgThresholdHSVDarkColors = IplImage.create(cvGetSize(imgOrg), 8, 1);
//	    imgThresholdHSVDarkColors = cvCreateImage(cvGetSize(imgHSV), 8, 1);
	    cvInRangeS(imgHSV, minHSV, maxHSV, imgThresholdHSVDarkColors);
//	    IplImage cannyEdge2 = cvCreateImage(cvGetSize(imgHSV), 8, 1);
//	    cvCanny(imgThresholdHSVDarkColors, imgThresholdHSVDarkColors, 0, 255, 3);
	    //TODO cvSaveImage van hieronder commenten
	    //cvSaveImage("C:/Users/Jeroen/Desktop/ThresholdHSVDarkColors.jpg", imgThresholdHSVDarkColors);
	    
	    findContoursAndHull(imgSmooth, imgThresholdWhiteCanny);
	    findContoursAndHull(imgSmooth, imgThresholdHSVDarkColors);
	    
	   // cvSaveImage("C:/Users/Jeroen/Desktop/Analysed.jpg", imgSmooth);
	    cvSaveImage("src/images/analyse.jpg", imgSmooth);
	    
	    imgHSV.release();
	    imgThresholdHSVDarkColors.release();
	    imgThresholdWhite.release();
	    imgOrg.release();
	    imgSmooth.release();
	    imgThresholdWhiteCanny.release();
	}

	private void findContoursAndHull(IplImage imgOrg, IplImage imgThreshold) {
		CvMemStorage memory = CvMemStorage.create();
		CvSeq contour = CvSeq.create(0, Loader.sizeof(CvSeq.class), 
		          Loader.sizeof(CvPoint.class), memory);
		cvFindContours(imgThreshold, memory, contour, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

		CvMemStorage storage = CvMemStorage.create();    
		while (contour != null && !contour.isNull()) {
			if (contour.elem_size() > 0) {
				
				cvApproxPoly(contour, Loader.sizeof(CvContour.class),
						storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 1);
				int centerX = 0;
				int centerY = 0;        
				CvMoments moments = new CvMoments();
				cvMoments(contour, moments, 1);
				double momX10 = cvGetSpatialMoment(moments, 1, 0);
				double momY01 = cvGetSpatialMoment(moments, 0, 1);
				double area = cvGetCentralMoment(moments, 0, 0);
				centerX = (int) (momX10 / area);
				centerY = (int) (momY01 / area);
				if(area > 500){
					ArrayList<CvPoint2D32f> punten = new ArrayList<CvPoint2D32f>();
					boolean onEdge = false;

					int offset = 5;

					for (int i=0; i <= imgOrg.height(); i++) {
						punten.add(cvPoint2D32f(offset, i));
					}
					for (int i=0; i <= imgOrg.height(); i++) {
						punten.add(cvPoint2D32f(imgOrg.width()-offset, i));
					}
					for (int i=0; i <= imgOrg.width(); i++) {
						punten.add(cvPoint2D32f(i, offset));
					}
					for (int i=0; i <= imgOrg.width(); i++) {
						punten.add(cvPoint2D32f(i, imgOrg.height()-offset));
					}

					for (CvPoint2D32f p : punten)
						if (cvPointPolygonTest(contour, p, 0) != -1)
							onEdge = true;

					if (! onEdge) {
						CvSeq convexContour = cvConvexHull2(contour, storage, CV_CLOCKWISE, 1);
						cvMoments(convexContour, moments, 1);
						double areaHull = cvGetCentralMoment(moments, 0, 0);
						
						//draw points
						for(int i = 0; i < contour.total(); i++){
							CvPoint v = new CvPoint(cvGetSeqElem(contour, i));
							cvDrawCircle(imgOrg, v, 1, CvScalar.WHITE, -1, 8, 0);
						}
						
						Vector vectorCenter = new Vector(centerX, centerY);

						ArrayList<Double> list = new ArrayList<Double>();
						for(int i = 0; i < contour.total(); i++){
							CvPoint v =new CvPoint(cvGetSeqElem(contour, i));
							Vector punt = new Vector(v.x(), v.y());
							list.add(punt.getDistance(vectorCenter));
						}            
						Collections.sort(list);
						double radius = list.get(list.size()-1);
						double areaCircle = Math.PI*radius*radius;
						String imageTxt = "";
						System.out.println("AreaHull/area == " + areaHull/area);
						System.out.println("AreaCircle/area == " + areaCircle/area);
						if(areaHull/area > 1.9){ //TODO 1.9 testen en aanpassen
							//TODO
							//TODO
							//TODO
							//System.out.println("Unidentified AREAHULL/AREA == " + areaHull/area);
							shapes.add("Unidentified");
							unidentifiedShapes++;
							imageTxt = "U";
						}
						else if(areaHull/area > 1.2){
							//System.out.println("Star AREAHULL/AREA == " + areaHull/area);
							shapes.add("Star");
							stars++;;
							imageTxt = "S";
						}
						else if(areaCircle/area > 1.6){
							//System.out.println("Rectangle AreaCircle/area == " + areaCircle/area);
							//System.out.println("Rectangle AreaHull/area == " + areaHull/area);
							shapes.add("Rectangle");
							rectangles++;
							imageTxt = "R";
						}
						else if(areaCircle/area > 1.3){
							//System.out.println("Heart AreaCircle/area == " + areaCircle/area);
							//System.out.println("Heart AreaHull/area == " + areaHull/area);
							shapes.add("Heart");
							hearts++;
							imageTxt = "H";
						}
						else if(areaCircle/area >= 1){
							shapes.add("Circle");
							circles++;
							imageTxt = "C";
						}
						else {
							shapes.add("Unidentified");
							unidentifiedShapes++;
							imageTxt = "U";
						}

						colorScalar = cvGet2D(imgOrg, centerY, centerX);   
						Color figureColor = findColor((int)colorScalar .val(2), (int)colorScalar .val(1), (int)colorScalar .val(0));           
						String figureColorString = colorToString(figureColor);
						colors.add(figureColorString);
						imageTxt = figureColorString.substring(0,1) + imageTxt;
						foundColorCodesRGB.add("RGB: [" + colorScalar.val(2) + ", " + colorScalar.val(1) + ", "+ colorScalar.val(0) + "]");

						centers.add(new Vector(centerX, centerY));
						cvPutText(imgOrg, imageTxt, cvPoint((int)centerX, (int)centerY), 
								cvFont(2, 3), CvScalar.BLACK);

						String printString = "";
						printString += "FOUND COLOR & SHAPE: " + colors.get(colors.size()-1) + " " + shapes.get(shapes.size()-1);
						printString += " --- CENTER:("+centerX+", " + centerY+")";
						printString += " --- AREA = " + area; 
						printString += " --- " + foundColorCodesRGB.get(foundColorCodesRGB.size()-1);
						System.out.println(printString);
						System.out.println();
						//System.out.println("AREAHULL = " + areaHull + " --- AreaHull/Area = " + areaHull/area);
						//System.out.println("AREACIRCLE = "+ areaCircle + " (r= " + radius + ") --- AreaCircle/Area = " + areaCircle/area);
					}
				}
            }
            contour = contour.h_next();
        }
		memory.release();
	}
	
	private String colorToString(Color figureColor) {
		if(figureColor == Color.blue){
			return "Blue";
		}
		if(figureColor == Color.white){
			return "White";
		}
		else if(figureColor == Color.red){
			return "Red";
		}
		else if(figureColor == Color.yellow){
			return "Yellow";
		}
		else if(figureColor == Color.green){
			return "Green";
		}
		else if(figureColor == Color.cyan){
			return "Cyan";
		}
		else if(figureColor == Color.black){
			return "Black";
		}
		else{
			unidentifiedColors++;
			return "Unidentified";
		}
	}
	
	/**
	 * Finds the color blue, white, red, yellow or green. If it is an other color this returns cyan.
	 * 
	 * KLEUR: SCALAR(BLAUW,GROEN,ROOD)
	 * Blauw: Scalar(255,0,0)
	 * Wit: Scalar(255,255,255)
	 * Rood: Scalar(0,0,255)
	 * Geel: Scalar(0,255,255)		 
	 * Groen: Scalar(0,255,0)
	 * Zwart: Scalar(0,0,0)
	 */
	private Color findColor(int averageRed, int averageGreen, int averageBlue) {
		int min = 150;
		int zwartGrijs = 50;
		if(averageRed >= min && averageGreen >= min && averageBlue >= min){
			return Color.white;
		}
		else if(averageRed <= zwartGrijs && averageGreen <= zwartGrijs && averageBlue <= zwartGrijs){
			return Color.black;
		}
		else if(averageRed >= min && averageGreen >= min){
			return Color.yellow;
		}
		else if(averageBlue >= averageRed && averageBlue >= averageGreen + 10){
			return Color.blue;
		}
		else if(averageRed >= averageGreen && averageRed >= averageBlue){
			return Color.red;
		}
		else if(averageGreen >= averageRed && averageGreen >= averageBlue - 10){
			return Color.green;
		}
		else{
			return Color.cyan;
		}
	}
	
	private void emptyAllParameters() {
		shapes.clear();
		colors.clear();
		points.clear();
		centers.clear();
		foundColorCodesRGB.clear();
		rectangles = 0;
		stars = 0;
		hearts = 0;
		circles = 0;
		unidentifiedShapes = 0;
		unidentifiedColors = 0;
		contours.clear();
	}

	private ArrayList<Shape> makeShapeList() {
		shapeList.clear();
		for(int i = 0; i < shapes.size(); i++){
			if(!shapes.get(i).equals("Unidentified")){
				shapeList.add(new Shape(centers.get(i), colors.get(i), shapes.get(i)));
			}
		}
		return shapeList;
	}
	
	public List<MatOfPoint> getContours(){
		return contours;
	}
	
	public ArrayList<String> getShapes(){
		return shapes;
	}
	
	public ArrayList<String> getColors(){
		return colors;
	}

	public void setFile(String path) {
		originalImagePath = path;		
	}
}