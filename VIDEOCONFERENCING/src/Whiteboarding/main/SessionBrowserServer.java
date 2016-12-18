package Whiteboarding.main;

import Whiteboarding.network.*;
import Whiteboarding.model.*;

import java.net.*;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import videoconferencing.MainFrame;

/**
 * <p>The SessionBrowserServer acts as the root for the server
 * version of the app. It sets up a ServerSocket which listens
 * for connections from remote {@link SessionBrowser}s. It will return
 * lists of hosted sessions on request, service clients joining
 * those sessions and spawn new sessions when requested.</p>
 * 
 * @author idb20
 * @author David Morgan
 */

public class SessionBrowserServer
  implements Runnable, ConnectionOwner, SessionListener, SessionInfoListener
{

  /**
   * <p>The port the SessionBrowserServer will listen on.</p>
   */

  static int BASE_PORT  = 3572; 

  /**
   * <p>The ServerSocket to which SessionBrowsers connect.</p>
   */
    
  private static ServerSocket serverSocket;

  /**
   * <p>Vector of {@link Session} objects describing hosted Sessions.</p>
   */

  private static Vector sessions;

  /**
   * <p>Vector of {@link Connection} objects corresponding to connected
   * clients.</p>
   */

  private static Vector clients;

  /**
   * <p>A counter for assigning unique clientIDs.</p>
   */

  private int nextClientID=0;

  /**
   * <p>The Thread listening for connections.</p>
   */

  private Thread listenThread;

  /**
   * <p>Create a new SessionBrowserServer and start listening for clients.</p>
   */

  public SessionBrowserServer() throws IOException
  {
    sessions = new Vector();
    clients  = new Vector();
    System.out.println(BASE_PORT);
    serverSocket = new ServerSocket(BASE_PORT);

    InetSocketAddress serverAddress=
      new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),
			    serverSocket.getLocalPort());

    serverAddress.getHostName();

    System.out.println("SessionBrowserServer started at " + serverAddress);
    listenThread=new Thread(this);
    listenThread.start();
    JOptionPane.showMessageDialog(MainFrame.container,"New Session Started.", "WhiteBoard Session", JOptionPane.INFORMATION_MESSAGE);
    String[] arg = null;
    Client.main(arg);
  }

  /**
   * <p>Accept loop for new connections.</p>
   */

  public void run()
  {
    try
    {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SessionBrowserServer.class.getName()).log(Level.SEVERE, null, ex);
            }
      while(true)
      {
	Socket socket=serverSocket.accept();
	Connection connection=new Connection(socket, this, ++nextClientID);
	
	clients.add(connection);
	sendSessionList(connection);

	System.out.println("SessionBrowsers connected (+1): "+ clients.size());        
	// Take this opportunity to purge dead clients from the list

	purgeClientVector();
      }
    }
    catch (IOException e)
    {
      System.out.println("SessionBrowserServer listen loop failed.");
      e.printStackTrace();
    }   
  }

  /**
   * <p>Called when a connection to a SessionBrowser has been closed.</p>
   */

  public void close(Connection connection)
  {
    purgeClientVector();
  }
    
  /**
   * <p>Called when a locally hosted {@link Session} dies.</p>
   */

  public void handleSessionClose(Session session)
  {
    sessions.remove(session);

    System.out.println("Session closed (-1): "+sessions.size());

    sendSessionList(null);
  }

  /**
   * <p>Remove disconnected clients from the client Vector.</p>
   */
   
  private void purgeClientVector()
  {
    for (int i=clients.size()-1; i>=0; i--)
    {
      Connection connection = (Connection)clients.elementAt(i);
                   
      if (connection.isClosed())
      {
	clients.removeElementAt(i);
	System.out.println("SessionBrowsers connected (-1): "+ clients.size());
      }
    }
  }

  /**
   * <p>Receive notification of changes to SessionInfo objects which must
   * be propagated to clients.</p>
   */

  public void sessionInfoChange(SessionInfo sessionInfo)
  {
    for (int i=0; i!=sessions.size(); i++)
    {
      if(sessionInfo==(SessionInfo)((Session)sessions.get(i)).getSessionInfo())
      {
	sendSessionListUpdate(null, i);
      }
    }
  }

  /**
   * <p>Send a Message containg an array of {@link SessionInfo} objects to the 
   * client on the specified {@link Connection}. If the supplied Connection is
   * null, a Message is sent to every connected client.</p>
   */

  private void sendSessionList(Connection connection)
  {
    SessionInfo[] infoList = new SessionInfo[sessions.size()];

    for (int i=0; i!=sessions.size(); i++)
    {
      // Clone SessionInfo objects to force serialization to count changes

      infoList[i]=(SessionInfo)((Session)sessions.get(i)).getSessionInfo()
	.clone();
    }
        
    Message message = new Message(Message.SESSION_LIST, infoList);

    try
    {
      if(connection!=null)
      {
	connection.sendMessage(message);
      }
      else
      {
	for(int i=0; i!=clients.size(); i++)
	{
	  ((Connection)clients.get(i)).sendMessage(message);
	}
      }
    }
    catch(IOException e)
    {
      System.out.println("SessionBrowserServer failed to send a Message of type SESSION_LIST.");
      e.printStackTrace();
    }
  }

  /**
   * <p>Send a Message containg an updated {@link SessionInfo} object to the 
   * client on the specified {@link Connection}. If the supplied Connection is
   * null, a Message is sent to every connected client.</p>
   */

  private void sendSessionListUpdate(Connection connection, int index)
  {
    SessionInfo info=(SessionInfo)
      ((Session)sessions.get(index)).getSessionInfo().clone();
        
    Message message = new Message(Message.SESSION_LIST_UPDATE,
				  new Object[] {new Integer(index),
						info});

    try
    {
      if(connection!=null)
      {
	connection.sendMessage(message);
      }
      else
      {
	for(int i=0; i!=clients.size(); i++)
	{
	  ((Connection)clients.get(i)).sendMessage(message);
	}
      }
    }
    catch(IOException e)
    {
      System.out.println("SessionBrowserServer failed to send a Message of type SESSION_LIST_UPDATE.");
      e.printStackTrace();
    }
  }

  /**
   * <p>Create a new {@link Session} hosted on this server.</p>
   */

  private void createSession(SessionInfo sessionInfo, SessionModel sessionModel)
  {
    try
    {
      sessions.add(new Session(sessionInfo, sessionModel, this));

      sendSessionListUpdate(null, sessions.size()-1);

      sessionInfo.setListener(this);
    }
    catch (IOException e)
    {
      System.err.println("SessionBrowserServer failed to create a new Session.");
      e.printStackTrace();
    }     
  }
    
  /**
   * <p>Handle a message from a remove SessionBrowser.</p>
   *
   */

  public void receiveRemoteMessage(Message message)
  {
    assert(message!=null);
       
    Connection connection=getMessageClient(message);

    assert(connection!=null);
        
    switch(message.getType())
    {
      case Message.REQUEST_SESSION_LIST:
      {
	sendSessionList(connection);

	break;
      }
      
      case Message.REQUEST_SESSION_CREATE:
      {
	createSession((SessionInfo)((Object[])message.getData())[0],
		      (SessionModel)((Object[])message.getData())[1]);

	break;
      }

      default:
      {
	System.out.println("SessionBrowserServer received unexpected Message:");
	System.out.println(message);
      }
    }
  }

  /**
   * <p>Get the {@link Connection} of a client, given a {@link Message}
   * received from them. Returns null if the client is unknown.</p>
   */

  private Connection getMessageClient(Message message)
  {
    for (int i=0; i<clients.size(); i++)
    {
      Connection connection=(Connection)clients.get(i);

      if(connection.getClientID()==message.getClientID())
      {
	return connection;
      }
    }

    return null;
  }
}
