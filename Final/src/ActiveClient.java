import java.io.*;
import java.net.*;
import javax.swing.JTextArea;

public class ActiveClient extends MessageParser implements Runnable
{
    Util logger;
    GameBoard gb;
    JTextArea log;
    public static String MonitorName;
    Thread runner;
    Socket toMonitor = null;
    public static int MONITOR_PORT;
    public static int LOCAL_PORT;
    public int SleepMode;
    int DELAY = 90000;  //Interval after which a new Active Client is started 
    long prevTime,present;
    public boolean connected = false;

    public ActiveClient()
    {
        super("[no-name]", "[no-password]");
        logger = new Util(null);
        MonitorName="";
        toMonitor = null;
        MONITOR_PORT=0;
        LOCAL_PORT=0;
    }

    public ActiveClient(GameBoard gb, String mname, int p, int lp, int sm,
                        String name, String password)
    {
        super(name, password);
        this.gb = gb;
        this.log = gb.clientLog;
        logger = new Util(log);
        try
        {
            SleepMode = sm;
            MonitorName = mname; 
            MONITOR_PORT = p; 
            LOCAL_PORT = lp;
        }
        catch ( NullPointerException n )
        {
            logger.Print(DbgSub.ACTIVE_CLIENT, "[Constructor] TIMEOUT Error: "+n);
            if ( gb != null )
            {
                gb.appGlobalMessage.setText( "[Constructor] TIMEOUT Error: "+n );
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

    public void run()
    {
        while ( Thread.currentThread() == runner )
        {
            try
            {
                logger.Print(DbgSub.ACTIVE_CLIENT, "trying monitor: "+MonitorName+
                                 " port: "+MONITOR_PORT+"...");
                toMonitor = new Socket(MonitorName, MONITOR_PORT);
                logger.Print(DbgSub.ACTIVE_CLIENT, "completed.");
                plainOut = new PrintWriter(toMonitor.getOutputStream(), true);
                plainIn = new BufferedReader(new InputStreamReader(toMonitor.getInputStream()));
                
                out = plainOut;
                in = plainIn;

                HOSTNAME = toMonitor.getLocalAddress().getHostName();
                CType = 0;   //Indicates Client 
                HOST_PORT = LOCAL_PORT;
                if ( !Login() )
                {
                    if ( IsVerified == 0 ) System.exit(1);
                }
                logger.Print(DbgSub.ACTIVE_CLIENT, "***************************");
//                if ( Execute("GET_GAME_IDENTS") )
//                {
//                    String msg = GetMonitorMessage();
//                    System.out.println("ActiveClient [GET_GAME_IDENTS]:\n\t"+msg);
//                }
//                if ( Execute("RANDOM_PARTICIPANT_HOST_PORT") )
//                {
//                    String msg = GetMonitorMessage();
//                    System.out.println("ActiveClient [RANDOM_PARTICIPANT_HOST_PORT]:\n\t"+msg);
//                }
//                if ( Execute("PARTICIPANT_HOST_PORT", "FRANCO") )
//                {
//                    String msg = GetMonitorMessage();
//                    System.out.println("ActiveClient [PARTICIPANT_HOST_PORT]:\n\t"+msg);
//                }
//                if ( Execute("PARTICIPANT_STATUS") )
//                {
//                    String msg = GetMonitorMessage();
//                    System.out.println("ActiveClient [PARTICIPANT_STATUS]:\n\t"+msg);
//                }
//                ChangePassword(PASSWORD);
//                System.out.println("Password:"+PASSWORD);

                toMonitor.close(); 
                out.close(); 
                in.close();
                try
                {
                    Thread.sleep(DELAY);
                }
                catch ( Exception e )
                {
                	e.printStackTrace();
                    if ( gb != null )
                    {
                        gb.appGlobalMessage.setText( e.toString() );
                    }
                }

            }
            catch ( UnknownHostException e )
            {
            	e.printStackTrace();
                if ( gb != null )
                {
                    gb.appGlobalMessage.setText( e.toString() );
                }
            }
            catch ( IOException e )
            {
                try
                {
                    toMonitor.close();  
                    //toMonitor = new Socket(MonitorName,MONITOR_PORT);
                }
                catch ( IOException ioe )
                {
                	e.printStackTrace();
                    if ( gb != null )
                    {
                        gb.appGlobalMessage.setText( ioe.toString() );
                    }
                }
                catch ( NullPointerException n )
                {
                    try
                    {
                        if ( toMonitor != null )
                        {
                            toMonitor.close();
                        }
                        //toMonitor = new Socket(MonitorName,MONITOR_PORT);
                    }
                    catch ( IOException ioe )
                    {
                    	e.printStackTrace();
                        if ( gb != null )
                        {
                            gb.appGlobalMessage.setText( ioe.toString() );
                        }
                    }
                }
            }
        }
    }
}

