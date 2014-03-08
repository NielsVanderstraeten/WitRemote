package Rooster;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TestGrid {

	
	@Test
	public void testGetPosition() {
		Grid myGrid = new Grid(4, 6);
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
		myPoints.add("CB");
		myPoints.add("CC");
		myPoints.add("CD");
		
		myPoints.add("DA");
		myPoints.add("DB");
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
		
		Vector vector1 = new Vector(20,10);
		Vector vector2 = new Vector(80,10);
		Vector vector3 = new Vector(50, 62);
//		Vector vector4 = new Vector(1200,688);
//		Vector vector5 = new Vector(0,688);
//		ArrayList<Integer> list1 = new ArrayList<Integer>();
//		ArrayList<Integer> list2 = new ArrayList<Integer>();
//		ArrayList<Integer> list3 = new ArrayList<Integer>();
		HashMap<String,Vector> myMap = new HashMap<String,Vector>();
		myMap.put("CB", vector2);
		myMap.put("CC", vector3);
		myMap.put("DB", vector1);
//		myMap.put("CD", vector4);
//		myMap.put("CA", vector5);
		ArrayList<Integer> result = myGrid.getPoints(myMap);
		System.out.println("de gevonden punten op het rooster zijn:");
		for (int i = 0; i< result.size(); i++) {
			System.out.println(result.get(i));
		}
		Vector myVector = myGrid.getPosition(myMap);
		System.out.println(myVector.getX() + ", " +  myVector.getY());
		double myAngle = myGrid.getRotation(myMap);
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

}
