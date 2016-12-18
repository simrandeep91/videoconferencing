package Whiteboarding.network;

import  Whiteboarding.main.*;
import  Whiteboarding.model.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * SessionClient contains the core network logic functionality.
 * 
 * Within a client version of the app, it:
 *  o runs through the initial 'logon' procedure after joining a session,
 *    requesting the necessary data from the remote SessionServer to
 *    bring its local SessionModel up to date
 *  o it relays Entity lock request/relinquish, Entity.Change and old-history
 *    request Messages from the SessionControl to the remote SessionServer and
 *    returns reply Messages when received
 *  o it acts as an intermediary between the SessionModel and the SessionControl
 *    to avoid concurrent access to the former
 *  o it receives Entity.Change Messages from the remote SessionServer and
 *    passes them on to the SessionModel for integration
 * 
 * Within a server version of the app, it:
 *  o serves the data for other clients logging on to the session, from its
 *    local (reference) SessionModel, well as old-history requests
 *  o it grants or denies Entity lock requests, by inspection of its local
 *    LockList
 *  o it validates and then integrates Entity.Changes sent from remote clients
 * 
 */

public class SessionClient implements ConnectionOwner
{
  /**
   * <p>The {@link Connection} to the server.
   * </p>
   */

  private Connection connection;

  /**
   * <p>The current {@link session}.</p>
   */

  private Session session;

  /**
   * <p>List of locks -- currently unused.</p>
   */

  private Vector lockList;

  /**
   * <p>Integer counter for assigning unique global entity identifiers.</p>
   */

  private int globalIdentifier;

  /**
   * <p>Long time offset for counting session time.</p>
   */

  private long timeOffset;

  /**
   * <p>Create a new SessionClient attached to the specified session.</p>
   */

  public SessionClient(Session session) throws IOException
  {
    assert(session!=null);

    this.session=session;

    if(session.isServerSession())
    {
      timeOffset=System.currentTimeMillis()-
	session.getSessionModel().getLatestTimeStamp();
    }
    else
    {
      SessionInfo info=session.getSessionInfo();

      assert(info!=null);
      
      System.out.println("SessionClient logging on to SessionServer at: " + info.getServerAddress());            

      connection=new Connection(
	new Socket(info.getServerAddress().getAddress(),
		   info.getServerAddress().getPort()), this); 
     
      connection.sendMessage(new Message(
	Message.REQUEST_STARTUP_DATA, null));
    }

    globalIdentifier=0;
  }

  /**
   * <p>Shut down the connection.</p>
   */

  public void close(Connection connection)
  {
    close();
  }

  /**
   * <p>Shut down the connection.</p>
   */

  public void close()
  {
    if(connection!=null)
    {
      try
      {
	connection.flush();
      }
      catch(IOException e) {}

      connection.close();
      connection=null;
    }
  }

  /**
   * <p>Only relevant to the server's SessionClient.</p>
   *
   * <p>Called to notify that a client, identified by their clientID, has
   * disconnected. The session model will be cleaned up as appropriate; in
   * particular, {@link UserInfo} entities will be updated.</p>
   */

  public void clientDisconnected(int clientID)
  {
    SessionModel sessionModel=session.getSessionModel();

    SessionState sessionState=sessionModel.getSessionState(
      Integer.MAX_VALUE);

    Vector userInfoVector=sessionState.getUserInfo();

    for(int i=0; i!=userInfoVector.size(); i++)
    {
      UserInfo userInfo=(UserInfo)userInfoVector.get(i);

      if(userInfo.getClientID()==clientID)
      {
	ChangeGroup clientDisconnected=new ChangeGroup();

	clientDisconnected.add(
	  new UserInfo.Change(
	    UserInfo.Change.SET_USER_CLIENT_ID,
	    new Object[] {new Integer(0)},
	    new EntityID[] {userInfo.getEntityID()}));

	Vector chatVector=sessionState.getChatStates();

	if(chatVector.size()>=1)
	{
	  clientDisconnected.add(
	    new ChatState.Change(
	      ChatState.Change.SAY,
	      new Object[]{" \u00fd\u00ff"+userInfo.getName()+
	"\u00fe has been disconnected from the session\u00fc"},
	      new EntityID[] {((Entity)chatVector.get(0)).getEntityID()}));
	}

	receiveUpdateRequest(clientDisconnected, -1);
      }
    }
  }
  
  /**
   * <p>Accept a {@link Message} from the remote {@link SessionServer} and
   * handle appropriately.</p>
   */

  public void receiveRemoteMessage(Message message)
  {
    switch(message.getType())
    {
      case Message.STARTUP_DATA:
      {
	receiveStartupData((SessionModel)message.getData());
	break;
      }

      case Message.CHANGE_GROUP:
      {
	receiveUpdate((ChangeGroup)message.getData());
	break;
      }

      default:
      {
	System.err.println("SessionClient received unexpected Message:" +
			   message);
      }
    }
  }

  /**
   * <p>Handle a message from the local {@link SessionServer} and respond with
   * an appropriate reply.</p>
   */

  public Message receiveLocalMessage(Message message)
  {
    switch(message.getType())
    {
      case Message.REQUEST_STARTUP_DATA:
      {
	return getStartupData();
      }

      case Message.SUGGEST_CHANGE_GROUP:
      {
	return receiveUpdateRequest((ChangeGroup)message.getData(),
				    message.getClientID());
      }

      case Message.REQUEST_SESSION_CLOSE:
      {
	return receiveCloseRequest();
      }

      default:
      {
	System.err.println("(local)SessionClient received unexpected Message: " +
			   message);
	return null;
      }
    }
  }

  /**
   * <p>Handle outgoing messages to the remote {@link SessionServer}.</p>
   */

  synchronized public void sendMessage(Message message)
    throws IOException
  {
    if(connection!=null)
    {
      connection.sendMessage(message);
    }
  }

  /**
   * <p>Obtain a STARTUP_DATA message.</p>
   */

  private Message getStartupData()
  {
    return new Message(Message.STARTUP_DATA, session.getSessionModel());
  }
    
  /**
   * <p>Send a ChangeGroup to the main server.</p>
   */

  public void sendChangeGroup(ChangeGroup changeGroup)
  {
    try
    {
      sendMessage(new Message(Message.SUGGEST_CHANGE_GROUP, changeGroup));
    }
    catch(IOException e) { e.printStackTrace(); }
  }


  /**
   * <p>Send a request to the main server that the session be closed. The
   * session will only be closed if it has no active members.</p>
   */

  public void sendCloseRequest()
  {
    try
    {
      sendMessage(new Message(Message.REQUEST_SESSION_CLOSE, null));
    }
    catch(IOException e) { e.printStackTrace(); }
  }

  /**
   * <p>Determine whether a close request is valid, i.e. whether there are
   * no active members. Return a Message if and only if the request is
   * valid.</p>
   */

  public Message receiveCloseRequest()
  {
    SessionModel sessionModel=session.getSessionModel();

    SessionState sessionState=sessionModel.getSessionState(
      Integer.MAX_VALUE);

    Vector userInfoVector=sessionState.getUserInfo();
    
    int remainingClients=0;
    
    for(int i=0; i!=userInfoVector.size(); i++)
    {
      UserInfo userInfo=(UserInfo)userInfoVector.get(i);
      
      if(userInfo.getClientID()!=0) remainingClients++;
    }

    return remainingClients==0?new Message(Message.REQUEST_SESSION_CLOSE, null):
      null;
  }

  /**
   * <p>Receive an CHANGE_GROUP from the remote server.</p>
   */
  
  private void receiveUpdate(ChangeGroup changeGroup)
  {
    session.getSessionModel().addChangeGroup(changeGroup);

    if(session.getSessionViewer()!=null)
    {
      session.getSessionViewer().receiveChangeGroup(changeGroup); 
    }
  }   

  /**
   * <p>Deals with the info from the STARTUP_DATA message, setting up the
   * SessionModel as appropriate</p>
   */

  private void receiveStartupData(SessionModel sessionModel)
  {
    session.setSessionModel(sessionModel);
  }

  /**
   * <p>Deal with an update request to the server.</p>
   */

  private Message receiveUpdateRequest(ChangeGroup changeGroup,
				       int userIdentifier)
  {
    int timeStamp=(int)(System.currentTimeMillis()-timeOffset);
    if(timeStamp<=session.getSessionModel().getLatestTimeStamp())
      timeStamp=session.getSessionModel().getLatestTimeStamp()+1;

    changeGroup.setTimeStamp(timeStamp);

    changeGroup.acceptedByServer();

    // Substitute local entity IDs for consistant global ones

    EntityID[][] identifier=changeGroup.getEntityIDArrays();

    for(int x=0; x!=identifier.length; x++)
    {
      for(int y=0; y!=identifier[x].length; y++)
      {
	if(!identifier[x][y].isGlobal())
	{
	  identifier[x][y].setUserIdentifier(userIdentifier);
	  identifier[x][y]=(EntityID)identifier[x][y].clone();
        }
      }
    }

    // Updates that cannot be successfully applied to the model are rejected

    try
    {
      session.getSessionModel().addChangeGroup(changeGroup);
      
      return new Message(Message.CHANGE_GROUP, changeGroup);
    }
    catch(Exception e)
    {
      System.out.println("Update caused exception on apply; rejecting.");
      return null;
    }
    catch(Error e)
    {
      System.out.println("Update caused error on apply; rejecting.");
      return null;
    }
  }

  /**
   * <p>Get the clientID associated with this specific connection to the
   * {@link SessionServer}.</p>
   */

  public int getClientID()
  {
    return connection.getClientID();
  }
}
