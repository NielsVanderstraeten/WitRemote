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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import Rooster.Shape;
import Rooster.Vector;

import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
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
	
	private Mat imageOriginal,imageGREY,imageBlurr,imageAdaptiveThreshold,imageThresh1,
			imageThresh2, imageHSV;
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
	
	/**
	 * Threshvalue voor Otsu (maakt niks uit eigenlijk want algoritme bepaalt automatisch de waarde...)
	 */
	private int threshValue1;
	/**
	 * Threshvalue voor binary inverted.
	 */
	private int threshValue2;
	/**
	 * 1 voor twee keer thresh en 2 voor adaptiveThreshold
	 */
	private int threshMethode;
	
	public static void main(String args[]){
		NewShapeRecognition shapeRecog = new NewShapeRecognition("C:/Users/Jeroen/Desktop/pic5.jpg");
		//NewShapeRecognition shapeRecog = new NewShapeRecognition("pic1.jpg");
		shapeRecog.doAllTheStuff();
	}	
	
	public NewShapeRecognition(String path){
		originalImagePath = path;
		writeToPath = "C:/Users/Jeroen/Desktop/ShapeRecognition/";
		//TODO: testen wat de minimale waarde hiervan moet zijn of robuuster maken adhv hoogte van zeppelin... 
		minimalAreaOfRectangleAroundShape = 3000;
		maximalAreaOfRectangleAroundShape = 14000;
		threshValue1 = 100; // voor otsu maakt het niks uit
		threshValue2 = 90;  // 95 voor de Test klasse
		threshMethode = 1; // 1 voor twee keer thresh en 2 voor adaptiveThreshold
	}
	
	public ArrayList<Shape> doAllTheStuff(){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		createImagesAndFindContours();

		findShapesAndDrawPoints();
		
		System.out.println("Rectangles: " + rectangles);
		System.out.println("Stars: "  + stars);
		System.out.println("Hearts: " + hearts);
		System.out.println("Circles: " + circles);
		System.out.println("Unidentified shapes: " + " " + unidentifiedShapes);
		System.out.println("Unidentified colors: " + unidentifiedColors);
		System.out.println("Shapes found: ");
		System.out.println(colors.size() + " " + shapes.size() + " "+ points.size());
		for(int i = 0; i <= shapes.size() - 1; i++){
			System.out.println((i+1) + ") "  + colors.get(i) + " " + shapes.get(i) + 
					" (" + points.get(i) + " points) Found color-codes: " + foundColorCodesRGB.get(i));
		}
		System.out.println("Actual time needed (without writing away images): " + actualTimeToProcess + "ms");
		System.out.println("Actual time needed JAVACV: " + javaCVTime +"ms");
		
		ArrayList<Shape> shapeList = makeShapeList();
		
		//emptyAllParameters();
		
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
		
		//****** Afbeelding inladen, TODO nog nader bekijken wat Core.DEPTH_... juist is.*****//
		prev = System.currentTimeMillis();
		// image = Highgui.imread(originalImagePath, Imgproc.COLOR_BGR2GRAY);
		// TODO: MASKS !!!
		imageOriginal = Highgui.imread(originalImagePath);
		imageHSV = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		imageGREY = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		imageBlurr = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		imageAdaptiveThreshold = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		imageThresh1 = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		imageThresh2 = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME MatImage inlezen + Mats aan maken: " + (System.currentTimeMillis() - prev) + "ms");

		//***********************************************************************************
		//***********************************************************************************
		//***********************************************************************************
		IplImage imgOrg = cvLoadImage(originalImagePath, CV_LOAD_IMAGE_UNCHANGED);
		
		// ignore deze, zo kon ik snel terug naar de nieuwe code gaan :p
		IplImage channelRed = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.height());
	    IplImage channelGreen = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), 1);
	    IplImage channelBlue;
	    IplImage channelGreenBlue;
	    IplImage channelGreenRed;
	    IplImage channelRedBlue;
	    IplImage channelRedSeperated;
	    IplImage channelBlueSeperated;
	    IplImage channelGreenSeperated;

	    
	    // CvScalar arg2 = new CvScalar(0, 0, 255, 0);
	    // BEWARE: the params for the cvScalar constructor are not in RGB order
	    //      it is:   new cvScalar(blue, green, red, unused)
	    // note how the 4th scalar is unused.
	   
	    //***** HSV en GREYSCALE *****//
	    // http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-image-thresholding-hsv-color.html
	    // Nog niet geprobeerd: http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-image-thresholding-hsv-color.html
	    IplImage imgHSV = cvCreateImage(cvGetSize(imgOrg), imgOrg.depth(), imgOrg.nChannels());
	    IplImage imgGrayscale = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    cvCvtColor(imgOrg, imgHSV, CV_BGR2HSV);
	    cvCvtColor(imgOrg, imgGrayscale, CV_BGR2GRAY);
	    //cvInRangeS(imgHSV, cvScalar(15, 234, 120, 0), cvScalar(21, 234, 120, 0), imgHSV);
	    cvSaveImage(writeToPath + "HSV.jpg", imgHSV);
	    cvSaveImage(writeToPath + "grayScale.jpg", imgGrayscale);
	    
	    //***** Color based thresholding *****//
	    //http://ganeshtiwaridotcomdotnp.blogspot.be/2011/12/javacv-simple-color-detection-using.html
	    //TODO: kleuren al testend bepalen of overschakelen op HSV.
	    //TODO: HSV zou beter moeten zijn omdat de invloed van licht/belichting (veel) minder is dacht ik
	    
	    // ROOD
	    prev = System.currentTimeMillis();
	    CvScalar minRed = cvScalar(0, 0, 130, 0);//BGR-A
	    CvScalar maxRed= cvScalar(140, 110, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdRed = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minRed, maxRed, imgThresholdRed);
	    //smooth filter- median
	    cvSmooth(imgThresholdRed, imgThresholdRed, CV_MEDIAN, 13);
	 	//time recording
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdRed.jpg", imgThresholdRed);
	    
	    // GROEN
	    prev = System.currentTimeMillis();
	    CvScalar minGreen = cvScalar(50, 1, 1, 0);//BGR-A
	    CvScalar maxGreen = cvScalar(255, 100, 100, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdGreen = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minGreen, maxGreen, imgThresholdGreen);
	    //smooth filter- median
	    cvSmooth(imgThresholdGreen, imgThresholdGreen, CV_MEDIAN, 13);
	    //time recording
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdGreen.jpg", imgThresholdGreen);
	    
	    // WIT
	    prev = System.currentTimeMillis();
	    CvScalar minWhite = cvScalar(170, 170, 170, 0);//BGR-A
	    CvScalar maxWhite = cvScalar(255, 255, 255, 0);//BGR-A
	    //create binary image of original size
	    IplImage imgThresholdWhite = cvCreateImage(cvGetSize(imgOrg), 8, 1);
	    //apply thresholding
	    cvInRangeS(imgOrg, minWhite, maxWhite, imgThresholdWhite);
	    //smooth filter- median
	   // cvSmooth(imgThresholdWhite, imgThresholdWhite, CV_MEDIAN, 13);
	    //time recording
	    javaCVTime =+ (System.currentTimeMillis() - prev);
	    //save
	    cvSaveImage(writeToPath + "thresholdWhite.jpg", imgThresholdWhite);
	    
	    int header_size = 0;
		CvSeq first_contour = new CvSeq();
		CvMemStorage contouren = new CvMemStorage();
		cvFindContours(imgThresholdWhite, contouren, first_contour, header_size, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
		System.out.println("Test9999");
		System.out.println(contouren.top());
	    //Imgproc.findContours(imageThresh1, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	    
	    //cvApproxPoly(arg0, arg1, arg2, arg3, arg4, arg5)
	    //cvConvexHull2(arg0, arg1, arg2, arg3)
	    
	    // TODO
	    // wat als ge nu eerst foto pakt van de vloer en zowa het gemiddelde bepaalt van die kleur,
	    // daarna gade die kleur (en natuurlijk de kleuren die daar dicht bij liggen) er uit filteren 
		// met bovenstaande methode ...
	    // TODO

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

	    //***** Afbeelding BGR naar HSV *****//
	    prev = System.currentTimeMillis();
	    //TODO er is ook nog nen COLOR_BGR2HSV_FULL
	    Imgproc.cvtColor(imageOriginal, imageHSV, Imgproc.COLOR_BGR2HSV);
	    actualTimeToProcess =+ (System.currentTimeMillis() - prev);
	    System.out.println("TIME HSV: " + (System.currentTimeMillis() - prev) + "ms");

	    //***** Afbeeldingen grijs maken*****//
		prev = System.currentTimeMillis();
		Imgproc.cvtColor(imageOriginal, imageGREY, Imgproc.COLOR_BGR2GRAY); 
		//TODO: Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_RGB2GRAY);
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME cvtColor: " + (System.currentTimeMillis() - prev) + "ms");

		//***** Blur *****//
		prev = System.currentTimeMillis();
		Imgproc.medianBlur(imageGREY, imageBlurr, 5);
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME Blur: " + (System.currentTimeMillis() - prev) + "ms");
		
		//***** Adaptive Thresh *****//
		prev = System.currentTimeMillis();
		Imgproc.adaptiveThreshold(imageBlurr, imageAdaptiveThreshold, 255, 
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 13, 3); /// Gaussian_C ipv Mean_C
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME AdaptiveThreshold: " + (System.currentTimeMillis() - prev) + "ms");

		//***** Otsu en binary inverted thresh *****//
		prev = System.currentTimeMillis();
		//Imgproc.threshold(src, dst, thresh, maxval, type)
		double thresh1 = Imgproc.threshold(imageBlurr, imageThresh1, threshValue1, 255, Imgproc.THRESH_OTSU);
		//TODO
		double thresh2 = Imgproc.threshold(imageBlurr, imageThresh2, threshValue2, 255, Imgproc.THRESH_BINARY_INV);
		System.out.println();
		System.out.println("Thresh1 = " + thresh1 + ", Thresh2 = " + thresh2);
		System.out.println();
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME Mat voor Thresh aan maken + threshold: " + (System.currentTimeMillis() - prev) + "ms");
		
		//***** Voorbeelden van imageHSV, imageA, imageBlurr en imageThresh wegschrijven. *****//
		prev = System.currentTimeMillis();
		Highgui.imwrite(writeToPath + "testOriginal.png", imageOriginal);
		Highgui.imwrite(writeToPath + "testHSV.png", imageHSV);
		Highgui.imwrite(writeToPath + "testImagGREY.png",imageGREY);
		Highgui.imwrite(writeToPath + "testImageADAPTIVE.png",imageAdaptiveThreshold);
		Highgui.imwrite(writeToPath + "testBlurr.png",imageBlurr);
		Highgui.imwrite(writeToPath + "testThreshOtsu.png",imageThresh1);
		Highgui.imwrite(writeToPath + "testThreshBinaryInverted.png",imageThresh2);
		
		System.out.println("TIME Images wegschrijven: " + (System.currentTimeMillis() - prev) + "ms");

		
		//***** Contours vinden. *****//
		prev = System.currentTimeMillis();
		if(threshMethode == 1){
			Imgproc.findContours(imageThresh1, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			Imgproc.findContours(imageThresh2, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		}
		else if(threshMethode == 2){
			Imgproc.findContours(imageAdaptiveThreshold, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		}
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME Contours vinden: " + (System.currentTimeMillis() - prev) + "ms");
		System.out.println("Aantal gevonden contours1: " + contours.size());
		System.out.println("Aantal gevonden contours2: " + contours.size());
	}

	private void findShapesAndDrawPoints() {
		/**
		 * KLEUR: SCALAR(BLAUW,GROEN,ROOD)
		 * Blauw: Scalar(255,0,0)
		 * Wit: Scalar(255,255,255)
		 * Rood: Scalar(0,0,255)
		 * Geel: Scalar(0,255,255)		 
		 * Groen: Scalar(0,255,0)
		 * 
		 * Zwart: Scalar(0,0,0)
		 */
		
		prev = System.currentTimeMillis();
		for(int i=0; i< contours.size();i++){
			Rect rect = Imgproc.boundingRect(contours.get(i));
			
			//TODO MANIER VERFIJNEN OM TE KLEINE GEVONDEN VORMEN TE VERWIJDEREN
			if((rect.height*rect.width*2) > minimalAreaOfRectangleAroundShape && (rect.height*rect.width*2) < maximalAreaOfRectangleAroundShape) {
				System.out.println("Area of rectangle: " + (rect.height*rect.width*2));
				MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
				MatOfPoint2f mMOP2f2 = new MatOfPoint2f();
				contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);

				double length = Imgproc.arcLength(mMOP2f1, true);

				//Imgproc.approxPolyDP(curve, approxCurve, epsilon, closed);
				Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 0.02*length, true);

				/**
				 * Midden van contour krijgen:
				 */
				Moments moments = Imgproc.moments(contours.get(i));
				double centerX = (moments.get_m10()/moments.get_m00());
				double centerY = (moments.get_m01()/moments.get_m00());
				centers.add(new Vector(centerX, centerY));
				points.add(mMOP2f2.height());
				
				if(mMOP2f2.height() <= 6){
					shapes.add("Rectangle");
					rectangles++;
				}
				else if(mMOP2f2.height() <= 8){
					shapes.add("Circle");
					circles++;
				}
				else if(mMOP2f2.height() <= 9){
					shapes.add("Heart");
					hearts++;
				}
				else if(mMOP2f2.height() <= 10){
					shapes.add("Star");
					stars++;
				}
				else {
					shapes.add("Unidentified");
					unidentifiedShapes++;
				} 
				
				/**
				 * Diameter van cirkel verkrijgen:
				 */
				double oppervlakte = Imgproc.contourArea(contours.get(i));
				int diameter = (int) Math.sqrt(4 * oppervlakte/Math.PI);
				
				/**
				 * Kleur vinden.
				 */
				Color figureColor = findColorAtXY(centerX, centerY, 3);
				colors.add(colorToString(figureColor));
			}
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

		System.out.println("Aantal pixels geïnspecteerd voor de kleur: " + numberOfPixelsInspected);
		averageRed = averageRed/(numberOfPixelsInspected);
		averageGreen = averageGreen/(numberOfPixelsInspected);
		averageBlue = averageBlue/(numberOfPixelsInspected);
		 
		foundColorCodesRGB.add("RGB: [" + averageRed + ", " + averageGreen + ", "+ averageBlue + "]");
		//System.out.println("Averages (red, green, blue): " + averageRed + ", " + averageGreen + ", "+ averageBlue);
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
		//System.out.println("AverageColors: "+ averageRed + " " +averageGreen +" "+  averageBlue);
		//color = new Color(averageRed, averageGreen, averageBlue);
		
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