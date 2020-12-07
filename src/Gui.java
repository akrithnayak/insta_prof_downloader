//Authors:	1. Akrith H Nayak
//		 	2. Akash Shetty


//importing required libraries
import java.io.IOException;

import javax.swing.JFrame;
public class Gui {

	public static void main(String[] args)  {					// main method.
		InstaGui object;
		object= new InstaGui();									// instantiating the class.
		object.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		object.setSize(600, 500);
		object.setVisible(true);
		object.setResizable(false);	
	}
}
