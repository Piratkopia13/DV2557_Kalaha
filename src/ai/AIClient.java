package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;
import ai.MinimaxTree;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;

    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
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
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
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
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
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
                                addText("Made move " + cMove + " in " + e + " secs");
                                totalTime += e;
                                numberOfPlays++;
                                addText("Current avg " + totalTime / numberOfPlays + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
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

        /*MinimaxTree tree = new MinimaxTree();
        int stopDepth = 7;
        DepthFirstStop(tree.getRoot(), stopDepth, currentBoard);

        int maxScore = Integer.MIN_VALUE;
        int bestAmbo = -1;
        TreeNode child = tree.getRoot().getFirstChild();
        // Check all valid moves for the best utility value
        for (int ambo = 1; ambo <= 6; ambo++) {
            int childScore = child.getScore();
            if (childScore > maxScore) {
                maxScore = childScore;
                bestAmbo = ambo;
            }
            child = child.getNextSibling();
        }

        // The currently BEST possible move! (given an optimal opponent)
        return bestAmbo;*/

        return iterativeDeepening(5000, currentBoard);
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
            }

            // Create the current node
            if (ambo == 1) {
                parent.setFirstChild(new TreeNode(score));
                lastNode = parent.getFirstChild();
            } else {
                lastNode.setNextSibling(new TreeNode(score));
                lastNode = lastNode.getNextSibling();
            }

            // Recurse if possible
            if (movePossible && levelsRemaining > 1 && newBoard.getNoValidMoves(player) != 0) {
                score = DepthFirstStop(lastNode, levelsRemaining - 1, newBoard, endTime);
                // Recursion rewind utility score update
                lastNode.setScore(score);
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

    private int iterativeDeepening(long milliSeconds, GameState currentBoard) {

        long endTime = System.currentTimeMillis() + milliSeconds - 10;

        int maxScore = Integer.MIN_VALUE;
        int bestAmbo = -1;
        int stopDepth = 1;

        while (System.currentTimeMillis() < endTime) {
            MinimaxTree tree = new MinimaxTree();
            DepthFirstStop(tree.getRoot(), stopDepth, currentBoard, endTime);

            TreeNode child = tree.getRoot().getFirstChild();
            // Check all valid moves for the best utility value
            for (int ambo = 1; ambo <= 6; ambo++) {
                int childScore = child.getScore();
                if (childScore > maxScore) {
                    maxScore = childScore;
                    bestAmbo = ambo;
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