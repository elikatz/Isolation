import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Elliot Katz @epkatz
 * March 29th 2011
 * Isolation Game
 */

/**
 * This is the Tester Class
 */
public class Genius {

	/**
	 * The main driver will run each turn and determine times and starting player
	 */
	public static void main(String[] args) {
		int a1;
		int a2;
		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Enter Time Limit in Seconds");
		String time = " ";
		try {
			time = br.readLine();
		} catch (IOException e) {
			System.out.println("Bad Input");
			System.exit(1);
		}
		long setTime = Long.parseLong(time);
		Player me = new Player(setTime * 1000);
		
		String first = "me";
		
		System.out.println("Who goes first");
		try {
			first = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Input was Bad. Try again.");
			System.exit(0);
		}
				
		int[][] pPos = new int[2][2]; //X is 0, Y is 1; Row is 0, Col is 1
		int[][] b = me.createBoard(pPos, 0);
		//Determines whether I don't go first
		if (!first.equalsIgnoreCase("me")){
			b = me.createBoard(pPos, 1);
		}
		me.print(b);
		//I don't actually end the game because I'm afraid what might happen if I type an invalid move
		while(true){
			try {
				if (first.equals("me")){
					if (first.equalsIgnoreCase("me")){
						System.out.println("Wait for me to compute first turn please");
						b = me.myTurn(b, pPos);
						me.print(b);
						first = "not me";
					}
				}
				System.out.println("Enter Row");
				String coordRow = br.readLine();
				a1 = Integer.parseInt(coordRow);
				a1 -= 1;
				System.out.println("Enter Col");
				String coordCol = br.readLine();
				a2 = Integer.parseInt(coordCol);
				a2 -= 1;
				
				System.out.println("Are You Sure? (Type \"Yes I am Sure\")");
				String areyousure = br.readLine();
				if (areyousure.equalsIgnoreCase("Yes I am Sure")) {
					//System.out.println("Trying " + a1 + "," + a2);
					if (me.hisTurn(b, pPos, a1, a2)) {
						b = me.myTurn(b, pPos);
						me.print(b);
					}
				}
			} catch (IOException ioe) {
				System.out.println("Bad Input");
				System.exit(1);
			} catch (NumberFormatException nfe){
				System.out.println("Not a number");
			}
		}

	}
}
