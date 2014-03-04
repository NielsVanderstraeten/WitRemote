

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class TestGrid {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
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
		
		ArrayList<Integer> list1 = new ArrayList<Integer>();
		ArrayList<Integer> list2 = new ArrayList<Integer>();
		ArrayList<Integer> list3 = new ArrayList<Integer>();
		HashMap<String,ArrayList<Integer>> myMap = new HashMap<String,ArrayList<Integer>>();
		myMap.put("BB", list1);
		myMap.put("CC", list2);
		myMap.put("CB", list3);
		ArrayList<Integer> result = myGrid.getPoints(myMap);
		System.out.println("de gevonden punten op het rooster zijn:");
		for (int i = 0; i< result.size(); i++) {
			System.out.println(result.get(i));
		}
		Vector myVector = myGrid.getPosition(myMap);
		System.out.println(myVector.getX() + myVector.getY());
	}

}
