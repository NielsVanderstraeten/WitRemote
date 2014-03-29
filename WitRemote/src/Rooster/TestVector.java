package Rooster;

//import static org.junit.Assert.*;

import org.junit.Test;

public class TestVector {

	
	@Test
	public void TestGetAngle() {
		Vector vector1 = new Vector (0,0);
		Vector vector = new Vector(Math.sqrt(9),-Math.sqrt(3));
		double deltaX = (vector.getX() - vector1.getX());
		double deltaY = (vector.getY() - vector1.getY());
		double slope = deltaY/deltaX;
		double deviation;
		
		if (deltaX == 0) {
			if (deltaY >= 0) {
				deviation = Math.PI/2;
			} else {
				deviation = 3*Math.PI/2;
			}
		}
		else if (deltaX > 0) {
			//deviation = Math.PI/2 - Math.atan(slope);
			deviation = Math.atan(slope);
		}
		else {
			//deviation = -(Math.PI/2 + Math.atan(slope));
			deviation = Math.atan(slope) + Math.PI;
		}
		deviation = deviation/Math.PI*180 + 360;
		System.out.println(deviation%360);
	}

}
