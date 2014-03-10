import gui.KirovAirship;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import Rooster.Grid;
import Rooster.Shape;
import Rooster.Vector;
import commands.Command;
import commands.SetPosition;

/**
 * VORMEN: 1) Harten 2) Cirkels 3) Rechthoeken 4) Sterren
 * KLEUREN: 1) Blauw 2) Wit 3) Rood 4) Geel 5) Groen
 */

public class ShapeRecognition implements Runnable{

	private BufferedImage buffered = null;
	private ArrayList<String> shapes = new ArrayList<String>(); 
	private ArrayList<String> colors = new ArrayList<String>();
	
	/**
	 * Aantal punten die de benaderende polygonaal heeft
	 */
	private ArrayList<Integer> points = new ArrayList<Integer>();
	
	/**
	 * Vectors die de centrums/zwaartepunten van de figuren bijhoudt
	 */
	private ArrayList<Vector> centers = new ArrayList<Vector>();
	
	/**
	 * Gevonden RGB codes
	 */
	private ArrayList<String> foundColorCodesRGB = new ArrayList<String>();
	
	private int rectangles = 0;
	private int stars = 0;
	private int hearts = 0;
	private int circles = 0;	
	private int unidentifiedShapes = 0;
	private int unidentifiedColors = 0;
	private String originalImagePath;
	private Mat imageOriginal;
	private Mat imageHSV;
	private Mat imageBlurr;
	private Mat imageAdaptiveThreshold;
	private Mat imageThresh1;
	private Mat imageThresh2;
	private long prev;
	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private String writeToPath;
	private long actualTimeToProcess = 0;

	private boolean printAllInfo;
	private boolean saveAllImages;
	
	private int minimalAreaOfRectangleAroundShape;
	private int maximalAreaOfRectangleAroundShape;
	
	private int threshValue1;
	private int threshValue2;
	/**
	 * 1 voor twee keer thresh en 2 voor adaptiveThreshold
	 */
	private int threshMethode;
	private String analyseImagePath = "D:/";
	
//	public static void main(String args[]){
//		ShapeRecognition test35 = new ShapeRecognition("C:/Users/Jeroen/Desktop/test97.jpg", n);
//		test35.run();
//	}	
	
	private KirovAirship gui;
	private Grid grid;
	private LinkedList<Command> queue;
	
	public ShapeRecognition(String path, KirovAirship gui, Grid grid, LinkedList<Command> queue){
		this.gui = gui;
		this.grid = grid;
		this.queue = queue;
		originalImagePath = path;
		writeToPath = "C:/Users/Jeroen/Desktop/";
		//TODO: testen wat de minimale waarde hiervan moet zijn of robuuster maken adhv hoogte van zeppelin... 
		minimalAreaOfRectangleAroundShape = 3000;
		maximalAreaOfRectangleAroundShape = 14000;
		printAllInfo = true;
		saveAllImages = false;
		threshValue1 = 100; // voor otsu maakt het niks uit
		threshValue2 = 90;  // 95 voor de Test klasse
		threshMethode = 1; // 1 voor twee keer thresh en 2 voor adaptiveThreshold
	}
	
	public void run(){
		System.out.println("Analysing picture...");
		/* Library loaden. NUMMER MOET OVEREENKOMEN MET UW VERSIE
		 * Ook mogelijk om dit automatisch te doen met NATIVE_LIBRARY_NAME, werkte in het begin niet?
		 */
		//System.loadLibrary("opencv_java246");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		createImagesAndFindContours();

		findShapesAndDrawPoints();
		
		gui.updatePhoto();
		
		if(printAllInfo == true){
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
			System.out.println();
		}
		
		ArrayList<Shape> shapeList = makeShapeList();
		
		Vector position = grid.getPosition(shapeList);
		double rotation = grid.getRotation(shapeList);
		
		queue.add(new SetPosition((int) position.getX(), (int) position.getY(), rotation));
		
		emptyAllParameters();
	}

	private ArrayList<Shape> makeShapeList() {
		ArrayList<Shape> shapeList = new ArrayList<Shape>();
		for(int i = 0; i < shapes.size(); i++){
			if(!shapes.get(i).equals("Unidentified")){
				shapeList.add(new Shape(centers.get(i), colors.get(i), shapes.get(i)));
			}
		}
		return shapeList;
		
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
			System.out.println("Failed to read image!!!!!! JC");
		}
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		if(printAllInfo){
			System.out.println("TIME BufferedImage inlezen: " + (System.currentTimeMillis() - prev) + "ms");
		}
		
		//************************************************************************************//
		//****** Afbeelding inladen, TODO nog nader bekijken wat Core.DEPTH_... juist is.*****//
		//************************************************************************************//
		prev = System.currentTimeMillis();
		// image = Highgui.imread(originalImagePath, Imgproc.COLOR_BGR2GRAY);
		imageOriginal = Highgui.imread(originalImagePath);
		
		//Mat image = Highgui.imread("C:/Users/Jeroen/Desktop/shape8.png", Imgproc.COLOR_RGB2GRAY);
		imageHSV = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		imageBlurr = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
//TODO !!!!		imageAdaptiveThreshold = new Mat(imageOriginal.size(), Core.DEPTH_MASK_ALL);
		imageAdaptiveThreshold = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		System.out.println("TIME MatImage inlezen + Mats aan maken: " + (System.currentTimeMillis() - prev) + "ms");

		//*******************************************************************************//
		//****** Afbeeldingen bewerken, grijs maken, blur toevoegen en nog bewerken.*****//
		//*******************************************************************************//
		prev = System.currentTimeMillis();
		Imgproc.cvtColor(imageOriginal, imageHSV, Imgproc.COLOR_BGR2GRAY); 		
		//************//
		//TODO: Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_RGB2GRAY);
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		if(printAllInfo){
			System.out.println("TIME cvtColor: " + (System.currentTimeMillis() - prev) + "ms");
		}
		
		prev = System.currentTimeMillis();
		//Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(9,9), 3); //////////////////////////
		Imgproc.medianBlur(imageHSV, imageBlurr, 5);
		//Imgproc.bilateralFilter(imageHSV, imageBlurr, 9, 75, 75);
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		if(printAllInfo){
			System.out.println("TIME GaussianBlur: " + (System.currentTimeMillis() - prev) + "ms");
		}
		
		//Imgproc.medianBlur(imageHSV, imageBlurr, 5);
		//Imgproc.GaussianBlur(Mat source, Mat destiny, Size ksize, double sigmaX);

		prev = System.currentTimeMillis();
		Imgproc.adaptiveThreshold(imageBlurr, imageAdaptiveThreshold, 255, 
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 13, 3); /// Gaussian_C ipv Mean_C
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		if(printAllInfo){
			System.out.println("TIME AdaptiveThreshold: " + (System.currentTimeMillis() - prev) + "ms");
		}
		
		//*******************//
		//*****THRESHOLD*****//
		//*******************//
		prev = System.currentTimeMillis();
		imageThresh1 = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		imageThresh2 = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		//Imgproc.threshold(src, dst, thresh, maxval, type)
		double thresh1 = Imgproc.threshold(imageBlurr, imageThresh1, threshValue1, 255, Imgproc.THRESH_OTSU);
		//TODO
		double thresh2 = Imgproc.threshold(imageBlurr, imageThresh2, threshValue2, 255, Imgproc.THRESH_BINARY_INV);
		if(printAllInfo){
			System.out.println();
			System.out.println("Thresh1 = " + thresh1 + ", Thresh2 = " + thresh2);
			System.out.println();
		}
		//imageThresh2 = invertMatImage(imageThresh2);		
		
		/*Mat imageFloodfill = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		
		Imgproc.floodFill(imageFloodfill, imageBlurr, new Point(0, 0), new Scalar(0,0,64));
		Highgui.imwrite(writeToPath + "test1floodFill.png", imageFloodfill);
		System.out.println("teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeest");*/
		
		//**************INVERT IMAGE***************//
		/*int w = buffered.getWidth();
		int h = buffered.getHeight();
		BufferedImage invertedBuffered = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
		BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invertTable), null);
		
		invertOp.filter(buffered, invertedBuffered);

		File outputfile = new File(writeToPath+"imageInverted.png");
		try {
			ImageIO.write(invertedBuffered, "png", outputfile);
		} catch (IOException e) {
			System.out.println("Failed to write invertedBuffered.");
		}
		
		//Mat imageInverted = Highgui.imread(writeToPath+"imageInverted.png");
		Mat imageInverted = invertMatImage(imageOriginal);
		Highgui.imwrite(writeToPath + "INVERTEDORIGINAL.png",imageInverted);
		Mat imageInvertedHSV = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		Mat imageInvertedBlurr = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		Mat imageInvertedThresh = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
		
		Imgproc.cvtColor(imageInverted, imageInvertedHSV, Imgproc.COLOR_BGR2GRAY);
		//imageInvertedHSV = invertMatImage(imageInvertedHSV);
		Imgproc.GaussianBlur(imageInvertedHSV, imageInvertedBlurr, new Size(9,9), 0);
		Imgproc.threshold(imageInvertedBlurr, imageInvertedThresh, 100, 255, Imgproc.THRESH_TRUNC);
		Highgui.imwrite(writeToPath + "INVERFTEETFD.png",imageInvertedThresh);
		 */
		//***************END INVERT IMAGE***********//


		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		if(printAllInfo){
			System.out.println("TIME Mat voor Thresh aan maken + threshold: " + (System.currentTimeMillis() - prev) + "ms");
		}
		/**
		 * Voorbeelden van imageHSV, imageA, imageBlurr en imageThresh wegschrijven.
		 */
		if(saveAllImages){
			prev = System.currentTimeMillis();
			Highgui.imwrite(writeToPath + "test1Original.png", imageOriginal);
			Highgui.imwrite(writeToPath + "test1ImagHSV.png",imageHSV);
			Highgui.imwrite(writeToPath + "test1ImageA.png",imageAdaptiveThreshold);
			Highgui.imwrite(writeToPath + "test1Blurr.png",imageBlurr);
			Highgui.imwrite(writeToPath + "test1Thresh1.png",imageThresh1);
			Highgui.imwrite(writeToPath + "test1Thresh2.png",imageThresh2);

			System.out.println("TIME Images wegschrijven: " + (System.currentTimeMillis() - prev) + "ms");
		}
		/**
		 * Contours vinden.
		 */
		prev = System.currentTimeMillis();
		if(threshMethode == 1){
			Imgproc.findContours(imageThresh1, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			Imgproc.findContours(imageThresh2, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		}
		else if(threshMethode == 2){
			Imgproc.findContours(imageAdaptiveThreshold, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		}
		//Imgproc.findContours(imageBlurr, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		
		
		//Imgproc.drawContours(imageBlurr, contours, contours.size()-1, new Scalar(0,0,255)); ZOU DE GEGEVEN CONTOURS MOETEN TEKENEN OP DE GEGEVEN AFBEELDING IN DE GEGEVEN KLEUR MET DE GEGEVEN DIKTE MAAR DOET DA DUS NI Hé
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		if(printAllInfo){
			System.out.println("TIME Contours vinden: " + (System.currentTimeMillis() - prev) + "ms");
			//LAATSTE VERWIJDEREN OMDAT DAT DE KADER ZELF IS...

			System.out.println("Aantal gevonden contours1: " + contours.size());
			//contours.remove(contours.size()-1);
			System.out.println("Aantal gevonden contours2: " + contours.size());
		}
	}
	
//	private Mat invertMatImage(Mat ImageToInvert){
//		Mat invertcolormatrix= new Mat(ImageToInvert.rows(),ImageToInvert.cols(), ImageToInvert.type(), new Scalar(255,255,255));
//		Mat invertedImage = new Mat(imageOriginal.size(), Core.DEPTH_MASK_8U);
//		Core.subtract(invertcolormatrix, ImageToInvert, invertedImage);
//		return invertedImage;
//	}

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

			//if(rect.height > 25){
			//TODO MANIER VERFIJNEN OM TE KLEINE GEVONDEN VORMEN TE VERWIJDEREN
			if((rect.height*rect.width*2) > minimalAreaOfRectangleAroundShape && (rect.height*rect.width*2) < maximalAreaOfRectangleAroundShape) {
				if(printAllInfo){
					System.out.println("Area of rectangle: " + (rect.height*rect.width*2));
				}
				MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
				MatOfPoint2f mMOP2f2 = new MatOfPoint2f();
				contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);

				double length = Imgproc.arcLength(mMOP2f1, true);

				//Imgproc.approxPolyDP(curve, approxCurve, epsilon, closed);
				//Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 2, true);
				Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 0.02*length, true);
				//Imgproc.contour

				//System.out.println("List of points:");

				/**
				 * Midden van contour krijgen:
				 */
				Moments moments = Imgproc.moments(contours.get(i));
				//int centerX = (int) (moments.get_m10()/moments.get_m00());
				//int centerY = (int) (moments.get_m01()/moments.get_m00());
				double centerX = (moments.get_m10()/moments.get_m00());
				double centerY = (moments.get_m01()/moments.get_m00());
				centers.add(new Vector(centerX, centerY));

				double count = 0;
				double n =  mMOP2f2.toList().size();
				for(Point p : mMOP2f2.toList())
				{
					count += calcDistance(centerX, p.x, centerY, p.y);
				}

				double r_avg = count/n;

				count = 0;
				for(Point p : mMOP2f2.toList())
				{
					count += Math.pow( ( calcDistance(centerX, p.x, centerY, p.y) - r_avg) / r_avg, 2);
				}
				double variance = count/n;
				if(printAllInfo){
					System.out.println("Variance :" + variance);
				}

				//System.out.println(mMOP2f2.size()  + ", " + mMOP2f2.height());
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
				/*else if(mMOP2f2.height() <= 15){
					shapes.add("Heart");
					hearts++;
				}
				else if(mMOP2f2.height() > 15){
					shapes.add("Circle");
					circles++;
				}*/
				else {
					shapes.add("Unidentified");
					unidentifiedShapes++;
				} 

				//System.out.println(detectColour(image, centerX, centerY));

				/**
				 * Diameter van cirkel verkrijgen:
				 */
				double oppervlakte = Imgproc.contourArea(contours.get(i));
				int diameter = (int) Math.sqrt(4 * oppervlakte/Math.PI);

				Color figureColor = findColorAtXY(centerX, centerY, 3);
				colors.add(colorToString(figureColor));


				//System.out.println(Imgproc.contourArea(contours.get(i)));
				//if (Imgproc.contourArea(contours.get(i)) > 50 && numberOfPointsToApprox.get(i) == 4){
				//if (Imgproc.contourArea(contours.get(i)) > 50){
				//TODO mss deze if toch is weglaten, idk, idc actually
				if(true){
					// VOOR CIRKELS TE VINDEN Imgproc.HoughCircles(imageBlurr, circles, method, dp, minDist);

					//System.out.println(rect.x +","+rect.y+","+rect.height+","+rect.width);


					//***************************//
					//*****RECHTHOEK TEKENEN*****//
					//***************************//
					//KLEUR: SCALAR(BLAUW,GROEN,ROOD)
					if(saveAllImages){
						Scalar colorScalar = new Scalar(figureColor.getBlue(), figureColor.getGreen(), figureColor.getRed());

						Core.rectangle(imageOriginal, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),
								colorScalar);
					}
					//*************************//
					//*****CIRKEL TEKENEN*****//
					//*************************//
					if(saveAllImages){
						Scalar colorScalar = findColorAtXYBGR(centerX, centerY);
						//colorScalar = findColorAtXY(centerX, centerY);
						Core.circle(imageOriginal, new Point(centerX,centerY), diameter/2, colorScalar,3);
					}
					//********************************************//
					//*****MIDDELPUNT EN SCALARPUNTEN TEKENEN*****//
					//********************************************//

					//*****MIDDELPUNT*****//
					Color pointColor = Color.BLACK;
					Scalar pointScalar = new Scalar(pointColor.getBlue(), pointColor.getGreen(), pointColor.getRed());
					Core.circle(imageOriginal, new Point(centerX,centerY), 1, pointScalar, 2);
					pointColor = Color.yellow;
					Core.circle(imageBlurr, new Point(centerX,centerY), 1, pointScalar, 2);

					//*****SCALARPUNTEN*****//
					pointColor = Color.magenta;
					pointScalar = new Scalar(pointColor.getBlue(), pointColor.getGreen(), pointColor.getRed());
					for(int j = 0; j < mMOP2f2.height(); j++){
						Core.circle(imageOriginal, new Point(mMOP2f2.toArray()[j].x,mMOP2f2. toArray()[j].y), 1, pointScalar, 2);
						Core.circle(imageBlurr, new Point(mMOP2f2.toArray()[j].x,mMOP2f2. toArray()[j].y), 1, pointScalar, 2);
					}

				}
			}
		}
		if(printAllInfo){
			System.out.println("TIME Kleur bepalen en punten bepalen: " + (System.currentTimeMillis() - prev) + "ms");
		}
		actualTimeToProcess =+ (System.currentTimeMillis() - prev);
		prev = System.currentTimeMillis();
		
		Highgui.imwrite(analyseImagePath + "analyse.png",imageOriginal);
		
		if(saveAllImages){
			Highgui.imwrite(writeToPath + "test2Blurr.png",imageBlurr);
		}
		if(printAllInfo){
			System.out.println("TIME Afbeeldingen wegschrijven: " + (System.currentTimeMillis() - prev) + "ms");
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

	private Scalar findColorAtXY(double centerX, double centerY) {
		int color;	
		color=  buffered.getRGB((int)centerX,(int)centerY);
		return(new Scalar( ((color & 0x00ff0000) >> 16), ((color & 0x0000ff00) >> 8), (color & 0x000000ff)));
		//averageRed = (color & 0x00ff0000) >> 16;
		//averageGreen = (color & 0x0000ff00) >> 8;
		//averageBlue = color & 0x000000ff;
	}
	
	//BGR
	private Scalar findColorAtXYBGR(double centerX, double centerY) {
		int color;	
		color=  buffered.getRGB((int)centerX,(int)centerY);
		return(new Scalar( (color & 0x000000ff), ((color & 0x0000ff00) >> 8), ((color & 0x00ff0000) >> 16)));
		//averageRed = (color & 0x00ff0000) >> 16;
		//averageGreen = (color & 0x0000ff00) >> 8;
		//averageBlue = color & 0x000000ff;
	}
	
	/**
	 * Finds the average color around centerX and centerY from the bufferedimage.
	 */
	private Color findColorAtXY(double centerX, double centerY, int numberOfPixelsToInspect) {
		int color;
		int averageRed = 0;
		int averageGreen = 0;
		int averageBlue = 0;
		
		/*
		color=  buffered.getRGB((int)centerX,(int)centerY);

		
		averageRed = (color & 0x00ff0000) >> 16;
		averageGreen = (color & 0x0000ff00) >> 8;
		averageBlue = color & 0x000000ff;
		 */
		
		//******************************************************************************//
		//*****VOOR ALS DE GEMIDDELDE KLEUR VAN MEERDERE PIXELS GENOMEN MOET WORDEN*****// 
		//******************************************************************************//
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
		
		if(printAllInfo){
			System.out.println("Number of pixels inspected: " + numberOfPixelsInspected);
		}
		//******************************************************************************//
		//*****VOOR ALS DE GEMIDDELDE KLEUR VAN MEERDERE PIXELS GENOMEN MOET WORDEN*****// 
		//******************************************************************************//
		averageRed = averageRed/(numberOfPixelsInspected);
		averageGreen = averageGreen/(numberOfPixelsInspected);
		averageBlue = averageBlue/(numberOfPixelsInspected);
		 
		foundColorCodesRGB.add("RGB: [" + averageRed + ", " + averageGreen + ", "+ averageBlue + "]");
		//System.out.println("Averages (red, green, blue): " + averageRed + ", " + averageGreen + ", "+ averageBlue);
		//System.out.println(parse("rgb ("+averageRed+", "+averageGreen+", "+averageBlue+")"));
		return findColor(averageRed, averageGreen, averageBlue);
	}
	
	public Color parse(String input) 
	{
	    Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
	    Matcher m = c.matcher(input);

	    if (m.matches()) 
	    {
	        return new Color(Integer.valueOf(m.group(1)),  // red
	                         Integer.valueOf(m.group(2)),  // green
	                         Integer.valueOf(m.group(3))); // blue
	    }

	    return null;  
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
	
//	private Scalar detectColour(Mat srcMat, double x, double y) {
//		Scalar bgrPixel = new Scalar(0, 0, 0);
//		int cn = srcMat.channels();
//		byte[] temp = new byte[(int) (srcMat.total() * cn)];
//		srcMat.get(0, 0, temp);
//		int cols = srcMat.cols();
//		//		int rows = srcMat.rows();
////		double blauw = temp[(int) (y*cols*cn + x*cn + 0)]; //blauwwaarde (BGR, niet RGB in opencv!)
////		double groen = temp[(int) (y*cols*cn + x*cn + 1)]; //groenwaarde
////		double rood = temp[(int) (y*cols*cn + x*cn + 2)]; //roodwaarde
//						double[] bgr = srcMat.get((int) x, (int) y);
//
//		////				if (! (bgr[0] == 255 || bgr[1] == 255 | bgr[2] == 255)) {
//							bgrPixel = new Scalar(bgr[0], bgr[1], bgr[2]);
//		//				  System.out.println(bgrPixel);
//
////		bgrPixel = new Scalar(blauw, groen, rood);
//			
//		srcMat.put(0, 0, temp);
//		
//		//Scalar rgbPixel = new Scalar(bgr[0]%256, bgr[1]
//		
//		return bgrPixel;
//	}
//
//	private double calcDistance(Point p1, Point p2){
//		return calcDistance(p1.x, p2.x, p1.y, p2.y);
//	}
	
	private double calcDistance(double x1, double x2, double y1, double y2){
		return Math.sqrt( ( Math.pow((y1 - y2),2) + Math.pow((x1 - x2),2)) );
	}	
}
