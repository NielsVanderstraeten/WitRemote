import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvFont;
import static com.googlecode.javacv.cpp.opencv_core.cvGet2D;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint2D32f;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
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
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
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
import org.opencv.core.MatOfPoint;

import Rooster.Grid;
import Rooster.Shape;
import Rooster.Vector;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
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
	 * Lijst met de gevonden vormen met kleur in code; bv Red Rectangle == RR.
	 */
	private ArrayList<String> colorShapeCodes = new ArrayList<String>();
	
	/**
	 * Aantallen...
	 */
	private int rectangles = 0;
	private int stars = 0;
	private int hearts = 0;
	private int circles = 0;	
	private int unidentifiedShapes = 0;
	private int unidentifiedColors = 0;
	
	/**
	 * IpkImages
	 */
	private IplImage imgOrg;
	
	
	private String originalImagePath;
	
	private long prev;
	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private String writeToPath;
	
	/**
	 * Tijd dat het duurt om de operaties op de afbeeldingen te doen...
	 */
	private long imageReadingTime = 0;
	private long javaCVProcessingTime = 0;
	private long imageWritingTime = 0;

	private int minimalAreaOfRectangleAroundShape;
	private int maximalAreaOfRectangleAroundShape;
	
	private KirovAirship gui;
	private Grid grid;
	private LinkedList<Command> queue;
	
	public static void main(String args[]){
		
		Grid grid = new Grid("");
		NewShapeRecognition shapeRecog = new NewShapeRecognition("C:/Users/Jeroen/Desktop/Pics/TestA2.jpg", null, grid, null);
		//NewShapeRecognition shapeRecog = new NewShapeRecognition("pic1.jpg");
		Thread t = new Thread(shapeRecog);
		t.start();
	}	

	public NewShapeRecognition(String path, KirovAirship gui, Grid grid, LinkedList<Command> queue){
		this.gui = gui;
		this.grid = grid;
		this.queue = queue;

		originalImagePath = path;
		writeToPath = "C:/Users/Jeroen/Desktop/ShapeRecognition/";
		//TODO: testen wat de minimale waarde hiervan moet zijn of robuuster maken adhv hoogte van zeppelin... 
		minimalAreaOfRectangleAroundShape = 3000;
		maximalAreaOfRectangleAroundShape = 14000;
	}
	
	public void run(){
		
		emptyAllParameters();
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		createImagesAndFindContours();

		gui.updatePhoto();
		
		System.out.println("Rectangles: " + rectangles);
		System.out.println("Stars: "  + stars);
		System.out.println("Hearts: " + hearts);
		System.out.println("Circles: " + circles);
		System.out.println("Unidentified shapes: " + " " + unidentifiedShapes);
		System.out.println("Unidentified colors: " + unidentifiedColors);
		System.out.println("Shapes found: ");
		System.out.println(colors.size() + " " + shapes.size() + " "+ points.size());
		for(int i = 0; i <= shapes.size() - 1; i++){
			//System.out.println((i+1) + ") "  + colors.get(i) + " " + shapes.get(i) + 
			//		" (" + points.get(i) + " points) Found color-codes: " + foundColorCodesRGB.get(i));
			System.out.println((i+1) + ") "  + colors.get(i) + " " + shapes.get(i) +
					" Found color-codes: " + foundColorCodesRGB.get(i));
		
		}
		System.out.println("Time needed to read images: " + imageReadingTime + "ms");
		System.out.println("Time needed to write images: " + imageWritingTime + "ms");
		System.out.println("Actual time needed processing images with JAVACV: " + javaCVProcessingTime +"ms");
		System.out.println("Total time for real program: " + (imageReadingTime + javaCVProcessingTime));
		
		ArrayList<Shape> shapeList = makeShapeList();
		
		System.out.println("Made shape list");
		Vector position = grid.getPosition(shapeList);
		System.out.println("Position: " + position.toString());
		double rotation = grid.getRotation(shapeList);
		
		System.out.println("Rotation: " + rotation);
		
		queue.add(new SetPosition((int) position.getX(), (int) position.getY(), rotation));

	}
	
	private void createImagesAndFindContours() {

		//***********************************************************************************
		//***********************************************************************************
		//***********************************************************************************
		prev = System.currentTimeMillis();
		imgOrg = cvLoadImage(originalImagePath, CV_LOAD_IMAGE_UNCHANGED);
		imageReadingTime =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME IplImage inlezen: " + (System.currentTimeMillis() - prev) + "ms");
	    
	    //***** HSV en GREYSCALE *****//
	    // http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-image-thresholding-hsv-color.html
	    // Nog niet geprobeerd: http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-image-thresholding-hsv-color.html
	    IplImage imgHSV = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
	    cvCvtColor(imgOrg, imgHSV, CV_BGR2HSV);
	    //cvInRangeS(imgHSV, cvScalar(15, 234, 120, 0), cvScalar(21, 234, 120, 0), imgHSV);
	    prev = System.currentTimeMillis();
	    cvSaveImage(writeToPath + "HSV.jpg", imgHSV);
	    imageWritingTime += System.currentTimeMillis() - prev;
	    
	    //***** Color based thresholding *****//
	    //http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-simple-color-detection-using.html
	    //TODO: kleuren al testend bepalen of overschakelen op HSV.
	    //TODO: HSV zou beter moeten zijn omdat de invloed van licht/belichting (veel) minder is dacht ik

	    cvSmooth(imgOrg, imgOrg, CV_GAUSSIAN, 5);
	    
	    
	    // BLAUW
	    prev = System.currentTimeMillis();
	    CvScalar minBlue = cvScalar(1, 1, 1, 0);//BGR-A
	    CvScalar maxBlue = cvScalar(255, 255, 90, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdBlue = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minBlue, maxBlue, imgThresholdBlue);
	    //smooth filter- median
	    //cvSmooth(imgThresholdBlue, imgThresholdBlue, CV_MEDIAN, 13);
	    //time recording
	    javaCVProcessingTime =+ (System.currentTimeMillis() - prev);
	    //save
	    prev = System.currentTimeMillis();
	    cvSaveImage(writeToPath + "thresholdBlue.jpg", imgThresholdBlue);
	    imageWritingTime += System.currentTimeMillis() - prev;

	    // GROEN
	    prev = System.currentTimeMillis();
	    CvScalar minGreen = cvScalar(1, 70, 1, 0);//BGR-A
	    CvScalar maxGreen = cvScalar(120, 255, 70, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdGreen = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minGreen, maxGreen, imgThresholdGreen);
	    //smooth filter- median
	    //cvSmooth(imgThresholdGreen, imgThresholdGreen, CV_MEDIAN, 13);
	    //time recording
	    javaCVProcessingTime =+ (System.currentTimeMillis() - prev);
	    //save
	    prev = System.currentTimeMillis();
	    cvSaveImage(writeToPath + "thresholdGreen.jpg", imgThresholdGreen);
	    imageWritingTime += System.currentTimeMillis() - prev;

	    // ROOD
	    prev = System.currentTimeMillis();
	    CvScalar minRed = cvScalar(0, 0, 130, 0);//BGR-A
	    CvScalar maxRed= cvScalar(140, 70, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdRed = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minRed, maxRed, imgThresholdRed);
	    //smooth filter- median
	    //cvSmooth(imgThresholdRed, imgThresholdRed, CV_MEDIAN, 13);
	 	//time recording
	    javaCVProcessingTime =+ (System.currentTimeMillis() - prev);
	    //save
	    prev = System.currentTimeMillis();
	    cvSaveImage(writeToPath + "thresholdRed.jpg", imgThresholdRed);
	    imageWritingTime += System.currentTimeMillis() - prev;
	    
	    // WIT
	    prev = System.currentTimeMillis();
	    CvScalar minWhite = cvScalar(170, 170, 170, 0);//BGR-A
	    CvScalar maxWhite = cvScalar(255, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdWhite = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minWhite, maxWhite, imgThresholdWhite);
	    //CANNY EDGE
	    //TODO canny weg of niet?	
        cvCanny(imgThresholdWhite, imgThresholdWhite, 0, 255, 3);
	    //smooth filter- median
	    //cvSmooth(imgThresholdWhite, imgThresholdWhite, CV_MEDIAN, 13);
	    //time recording
	    javaCVProcessingTime =+ (System.currentTimeMillis() - prev);
	    //save
	    prev = System.currentTimeMillis();
	    cvSaveImage(writeToPath + "thresholdWhiteWithCanny.jpg", imgThresholdWhite);
	    imageWritingTime += System.currentTimeMillis() - prev;

	    // YELLOW
	    prev = System.currentTimeMillis();
	    CvScalar minYellow = cvScalar(0, 150, 150, 0);//BGR-A
	    CvScalar maxYellow = cvScalar(150, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdYellow = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minYellow, maxYellow, imgThresholdYellow);
	    // canny
	    //TODO canny weg of niet?	    
	    cvCanny(imgThresholdYellow, imgThresholdYellow, 0, 255, 3);
	    cvDilate(imgThresholdYellow, imgThresholdYellow, null, 1);

	    CvRect rect = new CvRect(0, 0, imgOrg.width(), imgOrg.height());
	    cvRectangle(imgThresholdYellow, cvPoint(rect.x(), rect.y()), cvPoint(rect.x()+rect.width(), rect.y()+rect.height()), 
	    		cvScalar(0,0,0,0), 1, 0, 0);
	    CvPoint seedPoint = cvPoint(0, 0);
		CvScalar floodColorBGR = cvScalar(255,255,255,0);
		//cvFloodFill(imgThresholdYellow, seedPoint , floodColorBGR , cvScalarAll(3), cvScalarAll(3),null,4,null);
//		System.out.println("Area rectangle = " + (rect.height()*rect.width()));
//		cvRectangle(imgOrg, cvPoint(rect.x(), rect.y()), cvPoint(rect.x()+rect.width(), rect.y()+rect.height()), 
//				cvScalar(255,0,0,0), 1, 0, 0);
		//cvRectangle(imgThresholdYellow, cvPoint(rect.x(), rect.y()), cvPoint(rect.x()+rect.width(), rect.y()+rect.height()), 
		//			cvScalar(255,255,255,0), 1, 0, 0);
	    //time recording
	    javaCVProcessingTime =+ (System.currentTimeMillis() - prev);
	    //save
	    prev = System.currentTimeMillis();
	    cvSaveImage(writeToPath + "thresholdYellowWithCanny.jpg", imgThresholdYellow);
	    imageWritingTime += System.currentTimeMillis() - prev;
	    
	    // HSV => DONKEREKLEUREN
	    prev = System.currentTimeMillis();
	    CvScalar minHSV = cvScalar(50, 50, 50, 0);//BGR-A
	    CvScalar maxHSV = cvScalar(255, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdHSVDarkColors = cvCreateImage(cvGetSize(imgHSV), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgHSV, minHSV, maxHSV, imgThresholdHSVDarkColors);
	    //CANNY EDGE
	    IplImage cannyEdge2 = cvCreateImage(cvGetSize(imgHSV), 8, 1);
	    cvCanny(imgThresholdHSVDarkColors, cannyEdge2, 0, 255, 3);
	    //time r
	    javaCVProcessingTime =+ (System.currentTimeMillis() - prev);
	    prev = System.currentTimeMillis();
        cvSaveImage(writeToPath + "cannyEdge.jpg", cannyEdge2);
	    cvSaveImage(writeToPath + "thresholdHSVDarkColors.jpg", imgThresholdHSVDarkColors);
	    imageWritingTime += System.currentTimeMillis() - prev;
	    
	    // HSV => GEEL
	    /*prev = System.currentTimeMillis();
	    CvScalar minYellowHSV = cvScalar(20, 100, 100, 0);//BGR-A
	    CvScalar maxYellozHSV = cvScalar(30, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdHSVYellow = cvCreateImage(cvGetSize(imgHSV), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgHSV, minYellowHSV, maxYellozHSV, imgThresholdHSVYellow);
	    //CANNY EDGE
	    IplImage cannyEdge3 = cvCreateImage(cvGetSize(imgHSV), 8, 1);
	    cvCanny(imgThresholdHSVYellow, cannyEdge3, 0, 255, 3);
	    //time r
	    javaCVProcessingTime =+ (System.currentTimeMillis() - prev);
	    prev = System.currentTimeMillis();
        cvSaveImage(writeToPath + "cannyEdge.jpg", cannyEdge3);
	    cvSaveImage(writeToPath + "thresholdHSVYellow.jpg", imgThresholdHSVYellow);
	    imageWritingTime += System.currentTimeMillis() - prev;*/
	    
	    findContoursAndHull(imgOrg, imgThresholdWhite);
	    findContoursAndHull(imgOrg, imgThresholdYellow);
	    findContoursAndHull(imgOrg, imgThresholdHSVDarkColors);
	    
	    prev = System.currentTimeMillis();
	    cvSaveImage(writeToPath + "zzz.jpg", imgOrg);
	    cvSaveImage("src/images/analyse.jpg", imgOrg);
	    imageWritingTime += System.currentTimeMillis() - prev;

	   /* cvNamedWindow("HERPEDERP");
	    cvShowImage("HERPEDERP", imgOrg);
	    cvWaitKey(0);*/

	    /*TODO: ge kunt iets fancy demonstreren door om de 5 seconden ofzo de verschillende afbeeldingen
	     *			te laten zien met CanvasFrame https://code.google.com/p/javacv/wiki/ConvertingOpenCV
	     * Of met dit:
	     * cvNamedWindow("HERPEDERP");
	     * cvNamedWindow("red");
	     * cvShowImage("original", imgOrg);
	     * cvShowImage("red", imgThresholdRed);
	     * cvWaitKey(0);
	     */
	    //***********************************************************************************
	    //***********************************************************************************
	    //***********************************************************************************
	}

	private void findContoursAndHull(IplImage imgOrg, IplImage imgThreshold) {
		CvSeq contour = new CvSeq();
		CvMemStorage memory = CvMemStorage.create();
		int numberOfContours = cvFindContours(imgThreshold, memory, contour, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
		//cvFindCoc
		//cvFindContours
		//int  numberOfContours= cvFindContours(imgThreshold, memory, contour, Loader.sizeof(CvContour.class),  CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE, cvPoint(-1, -1));
		System.out.println("Number of contours: " + numberOfContours);

		CvMemStorage storage = CvMemStorage.create();    
		while (contour != null && !contour.isNull()) {
			if (contour.elem_size() > 0) {
				CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class),
						storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 1);
				cvDrawContours(imgOrg, points, CvScalar.MAGENTA, CvScalar.MAGENTA, -1, 1, CV_AA);
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
					System.out.println("area = " + area);
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

					if (onEdge)
						System.out.println("Ligt op rand!");
					else
						System.out.println("Niet op rand.");

					if (! onEdge) {


						//convex omhullende
						CvSeq convexContour = cvConvexHull2(contour, storage, CV_CLOCKWISE, 1);
						cvDrawContours(imgOrg, convexContour, CvScalar.CYAN, CvScalar.CYAN, -1, 1, CV_AA);
						cvMoments(convexContour, moments, 1);
						double areaHull = cvGetCentralMoment(moments, 0, 0);
						System.out.println("areaHull = " + areaHull);

						System.out.println("Verhouding " + areaHull/area);

						//draw points
						for(int i = 0; i < contour.total(); i++){
							CvPoint v=new CvPoint(cvGetSeqElem(contour, i));
							cvDrawCircle(imgOrg, v, 1, CvScalar.WHITE, -1, 8, 0);
						}

						System.out.println("CENTER:("+centerX+", " + centerY+")");
						Vector vectorCenter = new Vector(centerX, centerY);

						ArrayList<Double> list = new ArrayList<Double>();
						for(int i = 0; i < contour.total(); i++){
							CvPoint v =new CvPoint(cvGetSeqElem(contour, i));
							Vector punt = new Vector(v.x(), v.y());
							list.add(punt.getDistance(vectorCenter));
						}            
						Collections.sort(list);
						double radius = list.get(list.size()-1);
						System.out.println("radius " + radius);
						double areaCircle = Math.PI*radius*radius;
						System.out.println("oppervlakte cirkel = "+ areaCircle);
						System.out.println("verhouding oppervlakte cirkel = "+ areaCircle/area);
						String imageTxt = "";
						if(areaHull/area > 1.2){
							shapes.add("Star");
							stars++;
							imageTxt = "S";
						}
						else if(areaCircle/area > 1.6){
							shapes.add("Rectangle");
							rectangles++;
							imageTxt = "R";
						}
						else if(areaCircle/area > 1.3){
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

						//KLEUR VINDEN & TOEVOEGEN
						CvScalar colorScalar = cvGet2D(imgOrg, centerY, centerX);   
						Color figureColor = findColor((int)colorScalar .val(2), (int)colorScalar .val(1), (int)colorScalar .val(0));           
						String figureColorString = colorToString(figureColor);
						colors.add(figureColorString);
						imageTxt = figureColorString.substring(0,1) + imageTxt;
						//System.out.println("R:" + colorScalar.val(2)+ " G:" + colorScalar.val(1)+" B:"+ colorScalar.val(0));
						foundColorCodesRGB.add("RGB: [" + colorScalar.val(2) + ", " + colorScalar.val(1) + ", "+ colorScalar.val(0) + "]");

						//CENTER TOEVOEGEN
						centers.add(new Vector(centerX, centerY));

						//TEKST TOEVOEGEN
						cvPutText(imgOrg, imageTxt, cvPoint((int)centerX, (int)centerY), 
								cvFont(2, 3), CvScalar.BLACK);


						//				CvRect rect = cvBoundingRect(contour, 0);
						//				System.out.println("Area rectangle = " + (rect.height()*rect.width()));
						//				cvRectangle(imgOrg, cvPoint(rect.x(), rect.y()), cvPoint(rect.x()+rect.width(), rect.y()+rect.height()), 
						//						cvScalar(255,0,0,0), 1, 0, 0);
						System.out.println();
					}
				}
            }
            contour = contour.h_next();
        }
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
		else{
			unidentifiedColors++;
			return "Unidentified";
		}
	}
	
	/**
	 * Finds the average color around centerX and centerY from the bufferedimage.
	 */
	/*private Color findColorAtXY(double centerX, double centerY, int numberOfPixelsToInspect) {
		int color;
		int averageRed = 0;
		int averageGreen = 0;
		int averageBlue = 0;
		
		int numberOfPixelsInspected = 0;
		for(int i = (int) centerX - numberOfPixelsToInspect/2; i <= (int) centerX + numberOfPixelsToInspect/2; i++){
			for(int j = (int) centerY - numberOfPixelsToInspect/2; j <= (int) centerY + numberOfPixelsToInspect/2; j++){
				if(i >= 0 && i < buffered.getWidth() && j >= 0 && j < buffered.getHeight()){
					color=  buffered.getRGB(i,j); 
					averageRed += (color & 0x00ff0000) >> 16;
					averageGreen += (color & 0x0000ff00) >> 8;
					averageBlue += color & 0x000000ff;
					numberOfPixelsInspected++;
					
				}
			}
		}
		//System.out.println("Aantal pixels geïnspecteerd voor de kleur: " + numberOfPixelsInspected);
		averageRed = averageRed/(numberOfPixelsInspected);
		averageGreen = averageGreen/(numberOfPixelsInspected);
		averageBlue = averageBlue/(numberOfPixelsInspected);
		 
		foundColorCodesRGB.add("RGB: [" + averageRed + ", " + averageGreen + ", "+ averageBlue + "]");
		return findColor(averageRed, averageGreen, averageBlue);
	}*/
	
	/**
	 * Finds the color blue, white, red, yellow or green. If it is an other color this returns cyan.
	 * 
	 * KLEUR: SCALAR(BLAUW,GROEN,ROOD)
	 * Blauw: Scalar(255,0,0)
	 * Wit: Scalar(255,255,255)
	 * Rood: Scalar(0,0,255)
	 * Geel: Scalar(0,255,255)		 
	 * Groen: Scalar(0,255,0)
	 * 
	 * Roze: Scalar(255,0,255)
	 * 
	 * Zwart: Scalar(0,0,0)
	 */
	private Color findColor(int averageRed, int averageGreen, int averageBlue) {
		int min = 150;
		if(averageRed >= min && averageGreen >= min && averageBlue >= min){
			return Color.white;
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
			//System.out.println("cyan");
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
		imageReadingTime = 0;
	}

	private ArrayList<Shape> makeShapeList() {
		ArrayList<Shape> shapeList = new ArrayList<Shape>();
		for(int i = 0; i < shapes.size(); i++){
			if(!shapes.get(i).equals("Unidentified")){
				shapeList.add(new Shape(centers.get(i), colors.get(i), shapes.get(i)));
			}
		}
		if(shapeList.size() > 0){
			return shapeList;
		}
		else{
			return null;
		}
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
}