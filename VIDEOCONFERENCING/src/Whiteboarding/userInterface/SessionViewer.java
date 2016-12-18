package Whiteboarding.userInterface;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Vector;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.*;
//import Whiteboarding.model.*;
import Whiteboarding.model.Shape;
import Whiteboarding.main.*;
import Whiteboarding.model.Brush;
import Whiteboarding.model.ChangeGroup;
import Whiteboarding.model.ChatState;
import Whiteboarding.model.Claim;
import Whiteboarding.model.Drawable;
import Whiteboarding.model.Entity;
import Whiteboarding.model.EntityID;
import Whiteboarding.model.Group;
import Whiteboarding.model.LineEnding;
import Whiteboarding.model.Pen;
import Whiteboarding.model.Polyline;
import Whiteboarding.model.Property;
import Whiteboarding.model.SessionState;
import Whiteboarding.model.UserInfo;

/**
 * The SessionViewer object contains a set of GUI components. These
 * provide a view of a whiteboard session to the user and are also
 * sources for events enabling user interaction. The SessionViewer
 * does not live within a Session - thus it may toggle between
 * multiple open sessions without requiring a window for each one.
 * It should be thought of as a light/thin layer above the SessionControl
 * object, which is its link to a specific Session - the link conveys
 * user actions down towards the SessionControl (for interpretation and
 * further action), and it conveys updates to the appearance of the
 * GUI back up towards the SessionViewer. Any 'rendering' which occurs
 * (usually to the main session view pane) will actually take place
 * within the SessionControl object - where there is access to the
 * requisite state data.
 */

public class SessionViewer extends JFrame
{
       String file="C:\\Users\\Simran\\Downloads\\graph.jpg";
            private Image img = null;
    private static String currentdir = System.getProperty("user.dir");
           
  /**
   * <p>Keep a local SessionState for rendering purposes.</p>
   */

  private SessionState sessionState;

  /**
   * <p>Button group for main buttons.</p>
   */

  private ButtonGroup buttonGroup;

  private Action groupAction;
  private Action ungroupAction;
  private Action deleteAction;

  private Action bringToFrontAction;
  private Action sendToBackAction;

 // private AbstractAction saveAction;
  private AbstractAction closeAction;

  private ActionListener selectToolActionListener;

  private JMenuBar menuBar;
  private JMenu sessionMenu;
  private JMenu editMenu;

  private JToolBar commandToolBar;
  private JToolBar optionToolBar;

  private Container whiteboardPane;
  private Container chatPane;
  private JSplitPane splitPane;

  private JToggleButton scrollButton;
  //private JToggleButton selectButton;
  private JToggleButton claimButton;
  private JToggleButton rectangleButton;
  private JToggleButton ellipseButton;
  private JToggleButton polylineButton;
  private JToggleButton curvedPolylineButton;
  private JToggleButton textButton;

  private JToggleButton moveButton;
  private JToggleButton resizeButton;
  private JToggleButton rotateButton;
  private JToggleButton flipButton;

  private JToggleButton movePointButton;

  private JTextField chatTextField;
  private ActionListener chatTextFieldActionListener;
  private JScrollPane chatScrollPane;

  private JList userList;
  private JScrollPane userListScrollPane;

  /**
   * <p>A link to this user's UserInfo.</p>
   */

  private EntityID userInfo;

  /**
   * <p>The whiteboard itself.</p>
   */

  private JWhiteboard whiteboard;
  private int zoomLevel;
  private Point offset;

  /**
   * <p>The properties pane.</p>
   */

  private JComponent propertiesPane;

  /**
   * <p>The chat text area.</p>
   */

  private JTextPane chatTextPane;

  /**
   * <p>Count of lines currently displayed in the chatTextPane.</p>
   */

  private int chatTextPaneLinesDisplayed;

  /**
   * <p>The status bar.</p>
   */

  private StatusBar statusBar;

  private Session session;

  private Set selectionEntityIDs = new LinkedHashSet();

  private Color userColor;

  private String userName;

  /**
   * <p>The color chooser used in various dialog boxes. It is global
   * so that it will preserve the list of recent colors.</p>
   */

  private JColorChooser theColourChooser = new JColorChooser();

  /**
   * <p>Whether the user is notified is a Claim or a lock prevents a change.</p>
   */

  private boolean notifyOfChangeNotAllowed;

  /**
   * <p>User's current brush.</p>
   */

  private Brush activeBrush;

  /**
   * <p>User's current pen.</p>
   */

  private Pen activePen;

  /**
   * <p>Whether this client session is read-only.</p>
   */

  private boolean isReadOnly;

  /**
   * <p>The constructor initiates the GUI elements. A boolean specifies
   * whether the GUI is allowed to request changes from the server, and
   * a String and {@link Color} give the user name and color.</p>
   * <p>The constructor does as much setup as is possible without assuming
   * the presence of a valid model. To complete the construction, call
   * initialize.</p>
   */

  public SessionViewer(Session session, boolean isReadOnly,
		       String userName, Color userColor)
  {
    super(session.getSessionInfo().getSubject());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.session=session;
    this.userName=userName;
    this.userColor=userColor;
    this.notifyOfChangeNotAllowed=true;
    this.isReadOnly=isReadOnly;
    this.activeBrush=null;
    this.activePen=null;

    handleModelChange();    // Sets Session State

    createActionsAndListeners();
    createGUIComponents();

    pack();
  }

  /**
   * <p>Initialize the SessionViewer proper.</p>
   */

  public void initialize()
  {
    setVisible(true);
    updateUserInfo();

    String joinText=" \u00fd\u00ff"+this.userName+
      (isReadOnly?"\u00fe enters the session (as an observer only)\u00fc":
      "\u00fe enters the session\u00fc");

    sendChangeGroup(
      ChangeGroup.getSayChangeGroup(session, joinText), true);

    whiteboard.initialize();

    handleModelLoad();
  }

  /**
   * <p>Create GUI components.</p>
   */

  private void createGUIComponents()
  {
    menuBar=new JMenuBar();
    setJMenuBar(menuBar);

    Container pane = getContentPane();
    pane.setLayout(new BorderLayout());

    statusBar=new StatusBar();
    statusBar.setMessage("Ready");
    pane.add(statusBar, BorderLayout.SOUTH);

    sessionMenu = new JMenu("Session");
    sessionMenu.setMnemonic(KeyEvent.VK_S);
    sessionMenu.getAccessibleContext().setAccessibleDescription("Contains commands relating to the session as a whole.");
    menuBar.add(sessionMenu);

    whiteboardPane = new Container();
    whiteboardPane.setLayout(new BorderLayout());
   
    chatPane = new Container();
    splitPane = new JSplitPane(
      JSplitPane.VERTICAL_SPLIT, true, whiteboardPane, chatPane);
    splitPane.setResizeWeight(0.75);
    pane.add(splitPane);

    commandToolBar = new JToolBar(JToolBar.HORIZONTAL);
    optionToolBar = new JToolBar(JToolBar.VERTICAL);

    editMenu = new JMenu("Edit");
    editMenu.setMnemonic(KeyEvent.VK_E);
    editMenu.getAccessibleContext().setAccessibleDescription(
	"Contains commands for editing objects on the whiteboard.");
    menuBar.add(editMenu);

   /* JMenuItem menuItem = new JMenuItem(saveAction);
    menuItem.setMnemonic(KeyEvent.VK_S);
    sessionMenu.add(menuItem);
    commandToolBar.add(saveAction);*/

    JMenuItem menuItem = new JMenuItem(closeAction);
    menuItem.setMnemonic(KeyEvent.VK_L);
    sessionMenu.add(menuItem);
    commandToolBar.add(closeAction);

    commandToolBar.addSeparator();

    menuItem = new JMenuItem(groupAction);
    menuItem.setMnemonic(KeyEvent.VK_G);
    editMenu.add(menuItem);
    commandToolBar.add(groupAction);

    menuItem = new JMenuItem(ungroupAction);
    menuItem.setMnemonic(KeyEvent.VK_U);
    editMenu.add(menuItem);
    commandToolBar.add(ungroupAction);

    editMenu.addSeparator();
    commandToolBar.addSeparator();

    menuItem = new JMenuItem(bringToFrontAction);
    menuItem.setMnemonic(KeyEvent.VK_F);
    editMenu.add(menuItem);
    commandToolBar.add(bringToFrontAction);

    menuItem = new JMenuItem(sendToBackAction);
    menuItem.setMnemonic(KeyEvent.VK_B);
    editMenu.add(menuItem);
    commandToolBar.add(sendToBackAction);

    editMenu.addSeparator();
    commandToolBar.addSeparator();

    menuItem = new JMenuItem(deleteAction);
    menuItem.setMnemonic(KeyEvent.VK_D);
    editMenu.add(menuItem);
    commandToolBar.add(deleteAction);

    buttonGroup = new ButtonGroup();

    scrollButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\scroll.gif"));
    scrollButton.setToolTipText("Scroll the whiteboard");
    scrollButton.setActionCommand("scroll");
    buttonGroup.add(scrollButton);
    optionToolBar.add(scrollButton);

    /*
    selectButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\select.gif"));
    selectButton.setToolTipText("Select objects to edit");
    selectButton.setActionCommand("select");
    buttonGroup.add(selectButton);
    optionToolBar.add(selectButton);
    */

    optionToolBar.addSeparator();

    claimButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\claim.gif"));
    claimButton.setToolTipText("Claim an area of whiteboard space for your own use");
    claimButton.setActionCommand("claim");
    buttonGroup.add(claimButton);
    optionToolBar.add(claimButton);

    rectangleButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\rectangle.gif"));
    rectangleButton.setToolTipText("Draw squares or rectangles");
    rectangleButton.setActionCommand("rectangle");
    buttonGroup.add(rectangleButton);
    optionToolBar.add(rectangleButton);

    ellipseButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\ellipse.gif"));
    ellipseButton.setToolTipText("Draw circles or ellipses");
    ellipseButton.setActionCommand("ellipse");
    buttonGroup.add(ellipseButton);
    optionToolBar.add(ellipseButton);

    polylineButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\polyline.gif"));
    polylineButton.setToolTipText("Draw lines or polylines");
    polylineButton.setActionCommand("polyline");
    buttonGroup.add(polylineButton);
    optionToolBar.add(polylineButton);

    curvedPolylineButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\curvedPolyline.gif"));
    curvedPolylineButton.setToolTipText("Draw curved polylines");
    curvedPolylineButton.setActionCommand("curvedPolyline");
    buttonGroup.add(curvedPolylineButton);
    optionToolBar.add(curvedPolylineButton);

    textButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\text.gif"));
    textButton.setToolTipText("Type text on to the whiteboard");
    textButton.setActionCommand("text");
    buttonGroup.add(textButton);
    optionToolBar.add(textButton);

    optionToolBar.addSeparator();

    moveButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\move.gif"));
    moveButton.setToolTipText("Move an object");
    moveButton.setActionCommand("move");
    buttonGroup.add(moveButton);
    optionToolBar.add(moveButton);

    resizeButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\resize.gif"));
    resizeButton.setToolTipText("Resize an object");
    resizeButton.setActionCommand("resize");
    buttonGroup.add(resizeButton);
    optionToolBar.add(resizeButton);

    rotateButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\rotate.gif"));
    rotateButton.setToolTipText("Rotate an object");
    rotateButton.setActionCommand("rotate");
    buttonGroup.add(rotateButton);
    optionToolBar.add(rotateButton);

    flipButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\flip.gif"));
    flipButton.setToolTipText("Flip an object");
    flipButton.setActionCommand("flip");
    buttonGroup.add(flipButton);
    optionToolBar.add(flipButton);

    movePointButton = new JToggleButton(new ImageIcon(currentdir+"\\src\\data\\movePoint.gif"));
    movePointButton.setToolTipText("Move a polyline point");
    movePointButton.setActionCommand("movePoint");
    buttonGroup.add(movePointButton);
    optionToolBar.add(movePointButton);

    scrollButton.addActionListener(selectToolActionListener);
    //selectButton.addActionListener(selectToolActionListener);
    claimButton.addActionListener(selectToolActionListener);
    rectangleButton.addActionListener(selectToolActionListener);
    ellipseButton.addActionListener(selectToolActionListener);
    polylineButton.addActionListener(selectToolActionListener);
    curvedPolylineButton.addActionListener(selectToolActionListener);
    textButton.addActionListener(selectToolActionListener);

    moveButton.addActionListener(selectToolActionListener);
    resizeButton.addActionListener(selectToolActionListener);
    rotateButton.addActionListener(selectToolActionListener);
    flipButton.addActionListener(selectToolActionListener);
    movePointButton.addActionListener(selectToolActionListener);

    buttonGroup.setSelected(moveButton.getModel(), true);

    //button = new JButton(new ImageIcon(currentdir+"\\src\\data\\delete.gif"));
    //toggleButton.setToolTipText("Delete selected object or objects");
    //createToolBar.add(toggleButton);

    propertiesPane = new Box(BoxLayout.X_AXIS);
    //propertiesPane = new JPanel();
    //propertiesPane.setLayout(new FlowLayout());

    whiteboardPane.add(commandToolBar, BorderLayout.NORTH);
    whiteboardPane.add(whiteboardPane = new Container());
    whiteboardPane.setLayout(new BorderLayout());
    whiteboardPane.add(optionToolBar, BorderLayout.WEST);
    whiteboardPane.add(whiteboardPane = new Container());
    whiteboardPane.setLayout(new BorderLayout());
    whiteboardPane.add(propertiesPane, BorderLayout.NORTH);
       /*
    wp2.setLayout(new BorderLayout());
    borderlayout
    JPanel panel=new JPanel();

    panel.add(createToolBar);
    panel.add(editToolBar);
    panel.add(propertiesPane);

    whiteboardPane.add(panel, BorderLayout.WEST);
    */

    /*
    JViewport whiteboardViewport = new JViewport();
    whiteboardViewport.setView(new JWhiteboard());
    whiteboardPane.add(whiteboardViewport);
    */

    chatPane.setLayout(new BorderLayout());
    chatTextPane = new JTextPane();
    chatTextPane.setEditable(false);
    chatTextPaneLinesDisplayed=0;

    chatTextField = new JTextField();
    chatTextField.addActionListener(chatTextFieldActionListener);
    chatPane.add(chatTextField, BorderLayout.SOUTH);

    userList = new JList();
    updateUserList();
    userList.setCellRenderer(new UserListCellRenderer());
    userListScrollPane = new JScrollPane(userList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    chatScrollPane = new JScrollPane(chatTextPane,
				     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    chatScrollPane.getViewport().setBackground(Color.WHITE);

    JSplitPane chatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, chatScrollPane, userListScrollPane);
    chatSplitPane.setResizeWeight(0.85);
    chatPane.add(chatSplitPane);

    whiteboard=new JWhiteboard();
    whiteboardPane.add(whiteboard);

    selectionChanged(); /* so that the default pen and brush properties appear */
  }
/*public void paint( Graphics g )  
    {  
        super.paint(g);
        g.drawImage( img ,60, 130,580,440, this );  
        this.pack();
    } */
  /**
   * <p>Override 'hide'; it seems to get called erroneously when the user
   * tries to close the window but cancels the action.</p>
   */

  public void hide()
  {
  }

  /**
   * <p>Create Actions and Listeners.</p>
   */

  private void createActionsAndListeners()
  {
      /*saveAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) { saveSessionModel(); }
    };
    saveAction.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\save.gif"));
    saveAction.putValue(Action.NAME, "Save Session");
    saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    saveAction.putValue(Action.SHORT_DESCRIPTION, "Save a local copy of this whiteboard session");
*/
    closeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e)
	{ SessionViewer.this.session.close(); }
    };
    closeAction.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\leave.gif"));
    closeAction.putValue(Action.NAME, "Leave Session");
    closeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    closeAction.putValue(Action.SHORT_DESCRIPTION, "Disconnect from this whiteboard session");
    closeAction.putValue(Action.LONG_DESCRIPTION, "Disconnects you from this whiteboard session. If no one else is connected, the session will terminate.");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e)
	{ SessionViewer.this.session.close(); }
    });

    chatTextFieldActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	sendChangeGroup(ChangeGroup.getSayChangeGroup(
	  session,
	  " \u00fd\u00ff"+SessionViewer.this.userName+"\u00fe: \u00fc"+
	  chatTextField.getText()));

	chatTextField.setText("");
      }
    };

    selectToolActionListener = new ActionListener() {
	public void actionPerformed(ActionEvent e)
	{
	  whiteboard.mouseState = whiteboard.READY;
	  whiteboard.updateStatusBar();
	}
    };

    groupAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e)
	{ whiteboard.groupSelectedDrawables(); }
    };
    groupAction.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\group.gif"));
    groupAction.putValue(Action.NAME, "Group");
    groupAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
    groupAction.putValue(Action.SHORT_DESCRIPTION, "Group selected objects so they can be treated as one");
    groupAction.putValue(Action.LONG_DESCRIPTION, "This groups all the objects selected so they can be moved, resized and rotated as one.");
    groupAction.setEnabled(false);

    ungroupAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) { whiteboard.ungroupSelectedGroups(); }
    };
    ungroupAction.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\ungroup.gif"));
    ungroupAction.putValue(Action.NAME, "Ungroup");
    ungroupAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
    ungroupAction.putValue(Action.SHORT_DESCRIPTION, "Break a group or multiple groups into their component objects");
    ungroupAction.putValue(Action.LONG_DESCRIPTION, "This breaks up any groups selected so that the parts can be edited individually.");
    ungroupAction.setEnabled(false);

    deleteAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e)
	{ sendChangeGroup(
	  ChangeGroup.getDeleteChangeGroup(session, selectionEntityIDs)); }
    };
    deleteAction.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\delete.gif"));
    deleteAction.putValue(Action.NAME, "Delete");
    deleteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    deleteAction.putValue(Action.SHORT_DESCRIPTION, "Delete selected object or objects");
    deleteAction.putValue(Action.LONG_DESCRIPTION, "This deletes the object or objects you have selected, provided you have permission to do this."); // FIXME: note about ability to undo this?
    deleteAction.setEnabled(false);

    bringToFrontAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e)
	{ sendChangeGroup(
	  ChangeGroup.getBringToFrontChangeGroup(session, selectionEntityIDs)); }
    };
    bringToFrontAction.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\bringToFront.gif"));
    bringToFrontAction.putValue(Action.NAME, "Bring To Front");
    bringToFrontAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
    bringToFrontAction.putValue(Action.SHORT_DESCRIPTION, "Bring selected objects to the front");
    bringToFrontAction.putValue(Action.LONG_DESCRIPTION, "This brings the selected objects to the front of the whiteboard.");
    bringToFrontAction.setEnabled(false);

    sendToBackAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e)
	{ sendChangeGroup(
	  ChangeGroup.getSendToBackChangeGroup(session, selectionEntityIDs)); }
    };
    sendToBackAction.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\sendToBack.gif"));
    sendToBackAction.putValue(Action.NAME, "Send To Back");
    sendToBackAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
    sendToBackAction.putValue(Action.SHORT_DESCRIPTION, "Sends selected objects to the back");
    sendToBackAction.putValue(Action.LONG_DESCRIPTION, "This sends the selected objects to the back of the whiteboard.");
    sendToBackAction.setEnabled(false);
  }

  /**
   * <p>Get the current viewing position offset.</p>
   */

  public Point getOffset()
  {
    return offset;
  }

  /**
   * <p>Get the current zoom level.</p>
   */

  public int getZoomLevel()
  {
    return zoomLevel;
  }

  /**
   * <p>Get a reference to the user's UserInfo.</p>
   */

  public EntityID getUserInfo()
  {
    return userInfo;
  }

  /**
   * <p>Get the SessionState the SessionViewer is currently drawing from.</p>
   */

  public SessionState getSessionState()
  {
    return sessionState;
  }

  /**
   * <p>Get the active {@link Brush}, or null for the default Brush.</p>
   */

  public Brush getActiveBrush()
  {
    return activeBrush;
  }

  /**
   * <p>Get the active {@link Pen}, or null for the default Pen.</p>
   */

  public Pen getActivePen()
  {
    return activePen;
  }

  /**
   * <p>Update the local state when a changeGroup comes in from the server.
   * Does not apply the changes itself, but instead consults the session.
   * However, it may want to scan the changeGroup to discover which
   * Drawables have been changed.</p>
   */

  public void receiveChangeGroup(final ChangeGroup changeGroup)
  {
    Runnable runnable = new Runnable() {
      public void run() { useReceivedChangeGroup(changeGroup); }
    };
    SwingUtilities.invokeLater(runnable);
  }

  private void useReceivedChangeGroup(ChangeGroup changeGroup)
  {
    EntityID changeUserInfoID = changeGroup.getUserInfo();

    EntityID[][] entityID=changeGroup.getEntityIDArrays();

    whiteboard.queueRepaints(sessionState, entityID);

    SessionState oldState = sessionState;
    handleModelChange();

    boolean selectionChanged = false;
    boolean selectionFixed=false;
    boolean selectionReset=false;

    if (entityID != null) for (int j = 0; j < entityID.length; j++) {
      if (entityID[j] != null) for (int i = 0; i < entityID[j].length; i++) {
	Entity entity = sessionState.getEntity(entityID[j][i]);

	if(selectionEntityIDs.contains(entityID[j][i])) selectionChanged=true;

	if (entity == null) { 
	  selectionEntityIDs.remove(entityID[j][i]);
	  selectionChanged = true;
	  entity = oldState.getEntity(entityID[j][i]);
	  if (!changeUserInfoID.equals(userInfo) && entity != null && entity instanceof Drawable) {
	    markDrawableChanged((Drawable)entity, changeUserInfoID);
	  }

	  if (changeUserInfoID.equals(userInfo))
	  {
	    if(entity instanceof Group)
	    {
	      selectionEntityIDs=new LinkedHashSet();
	      Vector members=((Group)entity).getMembers();
	      
	      for(int k=0; k!=members.size(); k++)
	      {
		selectionEntityIDs.add(((Entity)members.get(k)).getEntityID());
	      }
	      
	      selectionFixed=true;
	      selectionChanged=true;
	    }
	  }
	}
	else if (entity instanceof Group)
	{
	  if (changeUserInfoID.equals(userInfo))
	  {
	    if(!selectionFixed)
	    {
	      selectionEntityIDs=new LinkedHashSet();
	      selectionEntityIDs.add(entityID[j][i]);
	      if(oldState.getEntity(entityID[j][i])==null) selectionFixed=true;
	      selectionChanged=true;
	    }
	  }
	}
	else if (entity instanceof Drawable) {
	  if (changeUserInfoID.equals(userInfo)) {
	    whiteboard.queueRepaints(sessionState, selectionEntityIDs);
	    if(!selectionFixed)
	    {
	      if(!selectionReset)
	      {
		selectionEntityIDs=new LinkedHashSet();
		selectionReset=true;
	      }
	      selectionEntityIDs.add(entityID[j][i]);
	      selectionChanged = true;
	    }
	  } else {
	    markDrawableChanged((Drawable)entity, changeUserInfoID);
	  }
	} else if (entity instanceof UserInfo)
	  updateUserList();
	else if (entity instanceof ChatState)
	  updateChat();
      }
    }

    whiteboard.repaintEntity(sessionState, entityID);

    if (selectionChanged)
      selectionChanged();
  }

  private void updateUserList()
  {
    Vector v = sessionState.getUserInfo();
    Vector alive = new Vector();
    for (int i = 0; i < v.size(); i++) {
      UserInfo userInfo = (UserInfo)v.get(i);
      if (userInfo.getClientID() != 0) alive.add(userInfo);
    }
    Object list[] = alive.toArray();
    Arrays.sort(list, new Comparator() {
      public int compare(Object o1, Object o2) {
	return ((UserInfo)o1).getName().compareToIgnoreCase(((UserInfo)o2).getName());
      }
    });
    userList.setListData(list);
  }

  private void markDrawableChanged(Drawable drawable, EntityID userInfo)
  {
    UserInfo userInfoEntity=(UserInfo)sessionState.getEntity(userInfo);
    if(userInfoEntity==null) return;

    Color color=userInfoEntity.getColor();
    Vector controlPoints=drawable.getControlPoints();
    
    for(int i=0; i!=controlPoints.size(); i++)
    {
      whiteboard.displayControlPoint(new ControlPoint(
	(Point)controlPoints.get(i), color, 1000), true);
    }
    
    whiteboard.displayControlPoint(new ControlPoint(
      drawable.getCenter(), color, 1000), true);
  }

  public void handleModelLoad()
  {
    handleModelChange();
    whiteboard.redraw();
    updateChat();
  }

  public void handleModelChange()
  {
    sessionState=session.getSessionModel().getSessionState(Integer.MAX_VALUE);
  }

  public void updateChat()
  {
    Vector chatVector=sessionState.getChatStates();
    if(chatVector.size()!=0)
    {
      ChatState chat=(ChatState)chatVector.get(0);
      
      for(int lineCount=chatTextPaneLinesDisplayed;
	  lineCount<chat.getChatLines().size();
	  lineCount++)
      {
	chatTextPane.setEditable(true);
	
	if(lineCount!=0)
	{
	  chatTextPane.setSelectionStart(chatTextPane.getText().length());
	  chatTextPane.replaceSelection("\n");
	}
	
	String line=chat.getChatLines().get(lineCount).toString();
	String addition=line;
	
	int nameStart=line.indexOf(255);
	int nameEnd=line.indexOf(254);
   
	int colorStart=line.indexOf(253);
	int colorEnd=line.indexOf(252);
	
	if((nameEnd!=-1)&&(colorEnd!=-1)&&(colorStart!=-1)&&(colorEnd!=-1))
	{
	  String name=removeControlCodes(line.substring(nameStart+1, nameEnd));
	  
	  String preColorText=removeControlCodes(line.substring(0, colorStart));

	  String colorText=removeControlCodes(
	    line.substring(colorStart+1, colorEnd));

	  String postColorText=removeControlCodes(line.substring(colorEnd+1));

	  chatTextPane.setSelectionStart(chatTextPane.getText().length());
	  MutableAttributeSet attributes=new SimpleAttributeSet();
	  StyleConstants.setForeground(attributes, Color.BLACK);
	  chatTextPane.setCharacterAttributes(attributes, false);
	  chatTextPane.replaceSelection(preColorText);

  	  
	  chatTextPane.setSelectionStart(chatTextPane.getText().length());
 	  attributes=new SimpleAttributeSet();
	  StyleConstants.setForeground(attributes,
				       sessionState.getUserColor(name));
	  chatTextPane.setCharacterAttributes(attributes, false);
	  chatTextPane.replaceSelection(colorText);  

	  chatTextPane.setSelectionStart(chatTextPane.getText().length());
 	  attributes=new SimpleAttributeSet();
	  StyleConstants.setForeground(attributes,
				       Color.BLACK);
	  chatTextPane.setCharacterAttributes(attributes, false);
	  chatTextPane.replaceSelection(postColorText);  
  	}
        else
	{
	  chatTextPane.setSelectionStart(chatTextPane.getText().length());
 	  MutableAttributeSet attributes=new SimpleAttributeSet();
	  StyleConstants.setForeground(attributes,
				       Color.BLACK);
	  chatTextPane.setCharacterAttributes(attributes, false);
	  chatTextPane.replaceSelection(removeControlCodes(line));  
	}

	chatTextPane.setEditable(false);

	chatTextPaneLinesDisplayed++;
      }
    }
  }
  
  private String removeControlCodes(String text)
  {
    StringBuffer result=new StringBuffer();

    for(int posL=0; posL!=text.length(); posL++)
    {
      if((text.charAt(posL)>31)&&(text.charAt(posL)<252))
      {
	result.append(text.charAt(posL));
      }
    }

    return new String(result);
  }

  /**
   * <p>Prompt the user for a filename, and save a complete 'session',
   * i.e. the {@link SessionModel}, to the chosen file.</p>
   */

 /* private void saveSessionModel()
  {
    try
    {
      JFileChooser chooser = new JFileChooser();

      chooser.setApproveButtonText("Save");
      chooser.setDialogTitle("Save");

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
	String fileName=chooser.getSelectedFile().toString();

	if(!fileName.endsWith(".jinn")) fileName+=".jinn";

	ObjectOutputStream writer=new ObjectOutputStream(
	  new FileOutputStream(fileName));

	writer.writeObject(session.getSessionModel());
	writer.close();
      }
    }
    catch(FileNotFoundException e)
    {
      JOptionPane.showMessageDialog(
	this,  "An error occurred while trying to save the session.\n\n"+
	"The file you chose does not have a valid filename.",
	"Could not save session", JOptionPane.ERROR_MESSAGE);
    }
    catch(IOException e)
    {
      JOptionPane.showMessageDialog(
	this,  "An error occurred while trying to save the session.\n\n"+
	"The file you chose could not be written to.",
	"Could not save session", JOptionPane.ERROR_MESSAGE);
    }
  }
*/
  /**
   * <p>Remove the SessionViewer cleanly from the Session and from the
   * screen. If the user cancels the close, false is returned.</p>
   */

  public boolean close()
  {
    if(userInfo!=null)
    {
      boolean sendCloseRequest=false;

      Vector userInfoVector=sessionState.getUserInfo();

      int remainingClients=0;

      for(int i=0; i!=userInfoVector.size(); i++)
      {
	UserInfo userInfo=(UserInfo)userInfoVector.get(i);

	if((userInfo.getClientID()!=0)&&
	   (!(userInfo.getName().equals(userName))))
	  remainingClients++;
      }

      if((remainingClients==0)&&(!isReadOnly))
      {
	int choice=JOptionPane.showConfirmDialog(
	  this, 
	  "You are the last user in the session.\n\n"+
	  "Do you wish to close it as you leave?",
	  "Close session?",
	  JOptionPane.YES_NO_CANCEL_OPTION);

	if(choice==JOptionPane.CANCEL_OPTION)
	{
	  return false;
	}

	if(choice==JOptionPane.YES_OPTION)
	{
	  sendCloseRequest=true;
          session.close();
	}
      }

    sendChangeGroup(
      ChangeGroup.getSayChangeGroup(session,
	" \u00fd\u00ff"+this.userName+"\u00fe leaves the session\u00fc"), true);

      ChangeGroup markUserInfoUnused=new ChangeGroup(userInfo);
      
      markUserInfoUnused.add(
	new UserInfo.Change(
	  UserInfo.Change.SET_USER_CLIENT_ID,
	  new Object[] {new Integer(0)},
	  new EntityID[] {userInfo}));
    
      sendChangeGroup(markUserInfoUnused, true);

      if(sendCloseRequest) session.getSessionClient().sendCloseRequest();

      userInfo=null;
    }

    dispose();

    return true;
  }

  /**
   * <p>Handle a new ChangeGroup appropriately.</p>
   */

  private void sendChangeGroup(ChangeGroup changeGroup)
  {
    sendChangeGroup(changeGroup, false);
  }


  /**
   * <p>Handle a new ChangeGroup appropriately, with a boolean specifying
   * whether to ignore restrictions of locks, claims, and local access.</p>
   */

  private void sendChangeGroup(ChangeGroup changeGroup, boolean override)
  {
    if(changeGroup==null) return;

    if(override||
       (!isReadOnly&&sessionState.isChangeGroupAllowed(changeGroup, userInfo)))
    {
      session.getSessionClient().sendChangeGroup(changeGroup);
    }
    else if(notifyOfChangeNotAllowed)
    {
      JOptionPane pane=new JOptionPane(
	isReadOnly?"You are an observer and are not allowed to make changes.":
	"That change is prevented by someone else's Claim.",
	JOptionPane.INFORMATION_MESSAGE,
	JOptionPane.DEFAULT_OPTION);

      pane.setOptions(new Object[] {"OK", "Don't tell me again"});

      JDialog dialog=pane.createDialog(this, "Change not allowed");
      dialog.show();

      Object selectedValue=pane.getValue();

      if(selectedValue.toString().equals("Don't tell me again"))
      {
	notifyOfChangeNotAllowed=false;
      }
    }
  }

  private void selectionChanged()
  {
    int numGroupable = 0;
    boolean groupSelected = false;

    deleteAction.setEnabled(!selectionEntityIDs.isEmpty());

    propertiesPane.removeAll();

    IsGroupNormalPropertyEditor isGroupNormalPropertyEditor = null;
    FontPropertyEditor fontPropertyEditor = null;
    IsOpenPropertyEditor isOpenPropertyEditor = null;
    LineEndingsPropertyEditor lineEndingsPropertyEditor = null;
    TextPropertyEditor textPropertyEditor = null;

    BrushPropertyEditor brushPropertyEditor = new BrushPropertyEditor();
    PenPropertyEditor penPropertyEditor = new PenPropertyEditor();

    whiteboard.clearControlPointsDisplayed();

    Iterator iterator=selectionEntityIDs.iterator();

    while(iterator.hasNext())
    {
      EntityID identifier=(EntityID)iterator.next();
      Drawable drawable=(Drawable)sessionState.getEntity(identifier);

      Vector controlPoints=drawable.getControlPoints();

      for(int i=0; i!=controlPoints.size(); i++)
      {
	whiteboard.displayControlPoint(new ControlPoint(
	  (Point)controlPoints.get(i), new Color(127, 127, 127, 200), -1), false);
      }

      whiteboard.displayControlPoint(new ControlPoint(
	drawable.getCenter(), new Color(200, 200, 200, 200), -1), false);

      if (!(drawable instanceof Claim)) numGroupable++; // FIXME: should this be a flag specified in the Entity code?
      if (drawable instanceof Group) groupSelected = true;

      Vector properties=drawable.getProperties();

      for(int i=0; i!=properties.size(); i++)
      {
	Property property = (Property)properties.get(i);
	switch (property.getType()) {
	  case Property.BRUSH:
	    brushPropertyEditor.add(identifier, property);
	    break;
	  case Property.FONT:
	    if (fontPropertyEditor == null)
	      fontPropertyEditor = new FontPropertyEditor(identifier, property);
	    else
	      fontPropertyEditor.add(identifier, property);
	    break;
	  case Property.IS_GROUP_NORMAL:
	    if (isGroupNormalPropertyEditor == null)
	      isGroupNormalPropertyEditor = new IsGroupNormalPropertyEditor(identifier, property);
	    else
	      isGroupNormalPropertyEditor.add(identifier, property);
	    break;
	  case Property.IS_LOCKED:
	    break;
	  case Property.IS_OPEN:
	    if (isOpenPropertyEditor == null)
	      isOpenPropertyEditor = new IsOpenPropertyEditor(identifier, property);
	    else
	      isOpenPropertyEditor.add(identifier, property);
	    break;
	  case Property.LINE_ENDINGS:
	    if (lineEndingsPropertyEditor == null)
	      lineEndingsPropertyEditor = new LineEndingsPropertyEditor(identifier, property);
	    else
	      lineEndingsPropertyEditor.add(identifier, property);
	    break;
	  case Property.PEN:
	    penPropertyEditor.add(identifier, property);
	    break;
	  case Property.SHORTCUTS:
	    break;
	  case Property.TEXT:
	    if (textPropertyEditor == null)
	      textPropertyEditor = new TextPropertyEditor(identifier, property);
	    else
	      textPropertyEditor.add(identifier, property);
	    break;
	}
      }
    }

    groupAction.setEnabled(numGroupable >= 2);
    ungroupAction.setEnabled(groupSelected);

    bringToFrontAction.setEnabled(numGroupable > 0);
    sendToBackAction.setEnabled(numGroupable > 0);

    brushPropertyEditor.addSelfToPropertiesPane();
    penPropertyEditor.addSelfToPropertiesPane();
    
    propertiesPane.revalidate();
    propertiesPane.repaint();
    //SessionViewer.this.validate(); // fixme: updating the whole window? awful!
  }

  /**
   * <p>Update the user's {@link UserInfo} Entity, creating if necessary.</p>
   */

  public void updateUserInfo()
  {
    ChangeGroup updateUserInfo=new ChangeGroup(userInfo);

    if(userInfo==null)
    {
      UserInfo testUserInfo=sessionState.getUserInfo(userName);

      while((testUserInfo!=null)&&(testUserInfo.getClientID()!=0))
      {
	userName=JOptionPane.showInputDialog(
	  SessionViewer.this, "The name you have chosen is already in use. "+
	  "Please pick another:", userName);

	if(userName==null)
	{
	  session.close();
	  return;
	}
	else if(userName.length()==0)
	{
	  userName="jinn user";
	}

	testUserInfo=sessionState.getUserInfo(userName);
      }

      if((testUserInfo!=null)&&(testUserInfo.getClientID()==0))
      {
	JOptionPane.showMessageDialog(
	  this,  "The name you have chosen has already been used in this "+
	  "session.\n\n"+
	  "You have taken control of the previous "+userName+"\'s objects.",
	  "Name Recognised", JOptionPane.INFORMATION_MESSAGE);
	
	userInfo=sessionState.getUserInfo(userName).getEntityID();
      }
      else
      {
	Vector userInfoVector=sessionState.getUserInfo();

	boolean userColorApproved=false;

	while(!userColorApproved)
	{
	  userColorApproved=true;

	  for(int i=0; i!=userInfoVector.size(); i++)
	  {
	    UserInfo userInfo=(UserInfo)userInfoVector.get(i);
	    
	    if(userInfo.getClientID()!=0)
	    {
	      Color color=userInfo.getColor();
	      
	      int difference=colorDifference(
		abs(color.getRed()-userColor.getRed()),
		abs(color.getBlue()-userColor.getBlue()),
		abs(color.getGreen()-userColor.getGreen()));

	      if(difference<16)
	      {
		JOptionPane pane = new JOptionPane(
		  "Your user colour (or one close to it) is currently in use.\n\n"+
		  "Press ok to choose a new colour, or cancel to leave the session.",
		  JOptionPane.INFORMATION_MESSAGE,
		  JOptionPane.DEFAULT_OPTION);

		pane.setOptions(new Object[] {"Ok", "Cancel"});
		
		JDialog dialog=pane.createDialog(SessionViewer.this,
						 "Color In Use");
		dialog.show();

		Object selectedValue=pane.getValue();

		if(selectedValue.toString().equals("Cancel")||
		  selectedValue.toString().equals("uninitializedValue"))
		{
		  session.close();
		  return;
		}
		
		Color newColor=
		  JColorChooser.showDialog(
		    SessionViewer.this, "Select user color",
		    userColor);
		
		if(newColor==null)
		{
		  session.close();
		  return;
		}

		userColor=newColor;
		userColorApproved=false;
	      }
	    }
	  }
	}


	userInfo=new EntityID(session.getSessionClient().getClientID());
	
	updateUserInfo.add(
	  new UserInfo.Change(
	    UserInfo.Change.CREATE,
	    null,
	    new EntityID[] {userInfo}));

	updateUserInfo.add(
	  new UserInfo.Change(
	    UserInfo.Change.SET_USER_NAME,
	    new Object[] {userName},
	    new EntityID[] {userInfo}));
	
	updateUserInfo.add(
	  new UserInfo.Change(
	    UserInfo.Change.SET_USER_COLOR,
	    new Object[] {userColor},
	    new EntityID[] {userInfo}));
      }

      updateUserInfo.add(
	new UserInfo.Change(
	  UserInfo.Change.SET_USER_CLIENT_ID,
	  new Object[] {new Integer(session.getSessionClient().getClientID())},
	  new EntityID[] {userInfo}));

    }

    updateUserInfo.add(
      new UserInfo.Change(
	UserInfo.Change.SET_USER_VIEWING_AREA,
	new Object[] { new java.awt.Rectangle(0, 0, 0, 0) }, // fixme
	new EntityID[] {userInfo}));

    updateUserInfo.add(
      new UserInfo.Change(
	UserInfo.Change.SET_USER_ZOOM_LEVEL,
	new Object[] { new Integer(zoomLevel) },
	new EntityID[] {userInfo}));

    sendChangeGroup(updateUserInfo, true);
  }

  /**
   * <p>Return the absolute value of an integer.</p>
   */

  private int abs(int x)
  {
    return x>=0?x:-x;
  }

  /**
   * <p>Return a score for the difference between two colors, given the
   * differences in red, green and blue.</p>
   */

  private int colorDifference(int r, int g, int b)
  {
    int w=(r+g+b)/2;

    return max(max(r, g), max(b, w));
  }

  /**
   * <p>Return the maximum of two integers.</p>
   */

  private int max(int x, int y)
  {
    return x>y?x:y;
  }

  /**
   * A class to draw the whiteboard itself.
   */

  private class JWhiteboard extends JComponent
    implements MouseListener, MouseMotionListener, Runnable
  {
    private static final int READY=0;
    private static final int CLICKING=1;
    private static final int CREATING=2;
    private static final int EDITING=3;
    private static final int SELECTING=4;
    private static final int SCROLLING=5;

    private int mouseState;
    private boolean selectMultiple;
    private Point mousePoint;
    private Vector mousePoints;

    private EntityID editingEntityID;
    private int editingControlPointIndex;

    private SessionState rubberBandSessionState;

    private ChangeGroup rubberBandChangeGroup;
    // FIXME: think about the implications of this ChangeGroup when receiving ChangeGroups from the server?

    private String textBeingCreated;

    /**
     * <p>Vector of repaints waiting to be passed to Swing when we're done
     * updating.</p>
     */

    private Vector repaints;

    /**
     * <p>Vector of {@link ControlPoint}s being displayed.</p>
     */
    
    private Vector controlPointsDisplayed;

    /**
     * <p>The ControlPoint currently highlighted as being nearest the
     * cursor.</p>
     */

    private ControlPoint nearestControlPoint;
    
    private Cursor scrollCursor;
    private Cursor selectCursor;
    private Cursor claimCursor;
    private Cursor rectangleCursor;
    private Cursor ellipseCursor;
    private Cursor polylineCursor;
    private Cursor curvedPolylineCursor;
    private Cursor textCursor;
    private Cursor moveCursor;
    private Cursor resizeCursor;
    private Cursor rotateCursor;
    private Cursor flipCursor;
    private Cursor movePointCursor;
    private Cursor otherCursor;
        private Boolean kk=false;


    /**
     * <p>Create a new JWhiteboard, doing as much setup as possible without
     * relying on a valid session model being present, and without doing
     * any rendering. Call initialize to set up the whiteboard proper.</p>
     */

    public JWhiteboard()
    {
      repaints=new Vector();
      controlPointsDisplayed=new Vector();

      mouseState=READY;
      mousePoint=null;
      rubberBandChangeGroup=null;
      zoomLevel=65536;
      offset=new Point(-getWidth()/2, -getHeight()/2);

      addMouseListener(this);
      addMouseMotionListener(this);

      scrollCursor=new Cursor(Cursor.MOVE_CURSOR);
      selectCursor=new Cursor(Cursor.DEFAULT_CURSOR);
      claimCursor=
      rectangleCursor=loadCursor(
	currentdir+"\\src\\data\\rectangleCursor", new Point(4, 4), "rectangle");

      ellipseCursor=loadCursor(
	currentdir+"\\src\\data\\ellipseCursor", new Point(4, 4), "ellipse");

      polylineCursor=loadCursor(
	currentdir+"\\src\\data\\polylineCursor", new Point(4, 4), "polyline");

      curvedPolylineCursor=loadCursor(
	currentdir+"\\src\\data\\curvedPolylineCursor", new Point(4, 4), "curve");

      textCursor=new Cursor(Cursor.TEXT_CURSOR);
      moveCursor=new Cursor(Cursor.MOVE_CURSOR);
      resizeCursor=new Cursor(Cursor.MOVE_CURSOR);
      rotateCursor=new Cursor(Cursor.MOVE_CURSOR);
      flipCursor=new Cursor(Cursor.MOVE_CURSOR);
      movePointCursor=new Cursor(Cursor.MOVE_CURSOR);
      otherCursor=new Cursor(Cursor.DEFAULT_CURSOR);
    }

    /**
     * <p>Set up the whiteboard proper.</p>
     */

    public void initialize()
    {
      new Thread(this).start();
    }

    /**
     * <p>Regularly clear rubber bands from the screen.</p>
     */

    public void run()
    {
      boolean mightClearNext=false;

      // Loop is terminated by null pointer exception on graphics when
      // whiteboard is disposed

      try
      {
	while(true)
	{
	  try
	  {
	    Thread.sleep(200);
	  }
	  catch(InterruptedException e) {}

	  if(mightClearNext&&(mouseState==READY))
	  {
	    drawRubberBand(null);
	  }

	  mightClearNext=(mouseState==READY);

	  Vector claims=sessionState.getClaims();

	  Graphics g=getGraphics();

	  java.awt.Rectangle clip=
	    new java.awt.Rectangle(0, 0, getWidth(), getHeight());

	  g.clipRect(clip.x, clip.y, clip.width, clip.height);

	  java.awt.Rectangle viewable = new java.awt.Rectangle(
	    (clip.x+offset.x)*zoomLevel, (clip.y+offset.y)*zoomLevel,
	    clip.width*zoomLevel, clip.height*zoomLevel);

	  for(int i=0; i!=claims.size(); i++)
	  {
	    ((Claim)claims.get(i)).render(sessionState, (Graphics2D)g, viewable);
	  }

	  synchronized(controlPointsDisplayed)
	  {
	    for(int i=0; i!=controlPointsDisplayed.size(); i++)
	    {
	      ControlPoint controlPoint=(ControlPoint)controlPointsDisplayed.get(i);
	      if(controlPoint.timeLeft!=-1)
	      {
		controlPoint.timeLeft-=200;
		controlPoint.color=new Color(
		  controlPoint.color.getRed(), controlPoint.color.getGreen(),
		  controlPoint.color.getBlue(), controlPoint.color.getAlpha()/2);
		if(controlPoint.timeLeft<0)
		{
		  controlPointsDisplayed.remove(controlPoint);
		  i--;
	      }
		
		repaintControlPoint(controlPoint);
	      }
	    }
	  }
	}
      }
      catch(Exception e) {}
    }

    /**
     * <p>Load a cursor from a given file, with the specified hotspot
     * position and accessiblity name.</p>
     */

    private Cursor loadCursor(String fileName, Point hotspot, String name)
    {
      Dimension size=getToolkit().getBestCursorSize(16, 16);

      java.awt.Image image=null;

      switch(size.width)
      {
	case 16: image=getToolkit().createImage(
	  fileName+".gif"); break;

	case 32: image=getToolkit().createImage(
	  fileName+"32.gif"); break;

	case 64: image=getToolkit().createImage(
	  fileName+"64.gif"); break;

	default:
	  image=getToolkit().createImage(fileName+".gif"); break;
      }

      return getToolkit().createCustomCursor(image, hotspot, name);
    }


    /**
     * <p>Update the status bar depending on the currently selected tool
     * and mouse state.</p>
     */
    
    public void updateStatusBar()
    {
      String selected=buttonGroup.getSelection().getActionCommand();

      switch(mouseState)
      {
	case READY:
	{
	  if(selected=="scroll")
	  {
	    statusBar.setMessage("Click and drag to scroll the whiteboard.");
	  }
	  else if(selected=="claim")
	  {
	    statusBar.setMessage("Click and drag to create a Claim.");
	  }
	  else if(selected=="rectangle")
	  {
	    statusBar.setMessage("Click and drag to create a Rectangle.");
	  }
	  else if(selected=="ellipse")
	  {
	    statusBar.setMessage("Click and drag to create an Ellipse.");
	  }
	  else if(selected=="polyline")
	  {
	    statusBar.setMessage("Click to start drawing a Polyline.");
	  }
	  else if(selected=="curvedPolyline")
	  {
	    statusBar.setMessage("Click to start drawing a Curved Polyline.");
	  }
	  else if(selected=="text")
	  {
	    statusBar.setMessage("Click to create text.");
	  }
	  else if(/*(selected=="select")||*/(editingEntityID==null))
	  {
	    statusBar.setMessage("Click to select a single object. Hold Control to select or deselect extra objects. "+
				 "Click and drag to select multiple objects.");
	  }
	  else if(selected=="move")
	  {
	    statusBar.setMessage("Drag this control point to move the object.");
	  }
	  else if(selected=="resize")
	  {
	    statusBar.setMessage("Drag this control point to resize the object "+
	      "(hold Shift to resize around the centre).");
	  }
	  else if(selected=="rotate")
	  {
	    statusBar.setMessage("Drag this control point to rotate the object.");
	  }
	  else if(selected=="flip")
	  {
	    statusBar.setMessage("Click on this control point to flip the object horizontally "+
	      "(hold Shift to flip vertically).");
	  }
	  else if(selected=="movePoint")
	  {
	    statusBar.setMessage("Drag this control point to move it.");
	  }
	  else
	  {
	    statusBar.setMessage("Ready");
	  }

	  break;
	}

	case CLICKING:
	{
	  break;
	}

	case SCROLLING:
	{
	  break;
	}

	case SELECTING:
	{
	  break;
	}

	case CREATING:
	{
	  if(selected=="polyline")
	  {
	    statusBar.setMessage("Double-click to create the last Polyline point.");
	  }
	  else if(selected=="curvedPolyline")
	  {
	    statusBar.setMessage("Double-click to create the last Curved Polyline point.");
	  }
	  break;
	}

	case EDITING:
	{
	  break;
	}
      }
    }

    public void updateCursor()
    {
      String selected=buttonGroup.getSelection().getActionCommand();

      switch(mouseState)
      {
	case READY:
	{
	  if(selected=="scroll")
	  {
	    setCursor(scrollCursor);
	  }
	  else if(selected=="claim")
	  {
	    setCursor(claimCursor);
	  }
	  else if(selected=="rectangle")
	  {
	    setCursor(rectangleCursor);
	  }
	  else if(selected=="ellipse")
	  {
	    setCursor(ellipseCursor);
	  }
	  else if(selected=="polyline")
	  {
	    setCursor(polylineCursor);
	  }
	  else if(selected=="curvedPolyline")
	  {
	    setCursor(curvedPolylineCursor);
	  }
	  else if(selected=="text")
	  {
	    setCursor(textCursor);
	  }
	  else if(/*(selected=="select")||*/(editingEntityID==null))
	  {
	    setCursor(selectCursor);
	  }
	  else if(selected=="move")
	  {
	    setCursor(moveCursor);
	  }
	  else if(selected=="resize")
	  {
	    setCursor(resizeCursor);
	  }
	  else if(selected=="rotate")
	  {
	    setCursor(rotateCursor);
	  }
	  else if(selected=="flip")
	  {
	    setCursor(flipCursor);
	  }
	  else if(selected=="movePoint")
	  {
	    setCursor(movePointCursor);
	  }
	  else
	  {
	    setCursor(otherCursor);
	  }

	  break;
	}

	case CLICKING:
	{
	  break;
	}

	case SCROLLING:
	{
	  break;
	}

	case SELECTING:
	{
	  break;
	}

	case CREATING:
	{
	  break;
	}

	case EDITING:
	{
	  break;
	}
      }
    }

    public void redraw()
    {
      Graphics g=getGraphics();
      g.clipRect(0, 0, getWidth(), getHeight());
      update(g);
    }

    public void paint(Graphics g)
    {
      java.awt.Rectangle clip = g.getClipBounds();

      ((Graphics2D)g).setTransform(new AffineTransform());
      /*BufferedImage image = null;
            try {
                image = ImageIO.read(new File("C:\\Users\\Simran\\Downloads\\graph.gif"));
            } catch (IOException ex) {
                Logger.getLogger(SessionViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
            int w = image.getWidth(null);
            int h = image.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      g=bi.getGraphics();
      g.drawImage(image,0,0,this);
      System.out.println(image.getWidth(null) + " " + image.getHeight(null));*/
       // toolkit is an interface to the environment
      
        Toolkit toolkit = getToolkit();
        // create the image using the toolkit
        img = toolkit.getImage(file);//.createImage(file);
        super.paint(g);
         Dimension d = getSize();
        // the internal margins of the component
        Insets i = getInsets();
            // draw to fill the entire component
            //g.drawImage(img, i.left, i.top, d.width - i.left - i.right, d.height - i.top - i.bottom, this );
        //if(kk==false)
        //{
        // kk=g.drawImage(img, clip.x, clip.y, clip.width, clip.height, this );
        //}
        //else
        //{
         g.setColor(Color.WHITE);
              //kk=g.drawImage(img, clip.x, clip.y, clip.width, clip.height, this );
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        //}
      ((Graphics2D)g).setRenderingHint(
	RenderingHints.KEY_ANTIALIASING,
	RenderingHints.VALUE_ANTIALIAS_ON);

      java.awt.Rectangle viewable = new java.awt.Rectangle(
	(clip.x+offset.x)*zoomLevel, (clip.y+offset.y)*zoomLevel,
	clip.width*zoomLevel, clip.height*zoomLevel);

      paintDrawables(g, sessionState.getDrawables(), viewable, true);

      if(rubberBandSessionState!=null)
      {
	paintDrawables(g, rubberBandSessionState.getDrawables(),
		       viewable, false);
      }

      paintControlPoints(g);
    }

    private void paintDrawables(Graphics g, Vector drawables,
				java.awt.Rectangle viewable,
				boolean drawControlPoints)
    {
      for (int i = 0; i < drawables.size(); i++) {
	Drawable drawable = (Drawable)drawables.get(i);
	drawable.render(sessionState, (Graphics2D)g, viewable);
      }
    }

    private void paintControlPoints(Graphics g)
    {
      synchronized(controlPointsDisplayed)
      {
	for(int i=0; i!=controlPointsDisplayed.size(); i++)
	{
	  ControlPoint point=(ControlPoint)controlPointsDisplayed.get(i);
	  
	  g.setColor(point.color);
	  
	  g.fill3DRect(point.x/zoomLevel-4-offset.x,
		       point.y/zoomLevel-4-offset.y,
		       8, 8, true);
	}
      }
    }

    private void clearControlPointsDisplayed()
    {
      synchronized(controlPointsDisplayed)
      {
	for(int i=0; i!=controlPointsDisplayed.size(); i++)
	{
	  ControlPoint controlPoint=(ControlPoint)controlPointsDisplayed.get(i);
	  
	  if(controlPoint.timeLeft==-1)
	  {
	    controlPointsDisplayed.remove(controlPoint);
	    repaintControlPoint(controlPoint);
	    
	    i--;
	  }
	}
      }
    }

    private void displayControlPoint(ControlPoint controlPoint,
				     boolean keepOnScreen)
    {
      synchronized(controlPointsDisplayed)
      {
	if(keepOnScreen)
	{
	  Point point=new Point(controlPoint);
	  
	  point.x=(point.x/zoomLevel)-offset.x;
	  point.y=(point.y/zoomLevel)-offset.y;
	  
	  if(point.x<4) point.x=4;
	  if(point.x>getWidth()-5) point.x=getWidth()-5;
	  if(point.y<4) point.y=4;
	  if(point.y>getHeight()-5) point.y=getHeight()-5;
	  
	  controlPoint.x=(point.x+offset.x)*zoomLevel;
	  controlPoint.y=(point.y+offset.y)*zoomLevel;
	}

	controlPointsDisplayed.add(controlPoint);
	repaintControlPoint(controlPoint);
      }
    }

    private void repaintControlPoint(ControlPoint controlPoint)
    {
      repaint(controlPoint.x/zoomLevel-4-offset.x,
	      controlPoint.y/zoomLevel-4-offset.y,
	      8, 8);
    }

  /*  private void highlightNearestPoint(Point mousePoint)
    {
      Point absoluteMousePoint=Drawable.absolutePoint(offset, zoomLevel, mousePoint);

      double minDistance=-1;

      Point nearestPoint=null;

      Vector drawables = sessionState.getDrawables();
      for (int i=0; i!=drawables.size(); i++)
      {
	Drawable drawable = (Drawable)drawables.get(i);
	Vector points = drawable.getControlPoints();

	for(int j=-1; j!=points.size(); j++)
	{
	  Point point=j!=-1?(Point)points.get(j):
	    drawable.getCenter();
	  
	  double distance=point.distance(absoluteMousePoint);
	  
	  if((minDistance==-1)||(distance<minDistance))
	  {
	    minDistance=distance;
	    nearestPoint=point;
	  }
	}
      }

      if(nearestPoint!=null)
      {
	if(nearestControlPoint!=null)
	{
	  controlPointsDisplayed.remove(nearestControlPoint);
	  
	  repaintControlPoint(nearestControlPoint);
	}

	nearestControlPoint=new ControlPoint(
	  nearestPoint, new Color(255, 0, 0, 255), -1);

	displayControlPoint(nearestControlPoint, true);
      }
    }
*/

    public void mouseClicked(MouseEvent e)
    {
      handleMouseEvent(e);
    }

    public void mouseEntered(MouseEvent e)
    {
      handleMouseEvent(e);
    }

    public void mouseExited(MouseEvent e)
    {
      handleMouseEvent(e);
    }

    public void mousePressed(MouseEvent e)
    {
      handleMouseEvent(e);
    }

    public void mouseReleased(MouseEvent e)
    {
      handleMouseEvent(e);
    }

    public void mouseDragged(MouseEvent e)
    {
      handleMouseEvent(e);
    }

    public void mouseMoved(MouseEvent e)
    {
      handleMouseEvent(e);
    }

    private boolean reallyScrolling;

    private void handleMouseEvent(MouseEvent e)
    {
      ButtonModel buttonModel=buttonGroup.getSelection();

      if(buttonModel==null)
      {
	return;
      }

      int oldState=mouseState;

      if(e.getButton()==MouseEvent.BUTTON3)
      {
	reallyScrolling=true;
      }
      else
      {
	if(e.getButton()==MouseEvent.BUTTON1) reallyScrolling=false;
      }

      String selected=buttonGroup.getSelection().getActionCommand();

      if(reallyScrolling)
      {
	selected="scroll";
      }

      Point currentMousePoint=e.getPoint();

      switch(mouseState)
      {
	case READY:
	{
	  selectEditingEntityID(currentMousePoint);

	  switch(e.getID())
	  {
	    case MouseEvent.MOUSE_PRESSED:
	    {
	      if((selected=="claim")||(selected=="rectangle")||
		 (selected=="ellipse"))
	      {
		mouseState=CREATING;
		mousePoint=currentMousePoint;
		drawRubberBand(getCreateChangeGroup(currentMousePoint));
	      }
	      else if ((selected=="polyline")||(selected=="curvedPolyline"))
	      {
		mouseState=CREATING;
		mousePoint=currentMousePoint;
		mousePoints=new Vector();
		mousePoints.add(mousePoint);
		drawRubberBand(getCreateChangeGroup(currentMousePoint));
	      }
	      else
	      {
		mouseState=CLICKING;
		mousePoint=currentMousePoint;
		selectMultiple = e.isControlDown();
	      }
	      break;
	    }
	  }
	  break;
	}

	case CLICKING:
	{
	  switch(e.getID())
	  {
	    case MouseEvent.MOUSE_DRAGGED:
	    {
	      if (selected == "text") break;
	      if (currentMousePoint.x <= mousePoint.x - 4 ||
	          currentMousePoint.x >= mousePoint.x + 4 ||
	          currentMousePoint.y <= mousePoint.y - 4 ||
	          currentMousePoint.y >= mousePoint.y + 4)
	      {
		if (selected=="scroll")
		{
		  mouseState=SCROLLING;
		}
		/*
		else if((selected=="select"))
		{
		  mouseState=SELECTING;
		  drawRubberBand(getCreateChangeGroup(currentMousePoint));
	        }
		*/
	        else if((selected=="move")||(selected=="resize")||
		        (selected=="rotate")||(selected=="movePoint")||
		  (selected=="flip"))
	        {
		  mouseState=EDITING;
		  editingEntityID=null;

		  selectEditingEntityID(mousePoint);

		  if (editingEntityID == null) {
		    mouseState=SELECTING;
		    drawRubberBand(getCreateChangeGroup(currentMousePoint));
		  } else
		    drawRubberBand(getEditChangeGroup(e));
	        }
	      }
	      break;
	    }
	    case MouseEvent.MOUSE_RELEASED:
	    {
	      if(selected=="flip")
	      {
	        // fixme: prefer to use the currently selected entity? ability to flip multiple around single axis?
		selectEditingEntityID(mousePoint);

		if(editingEntityID!=null)
		{
		  drawRubberBand(getEditChangeGroup(e));
		  sendChangeGroup(getEditChangeGroup(e));
		  mouseState=READY;
		  break;
		}
	      }
	      else if (selected=="text")
	      {
		textBeingCreated=JOptionPane.showInputDialog(SessionViewer.this, "Enter text:");
		if (textBeingCreated != null && !textBeingCreated.equals("")) {
		  mouseState=CREATING;
		  drawRubberBand(getCreateChangeGroup(currentMousePoint));
		}
		break;
	      }

	      Point absoluteMousePoint = Drawable.absolutePoint(offset, zoomLevel, mousePoint);
	      Vector drawables = sessionState.getDrawables();
	      if (drawables.size() == 0) break;
	      int selectedIndex = 0;
	      if (!selectMultiple && selectionEntityIDs.size() == 1) {
		EntityID entityID = (EntityID)selectionEntityIDs.iterator().next();
		Drawable selectedDrawable = (Drawable)sessionState.getEntity(entityID);
		java.awt.Rectangle rect = selectedDrawable.getBoundingRectangle(zoomLevel);
		if (rect.contains(absoluteMousePoint)) {
		  selectedIndex = drawables.indexOf(selectedDrawable);
		  if (selectedIndex < 0) selectedIndex = 0;
		}
	      }
	      whiteboard.queueRepaints(sessionState, selectionEntityIDs);
	      if (!selectMultiple) selectionEntityIDs = new LinkedHashSet();
	      int i = selectedIndex;
	      do {
	        if (i <= 0) i = drawables.size();
		i--;
		Drawable drawable = (Drawable)drawables.get(i);
		java.awt.Rectangle rect = drawable.getBoundingRectangle(zoomLevel);
		if (rect.contains(absoluteMousePoint)) {
		  EntityID entityID = drawable.getEntityID();
		  if (selectMultiple && selectionEntityIDs.contains(entityID))
		    selectionEntityIDs.remove(entityID);
		  else
		    selectionEntityIDs.add(entityID);
//		  whiteboard.repaintEntity(sessionState, entityID);
		  break;
		}
	      } while (i != selectedIndex);
	      selectionChanged();
	      mouseState = READY;
	      break;
	    }
	  }
	  break;
	}

	case SCROLLING:
	{
	  switch (e.getID())
	  {
	    case MouseEvent.MOUSE_DRAGGED:
	    {
	      // fixme
	      break;
	    }
	    case MouseEvent.MOUSE_RELEASED:
	    {
	      offset.translate(mousePoint.x - currentMousePoint.x,
	                       mousePoint.y - currentMousePoint.y);
	      redraw();
	      mouseState=READY;
	      break;
	    }
	  }
	  break;
	}

	case SELECTING:
	{
	  switch(e.getID())
	  {
	    case MouseEvent.MOUSE_DRAGGED:
	    {
	      drawRubberBand(getCreateChangeGroup(currentMousePoint));

	      break;
	    }

	    case MouseEvent.MOUSE_RELEASED:
	    {
	      setSelection(currentMousePoint);

	      mouseState=READY;

	      break;
	    }
	  }

	  break;
	}

	case CREATING:
	{
	  switch(e.getID())
	  {
	    case MouseEvent.MOUSE_DRAGGED:
	    case MouseEvent.MOUSE_MOVED:
	    {
	      drawRubberBand(getCreateChangeGroup(currentMousePoint));
	      break;
	    }

	    case MouseEvent.MOUSE_PRESSED:
	    {
	      if((selected=="polyline")||(selected=="curvedPolyline"))
	      {
		if(e.getClickCount()==1)
		{
		  mousePoints.add(currentMousePoint);
		}
		else if(e.getClickCount()==2)
		{
		  sendChangeGroup(
		    ChangeGroup.getNewPolylineChangeGroup(
		      session, mousePoints, null, selected=="curvedPolyline"));
		  mouseState=READY;
		}
	      }

	      /* Fall through */
	    }

	    case MouseEvent.MOUSE_RELEASED:
	    {
	      if (selected == "claim" || selected == "rectangle" || selected == "ellipse" || selected == "text")
	      {
		sendChangeGroup(getCreateChangeGroup(currentMousePoint));
		mouseState = READY;
	      }
	      break;
	    }
	  }

	  break;
	}

	case EDITING:
	{
	  switch(e.getID())
	  {
	    case MouseEvent.MOUSE_DRAGGED:
	    case MouseEvent.MOUSE_MOVED:
	    {
	      drawRubberBand(getEditChangeGroup(e));
	      break;
	    }

	    case MouseEvent.MOUSE_RELEASED:
	    {
	      sendChangeGroup(getEditChangeGroup(e));
	      mouseState=READY;
	      break;
	    }
	  }

	  break;
	}
      }

      updateStatusBar();
      updateCursor();
    }

    private void selectEditingEntityID(Point mousePoint)
    {
      Point absoluteMousePoint=Drawable.absolutePoint(offset, zoomLevel, mousePoint);
      double minDistance=6*zoomLevel;
      
      editingEntityID=null;

      Iterator iterator = selectionEntityIDs.iterator();
      while (iterator.hasNext()) {
	EntityID entityID = (EntityID)iterator.next();
	Drawable drawable = (Drawable)sessionState.getEntity(entityID);
	Vector points = drawable.getControlPoints();

	for(int j=-1; j!=points.size(); j++)
	{
	  Point point=j!=-1?(Point)points.get(j):
	    drawable.getCenter();

	  double distance=point.distance(absoluteMousePoint);

	  if(distance<minDistance)
	  {
	    minDistance=distance;
	    editingEntityID=drawable.getEntityID();
	    editingControlPointIndex=j;
	  }
	}
      }
    }

    private void setSelection(Point currentMousePoint)
    {
      Point absoluteMousePoint=Drawable.absolutePoint(offset, zoomLevel, mousePoint);
      Point absoluteCurrentMousePoint=Drawable.absolutePoint(offset, zoomLevel, currentMousePoint);

      java.awt.Rectangle selectionBounds=Drawable.createBoundingRectangle(
	absoluteMousePoint.x, absoluteMousePoint.y,
	absoluteCurrentMousePoint.x, absoluteCurrentMousePoint.y);

      if (!selectMultiple)
	selectionEntityIDs=new LinkedHashSet();

      Vector drawables = sessionState.getDrawables();
      for (int i=0; i!=drawables.size(); i++)
      {
	Drawable drawable = (Drawable)drawables.get(i);
	Vector points = drawable.getControlPoints();

	for(int j=0; j!=points.size(); j++)
	{
	  Point point=(Point)points.get(j);

	  if(selectionBounds.contains(point))
	  {
	    selectionEntityIDs.add(drawable.getEntityID());

	    break;
	  }
	}
      }

      repaint(0, 0, getWidth(), getHeight());
      selectionChanged();
    }

    /**
     * <p>Group the selected entities together.</p>
     */

    private void groupSelectedDrawables()
    {
      if(selectionEntityIDs.size() >= 2)
      {
	ChangeGroup changeGroup=ChangeGroup.getGroupChangeGroup(
	  session, selectionEntityIDs);

	drawRubberBand(changeGroup);
	sendChangeGroup(changeGroup);
      }
    }

    /**
     * <p>Ungroup any selected groups.</p>
     */

    private void ungroupSelectedGroups()
    {
      ChangeGroup changeGroup = null;
      Iterator iterator = selectionEntityIDs.iterator();
      while (iterator.hasNext()) {
	EntityID entityID = (EntityID)iterator.next();
	Entity entity = sessionState.getEntity(entityID);
	if (entity instanceof Group) {
	  if (changeGroup == null) changeGroup = new ChangeGroup(userInfo);
	  changeGroup.add(new Group.Change(
	    Group.Change.DELETE,
	    null,
	    new EntityID[] {entityID}));
	}
      }
      if (changeGroup != null) {
	//drawRubberBand(changeGroup);
	sendChangeGroup(changeGroup);
      }
    }

    /**
     * <p>Add a repaint to our own list of rectangles to be repainted; these
     * will be passed to Swing on call to repaintEntity.</p>
     * <p>This allows you to: 1. call queueRepaint(s) on a changeGroup's
     * entities; 2. apply the changes; 3. call repaintEntity on the same
     * changeGroup's entities. The change will be drawn correctly.</p>
     */

    private void queueRepaint(SessionState state, EntityID entityID)
    {
      Entity entity = state.getEntity(entityID);
      if (entity != null && entity instanceof Drawable) {
	java.awt.Rectangle rect = ((Drawable)entity).getBoundingRectangle(zoomLevel);
	int x = rect.x/zoomLevel - offset.x - 4;
	int y = rect.y/zoomLevel - offset.y - 4;
	int width = rect.width/zoomLevel + 1 + 8;
	int height = rect.height/zoomLevel + 1 + 8;
	repaints.add(new java.awt.Rectangle(x, y, width, height));
      }
    }

    private void queueRepaints(SessionState state, Set entityIDs)
    {
      if (entityIDs != null) {
        Iterator iterator = entityIDs.iterator();
        while (iterator.hasNext())
          queueRepaint(state, (EntityID)iterator.next());
      }
    }

    private void queueRepaints(SessionState state, EntityID entityID[][])
    {
      if (entityID != null) for (int j = 0; j < entityID.length; j++)
	if (entityID[j] != null) for (int i = 0; i < entityID[j].length; i++)
	  queueRepaint(state, entityID[j][i]);
    }

    private void flushRepaints()
    {
      for(int i=0; i!=repaints.size(); i++)
      {
	java.awt.Rectangle rectangle=(java.awt.Rectangle)repaints.get(i);

	repaint(rectangle.x, rectangle.y,
		rectangle.width, rectangle.height);
      }

      repaints=new Vector();
    }

    private void doRepaintEntity(SessionState state, EntityID entityID)
    {
      Entity entity = state.getEntity(entityID);
      if (entity != null && entity instanceof Drawable) {
	java.awt.Rectangle rect = ((Drawable)entity).getBoundingRectangle(zoomLevel);
	int x = rect.x/zoomLevel - offset.x - 4;
	int y = rect.y/zoomLevel - offset.y - 4;
	int width = rect.width/zoomLevel + 1 + 8;
	int height = rect.height/zoomLevel + 1 + 8;
	repaint(x, y, width, height);
      }
    }

    private void repaintEntity(SessionState state, EntityID entityID)
    {
      flushRepaints();
      doRepaintEntity(state, entityID);
    }

    private void repaintEntity(SessionState state, EntityID entityID[][])
    {
      flushRepaints();

      if (entityID != null) for (int j = 0; j < entityID.length; j++)
	if (entityID[j] != null) for (int i = 0; i < entityID[j].length; i++)
	  doRepaintEntity(state, entityID[j][i]);
    }

    /**
     * <p>Produce a {@link Set} of all the unique EntityIDs in a two-dimensional
     * array of EntityIDs.</p>
     */

    private Set getEntityIDSet(EntityID[][] entityID)
    {
      LinkedHashSet result=new LinkedHashSet();

      if(entityID!=null)
      {
	for(int i=0; i!=entityID.length; i++)
	{
	  if(entityID[i]!=null)
	  {
	    for(int j=0; j!=entityID[i].length; j++)
	    {
	      result.add(entityID[i][j]);
	    }
	  }
	}
      }

      return result;
    }

    private void drawRubberBand(ChangeGroup changeGroup)
    {
      if(rubberBandSessionState!=null)
      {
	queueRepaints(rubberBandSessionState,
		      rubberBandChangeGroup.getEntityIDArrays());
      }

      rubberBandSessionState=null;
      rubberBandChangeGroup=changeGroup;

      if(changeGroup!=null)
      {
	Set entityIDSet=getEntityIDSet(changeGroup.getEntityIDArrays());
	Iterator iterator=entityIDSet.iterator();
	ChangeGroup makeResult=new ChangeGroup(null);

	while(iterator.hasNext())
	{
	  EntityID entityID=(EntityID)iterator.next();

	  Entity entity=sessionState.getEntity(entityID);

	  if((entity!=null)&&(entity instanceof Drawable))
	  {
	    makeResult.add(new Entity.Change(
	      Entity.Change.PASTE,
	      new Object[] {entity.clone()},
	      new EntityID[] {
		entityID}));
	  }
	}

	rubberBandSessionState=new SessionState();
	makeResult.apply(rubberBandSessionState);
	changeGroup.apply(rubberBandSessionState);

	repaintEntity(rubberBandSessionState, changeGroup.getEntityIDArrays());
      }

      flushRepaints();
    }

    /**
     * <p>For polylines, this includes currentMousePoint on the end (unless it is null).</p>
     */
    private ChangeGroup getCreateChangeGroup(Point currentMousePoint)
    {
      String selected=buttonGroup.getSelection().getActionCommand();
      ChangeGroup changeGroup = null;

      /*
       * Note: strictly speaking, "select" should not return a creation
       * ChangeGroup. However, "claim" draws a nice dashed bounding rectangle
       * and is ideal for rubber banding during selection.
       */

      if (mouseState == SELECTING)
	changeGroup=ChangeGroup.getNewBlackClaimChangeGroup(
	  session, mousePoint, currentMousePoint);
      else if(selected=="claim")
	changeGroup = ChangeGroup.getNewClaimChangeGroup(
	  session, mousePoint, currentMousePoint);
      else if(selected=="rectangle")
	changeGroup = ChangeGroup.getNewRectangleChangeGroup(
	  session, mousePoint, currentMousePoint);
      else if(selected=="ellipse")
	changeGroup = ChangeGroup.getNewEllipseChangeGroup(
	  session, mousePoint, currentMousePoint);
      else if((selected=="polyline")||(selected=="curvedPolyline"))
	changeGroup = ChangeGroup.getNewPolylineChangeGroup(
	  session, mousePoints, currentMousePoint,
	  selected=="curvedPolyline");
      else if(selected=="text")
	changeGroup = ChangeGroup.getNewTextChangeGroup(
	  session, mousePoint, currentMousePoint, textBeingCreated);

      return changeGroup;
    }

    private ChangeGroup getEditChangeGroup(MouseEvent e)
    {
      Point currentMousePoint=e.getPoint();

      Point translation=new Point(
	(currentMousePoint.x-mousePoint.x)*zoomLevel,
	(currentMousePoint.y-mousePoint.y)*zoomLevel);

      String selected=buttonGroup.getSelection().getActionCommand();

      Drawable drawable=(Drawable)sessionState.getEntity(editingEntityID);
      
      ChangeGroup changeGroup = null;

      if(selected=="move")
	changeGroup = ChangeGroup.getMoveChangeGroup(
	  session, editingEntityID, translation);
      else if(selected=="resize")
      {
	if((drawable instanceof Polyline)||(e.isShiftDown()))
	{
	  changeGroup = ChangeGroup.getResizeChangeGroup(
	    session, editingEntityID, editingControlPointIndex,
	    translation, true);
	}
	else
	{
	  changeGroup = ChangeGroup.getResizeChangeGroup(
	    session, editingEntityID, editingControlPointIndex,
	    translation, e.isShiftDown());
	}
      }
      else if(selected=="rotate")
	changeGroup = ChangeGroup.getRotateChangeGroup(
	  session, editingEntityID, editingControlPointIndex, translation);
      else if(selected=="flip")
	changeGroup = ChangeGroup.getFlipChangeGroup(
	  session, editingEntityID, e.isShiftDown());
      else if(selected=="movePoint")
	changeGroup = ChangeGroup.getMovePointChangeGroup(
	  session, editingEntityID, editingControlPointIndex, translation);

      return changeGroup;
    }
  }

  private class TextPropertyEditor extends JButton
  {
    private Set entityIDs;

    public TextPropertyEditor(EntityID entityID, Property property)
    {
      super();

      entityIDs = new LinkedHashSet();
      entityIDs.add(entityID);

      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent e)
        {
	  Iterator iterator = entityIDs.iterator();
	  String theText = null;
	  while(iterator.hasNext()) {
	    Drawable drawable = (Drawable)sessionState.getEntity((EntityID)iterator.next());
	    String newText = (String)drawable.getProperty(Property.TEXT).getValue();
	    if (theText == null)
	      theText = newText;
	    else if (!theText.equals(newText)) {
	      theText = "";
	      break;
	    }
	  }
	  String newText = JOptionPane.showInputDialog(SessionViewer.this, "Enter text:", theText);
	  if (newText != null && !newText.equals(""))
	    sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
	      session, entityIDs, new Property(Property.TEXT, newText)));
        }
      };
      //action.putValue(Action.SMALL_ICON, new ImageIcon(currentdir+"\\src\\data\\leave.gif"));
      action.putValue(Action.NAME, "Edit Text");
      //action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
      //action.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L)); // FIXME: this results in Alt+L becoming a shortcut for the tool bar button!
      //action.putValue(Action.SHORT_DESCRIPTION, "Disconnect from this whiteboard session");
      //action.putValue(Action.LONG_DESCRIPTION, "Disconnects you from this whiteboard session. If no one else is connected, the session will terminate.");
      //super(action);
      //configurePropertiesFromAction(action);
      setAction(action);

      propertiesPane.add(this);
    }

    public void add(EntityID entityID, Property property)
    {
      entityIDs.add(entityID);
    }
  }

  private class FontPropertyEditor extends JButton
  {
    private Set entityIDs;

    public FontPropertyEditor(EntityID entityID, Property property)
    {
      super();

      entityIDs = new LinkedHashSet();
      entityIDs.add(entityID);

      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent e)
        {
	  FontChooser fontChooser = new FontChooser(
	    SessionViewer.this,
	    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(),
	    new String[] {"12"});

	  fontChooser.show();

/*
	  Iterator iterator = entityIDs.iterator();
	  String theText = null;
	  while(iterator.hasNext()) {
	    Drawable drawable = (Drawable)sessionState.getEntity((EntityID)iterator.next());
	    String newText = (String)drawable.getProperty(Property.TEXT).getValue();
	    if (theText == null)
	      theText = newText;
	    else if (!theText.equals(newText)) {
	      theText = "";
	      break;
	    }
	  }
	  String newText = JOptionPane.showInputDialog(SessionViewer.this, "Enter text:", theText);
*/
	  Font newFont = fontChooser.getFont();
	  if (newFont != null)
	    sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
	      session, entityIDs, new Property(Property.FONT, newFont)));
        }
      };
      action.putValue(Action.NAME, "Choose Font");
      setAction(action);

      propertiesPane.add(this);
    }

    public void add(EntityID entityID, Property property)
    {
      entityIDs.add(entityID);
    }
  }

  private class IsOpenPropertyEditor extends JCheckBox
  {
    private Set entityIDs;

    public IsOpenPropertyEditor(EntityID entityID, Property property)
    {
      super();

      entityIDs = new LinkedHashSet();
      entityIDs.add(entityID);

      setText("Closed");
      setSelected(!((Boolean)property.getValue()).booleanValue());
      addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  boolean newIsOpen = e.getStateChange() == ItemEvent.DESELECTED;
	  sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
	    session, entityIDs, new Property(Property.IS_OPEN, new Boolean(newIsOpen))));
	}
      });

      propertiesPane.add(this);
    }

    public void add(EntityID entityID, Property property)
    {
      entityIDs.add(entityID);
    }
  }

  private class IsGroupNormalPropertyEditor extends JCheckBox
  {
    private Set entityIDs;

    public IsGroupNormalPropertyEditor(EntityID entityID, Property property)
    {
      super();

      entityIDs = new LinkedHashSet();
      entityIDs.add(entityID);

      setText("Transparent");
      setSelected(((Boolean)property.getValue()).booleanValue());
      addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  boolean newIsGroupNormal = e.getStateChange() == ItemEvent.SELECTED;
	  sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
	    session, entityIDs, new Property(Property.IS_GROUP_NORMAL, new Boolean(newIsGroupNormal))));
	}
      });

      propertiesPane.add(this);
    }

    public void add(EntityID entityID, Property property)
    {
      entityIDs.add(entityID);
    }
  }

  private class AlphaPanel extends JPanel
  {
    private JSlider slider;

    public AlphaPanel(int alpha)
    {
      super();

      JLabel label = new JLabel("Opacity");
      add(label);

      slider = new JSlider(0, 255, alpha);
      slider.setMajorTickSpacing(85);
      slider.setMinorTickSpacing(17);
      slider.setPaintTicks(true);
      slider.setPaintLabels(true);
      add(slider);
    }

    public int getAlpha()
    {
      return slider.getValue();
    }
  }

  private class PenPropertyEditor
  {
    private Set entityIDs;
    Pen pen;

    /**
     * <p>This should only be called once, after all calls to add().</p>
     */
    public void addSelfToPropertiesPane()
    {
      int lineStyle = pen == null ? Pen.SOLID : pen.getStyle();

      JButton button = new JButton("Line Colour");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
	{
	  final JDialog editor = new JDialog(SessionViewer.this, "Edit Line Colour", true);
	  editor.setResizable(false);
	  Container pane = editor.getContentPane();
	  Color colour = pen == null ? Color.BLACK : pen.getColor();
	  theColourChooser.setColor(colour == null ? Color.WHITE : colour); // fixme: is colour ever null?
	  pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
	  pane.add(theColourChooser);
	  final AlphaPanel alphaPanel = new AlphaPanel(colour == null ? 255 : colour.getAlpha());
	  pane.add(alphaPanel);
	  JPanel buttons = new JPanel();

	  JButton button = new JButton("Set");
	  button.setMnemonic(KeyEvent.VK_S);
	  button.setToolTipText("Sets the colour to be used for the lines.");
	  button.setDefaultCapable(true);
	  button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	      int style = pen == null ? Pen.SOLID : pen.getStyle();
	      int width = pen == null ? 1 : pen.getWidth();
	      boolean isAbsolute = pen == null ? false : pen.isAbsolute();
	      Color colour = theColourChooser.getColor();
	      activePen = new Pen(style == Pen.CLEAR ? Pen.SOLID : style,
	                          width == 0 ? 1 : width,
				  new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alphaPanel.getAlpha()),
				  isAbsolute);
	      if (entityIDs == null)
		pen = activePen;
	      else
		sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
		  session, entityIDs, new Property(Property.PEN, activePen)));
	      editor.dispose();
	    }
	  });
	  buttons.add(button);

	  button = new JButton("Cancel");
	  button.setToolTipText("Aborts the operation without changing the object or objects selected in any way.");
	  button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) { editor.dispose(); }
	  });
	  buttons.add(button);

	  pane.add(buttons);
	  editor.pack();
	  editor.setVisible(true);
	}
      });
      propertiesPane.add(button);

      final JComboBox lineStyleComboBox = new JComboBox(
	new Integer[] {new Integer(Pen.CLEAR),
		       new Integer(Pen.DASHED),
		       new Integer(Pen.DOTTED),
		       new Integer(Pen.SOLID)});
      ListCellRenderer renderer = new ListCellRenderer() {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	  final int lineStyle = ((Integer)value).intValue();
	  JComponent component = new JComponent() {
	    public void paint(Graphics g) {
	      int w = getWidth(), h = getHeight();
	      g.setColor(getBackground());
	      g.fillRect(0, 0, w, h);
	      if (lineStyle == Pen.CLEAR) return;
	      Shape.configureGraphicsPen((Graphics2D)g, 65536, new Pen(lineStyle, 2, getForeground(), false));
	      int y = h/2;
	      g.drawLine(0, y, w, y);
	    }
	  };
	  component.setPreferredSize(new Dimension(64, 16));
	  if (isSelected) {
	    component.setBackground(list.getSelectionBackground());
	    component.setForeground(list.getSelectionForeground());
	  } else {
	    component.setBackground(list.getBackground());
	    component.setForeground(list.getForeground());
	  }
	  return component;
        }
      };
      lineStyleComboBox.setRenderer(renderer);
      switch (lineStyle) {
        case Pen.CLEAR:  lineStyleComboBox.setSelectedIndex(0); break;
        case Pen.DASHED: lineStyleComboBox.setSelectedIndex(1); break;
        case Pen.DOTTED: lineStyleComboBox.setSelectedIndex(2); break;
        case Pen.SOLID:  lineStyleComboBox.setSelectedIndex(3); break;
      }
      lineStyleComboBox.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  int style = ((Integer)lineStyleComboBox.getSelectedItem()).intValue();
	  int width = pen == null ? 1 : pen.getWidth();
	  Color colour = pen == null ? null : pen.getColor();
	  boolean isAbsolute = pen == null ? false : pen.isAbsolute();
	  activePen = new Pen(style,
			      width == 0 ? 1 : width,
			      style == Pen.CLEAR ? null : colour == null ? Color.BLACK : colour,
			      isAbsolute);
	  if (entityIDs == null)
	    pen = activePen;
	  else
	    sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
	      session, entityIDs, new Property(Property.PEN, activePen)));
	}
      });
      propertiesPane.add(lineStyleComboBox);

      final JComboBox lineWidthComboBox = new JComboBox(
	new Integer[] {new Integer(1),
		       new Integer(2),
		       new Integer(3),
		       new Integer(4),
		       new Integer(6),
		       new Integer(8),
		       new Integer(10),
		       new Integer(12),
		       new Integer(15)});
      lineWidthComboBox.setEditable(true);
      lineWidthComboBox.setSelectedItem(new Integer(pen == null ? 1 : pen.getWidth()));
      lineWidthComboBox.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  int style = pen == null ? Pen.SOLID : pen.getStyle();
	  Object item = lineWidthComboBox.getSelectedItem();
	  int width;
	  if (item instanceof Integer)
	    width = ((Integer)item).intValue();
	  else try {
	    width = Integer.parseInt((String)item);
	  } catch (NumberFormatException ex) {
	    width = -1;
	  }
	  if (width < 0) {
	    lineWidthComboBox.setSelectedItem(new Integer(pen == null ? 1 : pen.getWidth()));
	    return;
	  }
	  Color colour = pen == null ? null : pen.getColor();
	  boolean isAbsolute = pen == null ? false : pen.isAbsolute();
	  if (width == 0) {
	    width = 1;
	    style = Pen.CLEAR;
	  } else if (style == Pen.CLEAR)
	    style = Pen.SOLID;
	  activePen = new Pen(style,
			      width,
			      style == Pen.CLEAR ? null : colour == null ? Color.BLACK : colour,
			      isAbsolute);
	  if (entityIDs == null)
	    pen = activePen;
	  else
	    sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
	      session, entityIDs, new Property(Property.PEN, activePen)));
	}
      });
      propertiesPane.add(lineWidthComboBox);
    }

    public PenPropertyEditor()
    {
      entityIDs = null;
      pen = activePen;
    }

    public void add(EntityID entityID, Property property)
    {
      if (entityIDs == null) {
	entityIDs = new LinkedHashSet();
	pen = (Pen)property.getValue();
      }
      entityIDs.add(entityID);
    }
  }

  private class LineEndingsPropertyEditor
  {
    private Set entityIDs;
    //int lineStyle;

    public LineEndingsPropertyEditor(EntityID entityID, Property property)
    {
      entityIDs = new LinkedHashSet();
      entityIDs.add(entityID);

      final LineEnding lineEnding[] = (LineEnding[])property.getValue();

      for (int i = 0; i < 2; i++) {
        final int whichEnd = i;
	JButton button = new JButton(whichEnd == 0 ? "Line Start" : "Line End");
	button.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e)
	  {
	    final JDialog editor = new JDialog(SessionViewer.this, whichEnd == 0 ? "Edit Line Start" : "Edit Line End", true);
	    editor.setResizable(false);
	    Container pane = editor.getContentPane();
	    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

	    JComponent subpane = new JComponent() { };
	    subpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
	    subpane.setLayout(new GridLayout(2, 2, 0, 5));

	    subpane.add(new JLabel("Arrow-head radius:"));
	    final JComboBox widthComboBox = new JComboBox(
	      new Integer[] {new Integer(2),
			     new Integer(3),
			     new Integer(4),
			     new Integer(6),
			     new Integer(8),
			     new Integer(10),
			     new Integer(12),
			     new Integer(15),
			     new Integer(20),
			     new Integer(30)});
	    widthComboBox.setEditable(true);
	    widthComboBox.setSelectedItem(new Integer(lineEnding == null || lineEnding[whichEnd] == null ? 4 : lineEnding[whichEnd].getWidth()));
	    subpane.add(widthComboBox);

	    subpane.add(new JLabel("Arrow-head length:"));
	    final JComboBox heightComboBox = new JComboBox(
	      new Integer[] {new Integer(2),
			     new Integer(3),
			     new Integer(4),
			     new Integer(6),
			     new Integer(8),
			     new Integer(10),
			     new Integer(12),
			     new Integer(15),
			     new Integer(20),
			     new Integer(30)});
	    heightComboBox.setEditable(true);
	    heightComboBox.setSelectedItem(new Integer(lineEnding == null || lineEnding[whichEnd] == null ? 6 : lineEnding[whichEnd].getHeight()));
	    subpane.add(heightComboBox);

	    pane.add(subpane);

	    subpane = new JComponent() { };
	    subpane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
	    subpane.setLayout(new BoxLayout(subpane, BoxLayout.X_AXIS));

	    JButton button = new JButton("Set");
	    button.setMnemonic(KeyEvent.VK_S);
	    button.setToolTipText("Creates a new arrow-head, or sets the dimensions of the existing one.");
	    button.setDefaultCapable(true);
	    button.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
		int width, height;
		Object item = widthComboBox.getSelectedItem();
		if (item instanceof Integer)
		  width = ((Integer)item).intValue();
		else try {
		  width = Integer.parseInt((String)item);
		} catch (NumberFormatException ex) {
		  width = 0;
		}
		item = heightComboBox.getSelectedItem();
		if (item instanceof Integer)
		  height = ((Integer)item).intValue();
		else try {
		  height = Integer.parseInt((String)item);
		} catch (NumberFormatException ex) {
		  height = 0;
		}
		LineEnding newLineEnding[] = new LineEnding[2];
		if (width > 0 && height > 0)
		  newLineEnding[whichEnd] = new LineEnding(width, height,
		    lineEnding == null || lineEnding[whichEnd] == null ? null : lineEnding[whichEnd].getBrush(),
		    lineEnding == null || lineEnding[whichEnd] == null ? null : lineEnding[whichEnd].getPen());
		else
		  newLineEnding[whichEnd] = null;
		newLineEnding[whichEnd^1] = lineEnding == null ? null : lineEnding[whichEnd^1];
		sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
		  session, entityIDs, new Property(Property.LINE_ENDINGS, newLineEnding)));
		editor.dispose();
	      }
	    });
	    subpane.add(button);

	    button = new JButton("Remove");
	    button.setMnemonic(KeyEvent.VK_R);
	    button.setToolTipText("Removes any arrow-head from this end of the line.");
	    button.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
		LineEnding newLineEnding[] = new LineEnding[2];
		newLineEnding[whichEnd] = null;
		newLineEnding[whichEnd^1] = lineEnding == null ? null : lineEnding[whichEnd^1];
		sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
		  session, entityIDs, new Property(Property.LINE_ENDINGS, newLineEnding)));
		editor.dispose();
	      }
	    });
	    subpane.add(button);

	    button = new JButton("Cancel");
	    button.setToolTipText("Aborts the operation without changing the object or objects selected in any way.");
	    button.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) { editor.dispose(); }
	    });
	    subpane.add(button);

	    pane.add(subpane);

	    editor.pack();
	    editor.setVisible(true);
	  }
	});
        propertiesPane.add(button);
      }
    }

    public void add(EntityID entityID, Property property)
    {
      entityIDs.add(entityID);
    }
  }

  private class BrushPropertyEditor extends JButton
  {
    private Set entityIDs;
    Brush brush;

    /**
     * <p>This should only be called once, after all calls to add().</p>
     */
    public void addSelfToPropertiesPane()
    {
      setText("Edit Fill");
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e)
	{
	  final JDialog editor = new JDialog(SessionViewer.this, "Edit Fill", true);
	  editor.setResizable(false);
	  Container pane = editor.getContentPane();
	  Color colour = brush == null ? Color.WHITE : brush.getColor();
	  theColourChooser.setColor(colour == null ? Color.WHITE : colour);
	  pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
	  pane.add(theColourChooser);
	  final AlphaPanel alphaPanel = new AlphaPanel(colour == null ? 255 : colour.getAlpha());
	  pane.add(alphaPanel);
	  JPanel buttons = new JPanel();

	  JButton button = new JButton("Set");
	  button.setMnemonic(KeyEvent.VK_S);
	  button.setToolTipText("Sets the colour to be used to fill the selected object or objects.");
	  button.setDefaultCapable(true);
	  button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	      Color colour = theColourChooser.getColor();
	      activeBrush = new Brush(Brush.SOLID, new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alphaPanel.getAlpha()));
	      if (entityIDs == null)
		brush = activeBrush;
	      else
		sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
		  session, entityIDs, new Property(Property.BRUSH, activeBrush)));
	      editor.dispose();
	    }
	  });
	  buttons.add(button);

	  button = new JButton("Remove Fill");
	  button.setMnemonic(KeyEvent.VK_E);
	  button.setToolTipText("Renders the contents of the shape or shapes completely transparent.");
	  button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	      activeBrush = new Brush(Brush.CLEAR, null);
	      if (entityIDs == null)
		brush = activeBrush;
	      else
		sendChangeGroup(ChangeGroup.getPropertyChangeGroup(
		  session, entityIDs, new Property(Property.BRUSH, activeBrush)));
	      editor.dispose();
	    }
	  });
	  buttons.add(button);

	  button = new JButton("Cancel");
	  button.setToolTipText("Aborts the operation without changing the object or objects selected in any way.");
	  button.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) { editor.dispose(); }
	  });
	  buttons.add(button);

	  pane.add(buttons);
	  editor.pack();
	  editor.setVisible(true);
	}
      });
      propertiesPane.add(this);
    }

    public BrushPropertyEditor()
    {
      super();
      entityIDs = null;
      brush = activeBrush;
    }

    public void add(EntityID entityID, Property property)
    {
      if (entityIDs == null) {
	entityIDs = new LinkedHashSet();
	brush = (Brush)property.getValue();
      }
      entityIDs.add(entityID);
    }
  }

  private class UserListCellRenderer extends JLabel implements ListCellRenderer
  {
    public Component getListCellRendererComponent(
      JList list,
      Object value,            // value to display
      int index,               // cell index
      boolean isSelected,      // is the cell selected
      boolean cellHasFocus)    // the list and the cell have the focus
    {
      UserInfo userInfo = (UserInfo)value;
      String s = userInfo.getName();
      setText(s);
      //setIcon((s.length() > 10) ? longIcon : shortIcon);
      if (isSelected) {
	setBackground(list.getSelectionBackground());
	setForeground(list.getSelectionForeground());
      } else {
	setBackground(list.getBackground());
	//setForeground(list.getForeground());
	setForeground(userInfo.getColor());
      }
      setEnabled(list.isEnabled());
      setFont(list.getFont());
      setOpaque(true);
      return this;
    }
  }
}
