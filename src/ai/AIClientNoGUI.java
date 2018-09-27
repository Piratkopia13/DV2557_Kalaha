package ai;

import kalaha.Commands;
import kalaha.Errors;
import kalaha.GameState;
import kalaha.KalahaMain;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClientNoGUI implements Runnable
{
    private int player;

    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;

    /**
     * Creates a new client.
     */
    public AIClientNoGUI()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.

        try
        {
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
        }
        catch (Exception ex)
        {
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }

    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;

        double totalTime = 0;
        int numberOfPlays = 0;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    running = false;
                }
                if(reply.equals("0"))
                {
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                totalTime += e;
                                numberOfPlays++;
                            }
                        }
                    }
                }
                
                //Wait
                // Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            //ex.printStackTrace();
            running = false;
        }
        
        try
        {
            socket.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     *
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard) {
        //addText(String.valueOf(currentBoard.getHash()));
        // return getMoveWithID(5000, currentBoard);
        return getMoveWithDF(6, currentBoard);
    }

    public int getMoveWithID(long milliSeconds, GameState currentBoard) {
        return iterativeDeepeningWithAlphaBetaPruning(milliSeconds, currentBoard);
    }

    public int getMoveWithDF(int stopDepth, GameState currentBoard) {
        MinimaxTree tree = new MinimaxTree();
        DepthFirstStopAlphaBetaPruning(tree.getRoot(), stopDepth, currentBoard, Long.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

        int maxScore = Integer.MIN_VALUE;
        int bestAmbo = -1;
        TreeNode child = tree.getRoot().getFirstChild();
        // Check all valid moves for the best utility value
        while (child != null) {
            int childScore = child.getScore();
            if (childScore > maxScore) {
                maxScore = childScore;
                bestAmbo = child.getAmbo();
            }
            child = child.getNextSibling();
        }

        // The currently BEST possible move! (given an optimal opponent)
        return bestAmbo;
    }

    private int DepthFirstStop(TreeNode parent, int levelsRemaining, GameState currentBoard, long endTime) {

        // Check if MIN or MAX
        int minOrMax = Integer.MAX_VALUE;
        boolean minimize = true;
        if (currentBoard.getNextPlayer() == player) {
            minOrMax = Integer.MIN_VALUE;
            minimize = false;
        }

        TreeNode lastNode = null;
        // Check all 6 nodes of the parent (maximum of 6 different moves per play)
        for (int ambo = 1; ambo <= 6; ambo++) {

            // Clone the board so not to change the current gamestate
            GameState newBoard = currentBoard.clone();
            int otherPlayer = (player == 1) ? 2 : 1;
            int score = (minimize) ? Integer.MAX_VALUE : Integer.MIN_VALUE;

            // Make move if it is possible and within the time limit
            boolean movePossible = newBoard.moveIsPossible(ambo) && System.currentTimeMillis() < endTime;
            if (movePossible) {
                newBoard.makeMove(ambo);
                score = newBoard.getScore(player) - newBoard.getScore(otherPlayer);

                // Create the current node
                if (parent.getFirstChild() == null) {
                    parent.setFirstChild(new TreeNode(ambo));
                    lastNode = parent.getFirstChild();
                } else {
                    lastNode.setNextSibling(new TreeNode(ambo));
                    lastNode = lastNode.getNextSibling();
                }

                // Recurse if possible
                if (levelsRemaining > 1 && newBoard.getNoValidMoves(player) != 0) {
                    score = DepthFirstStop(lastNode, levelsRemaining - 1, newBoard, endTime);
                    // Recursion rewind utility score update
                    lastNode.setScore(score);
                }
            }

            // Assign the utility value depending on if it's MIN's or MAX's turn
            if (minimize) {
                if (score < minOrMax) {
                    minOrMax = score;
                }
            } else {
                if (score > minOrMax) {
                    minOrMax = score;
                }
            }

        }

        return minOrMax;

    }

    private int DepthFirstStopAlphaBetaPruning(TreeNode parent, int levelsRemaining, GameState currentBoard, long endTime, int alpha, int beta) {

        // Check if MIN or MAX
        int minOrMax = Integer.MAX_VALUE;
        boolean minimize = true;
        if (currentBoard.getNextPlayer() == player) {
            minOrMax = Integer.MIN_VALUE;
            minimize = false;
        }

        TreeNode lastNode = null;
        // Check all 6 nodes of the parent (maximum of 6 different moves per play)
        for (int ambo = 1; ambo <= 6; ambo++) {

            // Clone the board so not to change the current gamestate
            GameState newBoard = currentBoard.clone();
            int otherPlayer = (player == 1) ? 2 : 1;
            int score = (minimize) ? Integer.MAX_VALUE : Integer.MIN_VALUE;

            // Make move if it is possible and within the time limit
            boolean movePossible = newBoard.moveIsPossible(ambo) && System.currentTimeMillis() < endTime;
            if (movePossible) {
                newBoard.makeMove(ambo);
                score = newBoard.getScore(player) - newBoard.getScore(otherPlayer);

                // Create the current node
                if (parent.getFirstChild() == null) {
                    parent.setFirstChild(new TreeNode(ambo));
                    lastNode = parent.getFirstChild();
                } else {
                    lastNode.setNextSibling(new TreeNode(ambo));
                    lastNode = lastNode.getNextSibling();
                }

                // Recurse if possible
                if (movePossible && levelsRemaining > 1 && newBoard.getNoValidMoves(player) != 0) {
                    score = DepthFirstStopAlphaBetaPruning(lastNode, levelsRemaining - 1, newBoard, endTime, alpha, beta);
                    // Recursion rewind utility score update
                    lastNode.setScore(score);
                }
            }

            // Assign the utility value depending on if it's MIN's or MAX's turn
            if (minimize) {
                minOrMax = Math.min(minOrMax, score);
                beta = Math.min(beta, minOrMax);
            } else {
                minOrMax = Math.max(minOrMax, score);
                alpha = Math.max(alpha, minOrMax);
            }
            // Alpha beta pruning
            if (beta <= alpha)
                break;

        }

        return minOrMax;

    }

    private int iterativeDeepeningWithAlphaBetaPruning(long milliSeconds, GameState currentBoard) {

        long endTime = System.currentTimeMillis() + milliSeconds - 10;

        int maxScore = Integer.MIN_VALUE;
        int bestAmbo = -1;
        int stopDepth = 1;

        while (System.currentTimeMillis() < endTime) {
            MinimaxTree tree = new MinimaxTree();
            DepthFirstStopAlphaBetaPruning(tree.getRoot(), stopDepth, currentBoard, endTime, Integer.MIN_VALUE, Integer.MAX_VALUE);
            //DepthFirstStop(tree.getRoot(), stopDepth, currentBoard, endTime);


            TreeNode child = tree.getRoot().getFirstChild();
            // Check all valid moves for the best utility value
            while (child != null) {
                int childScore = child.getScore();
                if (childScore > maxScore) {
                    maxScore = childScore;
                    bestAmbo = child.getAmbo();
                }
                child = child.getNextSibling();
            }

            stopDepth++;
        }

        // The currently BEST possible move! (given an optimal opponent)
        return bestAmbo;
    }

    /**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */
    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }
}