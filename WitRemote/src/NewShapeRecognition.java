import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;
import static com.googlecode.javacv.cpp.opencv_features2d.*;
import static com.googlecode.javacv.cpp.opencv_flann.*;
import static com.googlecode.javacv.cpp.opencv_legacy.*;
import static com.googlecode.javacv.cpp.opencv_ml.*;
import static com.googlecode.javacv.cpp.opencv_nonfree.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_photo.*;
import static com.googlecode.javacv.cpp.opencv_stitching.*;
import static com.googlecode.javacv.cpp.opencv_video.*;
import static com.googlecode.javacv.cpp.opencv_videostab.*;
import Rooster.Shape;
import Rooster.Vector;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacpp.BoolPointer;
import com.googlecode.javacpp.BuildMojo;
import com.googlecode.javacpp.BytePointer;
import com.googlecode.javacpp.Builder;
import com.googlecode.javacpp.CharPointer;
import com.googlecode.javacpp.CLongPointer;
import com.googlecode.javacpp.DoublePointer;
import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.FunctionPointer;
import com.googlecode.javacpp.Generator;
import com.googlecode.javacpp.IntPointer;
import com.googlecode.javacpp.LongPointer;
import com.googlecode.javacpp.Parser;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.ShortPointer;
import com.googlecode.javacpp.SizeTPointer;
import com.googlecode.javacpp.annotation.*;
import com.googlecode.javacpp.properties.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * VORMEN: 1) Harten 2) Cirkels 3) Rechthoeken 4) Sterren
 * KLEUREN: 1) Blauw 2) Wit 3) Rood 4) Geel 5) Groen
 */

public class NewShapeRecognition{

	/**
	 * Originele afbeelding.
	 */
	private BufferedImage buffered = null;
	
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
	 * Aantallen...
	 */
	private int rectangles = 0;
	private int stars = 0;
	private int hearts = 0;
	private int circles = 0;	
	private int unidentifiedShapes = 0;
	private int unidentifiedColors = 0;
	
	
	private String originalImagePath;
	
	private long prev;
	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private String writeToPath;
	
	/**
	 * Tijd dat het duurt om de operaties op de afbeeldingen te doen...
	 */
	private long actualTimeToProcess = 0;
	private long javaCVTime = 0;

	private int minimalAreaOfRectangleAroundShape;
	private int maximalAreaOfRectangleAroundShape;
	
	public static void main(String args[]){
		NewShapeRecognition shapeRecog = new NewShapeRecognition("C:/Users/Jeroen/Desktop/Pics/TestC3.jpg");
		//NewShapeRecognition shapeRecog = new NewShapeRecognition("pic1.jpg");
		shapeRecog.doAllTheStuff();
	}	
	
	public NewShapeRecognition(String path){
		originalImagePath = path;
		writeToPath = "C:/Users/Jeroen/Desktop/ShapeRecognition/";
		//TODO: testen wat de minimale waarde hiervan moet zijn of robuuster maken adhv hoogte van zeppelin... 
		minimalAreaOfRectangleAroundShape = 3000;
		maximalAreaOfRectangleAroundShape = 14000;
	}
	
	public ArrayList<Shape> doAllTheStuff(){
		emptyAllParameters();
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		createImagesAndFindContours();

		//findShapesAndDrawPoints();
		
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
		System.out.println("Actual time needed (without writing away images): " + actualTimeToProcess + "ms");
		System.out.println("Actual time needed JAVACV: " + javaCVTime +"ms");
		
		ArrayList<Shape> shapeList = makeShapeList();
		
		return shapeList;
	}
	
	private void createImagesAndFindContours() {
		/*
		 * Afbeelding inladen als bufferedimage, gebruikt voor pixel te lezen. 
		 */
		prev = System.currentTimeMillis();
		File file= new File(originalImagePath);
		try {
			buffered = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to read image!");
		}
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME BufferedImage inlezen: " + (System.currentTimeMillis() - prev) + "ms");

		//***********************************************************************************
		//***********************************************************************************
		//***********************************************************************************
		prev = System.currentTimeMillis();
		IplImage imgOrg = cvLoadImage(originalImagePath, CV_LOAD_IMAGE_UNCHANGED);
		System.out.println("TIME IplImage inlezen: " + (System.currentTimeMillis() - prev) + "ms");
	    
	    //***** HSV en GREYSCALE *****//
	    // http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-image-thresholding-hsv-color.html
	    // Nog niet geprobeerd: http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-image-thresholding-hsv-color.html
	    IplImage imgHSV = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
	    cvCvtColor(imgOrg, imgHSV, CV_BGR2HSV);
	    //cvInRangeS(imgHSV, cvScalar(15, 234, 120, 0), cvScalar(21, 234, 120, 0), imgHSV);
	    cvSaveImage(writeToPath + "HSV.jpg", imgHSV);
	    
	    
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
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdBlue.jpg", imgThresholdBlue);

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
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdGreen.jpg", imgThresholdGreen);

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
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdRed.jpg", imgThresholdRed);

	    // WIT
	    prev = System.currentTimeMillis();
	    CvScalar minWhite = cvScalar(170, 170, 170, 0);//BGR-A
	    CvScalar maxWhite = cvScalar(255, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdWhite = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minWhite, maxWhite, imgThresholdWhite);
	    //CANNY EDGE
	    IplImage cannyEdge = cvCreateImage(cvGetSize(imgOrg), 8, 1);
        cvCanny(imgThresholdWhite, imgThresholdWhite, 0, 255, 3);
        cvSaveImage(writeToPath + "cannyEdge.jpg", cannyEdge);
	    //smooth filter- median
	    // cvSmooth(imgThresholdWhite, imgThresholdWhite, CV_MEDIAN, 13);
	    //time recording
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdWhite.jpg", imgThresholdWhite);

	    // YELLOW
	    prev = System.currentTimeMillis();
	    CvScalar minYellow = cvScalar(0, 150, 150, 0);//BGR-A
	    CvScalar maxYellow = cvScalar(150, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdYellow = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minYellow, maxYellow, imgThresholdYellow);
	    //smooth filter- median
	    // cvSmooth(imgThresholdWhite, imgThresholdWhite, CV_MEDIAN, 13);
	    //time recording
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdYellow.jpg", imgThresholdYellow);

	    // HSV => DONKEREKLEUREN
	    prev = System.currentTimeMillis();
	    CvScalar minWhiteHSV = cvScalar(50, 50, 50, 0);//BGR-A
	    CvScalar maxWhiteHSV = cvScalar(255, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdHSVDarkColors = cvCreateImage(cvGetSize(imgHSV), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgHSV, minWhiteHSV, maxWhiteHSV, imgThresholdHSVDarkColors);
	    //CANNY EDGE
	    IplImage cannyEdge2 = cvCreateImage(cvGetSize(imgHSV), 8, 1);
	    cvCanny(imgThresholdHSVDarkColors, cannyEdge2, 0, 255, 3);
        cvSaveImage(writeToPath + "cannyEdge.jpg", cannyEdge2);
	    //smooth filter- median
	    //cvSmooth(imgThresholdWhite, imgThresholdWhite, CV_MEDIAN, 13);
	    //time recording
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdHSVDarkColors.jpg", imgThresholdHSVDarkColors);
	    
	    findContoursAndHull(imgOrg, imgThresholdWhite);
	    findContoursAndHull(imgOrg, imgThresholdYellow);
	    findContoursAndHull(imgOrg, imgThresholdHSVDarkColors);
	    
	    cvSaveImage(writeToPath + "zzz.jpg", imgOrg);

	    /*TODO: ge kunt iets fancy demonstreren door om de 5 seconden ofzo de verschillende afbeeldingen
	     *			te laten zien met CanvasFrame https://code.google.com/p/javacv/wiki/ConvertingOpenCV
	     * Of met dit:
	     * cvNamedWindow("original");
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
                // cv Spatial moment : Mji=sumx,y(I(x,y)•xj•yi)
                // where I(x,y) is the intensity of the pixel (x, y).
                double momX10 = cvGetSpatialMoment(moments, 1, 0); // (x,y)
                double momY01 = cvGetSpatialMoment(moments, 0, 1);// (x,y)
                double area = cvGetCentralMoment(moments, 0, 0);
                centerX = (int) (momX10 / area);
                centerY = (int) (momY01 / area);
                //System.out.println("("+posX+", "+posY+")");
                if(area > 500){
                System.out.println("area = " + area);
                
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
                	//System.out.println(" X value = "+v.x()+" ; Y value ="+v.y());
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
				
				//KLEUR TOEVOEGEN
				Color figureColor = findColorAtXY(centerX, centerY, 3);
				String figureColorString = colorToString(figureColor);
				colors.add(figureColorString);
				imageTxt = figureColorString.substring(0,1) + imageTxt;
				
				
				//CENTER TOEVOEGEN
				centers.add(new Vector(centerX, centerY));
	
				cvPutText(imgOrg, imageTxt, cvPoint((int)centerX, (int)centerY), 
						cvFont(2, 3), CvScalar.BLACK);
				
				
//				CvRect rect = cvBoundingRect(contour, 0);
//				System.out.println("Area rectangle = " + (rect.height()*rect.width()));
//				cvRectangle(imgOrg, cvPoint(rect.x(), rect.y()), cvPoint(rect.x()+rect.width(), rect.y()+rect.height()), 
//						cvScalar(255,0,0,0), 1, 0, 0);
				System.out.println();
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
	private Color findColorAtXY(double centerX, double centerY, int numberOfPixelsToInspect) {
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
		actualTimeToProcess = 0;
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