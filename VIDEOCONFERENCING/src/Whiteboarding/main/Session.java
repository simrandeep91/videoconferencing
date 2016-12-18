package Whiteboarding.main;

import Whiteboarding.network.*;
import Whiteboarding.model.*;
import Whiteboarding.userInterface.*;
import java.io.*;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import videoconferencing.MainFrame;

/** 
 * Session is used mainly to contain the modules associated with a given
 * org.davidmorgan.jinn.session, either for a client or server (SessionModel, SessionClient,
 * SessionControl and SessionServer). It ensures that the component modules
 * are constructed in the correct sequence and that they have references to each
 * other as appropriate.
 * 
 * @author idb20
 * @author David Morgan
 */

public class Session
{
  // Session state

  private SessionInfo sessionInfo;
  private SessionClient sessionClient;
  private SessionModel sessionModel;
  private SessionBrowser sessionBrowser;
  private SessionListener sessionListener;

  private boolean isServerSession;

  // Session viewer, if this machine is a client

  private SessionViewer sessionViewer;

  // Local server, if this machine is the server

  private SessionServer localServer;

  // Parameters for SessionViewer creation

  private boolean isReadOnly;
  private String userName;
  private java.awt.Color userColor;

  // Dialog for displaying wait message

  private JDialog waitDialog;

  // Boolean indicating whether the user has cancelled the join

  private boolean cancelled;

  // Boolean indicating whether the user has definitely not cancelled the join

  private boolean approved;

  // Boolean indicating whether the session is closing

  private boolean closing;
 
  /**
   * <p>Create a new Session local to the server with the specified
   * {@link SessionInfo}, {@link SessionModel} and {@link SessionDeathNotify}
   * object to be notified when it closes.
   */

  public Session(SessionInfo sessionInfo, SessionModel sessionModel,
		 SessionListener sessionListener)
    throws IOException
  {
    assert(sessionInfo!=null);

    this.isServerSession=true;
    this.sessionInfo=sessionInfo;
    this.sessionModel=sessionModel;
    this.sessionListener=sessionListener;
    this.closing=false;
    
    // Create a blank Model if one was not loaded from a file

    if(this.sessionModel==null)
    {
      this.sessionModel=new SessionModel(new SessionState());
      
      // Add a new ChatState
      
      ChangeGroup createChat=new ChangeGroup(null);
      
      EntityID entityID=new EntityID(-1);

      createChat.add(new ChatState.Change(
	ChatState.Change.CREATE,
	null,
	new EntityID[] {entityID}));

      createChat.setTimeStamp(1);

      this.sessionModel.addChangeGroup(createChat);
    }

    localServer=new SessionServer(this);
    sessionClient=new SessionClient(this);
  }

  /**
   * <p>Create a new Session belonging to a client using the specified
   * {@link SessionInfo}, {@link SessionModel}, boolean specifying whether
   * it is read only, and user name and color.</p>
   */

  public Session(SessionBrowser sessionBrowser,
		 SessionInfo sessionInfo, SessionModel sessionModel,
		 boolean isReadOnly, String userName,
		 java.awt.Color userColor)
    throws IOException
  {
    assert(sessionInfo!=null);

    this.isServerSession=false;
    this.sessionBrowser=sessionBrowser;
    this.sessionInfo=sessionInfo;
    this.sessionModel=sessionModel;

    this.isReadOnly=isReadOnly;
    this.userName=userName;
    this.userColor=userColor;

    this.cancelled=false;

    this.closing=false;
        
    sessionClient=new SessionClient(this);

    if(sessionModel==null)
    {
      this.approved=true;
      //System.out.println("hello sessionmodel is null");
      //displayWaitDialog();
    }
    else
    {
      this.approved=true;
      setSessionModel(sessionModel);
    }
  }

  /* This is used to shut down the Session - all Threads will be terminated,
   * so that when the last reference is discarded the Session will truly die.
   * The SessionViewer probably holds the last reference */

  public void close()
  {
    if(!closing)
    {
      closing=true;

      if(sessionViewer!=null)
      {
	if(!sessionViewer.close())
	{
	  closing=false;
	  return;
	}

	sessionViewer=null;
      }
      
      if(localServer!=null)
      {
	localServer.close();
	localServer=null;
      }

      sessionClient.close();
      
      if(sessionListener!=null)
      {
	sessionListener.handleSessionClose(this);
      }
    }
  }

  public SessionInfo getSessionInfo()
  {
    return sessionInfo;
  }

  public SessionClient getSessionClient()
  {
    return sessionClient;
  }

  public SessionModel getSessionModel()
  {
    return sessionModel;
  }

  public SessionViewer getSessionViewer()
  {
    return sessionViewer;
  }

  public void setSessionModel(SessionModel sessionModel)
  {
    this.sessionModel=sessionModel;

    if(!isServerSession)
    {
      if(sessionViewer==null)
      {
	destroyWaitDialog();

	while((!cancelled)&&(!approved))
	{
	  try
	  {
	    Thread.sleep(100);
	  }
	  catch(InterruptedException e) {}
	}

	if(approved)
	{
	  sessionViewer=new SessionViewer(this, isReadOnly, userName, userColor);
	  sessionViewer.initialize();
	  if(sessionViewer!=null) sessionViewer.show();
	}
      }
      else
      {
	sessionViewer.handleModelLoad();
      }
    }
  }

  public boolean isServerSession()
  {
    return isServerSession;
  }

  public void displayWaitDialog()
  {
    JOptionPane pane = new JOptionPane(
      "Receiving contents from server...",
      JOptionPane.PLAIN_MESSAGE,
      JOptionPane.DEFAULT_OPTION);
    
    pane.setOptions(new Object[] {"Cancel"});
    
    waitDialog=pane.createDialog(sessionBrowser, "Please wait");
    waitDialog.show();
    Object selectedValue=pane.getValue();

    if(selectedValue.toString().equals("uninitializedValue"))
    {
      approved=true;
    }
    else
    {
      cancelled=true;
      JOptionPane.showMessageDialog(MainFrame.container,"Server Connection Lost. Try Again.","Connection", JOptionPane.INFORMATION_MESSAGE); 
      close();
      }
  }

  public void destroyWaitDialog()
  {
    if(waitDialog!=null)
    {
      waitDialog.dispose();
      waitDialog=null;
    }
  }

  public String toString()
  {
    return "Session containg:\n"+sessionInfo+"\n"+sessionClient+"\n"+
      sessionModel+"\n"+sessionViewer+"\n"+localServer;
  }
}
