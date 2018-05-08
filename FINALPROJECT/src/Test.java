import javax.swing.JFrame;

public class Test {

	public static void main(String[] args) {
		JFrame myFrame = new JFrame("Remi's Game");

		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ThePanel pacmanPanel = new ThePanel(1200,700);
		
		myFrame.setResizable(false);
		
		myFrame.getContentPane().add(pacmanPanel);
		
		myFrame.pack();
		
		myFrame.setVisible(true);
		
		int x = 5;
		int y = 2;
		double w = Math.pow(x, y);
		System.out.println(w);
		
	}
}