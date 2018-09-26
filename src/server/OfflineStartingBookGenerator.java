package server;

import ai.*;
import client.BadClient;
import client.BadClientNoGUI;
import client.RandomClient;
import jdk.nashorn.api.tree.Tree;
import kalaha.Commands;
import kalaha.Errors;
import kalaha.GameState;
import kalaha.KalahaMain;

import java.awt.desktop.OpenFilesEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Random;

public class OfflineStartingBookGenerator implements Runnable {
    public OfflineStartingBookGenerator() {

    }

    private ServerGUI g;
    private static OfflineStartingBookGenerator instance;

    private GameState game;

    private ServerSocket ssocket;
    private int nextClient;
    private OfflineStartingBookGenerator.ClientThread[] clients;
    private boolean running;

    public static OfflineStartingBookGenerator getInstance()
    {
        if (instance == null)
        {
            instance = new OfflineStartingBookGenerator();
        }
        return instance;
    }
    /**
     * Starts the game server on the specified network port.
     */
    public void start() throws InterruptedException {
        nextClient = 0;
        game = new GameState();
        g = ServerGUI.getInstance();

        try
        {
            g.addText("Starting server at port " + KalahaMain.port);
            ssocket = new ServerSocket(KalahaMain.port);
            g.addText("Server started successfully");
            clients = new OfflineStartingBookGenerator.ClientThread[2];
        }
        catch(Exception ex)
        {
            g.addText("ERROR: Could not start server on port " + KalahaMain.port + ": " + ex.getMessage());
            return;
        }

        //Start the client listener thread
        Thread thr = new Thread(this);
        thr.start();


        // När third step går ur, kolla största fourth step och spara den med matchande hash från fourth step

        // Book gen
        OpeningBook book = new OpeningBook();

        // Modify starting state for opening book (opponent starts)
        for (int firstMove = 1; firstMove <= 10; firstMove++) {
            int bestSecondMove = Integer.MIN_VALUE;

            for (int secondMove = 1; secondMove <= 6; secondMove++) {
                int bestFourthMove = Integer.MIN_VALUE;

                for (int thirdMove = 1; thirdMove <= 6; thirdMove++) {

                    for (int fourthMove = 1; fourthMove <= 6; fourthMove++) {

                        if(firstMove < 6) {
                            game.makeMove(1);
                            game.makeMove(firstMove + 1);
                        } else {
                            game.makeMove(firstMove - 4);
                        }

                        // Store hash for second move


                        game.makeMove(secondMove);
                        game.makeMove(thirdMove);
                        game.makeMove(fourthMove);

                        // PLAY THE REST OF THE GAME!

                        bestFourthMove = Math.max(bestFourthMove, game.getScore(1));
                    }

                    book.setMove(game.getHash(), bestFourthMove);

                }
                bestSecondMove = Math.max(bestSecondMove, bestFourthMove);
            }
        }


        // AI is player 1

        long startTime = System.currentTimeMillis();

        MinimaxTree tree = new MinimaxTree();

        for (int firstMove = 1; firstMove <= 1; firstMove++) {
            GameState initialGame = new GameState();

            if(firstMove < 6) {
                initialGame.makeMove(1);
                initialGame.makeMove(firstMove + 1);
            } else {
                initialGame.makeMove(firstMove - 4);
            }

            buildAiFirstTree(tree.getRoot(), initialGame, true);

        }

        System.out.println("Tree built in " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("dun");


//        TreeNode lastNode = tree.getRoot();
//
//        int startMoveScore = Integer.MIN_VALUE;
//
//        for (int firstMove = 1; firstMove <= 10; firstMove++) {
//            int bestSecondScore = Integer.MIN_VALUE;
//            int bestSecondAmbo = -1;
//
//            if (firstMove == 1) {
//                lastNode.setFirstChild(new TreeNode(firstMove));
//                lastNode = lastNode.getFirstChild();
//            } else {
//                lastNode.setNextSibling(new TreeNode(firstMove));
//                lastNode = lastNode.getNextSibling();
//            }
//
//
//            for (int secondMove = 1; secondMove <= 6; secondMove++) {
//
//                int bestScore = Integer.MIN_VALUE;
//                int bestAmbo = -1;
//                int hashAfterOpponentsFirstMove = 0;
//
//                TreeNode
//
//                if (secondMove == 1) {
//                    lastNode.setFirstChild(new TreeNode(secondMove));
//                    lastNode = lastNode.getFirstChild();
//                } else {
//                    lastNode.setNextSibling(new TreeNode(secondMove));
//                    lastNode = lastNode.getNextSibling();
//                }
//
//                for (int thirdMove = 1; thirdMove <= 6; thirdMove++) {
//
//                    if(firstMove < 6) {
//                        game.makeMove(1);
//                        game.makeMove(firstMove + 1);
//                    } else {
//                        game.makeMove(firstMove - 4);
//                    }
//
//
//
//                    game.makeMove(secondMove);
//                    // Store hash for second move
//                    if (thirdMove == 1)
//                        hashAfterOpponentsFirstMove = game.getHash();
//
//
//                    game.makeMove(thirdMove);
//
//
//                    finishGame();
//
//                    int newScore = game.getScore(1);
//                    if (bestScore < newScore) {
//                        bestScore = newScore;
//                        bestAmbo = thirdMove;
//                    }
//
//                    game = new GameState();
//
//                }
//
//                if (bestSecondScore < bestScore) {
//                    bestSecondScore = bestScore;
//                    bestSecondAmbo = bestAmbo;
//                }
//
//            }
//
//
//
//            startMoveScore = Math.max(startMoveScore, bestSecondScore);
//
//        }


        // game.makeMove()

//        for (int it = 0; it < 2; it++) {
//
//
//
//            AIClient aiClient = new AIClient();
//            aiClient.start();
//
//            RandomClient badClient = new RandomClient();
//            badClient.start();
//
//            while (game.getWinner() == -1) {
//                Thread.sleep(100);
//            }
//
//            System.out.println("Iteration " + it + ": player " + game.getWinner() + " won, scores " + game.getScore(1) + " - " + game.getScore(2));
//
//            game = new GameState();
//
//        }

    }


    private void buildAiFirstTree(TreeNode parent, GameState gameState, boolean isOpponentsTurn) {

        TreeNode currentNode = null;

        for (int move = 1; move <= 6; move++) {
            GameState newState = gameState.clone();

            if (move == 1) {
                parent.setFirstChild(new TreeNode(-1));
                currentNode = parent.getFirstChild();
            } else {
                currentNode.setNextSibling(new TreeNode(-1));
                currentNode = currentNode.getNextSibling();
            }

            newState.makeMove(move);

            if (isOpponentsTurn) {
                currentNode.setScore(newState.getHash());
                buildAiFirstTree(currentNode, newState, false);
            } else {

                game = newState;

                try {
                    finishGame();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int score = game.getScore(1);

                currentNode.setScore(score);
                currentNode.setAmbo(move);


            }


        }

    }


    private void finishGame() throws InterruptedException {
        AIClientNoGUI aiClient = new AIClientNoGUI();
        aiClient.start();

        BadClientNoGUI badClient = new BadClientNoGUI();
        badClient.start();

        while (game.getWinner() == -1) {
            Thread.sleep(5);
        }

        System.out.println("One game finished");

//        System.out.println("Iteration " + it + ": player " + game.getWinner() + " won, scores " + game.getScore(1) + " - " + game.getScore(2));

    }

    /**
     * Stops the game server.
     */
    public void stop()
    {
        try
        {
            running = false;
            ssocket.close();

            if (clients[0] != null)
            {
                clients[0].stop();
                clients[0] = null;
            }
            if (clients[1] != null)
            {
                clients[1].stop();
                clients[1] = null;
            }
        }
        catch (Exception ex)
        {
            g.addText("Error closing game server: " + ex.getMessage());
            return;
        }
        g.addText("Game server stopped");
    }

    /**
     * Checks of both player have connect to this game.
     *
     * @return True if both players are connected, false otherwise.
     */
    public boolean gameIsFull()
    {
        if (clients[0] != null && clients[1] != null)
        {
            return true;
        }
        return false;
    }

    /**
     * Thread for listening to client connects. Once a client connects
     * to the server, a new client thread is started.
     */
    public void run()
    {
        running = true;

        while (running)
        {
            try
            {
                Socket mSocket = ssocket.accept();
                if (nextClient == 0)
                {
                    if (clients[0] != null)
                        clients[0].stop();
                    clients[0] = new OfflineStartingBookGenerator.ClientThread(mSocket, 1);
                }
                else if (nextClient == 1)
                {
                    if (clients[1] != null)
                        clients[1].stop();
                    clients[1] = new OfflineStartingBookGenerator.ClientThread(mSocket, 2);
                }
                else
                {
                    //No more players allowed. Close socket.
                    mSocket.close();
                }
                nextClient = (nextClient == 0) ? 1 : 0;
            }
            catch (Exception ex)
            {
                g.addText("Error starting client " + (nextClient + 1) + ": " + ex.getMessage());
                running = false;
            }
        }
    }

    /**
     * Thread class for a client (player).
     */
    private class ClientThread implements Runnable
    {
        private Socket socket;
        private boolean running;
        private ServerGUI g;
        private int iAmPlayer;

        /**
         * Creates and starts a new client thread.
         *
         * @param socket Network socket
         * @param iAmPlayer Player number for this client (1 or 2)
         */
        public ClientThread(Socket socket, int iAmPlayer)
        {
            this.socket = socket;
            this.iAmPlayer = iAmPlayer;
            g = ServerGUI.getInstance();

            running = true;
            Thread thr = new Thread(this);
            thr.start();
        }

        /**
         * Stops this client thread.
         */
        public void stop()
        {
            try
            {
                running = false;
                socket.close();
            }
            catch (Exception ex)
            {
                g.addText("Error closing client " + iAmPlayer + ": " + ex.getMessage());
                return;
            }
            g.addText("Client " + iAmPlayer + " closed");
        }

        /**
         * Thread for listening to commands sent from the connected client.
         */
        public void run()
        {
            running = true;
            while (running)
            {
                try
                {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String cmd = in.readLine();
                    String reply;

                    while(cmd != null)
                    {
                        if (cmd.startsWith(Commands.HELLO))
                        {
                            reply = Commands.HELLO + " " + iAmPlayer;
                            g.addText("Client " + iAmPlayer + " connected");

                            if (iAmPlayer == 2)
                            {
                                //Both players connected. Update board.
                                g.updateBoard(game);
                            }
                        }
                        else if (cmd.startsWith(Commands.BOARD))
                        {
                            reply = game.toString();
                        }
                        else if (cmd.startsWith(Commands.MOVE))
                        {
                            if (!gameIsFull())
                            {
                                reply = Errors.GAME_NOT_FULL;
                            }
                            else
                            {
                                reply = makeMove(cmd);
                            }
                        }
                        else if (cmd.startsWith(Commands.NEXT_PLAYER))
                        {
                            if (!gameIsFull())
                            {
                                reply = Errors.GAME_NOT_FULL;
                            }
                            else
                            {
                                reply = "" + game.getNextPlayer();
                            }
                        }
                        else if (cmd.startsWith(Commands.NEW_GAME))
                        {
                            if (!gameIsFull())
                            {
                                reply = Errors.GAME_NOT_FULL;
                            }
                            else
                            {
                                g.addText("New game");
                                game = new GameState();
                                reply = game.toString();
                            }
                        }
                        else if (cmd.startsWith(Commands.WINNER))
                        {
                            if (!gameIsFull())
                            {
                                reply = Errors.GAME_NOT_FULL;
                            }
                            else
                            {
                                reply = "" + game.getWinner();
                            }
                        }
                        else
                        {
                            reply = Errors.CMD_NOT_FOUND;
                        }

                        out.println(reply);

                        //Read new line
                        cmd = in.readLine();
                    }
                }
                catch(Exception ex)
                {
                    g.addText("Connection error: " + ex.getMessage());
                }
            }

            running = false;
        }

        /**
         * Tries to make the move a requested from the client.
         *
         * @param cmd Move command string
         * @return Server reply
         */
        public String makeMove(String cmd)
        {
            String tokens[] = cmd.split(" ");
            int ambo;
            int player;

            if(tokens.length != 3)
            {
                return Errors.INVALID_PARAMS;
            }

            try
            {
                ambo = Integer.parseInt(tokens[1]);
                player = Integer.parseInt(tokens[2]);
            }
            catch(NumberFormatException ex)
            {
                return Errors.INVALID_PARAMS;
            }

            //Check if move is valid
            if (ambo < 1 || ambo > 6)
            {
                return Errors.INVALID_MOVE;
            }

            //Check if the correct player is
            //making the move
            if(player != game.getNextPlayer())
            {
                return Errors.WRONG_PLAYER;
            }

            //Check if the ambo is empty
            if(game.getSeeds(ambo, player) == 0)
            {
                return Errors.AMBO_EMPTY;
            }

            //Make the move!
            game.makeMove(ambo);
            //g.addText("Move " + ambo + " by Player " + player);
            g.updateBoard(game);

            if(game.gameEnded())
            {
                g.addText("Player " + game.getWinner() + " won");
                g.updateBoard(game);
            }

            //Valid move
            return game.toString();
        }
    }

}
