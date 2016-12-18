/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing;

import java.net.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.protocol.DataSource;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import videoconferencing.alpha.DBOperations;


/**
 * AVReceive22 to receive RTP transmission using the new RTP API.
 */
public class AVReceive3 extends Thread implements ReceiveStreamListener, SessionListener, 
	ControllerListener
{
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem menuItem,close;
    String sessions[] = null;
    RTPManager mgrs[] = null;
   // Vector playerWindows = null;
    public static Player p;
    boolean dataReceived = false;
    Object dataSync = new Object();
    private Player video_player;
    private Player audio_player;
    private int k=0;
    JPanel panel;
    JFrame jm;
    public static Boolean cl=false;
    private String ip="";
    DBOperations db=new DBOperations();
    public AVReceive3(String sessions[]) {
       // cl=false;
	this.sessions = sessions;
        jm=new JFrame();
        jm.setBounds( 0, 0, 650, 550 );
        panel=(JPanel) jm.getContentPane();
        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        menuItem = new JMenuItem("DownloadFile");
        close=new JMenuItem("Close");
        menu.add(menuItem);
        menu.addSeparator();
        menu.add(close);
        menuBar.setVisible(true);
        jm.setJMenuBar(menuBar);
        ip=sessions[0];
        //jm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //panel.setBounds(350,100, 400, 301);
        menuItem.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
                                        String pip=ip.substring(0, ip.indexOf("/"));
                                        ArrayList files=db.getfilesfromip(pip);
                                        
                                        new showFiles(files,pip);
                                        /*JFileChooser fc = new JFileChooser();
                                        int returnVal=fc.showOpenDialog(jm);
                                        if (returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
                                        String filename=fc.getSelectedFile().getName();
                                        String path=fc.getSelectedFile().getPath();
                                        System.out.println(filename+" and "+path);
                                        
                                        }
                                        else
                                        {
                                            System.out.println("cancelled");
                                        }*/
				}
			}
		);
        close.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
                                        jm.dispose();
                                        closesessions();
				}
			}
		);
       jm.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                jm.dispose();
                closesessions();
            }
        });
    }

    protected boolean initialize() {

        try {
	    InetAddress ipAddr;
	    SessionAddress localAddr = new SessionAddress();
	    SessionAddress destAddr;

	    mgrs = new RTPManager[sessions.length];
	   // playerWindows = new Vector();

	    SessionLabel session;

	    // Open the RTP sessions.
	    for (int i = 0; i < sessions.length; i++) {

	 	// Parse the session addresses.
		try {
		    session = new SessionLabel(sessions[i]);
		} catch (IllegalArgumentException e) {
		    System.err.println("Failed to parse the session address given: " + sessions[i]);
		    return false;
		}
                
                /*int ii=JOptionPane.showConfirmDialog(MainFrame.container,"Do you want to connect ","Select atleast one",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
                System.out.println(ii);
                if(ii==1)
                {
                close();
                }*/
		System.err.println("  - Open RTP session for: addr: " + session.addr + " port: " + session.port + " ttl: " + session.ttl);

		mgrs[i] = (RTPManager) RTPManager.newInstance();
		mgrs[i].addSessionListener(this);
		mgrs[i].addReceiveStreamListener(this);

		ipAddr = InetAddress.getByName(session.addr);

		if( ipAddr.isMulticastAddress()) {
		    // local and remote address pairs are identical:
		    localAddr= new SessionAddress( ipAddr,
						   session.port,
						   session.ttl);
		    destAddr = new SessionAddress( ipAddr,
						   session.port,
						   session.ttl);
		} else {
		    localAddr= new SessionAddress( InetAddress.getLocalHost(),
			  		           session.port);
                    destAddr = new SessionAddress( ipAddr, session.port);
		}
			
		mgrs[i].initialize( localAddr);

		// You can try out some other buffer size to see
		// if you can get better smoothness.
		BufferControl bc = (BufferControl)mgrs[i].getControl("javax.media.control.BufferControl");
		if (bc != null)
		    bc.setBufferLength(350);
		    
    		mgrs[i].addTarget(destAddr);
	    }

        } catch (Exception e){
            System.err.println("Cannot create the RTP Session: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

	// Wait for data to arrive before moving on.

	long then = System.currentTimeMillis();
	long waitingPeriod = 30000;  // wait for a maximum of 30 secs.

	try{
	    synchronized (dataSync) {
		while (!dataReceived) {
		    if (!dataReceived)
                    {
                        //cl=false;
                        System.err.println("  - Waiting for RTP data to arrive...");
                    }
		    dataSync.wait(1000);
		}
	    }
	} catch (Exception e) {}

	if (!dataReceived) {
	    System.err.println("No RTP data was received.");
	    close();
	    return false;
	}

        return true;
    }


    public boolean isDone() {
        //System.out.println(cl);
        return cl;
	//return playerWindows.size() == 0;
    }


    /**
     * Close the players and the session managers.
     */
    protected void close() {

	/*for (int i = 0; i < playerWindows.size(); i++) {
	    try {
		((PlayerWindow)playerWindows.elementAt(i)).close();
	    } catch (Exception e) {}
	}

	playerWindows.removeAllElements();

	// close the RTP session.
	for (int i = 0; i < mgrs.length; i++) {
	    if (mgrs[i] != null) {
                mgrs[i].removeTargets( "Closing session from AVReceive22");
                mgrs[i].dispose();
		mgrs[i] = null;
	    }
	}*/
    }

    
    protected void closesessions()
    {
            for (int pp = 0; pp < mgrs.length; pp++) {
                        if (mgrs[pp] != null) {
                        mgrs[pp].removeTargets( "Closing session from AVReceive22");
                        mgrs[pp].dispose();
                	mgrs[pp] = null;
                     }
                    }
                    //cl=true;
                    video_player.stop();
                    audio_player.stop();
                    video_player.close();
                    audio_player.close();
                    video_player.deallocate();
                    audio_player.deallocate();   
                    jm.dispose();
    }
    
   /* PlayerWindow find(Player p) {
	for (int i = 0; i < playerWindows.size(); i++) {
	    PlayerWindow pw = (PlayerWindow)playerWindows.elementAt(i);
	    if (pw.player == p)
		return pw;
	}
	return null;
    }


    PlayerWindow find(ReceiveStream strm) {
	for (int i = 0; i < playerWindows.size(); i++) {
	    PlayerWindow pw = (PlayerWindow)playerWindows.elementAt(i);
	    if (pw.stream == strm)
		return pw;
	}
	return null;
    }
*/

    /**
     * SessionListener.
     */
    public synchronized void update(SessionEvent evt) {
	if (evt instanceof NewParticipantEvent) {
	    Participant p = ((NewParticipantEvent)evt).getParticipant();
            /*int i=JOptionPane.showConfirmDialog(MainFrame.container,"Do you want to connect to "+p.getCNAME(),"Select atleast one",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
            System.out.println(i);
            if(i==1)
            {
                close();
            }*/
	    System.err.println("  - A new participant had just joined: " + p.getCNAME());
	}
    }

    

    /**
     * ReceiveStreamListener
     */
    public synchronized void update( ReceiveStreamEvent evt) {

	RTPManager mgr = (RTPManager)evt.getSource();
	Participant participant = evt.getParticipant();	// could be null.
	ReceiveStream stream = evt.getReceiveStream();
        
        //System.out.println(evt.getClass());
       
	if (evt instanceof RemotePayloadChangeEvent) {
     
	    System.err.println("  - Received an RTP PayloadChangeEvent.");
	    System.err.println("Sorry, cannot handle payload change.");
	    System.exit(0);

	}
        
	else if (evt instanceof NewReceiveStreamEvent) {

	    try {
		stream = ((NewReceiveStreamEvent)evt).getReceiveStream();
		DataSource ds = stream.getDataSource();
                //System.out.println(stream.getDataSource());
                Format format=null;
		// Find out the formats.
		RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
		if (ctl != null){
                   format = ctl.getFormat();
                   //System.out.println(format.toString());
		    System.err.println("  - Recevied new RTP stream: " + ctl.getFormat());
		} else
		    System.err.println("  - Recevied new RTP stream");
        
                if (format != null && format instanceof VideoFormat) {
			Player video = javax.media.Manager.createRealizedPlayer(ds);
			//video.start();
                    	video_player = video;
                        //video_player.start();
                        k=k+1;
                        //p=video_player;
		}
		else if (format != null && format instanceof AudioFormat) {
                        //javax.sound.midi.Receiver dsr =new Receiver();
                        Player audio=javax.media.Manager.createRealizedPlayer(ds);
			//JavaSoundDataSourceReader dsr = new JavaSoundDataSourceReader();
			//Player audio = dsr.open(ds);
			//audio.start();
			audio_player = audio;
                        //audio_player.getControlPanelComponent();
                        //audio_player.start();
                        k=k+1;
                       //p=audio_player;
		}
		if (participant == null)
		    System.err.println("The sender of this stream had yet to be identified.");
		else {
		    System.err.println("      The stream comes from: " + participant.getCNAME()); 
		}
                
                if((format instanceof AudioFormat) && (audio_player.getState()==300))
                {
		//audio_player.addControllerListener(this);
		//audio_player.realize();
                if (audio_player.getControlPanelComponent() != null)
                    jm.add("South", audio_player.getControlPanelComponent());
                System.err.println("audio");
                //PlayerWindow pw = new PlayerWindow(audio_player, stream);
		//playerWindows.addElement(pw);
                }
                if((format instanceof VideoFormat) && video_player.getState()==300)
                {
		//video_player.addControllerListener(this);
		//video_player.realize();
                if (video_player.getVisualComponent() != null)
                    jm.add("Center", video_player.getVisualComponent());
                //System.err.println("video");
                //PlayerWindow pw = new PlayerWindow(video_player, stream);
		//playerWindows.addElement(pw);
                }
               // System.out.println("hi"+audio_player.getState());
                //System.out.println("helo"+video_player.getState());
                /*if(video_player.getState()==300 && audio_player.getState()==300)
                {
                    System.out.println(audio_player.getState()+video_player.getState());
                 if(k==2)
                 */
                if(k==2)
                {
                    //Participant ppp = (evt).getParticipant();
                    int i=JOptionPane.showConfirmDialog(jm,"Connection Accepted.\r\nDo you want to continue ?","Connection Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
                    //System.out.println(i);
                    if(i==1)
                    {
                         closesessions();
                    }
                audio_player.addController(video_player);
                audio_player.start();
                jm.setVisible(true);
                }

		// Notify intialize() that a new stream had arrived.
		synchronized (dataSync) {
		    dataReceived = true;
		    dataSync.notifyAll();
		}

	    } catch (Exception e) {
		System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
                e.printStackTrace();
		return;
	    }
        
	}

	else if (evt instanceof StreamMappedEvent) {

	     if (stream != null && stream.getDataSource() != null) {
		DataSource ds = stream.getDataSource();
                /*Vector s=participant.getStreams();
                for(Iterator j=s.iterator();j.hasNext();)
                    System.out.println("hi "+j.next().toString());*/
		// Find out the formats.
		RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
		System.err.println("  - The previously unidentified stream ");
		if (ctl != null)
		    System.err.println("      " + ctl.getFormat());
		System.err.println("      had now been identified as sent by: " + participant.getCNAME());
                
            /*Vector vs=participant.getStreams();
            if(vs==null)
                System.out.println("dsda");// could be null.
            System.out.println("size is"+vs.size());// could be null.*/
          //  for(int i=0;i<=vs.size();i++){
        //System.out.println(vs.elementAt(i));
        }
	     }
             

	else if (evt instanceof ByeEvent) {
             //cl=false;
	     System.err.println("  - Got \"bye\" from: " + participant.getCNAME());
	     /*PlayerWindow pw = find(stream);
	     if (pw != null) {
		pw.close();
		playerWindows.removeElement(pw);
	     }*/
	}
        else if(evt instanceof ActiveReceiveStreamEvent)
        {
            System.out.println("event of ActiveReceiveStreamEvent");
        }
        else if(evt instanceof ApplicationEvent)
        {
            System.out.println("event of ApplicationEvent");
        }
        else if(evt instanceof InactiveReceiveStreamEvent)
        {
            System.out.println("event of InactiveReceiveStreamEvent");
        }
        else if(evt instanceof TimeoutEvent)
        {
            System.out.println("event of TimeoutEvent");
        }
        //System.out.println("none of the event happened");
    }


    /**
     * ControllerListener for the Players.
     */
    public synchronized void controllerUpdate(ControllerEvent ce) {

	Player p = (Player)ce.getSourceController();

	if (p == null)
	    return;

	// Get this when the internal players are realized.
	if (ce instanceof RealizeCompleteEvent) {
	    /*PlayerWindow pw = find(p);
	    if (pw == null) {
		// Some strange happened.
		System.err.println("Internal error!");
		System.exit(-1);
	    }
	    pw.initialize();
	    pw.setVisible(true);*/
	    //p.start();
	}

	if (ce instanceof ControllerErrorEvent) {
	    p.removeControllerListener(this);
	    /*PlayerWindow pw = find(p);
	    if (pw != null) {
		pw.close();	
		playerWindows.removeElement(pw);
	    }*/
	    System.err.println("AVReceive22 internal error: " + ce);
	}

    }


    /**
     * A utility class to parse the session addresses.
     */
    class SessionLabel {

	public String addr = null;
	public int port;
	public int ttl = 1;

	SessionLabel(String session) throws IllegalArgumentException {

	    int off;
	    String portStr = null, ttlStr = null;

	    if (session != null && session.length() > 0) {
		while (session.length() > 1 && session.charAt(0) == '/')
		    session = session.substring(1);

		// Now see if there's a addr specified.
		off = session.indexOf('/');
		if (off == -1) {
		    if (!session.equals(""))
			addr = session;
		} else {
		    addr = session.substring(0, off);
		    session = session.substring(off + 1);
		    // Now see if there's a port specified
		    off = session.indexOf('/');
		    if (off == -1) {
			if (!session.equals(""))
			    portStr = session;
		    } else {
			portStr = session.substring(0, off);
			session = session.substring(off + 1);
			// Now see if there's a ttl specified
			off = session.indexOf('/');
			if (off == -1) {
			    if (!session.equals(""))
				ttlStr = session;
			} else {
			    ttlStr = session.substring(0, off);
			}
		    }
		}
	    }

	    if (addr == null)
		throw new IllegalArgumentException();

	    if (portStr != null) {
		try {
		    Integer integer = Integer.valueOf(portStr);
		    if (integer != null)
			port = integer.intValue();
		} catch (Throwable t) {
		    throw new IllegalArgumentException();
		}
	    } else
		throw new IllegalArgumentException();

	    if (ttlStr != null) {
		try {
		    Integer integer = Integer.valueOf(ttlStr);
		    if (integer != null)
			ttl = integer.intValue();
		} catch (Throwable t) {
		    throw new IllegalArgumentException();
		}
	    }
	}
    }


    /**
     * GUI classes for the Player.
     */
    /*class PlayerWindow extends Frame {

	Player player;
	ReceiveStream stream;

	PlayerWindow(Player p, ReceiveStream strm) {
	    player = p;
	    stream = strm;
	}

	public void initialize() {
	    add(new PlayerPanel(player));
	}

	public void close() {
	    player.close();
	    setVisible(false);
	    dispose();
	}

	public void addNotify() {
	    super.addNotify();
	    pack();
	}
    }

*/
    /**
     * GUI classes for the Player.
     */
  /*  class PlayerPanel extends Panel {

	Component vc, cc;

	PlayerPanel(Player p) {
	    setLayout(new BorderLayout());
	    if ((vc = p.getVisualComponent()) != null)
            {
                System.out.println("in visual component");
		add("Center", vc);
            }
	    if ((cc = p.getControlPanelComponent()) != null)
            {
                System.out.println("in control component");
                add("South", cc);
            }
	}

	public Dimension getPreferredSize() {
	    int w = 0, h = 0;
	    if (vc != null) {
		Dimension size = vc.getPreferredSize();
		w = size.width;
		h = size.height;
	    }
	    if (cc != null) {
		Dimension size = cc.getPreferredSize();
		if (w == 0)
		    w = size.width;
		h += size.height;
	    }
	    if (w < 160)
		w = 160;
	    return new Dimension(w, h);
	}
    }

*/
   
    public static void main(String s[]) {
              String argv[]={"111.93.53.5/8000","111.93.53.5/8002"};
	if (argv.length == 0)
	    prUsage();

	AVReceive3 avReceive = new AVReceive3(argv);
        avReceive.start();
        if (!avReceive.initialize()) {
	    System.err.println("Failed to initialize the sessions.");
	    System.exit(-1);
	}
        	
	// Check to see if AVReceive22 is done.
        
	/*try {
	    while (!avReceive.isDone())
            {
                System.out.println("hhhhhhhh");
		Thread.sleep(1000);
            }
	} catch (Exception e) {}*/

	
        
    }
    public void run()
    {
        try
        {
    while(!(isDone()))
    {
       		Thread.currentThread().sleep(1000);
               // System.out.println("dsadad");
    }
   }
        catch(Exception e)
        {}
    System.err.println("Exiting AVReceive3");
    }

    static void prUsage() {
	System.err.println("Usage: AVReceive22 <session> <session> ...");
	System.err.println("     <session>: <address>/<port>/<ttl>");
	System.exit(0);
    }
}// end of AVReceive22