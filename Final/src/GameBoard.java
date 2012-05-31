import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GameBoard extends Applet {

	private static final long serialVersionUID = 944417097790033967L;
	public static String MONITOR_NAME = "helios.ececs.uc.edu";
    public static int MONITOR_PORT = 8160;
    public static int HOST_PORT = 20000 +(int)(Math.random()*1000);
    public static String name = "asdfname";
    public static String password = "asdfpass";

    JButton encryptButton;
    JButton identButton;
    JButton passwordButton;
    JButton hportButton;
    JButton aliveButton;
    JButton gameIdentsButton;
    JButton changePasswordButton;
    JButton statusButton;
    JButton signOffButton;
    JButton quitButton;
    JButton makeCertButton;
    JButton playerHostPortButton;
    JButton randomHostPortButton;
    JButton declareWarButton;
    JButton warTruceOfferButton;
    JButton warStatusButton;
    JButton warTruceResponseButton;
    JButton warDefendButton;
    JButton getCertButton;
    JButton tradeRequestButton;
    JButton tradeResponeButton;
    JButton synthesizeButton;
    JTextField identBlank1;
    JTextField identBlank2;
    JTextField passwordBlank;
    JTextField hportArg1;
    JTextField hPortArg2;
    JTextField certBlank1;
    JTextField certBlank2;
    JTextField passwordArg;
    JTextField playerHostPortArg;
    JTextField getCertArg;
    JComboBox truceResponseBox;
    JComboBox tradeResponseBox;
    JTextArea clientLog;
    JTextArea serverLog;
    JTextField portTextField;
    JButton startServerButton;
    JButton stopServerButton;
    JButton startClientButton;
    JButton stopClientButton;
    String result;
    
    ActiveClient ac;
    Server server;

    class Frame extends JFrame implements ActionListener {

		private static final long serialVersionUID = 1264061647495656599L;

		Frame() {
            setLayout(new BorderLayout());
        }

        void setUpControlPanel() {
            JPanel tempPanel = new JPanel();
            tempPanel = new JPanel();
            tempPanel.setLayout(new GridLayout(16, 3));
            
            // Add all components to the control panel
            tempPanel.add(encryptButton = new JButton("Start Encryption"));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(identButton = new JButton("IDENT"));
            tempPanel.add(identBlank1 = new JTextField(15));
            identBlank1.setEditable(false);
            tempPanel.add(identBlank2 = new JTextField(15));
            identBlank2.setEditable(false);
            tempPanel.add(passwordButton = new JButton("PASSWORD"));
            tempPanel.add(passwordBlank = new JTextField(15));
            passwordBlank.setEditable(false);
            tempPanel.add(statusButton = new JButton("PLAYER_STATUS"));
            tempPanel.add(hportButton = new JButton("HOST_PORT"));
            tempPanel.add(hportArg1 = new JTextField(15));
            tempPanel.add(hPortArg2 = new JTextField(15));
            hportArg1.setEditable(true);
            hPortArg2.setEditable(true);
            tempPanel.add(changePasswordButton = new JButton("CHANGE_PASSWORD"));
            tempPanel.add(passwordArg = new JTextField(15));
            tempPanel.add(quitButton = new JButton("QUIT"));
            tempPanel.add(aliveButton = new JButton("ALIVE"));
            tempPanel.add(gameIdentsButton = new JButton("GET_GAME_IDENTS"));
            tempPanel.add(signOffButton = new JButton("SIGN_OFF"));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(getCertButton = new JButton("GET_CERTIFICATE"));
            tempPanel.add(getCertArg = new JTextField(15));
            tempPanel.add(synthesizeButton = new JButton("SYNTHESIZE"));
            tempPanel.add(makeCertButton = new JButton("MAKE_CERTIFICATE"));
            tempPanel.add(certBlank1 = new JTextField(15));
            tempPanel.add(certBlank2 = new JTextField(15));
            certBlank1.setEditable(false);
            certBlank2.setEditable(false);
            tempPanel.add(playerHostPortButton = new JButton("PLAYER_HOST_PORT"));
            tempPanel.add(playerHostPortArg = new JTextField(15));
            tempPanel.add(randomHostPortButton = new JButton("RANDOM_PLAYER_HOST_PORT"));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(tradeRequestButton = new JButton("TRADE_REQUEST"));
            tempPanel.add(tradeResponeButton = new JButton("TRADE_RESPONSE"));
            tempPanel.add(tradeResponseBox = new JComboBox());
            tradeResponseBox.addItem("Accept");
            tradeResponseBox.addItem("Decline");
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(new JLabel("  "));
            tempPanel.add(declareWarButton = new JButton("WAR_DECLARE"));
            tempPanel.add(warDefendButton = new JButton("WAR_DEFEND"));
            tempPanel.add(warStatusButton = new JButton("WAR_STATUS"));
            tempPanel.add(warTruceOfferButton = new JButton("WAR_TRUCE_OFFER"));
            tempPanel.add(warTruceResponseButton = new JButton("WAR_TRUCE_RESPONSE"));
            tempPanel.add(truceResponseBox = new JComboBox());
            truceResponseBox.addItem("Accept");
            truceResponseBox.addItem("Decline");
            add("South", tempPanel);
            
            // Add client and server log windows
            tempPanel = new JPanel();
            tempPanel.setLayout(new BorderLayout());
            tempPanel.add("Center", new JScrollPane(clientLog = new JTextArea(30,40)));
            tempPanel.add("South", new JLabel("Active Client Log\n\n\n", 0));
            add("West", tempPanel);
            tempPanel = new JPanel();
            tempPanel.setLayout(new BorderLayout());
            tempPanel.add("Center", new JScrollPane(serverLog = new JTextArea(30,40)));
            tempPanel.add("South", new JLabel("Passive Server Log\n\n\n", 0));
            add("East", tempPanel);
            
            JPanel topPanel = new JPanel();
            topPanel.setLayout( new GridLayout( 3, 2 ) );
            topPanel.add( new JLabel( "Host port" ) );
            topPanel.add( portTextField = new JTextField( "20000" ) );
            topPanel.add( startServerButton = new JButton( "Start Server" ) );
            topPanel.add( stopServerButton = new JButton( "Stop Server" ) );
            topPanel.add( startClientButton = new JButton( "Start Client" ) );
            topPanel.add( stopClientButton = new JButton( "Stop Client" ) );
            add("North", topPanel);
            
            // Add action listeners for all buttons
            encryptButton.addActionListener(this);
            identButton.addActionListener(this);
            passwordButton.addActionListener(this);
            hportButton.addActionListener(this);
            aliveButton.addActionListener(this);
            gameIdentsButton.addActionListener(this);
            changePasswordButton.addActionListener(this);
            statusButton.addActionListener(this);
            signOffButton.addActionListener(this);
            quitButton.addActionListener(this);
            makeCertButton.addActionListener(this);
            playerHostPortButton.addActionListener(this);
            randomHostPortButton.addActionListener(this);
            declareWarButton.addActionListener(this);
            warTruceOfferButton.addActionListener(this);
            warStatusButton.addActionListener(this);
            warTruceResponseButton.addActionListener(this);
            warDefendButton.addActionListener(this);
            getCertButton.addActionListener(this);
            tradeRequestButton.addActionListener(this);
            tradeResponeButton.addActionListener(this);
            synthesizeButton.addActionListener(this);
            startServerButton.addActionListener(this);
            stopServerButton.addActionListener(this);
            startClientButton.addActionListener(this);
            stopClientButton.addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent e) {
            if ( e.getSource() == encryptButton ) {
                // TODO
            } else if ( e.getSource() == identButton ) {
                ac.Execute("IDENT");
            } else if ( e.getSource() == passwordButton ) {
                ac.Execute("PASSWORD");
            } else if ( e.getSource() == hportButton ) {
                ac.Execute("HOST_PORT");
            } else if ( e.getSource() == aliveButton ) {
                ac.Execute("ALIVE");
            } else if ( e.getSource() == gameIdentsButton ) {
                ac.Execute( "GET_GAME_IDENTS" );
            } else if ( e.getSource() == changePasswordButton ) {
                ac.ChangePassword(passwordArg.getText());
            } else if ( e.getSource() == statusButton ) {
                ac.Execute( "PLAYER_STATUS" );
            } else if ( e.getSource() == signOffButton ) {
                ac.Execute( "SIGN_OFF" );
            } else if ( e.getSource() == quitButton ) {
                ac.Execute( "QUIT" );
            } else if ( e.getSource() == makeCertButton ) {
                // TODO
            } else if ( e.getSource() == playerHostPortButton ) {
                // TODO
            } else if ( e.getSource() == randomHostPortButton ) {
                ac.Execute( "RANDOM_PLAYER_HOST_PORT" );
            } else if ( e.getSource() == declareWarButton ) {
                // TODO
            } else if ( e.getSource() == warTruceOfferButton ) {
                // TODO
            } else if ( e.getSource() == warStatusButton ) {
                // TODO
            } else if ( e.getSource() == warTruceResponseButton ) {
                ac.Execute("WAR_TRUCE_RESPONSE", truceResponseBox.getSelectedItem().toString());
            } else if ( e.getSource() == warDefendButton ) {
                // TODO
            } else if ( e.getSource() == getCertButton ) {
                ac.Execute("GET_CERTIFICATE", getCertArg.getText());
            } else if ( e.getSource() == tradeRequestButton ) {
                // TODO
            } else if ( e.getSource() == tradeResponeButton ) {
                ac.Execute("TRADE_RESPONSE", tradeResponseBox.getSelectedItem().toString());
            } else if ( e.getSource() == synthesizeButton ) {
                // TODO
            } else if ( e.getSource() == startServerButton ) {
                server.start();
            } else if ( e.getSource() == stopServerButton ) {
                //TODO
            } else if ( e.getSource() == startClientButton ) {
                ac.start();
            } else if ( e.getSource() == stopClientButton ) {
                //TODO
            }
        }
    }

    public void init() {
        Frame frame = new Frame();
        frame.setUpControlPanel();
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        ac = new ActiveClient( MONITOR_NAME, MONITOR_PORT, HOST_PORT, 0, name, password );
        server = new Server( HOST_PORT, HOST_PORT, name, password );
    }
}