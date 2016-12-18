package Whiteboarding.main;

import Whiteboarding.main.*;
import Whiteboarding.network.*;
import Whiteboarding.userInterface.*;
import Whiteboarding.model.*;

import java.util.Comparator;
import java.util.Arrays;
import java.util.Vector;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import videoconferencing.LoginFrame;
import videoconferencing.alpha.DBOperations;

/**
 * <p>The SessionBrowser has a simple GUI which displays a list of
 * sessions currently hosted on the server. Through
 * the GUI, the user may select an existing session to join or
 * may request that a new session be created, optionally using
 * data from a previously saved local file.</p>
 * 
 * @author idb20
 * @author David Morgan
 */

public class SessionBrowser extends JFrame implements ConnectionOwner
{
    DBOperations db=new DBOperations();
  /**
   * <p>{@link Connection} to the SessionBrowserServer.</p>
   */

  private Connection connection;

  /**
   * <p>Vector of {@link SessionInfo} describing currently hosted Sessions.</p>
   */

  private Vector openSessions;

  /**
   * <p>Default address of the server.</p>
   */

  private String defaultAddress;


  /* GUI components */

  private JPanel mainPanel;
  private JLabel listLabel;
  private JButton refreshButton;
  private JButton joinButton;
  private JButton createDialogButton;
  private JScrollPane sessionListScrollPane;
  private JList sessionList;
  private JTextField userName;
  private JLabel createLabel;
  private JButton colorButton;
  private int k=0;

  /**
   * <p>Create and display a new SessionBrowser.</p>
   */

  public SessionBrowser()
  {
    initialiseComponents();
    openSessions = new Vector();
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    assert(connection==null);

    try
    {
      FileReader file=new FileReader("server.cfg");
      BufferedReader reader=new BufferedReader(file);

      while(true)
      {
	String address=reader.readLine();

	if(address==null) break;

	connect(address);

	if(connection!=null) break;
      }

      reader.close();
    }
    catch (IOException e)
    {
        String ip=db.getIPforwhiteboard();
      // Fail silently; this means the file is missing
        //System.out.println("localhost");
        if(ip==null)
      connect("localhost");
        else
            connect(ip);
    }
     
    
    if (connection==null)
    {
        k=1;
      JOptionPane.showMessageDialog(
	this,  "Unable to find the server.\r\n"+
	"Please try again later.\r\nOr Create your own Server.",
	"Could not locate server", JOptionPane.ERROR_MESSAGE);
      this.dispose();
      
    //  System.exit(0);
    }
    if(k==0)
    {
    System.out.println("hello");
    sessionList.setEnabled(true);
    sessionList.grabFocus();
    refreshButton.setEnabled(true);
    createDialogButton.setEnabled(true);
    }
    k=0;
  }
    
  public void connect(String address)
  {
    try
    {
      Socket socket=new Socket(address, SessionBrowserServer.BASE_PORT);
      System.out.println(address);
      System.out.println(SessionBrowserServer.BASE_PORT);
      connection = new Connection(socket,
				  this);
    }
    catch(IOException e)
    {
      // Fail silently; failure is indicated by a null connection
    }
  }
 
  /**
   * <p>Terminates the connection with the remote
   * SessionBrowserServer.</p>
   */

  public void disconnect()
  {
    if(connection!=null)
    {
      connection.close();
      connection=null;
    
      openSessions.clear();
      populateSessionList();
            
      sessionList.setEnabled(false);
      refreshButton.setEnabled(false);
      createDialogButton.setEnabled(false);
    }
  }

  /**
   * <p>This function is called when the {@link Connection} to the
   * SessionBrowserServer has been closed.</p>
   * 
   */
  public void close(Connection connection)
  {
      JOptionPane.showMessageDialog(
      this,  "Connection to server lost.\n\n"+
      "Please Connect again to join further sessions.",
      "Connection to server lost", JOptionPane.INFORMATION_MESSAGE);

    disconnect();
  }
      
  /**
   * <p>Request an up-to-date version of the session list from the
   * {@link SessionBrowserServer}.</p>
   */

  private void requestSessionList()
  {
    try
    {
      connection.sendMessage(new Message(Message.REQUEST_SESSION_LIST, null));
    }
    catch(IOException e)
    {
      disconnect();
    }
  }
   
  /**
   * <p>Handle {@link Message}s from the {@link SessionBrowserServer}.</p>
   */

  public void receiveRemoteMessage(Message message)
  {
    System.out.println(
      "SessionBrowser received a message of type " + message.getTypeString());
    
    switch(message.getType())
    {
      case Message.SESSION_LIST:
      {
	SessionInfo[] newList=(SessionInfo[])message.getData();

	openSessions.clear();
	for (int i=0; i<newList.length;i++)
	{
	  openSessions.add(newList[i]);
	}
	populateSessionList();

	break;
      }

      case Message.SESSION_LIST_UPDATE:
      {
	Object[] data=(Object[])message.getData();
	int index=((Integer)data[0]).intValue();
	SessionInfo info=(SessionInfo)data[1];

	if(openSessions.size()<index+1)
	{
	  openSessions.add(info);
	}
	else
	{
	  openSessions.set(index, info);
	}

	populateSessionList();

	break;
      }

      default:
      {
	System.err.println("SessionBrowser received unexpected Message of type: " +
			   message.getTypeString());
      }
    }
  }

  /**
   * <p>Join a remote {@link Session} by spawning a local {@link Session}
   * connected to its {@link SessionServer}.</p>
   */
    
  public void joinSession()
  {
    SessionInfo info=(SessionInfo)sessionList.getSelectedValue();

    boolean readOnly=false;

    if(info.getReadOnlyPassword()!=null || info.getReadWritePassword()!=null)
    {
      PasswordDialog passwordDialog=new PasswordDialog(
	this, "Please enter the password to join");

      if(passwordDialog.showDialog())
      {
	String text=passwordDialog.getPass();
	
	if(!text.equals(info.getReadWritePassword()))
	{
	  if(text.equals(info.getReadOnlyPassword()))
	  {
	    readOnly=true;
	  }
	  else
	  {
	    JOptionPane.showMessageDialog(
	      this, "The password you entered was incorrect.",
	      "Password Incorrect",
	      JOptionPane.ERROR_MESSAGE);

	    return;
	  }
	}
      }
      else
      {
	return;
      }
    }

    try
    {
      String name=userName.getText();
      if(name.length()==0) name=LoginFrame.un+"-user";

      new Session(this, info, null, readOnly, name, colorButton.getBackground());
    }
    catch (IOException e)
    {
      JOptionPane.showMessageDialog(
	this,  "Unable to connect to Session.\n\n"+
	"This is either a problem with the server or with local firewall"+
	"settings.",
	"Could not connect to Session", JOptionPane.ERROR_MESSAGE);

      e.printStackTrace();
      
      return;
    }
    catch (Exception e)
    {
      JOptionPane.showMessageDialog(
	this,  "Unable to start Session Viewer.\n\n"+
	"This is a problem of some kind internal to Server.",
	"Could not start Session Viewer", JOptionPane.ERROR_MESSAGE);

      e.printStackTrace();
      
      return;
    }
  }
    
  public void exitForm(WindowEvent event)
  {
    disconnect();
    this.dispose();
  }
    
  public void showForm(ComponentEvent event)
  {
  }

  /**
   * <p>Populate the session list with the session information, in sorted
   * order.</p>
   */

  private void populateSessionList()
  {
    Object[] sessionInfo=openSessions.toArray();

    Arrays.sort(sessionInfo, new Comparator()
      {
	public int compare(Object o1, Object o2)
	{
	  return o1.toString().compareToIgnoreCase(o2.toString());
	}
      });

    sessionList.setListData(sessionInfo);
  }
	      
  /**
   * <p>Create and configure GUI components.</p>
   */

  private void initialiseComponents()
  {
    setTitle("WhiteBoard Session");
    setName("WhiteBoard Session");

    // Create components

    mainPanel = new JPanel();      
    mainPanel.setBorder(new EtchedBorder());
    mainPanel.setFocusable(false);

    listLabel = new JLabel("Available sessions:", JLabel.CENTER);
    mainPanel.add(listLabel);

    refreshButton = new JButton("Refresh List");
    refreshButton.setToolTipText("Refresh the list of available sessions");
    refreshButton.setEnabled(false);
    mainPanel.add(refreshButton);

    joinButton = new JButton("Join with name:");
    joinButton.setToolTipText("Join the selected Session");
    joinButton.setEnabled(false);
    mainPanel.add(joinButton);

    createLabel=new JLabel("...or, create your own:", JLabel.CENTER);
    mainPanel.add(createLabel);

    createDialogButton = new JButton("Create Your Own Session");
    createDialogButton.setToolTipText("Create a new Session");
    createDialogButton.setEnabled(false);
    mainPanel.add(createDialogButton);

    sessionListScrollPane = new JScrollPane();
    sessionListScrollPane.setFocusable(false);

    sessionList = new JList();
    sessionList.setBorder(
      new BevelBorder(BevelBorder.LOWERED));
    sessionList.setToolTipText(
      "A list of currently active Sessions");
    sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    sessionList.setPrototypeCellValue((Object)"Probably long enough for Session titles");
    sessionList.setForeground(Color.black);
    sessionList.setAutoscrolls(false);
    sessionList.setEnabled(false);
    sessionListScrollPane.setViewportView(sessionList);

    mainPanel.add(sessionListScrollPane);
    
    userName=new JTextField(LoginFrame.un+"-user");
    userName.setHorizontalAlignment(JTextField.CENTER);
    mainPanel.add(userName);

    colorButton=new JButton();

    float h=(float)(Math.random());
    float s=1;
    float b=0.6f;
    
    colorButton.setBackground(Color.getHSBColor(h, s, b));

    mainPanel.add(colorButton);

    // Set layout managers

    getContentPane().add(mainPanel);

    mainPanel.setLayout(new LayoutManagerAdapter(320, 320, 320, 320)
      {
	public void layoutContainer(Container parent)
	{
	  final int border=4;
	  final int hborder=border/2;

	  listLabel.setBounds(
	    border,
	    border,
	    parent.getWidth()-2*border,
	    18);

	  sessionListScrollPane.setBounds(
	    border,
	    listLabel.getY()+listLabel.getHeight()+border,
	    parent.getWidth()-2*border,
	    parent.getHeight()-5*border-2*22-18);

	  joinButton.setBounds(
	    border,
	    sessionListScrollPane.getY()+sessionListScrollPane.getHeight()+border,
	    parent.getWidth()/2-border,
	    22);

	  userName.setBounds(
	    joinButton.getX()+joinButton.getWidth()+hborder,
	    joinButton.getY(),
	    parent.getWidth()/2-5*hborder-22,
	    22);

	  colorButton.setBounds(
	    parent.getWidth()-22-border,
	    joinButton.getY(),
	    22,
	    22);

	  createDialogButton.setBounds(
	    border,
	    userName.getY()+userName.getHeight()+border,
	    parent.getWidth()-2*border,
	    22);
	};
      });

    // Add listeners

    addWindowListener(new WindowAdapter()
      {
	public void windowClosing(WindowEvent event)
	{
	  exitForm(event);
	}
      });

    addComponentListener(new ComponentAdapter()
      {
	public void componentShown(ComponentEvent event)
	{
	  showForm(event);
	}
      });

    refreshButton.addActionListener(new ActionListener()
      {
	public void actionPerformed(ActionEvent event)
	{
	  requestSessionList();
	}
      });

    joinButton.addActionListener(new ActionListener()
      {
	public void actionPerformed(ActionEvent event)
	{
	  joinSession();
	}
      });

    colorButton.addActionListener(new ActionListener()
      {
	public void actionPerformed(ActionEvent event)
	{
	  Color newColor=
	    JColorChooser.showDialog(SessionBrowser.this, "Select user color",
	      colorButton.getBackground());

	  if(newColor!=null) colorButton.setBackground(newColor);
	}
      });

    createDialogButton.addActionListener(new ActionListener()
      {
	public void actionPerformed(ActionEvent event)
	{
	  CreateSessionDialog createSessionDialog=new CreateSessionDialog();
	  
	  createSessionDialog.show();
	}
      });

    sessionList.addListSelectionListener(
      new ListSelectionListener()
        {
	  public void valueChanged(ListSelectionEvent event)
	  {
	    joinButton.setEnabled(sessionList.getSelectedIndex()!=-1);
	  }
        });

    sessionList.addMouseListener(new MouseAdapter()
      {
	public void mouseClicked(MouseEvent e) {
	  if (e.getClickCount() == 2)
	  {
	    joinSession();
	  }
	}
      });

    // Set sensible size and position

    pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // Hack to allow for Dave Morgan's Weird Screen Setup

    if((screenSize.width==1024)&&(screenSize.height==1792))
    {
      screenSize.width=768;
      screenSize.height=1024;
    }

    Dimension dialogSize = getSize();
    setLocation(
      (screenSize.width - dialogSize.width) / 2,
      (screenSize.height - dialogSize.height) / 2);

  }

  private class CreateSessionDialog extends JFrame
  {
    private JPanel createPanel;

    private JButton createButton;
    
    private JTextField newSessionName;
    private JCheckBox newSessionPasswordProtected;
    private JCheckBox newSessionReadonlyPasswordProtected;
    
    private JPasswordField newSessionPassword;
    private JPasswordField newSessionReadonlyPassword;

   // private JButton loadButton;

    public CreateSessionDialog()
    {
      setTitle("Create new session");
      setName("new session");
      setResizable(false);

      // Create components

      createPanel = new JPanel();      
      createPanel.setBorder(new EtchedBorder());
      createPanel.setFocusable(false);

      createButton = new JButton("Create");
      createButton.setToolTipText("Create a new Session");
      createPanel.add(createButton);
   
      newSessionName=new JTextField("WhiteBoard session");
      newSessionName.setHorizontalAlignment(JTextField.CENTER);
      createPanel.add(newSessionName);

      newSessionPasswordProtected=new JCheckBox("Password Protected");
      newSessionPasswordProtected.setHorizontalAlignment(JTextField.CENTER);
      createPanel.add(newSessionPasswordProtected);

      newSessionReadonlyPasswordProtected=new JCheckBox("Read-only Password");
      newSessionReadonlyPasswordProtected.setHorizontalAlignment(JTextField.CENTER);
      createPanel.add(newSessionReadonlyPasswordProtected);

      newSessionPassword=new JPasswordField();
      createPanel.add(newSessionPassword);

      newSessionReadonlyPassword=new JPasswordField();
      createPanel.add(newSessionReadonlyPassword);

      //loadButton = new JButton("Load a Session");
      //loadButton.setToolTipText("Load from a saved Session");
     // createPanel.add(loadButton);

      // Set layout managers

      getContentPane().add(createPanel);

      createPanel.setLayout(new LayoutManagerAdapter(320, 118, 320, 118)
	{
	  public void layoutContainer(Container parent)
	  {
	    final int border=4;
	    final int hborder=border/2;

	    newSessionName.setBounds(
	      border,
	      border,
	      parent.getWidth()-2*border,
	      22);

	    newSessionPasswordProtected.setBounds(
	      border,
	      newSessionName.getY()+newSessionName.getHeight()+border,
	      parent.getWidth()/2-3*hborder,
	      22);

	    newSessionPassword.setBounds(
	      newSessionPasswordProtected.getX()+
	      newSessionPasswordProtected.getWidth()+border,
	      newSessionName.getY()+newSessionName.getHeight()+border,
	      parent.getWidth()/2-3*hborder,
	      22);

	    newSessionReadonlyPasswordProtected.setBounds(
	      border,
	      newSessionPassword.getY()+newSessionPassword.getHeight()+border,
	      parent.getWidth()/2-3*hborder,
	      22);

	    newSessionReadonlyPassword.setBounds(
	      newSessionReadonlyPasswordProtected.getX()+
	      newSessionReadonlyPasswordProtected.getWidth()+border,
	      newSessionReadonlyPasswordProtected.getY(),
	      newSessionReadonlyPasswordProtected.getWidth(),
	      22);

	 /*   loadButton.setBounds(
	      border,
	      newSessionReadonlyPassword.getY()+
	      newSessionReadonlyPassword.getHeight()+border,
	      parent.getWidth()-2*border,
	      22);*/

	    createButton.setBounds(
	      border,
	      newSessionReadonlyPassword.getY()+4+newSessionReadonlyPassword.getHeight()+border,
	      parent.getWidth()-2*border,
	      22);
 
	  };
	});

      // Add listeners

      addWindowListener(new WindowAdapter()
	{
	  public void windowClosing(WindowEvent event)
	  {
	    dispose();
	  }
	});

      createButton.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent event)
	  {
	    requestCreateSession(null);
	  }
	});

    /*  loadButton.addActionListener(new ActionListener()
	{
	  public void actionPerformed(ActionEvent event)
	  {
	    SessionModel sessionModel=loadSessionModel();
	    if(sessionModel!=null)
	    {
	      requestCreateSession(sessionModel);
	    }
	  }
	});*/

      newSessionPassword.getDocument().addDocumentListener(new DocumentListener()
	{
	  public void changedUpdate(DocumentEvent event)
	  {
	    updateSelected();
	  }

	  public void removeUpdate(DocumentEvent event)
	  {
	    updateSelected();
	  }

	  public void insertUpdate(DocumentEvent event)
	  {
	    updateSelected();
	  }

	  public void updateSelected()
	  {
	    newSessionPasswordProtected.setSelected(
	      newSessionPassword.getPassword().length!=0);
	  }
	});

      newSessionReadonlyPassword.getDocument().addDocumentListener(new DocumentListener()
	{
	  public void changedUpdate(DocumentEvent event)
	  {
	    updateSelected();
	  }

	  public void removeUpdate(DocumentEvent event)
	  {
	    updateSelected();
	  }

	  public void insertUpdate(DocumentEvent event)
	  {
	    updateSelected();
	  }

	  public void updateSelected()
	  {
	    newSessionReadonlyPasswordProtected.setSelected(
	      newSessionReadonlyPassword.getPassword().length!=0);
	  }
	});


      // Set sensible size and position

      pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      // Hack to allow for Dave Morgan's Weird Screen Setup
      
      if((screenSize.width==1024)&&(screenSize.height==1792))
      {
	screenSize.width=768;
	screenSize.height=1024;
      }

      Dimension dialogSize = getSize();
      setLocation(
	(screenSize.width - dialogSize.width) / 2,
	(screenSize.height - dialogSize.height) / 2);

    }

    /**
     * <p>Prompt the user for a file, and load a {@link SessionModel} from
     * it.</p>
     */
    
    private SessionModel loadSessionModel()
    {
      try
      {
	JFileChooser chooser = new JFileChooser();

	chooser.setFileFilter(new FileFilter()
	  {
	    public boolean accept(File f)
	    {
	      return f.isDirectory()||f.toString().endsWith(".jinn");
	    }
	  
	    public String getDescription()
	    {
	      return "*.jinn (jinn session files)";
	    }
	  });

	if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
	{
	  FileInputStream file=new FileInputStream(chooser.getSelectedFile());
	  ObjectInputStream input=new ObjectInputStream(file);

	  SessionModel sessionModel=(SessionModel)input.readObject();
	  sessionModel.initializeAfterLoad();

	  return sessionModel;
	}
      }
      catch(IOException e)
      {
	JOptionPane.showMessageDialog(
	  this,  "An error occured while trying to load the session.\n\n"+
	  "The file you chose is not compatible, or could not "+
	  "be accessed.",
	  "Could not load session", JOptionPane.ERROR_MESSAGE);
      }
      catch(ClassNotFoundException e)
      {
	JOptionPane.showMessageDialog(
	  this,  "An error occured while trying to load the session.\n\n"+
	  "The file you chose is not compatible.",
	  "Could not load session", JOptionPane.ERROR_MESSAGE);
      }

      return null;
    }

    /**
     * <p>Request a new session with the specified SessionModel (null
     * for a new empty SessionModel).</p>
     */

    private void requestCreateSession(SessionModel sessionModel)
    {
      try
      {
	String sessionName=newSessionName.getText();
	
	if(sessionName.equals("")) sessionName="Whiteboard session";

	String readOnly=null;

	if(newSessionReadonlyPasswordProtected.isSelected())
	{
	  readOnly=new String(newSessionReadonlyPassword.getPassword());
	}

	String readWrite=null;

	if(newSessionPasswordProtected.isSelected())
	{
	  readWrite=new String(newSessionPassword.getPassword());
	}

	boolean readOnlyBlank=readOnly!=null&&readOnly.length()==0;
	boolean readWriteBlank=readWrite!=null&&readWrite.length()==0;

	if(readOnlyBlank||readWriteBlank)
	{
	  JOptionPane.showMessageDialog(
	    this,  "You entered " +((readOnlyBlank&&readWriteBlank)?
				    "two":"one")+" blank password"+
	    (readOnlyBlank&&readWriteBlank?"s":"")+".\n\n"+
	    "Password protection has been disabled.",
	    "Blank password"+(readOnlyBlank&&readWriteBlank?"s":""),
	    JOptionPane.INFORMATION_MESSAGE);

	    readOnly=null;
	    readWrite=null;
	}

	connection.sendMessage(new Message(
	  Message.REQUEST_SESSION_CREATE,
	  new Object[]
	  {
	    new SessionInfo(null, sessionName, readOnly, readWrite, null),
	    sessionModel
	    }));
      }
      catch (IOException e)
      {  
	disconnect();
      }

      dispose();
    }
  }
}
