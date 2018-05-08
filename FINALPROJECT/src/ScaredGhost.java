import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

//scared ghost class

public class ScaredGhost {
	// static constant
	private static String imageFolder = "images/";
	
	// instance variables
	private Image pic;
	private Point position;
	
	// constructor
	public ScaredGhost(int x, int y) {
		
		position = new Point(x, y);
		
		// set the current animation to appropriate images
		setAnimation();
	}

	// setAnimation method
	private void setAnimation() {
		// load the image on the scared ghost
		try {
			pic = ImageIO.read(new File(imageFolder + "scaredGhost.jpg"));
			//scale image 55,55
			pic = pic.getScaledInstance(55, 55, pic.SCALE_AREA_AVERAGING);
		} catch (IOException e) {
			System.out.println("Scared Ghost image file not found");
			System.exit(0); // exit the program
		}

	}
	
	// draw method
	public void draw(Graphics page) {
		page.drawImage(pic,position.x, position.y, null);
	}

	// move method
	public void move(int width, int height) {
		
		// move the scared ghost off the screen
		if(position.x>=width/2){
			position.x+=20;
		}
		if(position.x<width/2){
			position.x-=20;
		}
		if(position.y>=height/2){
			position.y+=20;
		}
		if(position.y<height/2){
			position.y-=20;
		}
	}
	
	public int sGhostPosX(){
		return position.x;
	}
	public int sGhostPosY(){
		return position.y;
	} 
}