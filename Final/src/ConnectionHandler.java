import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

class ConnectionHandler extends MessageParser implements Runnable
{
    GameBoard gb;
    private Socket incoming;
    //private int counter;
    Thread runner;

    public ConnectionHandler (GameBoard gb, Socket i, int c, String name, String password)
    {
        super(name, password);
        this.gb = gb;
        logger = new Util(gb.serverLog);
        parentSub = DbgSub.CONNECTION_HANDLER;
        incoming = i;
        //counter = c;
    }

    public boolean Login()
    {
        GetMonitorMessage();

        ProcessUntilQuit();
        return ProcessResult();
    }

    public void ProcessUntilQuit()
    {
        while (!require.equals("none") ) {
            ProcessResult();
            if ( require.equalsIgnoreCase("ALIVE") ) {
                Execute(require + " " + GlobalData.GetCookie());
            } else if ( require.equalsIgnoreCase("WAR_DEFEND") ) {
                gb.warDefendButton.setBackground(Color.yellow);
            } else {
                Execute(require);
            }
            GetMonitorMessage();
        }
    }

    public boolean ProcessResult()
    {
        if ( result.equals("none") ) {
            return false;
        }
        StringTokenizer st = new StringTokenizer(result);
        if ( st.hasMoreTokens() ) {
            if ( st.nextToken().equalsIgnoreCase(lastCommandSent) ) {
                return true;
            }
        }
        return false;
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
