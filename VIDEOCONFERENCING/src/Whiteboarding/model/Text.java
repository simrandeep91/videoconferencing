package Whiteboarding.model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Font;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.font.FontRenderContext;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>Class representing the {@link Text} {@link Entity}. It extends
 * {@link Shape} to provide a Shape that starts off with, and retains,
 * an elliptical shape.</p>
 * <p>A newly created Text has dimensions (1, 0) at position (-1, 0).</p>
 * @author David Morgan
 */

public class Text extends Shape implements Serializable
{
  static final long serialVersionUID = -201709745056467340L;

  // 2. Instance variable field declarations

  /**
   * <p>{@link Font} storing the text font.</p>
   */

  protected Font font;

  /**
   * <p>String storing the text this object displays.</p>
   */

  protected String text;
 
  // 4. Static inner method declarations

  /**
   * <p>Subclass of Shape.Change providing extra operations on Texts,
   * and implementing existing operations differently where necessary.</p>
   */ 

  public static class Change extends Shape.Change
  {
    static final long serialVersionUID = -1632643929565631257L;

    // 4.7 Instance constructor declarations

    /**
     * <p>Protected empty constructor.</p>
     */

    protected Change() {}

    /**
     * <p>Change modifies Shape in a specified way.</p>
     */

    public Change(int changeType, Object[] parameter, EntityID[] target)
    {
      initialize(changeType, parameter, target);
    }

    // 4.9 Instance method declarations

    protected void initialize(int changeType, Object[] parameter,
			      EntityID[] target)
    {
      // Determine whether this change is handled here or by Shape.Change

      if(isParent(changeType))      // if new creation then false, otherwise true
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
	case CREATE:
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
	  return (parameter==null)&&(target!=null)&&(target.length==1)
	    &&(target[0]!=null);
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

	default:
	{
	  return super.isUndoDataValid();
	}
      }
    }

    /**
     * <p>Apply the change. This must be overriden by subclasses of
     * Text.Change that provide new functionality.</p>
     */

    public void apply(SessionState state)
    {
      // Determine whether this change is handled here or by Shape.Change

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

	  Vector controlPoints=new Vector();
	  controlPoints.add(new Point(-1,0));

	  Text text=new Text(target[0], new Point(0, 0), controlPoints,
			     new Vector(), false, null, null, null, "Text",
			     new Font("Times", Font.PLAIN, 8));

	  state.addEntity(text);
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
     * Text.Change that provide new functionality.</p>
     */

    public void undo(SessionState state)
    {
      // Determine whether this change is handled here or by Shape.Change

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
	return "Text.Change:"+super.toString();
      }

      switch(changeType)
      {
	case CREATE:
	{
	  return "Text.Change:CREATE:"+target[0];
	}
  
	default:
	{
	  return "Text.Change:UNKNOWN";
	}
      }
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new Text with the specified identifier, center, Vector
   * of control points, boolean indicating whether it is locked, {@link Brush}
   * and {@link Pen}.</p>
   */

  protected Text(EntityID identifier, Point center, Vector controlPoints,
		 Vector controlPointsAnchored, boolean locked,
		 Shortcut[] shortcut, Brush brush, Pen pen,
		 String text, Font font)
  {
    super(identifier, center, controlPoints, controlPointsAnchored, locked,
	  shortcut, brush, pen);

    this.text=text;
    this.font=font;
    
    assert(isTextDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isTextDataValid()
  {
    if(!isShapeDataValid()) return false;

    return (text!=null)&&(font!=null);
  }

  /**
   * <p>Override Shape.getPropeties to provide access to IS_OPEN,
   * IS_STRAIGHT_LINES and LINE_ENDINGS {@link Property} types.
   * data.</p>
   * @return a Vector of Property objects describing the Shape
   */

  public Vector getProperties()
  {
    Vector result=super.getProperties();

    result.add(new Property(Property.TEXT, text));
    result.add(new Property(Property.FONT, font));

    return result;
  }

  /**
   * <p>Set a property of the Polygon. Subclasses must override this to
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
      case Property.TEXT:
      {
	text=(String)property.getValue();
	break;
      }

      case Property.FONT:
      {
	font=(Font)property.getValue();
	break;
      }

      default:
      {
	super.edit(property);
      }
    }
  }

  /**
   * <p>Obtain the Bounding Rectangle of the Text.</p>
   */

  public java.awt.Rectangle getInnerBoundingRectangle(int zoomLevel)
  {
    Point point=(Point)controlPoints.get(0);

    TextLayout layout=new TextLayout(text, font, new FontRenderContext(
      new AffineTransform(), true, true));

    Rectangle2D layoutBounds=layout.getBounds();

    double width=center.distance(point)*2;
    double scaleFactor=((double)layoutBounds.getWidth())/width;
    double height=((double)layoutBounds.getHeight())*scaleFactor;

    AffineTransform transform=new AffineTransform();

    transform.translate(((double)center.x), ((double)center.y));
    transform.rotate(angle);
    transform.scale(1/scaleFactor, 1/scaleFactor);
    transform.translate((float)(-width*scaleFactor/2), 0);

    java.awt.Shape outline=layout.getOutline(transform);

    java.awt.Rectangle bounds=outline.getBounds();

    return new java.awt.Rectangle(bounds.x-1, bounds.y-1,
				  bounds.width+2, bounds.height+2);
  }

  /**
   * <p>In order to render an Entity, supply it with the graphics context to
   * render on, and a Text that details which part of the project. is
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
    g.translate(-view.x/zoomLevel+bounds.x, -view.y/zoomLevel+bounds.y);

    Point point=(Point)controlPoints.get(0);
    
    float x=((float)point.x)/zoomLevel;
    float y=((float)point.y)/zoomLevel;

    TextLayout layout=new TextLayout(text, font, g.getFontRenderContext());
    Rectangle2D layoutBounds=layout.getBounds();

    double width=(center.distance(point)*2)/((double)zoomLevel);
    double scaleFactor=((double)layoutBounds.getWidth())/width;
    double height=((double)layoutBounds.getHeight())*scaleFactor*zoomLevel;

    g.translate(((double)center.x)/zoomLevel, ((double)center.y)/zoomLevel);
    g.rotate(angle);
    g.scale(1/scaleFactor, 1/scaleFactor);
    g.translate((float)(-width*scaleFactor/2), 0);

    java.awt.Shape outline=layout.getOutline(g.getTransform());

    java.awt.Rectangle drawBounds=outline.getBounds();
    drawBounds=new java.awt.Rectangle(drawBounds.x-1, drawBounds.y-1,
				      drawBounds.width+2, drawBounds.height+2);

    if(bounds.intersects(drawBounds))
    {
      configureGraphicsBrush(g, zoomLevel, brush);
      layout.draw(g, 0, 0);

      g.setTransform(new AffineTransform());

      configureGraphicsPen(g, zoomLevel, pen);
      g.draw(outline);

      return true;
    }
    else
    {
      g.setTransform(new AffineTransform());

      return false;
    }
  }

  /**
   * <p>Create a deep copy of the Text.</p>
   */

  public Object clone()
  {
    Text result=(Text)super.clone();
    
    return result;
  }

  /**
   * <p>Obtain a String representation of the Text.</p>
   */

  public String toString()
  {
    return "Text:("+super.toString()+"):"
      +text+":"
      +font;
  }
}
