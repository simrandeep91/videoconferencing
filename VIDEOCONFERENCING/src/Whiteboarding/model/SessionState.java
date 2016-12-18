package Whiteboarding.model;

import java.awt.Point;
import java.awt.Color;

import java.io.Serializable;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * <p>SessionState stores the complete state of a whiteboard at a particular
 * point in time. It does not deal with history or undo information. It does
 * contain all the information required to render and modify a SessionState,
 * in the form of {@link Entity} objects.</p>
 * @author David Morgan
 */

public class SessionState implements Serializable, Cloneable
{
  static final long serialVersionUID = -2787603022756600422L;

  // 2. Instance variable field declarations

  /**
   * <p>Map storing all {@link Entity} objects by {@link EntityID}.</p>
   */

  private Map entityMap;

  /**
   * <p>Vector storing all {@link Entity} objects.</p>
   */

  private Vector entityVector;

  /**
   * <p>Vector storing all {@link Drawable} objects.</p>
   */

  private Vector drawableVector;

  /**
   * <p>Vector storing all {@link UserInfo} objects.</p>
   */

  private Vector userInfoVector;

  /**
   * <p>Vector storing all {@link Claim} objects.</p>
   */

  private Vector claimVector;

  /**
   * <p>Vector storing all {@link ChatState} objects.</p>
   */

  private Vector chatStateVector;

  /**
   * <p>Integer timestamp indicating number of milliseconds since the
   * session began.</p>
   */

  private int timeStamp;

  // 7. Instance constructor declarations

  /**
   * <p>Create an empty SessionState with a zero time stamp.</p>
   */

  public SessionState()
  {
    entityMap=new HashMap();
    entityVector=new Vector();
    drawableVector=new Vector();
    userInfoVector=new Vector();
    claimVector=new Vector();
    chatStateVector=new Vector();
    timeStamp=0;
  }

  // 9. Instance method declarations
  
  /**
   * <p>Obtain an Entity, given its EntityID. If the Entity does not exist,
   * null is returned.</p>
   * @param entityID the {@link EntityID} of the Entity to retrieve
   */
  
  public Entity getEntity(EntityID entityID)
  {
    // Preconditions; entityID not null
    
    assert(entityID!=null);
    
    // Method proper
    
    return (Entity)entityMap.get(entityID);
  }
  
  /**
    * <p>Add an Entity to the SessionState; its EntityID will be queried
    * directly. If an Entity with that EntityID already exists, it is
    * replaced.</p>
    * @param entity the Entity to add to the SessionState
    */

  public void addEntity(Entity entity)
  {
    // Preconditions; entity not null

    assert(entity!=null);

     // Method proper

    removeEntity(entity.getEntityID());

    entityMap.put(entity.getEntityID(), entity);
    entityVector.add(entity);

    if(entity instanceof Drawable) drawableVector.add(entity);
    if(entity instanceof UserInfo) userInfoVector.add(entity);
    if(entity instanceof Claim) claimVector.add(entity);
    if(entity instanceof ChatState) chatStateVector.add(entity);
  }

  /**
   * <p>Remove an Entity from the SessionState, given its EntityID.</p>
   */

  public void removeEntity(EntityID entityID)
  {
    // Preconditions; entityID not null

    assert(entityID!=null);

    // Method proper

    Entity entity=(Entity)entityMap.remove(entityID);

    if(entity!=null)
    {
      if(entity instanceof Drawable) drawableVector.remove(entity);
      if(entity instanceof UserInfo) userInfoVector.remove(entity);
      if(entity instanceof Claim) claimVector.remove(entity);
      if(entity instanceof ChatState) chatStateVector.remove(entity);
    }
  }

  /**
   * <p>Obtain a Vector of the top-level Drawable Entity objects (i.e. not those
   * in Groups). The Vector is ordered as the Entitys should be rendered.</p>
   * <p>Directly editing this Vector changes the rendering order.</p>
   * @return a Vector holding all Drawable objects in the state
   */

  public Vector getDrawables()
  {
    return drawableVector;
  }

  /**
   * <p>Obtain a Vector of the UserInfo Entity objects in the SessionState.</p>
   * @return a Vector holding all UserInfo objects in the state
   */

  public Vector getUserInfo()
  {
    return userInfoVector;
  }

  /**
   * <p>Obtain a Vector of the Claim Entity objects in the SessionState.</p>
   * @return a Vector holding all Claim objects in the state
   */

  public Vector getClaims()
  {
    return claimVector;
  }

  /**
   * <p>Obtain a Vector of the ChatState Entity objects in the SessionState.</p>
   * @return a Vector holding all ChatState objects in the state
   */

  public Vector getChatStates()
  {
    return chatStateVector;
  }

  /**
   * <p>Obtain a Vector of all the Entity objects in the SessionState.</p>
   * @return a Vector holding all Entity objects in the state
   */

  public Vector getAll()
  {
    return entityVector;
  }

  /**
   * <p>Set the timestamp indicating what time index the SessionState
   * represents, in milliseconds since session initialization.</p>
   * @param offset the number of milliseconds since session initialization
   */

  public void setTimeStamp(int timeStamp)
  {
    // Preconditions; timeStamp is positive or zero

    assert(timeStamp>=0);

    // Method proper

    this.timeStamp=timeStamp;
  }

  /**
   * <p>Get the timestamp indicating what time index the SessionState
   * represents, in milliseconds since session initialization.</p>
   * @return the number of milliseconds since session initialization
   */

  public int getTimeStamp()
  {
    return timeStamp;
  }

  /**
   * <p>Determine whether a ChangeGroup is allowed, given an {@link EntityID}
   * referring to the user's {@link UserInfo}.</p>
   */

  public boolean isChangeGroupAllowed(ChangeGroup changeGroup, EntityID userInfo)
  {
    Map beforeHashes=getClaimHashes();
    changeGroup.apply(this);

    // Determine whether any claims overlap in the resultant state

    for(int i=0; i!=claimVector.size(); i++)
    {
      Claim claim=(Claim)claimVector.get(i);

      for(int j=i+1; j!=claimVector.size(); j++)
      {
	Claim secondClaim=(Claim)claimVector.get(j);
	
	if(claim.getBoundingRectangle().intersects(
	  secondClaim.getBoundingRectangle()))
	{
	  changeGroup.undo(this);
	  return false;
	}
      }
    }

    Map afterHashes=getClaimHashes();
    changeGroup.undo(this);

    Iterator iterator=beforeHashes.entrySet().iterator();

    while(iterator.hasNext())
    {
      Map.Entry entry=(Map.Entry)iterator.next();

      Claim claim=(Claim)entry.getKey();
      long beforeHash=((Long)entry.getValue()).longValue();

      Long afterHashLong=(Long)afterHashes.get(claim);

      if(afterHashLong==null) // Deletion
      {
	if(!claim.getOwner().equals(userInfo)) return false;
	continue;
      }

      long afterHash=afterHashLong.longValue();

      if(beforeHash!=afterHash)
      {
	if(!claim.getOwner().equals(userInfo)) return false;
      }
    }

    return true;
  }

  /**
   * <p>Obtain a Map associating each {@link Claim} with a Long hash determined
   * by its contents.</p>
   */

  private Map getClaimHashes()
  {
    Map result=new LinkedHashMap();

    for(int i=0; i!=claimVector.size(); i++)
    {
      Claim claim=(Claim)claimVector.get(i);

      long hash=0;

      for(int j=0; j!=drawableVector.size(); j++)
      {
	Drawable drawable=(Drawable)drawableVector.get(j);
	Vector points=drawable.getControlPoints();

	boolean drawableIncluded=false;

	for(int k=0; k!=points.size()+1; k++)
	{
	  Point point=k==points.size()?drawable.getCenter():
	    (Point)points.get(k);
	  
	  if(claim.getBoundingRectangle().contains(point))
	  {
	    hash*=13;
	    hash+=point.x*point.y+point.x+point.y*73981;

	    if(!drawableIncluded)
	    {
	      drawableIncluded=true;

	      Vector properties=drawable.getProperties();

	      for(int l=0; l!=properties.size(); l++)
	      {
		hash*=13;
		hash+=((Property)properties.get(l)).getValueHash();
	      }
	    }
	  }
	}
      }

      result.put(claim, new Long(hash));
    }

    return result;
  }

  /**
   * <p>Obtain a user's color from their name. Will return black if the
   * user can't be found.</p>
   * <p>The color is determined from the user's {@link UserInfo}.</p>
   */

  public Color getUserColor(String name)
  {
    UserInfo userInfo=getUserInfo(name);

    return userInfo==null?Color.BLACK:userInfo.getColor();
  }

  /**
   * <p>Obtain a user's {@link UserInfo} from their name. Returns null if no
   * UserInfo with that name exists.</p>
   */

  public UserInfo getUserInfo(String name)
  {
    for(int i=0; i!=userInfoVector.size(); i++)
    {
      UserInfo userInfo=(UserInfo)userInfoVector.get(i);

      if(name.equals(userInfo.getName()))
      {
	return userInfo;
      }
    }

    return null;
  }

  /**
   * <p>Create a deep copy of an existing SessionState; this includes creating
   * complete copies of all Entity objects contained in the state.</p>
   */

  public Object clone()
  {
    SessionState result=new SessionState();

    Iterator iterator=entityMap.values().iterator();

    while(iterator.hasNext())
    {
      result.addEntity((Entity)((Entity)iterator.next()).clone());
    }

    result.timeStamp=timeStamp;

    return result;
  }

  /**
   * <p>Alter a SessionState as required after a complete session load.</p>
   */

  public void initializeAfterLoad()
  {
    for(int i=0; i!=entityVector.size(); i++)
    {
      Entity entity=(Entity)entityVector.get(i);

      entity.initializeAfterLoad();
    }
  }

  /**
   * <p>Return a string representation of the SessionState.</p>
   */

  public String toString()
  {
    StringBuffer result=new StringBuffer("SessionState@"+timeStamp
					 +" containing "
					 +entityMap.size()+" Entity objects:");

    Iterator iterator=entityMap.values().iterator();

    while(iterator.hasNext())
    {
      result.append("\n"+iterator.next().toString());
    }

    return new String(result);
  }
}
