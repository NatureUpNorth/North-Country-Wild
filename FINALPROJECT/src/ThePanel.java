import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;

//Remi LeBlanc
// 4/20/17 - 5/10/17
//CS219 final project

//Pac-Man game where pacman gets to fight back against the ghosts
//the dots he collected in the original game he now gets to shoot at the ghosts and kill them
//ghosts still chase pacman and try to get him


public class ThePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
	// instance variables
	
	JButton startGame, howToPlay, mainMenu, playAgain;
	
	JTextArea score, instructions;
	JLabel gameOver, finalScore;
	
	private Timer timer;
	private Timer ghostTimer;
	private Point mousePosition;
	private int windowWidth;
	private int windowHeight;
	private int points = 0;
	private double ghostTime = 5000;
	private boolean pause;
	private Target target;
	private ArrayList<PacMan> pacmans;
	private ArrayList<Ball> balls;
	private ArrayList<Ghost> ghosts;
	private ArrayList<ScaredGhost> scaredGhosts;
	private JComboBox<String> comboBox;
	private static String[] stringColors = {"Black", "Red", "Green", "Blue", "Erase"};

	// constructor
	public ThePanel(int width, int height) {
		this.setLayout(null);
		
		comboBox = new JComboBox<>(stringColors);
		comboBox.setSelectedIndex(0);
		comboBox.addActionListener(this);
		comboBox.setVisible(true);
		this.add(comboBox);
		
		
		startGame = new JButton("Start");
		startGame.setLocation(500, 327);
		startGame.setSize(200,50);
		startGame.addActionListener(this);
		this.add(startGame);
		
		howToPlay = new JButton("How To Play");
		howToPlay.setLocation(500, 371);
		howToPlay.setSize(200,50);
		howToPlay.addActionListener(this);
		this.add(howToPlay);
		
		mainMenu = new JButton("Return to Main Menu");
		mainMenu.setLocation(510, 400);
		mainMenu.setSize(189,30);
		mainMenu.addActionListener(this);
		this.add(mainMenu);
		mainMenu.setVisible(false);
		
		playAgain = new JButton("Play Again");
		playAgain.setLocation(497, 600);
		playAgain.setSize(200, 50);
		playAgain.addActionListener(this);
		this.add(playAgain);
		playAgain.setVisible(false);
		
		score = new JTextArea("  Score:  "+points); // counts down
		score.setFont(new Font("Plain", Font.BOLD, 12));
		score.setEditable(false);
		score.setLocation(10, 10);
		score.setSize(80, 16);
		this.add(score);
		score.setVisible(false);
		
		instructions = new JTextArea(" HOW TO PLAY ");
		instructions.setFont(new Font("Plain", Font.BOLD, 12));
		instructions.setEditable(false);
		instructions.setLocation(517, 250);
		instructions.setSize(175, 151);
		this.add(instructions);
		instructions.setVisible(false);
		howTo();
		
		gameOver = new JLabel("Game Over");
		gameOver.setFont(new Font("Courier", Font.BOLD, 218));
		gameOver.setForeground(Color.WHITE);
		gameOver.setLocation(10, 100);
		gameOver.setSize(1200, 500);
		this.add(gameOver);
		gameOver.setVisible(false);
		
		finalScore = new JLabel("Score: "+points);
		finalScore.setFont(new Font("Courier", Font.BOLD, 50));
		finalScore.setForeground(Color.WHITE);
		finalScore.setLocation(470, 400);
		finalScore.setSize(500, 200);
		this.add(finalScore);
		finalScore.setVisible(false);
		
		pacmans = new ArrayList<PacMan>();
		balls = new ArrayList<Ball>();
		ghosts = new ArrayList<Ghost>();
		scaredGhosts = new ArrayList<ScaredGhost>();
		
		windowHeight = height;
		windowWidth = width;
		
		// create a new pacman
		PacMan pacman = new PacMan(windowWidth, windowHeight);
		//add to arraylist (it will always be only one pacman long)
		pacmans.add(pacman);
		
		timer = new Timer(200, this);
	
		ghostTimer = new Timer((int)ghostTime, this);
		
		pause = true;
		
		//create a default position
		mousePosition = new Point(windowWidth/2, windowHeight/2);
		
		// change the size of this panel
		super.setPreferredSize(new Dimension(1200, 750));
		super.setBackground(Color.BLACK);
		
		// make myself responsible for key events
		super.addKeyListener(this);
		super.setFocusable(true);
		
		///make myself the responder the mouse events
		super.addMouseListener(this);
		super.addMouseMotionListener(this);
		
		//hit space bar to pause the game
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "Space");
		getActionMap().put("Space", new AbstractAction(){ // enter key
			public void actionPerformed(ActionEvent evt) {
				if(pause){
					//to resume the game
					pause = false;
					timer.start();
					ghostTimer.start();
				}else{
					//to pause the game
					pause = true;
					timer.stop();
					ghostTimer.stop();
				}
			}
		});
	}
	
	public void howTo(){//instructions, called when howToPlay button is clicked

		instructions.setText("         -HOW TO PLAY-\n"
							+ " Kill as many ghosts as you \n"
							+ " can before they get you!\n\n"
							+ " \"W\" - move up \n"
							+ " \"A\" - move left \n"
							+ " \"S\" - move down \n"
							+ " \"D\" - move right \n"
							+ " mouse - aim/shoot \n"
							+ " space bar - pause/resume ");
		
	}

	// override the paintComponent method
	public void paintComponent(Graphics page) {
		super.paintComponent(page);
		//draw pacman
		pacmans.get(0).draw(page);

		//draw each ghost in the list
		for(Ghost aGhost: ghosts){
			aGhost.draw(page);
		}
		//draw each ball in the list
		for(Ball aBall: balls){
			aBall.draw(page);
		}
		//draw each scared ghost in the list
		for(ScaredGhost aScaredGhost: scaredGhosts){
			aScaredGhost.draw(page);
		}
		//target that follows mouse color
		page.setColor(Color.WHITE);
		//dot in the center
		page.fillOval(mousePosition.x-5, mousePosition.y-5, 10, 10);
		//circle around the dot
		page.drawOval(mousePosition.x-25, mousePosition.y-25, 50, 50);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		if(event.getSource() == comboBox){
			String selected = (String) comboBox.getSelectedItem();
			System.out.println(selected);
		}
		
		
		//timer goes off every 5000 to start, then ghostTime decreases
		if (event.getSource() == ghostTimer) {
			//max 12 ghosts on screen at a time
			if(ghosts.size()<12){
				//make a new ghost
				Ghost aGhost = new Ghost(windowWidth, windowHeight);
				//add to list
				ghosts.add(aGhost);
			}
			//stop timer
			ghostTimer.stop();
			
			//decrease the amount of time between each event by 10%
			if(ghostTime>600){
				ghostTime = ghostTime*0.9;
			}else{// does get below 600
				ghostTime = 600;
			}
			
			//remake the timer with the smaller time
			ghostTimer = new Timer((int)ghostTime, this);
			ghostTimer.start();
		}
		if (event.getSource() == timer) {
			// animate pacman (open and close)
			pacmans.get(0).nextFrame();
			
			//always moves ghosts toward pacman
			for(Ghost aGhost: ghosts){
				aGhost.move(pacmans.get(0).pacmanPosX(), pacmans.get(0).pacmanPosY());
			}
			//always moves ghosts off the screen
			for(ScaredGhost sGhost: scaredGhosts){
				sGhost.move(windowWidth, windowHeight);
			}
			//deletes scaredGhosts after 25 (theyre off the screen at this point)
			if(scaredGhosts.size()>25){
				scaredGhosts.remove(0);
			}
			
			for(int ballIndex = 0; ballIndex<balls.size(); ballIndex++){
				//moves the ball
				balls.get(ballIndex).update();
				for(int ghostIndex = 0; ghostIndex<ghosts.size(); ghostIndex++){
				
					//check if the ball hits the ghosts
					if(balls.get(ballIndex).ballPosY()>=ghosts.get(ghostIndex).ghostPosY()-25 
							&& balls.get(ballIndex).ballPosY()<=ghosts.get(ghostIndex).ghostPosY()+25
							&& balls.get(ballIndex).ballPosX()>=ghosts.get(ghostIndex).ghostPosX()-25
							&& balls.get(ballIndex).ballPosX()<=ghosts.get(ghostIndex).ghostPosX()+25){
						
						//create a new scaredGhost
						ScaredGhost aScaredGhost = new ScaredGhost(ghosts.get(ghostIndex).ghostPosX(), ghosts.get(ghostIndex).ghostPosY());	
						//add to list
						scaredGhosts.add(aScaredGhost);
						
						//increases score by 10 for each ghost killed
						points += 10;
						//update final score display (not visible right now)
						finalScore.setText("Score: "+points);
						//update score that is shown in top left corner
						score.setText("  Score:  "+points);
						//make the text area larger if points is 1000+ so all the numbers fit
						if(points>= 1000){
							score.setSize(90,16);
						}
						//remove the ghost that was hit from the list, and therefore the screen
						ghosts.remove(ghostIndex);
						//remove the ball that was hit from the list, and therefore the screen
						balls.remove(ballIndex);
						break;
					}
				}
			}

			for(int ghostIndex = 0; ghostIndex<ghosts.size(); ghostIndex++){
				//check if a ghost has hit pacman
				if(ghosts.get(ghostIndex).ghostPosY()<=pacmans.get(0).pacmanPosY()+49
						&& ghosts.get(ghostIndex).ghostPosY()+49>=pacmans.get(0).pacmanPosY()
						&& ghosts.get(ghostIndex).ghostPosX()<=pacmans.get(0).pacmanPosX()+49
						&& ghosts.get(ghostIndex).ghostPosX()+49>=pacmans.get(0).pacmanPosX()){
					
					//stop all animation
					timer.stop();
					//stop ghosts from coming out
					ghostTimer.stop();
					//essentially pause the game
					pause = true;
					//remove balls from the screen
					balls.clear();
					//shown play again button
					playAgain.setVisible(true);
					//shown game over words
					gameOver.setVisible(true);
					//show their final score
					finalScore.setVisible(true);
					
				}
			}
			// repaint everything
			super.repaint();
		}
		//start button
		if(event.getSource() == startGame){
			//make a new pacman
			PacMan pacman = new PacMan(windowWidth, windowHeight);
			//add to list
			pacmans.add(pacman);
			//make a new ghost immediately so player doesn't to wait for timer to go off
			Ghost aGhost = new Ghost(windowWidth, windowHeight);
			//add to list
			ghosts.add(aGhost);
			//start the ghosts coming out every 5 seconds
			ghostTime = 5000;
			//resets the timer
			timer.restart();
			//remake the timer at the ghostTime amount (5000 right now)
			ghostTimer = new Timer((int)ghostTime, this);
			//start it
			ghostTimer.start();
			pause = false;
			//make start button disappear
			startGame.setVisible(false);
			//make how to play button disappear
			howToPlay.setVisible(false);
			//show the score for the player in the top left corner
			score.setVisible(true);
		}
		//how to play button
		if(event.getSource() == howToPlay){
			//makes start game button disappear
			startGame.setVisible(false);
			//make how to play button disappear
			howToPlay.setVisible(false);
			//show the instructions
			instructions.setVisible(true);
			//make button that returns to main menu visible
			mainMenu.setVisible(true);
		}
		//return to main menu button
		if(event.getSource() == mainMenu){
			//resets everything back to main menu screen
			startGame.setVisible(true);
			howToPlay.setVisible(true);
			instructions.setVisible(false);
			mainMenu.setVisible(false);
		}
		//play again button after pacman dies
		if(event.getSource() == playAgain){
			//resets everything back to main menu screen
			startGame.setVisible(true);
			howToPlay.setVisible(true);
			playAgain.setVisible(false);
			gameOver.setVisible(false);
			finalScore.setVisible(false);
			//resets points
			points = 0;
			//resets score displays
			score.setText("  Score:  "+points);
			finalScore.setText("Score: "+points);
			score.setSize(80,16);
			//clear all ghosts off the screen
			ghosts.clear();
			//clear all scared ghosts 
			scaredGhosts.clear();
			//remove pacman
			pacmans.remove(0);
		}	
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		//if game is still player
		if(pause==false){
			//delete ball if they go off the screen
			for(int ballIndex = 0; ballIndex<balls.size(); ballIndex++){
				if(balls.get(ballIndex).ballPosX()<0 || balls.get(ballIndex).ballPosX()>windowWidth
						|| balls.get(ballIndex).ballPosY()<0 || balls.get(ballIndex).ballPosY()>windowHeight){
					balls.remove(ballIndex);
				}
			}
			//see if its a right click
			//only allow 15 balls on the screen at a time
			if(event.getButton() == MouseEvent.BUTTON1 && balls.size() < 15){

				//makes a new target for ball to go after
				target = new Target(event.getPoint());

				//create a ball
				//starts ball at pacman and go toward the target
				Ball aBall = new Ball(pacmans.get(0).pacmanPosX(),pacmans.get(0).pacmanPosY(),
						target.targetPosX(),target.targetPosY());

				balls.add(aBall);

				//redraw the panel
				super.repaint();
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		//update my mouse position based on the position of the event
		mousePosition = event.getPoint();
		
		//force the repainting of the panel
		super.repaint();
	}

	@Override
	public void keyReleased(KeyEvent event) {
		//only can move pacman when the game is in progress
		if(pause==false){
			switch (event.getKeyChar()) {
			//move up
			case 'w':
				pacmans.get(0).move(0, -25, windowWidth, windowHeight);
				break;
			//move down
			case 's':
				pacmans.get(0).move(0, 25, windowWidth, windowHeight);
				break;
			//move left
			case 'a':
				pacmans.get(0).move(-25, 0, windowWidth, windowHeight);
				break;
			//move right
			case 'd':
				pacmans.get(0).move(25, 0, windowWidth, windowHeight);
				break;
			//create a new ghost
			//this is for me, not the user
			//will delete
			case 'g':
				Ghost aGhost = new Ghost(windowWidth, windowHeight);
				ghosts.add(aGhost);
			default:
			}
		}
	}
	
	@Override
	public void mousePressed(MouseEvent event) {}
	@Override
	public void mouseReleased(MouseEvent event) {}
	@Override
	public void mouseEntered(MouseEvent event) {}
	@Override
	public void mouseExited(MouseEvent event) {}
	@Override
	public void mouseDragged(MouseEvent event) {}
	@Override
	public void keyTyped(KeyEvent event) {}
	@Override
	public void keyPressed(KeyEvent event) {}
}

