import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

//ghost class

public class Ghost {
	// static constant
	private static String imageFolder = "images/";
	
	// instance variables
	private Image pic;
	private Point position;
	private String[] colors = {"RedGhost","BlueGhost","YellowGhost","PinkGhost"};
	private int random;
	private int randomX;
	private int randomY;
	
	// constructor
	public Ghost(int width, int height) {
		
		
		Random myRandom = new Random();
		//randomly selects a color ghost
		random = myRandom.nextInt(4);
		//randomly selects where on thier side they come out of
		randomX = myRandom.nextInt(width-100)+50;
		randomY = myRandom.nextInt(height-100)+50;
		
		// initial position
		//red ghost
		if(random == 0){
			//top of screen
			position = new Point(randomX, -50);
		}
		//blue ghost
		if(random == 1){
			//right of screen
			position = new Point(1200, randomY);
		}
		//yellow ghost
		if(random == 2){
			//bottom of screen
			position = new Point(randomX, 750);
		}
		//pink ghost
		if(random == 3){
			//left of screen
			position = new Point(-50, randomY);
		}
		
		// set the current animation to appropriate images
		setAnimation();
	}

	// setAnimation method
	private void setAnimation() {
		//load the image based on what randomly selected color ghost was picked
		try {
			pic = ImageIO.read(new File(imageFolder + colors[random]+".jpg"));
			//scale it 50,50
			pic = pic.getScaledInstance(50, 50, pic.SCALE_AREA_AVERAGING);
		} catch (IOException e) {
			System.out.println("Ghost image file not found");
			System.exit(0); // exit the program
		}

	}
	
	// draw method
	public void draw(Graphics page) {
		page.drawImage(pic,position.x, position.y, null);
	}


	// move method
	public void move(int pacmanx, int pacmany) {
		// update the position of ghost

		//always moves 10 toward pacman
		if(pacmanx>position.x){
			position.x+=10;
		}
		if(pacmany>position.y){
			position.y+=10;
		}
		if(pacmanx<position.x){
			position.x-=10;
		}
		if(pacmany<position.y){
			position.y-=10;
		}
	}
	
	public int ghostPosX(){
		return position.x;
	}
	public int ghostPosY(){
		return position.y;
	} 
}