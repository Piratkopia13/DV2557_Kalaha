package kalaha;

import server.OfflineStartingBookGenerator;

/**
 * Start point for the Kalaha application.
 * 
 * @author Johan Hagelbäck
 */
public class BookGenMain
{

    /**
     * Version number.
     */
    public static String VERSION = "1.6";

    /**
     * Default port to start server at.
     */
    public static int port = 10101;

    /**
     * Starts the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        BookGenMain main = new BookGenMain();
    }

    /**
     * Starts the Kalaha GUI and sserver.
     */
    public BookGenMain()
    {
        try
        {
            OfflineStartingBookGenerator server = OfflineStartingBookGenerator.getInstance();
            server.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
