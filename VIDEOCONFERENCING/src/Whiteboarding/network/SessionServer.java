package Whiteboarding.network;

import Whiteboarding.main.*;

import java.util.*;
import java.net.*;
import java.io.*;

/**
 * <p>The SessionServer holds a ServerSocket open and accepts connections.
 * It relays Messages from remote SessionClients to the local
 * SessionClient. It may block Entity lock request or Entity.Change
 * Messages from remote SessionClients deemed to be 'read-only' users
 * within this session.</p>
 * 
 * @author idb20
 * @author David Morgan
 */

public class SessionServer implements Runnable, ConnectionOwner
{

  /**
   * <p>Base port to start session server on.</p>
   */

  private static int BASE_PORT=6143;

  /**
   * <p>Port increment between server creation attempts.</p>
   */

  static final int PORT_INCREMENT=1;

  /**
   * <p>Number of ports to try when creating server.</p>
   */

  static final int PORT_TRIES=20;
        
  /**
   * <p>The ServerSocket to which SessionClients connect.</p>
   */

  private ServerSocket serverSocket;

  /**
   * <p>The Server {@link session}.</p>
   */

  private Session session;

  /* 
   * <p>Vector of client {@link Connection} objects.</p>
   */

  private Vector clientList;

  /*
   * <p>Counter for assigning unique clientIDs.</p>
   */

  private int nextClientID = 1;

  /**
   * The constructor initialises the ServerSocket for this session and
   * then waits for and accepts incoming connections.
   */

  public SessionServer(Session session) throws IOException
  {
    assert(session!=null);
    this.session=session;

    InetSocketAddress serverAddress;
    int port=BASE_PORT+=PORT_INCREMENT;
    int tries=0;

    while ((serverSocket==null)&&(tries<PORT_TRIES))
    {
      try
      {
	serverSocket = new ServerSocket(port);
      }
      catch (Exception e)
      {  
	port=BASE_PORT+=PORT_INCREMENT;
	tries++;
      }
    }

    if (serverSocket==null)
    {
      throw new IOException("SessionServer failed to find a free port");
    } 

    serverSocket.setSoTimeout(128);

    // Make the address of the ServerSocket available in the SessionInfo
      
    serverAddress = new InetSocketAddress(
      InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort());
    serverAddress.getHostName();
    session.getSessionInfo().setServerAddress(serverAddress);

    System.out.println("(" + session.getSessionInfo().getSubject() +
		       ") SessionServer started at:" + serverAddress);
    
    Thread serverThread=new Thread(this);
    serverThread.start();
   }

  /**
   * <p>Initialises the list of connected clients to be empty, then
   * monitors the ServerSocket for incoming connection requests, each of which
   * it accepts and adds (in a new Connection object) to the clientList. The
   * clientList will occasionally be purged of dead Connections.</p>
   */
  
  public void run()
  {
    clientList=new Vector();

    // Accept loop

    outer:
    while(true)
    {
      Socket newUser = null;
      
      try
      {
	/* Get the next connection... */
	newUser = serverSocket.accept();
      }
      catch (SocketTimeoutException e)
      {

      }
      catch (Exception e)
      {
	/* Err.... unless the ServerSocket has been closed, keep on trying */
	if (true == serverSocket.isClosed()) break outer;
      }
            
      if (null != newUser)
      {
	try
	{
	  Connection connection;

	  connection=new Connection(newUser, this, nextClientID++);

	  clientList.add(connection);

	  session.getSessionInfo().setPopulation(
	    session.getSessionInfo().getPopulation()+1);

	  System.out.println("(" + session.getSessionInfo().getSubject() +
			     ") SessionClients connected (+1): "+
			     clientList.size());
	}
	catch (IOException e)
	{
	  System.err.println("SessionServer failed to accept incoming connection");
	  e.printStackTrace();
	}
      }
      
      purgeClientList();
    }
    
    close();           
    System.out.println("(" + session.getSessionInfo().getSubject() +
		       ") SessionServer terminated.");
  }
    
  private int getNextClientID()
  {
    return nextClientID++;
  }
    
  /**
   * <p>This method scans the list of clients and removes ones which have been
   * disconnected.</p>
   */

  private void purgeClientList()
  {
    for (int i = clientList.size() - 1;i >= 0;i--)
    {
      Connection client = (Connection)clientList.elementAt(i);
                   
      if (true == client.isClosed())
      {
	assert(clientList.size() == session.getSessionInfo().getPopulation());
	clientList.removeElementAt(i);
	session.getSessionInfo().setPopulation(session.getSessionInfo().getPopulation() - 1);
	System.out.println("(" + session.getSessionInfo().getSubject() + ") SessionClients connected (-1): "+ clientList.size());
	assert(clientList.size() == session.getSessionInfo().getPopulation());
      }
    }
  }
  
  /**
   * Shut down the ServerSocket, closing all connections.
   */

  private boolean closing=false;

  public void close()
  {
    try
    {
      if (!serverSocket.isClosed()) serverSocket.close();
    }
    catch (IOException e)
    { 
      System.err.println("Error closing ServerSocket whilst closing SessionServer.");
      e.printStackTrace();
    }

    for (int i = 0; i<clientList.size(); i++)
    {
      Connection client = (Connection)clientList.elementAt(i);
      if (!client.isClosed()) client.close();
    }

    session.close();
  }
    
  /**
   * <p>Accepts messages from a remote client and passes them to the local
   * {@link SessionClient} as necessary, possibly sending a reply.</p>
   */

  public synchronized void receiveRemoteMessage(Message message)
  {
    System.out.println("SessionServer received a Message of type: " +
		       message.getTypeString());
    
    Connection client = getMessageClient(message);
    if (client==null)
    {
      System.err.println("SessionServer received a Message of type " +
			 message.getTypeString() +
			 " from an unknown client.");
      return;
    }
        
    switch(message.getType())
    {
      case Message.REQUEST_STARTUP_DATA:
	break;
      case Message.SUGGEST_CHANGE_GROUP:
	break;
      case Message.REQUEST_SESSION_CLOSE:
	break;
      default:
	System.err.println("SessionServer received unexpected Message of type: " +
			   message.getTypeString());
    }
        
    Message reply = session.getSessionClient().receiveLocalMessage(message);

    boolean distribute = false;

    if (reply==null) return;

    switch(reply.getType())
    {
      case Message.STARTUP_DATA:
	break;
      case Message.DENY_CHANGE_GROUP:
	break;
      case Message.CHANGE_GROUP:
	distribute = true;
	break;
      case Message.REQUEST_SESSION_CLOSE:
	close();
	return;
      default:
	System.err.println("SessionServer received unexpected Message of type: " +
			   message.getTypeString() +
			   " from the local SessionClient.");
    }

    if (distribute)
    {
      for (int i = 0;i < clientList.size();i++)
      {
	client = (Connection)clientList.elementAt(i);
	try
	{
	  client.sendMessage(reply);
	}
	catch (IOException e) { /* close will be dealt with elsewhere */ }
      }
    }
    else
    {
      try
      {
	client.sendMessage(reply);
      }
      catch (IOException e)
      {
	e.printStackTrace();
	client.close();
      }
    }
  }

  private Connection getMessageClient(Message message)
  {
    for (int i=0; i<clientList.size(); i++)
    {
      Connection connection = (Connection)clientList.elementAt(i);
      if (connection.getClientID() == message.getClientID())
      {
	return connection;
      }
    }
        
    return null;
  }

  /**
   * <p>Called when a connection to a SessionClient has been
   * closed - it then purges the client list to remove the dead client,
   * and informs the local sessionClient.</p>
   */

  public void close(Connection connection)
  {
    session.getSessionClient().clientDisconnected(connection.getClientID());

    purgeClientList();
  }
}
