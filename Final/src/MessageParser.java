import java.util.*;
import java.io.*;
import java.math.BigInteger;


public class MessageParser
{
    //Monitor Handling Declarations
    int COMMAND_LIMIT = 25;
    public  int CType;
    public static String HOSTNAME;
    PrintWriter out = null; 
    BufferedReader in = null; 
    String mesg,sentmessage;
    String filename;
    StringTokenizer t;
    String IDENT = "Skipper";
    String PASSWORD = "franco";
    String newPassword;
    static String COOKIE ="bkuhn";
    String PPCHECKSUM="";
    int HOST_PORT;
    public static int IsVerified;

    //File I/O Declarations
    PermanentStorage storage = null;
    static String InputFileName = "Input.dat";  
    String[] cmdArr = new String[COMMAND_LIMIT];

    static String MyKey;
    String MonitorKey;
    String first;
    ObjectInputStream oin = null;
    ObjectOutputStream oout = null;

    //Encryption stuff
    BigInteger myPublicKey;
    BigInteger mySecretKey;
    PlantDHKey newKey = new PlantDHKey();
    DiffieHellmanExchange dhExchange = new DiffieHellmanExchange();
    Karn myKarn = null;
    boolean IsEncrypted = false;

    // Transfer stuff
    String ROUNDS = "20";

    public MessageParser()
    {
        filename = "passwd.dat";
        storage = new PermanentStorage(this);
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file
    }

    public MessageParser(String ident, String password)
    {
        filename = ident+".dat";
        storage = new PermanentStorage(this, ident);
        PASSWORD = password;
        IDENT = ident;
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file
    }

    public String GetMonitorMessage()
    {
        String sMesg="", decrypt="";
        try
        {
            String temp = in.readLine();
            first = temp; // 1st
            sMesg = temp;
            decrypt = temp;

            System.out.println("Received: " + decrypt);
            //After IDENT has been sent-to handle partially encrypted msg group
            while ( !(decrypt.trim().equals("WAITING:")) )
            {
                temp = in.readLine();
                sMesg = sMesg.concat(" ");
                decrypt = temp;
                sMesg = sMesg.concat(decrypt);
            } //sMesg now contains the Message Group sent by the Monitor
        }
        catch ( IOException e )
        {
            System.out.println("MessageParser [getMonitorMessage]: error "+
                               "in GetMonitorMessage:\n\t"+e+this);
            sMesg="";
        }
        catch ( NullPointerException n )
        {
            sMesg = "";
        }
        catch ( NumberFormatException o )
        {
            System.out.println("MessageParser [getMonitorMessage]: number "+
                               "format error:\n\t"+o+this);
            sMesg="";
        }
        catch ( NoSuchElementException ne )
        {
            System.out.println("MessageParser [getMonitorMessage]: no such "+
                               "element exception occurred:\n\t"+this);
        }
        catch ( ArrayIndexOutOfBoundsException ae )
        {
            System.out.println("MessageParser [getMonitorMessage]: AIOB "+
                               "EXCEPTION!\n\t"+this);
            sMesg="";
        }
        return sMesg;
    }

    //Handling Cookie and PPChecksum
    public String GetNextCommand (String mesg, String sCommand)
    {
        try
        {
            String sDefault = "REQUIRE";
            if ( !(sCommand.equals("")) ) sDefault = sCommand;
            t = new StringTokenizer(mesg," :\n");
            //Search for the REQUIRE Command
            String temp = t.nextToken();
            while ( !(temp.trim().equals(sDefault.trim())) ) temp = t.nextToken();
            temp = t.nextToken();
            System.out.println("MessageParser [getNextCommand]: returning:\n\t"+
                               temp);
            return temp;  //returns what the monitor wants
        }
        catch ( NoSuchElementException e )
        {
            return null;
        }
    }

    public boolean Login()
    {
        boolean success = false;
        try
        {
            try {
                    myPublicKey = dhExchange.getDHParmMakePublicKey("DHKey");
            } catch (Exception e) {
                    Util.DebugPrint(DbgSub.MESSAGE_PARSER, "Caught exception: " + e);
                    e.printStackTrace();
            }
            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "DH public Key: " + myPublicKey.toString());

            if (CType == 0) {
                    Execute(GetNextCommand(GetMonitorMessage(), ""));
                    Execute(GetNextCommand(GetMonitorMessage(), ""));

                    String passwordString = GetMonitorMessage();
                    if (passwordString.contains("PASSWORD")) {
                            String[] tmp = passwordString.split(" ");
                            COOKIE = tmp[2];
                            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "Monitor Cookie: " + COOKIE);
                            storage.WritePersonalData(GlobalData.GetPassword(), COOKIE);
                    }
                    Execute("HOST_PORT");
//                    Util.DebugPrint(DbgSub.MESSAGE_PARSER, GetMonitorMessage());
                    success = true;
            }
            if (CType == 1) {
                    Execute(GetNextCommand(GetMonitorMessage(), ""));
                    Execute(GetNextCommand(GetMonitorMessage(), ""));
                    Execute(GetNextCommand(GetMonitorMessage(), ""));
                    success = true;
                    IsVerified = 2;
            }
        }
        catch ( NullPointerException n )
        {
            System.out.println("MessageParser [Login]: null pointer error "+
                               "at login:\n\t"+n);
            success = false;
        }

        System.out.println("Success Value Login = "+success);
        return success;
    }

    //Handle Directives and Execute appropriate commands with <s>one</s> argument(s)
    public boolean Execute (String sentmessage, String arg)
    {
        boolean success = false;
        try
        {
            // TODO JE:  do we need this for CS694?
            if ( sentmessage.trim().equals("PARTICIPANT_HOST_PORT") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("SYNTHESIZE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("GET_CERTIFICATE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("TRADE_REQUEST") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("TRADE_RESPONSE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("WAR_DECLARE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("WAR_DEFEND") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("WAR_TRUCE_OFFER") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("WAR_TRUCE_RESPONSE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("WAR_STATUS") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("PLAYER_STATUS_CRACK") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            } else if ( sentmessage.trim().equals("PLAYER_MONITOR_PASSWORD_CRACK") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(sentmessage);
                success = true;
            }
        }
        catch ( IOException e )
        {
            System.out.println("IOError:\n\t"+e);
            success = false;
        }
        catch ( NullPointerException n )
        {
            System.out.println("Null Error has occured");
            success=false;
        }
        return success;
    }

    //Handle Directives and Execute appropriate commands
    public boolean Execute (String sentmessage)
    {
        boolean success = false; 
        try
        {
            if ( sentmessage.trim().equals("IDENT") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(IDENT);
                SendIt (sentmessage);

                success = true;
            }
            else if ( sentmessage.trim().equals("PASSWORD") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(PASSWORD);
                SendIt (sentmessage.trim());
                success = true;  
            }
            else if ( sentmessage.trim().equals("HOST_PORT") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(HOSTNAME);//hostname
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(String.valueOf(HOST_PORT));
                SendIt (sentmessage);
                success = true;                                  
            }
            else if ( sentmessage.trim().equals("ALIVE") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(COOKIE);
                SendIt (sentmessage);
                success = true;
            }
            else if ( sentmessage.trim().equals("QUIT") )
            {
                SendIt(sentmessage);
                success = true;
            }
            else if ( sentmessage.trim().equals("SIGN_OFF") )
            {
                SendIt(sentmessage);
                success = true;
            }
            else if ( sentmessage.trim().equals("GET_GAME_IDENTS") )
            {
                SendIt(sentmessage);
                success = true;
            }
            else if ( sentmessage.trim().equals("PLAYER_STATUS") )
            {
                SendIt(sentmessage);
                success = true;
            }
            else if ( sentmessage.trim().equals("RANDOM_PLAYER_HOST_PORT") )
            {
                SendIt(sentmessage);
                success = true;
            }
        }
        catch ( IOException e )
        {
            System.out.println("IOError:\n\t"+e);
            success = false;
        }
        catch ( NullPointerException n )
        {
            System.out.println("Null Error has occured");
            success=false;
        }
        return success;
    }

    public void SendIt (String message) throws IOException {
        try
        {
            System.out.println("MessageParser [SendIt]: sent:\n\t"+message);
            out.println(message);
            if ( out.checkError() == true ) throw (new IOException());
            out.flush();
            if ( out.checkError() == true ) throw (new IOException());
        }
        catch ( IOException e )
        {
        } //Bubble the Exception upwards
    } 

    //In future send parameters here so that diff commands are executed   
    public boolean ProcessExtraMessages()
    {
        boolean success = false;
        System.out.println("MessageParser [ExtraCommand]: received:\n\t"+
                           mesg.trim());

        if ( (mesg.trim().equals("")) || (mesg.trim().equals(null)) )
        {
            mesg = GetMonitorMessage();
            System.out.println("MessageParser [ExtraCommand]: received (2):\n\t"+
                               mesg.trim());
        }

        String id = GetNextCommand (mesg, "");

        if ( id == null ) // No Require, can Launch Free Form Commands Now  
        {
            if ( Execute("PLAYER_STATUS") ) //Check for Player Status
            {
                mesg = GetMonitorMessage();
                success = true;
                try
                {
                    storage.SaveResources(mesg);  //Save the data to a file
                    SendIt("SYNTHESIZE WEAPONS");        
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE COMPUTERS");        
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE VEHICLES");        
                    mesg = GetMonitorMessage();        
                    if ( Execute("PLAYER_STATUS") ) //Check for Player Status
                    {
                        mesg = GetMonitorMessage();
                        success = true;
                        storage.SaveResources(mesg);//Save the data to a file
                    }
                }
                catch ( IOException e )
                {
                }
            }
        }
        else
        {
            mesg = GetMonitorMessage();      
            System.out.println("MessageParser [ExtraCommand]: failed "+
                               "extra message parse");
        }
        return success;
    }

    public void MakeFreeFlowCommands() throws IOException {
    }

    public void HandleTradeResponse(String cmd) throws IOException {
    }

    public boolean IsTradePossible(String TradeMesg)
    {
        return false;
    }

    public int GetResource(String choice) throws IOException {
        return 0;
    }

    public void HandleWarResponse(String cmd) throws IOException{
    }

    public void DoTrade(String cmd)  throws IOException{
    }

    public void DoWar(String cmd)  throws IOException{
    }

    public void ChangePassword(String newpassword)
    {
        GetIdentification(); //Gives u the previous values of Cookie and Password
        String quer = "CHANGE_PASSWORD "+PASSWORD+" "+newpassword;
        UpdatePassword(quer,newpassword);
    }

    //Update Password
    //throws IOException
    public void UpdatePassword(String cmd, String newpassword)
    {
    }

    public void GetIdentification()
    {
    }                                      


    //Check whether the Monitor is Authentic
    public boolean Verify(String passwd,String chksum)
    {
        return false;
    }

    public boolean IsMonitorAuthentic(String MonitorMesg)
    {
        return false;
    }
}
