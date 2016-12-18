package Whiteboarding.model;

import java.awt.Color;

import java.io.Serializable;

/**
 * <p>Class representing the UserInfo Entity.</p>
 * @author David Morgan
 */

public class UserInfo extends Entity implements Serializable, Cloneable
{
  static final long serialVersionUID = -6177991539630265273L;

  // 2. Instance variable field declarations

  /**
   * <p>The user's name.</p>
   */

  private String name;

  /**
   * <p>The user's identifying color.</p>
   */

  private Color color;

  /**
   * <p>The user's viewable rectangle.</p>
   */

  private java.awt.Rectangle viewable;

  /**
   * <p>The user's zoom level.</p>
   */

  private int zoomLevel;

  /**
   * <p>The user's clientID.</p>
   */

  private int clientID;

  // 4. Static member inner class declarations

  /**
   * Subclass of {@link Entity.Change} providing extra operations on UserInfos,
   * and implementing existing operations differently where necessary.
   */ 

  public static class Change extends Entity.Change
  {
    static final long serialVersionUID = 6513336937229995001L;

    // 4.1 Static variable field declarations

    /**
     * <p>Indicates an operation that changes the user's name.</p>
     *
     * <p>First and only parameter is a String giving the new name.</p>
     *
     * <p>First and only target specifies the UserInfo to alter.</p>
     */

    public static final int SET_USER_NAME=11;

    /**
     * <p>Indicates an operation that changes the user's identifying color.</p>
     *
     * <p>First and only parameter is a Color object giving the new color.</p>
     *
     * <p>First and only target specifies the UserInfo to alter.</p>
     */

    public static final int SET_USER_COLOR=12;

    /**
     * <p>Indicates an operation that changes the user's viewing area.</p>
     *
     * <p>First and only parameter is a java.awt.Rectangle giving the
     * new area.</p>
     *
     * <p>First and only target specifies the UserInfo to alter.</p>
     */

    public static final int SET_USER_VIEWING_AREA=13;

    /**
     * <p>Indicates an operation that changes the user's zoom level.</p>
     *
     * <p>First and only parameter is an Integer giving the new zoom level.</p>
     *
     * <p>First and only target specifies the UserInfo to alter.</p>
     */

    public static final int SET_USER_ZOOM_LEVEL=14;

    /**
     * <p>Indicates an operation that sets the user's client ID. This is
     * intended to allow the identify a UserInfo with a particular client.</p>
     *
     * <p>First and only parameter is an Integer giving the new client ID.</p>
     *
     * <p>First and only target specifies the UserInfo to alter.</p>
     */

    public static final int SET_USER_CLIENT_ID=-10;

    // 4.7 Instance constructor declarations

    /**
     * <p>Change modifies a UserInfo in a specified way.</p>
     */
   
    public Change(int changeType, Object[] parameter, EntityID[] target)
    {
      initialize(changeType, parameter, target);
    }

    // 4.9 Instance method declarations

    protected void initialize(int changeType, Object[] parameter,
			      EntityID[] target)
    {
      // Determine whether this change is handled here or by Entity.Change

      if(isParent(changeType))
      {
	super.initialize(changeType, parameter, target);
      }
      else
      {
	this.changeType=(byte)changeType;
	this.parameter=parameter;
	this.target=target;
	
	assert(isDataValid());
      }
    }

    /**
     * Determine whether a particular changeType should be handled by the parent
     * class.
     */

    private boolean isParent(int changeType)
    {
      switch(changeType)
      {
	case DELETE:
	case PASTE:
	{
	  return true;
	}
	
	default:
	{
	  return false;
	}
      }
    }

    /**
     * <p>Validate the main data.</p>
     */

    protected boolean isDataValid()
    {
      switch(changeType)
      {
	case CREATE:
	{
	  return (parameter==null)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case SET_USER_NAME:
	{
	  return (parameter!=null)&&
	    (parameter.length==1)&&(parameter[0]!=null)&&
	    (parameter[0] instanceof String)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case SET_USER_COLOR:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0]!=null)&&(parameter[0] instanceof Color)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case SET_USER_VIEWING_AREA:
	{
	  return (parameter!=null)&&(parameter.length==1)&
	    (parameter[0]!=null)&&(parameter[0] instanceof java.awt.Rectangle)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case SET_USER_ZOOM_LEVEL:
	case SET_USER_CLIENT_ID:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0]!=null)&&(parameter[0] instanceof Integer)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}
     
	default:
	{
	  return super.isDataValid();
	}
      }
    }

    /**
     * <p>Validate the undo data.</p>
     */

    protected boolean isUndoDataValid()
    {
      switch(changeType)
      {
	case CREATE:
	{
	  return undoData==null;
	}

	case SET_USER_NAME:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    (undoData[0] instanceof String);
	}

	case SET_USER_COLOR:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    (undoData[0] instanceof Color);
	}

	case SET_USER_VIEWING_AREA:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    (undoData[0] instanceof java.awt.Rectangle);
	}

	case SET_USER_ZOOM_LEVEL:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    (undoData[0] instanceof Integer)&&
	    (((Integer)undoData[0]).intValue()>0);
	}

	case SET_USER_CLIENT_ID:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    (undoData[0] instanceof Integer);
	}
      
	default:
	{
	  return super.isUndoDataValid();
	}
      }
    }

    /**
     * <p>Apply the change. This must be overriden by subclasses of Entity.Change
     * that provide new functionality.</p>
     */

    public void apply(SessionState state)
    {
      // Determine whether this change is handled here or by Entity.Change

      if(isParent(changeType))
      {
	super.apply(state);
	return;
      }

      // Preconditions; supplied state is not null

      assert(state!=null);

      // Method proper

      switch(changeType)
      {
	case CREATE:
	{
          // Gather data for undo

          // Make the change

	  UserInfo userInfo=new UserInfo(target[0], "",
					 Color.BLACK,
					 new java.awt.Rectangle(),
					 65536);
	  state.addEntity(userInfo);
	  break;
	}

	case SET_USER_NAME:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] { userInfo.name };

	  // Make the change

	  userInfo.name=(String)parameter[0];
	  break;
	}

	case SET_USER_COLOR:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] { userInfo.color };

	  // Make the change

	  userInfo.color=(Color)parameter[0];
	  break;
	}

	case SET_USER_VIEWING_AREA:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] { userInfo.viewable };

	  // Make the change

	  userInfo.viewable=(java.awt.Rectangle)parameter[0];
	  break;
	}

	case SET_USER_ZOOM_LEVEL:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] { new Integer(userInfo.zoomLevel) };

	  // Make the change

	  userInfo.zoomLevel=((Integer)parameter[0]).intValue();
	  break;
	}

	case SET_USER_CLIENT_ID:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] { new Integer(userInfo.clientID) };

	  // Make the change

	  userInfo.clientID=((Integer)parameter[0]).intValue();
	  break;
	}
  
	default:
	{
	  assert(false);
	}
      }

      // Postconditions; undo data is valid

      assert(isUndoDataValid());
    }

    /**
     * <p>Undo the change. This must be overriden by subclasses of Entity.Change
     * that provide new functionality.</p>
     */

    public void undo(SessionState state)
    {
      // Determine whether this change is handled here or by Entity.Change

      if(isParent(changeType))
      {
	super.undo(state);
	return;
      }

      // Preconditions; supplied state not null, undo data valid

      assert(state!=null);
      assert(isUndoDataValid());

      // Method proper

      switch(changeType)
      {
	case CREATE:
	{
	  state.removeEntity(target[0]);
	  break;
	}

	case SET_USER_NAME:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);
	  userInfo.name=(String)undoData[0];
	  break;
	}

	case SET_USER_COLOR:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);
	  userInfo.color=(Color)undoData[0];
	  break;
	}

	case SET_USER_VIEWING_AREA:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);
	  userInfo.viewable=(java.awt.Rectangle)undoData[0];
	  break;
	}

	case SET_USER_ZOOM_LEVEL:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);
	  userInfo.zoomLevel=((Integer)undoData[0]).intValue();
	  break;
	}

	case SET_USER_CLIENT_ID:
	{
	  UserInfo userInfo=(UserInfo)state.getEntity(target[0]);
	  userInfo.clientID=((Integer)undoData[0]).intValue();
	  break;
	}
      
	default:
	{
	  assert(false);
	}
      }
    }

    /**
     * <p>Obtain a new Change representing the reverse of the operation. Intended
     * to be used during undo, so an undo operation is still a new operation.</p>
     */

    public Entity.Change reverse()
    {
      // Determine whether this change is handled here or by Entity.Change

      if(isParent(changeType))
      {
	return super.reverse();
      }

      // Preconditions

      assert(isUndoDataValid());

      // Method proper

      switch(changeType)
      {
	case CREATE:
	{
	  return new Entity.Change(DELETE, undoData, target);
	}

	case SET_USER_NAME:
	case SET_USER_COLOR:
	case SET_USER_VIEWING_AREA:
	case SET_USER_ZOOM_LEVEL:
	case SET_USER_CLIENT_ID:
	{
	  return new UserInfo.Change(changeType, undoData, target);
	}

	default:
	{
	  assert(false); return null;
	}
      }
    }

    /**
     * <p>Alter the Change to make it suitable after a session load operation.
     * Changes that assign clientIDs are altered to assign zero clientIDs to
     * indicate that they are not in the loaded session.</p>
     */

    public void initializeAfterLoad()
    {
      if(changeType==SET_USER_CLIENT_ID)
      {
	parameter[0]=new Integer(0);
      }
    }

    /**
     * <p>Obtain a String representation of the Change.</p>
     * @return a String describing the effect of applying this Change
     */

    public String toString()
    {
      if(isParent(changeType))
      {
	return "UserInfo:"+super.toString();
      }
      else
      {
	switch(changeType)
	{
	  case CREATE:
	  {
	    return "UserInfo.Change:CREATE:"+target[0];
	  }

	  case SET_USER_NAME:
	  {
	    return "UserInfo.Change:SET_USER_NAME:"+target[0]+":"
	      +parameter[0];
	  }

	  case SET_USER_COLOR:
	  {
	    return "UserInfo.Change:SET_USER_COLOR:"+target[0]+":"
	      +parameter[0];
	  }

	  case SET_USER_VIEWING_AREA:
	  {
	    return "UserInfo.Change:SET_USER_VIEWING_AREA:"+target[0]+":"
	      +parameter[0];
	  }

	  case SET_USER_ZOOM_LEVEL:
	  {
	    return "UserInfo.Change:SET_USER_ZOOM_LEVEL:"+target[0]+":"
	      +parameter[0];
	  }

	  case SET_USER_CLIENT_ID:
	  {
	    return "UserInfo.Change:SET_USER_CLIENT_ID:"+target[0]+":"
	      +parameter[0];
	  }
	  
	  default:
	  {
	    return "UserInfo.Change:UNKNOWN";
	  }
	}
      }	
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new UserInfo with the specified identifier, name, color,
   * viewable area and zoom level.</p>
   */

  protected UserInfo(EntityID identifier, String name, Color color,
		     java.awt.Rectangle viewable, int zoomLevel)
  {
    super(identifier);

    this.name=name;
    this.color=color;
    this.viewable=viewable;
    this.zoomLevel=zoomLevel;

    assert(isUserInfoDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isUserInfoDataValid()
  {
    if(!isEntityDataValid()) return false;

    return (name!=null)&&(color!=null)&&(viewable!=null)&&(zoomLevel>0);
  }

  /**
   * <p>Obtain the user name of the client as a String.</p>
   * @return the user's name
   */

  public String getName()
  {
    return name;
  }

  /**
   * <p>Obtain the user's identifying {@link java.awt.Color}.</p>
   * @return the user's identifiying {@link java.awt.Color}
   */

  public Color getColor()
  {
    return color;
  }

  /**
   * <p>Obtain the user's viewable area as a {@link java.awt.Rectangle}.</p>
   * @return the user's viewable area
   */

  public java.awt.Rectangle getViewableArea()
  {
    return viewable;
  }

  /**
   * <p>Obtain the user's zoom level, as the number of org.davidmorgan.jinn.units displayed
   * in a single pixel.</p>
   * @return the number of org.davidmorgan.jinn.units to a single pixel
   */

  public int getZoomLevel()
  {
    return zoomLevel;
  }

  /**
   * <p>Obtain the clientID associated with the UserInfo. The default is zero;
   * a value of zero should be used when there is no valid clientID, e.g. when
   * the user has left the session.</p>
   */

  public int getClientID()
  {
    return clientID;
  }

  /**
   * <p>Create a deep copy of the UserInfo.</p>
   */

  public Object clone()
  {
    UserInfo result=(UserInfo)super.clone();
    
    result.name=new String(name);
    result.color=new Color(color.getRed(),
			   color.getGreen(),
			   color.getBlue());
    result.viewable=new java.awt.Rectangle(viewable);
    // result.zoomLevel=zoomLevel;

    return result;
  }

  /**
   * <p>Alter the UserInfo to make it suitable after a session load operation.
   * ClientIDs are zeroed to indicate that the users are no longer connected.</p>
   */
  
  public void initializeAfterLoad()
  {
    clientID=0;
  }

  /**
   * <p>Obtain a String representation of the UserInfo.</p>
   */

  public String toString()
  {
    return "UserInfo:("+super.toString()+"):"
      +getName()+":"
      +getColor()+":"
      +getViewableArea()+":"
      +getZoomLevel();
  }
}
