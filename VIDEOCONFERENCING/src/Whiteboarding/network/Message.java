

package Whiteboarding.network;

import Whiteboarding.main.*;
import Whiteboarding.model.*;

import java.io.*;

//this is temporary:

/**
 * Message is an abstract class which encapsulates the various
 * types of message which will be sent across the network.
 * 
 * @author idb20
 * @author David Morgan
 */

public class Message implements Serializable
{

  static final long serialVersionUID = -2412731649022497746L;

  /* These are used to identify Message types, starting with
   * those used by the SessionBrowser[Server]... */

  public static final int REQUEST_SESSION_LIST=0;

  public static final int SESSION_LIST=1;

  public static final int REQUEST_SESSION_CREATE=2;

  public static final int SESSION_LIST_UPDATE=3;

  public static final int SESSION_LIST_REMOVE=4;

  /* ... and ending with those used by the Session[Client|Server] */

  public static final int REQUEST_STARTUP_DATA=103;

  public static final int STARTUP_DATA=104;

  public static final int SUGGEST_CHANGE_GROUP=105;

  public static final int DENY_CHANGE_GROUP=106;

  public static final int CHANGE_GROUP=107;

  public static final int REQUEST_CLIENT_ID=108;

  public static final int CLIENT_ID=109;

  public static final int REQUEST_SESSION_CLOSE=110;

    /* These are used to identify messages from the server(s)
     * (clients will use unique values greater than zero) */
//  public static final int CLIENTID_SERVER        = -2;
//  public static final int CLIENTID_BROWSERSERVER = -1;
//  public static final int CLIENTID_UNDEFINED     = 0;

  /* The type of this Message - should be one of the static ints above */

  private int type;

  /* This identifies a client if this Message is client->server */

  private transient int clientID;

  /* The (optional) data to send with this Message
     * (interpretation yielded by 'type') */

  private Object data;

  /**
   * <p>Create a new message with the specified integer type and data.</p>
   */

  public Message(int type, Object data)
  {
    this.type=type;
    this.data=data;
   
    assert(isDataValid());
  }

  /**
   * <p>Validate the instance data.</p>
   */

  private boolean isDataValid()
  {
    switch(type)
    {
      case REQUEST_SESSION_LIST:
      case REQUEST_STARTUP_DATA:
      case DENY_CHANGE_GROUP:
      case REQUEST_CLIENT_ID:
      case REQUEST_SESSION_CLOSE:
      {
	return data==null;
      }

      case SESSION_LIST:
      {
	return data instanceof SessionInfo[];
      }

      case REQUEST_SESSION_CREATE:
      {
	return
	  (data instanceof Object[])&&
	  (((Object[])data)[0] instanceof  SessionInfo)&&
	  ((((Object[])data)[1]==null))||
	  (((Object[])data)[1] instanceof SessionModel);
      }

      case SESSION_LIST_UPDATE:
      {
	return (data instanceof Object[])&&
	  (((Object[])data)[0] instanceof Integer)&&
	  (((Object[])data)[1] instanceof SessionInfo);
      }

      case SESSION_LIST_REMOVE:
      {
	return data instanceof Integer;
      }

      case STARTUP_DATA:
      {
	return data instanceof SessionModel;
      }

      case SUGGEST_CHANGE_GROUP:
      {
	return data instanceof ChangeGroup;
      }

      case CHANGE_GROUP:
      {
	return data instanceof ChangeGroup;
      }

      case CLIENT_ID:
      {
	return data instanceof Integer;
      }

      default:
      {
	return false;
      }
    }
  }
    
  /**
   * <p>Returns the data held by the Message, or null if there isn't any.</p>
   */
  public Object getData()
  {
    assert(isDataValid());

    return data;
  }

  /**
   * <p>Returns the integer Message type.</p>
   */

  public int getType()
  {
    return type;
  }

  /**
   * <p>This returns a String describing a Message type.</p>
   */

  public String getTypeString()
  {
    switch(type)
    {
      case REQUEST_SESSION_LIST:
	return "REQUEST_SESSION_LIST";
      case SESSION_LIST:
	return "SESSION_LIST";
      case REQUEST_SESSION_CREATE:
	return "SESSION_CREATE";
      case SESSION_LIST_UPDATE:
	return "SESSION_LIST_UPDATE";
      case SESSION_LIST_REMOVE:
	return "SESSION_LIST_REMOVE";

      case REQUEST_STARTUP_DATA:
	return "REQUEST_STARTUP_DATA";
      case STARTUP_DATA:
	return "STARTUP_DATA";
      case SUGGEST_CHANGE_GROUP:
	return "SUGGEST_CHANGE_GROUP";
      case DENY_CHANGE_GROUP:
	return "DENY_CHANGE_GROUP";
      case CHANGE_GROUP:
	return "CHANGE_GROUP";
      case REQUEST_CLIENT_ID:
	return "REQUEST_CLIENT_ID";
      case CLIENT_ID:
	return "CLIENT_ID";
      case REQUEST_SESSION_CLOSE:
	return "REQUEST_SESSION_CLOSE";
      default:
	return "UNKNOWN";    
    }
  }
    
  /**
   * <p>Returns the clientID.</p>
   */

  public int getClientID()
  {
    return clientID;
  }

  /**
   * <p>Sets the clientID. Only the receiving {@link Connection} should do
   * this.</p>
   */

  public void setClientID(int clientID)
  {
    this.clientID=clientID;
  }

  /**
   * <p>Return a String representation of the Message.</p>
   */

  public String toString()
  {
    return getTypeString()+":"+getData();
  }
}
