package Whiteboarding.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Point;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>Class representing the {@link Shape} {@link Entity}. It extends
 * {@link Drawable} to provide a generic shape having a fill color and style
 * ({@link Brush}) and pen color and style ({@link Pen}).</p>
 * <p>These are both accessible via the getProperties() method.</p>
 * @author David Morgan
 */

public class Shape extends Drawable implements Serializable, Cloneable
{
  static final long serialVersionUID = 6629639705070182L;

  // 2. Instance variable field declarations

  /**
   * <p>The {@link Brush} used to fill the Shape.</p>
   */

  protected Brush brush;

  /**
   * <p>The {@link Pen} used to draw the Shape.</p>
   */

  protected Pen pen;

  // 4. Static inner method declarations
  
  /**
   * <p>Subclass of Drawable.Change providing extra operations on Shapes,
   * and implementing existing operations differently where necessary.</p>
   */ 

  public static class Change extends Drawable.Change
  {
    static final long serialVersionUID = 5009134955929445328L;

    // 4.7 Instance constructor declarations

    /**
     * <p>Protected empty constructor.</p>
     */

    protected Change() {}

    /**
     * <p>Change modifies a Shape in a specified way.</p>
     */

    public Change(int changeType, Object[] parameter, EntityID[] target)
    {
      initialize(changeType, parameter, target);
    }

    // 4.9 Instance method declarations

    protected void initialize(int changeType, Object[] parameter,
			      EntityID[] target)
    {
      // Determine whether this change is handled here or by Drawable.Change

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
     * <p>Determine whether a particular changeType should be handed by
     * the parent class.</p>
     */

    private boolean isParent(int changeType)
    {
      switch(changeType)
      {
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
	default:
	{
	  return super.isUndoDataValid();
	}
      }
    }

    /**
     * <p>Apply the change. This must be overriden by subclasses of
     * Shape.Change that provide new functionality.</p>
     */

    public void apply(SessionState state)
    {
      // Determine whether this change is handled here or by Drawable.Change

      if(isParent(changeType))
      {
	super.apply(state);
	return;
      }

      // No changes are handled here
    
      assert(false);
    }

    /**
     * <p>Undo the change. This must be overriden by subclasses of
     * Shape.Change that provide new functionality.</p>
     */

    public void undo(SessionState state)
    {
      // Determine whether this change is handled here or by Drawable.Change

      if(isParent(changeType))
      {
	super.undo(state);
	return;
      }

      // No changes are handled here

      assert(false);
    }

    
    public Entity.Change reverse()
    {
      // Determine whether this change is handled here or by Drawable.Change

      if(isParent(changeType))
      {
	return super.reverse();
      }

      // No changes are handled here

      assert(false); return null;
    }


    /**
     * <p>Obtain a String representation of the Change.</p>
     * @return a String describing the effect of applying this Change
     */

    public String toString()
    {
      // Determine whether this change is handled here or by Drawable.Change

      if(isParent(changeType))
      {
	return "Shape.Change:"+super.toString();
      }

      switch(changeType)
      {
	default:
	{
	  return "Shape.Change:UNKNOWN";
	}
      }
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new Shape with the specified identifier, 
   * center, Vector of control points, Vector of anchored control points,
   * boolean indicating whether it is locked, array of {@link Shortcut}
   * objects, {@link Brush} and {@link Pen}.</p>
   */

  protected Shape(EntityID identifier, Point center, Vector controlPoints,
		  Vector controlPointsAnchored, boolean locked,
		  Shortcut[] shortcut, Brush brush, Pen pen)
  {
    super(identifier, center, controlPoints, controlPointsAnchored,
	  locked, shortcut);
    
    this.brush=brush;
    this.pen=pen;

    assert(isShapeDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isShapeDataValid()
  {
    return isDrawableDataValid();
  }

  /**
   * <p>Obtain the number of pixels extra bounding rectangle to allow,
   * e.g. the pen width.</p>
   */

  protected int getExtraBoundingWidth(int zoomLevel)
  {
    if((pen==null)||(pen.getStyle()==Pen.CLEAR))
    {
      return 1;
    }
    else
    {
      return pen.isAbsolute()?pen.getWidth()+1:pen.getWidth()*zoomLevel+1;
    }
  }
 
  /**
   * <p>Override Drawable.getPropeties to provide access to PEN and BRUSH
   * data.</p>
   * @return a Vector of Property objects describing the Shape
   */

  public Vector getProperties()
  {
    Vector result=super.getProperties();

    result.add(new Property(Property.BRUSH, brush));
    result.add(new Property(Property.PEN, pen));

    return result;
  }

  /**
   * <p>Override Drawable.edit to provide editing of Pen and Brush data.</p>
   * @param property the {@link Property} to set
   */

  protected void edit(Property property)
  {
    // Preconditions; property not null

    assert(property!=null);

    // Method proper

    switch(property.getType())
    {
      case Property.BRUSH:
      {
	brush=((Brush)property.getValue());
	break;
      }

      case Property.PEN:
      {
	pen=((Pen)property.getValue());
	break;
      }

      default:
      {
	super.edit(property);
      }
    }
  }

  /**
   * <p>Configure a specified Graphics2D instance with specified
   * {@link Brush} settings.</p>
   * @param g the Graphics2D to configure
   * @param zoomLevel the zoom level, used for rendering absolute sizes
   */

  protected void configureGraphicsBrush(Graphics2D g, int zoomLevel, Brush brush)
  {
    // Preconditions; graphics not null, zoom level valid

    assert((g!=null)&&(zoomLevel>0));

    if((brush==null)||(brush.getStyle()==brush.CLEAR))
    {
      g.setPaint(new Color(0, 0, 0, 0));
    }
    else
    {
      g.setPaint(brush.getColor());
    }
  }

  /**
   * <p>Configure a specified Graphics2D instance with specified
   * {@link Pen} settings.</p>
   * @param g the Graphics2D to configure
   * @param zoomLevel the zoom level, used for rendering absolute sizes
   */

  public static void configureGraphicsPen(Graphics2D g, int zoomLevel, Pen pen)
  {
    // Preconditions; graphics not null, zoom level valid

    assert((g!=null)&&(zoomLevel>0));

    // Method proper

    if(pen==null)
    {
      g.setColor(Color.BLACK);
      g.setStroke(new BasicStroke(1));
    }
    else
    {
      g.setColor(pen.getColor());

      float width=pen.isAbsolute()?((float)pen.getWidth())/zoomLevel:
	pen.getWidth();
      

      switch(pen.getStyle())
      {
	case Pen.SOLID:
	{
	  g.setStroke(new BasicStroke(width));
	  break;
	}

	case Pen.CLEAR:
	{
	  g.setStroke(new BasicStroke(0));
	  break;
	}

	case Pen.DOTTED:
	{
	  g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT,
				      BasicStroke.JOIN_BEVEL, 1,
				      new float[]{1, 1}, 0));
	  break;
	}

	case Pen.DASHED:
	{
	  g.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT,
				      BasicStroke.JOIN_BEVEL, 1,
				      new float[]{3, 3}, 0));
	  break;
	}

	default:
	{
	  assert(false);
	}
      }
    }
  }

  /**
   * <p>Obtain a String representation of the Shape.</p>
   */

  public String toString()
  {
    return "Shape:("+super.toString()+"):"
      +brush+":"
      +pen;
  }
}
