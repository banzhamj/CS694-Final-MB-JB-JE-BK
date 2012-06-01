import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public class MessageParser
{
    Util debug;
    //Monitor Handling Declarations
    int COMMAND_LIMIT = 25;
    public  int CType;
    public static String HOSTNAME;
    PrintWriter out = null; 
    BufferedReader in = null; 
    
    KarnBufferedReader karnIn = null;
    KarnPrintWriter karnOut = null;
    
    PrintWriter plainOut = null;
    BufferedReader plainIn = null;
    
    String mesg,sentmessage;
    String filename;
    StringTokenizer t;
    String IDENT;
    String PASSWORD;
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
    boolean IsEncrypted = false;

    // Transfer stuff
    String ROUNDS = "20";

    //Directives
    String require;
    String result;
    String error;
    String comment;
    String ppChecksum;

    String lastCommandSent;

    public MessageParser()
    {
        debug = new Util(null);
        filename = "passwd.dat";
        storage = new PermanentStorage(this);
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file
    }

    public MessageParser(String ident, String password)
    {
        debug = new Util(null);
        filename = ident+".dat";
        storage = new PermanentStorage(this, ident);
        PASSWORD = password;
        IDENT = ident;
        GetIdentification(); // Gets Password and Cookie from 'passwd.dat' file
    }

    public String GetMonitorMessage()
    {
        String sMesg="";
        try
        {
            String temp;
            sMesg = "";
            boolean moreDirectives = true;
            boolean foundRequire = false;
            boolean foundResult = false;
            boolean foundError = false;
            boolean foundPPChecksum = false;
            boolean foundComment = false;
           
            //After IDENT has been sent-to handle partially encrypted msg group
            while ( moreDirectives ) {
                temp = in.readLine();
                String directive = null;
                System.out.println("Received: " + temp);

                if ( temp != null && !temp.equals("\r") ) {
                    BetterStringTokenizer st = new BetterStringTokenizer(temp);
                    if ( st.hasMoreTokens() ) {
                        directive = st.nextToken();
                        if ( directive.equalsIgnoreCase("WAITING:") ) {
                            moreDirectives = false;
                        } else if ( directive.equalsIgnoreCase("REQUIRE:") ) {
                            require = st.GetRemaining();
                            if ( require != null && !require.equals("") ) {
                                foundRequire = true;
                            }
                        } else if ( directive.equalsIgnoreCase("RESULT:") ) {
                            result = st.GetRemaining();
                            if ( result.equalsIgnoreCase("QUIT") || result.equalsIgnoreCase("SIGN_OFF") ) {
                                moreDirectives = false;
                            }
                            if ( result != null && !result.equals("")) {
                                foundResult = true;
                            }
                        } else if ( directive.equalsIgnoreCase("COMMENT:") ) {
                            comment = st.GetRemaining();
                            if ( comment != null && !comment.equals("") ) {
                                foundComment = true;
                                //Make sure if we get a timeout from the monitor (which only shows up as a comment), we stop trying to get directives
                                if ( comment.contains("Timeout") ) {
                                    moreDirectives = false;
                                }
                            }
                        } else if ( directive.equalsIgnoreCase("PLAYER_PASSWORD_CHECKSUM:") ) {
                            ppChecksum = st.GetRemaining();
                            if ( ppChecksum != null && !ppChecksum.equals("") ) {
                                foundPPChecksum = true;
                            }
                        } else if ( directive.equalsIgnoreCase("COMMAND_ERROR:") ) {
                            error = st.GetRemaining();
                            if ( error != null && !error.equals("") ) {
                                foundError = true;
                            }
                        } else {
                            System.out.println("Unknown Directive: " + directive);
                            moreDirectives = false;
                        }
                    }
                    sMesg = sMesg.concat(temp);
                }
                sMesg = sMesg.concat("\n");
            } // sMesg now contains the Message Group sent by the Monitor
            temp = "";
            if ( !foundRequire ) {
                require = "none";
            }
            if ( !foundResult ) {
                result = "none";
            }
            if ( !foundComment ) {
                comment = "none";
            }
            if ( !foundError ) {
                error = "none";
            }
            if ( !foundPPChecksum ) {
                ppChecksum = "none";
            }

        } catch (IOException e) {
                debug.Print(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: error "
                                + "in GetMonitorMessage:\n\t" + e + this);
                sMesg = "";
                e.printStackTrace();
        } catch (NullPointerException n) {
                sMesg = "";
                debug.Print(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: NULL POINTER");
                n.printStackTrace();
        } catch (NumberFormatException o) {
                debug.Print(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: number "
                                + "format error:\n\t" + o + this);
                sMesg = "";
                o.printStackTrace();
        } catch (NoSuchElementException ne) {
                debug.Print(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: no such "
                                + "element exception occurred:\n\t" + this);
                ne.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException ae) {
                debug.Print(DbgSub.MESSAGE_PARSER, "[getMonitorMessage]: AIOB "
                                + "EXCEPTION!\n\t" + this);
                sMesg = "";
                ae.printStackTrace();
        } 
        debug.Print(DbgSub.MESSAGE_PARSER, "[getMonitorMessage (" + CType + ")]: " + sMesg);
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
            debug.Print(DbgSub.MESSAGE_PARSER, "[getNextCommand]: " + temp);
            return temp; // returns what the monitor wants
        } catch (NoSuchElementException e) {
            // commenting out following line, it appears legit in some cases
            //e.printStackTrace();
            return null;
        }
    }
    
    public PlayerCertificate getRMICertificate(String ident)
    {
        PlayerCertificate pc = null;
        try {
            String server = "rmi://" + HOSTNAME + "/CertRegistry";
            CertRemote r = (CertRemote)(Naming.lookup(server));
            pc = r.getCert(ident);
        } catch (NotBoundException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return pc;
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
                SendIt(sentmessage);
                success = true;
            }
        } catch (IOException e) {
            debug.Print(DbgSub.MESSAGE_PARSER, "Transfer Request IOError: " + e);
            success = false;
            e.printStackTrace();
        } catch (NullPointerException np) {
            debug.Print(DbgSub.MESSAGE_PARSER, "Transfer Request Null Error" + np);
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
            e.printStackTrace();
            success = false;
        }
        catch ( NullPointerException n )
        {
            System.out.println("Null Error has occured");
            n.printStackTrace();
            success=false;
        }
        return success;
    }

    //Handle Directives and Execute appropriate commands
    public boolean Execute (String command)
    {
        String sentmessage = command.trim();
        
        boolean success = false; 
        try
        {            
            if ( sentmessage.equals("IDENT") )
            {
                if ( this.CType == 0 ) {
                    PlayerCertificate monCert = getRMICertificate("MONITOR");
                    RSA myKey = new RSA(256);
                    BigInteger m = myKey.publicKey().getModulus();
                    String myHalf = monCert.getPublicKey().encrypt(m).toString(32);
                    
                    SendIt("IDENT " + IDENT + " " + myHalf);
                    
                    String response = in.readLine();
                    String number = response.split("\\s+")[2];                    
                    
                    BigInteger srvHalf = myKey.decryptNum(new BigInteger(number, 32));
                    byte mine[] = myKey.publicKey().getModulus().toByteArray();
                    byte monitor[] = srvHalf.toByteArray();
                    
                    int keySize = 512;
                    
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(keySize/8);
                    
                    for(int i=0; i < keySize/16; i++){
                        bos.write(monitor[i]);
                        bos.write(mine[i]);
                    }
                    BigInteger sharedSecret = new BigInteger(1, bos.toByteArray());
                    
                    karnIn = new KarnBufferedReader(plainIn, sharedSecret);
                    try {
                        karnOut = new KarnPrintWriter(plainOut, true, sharedSecret);
                    } catch (NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                    }
                    
                    in = karnIn;
                    out = karnOut;                    
                } else {                     
                    SendIt("IDENT " + IDENT);                    
                }            
                
                success = true;
            }
            else if ( sentmessage.trim().equals("PASSWORD") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(PASSWORD);
                SendIt(sentmessage.trim());
                success = true;  
            }
            else if ( sentmessage.trim().equals("HOST_PORT") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(HOSTNAME);//hostname
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(String.valueOf(HOST_PORT));
                SendIt(sentmessage);
                success = true;                                  
            }
            else if ( sentmessage.trim().equals("ALIVE") )
            {
                sentmessage = sentmessage.concat(" ");
                sentmessage = sentmessage.concat(GlobalData.GetCookie());
                SendIt(sentmessage);
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
        } catch (IOException e) {
            debug.Print(DbgSub.MESSAGE_PARSER, "IOException: " + e);
            e.printStackTrace();
        } catch (NullPointerException np) {
            debug.Print(DbgSub.MESSAGE_PARSER, "Null Pointer Exception: " + np);
            np.printStackTrace();
        }

        if ( success == true ) {
            StringTokenizer st = new StringTokenizer(sentmessage);
            lastCommandSent = st.nextToken();
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
            // commenting following line out, it's legit for a QUIT command
            //e.printStackTrace();
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
                    SendIt("SYNTHESIZE WEAPONS");        
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE COMPUTERS");        
                    mesg = GetMonitorMessage();
                    SendIt("SYNTHESIZE VEHICLES");        
                    mesg = GetMonitorMessage();        
                    if ( Execute("PLAYER_STATUS")) //Check for Player Status
                    {
                        mesg = GetMonitorMessage();
                        success = true;
                        storage.SaveResources(mesg);//Save the data to a file
                    }
                }
                catch ( IOException e )
                {
                	e.printStackTrace();
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
    	// TODO
    }

    public void HandleTradeResponse(String cmd) throws IOException {
    	// TODO
    }

    public boolean IsTradePossible(String TradeMesg)
    {
    	// TODO
        return false;
    }

    public int GetResource(String choice) throws IOException {
    	// TODO
        return 0;
    }

    public void HandleWarResponse(String cmd) throws IOException{
    	// TODO
    }

    public void DoTrade(String cmd)  throws IOException{
    	// TODO
    }

    public void DoWar(String cmd)  throws IOException{
    	// TODO
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
        if ( Execute(cmd) ) {
            String msg = GetMonitorMessage();
            String oldPassword = GlobalData.GetPassword();
            String[] tmp = msg.split(" ");
            if ( tmp[0].trim().equals("RESULT:") ) {
                String newCookie = tmp[2];
                GlobalData.SetCookie(newCookie);
                GlobalData.SetPassword(newpassword);
                storage.WritePersonalData(newpassword, newCookie);
                debug.Print(DbgSub.MESSAGE_PARSER, "[UpdatePassword] Changed password from " + oldPassword + " to " + newpassword);
            }
        }
    }

    public void GetIdentification()
    {
        boolean pass = false;
        boolean cook = false;

        try {
            FileInputStream dataFile = new FileInputStream(IDENT + ".dat");

            DataInputStream dataIn = new DataInputStream(dataFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                            dataIn));
            String lineIn;

            while ((lineIn = reader.readLine()) != null) {
                    if (pass) {
                            GlobalData.SetPassword(lineIn.trim());
                            pass = false;
                    }
                    if (cook) {
                            GlobalData.SetCookie(lineIn.trim());
                            cook = false;
                    }
                    if (lineIn.contains("PASSWORD")) {
                            pass = true;
                    }
                    if (lineIn.contains("COOKIE")) {
                            cook = true;
                    }
            }
            dataFile.close();
        } catch (Exception e) {
            debug.Print(DbgSub.MESSAGE_PARSER, "Error: " + e.getMessage());
        }
        debug.Print(DbgSub.MESSAGE_PARSER, "Read from file: " + IDENT + ".dat.  Password = "
                        + GlobalData.GetPassword() + " COOKIE = " + GlobalData.GetCookie());
    }                                      


    //Check whether the Monitor is Authentic
    public boolean Verify(String passwd,String chksum)
    {
    	// TODO
        return false;
    }

    public boolean IsMonitorAuthentic(String MonitorMesg)
    {
    	// TODO
        return false;
    }
}
