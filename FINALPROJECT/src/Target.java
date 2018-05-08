import java.awt.Point;
import java.util.ArrayList;

//target class

public class Target {
	//instance variable
	private ArrayList<Point> points;
	
	//constructor
	public Target(Point startingPoint){
		
		//create an empty list for points
		points = new ArrayList<Point>();
		//add the starting point to the list
		points.add(startingPoint);
	}
	
	//addPoint method
	public void addPoint(Point aPoint){
		points.add(aPoint);
	}
	
	public int targetPosX(){
		return points.get(points.size()-1).x;
	}
	public int targetPosY(){
		return points.get(points.size()-1).y;
	}
}