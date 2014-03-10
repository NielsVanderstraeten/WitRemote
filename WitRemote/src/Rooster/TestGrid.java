package Rooster;

import static org.junit.Assert.*;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TestGrid {

	
	@Test
	public void testGetPosition() {
		Grid myGrid = new Grid(4, 6, "blabla");
		ArrayList<String> myPoints = new ArrayList<String>();
		myPoints.add("BB");
		myPoints.add("AB");
		myPoints.add("AC");
		myPoints.add("AD");
		myPoints.add("BA");
		myPoints.add("BB");
		myPoints.add("BC");
		myPoints.add("BD");
		myPoints.add("CA");
		myPoints.add("VV");
		myPoints.add("CC");
		myPoints.add("CD");
		
		myPoints.add("DA");
		myPoints.add("VW");
		myPoints.add("DC");
		myPoints.add("DD");
		myPoints.add("EA");
		myPoints.add("BB");
		myPoints.add("EC");
		myPoints.add("ED");
		myPoints.add("FA");
		myPoints.add("FB");
		myPoints.add("CC");
		myPoints.add("FD");
		
		myGrid.setMap(myPoints);
		
		
//		Vector vector1 = new Vector(20,10);
//		Vector vector2 = new Vector(80,10);
//		Vector vector3 = new Vector(50, 62);
//		Vector vector4 = new Vector(1200,688);
//		Vector vector5 = new Vector(0,688);
//		ArrayList<Integer> list1 = new ArrayList<Integer>();
//		ArrayList<Integer> list2 = new ArrayList<Integer>();
//		ArrayList<Integer> list3 = new ArrayList<Integer>();
//		HashMap<String,Vector> myMap = new HashMap<String,Vector>();
//		myMap.put("CB", vector2);
//		myMap.put("CC", vector3);
//		myMap.put("DB", vector1);
//		myMap.put("CD", vector4);
//		myMap.put("CA", vector5);
		ArrayList<Shape> myList = new ArrayList<Shape>();
		myList.add(new Shape(20,10,"VW"));
		myList.add(new Shape(80,10,"VV"));
		myList.add(new Shape(50,62,"CC"));
		ArrayList<Integer> result = myGrid.getPoints(myList);
		
		System.out.println("de gevonden punten op het rooster zijn:");
		for (int i = 0; i< result.size(); i++) {
			System.out.println(result.get(i));
		}
		Vector myVector = myGrid.getPosition(myList);
		System.out.println(myVector.getX() + ", " +  myVector.getY());
		double myAngle = myGrid.getRotation(myList);
		System.out.println("de rotatie = " + myAngle);
	}
	
	@Test
	public void TestCombination() {
		int z = 5;
		for (int i=0;i<z-2;i++) {
			for (int j = (i+1); j<z-1;j++) {
				for(int k = (j+1); k<z;k++) {
					//System.out.println(i + "," + j + "," + k);
				}
			}
		}
	}
	
	@Test
	public void TestGetAngle() {
		Vector vector1 = new Vector (0,0);
		Vector vector = new Vector(3,Math.sqrt(3));
		double deltaX = (vector.getX() - vector1.getX());
		double deltaY = (vector.getY() - vector1.getY());
		double slope = deltaX/deltaY;
		double deviation;
		
		if (deltaX > 0) {
			deviation = Math.PI/2 - Math.atan(slope);
		}
		else {
			deviation = -(Math.PI/2 + Math.atan(slope));
		}
		deviation = deviation/Math.PI*180;
		System.out.println(deviation);
	}
	
	//Gebruikt OpenCSV: http://opencsv.sourceforge.net/
	@Test
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
				int width = myEntries.size();
				int height = myEntries.get(0).length;
				ArrayList<String> myMap = new ArrayList<String>();
				for(String[] array: myEntries){
					for(String something: array){
						myMap.add(something);
						System.out.print(something + " ");
					}
					System.out.println("");
				}
				System.out.println(width);
				System.out.println(height);
//				for (String string: myMap) {
//					System.out.println(string);
//				}

			}				
		}

}
