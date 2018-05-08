import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

//pacman class

public class PacMan {
	// static constant
	private static String imageFolder = "images/";
	
	//two images (open and close)
	private static final int IMAGES = 2;
	
	private static final int RIGHT = 0;
	private static final int UP = 1;
	private static final int LEFT = 2;
	private static final int DOWN = 3;
	
	private static final String[] directions = {"right", "up", "left", "down"};
	
	// instance variables
	private Image[] currentAnimation;
	private int currentFrame;
	private Point position;
	private int direction;
	
	// constructor
	public PacMan(int width, int height) {
		// puts pacman in the middle of the screen
		position = new Point(width/2-25, height/2);
		
		// create an array for current animation
		currentAnimation = new Image[IMAGES];
		
		// set the initial direction to right
		direction = RIGHT;
		
		// set the current animation to appropriate images
		setAnimation();
	}
	
	// setAnimation method
	private void setAnimation() {
		// get the appropriate prefix for the direction the player
		// is moving
		String prefix = directions[direction];

		// load all the images
		for (int index = 0; index < IMAGES; index++) {
			try {
				currentAnimation[index] = ImageIO.read(new File(imageFolder + prefix + index + ".jpg"));
				//scale the image to 50,50
				currentAnimation[index] = currentAnimation[index].getScaledInstance(50, 50, currentAnimation[index].SCALE_AREA_AVERAGING);
			} catch (IOException e) {
				System.out.println("Image file not found");
				System.exit(0); // exit the program
			}
		}
		// set the current frame to 0
		currentFrame = 0;
	}

	// draw method
	public void draw(Graphics page) {
		page.drawImage(currentAnimation[currentFrame],position.x, position.y, null);
	}

	// nextFrame method
	public void nextFrame() {
		currentFrame = (currentFrame + 1) % IMAGES;
	}

	// move method
	public void move(int dx, int dy, int width, int height) {

		
		//doesn't allow pacman to move off the screen, stops at all borders
		if(position.y<height && position.y>0){
			position.y += dy;
		}
		if(position.x<width-50 && position.x>0){
			position.x += dx;
		}
		if(position.y>=height && dy<0){
			position.y += dy;
		}
		if(position.y<=0 && dy>0){
			position.y += dy;
		}
		if(position.x>=width-50 && dx<0){
			position.x += dx;
		}
		if(position.x<=0 && dx>0){
			position.x += dx;
		}

		// update the animation if necessary
		if (dy > 0 && direction != DOWN){ 
			direction = DOWN;
			setAnimation();
		}
		if (dy < 0 && direction != UP){ 
			direction = UP;
			setAnimation();
		}
		if (dx > 0 && direction != RIGHT){
			direction = RIGHT;
			setAnimation();
		}
		if (dx < 0 && direction != LEFT){
			direction = LEFT;
			setAnimation();
		}
	}
	public int pacmanPosX(){
		return position.x;
	}
	public int pacmanPosY(){
		return position.y;
	}

}