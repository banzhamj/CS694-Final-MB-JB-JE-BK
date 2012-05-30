
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
    JLabel title;
    String result;
    
    ActiveClient ac;

    class Frame extends JFrame implements ActionListener {

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
            
            // Add window title
            add("Center", title = new JLabel("Command & Control", 0));
            title.setFont(new Font("TimesRoman", 1, 20));
            
            // Add client and server log windows
            tempPanel = new JPanel();
            tempPanel.setLayout(new BorderLayout());
            tempPanel.add("Center", new JScrollPane(clientLog = new JTextArea(18,25)));
            tempPanel.add("South", new JLabel("Active Client Log\n\n\n", 0));
            add("West", tempPanel);
            tempPanel = new JPanel();
            tempPanel.setLayout(new BorderLayout());
            tempPanel.add("Center", new JScrollPane(serverLog = new JTextArea(18,25)));
            tempPanel.add("South", new JLabel("Passive Server Log\n\n\n", 0));
            add("East", tempPanel);
            
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
        }
        
        public void actionPerformed(ActionEvent e) {
            if ( e.getSource() == encryptButton ) {
                // TODO
            } else if ( e.getSource() == identButton ) {
                // TODO
            } else if ( e.getSource() == passwordButton ) {
                // TODO
            } else if ( e.getSource() == hportButton ) {
                // TODO
            } else if ( e.getSource() == aliveButton ) {
                // TODO
            } else if ( e.getSource() == gameIdentsButton ) {
                ac.Execute( "GET_GAME_IDENTS" );
            } else if ( e.getSource() == changePasswordButton ) {
                // TODO
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
                // TODO
            } else if ( e.getSource() == warDefendButton ) {
                // TODO
            } else if ( e.getSource() == getCertButton ) {
                // TODO
            } else if ( e.getSource() == tradeRequestButton ) {
                // TODO
            } else if ( e.getSource() == tradeResponeButton ) {
                // TODO
            } else if ( e.getSource() == synthesizeButton ) {
                // TODO
            }
        }
    }

    public void init() {
        Frame frame = new Frame();
        frame.setUpControlPanel();
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        ac = new ActiveClient();
    }
}
