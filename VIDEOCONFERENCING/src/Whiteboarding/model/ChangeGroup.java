package Whiteboarding.model;

import Whiteboarding.userInterface.SessionViewer;
import Whiteboarding.main.Session;

import Whiteboarding.model.Arc.Change;

import java.awt.Point;

import java.io.Serializable;

import java.util.Vector;
import java.util.Set;
import java.util.Iterator;

/**
 * <p>ChangeGroup encompases an ordered collection of changes to Entity objects
 * representing the effect of a single user change on the whiteboard.
 * Once a ChangeGroup object has been created it may be applied to a SessionState
 * object to apply the changes. It may also be applied in reverse to undo
 * the changes.</p>
 * <p>The class also contains static methods for creation ChangeGroups.</p>
 * @author David Morgan
 * @author Ben Davis
 */

public class ChangeGroup implements Serializable
{
  static final long serialVersionUID = -8131464787445337206L;
  
  // 2. Instance variable field declarations

  /**
   * <p>Vector storing the Entity.Change objects.</p>
   */

  private Vector changeVector;

  /**
   * <p>Time stamp indicating when the ChangeGroup was approved by the
   * server, in milliseconds since session initialization.</p>
   */

  private int timeStamp;

  /**
   * <p>{@link EntityID} referring to the {@link UserInfo} object associated
   * with the client causing the change. A null value indicates that for
   * some reason there is no such UserInfo, for example during UserInfo
   * creation.</p>
   */

  private EntityID userInfo;

  // 4. Instance constructor declarations

  /**
   * <p>Create an empty ChangeGroup.</p>
   * @deprecated Use ChangeGroup(EntityID userInfo) instead.
   */

  public ChangeGroup()
  {
    changeVector=new Vector();
  }

  /**
   * <p>Create an empty ChangeGroup owned by the client with {@link UserInfo}
   * referred to by the supplied {@link EntityID}.</p>
   */

  public ChangeGroup(EntityID userInfo)
  {
    this();
    this.userInfo=userInfo;
  }

  // 5. Static method declarations

  /**
   * <p>Obtain a ChangeGroup that carries out the SAY operation on a the
   * first ChatState Entity present in the SessionState.</p>
   */

  public static ChangeGroup getSayChangeGroup(Session session,
					      String text)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();
    SessionState sessionState=session.getSessionViewer().getSessionState();

    ChangeGroup sayChange=new ChangeGroup(userInfo);

    Vector chatVector=sessionState.getChatStates();
    ChatState chat=(ChatState)chatVector.get(0);
    EntityID identifier=chat.getEntityID();

    sayChange.add(
      new ChatState.Change(
	ChatState.Change.SAY,
	new Object[] {text},
	new EntityID[] {chat.getEntityID()}));

    return sayChange;
  }

  /**
   * <p>Obtain a ChangeGroup that deletes all the selected objects.</p>
   */

  public static ChangeGroup getDeleteChangeGroup(Session session,
						 Set entityIDs)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();
    
    assert(!entityIDs.isEmpty());
    
    ChangeGroup delete = new ChangeGroup(userInfo);
    Iterator iterator = entityIDs.iterator();
    
    while (iterator.hasNext())
    {
      addDeleteChange(delete, session, (EntityID)iterator.next());
    }
			 
    return delete;
  }
  
  /**
   * <p>Add to an existing ChangeGroup a Change that deletes the Entity
   * with the specified EntityID.</p>
   */

  private static void addDeleteChange(
    ChangeGroup changeGroup, Session session, EntityID entityID)
  {
    Entity entity=session.getSessionViewer().getSessionState()
      .getEntity(entityID);
    
    changeGroup.add(new Entity.Change(
      Entity.Change.DELETE,
      null,
      new EntityID[] {entityID}));

    if(entity instanceof Group)
    {
      Vector members=((Group)entity).getMembers();
      
      for(int i=0; i!=members.size(); i++)
      {
	addDeleteChange(changeGroup, session,
			((Entity)members.get(i)).getEntityID());
      }
    }
  }



  /**
   * <p>Obtain a ChangeGroup that brings all the selected Drawables to the
   * front of the whiteboard.</p>
   */

  public static ChangeGroup getBringToFrontChangeGroup(Session session,
						 Set entityIDs)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();

    assert(!entityIDs.isEmpty());

    ChangeGroup bringToFront = new ChangeGroup(userInfo);

    Iterator iterator = entityIDs.iterator();

    while (iterator.hasNext())
      bringToFront.add(new Drawable.Change(
	Drawable.Change.BRING_TO_FRONT,
	null,
	new EntityID[] {(EntityID)iterator.next()}));

    return bringToFront;
  }

  /**
   * <p>Obtain a ChangeGroup that sends all the selected Drawables to the
   * back of the org.davidmorgan.jinn.</p>
   */

  public static ChangeGroup getSendToBackChangeGroup(Session session,
						     Set entityIDs)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();

    assert(!entityIDs.isEmpty());

    ChangeGroup sendToBack = new ChangeGroup(userInfo);

    Iterator iterator = entityIDs.iterator();

    while (iterator.hasNext())
      sendToBack.add(new Drawable.Change(
	Drawable.Change.SEND_TO_BACK,
	null,
	new EntityID[] {(EntityID)iterator.next()}));

    return sendToBack;
  }

  public static ChangeGroup getPropertyChangeGroup(Session session,
						   Set entityIDs,
						   Property property)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();

    ChangeGroup propertyChange = new ChangeGroup(userInfo);

    Iterator iterator=entityIDs.iterator();

    while(iterator.hasNext())
    {
      EntityID identifier=(EntityID)iterator.next();

      propertyChange.add(
	new Drawable.Change(
	  Drawable.Change.EDIT,
	  new Object[] {property},
	  new EntityID[] {identifier}));
    }

    return propertyChange;
  }

  public static ChangeGroup getGroupChangeGroup(Session session, Set entityIDSet)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();
    SessionState sessionState=session.getSessionViewer().getSessionState();

    int size=0;
    Iterator iterator=entityIDSet.iterator();
    while(iterator.hasNext())
    {
      EntityID entityID=(EntityID)iterator.next();

      Entity entity=sessionState.getEntity(entityID);

      if((entity!=null)&!(entity instanceof Claim))
      {
	size++;
      }
    }
    
    EntityID[] identifiers=new EntityID[size+1];
    iterator=entityIDSet.iterator();

    int index=0;
    identifiers[index++]=new EntityID(
      session.getSessionClient().getClientID());
    
    while(iterator.hasNext())
    {
      EntityID entityID=(EntityID)iterator.next();

      Entity entity=sessionState.getEntity(entityID);

      if((entity!=null)&!(entity instanceof Claim))
      {
	identifiers[index++]=entityID;
      }
    }
    
    ChangeGroup createDrawable=new ChangeGroup(userInfo);
    
    createDrawable.add(new Group.Change(Group.Change.GROUP,
					  null,
					identifiers));
    
    return createDrawable;
  }

  public static ChangeGroup getNewClaimChangeGroup(
    Session session, Point topLeft, Point bottomRight)
  {
    SessionViewer sessionViewer=session.getSessionViewer();
    EntityID userInfo=sessionViewer.getUserInfo();
    Point offset=sessionViewer.getOffset();
    int zoomLevel=sessionViewer.getZoomLevel();

    ChangeGroup createDrawable=new ChangeGroup(userInfo);
    
    EntityID drawable=new EntityID(session.getSessionClient().getClientID());

    createDrawable.add(
      new Claim.Change(
	Claim.Change.CREATE,
	null,
	new EntityID[] {drawable}));

    createDrawable.add(
      new Claim.Change(
	Claim.Change.SET_CLAIM_OWNER,
	null,
	new EntityID[] {drawable, userInfo}));
    
    createDrawable.add(
      new Claim.Change(
	Claim.Change.MOVE,
	new Object[] {Drawable.absolutePoint(offset, zoomLevel, topLeft.x, topLeft.y)},
	new EntityID[] {drawable}));
      
    createDrawable.add(
      new Claim.Change(
	Claim.Change.RESIZE,
	new Object[] {new Integer(2),
		      new Point(
			(bottomRight.x-topLeft.x)*zoomLevel,
			(bottomRight.y-topLeft.y)*zoomLevel),
		      new Boolean(true)},
	new EntityID[] {drawable}));
      
    return createDrawable;
  }

  public static ChangeGroup getNewBlackClaimChangeGroup(
    Session session, Point topLeft, Point bottomRight)
  {
    SessionViewer sessionViewer=session.getSessionViewer();
    EntityID userInfo=sessionViewer.getUserInfo();
    Point offset=sessionViewer.getOffset();
    int zoomLevel=sessionViewer.getZoomLevel();

    ChangeGroup createDrawable=new ChangeGroup(userInfo);
    
    EntityID drawable=new EntityID(session.getSessionClient().getClientID());

    createDrawable.add(
      new Claim.Change(
	Claim.Change.CREATE,
	null,
	new EntityID[] {drawable}));
    
    createDrawable.add(
      new Claim.Change(
	Claim.Change.MOVE,
	new Object[] {Drawable.absolutePoint(offset, zoomLevel, topLeft.x, topLeft.y)},
	new EntityID[] {drawable}));
      
    createDrawable.add(
      new Claim.Change(
	Claim.Change.RESIZE,
	new Object[] {new Integer(2),
		      new Point(
			(bottomRight.x-topLeft.x)*zoomLevel,
			(bottomRight.y-topLeft.y)*zoomLevel),
		      new Boolean(true)},
	new EntityID[] {drawable}));
      
    return createDrawable;
  }

  public static ChangeGroup getNewRectangleChangeGroup(
    Session session, Point topLeft, Point bottomRight)
  {
    SessionViewer sessionViewer=session.getSessionViewer();
    EntityID userInfo=sessionViewer.getUserInfo();
    Point offset=sessionViewer.getOffset();
    int zoomLevel=sessionViewer.getZoomLevel();
    Pen pen=sessionViewer.getActivePen();
    Brush brush=sessionViewer.getActiveBrush();

    ChangeGroup createDrawable=new ChangeGroup(userInfo);

    EntityID drawable=new EntityID(session.getSessionClient().getClientID());

    createDrawable.add(
      new Rectangle.Change(
	Rectangle.Change.CREATE,
	null,
	new EntityID[] {drawable}));

    createDrawable.add(
      new Rectangle.Change(
	Rectangle.Change.MOVE,
	new Object[] {Drawable.absolutePoint(offset, zoomLevel, topLeft.x, topLeft.y)},
	new EntityID[] {drawable}));

    createDrawable.add(
      new Rectangle.Change(
	Rectangle.Change.RESIZE,
	new Object[] {new Integer(2),
		      new Point(
			(bottomRight.x-topLeft.x)*zoomLevel,
			(bottomRight.y-topLeft.y)*zoomLevel),
		      new Boolean(true)},
	new EntityID[] {drawable}));

    addBrushAndPenToChangeGroup(createDrawable, brush, pen);

    return createDrawable;
  }

  public static ChangeGroup getNewEllipseChangeGroup(
    Session session, Point topLeft, Point bottomRight)
  {
    SessionViewer sessionViewer=session.getSessionViewer();
    EntityID userInfo=sessionViewer.getUserInfo();
    Point offset=sessionViewer.getOffset();
    int zoomLevel=sessionViewer.getZoomLevel();
    Pen pen=sessionViewer.getActivePen();
    Brush brush=sessionViewer.getActiveBrush();

    ChangeGroup createDrawable=new ChangeGroup(userInfo);

    EntityID drawable=new EntityID(session.getSessionClient().getClientID());

    createDrawable.add(
      new Ellipse.Change(
	Ellipse.Change.CREATE,
	null,
	new EntityID[] {drawable}));

    createDrawable.add(
      new Ellipse.Change(
	Ellipse.Change.MOVE,
	new Object[] {Drawable.absolutePoint(offset, zoomLevel, topLeft.x, topLeft.y)},
	new EntityID[] {drawable}));

    createDrawable.add(
      new Ellipse.Change(
	Ellipse.Change.RESIZE,
	new Object[] {new Integer(4),
		      new Point(
			(bottomRight.x-topLeft.x)*zoomLevel,
			(bottomRight.y-topLeft.y)*zoomLevel),
		      new Boolean(true)},
	new EntityID[] {drawable}));

    addBrushAndPenToChangeGroup(createDrawable, brush, pen);

    return createDrawable;
  }

  public static ChangeGroup getNewPolylineChangeGroup(
    Session session, Vector points, Point extra, boolean isCurved)
  {
    SessionViewer sessionViewer=session.getSessionViewer();
    EntityID userInfo=sessionViewer.getUserInfo();
    Point offset=sessionViewer.getOffset();
    int zoomLevel=sessionViewer.getZoomLevel();
    Pen pen=sessionViewer.getActivePen();
    Brush brush=sessionViewer.getActiveBrush();

    boolean isClosed=false;

    ChangeGroup createDrawable=new ChangeGroup(userInfo);

    EntityID drawable=new EntityID(session.getSessionClient().getClientID());

    createDrawable.add(
      new Polyline.Change(
	Polyline.Change.CREATE,
	null,
	new EntityID[] {drawable}));

    long cx=0;
    long cy=0;

    if (extra!=null) points.add(extra);

    int size=points.size();

    if(size>2)
    {
      Point first=(Point)points.get(0);

      Point last=isCurved?(Point)points.get(points.size()-1-
					    ((points.size()&1)==1?1:0)):
	(Point)points.get(points.size()-1);

      double distance=first.distance(last);

      if(distance<4)
      {
	isClosed=true;
      }
    }

    if(isClosed)
    {
      if(isCurved)
      {
	if((size&1)==0)
	{
	  size--;
	}
	else
	{
	  points.set(points.size()-2, new Point((Point)points.get(0)));
	}
      }
      else
      {
	size--;
      }
    }

    for(int i=0; i!=size; i++)
    {
      Point absolutePoint=Drawable.absolutePoint(offset, zoomLevel, (Point)points.get(i));

      cx+=absolutePoint.x;
      cy+=absolutePoint.y;

      createDrawable.add(
	new Polyline.Change(
	  Polyline.Change.ADD_POINT,
	  new Object[] {new Integer(i), absolutePoint},
	  new EntityID[] {drawable}));
    }

    cx/=size;
    cy/=size;

    createDrawable.add(
      new Polyline.Change(
	Polyline.Change.MOVE_POINT,
	new Object[] {new Integer(-1), new Point((int)cx, (int)cy)},
	new EntityID[] {drawable}));

    if(isCurved)
    {
      createDrawable.add(new Polyline.Change(
	Polyline.Change.EDIT,
	new Object[] {new Property(Property.IS_STRAIGHT_LINES,
				   new Boolean(false))},
	new EntityID[] {drawable}));
    }

    if(isClosed)
    {
      createDrawable.add(new Polyline.Change(
	Polyline.Change.EDIT,
	new Object[] {new Property(Property.IS_OPEN,
				   new Boolean(false))},
	new EntityID[] {drawable}));
    }

    addBrushAndPenToChangeGroup(createDrawable, brush, pen);

    if(extra!=null) points.remove(points.size()-1);

    return createDrawable;
  }

  public static ChangeGroup getNewTextChangeGroup(
    Session session, Point topLeft, Point bottomRight, String text)
  {
    SessionViewer sessionViewer=session.getSessionViewer();
    EntityID userInfo=sessionViewer.getUserInfo();
    Point offset=sessionViewer.getOffset();
    int zoomLevel=sessionViewer.getZoomLevel();
    Pen pen=sessionViewer.getActivePen();
    Brush brush=sessionViewer.getActiveBrush();

    ChangeGroup createDrawable=new ChangeGroup(userInfo);

    EntityID drawable=new EntityID(session.getSessionClient().getClientID());

    createDrawable.add(
      new Text.Change(
	Text.Change.CREATE,
	null,
	new EntityID[] {drawable}));

    createDrawable.add(
      new Text.Change(
	Text.Change.EDIT,
	new Object[] {new Property(Property.TEXT, text)},
	new EntityID[] {drawable}));

    createDrawable.add(
      new Text.Change(
	Text.Change.MOVE,
	new Object[] {Drawable.absolutePoint(offset, zoomLevel, topLeft.x, topLeft.y)},
	new EntityID[] {drawable}));

    createDrawable.add(
      new Text.Change(
	Text.Change.RESIZE,
	new Object[] {new Integer(0),
		      new Point(
			(bottomRight.x-topLeft.x)*zoomLevel,
			(bottomRight.y-topLeft.y)*zoomLevel),
		      new Boolean(true)},
	new EntityID[] {drawable}));

    addBrushAndPenToChangeGroup(createDrawable, brush, pen);

    return createDrawable;
  }

  private static void addBrushAndPenToChangeGroup(
    ChangeGroup changeGroup, Brush brush, Pen pen)
  {
    if(brush!=null)
    {
      changeGroup.add(new Shape.Change(
	Shape.Change.EDIT,
	new Object[] {new Property(Property.BRUSH, brush)},
	new EntityID[] {changeGroup.getEntityIDArrays()[0][0]}));
    }
				 
    if(pen!=null)
    {
      changeGroup.add(new Shape.Change(
	Shape.Change.EDIT,
	new Object[] {new Property(Property.PEN, pen)},
	new EntityID[] {changeGroup.getEntityIDArrays()[0][0]}));
    }
  }

  public static ChangeGroup getMoveChangeGroup(
    Session session, EntityID target, Point translation)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();

    ChangeGroup changeDrawable=new ChangeGroup(userInfo);

    changeDrawable.add(
      new Drawable.Change(
	Drawable.Change.MOVE,
	new Object[] {translation},
	new EntityID[] {target}));

    return changeDrawable;
  }

  public static ChangeGroup getResizeChangeGroup(
    Session session, EntityID target, int pointIndex,
    Point translation, boolean aboutCenter)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();

    if(pointIndex==-1) return null;

    ChangeGroup changeDrawable=new ChangeGroup(userInfo);

    changeDrawable.add(
      new Drawable.Change(
	Drawable.Change.RESIZE,
	new Object[] {new Integer(pointIndex),
		      translation,
		      new Boolean(!aboutCenter)},
	new EntityID[] {target}));

    return changeDrawable;
  }

  public static ChangeGroup getRotateChangeGroup(
    Session session, EntityID target, int pointIndex, Point translation)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();

    if(pointIndex==-1) return null;

    ChangeGroup changeDrawable=new ChangeGroup(userInfo);

    changeDrawable.add(
      new Drawable.Change(
	Drawable.Change.ROTATE,
	new Object[] {new Integer(pointIndex),
		      translation},
	new EntityID[] {target}));

    return changeDrawable;
  }

  public static ChangeGroup getFlipChangeGroup(
    Session session, EntityID target, boolean vertical)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();
    ChangeGroup changeDrawable=new ChangeGroup(userInfo);

    changeDrawable.add(
      new Drawable.Change(
	Drawable.Change.FLIP,
	new Object[] {new Boolean(vertical)},
	new EntityID[] {target}));

    return changeDrawable;
  }
    
  public static ChangeGroup getMovePointChangeGroup(
    Session session, EntityID target, int pointIndex, Point translation)
  {
    EntityID userInfo=session.getSessionViewer().getUserInfo();
    SessionState sessionState=session.getSessionViewer().getSessionState();
    ChangeGroup changeDrawable=new ChangeGroup(userInfo);
    
    if(sessionState.getEntity(target) instanceof Polyline)
    {
      changeDrawable.add(
	new Polyline.Change(
	  Polyline.Change.MOVE_POINT,
	  new Object[] {new Integer(pointIndex), translation},
	  new EntityID[] {target}));
    }
    else
    {
      changeDrawable.add(
	new Drawable.Change(
	  Drawable.Change.MOVE,
	  new Object[] {translation},
	  new EntityID[] {target}));
    }
      
    return changeDrawable;
  }

  // 9. Instance method declarations

  /**
   * <p>Add a {@link Change} to the ChangeGroup. Changes must be added in
   * the order in which they are to be carried out.</p>
   * @param change the Change to add
   */

  public void add(Entity.Change change)
  {
    // Preconditions; change is not null

    assert(change!=null);

    // Method proper

    changeVector.add(change);
  }

  /**
   * <p>Apply the effects of a ChangeGroup to a {@link SessionState}.</p>
   * @param state the {@link SessionState} to apply the changes to
   */

  public void apply(SessionState state)
  {
    // Preconditions; state is not null

    assert(state!=null);

    // Method proper

    for(int i=0; i!=changeVector.size(); i++)
    {
      ((Entity.Change)changeVector.get(i)).apply(state, timeStamp);
    }
  }

  /**
   * <p>Undo the effects of a ChangeGroup in a {@link SessionState} by
   * applying the undo method of each Change, in reverse order.</p>
   * @param state the {@link SessionState} to undo the changes in
   */

  public void undo(SessionState state)
  {
    // Preconditions; state is not null

    assert(state!=null);

    // Method proper

    for(int i=changeVector.size()-1; i!=-1; i--)
    {
      ((Entity.Change)changeVector.get(i)).undo(state, timeStamp);
    }
  }

  /**
   * <p>Obtain a ChangeGroup that has the reverse effect of this
   * ChangeGroup. Intended to allow provision of undo functionality
   * which does not regress the history.</p>
   * <p>Note that the ChangeGroup must have been applied using
   * apply() or added to a {@link StateModel} before this is a valid
   * operation.</p>
   * <p>The resulting ChangeGroup has the same timestamp as the
   * creating ChangeGroup.</p>
   * @return a ChangeGroup comprised of changes obtain by
   * Change.reverse()
   */

  public ChangeGroup reverse()
  {
    ChangeGroup result=new ChangeGroup(userInfo);
    result.setTimeStamp(timeStamp);

    for(int i=changeVector.size()-1; i!=-1; i--)
    {
      result.add(((Entity.Change)changeVector.get(i)).reverse(timeStamp));
    }

    return result;
  }

  /**
   * <p>Set the timestamp indicating when the ChangeGroup was approved by
   * the server in milliseconds since session initialization.</p>
   * @param timeStamp the number of milliseconds since session initialization
   */

  public void setTimeStamp(int timeStamp)
  {
    // Preconditions; timeStamp is positive or zero

    assert(timeStamp>=0);

    // Method proper

    this.timeStamp=timeStamp;
  }

  /**
   * <p>Get the timestamp indicating when the ChangeGroup was approved by
   * the server in milliseconds since session initialization.</p>
   * @return the number of milliseconds since session initialization
   */

  public int getTimeStamp()
  {
    return timeStamp;
  }

  /**
   * <p>Get the an array of arrays of EntityID used by each Change object.
   * This is provided so that the server can correct invalid EntityID's, which
   * in turn allows clients to refer to Entity objects that they are in the
   * process of creating.</p>
   * @return an array of arrays of {@link EntityID} objects used by each
   * Entity.Change
   */

  public EntityID[][] getEntityIDArrays()
  {
    EntityID[][] result=new EntityID[changeVector.size()][];

    for(int i=0; i!=changeVector.size(); i++)
    {
      result[i]=((Entity.Change)changeVector.get(i)).getEntityIDs();
    }

    return result;
  }

  /**
   * <p>Return an {@link EntityID} referring to the {@link UserInfo} object
   * associated with the client causing the change. A null value indicates
   * that for some reason there is no such UserInfo, for example during UserInfo
   * creation.</p>
   */

  public EntityID getUserInfo()
  {
    return userInfo;
  }

  /**
   * <p>Set the {@link EntityID} referring to the {@link UserInfo} object
   * associated with the client causing the change. A null value indicates
   * that for some reason there is no such UserInfo, for example during UserInfo
   * creation.</p>
   */

  public void setUserInfo(EntityID userInfo)
  {
    this.userInfo=userInfo;
  }

  /**
   * <p>Signal to a ChangeGroup that it has been accepted by the server. Any
   * extra configuration that needs doing is done, by calling
   * Change.acceptedByServer() on each member Change.</p>
   */

  public void acceptedByServer()
  {
    for(int i=0; i!=changeVector.size(); i++)
    {
      Entity.Change change=(Entity.Change)changeVector.get(i);

      change.acceptedByServer();
    }
  }
 
  /**
   * <p>Alter a ChangeGroup as required after a complete session load. The
   * initializeAfterLoad() method of each individual change is called.</p>
   */

  public void initializeAfterLoad()
  {
    for(int i=0; i!=changeVector.size(); i++)
    {
      Entity.Change change=(Entity.Change)changeVector.get(i);

      change.initializeAfterLoad();
    }
  }

  /**
   * <p>Obtain a String representation of the ChangeGroup.</p>
   */

  public String toString()
  {
    StringBuffer result=new StringBuffer("ChangeGroup@"+timeStamp+" containing "
					 +changeVector.size()
					 +" Entity.Change objects:");

    for(int i=0; i!=changeVector.size(); i++)
    {
      result.append("\n"+changeVector.get(i));
    }

    return new String(result);
  }
}
