package client;

import ai.Global;
import kalaha.Commands;
import kalaha.Errors;
import kalaha.KalahaMain;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Bad playing client for the Kalaha game server. The client always
 * makes the first possible move (first non-empty ambo).
 * 
 * @author Johan Hagelbäck
 */
public class BadClientNoGUI implements Runnable
{
    private int player;

    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;

    /**
     * Creates a new bad playing client.
     */
    public BadClientNoGUI()
    {
	player = -1;
        
        try
        {
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        thr = new Thread(this);
        thr.start();
    }
    

    /**
     * Thread for server communication. Checks when it is this 
     * client's turn to make a move, and then makes a random
     * move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                }
                
                //Check if game has ended
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

                //Check if it is my turn
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        reply = in.readLine();
                        int cnt = 1;
                        boolean validMove = false;
                        while (!validMove)
                        {
                            String cMove = "" + cnt;
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (reply.startsWith("ERROR"))
                            {
                                cnt++;
                            }
                            else
                            {
                                validMove = true;
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
            // ex.printStackTrace();
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
}