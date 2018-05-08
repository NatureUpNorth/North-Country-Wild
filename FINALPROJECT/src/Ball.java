import java.awt.Color;
import java.awt.Graphics;

//ball class 

public class Ball {
	
	//instance variables
	private int StartPosX;
	private int StartPosY;
	private int TargetPosX;
	private int TargetPosY;
	private double disX;
	private double disY;
	
	//constructor
	public Ball(int startx, int starty, int targetx, int targety){
		
		//location of the pacman
		//where the ball starts from
		StartPosX = startx;
		StartPosY = starty;
		
		//location of the click
		//where the ball goes towards
		TargetPosX = targetx;
		TargetPosY = targety;
		
		//finds the x distance between the pacman and the target
		disX = StartPosX-TargetPosX;
		//finds the y distance between the pacman and the target
		disY = StartPosY-TargetPosY;

	}
	public void draw(Graphics page){
		//white circle for the dot
		page.setColor(Color.WHITE);
		page.fillOval(StartPosX+25,StartPosY+25,10,10);

	}

	public int ballPosX(){
		return StartPosX;
	}
	public int ballPosY(){
		return StartPosY;
	} 

	//moves the ball towards click by subtracting the distance from pacman to the click 
	//and dividing by 12 to make it go smoothly
	//add 2 to as correction to make it the most accurate
	public void update() {
		
		/*System.out.println("Start: ("+StartPosX+","+StartPosY+")");
		System.out.println("Target: ("+TargetPosX+","+TargetPosY+")");
		System.out.println("Distance: ("+disX+","+disY+")");
		
		double x = disX/12+2;
		double y = disY/12+2;
		System.out.println("Start2.0 = StartPosX("+StartPosX+") - disX/12+2 ("+x+")");
		System.out.println("Start2.0 = StartPosY("+StartPosY+") - disY/12+2 ("+y+")");
		
		if(disX<100 && disY<100 ){
			StartPosX-=disX/4+2;
			StartPosY-=disY/4+2;
		}else{
		*/
			StartPosX-=disX/12+2;
			StartPosY-=disY/12+2;
		//}
		//System.out.println("Start2.0: ("+StartPosX+","+StartPosY+")");

	}


}
