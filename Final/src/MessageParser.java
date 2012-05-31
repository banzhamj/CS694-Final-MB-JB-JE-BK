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
    String IDENT = "Skipper"; //TODO: change this
    String PASSWORD = "franco"; //TODO: change this
//    static String COOKIE ="bkuhn";
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
            sMesg = "";
            decrypt = temp;

            System.out.println("Received: " + decrypt);
            //After IDENT has been sent-to handle partially encrypted msg group
            while ( !(decrypt.trim().equals("WAITING:")) )
            {
                temp = in.readLine();
                if (IsEncrypted && temp != null) {
                        decrypt = myKarn.decrypt(temp);
                } else {
                        decrypt = temp;
                }
                if ( decrypt != null ) {
                    sMesg = sMesg.concat(decrypt);
                }
                sMesg = sMesg.concat("\n");
            } // sMesg now contains the Message Group sent by the Monitor
            decrypt = "";

        } catch (IOException e) {
                Util.DebugPrint(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: error "
                                + "in GetMonitorMessage:\n\t" + e + this);
                sMesg = "";
                e.printStackTrace();
        } catch (NullPointerException n) {
                sMesg = "";
                n.printStackTrace();
        } catch (NumberFormatException o) {
                Util.DebugPrint(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: number "
                                + "format error:\n\t" + o + this);
                sMesg = "";
                o.printStackTrace();
        } catch (NoSuchElementException ne) {
                Util.DebugPrint(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: no such "
                                + "element exception occurred:\n\t" + this);
                ne.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException ae) {
                Util.DebugPrint(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: AIOB "
                                + "EXCEPTION!\n\t" + this);
                sMesg = "";
                ae.printStackTrace();
        }
        Util.DebugPrint(DbgSub.MESSAGE_PARSER, "[getMonitorMessage (" + CType + ")]: " + sMesg);
        return sMesg;
    }

    // Handling Cookie and PPChecksum
    public String GetNextCommand(String mesg, String sCommand) {
        try {
            String sDefault = "REQUIRE";
            if (!(sCommand.equals("")))
                    sDefault = sCommand;
            t = new StringTokenizer(mesg, " :\n");
            // Search for the REQUIRE Command
            String temp = t.nextToken();
            while (!(temp.trim().equals(sDefault.trim())))
                    temp = t.nextToken();
            temp = t.nextToken();
            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "[getNextCommand]: " + temp);
            return temp; // returns what the monitor wants
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean Login()
    {
        boolean success = false;

        try {
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
                    //COOKIE = tmp[2];
                    GlobalData.SetCookie(tmp[2]);
                    Util.DebugPrint(DbgSub.MESSAGE_PARSER, "Monitor Cookie: " + tmp[2]);
                    storage.WritePersonalData(GlobalData.GetPassword(), GlobalData.GetCookie());
                }
                Execute("HOST_PORT");
                Util.DebugPrint(DbgSub.MESSAGE_PARSER, GetMonitorMessage());
                success = true;
            }
            if (CType == 1) {
                Execute(GetNextCommand(GetMonitorMessage(), ""));
                Execute(GetNextCommand(GetMonitorMessage(), ""));
                Execute(GetNextCommand(GetMonitorMessage(), ""));
                success = true;
                IsVerified = 2;
            }
        } catch (NullPointerException n) {
            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "[Login]: null pointer error "
                            + "at login:\n\t" + n);
            success = false;
            n.printStackTrace();
        }

        Util.DebugPrint(DbgSub.MESSAGE_PARSER, "Success Value Login = " + success);
        return success;
    }

    // Handle Directives and Execute appropriate transfer command
    public boolean Execute(String sentmessage, String to, String amount,
                    String from) {
        boolean success = false;
        try {
            if (sentmessage.trim().equals("TRANSFER_REQUEST")) {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(to);
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(amount);
                sentmessage = sentmessage.concat(" FROM ");
                sentmessage = sentmessage.concat(from);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            }
        } catch (IOException e) {
            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "Transfer Request IOError: " + e);
            success = false;
            e.printStackTrace();
        } catch (NullPointerException np) {
            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "Transfer Request Null Error" + np);
            success = false;
            np.printStackTrace();
        }

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
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("SYNTHESIZE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("GET_CERTIFICATE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("TRADE_REQUEST") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("TRADE_RESPONSE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("WAR_DECLARE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("WAR_DEFEND") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("WAR_TRUCE_OFFER") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("WAR_TRUCE_RESPONSE") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("WAR_STATUS") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("PLAYER_STATUS_CRACK") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            } else if ( sentmessage.trim().equals("PLAYER_MONITOR_PASSWORD_CRACK") ) {
                sentmessage = sentmessage.concat(" " + arg);
                SendIt(myKarn.encrypt(sentmessage));
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
                SendIt(myKarn.encrypt(sentmessage));

                success = true;
            }
            else if ( sentmessage.trim().equals("PASSWORD") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(PASSWORD);
                SendIt(myKarn.encrypt(sentmessage.trim()));
                success = true;  
            }
            else if ( sentmessage.trim().equals("HOST_PORT") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(HOSTNAME);//hostname
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(String.valueOf(HOST_PORT));
                SendIt(myKarn.encrypt(sentmessage));
                success = true;                                  
            }
            else if ( sentmessage.trim().equals("ALIVE") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(GlobalData.GetCookie());
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            }
            else if ( sentmessage.trim().equals("QUIT") )
            {
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            }
            else if ( sentmessage.trim().equals("SIGN_OFF") )
            {
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            }
            else if ( sentmessage.trim().equals("GET_GAME_IDENTS") )
            {
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            }
            else if ( sentmessage.trim().equals("PLAYER_STATUS") )
            {
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            }
            else if ( sentmessage.trim().equals("RANDOM_PLAYER_HOST_PORT") )
            {
                SendIt(myKarn.encrypt(sentmessage));
                success = true;
            }
        } catch (IOException e) {
            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "IOException: " + e);
            e.printStackTrace();
        } catch (NullPointerException np) {
            Util.DebugPrint(DbgSub.MESSAGE_PARSER, "Null Pointer Exception: " + np);
            np.printStackTrace();
        }

        return success;
    }

    public void SendIt(String message) throws IOException {
        try {
            out.println(message);
            if (out.checkError() == true)
                    throw (new IOException());
            out.flush();
            if (out.checkError() == true)
                    throw (new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    SendIt(myKarn.encrypt("SYNTHESIZE WEAPONS"));        
                    mesg = GetMonitorMessage();
                    SendIt(myKarn.encrypt("SYNTHESIZE COMPUTERS"));        
                    mesg = GetMonitorMessage();
                    SendIt(myKarn.encrypt("SYNTHESIZE VEHICLES"));        
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
