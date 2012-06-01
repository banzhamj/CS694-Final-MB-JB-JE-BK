import java.io.*;
import java.net.*;

class ConnectionHandler extends MessageParser implements Runnable
{
    GameBoard gb;
    Util logger;
    private Socket incoming;
    //private int counter;
    Thread runner;

    public ConnectionHandler (GameBoard gb, Socket i, int c, String name, String password)
    {
        super(name, password);
        this.gb = gb;
        logger = new Util(gb.serverLog);
        incoming = i;
        //counter = c;
    }

    public void run()
    {
        try
        {
            in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            out = new PrintWriter(incoming.getOutputStream(),true);

            HOST_PORT = Server.LOCAL_PORT;
            CType = 1;  //Indicates Server
            logger.Print(DbgSub.CONNECTION_HANDLER, "Starting login from Server..");
            if ( Login() )
            {
                logger.Print(DbgSub.CONNECTION_HANDLER, "[run]: success Logged In!");
            }
            else
            {
                logger.Print(DbgSub.CONNECTION_HANDLER, "Server could not log in.");
                if ( IsVerified != 1 )
                {
                }
            }
            incoming.close();
        }
        catch ( IOException e )
        {
        	e.printStackTrace();
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( e.toString() );
            }
        }
        catch ( NullPointerException n )
        {
        	n.printStackTrace();
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( n.toString() );
            }
        }
    }

    public void start()
    {
        if ( runner == null )
        {
            runner = new Thread(this);
            runner.start();
        }
    }
}
