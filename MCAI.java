/* Copyright 2012 David Pearson.
 * BSD License.
 */

import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * An AI that uses Monte Carlo Tree Search to play Hex.
 *
 * @author David Pearson
 */
public class MCAI extends AI {
	private int aiplayer=1;
	private int minLen=49;
	private Location lastPlayed;
	public int diffLevel=75;

	/**
	 * The default constructor.
	 * Assumes that the player is 1.
	 */
	public MCAI() {

	}

	/**
	 * Creates a new instance of MCAI.
	 *
	 * @param player The color to play as (see Constants.java)
	 */
	public MCAI(int player) {
		aiplayer=player;
	}

	/**
	 * Gets the color this AI is playing as.
	 *
	 * @return The color that the AI is playing as (see Constants.java)
	 */
	public int getPlayerCode() {
		return aiplayer;
	}

	/**
	 * Calculates the n-completion value for a given board.
	 *
	 * @param board The board to calculate for
	 * @param player The player to calculate for
	 * @param l The current visited Location on the board
	 * @param visited An ArrayList of Locations already visited
	 * @param count The number of spaces visted already
	 *
	 * @return The n-completion value for a path between l and the edge
	 */
	private int calcN(int[][] board, int player, Location l, ArrayList<Location> visited, int count) {
		if (count<minLen && ((player==1 && l.y==6) || (player==2 && l.x==6))) {
			minLen=count;

			return count;
		} else if ((player==1 && l.y==6) || (player==2 && l.x==6)) {
			return count;
		}

		if (player==1 && count+(6-l.y)>=minLen) {
			return 999;
		} else if (player==2 && count+(6-l.x)>=minLen) {
			return 999;
		}

		ArrayList<Location> adj=l.getAdjacentLocations();
		ArrayList<Bridge> bridges=l.getBridges();

		ArrayList<Location> v=Utils.ALCopy(visited);

		v.add(new Location(l.x, l.y));

		int min=999;

		for (int i=0; i<bridges.size(); i++) {
			Bridge b=bridges.get(i);

			boolean canUseBridge=board[b.mids.get(0).y][b.mids.get(0).x]==0 && board[b.mids.get(1).y][b.mids.get(1).x]==0;
			if (canUseBridge && !Utils.ALContains(v, b.l1) && (board[b.l1.y][b.l1.x]==player || board[b.l1.y][b.l1.x]==0) /*&& (board[b.l2.y][b.l2.x]==player || board[b.l2.y][b.l2.x]==0)*/) {
				int val=calcN(board, player, b.l1, v, count);

				if (val<min) {
					min=val;
				}
			}
		}

		for (int i=0; i<adj.size(); i++) {
			Location loc=adj.get(i);

			if (!Utils.ALContains(v, loc) && (board[loc.y][loc.x]==player || board[loc.y][loc.x]==0)) {
				int val=999;

				if (board[loc.y][loc.x]==player) {
					val=calcN(board, player, loc, v, count);
				} else {
					val=calcN(board, player, loc, v, count+1);
				}

				if (val<min) {
					min=val;
				}
			}
		}

		return min;
	}

	/**
	 * Calculates the n-completion value for a game board state.
	 * 	This is (more or less) a nice wrapper around calcN.
	 *
	 * @param board The board to calculate based on
	 *
	 * @return The n-completion value for the board state provided
	 */
	public double calcVal(int[][] board) {
		int opp=1;
		if (aiplayer==1) {
			opp=2;
		}

		minLen=49;
		double maxno=999;
		double minnp=999;

		for (int i=0; i<board.length; i++) {
			if (board[i][0]!=opp) {
				int initCountP=1;
				int initCountO=1;
				if (board[i][0]==aiplayer) {
					initCountP=0;
				}
				if (board[i][0]==opp) {
					initCountO=0;
				}

				Location pLoc, oLoc;
				if (aiplayer==1) {
					pLoc=new Location(i, 0);
					oLoc=new Location(0, i);
				} else {
					pLoc=new Location(0, i);
					oLoc=new Location(i, 0);
				}

				double no=(double)calcN(board, opp, oLoc, new ArrayList<Location>(), initCountO);
				minLen=49;

				if (no<maxno) {
					maxno=no;
				}
			}
		}

		return maxno;
	}


	/**
	 * Chooses a location for the next play by this AI.
	 *
	 * @param board The board state used in determining the play location
	 * @param last The last play in the game, as made by the opponent
	 *
	 * @return A location to play at, chosen using MCTS
	 */
	public Location getPlayLocation(int[][] board, Location last) {

		long t=System.currentTimeMillis();

		// TODO: If a bridge has had a piece played in it, play so as to hold it.
		lastPlayed=last;
		int roundNumber = getPastMoves(getPlayerCode(), board).size() + 1;
		int oppCode;
		if (getPlayerCode() == 1) {
			oppCode = 2;
		} else {
			oppCode = 1;
		}
		Location bestMove = null;

		/*  Special case => opening move
			If going first => pick middle 	*/
		if (last == null || (last.x == -1 && last.y == -1) || roundNumber == 0) {

			roundNumber++;
			bestMove =  new Location(3,3);
			System.out.println("Round #" + roundNumber + " | AI Move: " + bestMove.toString());

			return bestMove;

		}

		// If going second & P1 (white) placed the 1st move not in the middle => put your 1st move in the middle
		else if (!last.equals(new Location(3,3)) && roundNumber == 1) {

			roundNumber++;
			bestMove =  new Location(3,3);
			System.out.println("Round #" + roundNumber + " | AI Move: " + bestMove.toString());

			return bestMove;

		}

		// Check for bridge based on the last move
		if (last != null && (last.x != -1 && last.y != -1)) {

			// Get possible bridges based on the last move
			ArrayList<Bridge> bridges = last.getBridges();

			for (Bridge b : bridges) {

				// If last move is in a bridge's middle => try to hold or block
				if (b.mids.contains(last)) {

					if (board[b.l1.y][b.l1.x] == 0) {

						bestMove = b.l1;

					} 

					else if (board[b.l2.y][b.l2.x] == 0) {

						bestMove = b.l2;

					}
					if (bestMove != null) {

						System.out.println("Round #" + roundNumber + " | AI Holds Bridge at: " + bestMove.toString());

						return bestMove;

					}
				}
			}
		}

		// Gameplay strategy => minmax with recursion
		ArrayList<Location> legalMoves = getLegalMoves(board);

		if (legalMoves.isEmpty()) {
			System.err.println("Error in getPlayLocation() : no legal moves");
			return null;
		}

		// Minimize AI's score
		double bestScore = Double.POSITIVE_INFINITY; 

		// Adjustable depth limit. 4 is a good balance between deep enough and not too long for recursion to calculate
		int depth = 4; 

		// Evaluate all the possible legal moves using mixmax function
		for (Location move : legalMoves) {

			int[][] tempBoard = Board.BoardCopy(board);
			tempBoard[move.y][move.x] = getPlayerCode();
			
			// Use recursion to calculate the move's outcome/value
			double score = minmax(tempBoard, oppCode, depth - 1, false, move, last);

			if (score < bestScore) {

				bestScore = score;
				bestMove = new Location(move.x, move.y);

			}
		}

		System.out.println("Round #" + roundNumber + " | AI Move: " + bestMove.toString());

		return bestMove;
	}

	/*  Minmax with recursion
		base case: depth is zero or game is over, use calcN()
		else maximize opponent score & minimize yours to improve position 	*/
	public double minmax(int[][] board, int player, int depth, boolean isMaximizing, Location aiLast, Location oppLast) {
		int aiCode = getPlayerCode();
		int oppCode;
		if (aiCode == 1) {
			oppCode = 2;
		} else {
			oppCode = 1;
		}

		// Base case => depth reached or game over
		if (depth == 0 || !Board.hasEmpty(board)) {

			ArrayList<Location> aiPastMoves = getPastMoves(aiCode, board);
			ArrayList<Location> oppPastMoves = getPastMoves(oppCode, board);
			
			// Evaluate AI's path length
			int aiNVal = calcN(board, aiCode, aiLast, aiPastMoves, 0);
			int oppNVal = calcN(board, oppCode, oppLast, oppPastMoves, 0);

			// AI minimizes its path & maximizes opponent's
			return aiNVal - oppNVal;
		}

		ArrayList<Location> legalMoves = getLegalMoves(board);
		
		if (legalMoves.isEmpty()) {
			
			// If no moves left => evaluate the current state of the board
			return evaluate(board, aiLast, oppLast);

		}

		// Opponent's turn => maximize their score
		if (isMaximizing) {

			double maxEval = Double.NEGATIVE_INFINITY;
			for (Location move : legalMoves) {

				int[][] tempBoard = Board.BoardCopy(board);
				tempBoard[move.y][move.x] = player;
				double eval = minmax(tempBoard, aiCode, depth - 1, false, aiLast, move);
				maxEval = Math.max(maxEval, eval);

			}

			return maxEval;
		}

		// AI's turn => minimize its score
		else { 

			double minEval = Double.POSITIVE_INFINITY;

			for (Location move : legalMoves) {

				int[][] tempBoard = Board.BoardCopy(board);
				tempBoard[move.y][move.x] = player;
				double eval = minmax(tempBoard, oppCode, depth - 1, true, move, oppLast);
				minEval = Math.min(minEval, eval);

			}

			return minEval;
		}
	}

	// Evaluates the state of the board/game at the recursion's base case when no moves remain
	public double evaluate(int[][] board, Location aiLast, Location oppLast) {
		int aiCode = getPlayerCode();
		int oppCode;
		if (aiCode == 1) {
			oppCode = 2;
		} else {
			oppCode = 1;
		}

		ArrayList<Location> aiPastMoves = getPastMoves(aiCode, board);
		ArrayList<Location> oppPastMoves = getPastMoves(oppCode, board);

		// Aiming for shorter AI path & longer opponent path 
		int aiNVal = calcN(board, aiCode, aiLast, aiPastMoves, 0);
		int oppNVal = calcN(board, oppCode, oppLast, oppPastMoves, 0);

		return aiNVal - oppNVal;
	}

	// Function for obtaining all legal moves on the board
	public ArrayList<Location> getLegalMoves(int[][] board) {

		ArrayList<Location> moves = new ArrayList<>();

		for (int y = 0; y < board.length; y++) {

			for (int x = 0; x < board[y].length; x++) {

				if (board[y][x] == Constants.EMPTY) {

					moves.add(new Location(x, y));

				}
			}
		}

		return moves;
	}

	// Function for obtaining all past moves
	public ArrayList<Location> getPastMoves(int playerCode, int[][] board) {

		// For white
		int[][] copy = Board.BoardCopy(board);
		ArrayList<Location> moves = new ArrayList<>();

		if (playerCode == 1) {

			for (int y = 0; y < board.length; y++) {

				for (int x = 0; x < board[y].length; x++) {

					if (board[y][x] == Constants.WHITE) {

						moves.add(new Location(x, y));

					}
				}
			}
		} 

		// For black
		else if (playerCode == 2) {

			for (int y = 0; y < board.length; y++) {

				for (int x = 0; x < board[y].length; x++) {

					if (board[y][x] == Constants.BLACK) {

						moves.add(new Location(x, y));
					}
				}
			}

		} else {

			System.err.println("Error in getPastMoves() : invalid playerCode");
			return null;
		}
		return moves;
	}


}