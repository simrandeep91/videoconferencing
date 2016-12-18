package Whiteboarding.model;

import java.awt.Graphics2D;
import java.awt.Point;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>Class representing the Drawable Entity.</p>
 * @author David Morgan
 */

public class Drawable extends Entity implements Serializable, Cloneable
{
  // 1. Static variable field declarations

  static final long serialVersionUID = 8352092860333642818L;

  /**
   * <p>A boolean specifying whether normal operations are allowed to
   * change the aspect ratio of the Drawable.</p>
   */

  protected static boolean ALLOW_ASPECT_CHANGE=true;

  /**
   * <p>A boolean specifying whether resize by bounding rectangle uses
   * the actual bounding rectangle or the most distant control point.</p>
   */

  protected static boolean RESIZE_USES_BOUNDING_RECTANGLE=false;

  // 2. Instance variable field declarations

  /**
   * <p>The center point of the Drawable.</p>
   */

  protected Point center;

  /**
   * <p>The control points of the Drawable.</p>
   */

  protected Vector controlPoints;

  /**
   * <p>Whether the Drawable is locked.</p>
   */

  protected boolean locked;

  /**
   * <p>The angle the Drawable has been rotated to.</p>
   */

  protected double angle;

  /**
   * <p>The {@link Shortcut}s associated with this Drawable.</p>
   */

  protected Shortcut[] shortcut;

  /**
   * <p>A Vector containing Points anchored to this Drawable.</p>
   */

  protected Vector controlPointsAnchored;

  // 4. Static member inner class declarations

  /**
   * <p>Subclass of {@link Entity.Change} providing extra operations on
   * Drawables, and implementing existing operations differently where
   * necessary.</p>
   */ 

  public static class Change extends Entity.Change
  {
    static final long serialVersionUID = 1326682521941753377L;

    // 4.1 Static variable field declarations

    /**
     * <p>Indicates an operation that moves a Drawable.</p>
     *
     * <p>First and only parameter is a Point specifying the translation.</p>
     *
     * <p>Only one target Drawable is allowed.</p>
     */

     public static final int MOVE=2;

    /**
     * <p>Indicates an operation that resizes a Drawable.</p>
     *
     * <p>First parameter is an Integer specifying the index of the Control Point
     * used to resize the Entity. Second parameter is a Point specifying the
     * translation. Third parameter is a Boolean; true indicates resize is by
     * bounding rectangle, false indicates resize is around center.</p>
     * 
     * <p>Only one target Drawable is allowed.</p>
     */

     public static final int RESIZE=3;
    
    /**
     * <p>Indicates an operation that edits a property of a Drawable.</p>
     *
     * <p>First and only parameter is a Property object specifying the change.
     * </p>
     * 
     * <p>Only one target Drawable is allowed.</p>
     */

     public static final int EDIT=4;

    /**
     * <p>Indicates an operation that rotates a Drawable.</p>
     *
     * <p>First parameter is an Integer specifying the index of the Control Point
     * used to rotate the Entity. Second parameter is a Point specifying the
     * translation applied to it; the amount of rotation is determined from this.
     * </p>
     * 
     * <p>Only one target Drawable is allowed.</p>
     */

     public static final int ROTATE=5;

    /**
     * <p>Indicates an operation that rotates a Drawable by a specified
     * angle.</p>
     *
     * <p>First parameter and only parameter is a Double giving the amount to
     * rotate by.</p>
     * 
     * <p>Only one target Drawable is allowed.</p>
     */

     public static final int ROTATE_ABSOLUTE=-5;

    /**
     * <p>Indicates an operation that flips a Drawable.</p>
     *
     * <p>First and only parameter is an Boolean; true indicates flip is
     * vertical, false indicates flip is horizontal.</p>
     * 
     * <p>Only one target Drawable is allowed.</p>
     */

     public static final int FLIP=6;

     /**
      * <p>Indicates an operation that moves a Drawable to the top Layer.</p>
      *
      * <p>There are no parameters.</p>
      *
      * <p>Only one target Drawable is allowed.</p>
      */

    public static final int BRING_TO_FRONT=7;

     /**
      * <p>Indicates an operation that moves a Drawable to the bottom Layer.</p>
      *
      * <p>There are no parameters.</p>
      *
      * <p>Only one target Drawable is allowed.</p>
      */

    public static final int SEND_TO_BACK=8;

    /**
     * <p>Indicates an operation that moves a Drawable to a specified layer.</p>
     *
     * <p>First and only parameter is an Integer layer number.</p>
     *
     * <p>Only one target Drawable is allowed.</p>
     */

    public static final int MOVE_TO_LAYER=-2;

    // 4.7 Instance constructor declarations

    /**
     * <p>Protected empty constructor.</p>
     */
    
    protected Change() {}

    /**
     * <p>Change modifies a Drawable in a specified way.</p>
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

	case MOVE:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof Point)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case RESIZE:
	{
	  return (parameter!=null)&&(parameter.length==3)&&
	    (parameter[0] instanceof Integer)&&
	    (parameter[1] instanceof Point)&&
	    (parameter[2] instanceof Boolean)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case EDIT:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof Property)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case ROTATE:
	{
	  return (parameter!=null)&&(parameter.length==2)&&
	    (parameter[0] instanceof Integer)&&(parameter[1] instanceof Point)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case ROTATE_ABSOLUTE:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof Double)&&(target!=null)
	    &&(target.length==1)&&(target[0]!=null);
	}

	case FLIP:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof Boolean)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case BRING_TO_FRONT:
	{
	  return (parameter==null)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case SEND_TO_BACK:
	{
	  return (parameter==null)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case MOVE_TO_LAYER:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof Integer)&&
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
	case MOVE:
	case RESIZE:
	case FLIP:
	{
	  return (undoData==null);
	}

	case ROTATE:
	case ROTATE_ABSOLUTE:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    (undoData[0] instanceof Double);
	}

	case EDIT:
	{
	  return (undoData!=null)&&(undoData.length==1)
	    &&(undoData[0] instanceof Property);
	}

	case BRING_TO_FRONT:
	case SEND_TO_BACK:
	case MOVE_TO_LAYER:
	{
	  return (undoData!=null)&&(undoData.length==1)
	    &&(undoData[0] instanceof Integer);
	}
     
	default:
	{
	  return super.isUndoDataValid();
	}
      }
    }

    /**
     * <p>Apply the change. This must be overriden by subclasses of
     * Drawable.Change that provide new functionality.</p>
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

	  Drawable drawable=new Drawable(target[0], new Point(0, 0),
					 new Vector(), new Vector(),
					 false, null);

	  state.addEntity(drawable);
	  break;
	}

	case MOVE:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

          // Gather data for undo

          // Make the change
	  
	  drawable.move((Point)parameter[0]);
	  break;
	}

	case RESIZE:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

          // Gather data for undo

          // Make the change

	  drawable.resize(((Integer)parameter[0]).intValue(),
			  (Point)parameter[1],
			  ((Boolean)parameter[2]).booleanValue());
	  break;
	}

	case EDIT:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);
	  Property newProperty=(Property)parameter[0];

          // Gather data for undo

	  Vector properties=drawable.getProperties();
	  for(int i=0; i!=properties.size(); i++)
	  {
	    Property oldProperty=(Property)properties.get(i);
	    if(oldProperty.getType()==newProperty.getType())
	    {
	      undoData=new Object[] { oldProperty };
	    }
	  }

          // Make the change

	  drawable.edit(newProperty);
	  break;
	}

	case ROTATE:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

          // Make the change and gather data for undo

	  undoData=new Object[] {new Double(
	    -drawable.rotate(((Integer)parameter[0]).intValue(),
			    (Point)parameter[1]))};

	  break;
	}

	case ROTATE_ABSOLUTE:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] {new Double(
	    -((Double)parameter[0]).doubleValue())};

          // Make the change

	  drawable.absoluteRotate(drawable.center,
				  ((Double)parameter[0]).doubleValue());

	  break;
	}

	case FLIP:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

          // Gather data for undo

          // Make the change

	  drawable.flip(((Boolean)parameter[0]).booleanValue());
	  break;
	}

	case BRING_TO_FRONT:
	case SEND_TO_BACK:
	case MOVE_TO_LAYER:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);
	  assert(drawable!=null);

	  Vector drawables=state.getDrawables();
	  int index=0;

          // Gather data for undo

	  for(index=0; index!=drawables.size(); index++)
	  {
	    if(drawables.get(index)==drawable)
	    {
	      undoData=new Object[] { new Integer(index) };
	      break;
	    }
	  }

          // Make the change

	  drawables.remove(index);

	  if(changeType==BRING_TO_FRONT) drawables.add(drawable);
	  else if(changeType==SEND_TO_BACK) drawables.add(0, drawable);
	  else drawables.add(((Integer)parameter[0]).intValue(), drawable);
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
     * <p>Undo the change. This must be overriden by subclasses of
     * Drawable.Change that provide new functionality.</p>
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

	case MOVE:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

	  drawable.move(negatePoint((Point)parameter[0]));

	  break;
	}

	case RESIZE:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

	  drawable.resize(((Integer)parameter[0]).intValue(),
			  negatePoint((Point)parameter[1]),
			  ((Boolean)parameter[2]).booleanValue());
	  break;
	}

	case EDIT:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

	  drawable.edit((Property)undoData[0]);

	  break;
	}

	case ROTATE:
	case ROTATE_ABSOLUTE:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

	  drawable.absoluteRotate(drawable.center,
				  ((Double)undoData[0]).doubleValue());

	  break;
	}

	case FLIP:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);

	  drawable.flip(((Boolean)parameter[0]).booleanValue());

	  break;
	}

	case BRING_TO_FRONT:
	case SEND_TO_BACK:
	case MOVE_TO_LAYER:
	{
	  Drawable drawable=(Drawable)state.getEntity(target[0]);
	  assert(drawable!=null);

	  Vector drawables=state.getDrawables();
	  int index=0;

          // Obtain index of drawable

	  for(index=0; index!=drawables.size(); index++)
	  {
	    if(drawables.get(index)==drawable) break;
	  }

          // Make the change

	  drawables.remove(index);
	  drawables.add(((Integer)undoData[0]).intValue(), drawable);

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

	case MOVE:
	{
	  return new Drawable.Change(MOVE,
				     new Object[]
	      {negatePoint((Point)parameter[0])},
				     target);
	}

	case RESIZE:
	{
	  return new Drawable.Change(RESIZE,
				     new Object[]
	      {parameter[0],
	       negatePoint((Point)parameter[1]),
	       parameter[2]},
				     target);
	}

	case EDIT:
	{
	  return new Drawable.Change(EDIT,
				     undoData,
				     target);
	}

	case ROTATE:
	case ROTATE_ABSOLUTE:
	{
	  return new Drawable.Change(ROTATE_ABSOLUTE,
				     new Object[]
	      {undoData[0]},
				     target);
	}

	case FLIP:
	{
	  return new Drawable.Change(FLIP, parameter, target);
	}

	case BRING_TO_FRONT:
	case SEND_TO_BACK:
	case MOVE_TO_LAYER:
	{
	  return new Drawable.Change(MOVE_TO_LAYER, undoData, target);
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
      // Determine whether this change is handled here or by Entity.Change

      if(isParent(changeType))
      {
	return "Drawable.Change:"+super.toString();
      }

      switch(changeType)
      {
	case CREATE:
	{
	  return "Drawable.Change:CREATE:"+target[0];
	}

	case MOVE:
	{
	  return "Drawable.Change:MOVE:"+target[0]+":"+parameter[0];
	}

	case RESIZE:
	{
	  return "Drawable.Change:RESIZE:"+target[0]+":"+parameter[0]+
	    ":"+parameter[1]+":"+parameter[2];
	}

	case EDIT:
	{
	  return "Drawable.Change:EDIT:"+target[0]+":"+parameter[0];
	}

	case ROTATE:
	{
	  return "Drawable.Change:ROTATE:"+target[0]+":"+parameter[0]+":"+
	    parameter[1];
	}

	case ROTATE_ABSOLUTE:
	{
	  return "Drawable.Change:ROTATE_ABSOLUTE:"+
	    target[0]+":"+parameter[0];
	}

	case FLIP:
	{
	  return "Drawable.Change:FLIP:"+target[0]+":"+
	    (((Boolean)parameter[0]).booleanValue()?"vertical":"horizontal");
	}

	case BRING_TO_FRONT:
	{
	  return "Drawable.Change:BRING_TO_FRONT:"+target[0];
	}

	case SEND_TO_BACK:
	{
	  return "Drawable.Change:SEND_TO_BACK:"+target[0];
	}

	case MOVE_TO_LAYER:
	{
	  return "Drawable.Change:MOVE_TO_LAYER:"+target[0]+":"+parameter[0];
	}
 
	default:
	{
	  return "Drawable.Change:UNKNOWN";
	}
      }
    }
  }

  // 5. Static method declarations
  
  /**
   * <p>Create a point in absolute coordinates from screen
   * coordinates.</p>
   */
  
  public static Point absolutePoint(Point offset, int zoomLevel, int x, int y)
  {
    return new Point((x+offset.x)*zoomLevel, (y+offset.y)*zoomLevel);
  }

  /**
   * <p>Create a point in absolute coordinates from screen
     * coordinates.</p>
     */
  
  public static Point absolutePoint(Point offset, int zoomLevel, Point point)
  {
    return new Point((point.x+offset.x)*zoomLevel,
		     (point.y+offset.y)*zoomLevel);
  }
 
  /**
   * <p>Create a rectangle bounding two points.</p>
   */
  
  public static java.awt.Rectangle createBoundingRectangle(Point first,
							   Point second)
  {
    return createBoundingRectangle(first.x, first.y,
				   second.x, second.y);
  }

  /**
   * <p>Create a rectangle bounding two points.</p>
   */
  
  public static java.awt.Rectangle createBoundingRectangle(int x1, int y1,
							   int x2, int y2)
  {
    int left=x1<x2?x1:x2;
    int top=y1<y2?y1:y2;
    
    int right=x1>x2?x1:x2;
    int bottom=y1>y2?y1:y2;
    
    return new java.awt.Rectangle(left, top, right-left, bottom-top);
  }
 
  
  // 7. Instance constructor declarations

  /**
   * <p>Create a new Drawable with the specified identifier, 
   * center, Vector of control points, Vector of anchored control points,
   * boolean indicating whether it is locked and array of {@link Shortcut}
   * objects.</p>
   */

  protected Drawable(EntityID identifier, Point center,
		     Vector controlPoints, Vector controlPointsAnchored,
		     boolean locked, Shortcut[] shortcut)
  {
    super(identifier);

    this.center=center;
    this.controlPoints=controlPoints;
    this.locked=locked;
    this.shortcut=shortcut;
    this.controlPointsAnchored=controlPointsAnchored;

    assert(isDrawableDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isDrawableDataValid()
  {
    if(!isEntityDataValid()) return false;

    if((center==null)||(controlPoints==null)||
      (controlPointsAnchored==null)) return false;

    for(int i=0; i!=controlPoints.size(); i++)
    {
      if(!(controlPoints.get(i) instanceof Point)) return false;
    }
    
    return true;
  }

  /**
   * <p>Obtain the Bounding Rectangle of the Drawable, at the default
   * zoom level.</p>
   */

  public java.awt.Rectangle getBoundingRectangle()
  {
    return getBoundingRectangle(65536);
  }

  /**
   * <p>Obtain the Bounding Rectangle of the Drawable, at the specified
   * zoom level.</p>
   */

  public java.awt.Rectangle getBoundingRectangle(int zoomLevel)
  {
    java.awt.Rectangle inner=getInnerBoundingRectangle(zoomLevel);

    int width=getExtraBoundingWidth(zoomLevel);

    return new java.awt.Rectangle(inner.x-width/2, inner.y-width/2,
				  inner.width+width, inner.height+width);
  }

  /**
   * <p>Obtain the number of pixels extra bounding rectangle to allow,
   * e.g. the pen width.</p>
   */

  protected int getExtraBoundingWidth(int zoomLevel)
  {
    return 1;
  }

  /**
   * <p>Obtain the bounding Rectangle of the Drawable without taking
   * line widths into account.</p>
   */

  protected java.awt.Rectangle getInnerBoundingRectangle(int zoomLevel)
  {
    int left=center.x;
    int right=center.x;
    int top=center.y;
    int bottom=center.y;

    for(int i=0; i!=controlPoints.size(); i++)
    {
      Point point=(Point)controlPoints.get(i);

      if(point.x<left) left=point.x;
      if(point.x>right) right=point.x;

      if(point.y<top) top=point.y;
      if(point.y>bottom) bottom=point.y;
    }

    return new java.awt.Rectangle(left, top, right-left, bottom-top);
  }

  /**
   * <p>Obtain the position of the Center of the Drawable.</p>
   */

  public Point getCenter()
  {
    return center;
  }

  /**
   * <p>Obtain a Vector of the Control Points belonging to the Drawable, as
   * instances of Point.</p>
   */

  public Vector getControlPoints()
  {
    return controlPoints;
  }

  /**
   * <p>Obtain a Vector of the {@link Property} objects describing the
   * Drawable. Subclasses must override this to add access to new
   * properties.</p>
   * <p>Drawable provides access to IS_LOCKED and SHORTCUTS Properties.</p>
   * @return a Vector of Property objects describing the Drawable
   */

  public Vector getProperties()
  {
    Vector result=new Vector();

    result.add(new Property(Property.IS_LOCKED, new Boolean(locked)));
    result.add(new Property(Property.SHORTCUTS, shortcut));

    return result;
  }

  /**
   * <p>Obtain a specific {@link Property} object, identified by its type,
   * or null if this Drawable has no such property.</p>
   * <p>This method need not be overridden.</p>
   * @param type the integer representing the type of {@link Property} to return
   * @return the relevant {@link Property}, or null if it doesn't exist
   */

  public final Property getProperty(int type)
  {
    Vector vector=getProperties();

    for (int i=0; i<vector.size(); i++)
    {
      Property p=(Property)vector.get(i);
      if (p.getType()==type) return p;
    }

    return null;
  }

  /**
   * <p>Set a property of the Drawable. Subclasses must override this to
   * all setting of whatever properties they have.</p>
   * @param property the {@link Property} to set
   */

  protected void edit(Property property)
  {
    // Preconditions; property not null

    assert(property!=null);

    // Method proper

    switch(property.getType())
    {
      case Property.IS_LOCKED:
      {
	locked=((Boolean)property.getValue()).booleanValue();
	break;
      }

      case Property.SHORTCUTS:
      {
	shortcut=(Shortcut[])property.getValue();
	break;
      }
      
      default:
      {
	assert(false);
      }
    }

    // Postconditions; data is valid

    assert(isDrawableDataValid());
  }

  /**
   * <p>Obtain a boolean indicating whether the Drawable is locked.</p>
   */

  public boolean isLocked()
  {
    return locked;
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

    java.awt.Rectangle bounds=g.getClipBounds();

    int zoomLevel=view.width/bounds.width;
    Point origin=new Point(view.x, view.y);

    if(view.contains(center))
    {
      Point screenCenter=new Point((center.x-origin.x)/zoomLevel,
				   (center.y-origin.y)/zoomLevel);

      g.drawString(toString(), screenCenter.x, screenCenter.y);
      
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * <p>Move the Drawable.</p>
   * @param translation a {@link Point} representing the translation
   */

  protected void move(Point translation)
  {
    center.translate(translation.x, translation.y);

    for(int i=0; i!=controlPoints.size(); i++)
    {
      Point point=(Point)controlPoints.get(i);

      if(!(point instanceof AnchorPoint))
      {
	point.translate(translation.x, translation.y);
      }
    }

    for(int i=0; i!=controlPointsAnchored.size(); i++)
    {
      ((Point)controlPointsAnchored.get(i)).translate(translation.x,
						      translation.y);
    }
  }
  /**
   * <p>Anchor a control point (specified by an integer index) to the
   * {@link Drawable} in the specified {@link SessionState} with the
   * specified {@link EntityID}.</p>
   */

  protected void anchorPoint(SessionState state, EntityID identifier, int index)
  {
    // Preconditions; state and identifier not null

    assert((state!=null)&&(identifier!=null));

    // Method proper

    Drawable drawable=(Drawable)state.getEntity(identifier);

    Point point=(Point)controlPoints.get(index);
    AnchorPoint anchorPoint=new AnchorPoint(point, identifier);

    drawable.controlPointsAnchored.add(anchorPoint);
    controlPoints.set(index, anchorPoint);
  }

  /**
   * <p>Detach a control point (specified by an integer index) from the
   * {@link Drawable} in the specified {@link SessionState} with the
   * specified {@link EntityID}.</p>
   */

  protected void detachPoint(SessionState state, int index)
  {
    // Preconditions; state not null

    assert((state!=null));

    // Method proper

    Point possibleAnchorPoint=(Point)controlPoints.get(index);
    if(possibleAnchorPoint instanceof AnchorPoint)
    {
      AnchorPoint anchorPoint=(AnchorPoint) possibleAnchorPoint;
      EntityID identifier=anchorPoint.target;
      
      Drawable drawable=(Drawable)state.getEntity(identifier);

      for(int i=0; i!=drawable.controlPointsAnchored.size(); i++)
      {
	if(drawable.controlPointsAnchored.get(i)==anchorPoint)
	{
	  drawable.controlPointsAnchored.remove(i);
	  break;
	}
      }
     
      Point point=new Point(anchorPoint);
     
      controlPoints.set(index, point);
    }
  }

  /**
   * <p>Resize the Drawable.</p>
   * @param controlPointIndex the index of the control point used to resize
   * @param translation the translation that control point was moved by
   * @param byBoundingRectangle resize by bounding rectangle
   */
  
  protected void resize(int controlPointIndex, Point translation,
			boolean byBoundingRectangle)
  {
    Point controlPoint=(Point)controlPoints.get(controlPointIndex);
    assert(controlPoint!=null);

    Point movedControlPoint=new Point(controlPoint);
    movedControlPoint.translate(translation.x, translation.y);

    Point resizeCenter=new Point(center);
    
    if(byBoundingRectangle)
    {
      if(RESIZE_USES_BOUNDING_RECTANGLE)
      {
	java.awt.Rectangle boundingRectangle=getBoundingRectangle();
	
	Point[] corners=new Point[4];
	corners[0]=new Point(boundingRectangle.x, boundingRectangle.y);
	
	corners[1]=new Point(boundingRectangle.x+boundingRectangle.width,
			     boundingRectangle.y);
	
	corners[2]=new Point(boundingRectangle.x,
			     boundingRectangle.y+boundingRectangle.height);
	
	corners[3]=new Point(corners[1].x, corners[2].y);
	
	double maxDistance=0;
	
	for(int i=0; i!=4; i++)
	{
	  double testDistance=corners[i].distance(controlPoint);
	  
	  if(testDistance>maxDistance)
	  {
	    maxDistance=testDistance;
	    resizeCenter=corners[i];
	  }
	}
      }
      else
      {
	double maxDistance=0;
	
	for(int i=0; i!=controlPoints.size(); i++)
	{
	  Point point=(Point)controlPoints.get(i);
	  
	  double testDistance=controlPoint.distance(point);
	  
	  if(testDistance>maxDistance)
	  {
	    maxDistance=testDistance;
	    resizeCenter=new Point(point);
	  }
	}     
      }
    }

    double angleBuffer=angle;
    absoluteRotate(resizeCenter, -angleBuffer, true);
    absoluteRotatePoint(movedControlPoint, resizeCenter, -angleBuffer);

    double factorX=1;
    double factorY=1;
   
    if(ALLOW_ASPECT_CHANGE)
    {
      double dX1=movedControlPoint.x-resizeCenter.x;
      double dX2=controlPoint.x-resizeCenter.x;
      
      if(dX2!=0)
      {
	factorX=dX1/dX2;
      }
      
      double dY1=movedControlPoint.y-resizeCenter.y;
      double dY2=controlPoint.y-resizeCenter.y;

      if(dY2!=0)
      {
	factorY=dY1/dY2;
      }
    }
    else
    {
      double originalDistance=resizeCenter.distance(controlPoint);
      double newDistance=resizeCenter.distance(movedControlPoint);

      if(originalDistance!=0) factorX=newDistance/originalDistance;
      factorY=factorX;
    }

    absoluteResize(resizeCenter, factorX, factorY);
    absoluteRotate(resizeCenter, angleBuffer, true);
  }

  /**
   * <p>Resize the Drawable about a specified center by a specified amount.</p>
   * @param center the center of the resize operation
   * @param factor the enlargment factor
   */

  protected void absoluteResize(Point center, double factorX, double factorY)
  {
    absoluteResizePoint(this.center, center, factorX, factorY);

    for(int i=0; i!=controlPoints.size(); i++)
    {
      Point point=(Point)controlPoints.get(i);

      if(!(point instanceof AnchorPoint))
      {
	absoluteResizePoint(point, center, factorX, factorY);
      }
    }

    for(int i=0; i!=controlPointsAnchored.size(); i++)
    {
      absoluteResizePoint((Point)controlPointsAnchored.get(i), center,
			  factorX, factorY);
    }
  }

  /**
   * <p>Resize a Point about a specified center by a specified amount.</p>
   * @param center the center of the resize operation
   * @param factor the enlargment factor
   */

  protected void absoluteResizePoint(Point target, Point center,
				     double factorX, double factorY)
  {
    target.setLocation(
      (int)((target.x-center.x)*factorX)+center.x,
      (int)((target.y-center.y)*factorY)+center.y);
  }

  /**
   * <p>Rotate the drawable.</p>
   * @param controlPointIndex the index of the control point used to rotate
   * @param translation the translation that control point was moved by
   */

  protected double rotate(int controlPointIndex, Point translation)
  {
    Point controlPoint=(Point)controlPoints.get(controlPointIndex);
    assert(controlPoint!=null);

    Point movedControlPoint=new Point(controlPoint);
    movedControlPoint.translate(translation.x, translation.y);

    controlPoint=new Point(controlPoint);
    controlPoint.translate(-center.x, -center.y);
    movedControlPoint.translate(-center.x, -center.y);

    double angle=Math.atan2(movedControlPoint.y,movedControlPoint.x)-
      Math.atan2(controlPoint.y, controlPoint.x);

    absoluteRotate(center, angle);

    return angle;
  }

  /**
   * <p>Rotate the Drawable about a specified center by a specified angle,
   * with a boolean specifying whether anchored points are moved.</p>
   */

  protected void absoluteRotate(Point center, double angle)
  {
    absoluteRotate(center, angle, false);
  }

  /**
   * <p>Rotate the Drawable about a specified center by a specified angle,
   * with a boolean specifying whether anchored points are moved.</p>
   */

  protected void absoluteRotate(Point center, double angle,
				boolean moveAnchoredPoints)
  {
    absoluteRotatePoint(this.center, center, angle);

    for(int i=0; i!=controlPoints.size(); i++)
    {
      Point point=(Point)controlPoints.get(i);

      if(!(point instanceof AnchorPoint)||moveAnchoredPoints)
      {
	absoluteRotatePoint(point, center, angle);
      }
    }

    for(int i=0; i!=controlPointsAnchored.size(); i++)
    {
      absoluteRotatePoint((Point)controlPointsAnchored.get(i), center, angle);
    }

    this.angle+=angle;
  }

  /**
   * <p>Rotate a Point about a specified center by a specified angle.</p>
   */

  protected void absoluteRotatePoint(Point target, Point center, double angle)
  {
    int x=target.x-center.x;
    int y=target.y-center.y;
    
    int xx=(int)(x*Math.cos(angle)-y*Math.sin(angle));
    int yy=(int)(x*Math.sin(angle)+y*Math.cos(angle));
    
    target.setLocation(xx+center.x, yy+center.y);
  }
  
  /**
   * <p>Flip the drawable.</p>
   * @param vertical indicates whether the flip is vertical, as opposed
   * to horizontal
   */

  protected void flip(boolean vertical)
  {
    if(vertical)
    {
      angle=Math.PI-angle;

    }
    else
    {
      angle=-angle;
    }

    absoluteFlip(center, vertical);
  }

  /**
   * <p>Flip the drawable about a specified center.</p>
   */

  protected void absoluteFlip(Point center, boolean vertical)
  {
    absoluteFlipPoint(this.center, center, vertical);

    for(int i=0; i!=controlPoints.size(); i++)
    {
      Point point=(Point)controlPoints.get(i);

      if(!(point instanceof AnchorPoint))
      {
	absoluteFlipPoint(point, center, vertical);
      }
    }

    for(int i=0; i!=controlPointsAnchored.size(); i++)
    {
      absoluteFlipPoint((Point)controlPointsAnchored.get(i), center, vertical);
    }
  }

  /**
   * <p>Flip a Point about a specified center.</p>
   */

  protected void absoluteFlipPoint(Point target, Point center, boolean vertical)
  {
    if(vertical)
    {
      target.setLocation(target.x, -target.y+2*center.y);
    }
    else
    {
      target.setLocation(-target.x+2*center.x, target.y);
    }
  }

  /**
   * <p>Return a new {@link Point} with the same coordinates as the supplied
   * point, but negated.</p>
   */

  protected static Point negatePoint(Point point)
  {
    return new Point(-point.x, -point.y);
  }

  /**
   * <p>Create a deep copy of the Drawable.</p>
   */

  public Object clone()
  {
    Drawable result=(Drawable)super.clone();
    
    result.center=new Point(center);

    result.controlPoints=new Vector();
    for(int i=0; i!=controlPoints.size(); i++)
    {
      result.controlPoints.add(new Point((Point)controlPoints.get(i)));
    }

    // result.locked=locked

    return result;
  }

  /**
   * <p>Obtain a String representation of the Drawable.</p>
   */

  public String toString()
  {
    return "Drawable:("+super.toString()+"):"
      +center+":"
      +controlPoints+":"
      +controlPointsAnchored+":"
      +locked+":"
      +shortcut;
  }
}
