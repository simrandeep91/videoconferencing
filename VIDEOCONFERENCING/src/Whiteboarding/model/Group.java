package Whiteboarding.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Font;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.Vector;

/**
 * <p>Class representing the Group Entity.</p>
 * <p>Groups must be created using the GROUP operation.</p>
 * @author David Morgan
 */

public class Group extends Drawable implements Serializable, Cloneable
{
  static final long serialVersionUID = 7609763207732640399L;

  // 2. Instance variable field declarations

  /**
   * <p>A Vector of {@link EntityID} instances corresponding to the member
   * Drawables.</p>
   */

  protected Vector members;

  /**
   * <p>A boolean indicating whether the Group is in normal or labelled
   * mode.</p>
   */

  protected boolean labelled;

  /**
   * <p>A String containing the text to display in labelled mode.</p>
   */

  protected String text;

  // 4. Static member inner class declarations

  /**
   * <p>Subclass of {@link Drawable.Change} providing extra operations on
   * Groups, and implementing existing operations differently where
   * necessary.</p>
   */ 

  public static class Change extends Drawable.Change
  {
    static final long serialVersionUID = -3358273997274153075L;

    // 4.1 Static variable field declarations

    /**
      * Indicates an operation that creates a new Group from a selection of
      * Drawables.
      *
      * There are no parameters.
      *
      * The first target specified the identifier of the new group, and the
      * remaining targets specify the Drawables to be included in the new
      * Group.
      */

    public static final int GROUP=17;

    /**
     * <p>Indicates an operation that adds one or more Drawables to an existing
     * Group.</p>
     *
     * <p>There are no parameters.</p>
     *
     * <p>The first target specifies the Group. The remaining targets specify
     * Drawables to be added to it.</p>
     */

    public static final int ADD_TO_GROUP=9;

    /**
      * <p>Indicates an operation that removes one or more Drawables from an
      * existing Group.</p>
      *
      * <p>There are no parameters.</p>
      *
      * <p>The first target specifies the Group. The remaining targets specify
      * Drawables to be removed from it.</p>
      */

    public static final int REMOVE_FROM_GROUP=10;
   
    // 4.7 Instance constructor declarations

    /**
     * <p>Protected empty constructor.</p>
     */
    
    protected Change() {}

    /**
     * <p>Change modifies a Group in a specified way.</p>
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
     * <p>Determine whether a particular changeType should be handled by the
     * parent class.</p>
     */

    private boolean isParent(int changeType)
    {
      switch(changeType)
      {
	case CREATE:
	case GROUP:
	case DELETE:
	case ADD_TO_GROUP:
	case REMOVE_FROM_GROUP:
	{
	  return false;
	}
	
	default:
	{
	  return true;
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
	  return false;
	}

	case GROUP:
	case ADD_TO_GROUP:
	case REMOVE_FROM_GROUP:
	{
	  if(!((parameter==null)&&(target!=null)&&
	       (target.length>1))) return false;

	  for(int i=0; i!=target.length; i++)
	  {
	    if(target[i]==null) return false;
	  }

	  return true;
     	}

	case DELETE:
	{
	  return (parameter==null)&&(target!=null)&&
	    (target.length==1)&&(target[0]!=null);
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
	  assert(false);
	}

	case GROUP:
	case ADD_TO_GROUP:
	case REMOVE_FROM_GROUP:
	{
	  return (undoData==null);
	}

	case DELETE:
	{
	  if(!((undoData!=null)&&(undoData.length>1))) return false;
	  for(int i=0; i!=undoData.length; i++)
	  {
	    if(!(undoData[i] instanceof EntityID)) return false;
	  }

	  return true;
	}

	default:
	{
	  return super.isUndoDataValid();
	}
      }
    }

    /**
     * <p>Apply the change. This must be overriden by subclasses of Group.Change
     * that provide new functionality.</p>
     */

    public void apply(SessionState state)
    {
      // Determine whether this change is handled here or by Drawable.Change

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
	  assert(false);
	  break;
	}

	case GROUP:
	{
          // Gather data for undo

          // Make the change

	  Vector controlPoints=new Vector();
	  controlPoints.add(new Point(0,0));
	  controlPoints.add(new Point(0,0));
	  controlPoints.add(new Point(0,0));
	  controlPoints.add(new Point(0,0));

	  Group group=new Group(target[0], new Point(0, 0), controlPoints,
				new Vector(), false, null,
				new Vector(), false, "Group");

	  state.addEntity(group);

	  for(int i=1; i!=target.length; i++)
	  {
	    group.add(target[i], state);
	  }

	  break;
	}

	case DELETE:
	{
	  Group group=(Group)state.getEntity(target[0]);

          // Gather data for undo

	  undoData=new Object[group.members.size()+1];
	  undoData[0]=target[0];
	  for(int i=1; i!=group.members.size()+1; i++)
	  {
	    undoData[i]=((Entity)group.members.get(i-1)).getEntityID();
	  }

          // Make the change

	  for(int i=group.members.size()-1; i!=-1; i=group.members.size()-1)
	  {
	    group.remove(((Entity)group.members.get(i)).getEntityID(), state);
	  }

	  state.removeEntity(target[0]);

	  break;
	}
	
	case ADD_TO_GROUP:
	{
	  Group group=(Group)state.getEntity(target[0]);

          // Gather data for undo

          // Make the change

	  for(int i=1; i!=target.length; i++)
	  {
	    group.add(target[i], state);
	  }

	  break;
	}

	case REMOVE_FROM_GROUP:
	{
	  Group group=(Group)state.getEntity(target[0]);

          // Gather data for undo

          // Make the change

	  for(int i=1; i!=target.length; i++)
	  {
	    group.remove(target[i], state);
	  }
  
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
      // Determine whether this change is handled here or by Drawable.Change

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
	  assert(false);
	  break;
	}

	case GROUP:
	{
	  Group group=(Group)state.getEntity(target[0]);

	  for(int i=group.members.size()-1; i!=-1; i=group.members.size()-1)
	  {
	    group.remove(((Entity)group.members.get(i)).getEntityID(), state);
	  }

	  state.removeEntity(target[0]);

	  break;
	}

	case DELETE:
	{
	  Vector controlPoints=new Vector();
	  controlPoints.add(new Point(-1,-1));
	  controlPoints.add(new Point(1,-1));
	  controlPoints.add(new Point(1,1));
	  controlPoints.add(new Point(-1,1));

	  Group group=new Group((EntityID)undoData[0], new Point(0, 0), controlPoints,
				new Vector(), false, null, new Vector(),
				false, "Group");

	  state.addEntity(group);

	  for(int i=1; i!=undoData.length; i++)
	  {
	    group.add((EntityID)undoData[i], state);
	  }

	  break;
	}

	case ADD_TO_GROUP:
	{
	  Group group=(Group)state.getEntity(target[0]);

	  for(int i=1; i!=target.length; i++)
	  {
	    group.remove(target[i], state);
	  }

	  break;
	}

	case REMOVE_FROM_GROUP:
	{
	  Group group=(Group)state.getEntity(target[0]);

	  for(int i=1; i!=target.length; i++)
	  {
	    group.remove(target[i], state);
	  }

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
      // Determine whether this change is handled here or by Drawable.Change

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
	  assert(false);
	  return null;
	}

	case GROUP:
	{
	  return new Group.Change(DELETE, null, new EntityID[]
	      {target[0]});
	}

	case DELETE:
	{
	  return new Group.Change(GROUP, null, (EntityID[])undoData);
	}

	case ADD_TO_GROUP:
	{
	  return new Group.Change(REMOVE_FROM_GROUP, null, target);
	}

	case REMOVE_FROM_GROUP:
	{
	  return new Group.Change(ADD_TO_GROUP, null, target);
	}

	default:
	{
	  assert(false); return null;
	}
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
	return "Group:"+super.toString();
      }

      switch(changeType)
      {
	case CREATE:
	{
	  return "Group.Change:CREATE:"+target[0];
	}

	case GROUP:
	{
	  StringBuffer result=new StringBuffer("Group.Change:GROUP");
	  for(int i=0; i!=target.length; i++)
	  {
	    result.append(":"+target[i]);
	  }
	  return new String(result);
	}

	case DELETE:
	{
	  StringBuffer result=new StringBuffer("Group.Change:DELETE");
	  for(int i=0; i!=target.length; i++)
	  {
	    result.append(":"+target[i]);
	  }
	  return new String(result);
	}

	case ADD_TO_GROUP:
	{
	  StringBuffer result=new StringBuffer("Group.Change:ADD_TO_GROUP");
	  for(int i=0; i!=target.length; i++)
	  {
	    result.append(":"+target[i]);
	  }
	  return new String(result);
	}

	case REMOVE_FROM_GROUP:
	{
	  StringBuffer result=new StringBuffer("Group.Change:REMOVE_FROM_GROUP");
	  for(int i=0; i!=target.length; i++)
	  {
	    result.append(":"+target[i]);
	  }
	  return new String(result);
	}
  
	default:
	{
	  return "Group.Change:UNKNOWN";
	}
      }	
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new Group with the specified identifier, 
   * center, Vector of control points, and boolean indicating whether
   * it is locked.</p>
   */

  protected Group(EntityID identifier, Point center, Vector controlPoints,
		  Vector controlPointsAnchored, boolean locked,
		  Shortcut[] shortcut, Vector members, boolean labelled,
		  String text)
  {
    super(identifier, center, controlPoints, controlPointsAnchored, locked,
	  shortcut);

    this.members=members;
    this.labelled=labelled;
    this.text=text;

    assert(isGroupDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isGroupDataValid()
  {
    return isDrawableDataValid();
  }

  /**
   * Obtain a Vector of the Drawable objects comprising the members of the Group.
   */

  public Vector getMembers()
  {
    return members;
  }

  /**
   * <p>Override Drawable.getPropeties to provide access to IS_GROUP_NORMAL
   * data.</p>
   * @return a Vector of Property objects describing the Shape
   */

  public Vector getProperties()
  {
    Vector result=super.getProperties();

    result.add(new Property(Property.IS_GROUP_NORMAL, new Boolean(!labelled)));
    result.add(new Property(Property.TEXT, text));

    return result;
  }

  /**
   * <p>Override Drawable.edit to provide editing of IS_GROUP_NORMAL and
   * TEXT.</p>
   * @param property the {@link Property} to set
   */

  protected void edit(Property property)
  {
    // Preconditions; property not null

    assert(property!=null);

    // Method proper

    switch(property.getType())
    {
      case Property.IS_GROUP_NORMAL:
      {
        labelled=!((Boolean)property.getValue()).booleanValue();
	break;
      }

      case Property.TEXT:
      {
        text=(String)property.getValue();
	break;
      }

      default:
      {
	super.edit(property);
      }
    }
  }

  /**
   * <p>Add an Entity with a specified {@link EntityID} to the Group.</p>
   */

  private void add(EntityID identifier, SessionState state)
  {
    // Preconditions; identifier and state not null

    assert((identifier!=null)&&(state!=null));

    // Method proper

    Drawable drawable=(Drawable)state.getEntity(identifier);
    assert(drawable!=null);
    Vector drawables=state.getDrawables();
    Vector parent=locateParent(drawables, drawable);
    parent.remove(drawable);
    members.add(drawable);

    // Expand bounding rectangle

    buildBoundingRectangle(65536);
  }

  /**
   * <p>Remove an Entity with a specified {@link EntityID} from the Group.</p>
   */

  private void remove(EntityID identifier, SessionState state)
  {
    // Preconditions; identifier and state not null

    assert((identifier!=null)&&(state!=null));

    // Method proper

    Drawable drawable=(Drawable)state.getEntity(identifier);
    assert(drawable!=null);

    Vector groupMembers=locateParent(members, drawable);
    assert(groupMembers!=null);

    Vector drawables=state.getDrawables();
    Vector parent=locateParent(drawables, this);

    groupMembers.remove(drawable);
    parent.add(drawable);

    // Shrink bounding rectangle

    buildBoundingRectangle(65536);
  }

  /**
   * <p>Scan through a Vector and those belonging to its member Groups (if
   * any) to find the Vector that holds a particular Drawable.</p>
   */

  private Vector locateParent(Vector drawables, Drawable drawable)
  {
    // Preconditions; drawables and drawable not null

    assert((drawables!=null)&&(drawable!=null));

    // Method proper

    for(int i=0; i!=drawables.size(); i++)
    {
      Drawable possibleDrawable=(Drawable)drawables.get(i);

      if(possibleDrawable==drawable)
      {
	return drawables;
      }
      else if(possibleDrawable instanceof Group)
      {
	Vector possibleDrawables=locateParent(
	  ((Group)possibleDrawable).members, drawable);

	if(possibleDrawables!=null) return possibleDrawables;
      }
    }

    return null;
  }

  /**
   * <p>Obtain the Bounding Rectangle of the Drawable, at the specified
   * zoom level.</p>
   */

  public java.awt.Rectangle getInnerBoundingRectangle(int zoomLevel)
  {
    return buildBoundingRectangle(zoomLevel);
  }

  /**
   * <p>Determine the bounding rectangle required to surround all the
   * member Drawables, and move control points accordingly.</p>
   * @return the bounding rectangle
   */

  private java.awt.Rectangle buildBoundingRectangle(int zoomLevel)
  {
    java.awt.Rectangle bounds=new java.awt.Rectangle(0, 0, 0, 0);

    for(int i=0; i!=members.size(); i++)
    {
      Drawable drawable=(Drawable)members.get(i);

      java.awt.Rectangle newBounds=drawable.getBoundingRectangle(zoomLevel);

      if((bounds.width==0)&&(bounds.height==0))
      {
	bounds=newBounds;
      }
      else
      {
	if(bounds.x>newBounds.x)
	{
	  bounds.width+=bounds.x-newBounds.x;
	  bounds.x=newBounds.x;
	}

	if(bounds.y>newBounds.y)
	{
	  bounds.height+=bounds.y-newBounds.y;
	  bounds.y=newBounds.y;
	}
      
	if(bounds.x+bounds.width<newBounds.x+newBounds.width)
	  bounds.width=newBounds.x+newBounds.width-bounds.x;
      
	if(bounds.y+bounds.height<newBounds.y+newBounds.height)
	  bounds.height=newBounds.y+newBounds.height-bounds.y;
      }
    }

    controlPoints.set(0, new Point(bounds.x, bounds.y));
    controlPoints.set(1, new Point(bounds.x+bounds.width, bounds.y));
    controlPoints.set(2, new Point(bounds.x+bounds.width,
				   bounds.y+bounds.height));
    controlPoints.set(3, new Point(bounds.x, bounds.y+bounds.height));
    
    center=new Point(bounds.x+bounds.width/2,
		     bounds.y+bounds.height/2);

    return bounds;
  }

  /**
   * <p>In order to render an Entity, supply it with the graphics context to
   * render on, and a Rectangle that details which part of the org.davidmorgan.jinn.is
   * visible. </p>
   *
   * <p>Subclasses will override render to provide their own rendering
   * routines.</p>
   *
   * @return a boolean indicating whether any drawing was done
   */

  public boolean render(SessionState state, Graphics2D g,
			java.awt.Rectangle view)
  {
    // Preconditions; state, g and view not null

    assert((state!=null)&&(g!=null)&&(view!=null));

    // Method proper

    if(view.intersects(getBoundingRectangle()))
    {
      // Render group

      java.awt.Rectangle bounds=g.getClipBounds();
      int zoomLevel=view.width/bounds.width;
      g.translate(-view.x/zoomLevel+bounds.x, -view.y/zoomLevel+bounds.y);
      
      BasicStroke dashed=new BasicStroke(1, BasicStroke.CAP_BUTT, 
					 BasicStroke.JOIN_BEVEL, 1,
					 new float[]{1,5},
					 0);

      Color mainColor=Color.BLACK;

      int[] x=new int[4];
      int[] y=new int[4];

      for(int i=0; i!=4; i++)
      {
	Point point=(Point)controlPoints.get(i);

	x[i]=point.x/zoomLevel;
	y[i]=point.y/zoomLevel;
      }

      Polygon polygon=new Polygon(x, y, 4);

      // If the Group is labelled, fill with white

      if(labelled)
      {
	g.setColor(Color.WHITE);
	g.fill(polygon);
      }

      g.setColor(mainColor);
      g.setStroke(dashed);
      g.draw(polygon);

      // Render members if necessary

      if(!labelled)
      {
	g.setTransform(new AffineTransform());

	for(int i=0; i!=members.size(); i++)
	{
	  Drawable drawable=(Drawable)members.get(i);
	  
	  drawable.render(state, g, view);
	}
      }
      else // Otherwise, display the text label
      {
	TextLayout layout=new TextLayout(
	  text, new Font("Times", Font.PLAIN, 12), g.getFontRenderContext());

	Rectangle2D layoutBounds=layout.getBounds();
	Point topLeft=(Point)controlPoints.get(0);

	double groupWidth=(center.x-topLeft.x)*2;
	double groupHeight=(center.y-topLeft.y)*2;

	groupHeight/=zoomLevel;
	groupWidth/=zoomLevel;

	double width=((double)layoutBounds.getWidth());
	double height=((double)layoutBounds.getHeight());

	double factorOne=width/groupWidth*1.1;
	double factorTwo=height/groupHeight*1.1;

	double scaleFactor=factorOne>factorTwo?factorOne:factorTwo;
	
	g.translate(((double)center.x)/zoomLevel, ((double)center.y)/zoomLevel);
	g.scale(1/scaleFactor, 1/scaleFactor);
	g.translate((float)(-width/2), (float)(height/2)-layout.getDescent());

	g.setPaint(Color.BLACK);
	layout.draw(g, 0, 0);
      }

      g.setTransform(new AffineTransform());

      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * <p>Create a deep copy of the Group.</p>
   */

  public Object clone()
  {
    Group result=(Group)super.clone();

    result.members=new Vector();
    for(int i=0; i!=members.size(); i++)
    {
      result.members.add(((Entity)members.get(i)).clone());
    }

    return result;
  }

  /**
   * <p>Move the entire Group.</p>
   * @param translation a {@link Point} representing the translation
   */

  protected void move(Point translation)
  {
    for(int i=0; i!=members.size(); i++)
    {
      ((Drawable)members.get(i)).move(translation);
    }

    buildBoundingRectangle(65536);
  }

  /**
   * <p>Resize the entire Group about a specified center by a specified
   * amount.</p>
   * @param center the center of the resize operation
   * @param factor the enlargment factor
   */

  protected void absoluteResize(Point center, double factorX, double factorY)
  {
    for(int i=0; i!=members.size(); i++)
    {
      ((Drawable)members.get(i)).absoluteResize(center, factorX, factorY);
    }

    buildBoundingRectangle(65536);
  }

  /**
   * <p>Rotate the entire Group about a specified center by a specified
   * amount.</p>
   */

  protected void absoluteRotate(Point center, double angle)
  {
    for(int i=0; i!=members.size(); i++)
    {
      ((Drawable)members.get(i)).absoluteRotate(center, angle);
    }

    buildBoundingRectangle(65536);
  }

  /**
   * <p>Flip the entire Group about a specified center.</p>
   */

  protected void absoluteFlip(Point center, boolean vertical)
  {
    for(int i=0; i!=members.size(); i++)
    {
      ((Drawable)members.get(i)).absoluteFlip(center, vertical);
    }

    buildBoundingRectangle(65536);
  }

  /**
   * <p>Alter the Group to make it suitable after a session load operation.
   * All Group members have their initializeAfterLoad() method called.</p>
   */
  
  public void initializeAfterLoad()
  {
    for(int i=0; i!=members.size(); i++)
    {
      Entity entity=(Entity)members.get(i);

      entity.initializeAfterLoad();

    }
  }

  /**
   * <p>Obtain a String representation of the Group.</p>
   */

  public String toString()
  {
    return "Group:("+super.toString()+"):"
      +members+":"
      +labelled+":"
      +text;
  }
}
