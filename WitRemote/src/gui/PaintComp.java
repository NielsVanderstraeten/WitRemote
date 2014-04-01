package gui;

import java.awt.Color;
import java.awt.geom.Area;

public class PaintComp {

	public PaintComp(Area area, Color color, int position){
		this.area = area;
		this.color = color;
		this.position = position;
	}
	
	private Area area;
	private Color color;
	private int position;
	
	public Area getArea() {
		return area;
	}
	public Color getColor() {
		return color;
	}
	public int getPosition(){
		return position;
	}
	
	public void setArea(Area area){
		this.area=area;
	}
}
