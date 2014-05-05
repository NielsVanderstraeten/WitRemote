package Rooster;

//import static org.junit.Assert.*;

import org.junit.Test;

//import au.com.bytecode.opencsv.CSVReader;
//
//import java.io.FileReader;
import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;

public class TestGrid {

	
//	@Test
//	public void testGetPosition() {
//		Grid myGrid = new Grid(4, 6, "blabla");
//		ArrayList<String> myPoints = new ArrayList<String>();
//		myPoints.add("BB");
//		myPoints.add("AB");
//		myPoints.add("AC");
//		myPoints.add("AD");
//		myPoints.add("BA");
//		myPoints.add("BB");
//		myPoints.add("BC");
//		myPoints.add("BD");
//		myPoints.add("CA");
//		myPoints.add("VV");
//		myPoints.add("CC");
//		myPoints.add("CD");
//		
//		myPoints.add("DA");
//		myPoints.add("VW");
//		myPoints.add("DC");
//		myPoints.add("DD");
//		myPoints.add("EA");
//		myPoints.add("BB");
//		myPoints.add("EC");
//		myPoints.add("ED");
//		myPoints.add("FA");
//		myPoints.add("FB");
//		myPoints.add("CC");
//		myPoints.add("FD");
//		
//		myGrid.setMap(myPoints);
//		
//		
////		Vector vector1 = new Vector(20,10);
////		Vector vector2 = new Vector(80,10);
////		Vector vector3 = new Vector(50, 62);
////		Vector vector4 = new Vector(1200,688);
////		Vector vector5 = new Vector(0,688);
////		ArrayList<Integer> list1 = new ArrayList<Integer>();
////		ArrayList<Integer> list2 = new ArrayList<Integer>();
////		ArrayList<Integer> list3 = new ArrayList<Integer>();
////		HashMap<String,Vector> myMap = new HashMap<String,Vector>();
////		myMap.put("CB", vector2);
////		myMap.put("CC", vector3);
////		myMap.put("DB", vector1);
////		myMap.put("CD", vector4);
////		myMap.put("CA", vector5);
//		ArrayList<Shape> myList = new ArrayList<Shape>();
//		myList.add(new Shape(20,10,"VW"));
//		myList.add(new Shape(80,10,"VV"));
//		myList.add(new Shape(50,62,"CC"));
//		ArrayList<Integer> result = myGrid.getPoints(myList);
//		
//		System.out.println("de gevonden punten op het rooster zijn:");
//		for (int i = 0; i< result.size(); i++) {
//			System.out.println(result.get(i));
//		}
//		Vector myVector = myGrid.getPosition(myList);
//		System.out.println(myVector.getX() + ", " +  myVector.getY());
//		double myAngle = myGrid.getRotation(myList);
//		System.out.println("de rotatie = " + myAngle);
//	}
//	@Test
//	public void testParse() {
//		Grid test = new Grid("haha");
//	}
//	@Test
//	public void TestCombination() {
//		int z = 5;
//		for (int i=0;i<z-2;i++) {
//			for (int j = (i+1); j<z-1;j++) {
//				for(int k = (j+1); k<z;k++) {
//					//System.out.println(i + "," + j + "," + k);
//				}
//			}
//		}
//	}
	
//	@Test
//	public void TestGetAngle() {
//		Vector vector1 = new Vector (0,0);
//		Vector vector = new Vector(3,Math.sqrt(3));
//		double deltaX = (vector.getX() - vector1.getX());
//		double deltaY = (vector.getY() - vector1.getY());
//		double slope = deltaX/deltaY;
//		double deviation;
//		
//		if (deltaX > 0) {
//			deviation = Math.PI/2 - Math.atan(slope);
//		}
//		else {
//			deviation = -(Math.PI/2 + Math.atan(slope));
//		}
//		deviation = deviation/Math.PI*180;
//		System.out.println(deviation);
//	}
	
	//Gebruikt OpenCSV: http://opencsv.sourceforge.net/
//	@Test
//		public void parseCSV() {
//			CSVReader reader;	
//			java.util.List<String[]> myEntries = null;
//			try {
//				reader = new CSVReader(new FileReader("src/gui/resources/map.csv"), ',');
//				myEntries = (java.util.List<String[]>) reader.readAll();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if(myEntries != null){
//				int height = myEntries.size();
//				int width = myEntries.get(0).length;
//				ArrayList<String> myMap = new ArrayList<String>();
//				for(String[] array: myEntries){
//					for(String something: array){
//						myMap.add(something);
//						System.out.print(something + " ");
//					}
//					System.out.println("");
//				}
//				System.out.println(width);
//				System.out.println(height);
////				for (String string: myMap) {
////					System.out.println(string);
////				}
//
//			}				
//		}
	
	@Test
	public void completeGrid() {
//		System.out.println("in comleteGrid()");
		Grid grid = new Grid("blablabla");
//		ArrayList<Shape> myList = new ArrayList<Shape>();
//		
		
		System.out.println("Test1: ");
		System.out.println("-----------------");
		System.out.println("");
		ArrayList<Shape> myList = new ArrayList<Shape>();
		myList.add(new Shape(369,241,"WH"));
		myList.add(new Shape(461,506,"YC"));
		myList.add(new Shape(183,454,"BR"));
		myList.add(new Shape( 65,418,"RU"));
		myList.add(new Shape(641,288,"RR"));
		myList.add(new Shape(213, 62,"RU"));
		
//		
		
//		System.out.println("-------------------------------");
		long start = System.currentTimeMillis();
		Vector uitkomst = grid.getPositionNew(myList);
		System.out.println("Running time: " +  (System.currentTimeMillis() - start) + "ms");
		System.out.println(uitkomst.toString());
		System.out.println("rotation:");
		System.out.println(grid.getRotationNew(myList)*180/Math.PI);
		System.out.println("foundFigures:");
		ArrayList<Integer> figures = grid.getLastFigures();
		for (Integer i: figures) {
			System.out.println(i);
			System.out.println(grid.getPositionCalculator().getVector(i).toString());
		}
		
		
		System.out.println("Test2: ");
		System.out.println("-----------------");
		System.out.println("");
		myList = new ArrayList<Shape>();
		myList.add(new Shape(348,241,"WR"));
		myList.add(new Shape(418,477,"RC"));
		myList.add(new Shape(177,420,"BH"));
//		myList.add(new Shape( 65,418,"RU"));
		myList.add(new Shape(586,293,"RR"));
//		myList.add(new Shape(213, 62,"RU"));
		
		uitkomst = grid.getPositionNew(myList);
		System.out.println(uitkomst.toString());
		System.out.println("rotation:");
		System.out.println(grid.getRotationNew(myList)*180/Math.PI);
		System.out.println("foundFigures:");
		figures = grid.getLastFigures();
		for (Integer i: figures) {
			System.out.println(i);
			System.out.println(grid.getPositionCalculator().getVector(i).toString());
		}
		
		System.out.println("Test3: ");
		System.out.println("-----------------");
		System.out.println("");
		myList = new ArrayList<Shape>();
		myList.add(new Shape(661,301,"WR"));
		myList.add(new Shape(268,289,"WH"));
		myList.add(new Shape(349,477,"RC"));
		myList.add(new Shape(666,304,"WS"));
		myList.add(new Shape(476,300,"RH"));
		myList.add(new Shape(574,135,"BC"));
//		myList.add(new Shape(213, 62,"RU"));
		
		uitkomst = grid.getPositionNew(myList);
		System.out.println(uitkomst.toString());
		System.out.println("rotation:");
		System.out.println(grid.getRotationNew(myList)*180/Math.PI);
		System.out.println("foundFigures:");
		figures = grid.getLastFigures();
		for (Integer i: figures) {
			System.out.println(i);
			System.out.println(grid.getPositionCalculator().getVector(i).toString());
		}
		
		System.out.println("Test4: ");
		System.out.println("-----------------");
		System.out.println("");
		myList = new ArrayList<Shape>();
		myList.add(new Shape(602, 334,"WS"));
		myList.add(new Shape(135, 492,"BC"));
		myList.add(new Shape(326, 278,"YC"));
		myList.add(new Shape(49, 202,"BR"));
		myList.add(new Shape(539, 67,"RR"));
//		myList.add(new Shape(574,135,"BC"));
//		myList.add(new Shape(213, 62,"RU"));
		
		uitkomst = grid.getPositionNew(myList);
		System.out.println(uitkomst.toString());
		System.out.println("rotation:");
		System.out.println(grid.getRotationNew(myList)*180/Math.PI);
		System.out.println("foundFigures:");
		figures = grid.getLastFigures();
		for (Integer i: figures) {
			System.out.println(i);
			System.out.println(grid.getPositionCalculator().getVector(i).toString());
		}
//		ArrayList<Integer> result = grid.getPoints(myList);
//		System.out.println("de gevonden punten op het rooster zijn:");
//		System.out.println("de size = " + result.size());
//		for (int i = 0; i< result.size(); i++) {
//			System.out.println(result.get(i));
//		}		
//		Vector vector = grid.getPosition(myList);
//		double rot = grid.getRotation(myList);
//		System.out.println(vector.getX());
//		System.out.println(vector.getY());
//		System.out.println(rot);
//		
//		ArrayList<Integer> points = grid.getLastTriangle(); 
//		for (int i: points) {
//			System.out.println(i);
//		}
//		System.out.println(grid.getPositionCalculator().getVector(11).toString());
//		System.out.println(grid.getPositionCalculator().getVector(12).toString());
//		System.out.println(grid.getPositionCalculator().getVector(18).toString());
//		System.out.println(grid.getPositionCalculator().getVector(19).toString());
//		System.out.println(grid.getPositionCalculator().getVector(20).toString());
//		myList.add(new Shape(377,381,"GH"));
//		myList.add(new Shape(435,381,"YR"));
//		myList.add(new Shape(495,381,"RR"));
//		myList.add(new Shape(405,330,"YS"));
//		myList.add(new Shape(465,330,"WR"));
		
//		myList.add(new Shape(331,236,"RH"));
//		myList.add(new Shape(393,236,"BR"));
//		myList.add(new Shape(363,183,"GR"));
//		myList.add(new Shape(456,236,"BC"));
//		myList.add(new Shape(425,183,"BH"));
//		myList.add(new Shape(394,129,"YS"));
		
//		myList.add(new Shape(446,505,"YC"));
//		myList.add(new Shape(101,424,"BR"));
//		myList.add(new Shape(683,243,"RR"));
	}
	
//	@Test
//	public void VectorTest() {
//		System.out.println("vector angle test");
//		Vector vec = new Vector(0,0);
//		Vector test = new Vector(1,1);
//		Vector test2 = new Vector(1,-1);
//		System.out.println(vec.getAngle(test));
//		System.out.println(vec.getAngle(test2));
//		System.out.println(test.getAngle(vec));
//		System.out.println(test2.getAngle(vec));
//	}
//	

}
