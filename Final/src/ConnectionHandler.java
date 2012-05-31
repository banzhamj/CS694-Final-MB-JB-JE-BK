import java.io.*;
import java.net.*;

class ConnectionHandler extends MessageParser implements Runnable
{
    private Socket incoming;
    private int counter;
    Thread runner;

    public ConnectionHandler (Socket i, int c, String name, String password)
    {
        super(name, password);
        incoming = i;
        counter = c;
    }

    public void run()
    {
        try
        {
            in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            out = new PrintWriter(incoming.getOutputStream(),true);

            HOST_PORT = Server.LOCAL_PORT;
            CType = 1;  //Indicates Server
            System.out.println("Starting login from Server..");
            if ( Login() )
            {
                System.out.println("ConnectionHandler [run]: success Logged In!");
            }
            else
            {
                System.out.println("Server could not log in.");
                if ( IsVerified != 1 )
                {
                }
            }
            incoming.close();
        }
        catch ( IOException e )
        {
        	e.printStackTrace();
        }
        catch ( NullPointerException n )
        {
        	n.printStackTrace();
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
