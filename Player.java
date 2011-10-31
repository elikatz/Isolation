import java.util.LinkedList;

/**
 * @author Elliot Katz @epkatz
 * March 29th, 2011
 * Isolation Game
 */

/**
 * The player class is essentially the entire program. It runs the heuristics and 
 * the min-max (alpha-beta)
 */
public class Player {
	
	/**
	 * Constructor for the Player class sets the time
	 */
	public Player(long e){
		END_TIME = e + 1;
	}
	
	/**
	 * Creates a new board and returns it to start the game
	 */
	public int[][] createBoard(int[][] pPos, int whoAmI){
		if (whoAmI == 1){
			myPlayer = 1;
			hisPlayer = 0;
		}
		int[][] board = new int[SIZE][SIZE];
		for (int row = 0; row < SIZE; row++){
			for (int col = 0; col < SIZE; col++){
				board[row][col] = EMPTY;
			}
		}
		board[TOP_ROW][LEFT_COL] = symbols[myPlayer];
		pPos[myPlayer][0] = TOP_ROW;
		pPos[myPlayer][1] = LEFT_COL;
		board[BOTTOM_ROW][RIGHT_COL] = symbols[hisPlayer];
		pPos[hisPlayer][0] = BOTTOM_ROW;
		pPos[hisPlayer][1] = RIGHT_COL;
		movesSoFar = 2;
		return board;
	}
	
	/**
	 * His Turn basically consists of checking whether the move is legal and
	 * then moving the piece 
	 */
	public boolean hisTurn(int[][] b, int[][] pPos, int hisRow, int hisCol){
		movesSoFar++;
		int otherPlayer = 1;
		if (!canMove(b, pPos, otherPlayer, hisRow, hisCol)){
			System.out.println("Illegal Move!");
			return false;
		}
		else{
			move(b, pPos, otherPlayer, hisRow, hisCol);
			return true;
		}
	}
	
	/**
	 * My turn starts the alpha-beta calculation. It also sets the start time for the 
	 * turn. It keeps track of how many moves have gone for the heuristic.
	 */
	public int[][] myTurn(int[][] b, int[][] pPos){
		movesSoFar++;
		int otherPlayer = 1;
		int player = 0;
		startTime = System.currentTimeMillis();
		b = alpha_beta(b, pPos);
		//System.out.println("Took " + ((((double) System.currentTimeMillis()) - startTime)/ 1000) + " seconds");
		int myMoves = countMovesAvailable(b, pPos, player);
		int hisMoves = countMovesAvailable(b, pPos, otherPlayer);
		if (hisMoves == 0){
			System.out.println("BOOYA!! I WIN!\nCan I haz treat please?");
		}
		else if (myMoves == 0){
			System.out.println("I Lost :(\nOh no, my master will be angry");
		}
		return b;
	}

	/**
	 * alpha-beta starts iterative deepening alpha-beta search and handles the output
	 */
	public int[][] alpha_beta(int[][] b, int[][] pPos) {
		//Starting Depth
		END = 4;
		int player = 0;
		int start = 0;
		int depth = 0;
		int[] c = null;
		do { 
			int[] temp = do_alpha_beta(b, pPos, start);
			if (temp != null){
				c = temp;
				depth = END;
			}
			END += 2;
			//This checks to see whether we have gone beyond the time limit
		} while (((double) System.currentTimeMillis()) - startTime  < END_TIME && END < (62 - movesSoFar));
		System.out.println("Depth: " + depth);
		System.out.println("----> Move Your Piece to (" + (c[0] + 1) + "," + (c[1] + 1) + ")");
		move(b, pPos, player, c[0], c[1]);
		return b;
	}
	
	/**
	 * Start of Alpha-Beta, it only accepts a move if the recursion did not terminate early (ie:
	 * it didnt end after time was up)
	 */
	public int[] do_alpha_beta(int[][] b, int[][] pPos, long start){;
		maxValue(b, pPos, ni, pi, start);
		//Checks the time at the end of the recursion
		if (((double) System.currentTimeMillis()) - startTime  < END_TIME){
			return bestMove;
		}
		else{
			return null;
		}
	}

	/**
	 * This is the max value part of the recursion
	 */
	public int maxValue(int[][] b, int[][] pPos, int alpha, int beta, long start) {
		//Checks to see if time ends
		if (((double) System.currentTimeMillis()) - startTime > END_TIME){
			return 0;
		}
		int player = 0;
		int v = ni;
		//If we've reached the end return the recursion
		if (start > END) {
			return myHeurisitc(b, pPos);
		}
		//Generate Moves
		LinkedList<int[]> myList = generateMoves(b, pPos, player);
		for (int[] c : myList){
			int[][] newPos = copyIntArray(pPos);
			int oldRow = pPos[player][0];
			int oldCol = pPos[player][1];
			//Move to a generated move
			move(b, newPos, player, c[0], c[1]);
			//Get the min value
			int tempMin = minValue(b, newPos, alpha, beta, start+1);
			//Unmove
			unMove(b, newPos, player, oldRow, oldCol);
			//Continue the alpha-beta by checking whether we can max 
			if (tempMin > v){
				v = tempMin;
				if (start == 0){
					bestMove = c;
				}
			}
			if (v >= beta){
				return v;
			}
			
			//Take the max of alpha vs the current utility
			alpha = Math.max(alpha, v);
		}
		return v;
	}
	
	/**
	 * Similar in style to alpha-beta. See the comments for maxValue for control flow.
	 */
	public int minValue(int[][] b, int[][] pPos, int alpha, int beta, long start) {
		if (((double) System.currentTimeMillis()) - startTime > END_TIME){
			return 0;
		}
		int player = 1;
		int v = pi;
		if (start > END) {
			return myHeurisitc(b, pPos);
		}
		LinkedList<int[]> myList = generateMoves(b, pPos, player);
		for (int[] c : myList){
			int[][] newPos = copyIntArray(pPos);
			int oldRow = pPos[player][0];
			int oldCol = pPos[player][1];
			move(b, newPos, player, c[0], c[1]);
			int tempMax = maxValue(b, newPos, alpha, beta, start + 1);
			unMove(b, newPos, player, oldRow, oldCol);
			if (tempMax < v) {
				v = tempMax;
			}
			if (v <= alpha) {
				return v;
			}
			beta = Math.min(beta, v);
		}
		return v;
	}
	
	/**
	 * Main heuristic function divides the bored into stages 
	 */
	public int myHeurisitc(int[][] b, int[][] pPos){
		//Below 10 moves and it just uses degrees of freedom
		if (movesSoFar < 10){
			return netcanMove(b, pPos);
		}
		else{
			//Now it starts to trap the other player if possible
			int areaDif = areaDifference(b, pPos);
			if (areaDif != 0){
				return areaDif * 20;
			}
			else{
				//Or it just returns the degrees of freedom
				return netcanMove(b, pPos);
			} 
		}
	}
	
	/**
	 * Returns net degrees of freedom
	 */
	public int netcanMove(int[][] b, int[][] pPos){
		return countMovesAvailable(b, pPos, 0) - countMovesAvailable(b, pPos, 1);
	}
	
	/** 
	 * Returns the net area difference
	 */
	public int areaDifference(int[][] b, int[][] pPos){
		return areaSize(b, pPos, 0) - areaSize(b, pPos, 1);
	}
	
	/**
	 * Counts the Size of an area available to a player
	 */
	public int areaSize(int[][] b, int[][] pPos, int player){
		int row = pPos[player][0];
		int col = pPos[player][1];
		int counter = 0;
		int[][] board = copyArray(b);
		board = makeArea(board, row, col);
		for (int i = 0; i < SIZE; i++){
			for (int j = 0; j < SIZE; j++){
				if (board[i][j] == 9){
					counter++;
				}
			}
		}
		return counter;
	}
	
	/**
	 * Recursive Area maker to check how much area a player can reach from his current position
	 */
	public int[][] makeArea(int[][] board, int row, int col){
		//Down
		if (row + 1 < SIZE){
			if (board[row + 1][col] == EMPTY){
				board[row + 1][col] = SHELL;
				makeArea(board, row + 1, col);
			}
		}
		//Up
		if (row - 1 >= 0){
			if (board[row - 1][col] == EMPTY){
				board[row - 1][col] = SHELL;
				makeArea(board, row - 1, col);
			}
		}
		//Right
		if (col + 1 < SIZE){
			if (board[row][col + 1] == EMPTY){
				board[row][col + 1] = SHELL;
				makeArea(board, row, col + 1);
			}
		}
		//Left
		if (col - 1 >= 0){
			if (board[row][col - 1] == EMPTY){
				board[row][col - 1] = SHELL;
				makeArea(board, row, col - 1);
			}
		}
		//Down Right
		if (col + 1 < SIZE && row + 1 < SIZE){
			if (board[row + 1][col + 1] == EMPTY){
				board[row + 1][col + 1] = SHELL;
				makeArea(board, row + 1, col + 1);
			}
		}
		//Down Left
		if (col - 1 >= 0 && row + 1 < SIZE){
			if (board[row + 1][col - 1] == EMPTY){
				board[row + 1][col - 1] = SHELL;
				makeArea(board, row + 1, col - 1);
			}
		}
		//Up Right 
		if (col + 1 < SIZE && row - 1 >= 0){
			if (board[row - 1][col + 1] == EMPTY){
				board[row - 1][col + 1] = SHELL;
				makeArea(board, row - 1, col + 1);
			}
		}
		//Up Left
		if (col - 1 > 0 && row - 1 >= 0){
			if (board[row - 1][col - 1] == EMPTY){
				board[row - 1][col - 1] = SHELL;
				makeArea(board, row - 1, col - 1);
			}
		}
		return board;
	}
	
	/**
	 * Generates all Moves that a player can make from his position
	 */
	public LinkedList<int[]> generateMoves(int[][] b, int[][] pPos, int player){
		LinkedList<int[]> myList = new LinkedList<int[]>();
		// Generate All Moves
		int row = pPos[player][0];
		int col = pPos[player][1];
		for (int i = 0; i < SIZE; i++) {
			if (canMove(b, pPos, player, row, i)) {
				myList.add(new int[]{row, i});
			}
			if (canMove(b, pPos, player, i, col)) {
				myList.add(new int[]{i, col});
			}
		}
		int depth = SIZE - row;
		int side = SIZE - col;

		// Up Left - min(row, col)
		for (int i = 1; i < Math.min(row + 1, col + 1); i++) {
			if (canMove(b, pPos, player, row - i, col - i)) {
				myList.add(new int[]{row - i, col - i});
			}
		}
		// Up Right - min(row, side)
		for (int i = 1; i < Math.min(row + 1, side); i++) {
			if (canMove(b, pPos, player, row - i, col + i)) {
				myList.add(new int[]{row - i, col + i});
			}
		}
		// Down Left - min(depth, col)
		for (int i = 1; i < Math.min(depth, col + 1); i++) {
			if (canMove(b, pPos, player, row + i, col - i)) {
				myList.add(new int[]{row + i, col - i});
			}
		}
		// Down Right - min(depth, side)
		for (int i = 1; i < Math.min(depth, side); i++) {
			if (canMove(b, pPos, player, row + i, col + i)) {
				myList.add(new int[]{row + i, col + i});
			}
		}
		return myList;
	}
	
	/**
	 * Counts all available moves for a player
	 */
	public int countMovesAvailable(int[][] b, int[][] pPos, int player){
		int counter = 0;
		// Generate All Moves
		int row = pPos[player][0];
		int col = pPos[player][1];
		for (int i = 0; i < SIZE; i++) {
			if (canMove(b, pPos, player, row, i)) {
				counter++;
			}
			if (canMove(b, pPos, player, i, col)) {
				counter++;
			}
		}
		int depth = SIZE - row;
		int side = SIZE - col;

		// Up Left - min(row, col)
		for (int i = 1; i < Math.min(row + 1, col + 1); i++) {
			if (canMove(b, pPos, player, row - i, col - i)) {
				counter++;
			}
		}
		// Up Right - min(row, side)
		for (int i = 1; i < Math.min(row + 1, side); i++) {
			if (canMove(b, pPos, player, row - i, col + i)) {
				counter++;
			}
		}
		// Down Left - min(depth, col)
		for (int i = 1; i < Math.min(depth, col + 1); i++) {
			if (canMove(b, pPos, player, row + i, col - i)) {
				counter++;
			}
		}
		// Down Right - min(depth, side)
		for (int i = 1; i < Math.min(depth, side); i++) {
			if (canMove(b, pPos, player, row + i, col + i)) {
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * Prints the board
	 */
	public void print(int[][] board){
		String temp = "\n    ";
		for (int i = 0; i < SIZE; i++) {
			temp += (i + 1) + "  ";
		}
		temp += "\n   ";
		for (int i = 0; i < SIZE; i++) {
			temp += "___";
		}
		temp += "\n ";
		for (int i = 0; i < SIZE; i++){
			temp += (i + 1) + " |";
			for (int j = 0; j < SIZE; j++){
				if (board[i][j] == EMPTY){
					temp += "-  ";
				}
				if (board[i][j] == FILLED){
					temp += "*  ";
				}
				if (board[i][j] == myPlayer){
					temp += "X  ";
				}
				if (board[i][j] == hisPlayer){
					temp += "O  ";
				}
			}
			temp += "\n ";
		}
		System.out.println(temp);
	}
	
	/**
	 * Generating Moves for player to a new row and col space. This is meant to be called after using the
	 * check above.
	 */
	public int[][] move(int[][] board, int[][] pPos, int player, int newRow, int newCol){
		board[pPos[player][0]][pPos[player][1]] = FILLED;
		board[newRow][newCol] = symbols[player];
		pPos[player][0] = newRow;
		pPos[player][1] = newCol;
		return board;
	}
	
	/**
	 * This undos the move function
	 */
	public int[][] unMove(int[][] board, int[][] pPos, int player, int oldRow, int oldCol){
		board[pPos[player][0]][pPos[player][1]] = EMPTY;
		board[oldRow][oldCol] = symbols[player];
		pPos[player][0] = oldRow;
		pPos[player][1] = oldCol;
		return board;
	}
	
	/**
	 * Return the pPos copy
	 */
	public int[][] copyIntArray(int[][] pPos){
		int[][] temp = new int[2][2];
		temp[0][0] = pPos[0][0];
		temp[0][1] = pPos[0][1];
		temp[1][0] = pPos[1][0];
		temp[1][1] = pPos[1][1];
		return temp;
	}
	
	/**
	 * Copy an Double-int Array as a static method
	 * Used to avoid creating multiple objects for speed boost
	 */
	public int[][] copyArray(int[][] dArray){
		int[][] temp = new int[SIZE][SIZE];
		for (int i = 0; i < SIZE; i++){
			System.arraycopy(dArray[i], 0, temp[i], 0, dArray[i].length);
		}
		return temp;
	}
	
	/**
	 * canMove is a sophisticated model of movement. By breaking the evaluation into parts,
	 * we avoid extraneous checking and gain some speedup. We calculate the difference between
	 * the old row and col with the new row and col thus we can evaluate with zero which
	 * is always faster for the computer. We check to see the rowdif and coldif is 0 or not which
	 * would indicate single direction movement as opposed to horizontal. We then check whether
	 * it is moving up in value or down. Then we check every block in the direction to see
	 * if it is empty.
	 */
	public boolean canMove(int[][] board, int[][] pPos, int player, int newRow, int newCol){
		int oldRow = pPos[player][0];
		int oldCol = pPos[player][1];
		int rowDif = oldRow - newRow;
		int colDif = oldCol - newCol;
		//Moving horizontal, Row stays the same
		if (rowDif == 0){
			//System.out.println("Horizontal");
			if (colDif == 0){
				//System.out.println("No Move");
				return false;
			}
			//Moving down in value
			if (oldCol > newCol){
				//Check every value from oldCol + 1 to the newCol
				for (int i = oldCol - 1; i >= newCol; i--){
					//If the col doesn't equal empty then it is bad
					if (board[oldRow][i] != EMPTY){
						//System.out.println("Non-empty square in the way at " + oldRow + "," + i);
						return false;
					}
				}
			}
			else{
				for (int i = oldCol + 1; i <= newCol; i++){
					if (board[oldRow][i] != EMPTY){
						//System.out.println("Non-empty square in the way at " + oldRow + "," + i);
						return false;
					}
				}
			}
			return true;
		}
		//Moving vertical
		else if (colDif == 0){
			//System.out.println("Vertical");
			if (oldRow > newRow){
				for (int i = oldRow - 1; i >= newRow; i--){
					if (board[i][oldCol] != EMPTY){
						//System.out.println("Non-empty square in the way at " + i + "," + oldCol);
						return false;
					}
				}
			}
			else{
				for (int i = oldRow + 1; i <= newRow; i++){
					if (board[i][oldCol] != EMPTY){
						//System.out.println("Non-empty square in the way at " + i + "," + oldCol);
						return false;
					}
				}
			}
			return true;
		}
		//Diagonal upLeft or downRight
		else if (rowDif == colDif){
			if (oldCol > newCol) {
				//Up Left
				for (int i = 1; i <= rowDif; i++) {
					if (board[oldRow - i][oldCol - i] != EMPTY) {
						//System.out.println("Non-empty square in the way at " + (oldRow - i) + "," + (oldCol - i));
						return false;
					}
				}
			}
			else{
				//Down Right
				for (int i = 1; i <= (-rowDif); i++) {
					if (board[oldRow + i][oldCol + i] != EMPTY) {
						//System.out.println("Non-empty square in the way at " + (oldRow + i) + "," + (oldCol + i));
						return false;
					}
				}
			}
			return true;
		}
		//Diagonal upRight or downLeft
		else if (rowDif == -(colDif)){
			if (oldCol > newCol) {
				for (int i = 1; i <= colDif; i++) {
					if (board[oldRow + i][oldCol - i] != EMPTY) {
						//System.out.println("Non-empty square in the way at " + (oldRow + i) + "," + (oldCol - i));
						return false;
					}
				}
			}
			else{
				for (int i = 1; i <= rowDif; i++) {
					if (board[oldRow - i][oldCol + i] != EMPTY) {
						//System.out.println("Non-empty square in the way at " + (oldRow - i) + "," + (oldCol + i));
						return false;
					}
				}
			}
			return true;
		}
		else{
			System.out.println("Invalid Move");
			return false;
		}
	}
		
	//Instance Variables
	private int END;
	public int ni = -5000;
	public int pi = 5000;
	public double END_TIME;
	private double startTime;
	public int[] bestMove;
	public int movesSoFar;
	public static int myPlayer = 0;
	public static int hisPlayer = 1;
	
	//Board Constants
	public static final int[] symbols = {0, 1};
	public static final int SIZE = 8;
	public static final int EMPTY = 2;
	public static final int FILLED = 3;
	public static final int SHELL = 5;
	public static final int RIGHT_COL = SIZE - 1; //7
	public static final int LEFT_COL = SIZE - SIZE; //0
	public static final int TOP_ROW = SIZE - SIZE; //0
	public static final int BOTTOM_ROW = SIZE - 1; //7

}
