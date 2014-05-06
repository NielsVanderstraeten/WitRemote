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
import static com.googlecode.javacv.cpp.opencv_core.cvSplit;
import static com.googlecode.javacv.cpp.opencv_core.cvMerge;
import static com.googlecode.javacv.cpp.opencv_core.cvGetMat;
import static com.googlecode.javacv.cpp.opencv_core.cvInvert;
import static com.googlecode.javacv.cpp.opencv_core.cvInv;
import static com.googlecode.javacv.cpp.opencv_core.cvNot;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_UNCHANGED;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2Lab;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2YCrCb;

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
import static com.googlecode.javacv.cpp.opencv_imgproc.cvEqualizeHist;



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
import com.googlecode.javacv.cpp.opencv_ml.CvVectors;

import commands.Command;
import commands.SetPosition;

/**
 * VORMEN: 1) Harten 2) Cirkels 3) Rechthoeken 4) Sterren
 * KLEUREN: 1) Blauw 2) Wit 3) Rood 4) Geel 5) Groen
 */

public class NewShapeRecognition implements Runnable {
	
	private final boolean debug = true;

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
	imgThresholdWhiteCanny, imgThresholdHSVDarkColors, imgThresholdHSVDarkColorsLowLight;

	@SuppressWarnings("unused")
	private int stars,rectangles,hearts,circles,unidentifiedShapes,unidentifiedColors;


	private String originalImagePath;

	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

	private KirovAirship gui;
	private Grid grid;
	private List<Command> queue;

	private CvScalar minWhite = cvScalar(170, 170, 170, 0);
	private CvScalar maxWhite = cvScalar(255, 255, 255, 0);	 
//	private CvScalar minAll = cvScalar(0, 0, 0, 0);
//	private CvScalar maxAll = cvScalar(200, 200, 200, 0);
	private CvScalar minHSV = cvScalar(0, 50, 50, 0);
	private CvScalar maxHSV = cvScalar(255, 255, 255, 0);
	private CvScalar minHSVLowLight = cvScalar(0, 30, 50, 0);
	private CvScalar maxHSVLowLight = cvScalar(30, 200, 150, 0);
	private CvScalar colorScalar;

		public static void main(String args[]){
	
			Grid grid = new Grid("");
			NewShapeRecognition shapeRecog = new NewShapeRecognition(
					//"C:/Users/Jeroen/Desktop/Pics/A25.jpg", null);
					"E:/Recv9.jpg", null);
			//NewShapeRecognition shapeRecog = new NewShapeRecognition("pic1.jpg");
			Thread t = new Thread(shapeRecog);
			t.start();
		}	
	
	private ControlManager cm;

	public NewShapeRecognition(String path, ControlManager cm){
		this.cm = cm;
		this.gui = cm.getGUI();
		this.grid = cm.getGrid();
		this.queue = cm.getQueue();

		originalImagePath = path;
	}
	
	private long start = System.currentTimeMillis();

	public synchronized void run(){
		start = System.currentTimeMillis();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		emptyAllParameters();

		createImagesAndFindContours();
//		System.out.println("   Time1: " + (System.currentTimeMillis() - start));
		gui.updatePhoto(); //TODO


		/*System.out.println("Unidentified shapes: " + " " + unidentifiedShapes + " --- Unidentified colors: " + unidentifiedColors);
		System.out.println("Number of shapes found: " + shapes.size());
		for(int i = 0; i <= shapes.size() - 1; i++){
			System.out.println((i+1) + ") "  + colors.get(i) + " " + shapes.get(i) + " - Center: ("+ centers.get(i) +") "
					+ foundColorCodesRGB.get(i));
		}*/

		shapeList = makeShapeList();

		Vector position = grid.getPositionNew(shapeList);
		double rotation = grid.getRotationNew(shapeList);
		if (debug) {
			System.out.println("    Position: " + position.getX() + ", " + position.getY());
			System.out.println("    Rotation: " + rotation);
		}
		if (position.getX() != -1 && position.getY() != -1) {
			cm.setFoundPosition();
			queue.add(new SetPosition((int) position.getX(), (int) position.getY(), rotation));
			gui.updateRecognisedShapes(shapeList);
			gui.updateOwnPosition((int) position.getX(), (int) position.getY(), rotation);
		}
		

		for (MatOfPoint c : contours) {
			c.release();
		}
		
	}

	private void createImagesAndFindContours() {
		imgOrg = cvLoadImage(originalImagePath, CV_LOAD_IMAGE_UNCHANGED);

		imgHSV = IplImage.create(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
		//	    imgHSV = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
		//		imgHSV = null;
		cvCvtColor(imgOrg, imgHSV, CV_BGR2HSV);
		//cvSaveImage("C:/Users/Jeroen/Desktop/Original.jpg", imgfOrg);
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
				cvSaveImage("C:/Users/Jeroen/Desktop/ThresholdWhite.jpg", imgThresholdWhite);

		// HSV => DONKEREKLEUREN
		imgThresholdHSVDarkColors = IplImage.create(cvGetSize(imgOrg), 8, 1);
		imgThresholdHSVDarkColorsLowLight = IplImage.create(cvGetSize(imgOrg), 8, 1);
		//	    imgThresholdHSVDarkColors = cvCreateImage(cvGetSize(imgHSV), 8, 1);
		cvInRangeS(imgHSV, minHSV, maxHSV, imgThresholdHSVDarkColors);
		cvInRangeS(imgHSV, minHSVLowLight, maxHSVLowLight, imgThresholdHSVDarkColorsLowLight);
		//	    IplImage cannyEdge2 = cvCreateImage(cvGetSize(imgHSV), 8, 1);
		//	    cvCanny(imgThresholdHSVDarkColors, imgThresholdHSVDarkColors, 0, 255, 3);
		cvSaveImage("C:/Users/Jeroen/Desktop/ThresholdHSVDarkColors.jpg", imgThresholdHSVDarkColors);

		cvNot(imgThresholdHSVDarkColorsLowLight, imgThresholdHSVDarkColorsLowLight);
		//cvSaveImage("C:/Users/Jeroen/Desktop/temp.jpg", imgThresholdHSVDarkColorsLowLight);


		findContoursAndHull(imgSmooth, imgThresholdWhiteCanny, false);
		findContoursAndHull(imgSmooth, imgThresholdHSVDarkColors, false);
		findContoursAndHull(imgSmooth, imgThresholdHSVDarkColorsLowLight, true);

		cvSaveImage("src/images/analyse.jpg", imgSmooth);

//		imgThresholdBlack.release();
		imgHSV.release();
		imgThresholdHSVDarkColors.release();
		imgThresholdHSVDarkColorsLowLight.release();
		imgThresholdWhite.release();
//		imgThresholdAll.release();
		imgOrg.release();
		imgSmooth.release();
		imgThresholdWhiteCanny.release();

	}

	private void findContoursAndHull(IplImage imgOrg, IplImage imgThreshold, boolean lowLight) {
		CvMemStorage memory = CvMemStorage.create();
//		System.out.println("     Memory: " + (System.currentTimeMillis() - start));
		CvSeq contour = CvSeq.create(0, Loader.sizeof(CvSeq.class), 
				Loader.sizeof(CvPoint.class), memory);
//		System.out.println("     Contour: " + (System.currentTimeMillis() - start));
		cvFindContours(imgThreshold, memory, contour, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
		
//		System.out.println("     Contours: " + (System.currentTimeMillis() - start));

		//Median area berekenen
 

		while (contour != null && !contour.isNull()) {
			if (contour.elem_size() > 0) {
				
				CvMoments moments = new CvMoments();
				cvMoments(contour, moments, 1);
				double area = cvGetCentralMoment(moments, 0, 0);
				
				if(area > 300 && area < 5000){
					CvMemStorage storage = CvMemStorage.create();
//					System.out.println("     Storage: " + (System.currentTimeMillis() - start));

					cvApproxPoly(contour, Loader.sizeof(CvContour.class),
							storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 1);
//					System.out.println("     Approxpoly: " + (System.currentTimeMillis() - start));
					
					//Middelpunt
					int centerX = 0;
					int centerY = 0; 
					double momX10 = cvGetSpatialMoment(moments, 1, 0);
					double momY01 = cvGetSpatialMoment(moments, 0, 1);
					centerX = (int) (momX10 / area);
					centerY = (int) (momY01 / area);
//					System.out.println("     Moments: " + (System.currentTimeMillis() - start));
					
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

						Vector vectorCenter = new Vector(centerX, centerY);
						
//						System.out.println("     Center: " + (System.currentTimeMillis() - start));

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
						if (debug) {
							System.out.println("    AreaHull/area == " + areaHull/area);
							System.out.println("    AreaCircle/area == " + areaCircle/area);
						}

						boolean isUnidentified = false;
						
						double hullEdge;
						if (lowLight) 
							hullEdge = 1.04;
						else
							hullEdge = 1.038;

						//(vb omhullende klopt, maar circle-verhouding is absurd)
						if(areaHull/area > 1.975){
							//System.out.println("Unidentified AREAHULL/AREA == " + areaHull/area);
							shapes.add("Unidentified");
							unidentifiedShapes++;
							imageTxt = "U";
							isUnidentified = true;
						}
						else if(areaHull/area > 1.2  && areaCircle/area < 3){
							//System.out.println("Star AREAHULL/AREA == " + areaHull/area);
							shapes.add("Star");
							stars++;;
							imageTxt = "S";
						}
						//						else if(areaCircle/area > 1.6  && areaCircle/area < 3){
						else if(((areaCircle/area > 1.64) 
								|| (areaHull/area < hullEdge && areaCircle/area > 1.4))
								&& (areaCircle/area < 3)){
							//System.out.println("Rectangle AreaCircle/area == " + areaCircle/area);
							//System.out.println("Rectangle AreaHull/area == " + areaHull/area);
							shapes.add("Rectangle");
							rectangles++;
							imageTxt = "R";
						}
						//						else if(areaCircle/area > 1.3  && areaCircle/area < 3){
						else if(areaHull/area > hullEdge  && areaCircle/area < 3){
							//System.out.println("Heart AreaCircle/area == " + areaCircle/area);
							//System.out.println("Heart AreaHull/area == " + areaHull/area);
							shapes.add("Heart");
							hearts++;
							imageTxt = "H";
						}
						else if(areaCircle/area >= 1  && areaCircle/area < 3){
							shapes.add("Circle");
							circles++;
							imageTxt = "C";
						}
						else {
							shapes.add("Unidentified");
							unidentifiedShapes++;
							imageTxt = "U";
							isUnidentified = true;
						}
						
//						System.out.println("     Identify: " + (System.currentTimeMillis() - start));

						colorScalar = cvGet2D(imgOrg, centerY, centerX);   
						Color figureColor = findColor((int)colorScalar .val(2), (int)colorScalar .val(1), (int)colorScalar .val(0), lowLight);           
						String figureColorString = colorToString(figureColor);
						colors.add(figureColorString);
						imageTxt = figureColorString.substring(0,1) + imageTxt;
						foundColorCodesRGB.add("RGB: [" + colorScalar.val(2) + ", " + colorScalar.val(1) + ", "+ colorScalar.val(0) + "]");

						centers.add(new Vector(centerX, centerY));

						if (! isUnidentified) {
							//draw points
							for(int i = 0; i < contour.total(); i++){
								CvPoint v = new CvPoint(cvGetSeqElem(contour, i));
								cvDrawCircle(imgOrg, v, 1, CvScalar.WHITE, -1, 8, 0);
							}
							//draw text
							cvPutText(imgOrg, imageTxt, cvPoint((int)centerX, (int)centerY), 
									cvFont(2, 3), CvScalar.BLACK);
						}

						String printString = "";
						printString += "    FOUND COLOR & SHAPE: " + colors.get(colors.size()-1) + " " + shapes.get(shapes.size()-1);
						printString += " --- CENTER:("+centerX+", " + centerY+")";
						printString += " --- AREA = " + area; 
						printString += " --- " + foundColorCodesRGB.get(foundColorCodesRGB.size()-1);
						if (debug)
							System.out.println(printString);
						//System.out.println("AREAHULL = " + areaHull + " --- AreaHull/Area = " + areaHull/area);
						//System.out.println("AREACIRCLE = "+ areaCircle + " (r= " + radius + ") --- AreaCircle/Area = " + areaCircle/area);
					}
					storage.release();
//					System.out.println("     Srelease: " + (System.currentTimeMillis() - start));
				}
			}
			contour = contour.h_next();
//			System.out.println("     Nextcont: " + (System.currentTimeMillis() - start));
		}
		memory.release();
//		System.out.println("     Mrelease: " + (System.currentTimeMillis() - start));
//		storage.release();
//		System.out.println("     Srelease: " + (System.currentTimeMillis() - start));
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
	private Color findColor(int averageRed, int averageGreen, int averageBlue, boolean lowLight) {
		if (lowLight)
			return findColorLowLight(averageRed, averageGreen, averageBlue);
		else
			return findColorGoodLight(averageRed, averageGreen, averageBlue);
	}
	
	private Color findColorGoodLight(int averageRed, int averageGreen, int averageBlue) {
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
	
	//TODO testen
	private Color findColorLowLight(int averageRed, int averageGreen, int averageBlue) {
		int min = 100;
		int zwartGrijs = 30;
		if(averageRed >= min && averageGreen >= min && averageBlue >= min){
			return Color.white;
		}
		else if(averageRed <= zwartGrijs && averageGreen <= zwartGrijs && averageBlue <= zwartGrijs){
			return Color.black;
		}
		else if(averageRed >= 100 && averageGreen >= 90){
			return Color.yellow;
		}
		else if(averageBlue >= averageRed && averageBlue >= averageGreen){
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