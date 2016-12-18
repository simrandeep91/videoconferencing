package Whiteboarding.model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>Class representing the {@link Ellipse} {@link Entity}. It extends
 * {@link Shape} to provide a Shape that starts off with, and retains,
 * an elliptical shape.</p>
 * <p>A newly created Ellipse has dimensions (2, 2) at position (-1, -1).</p>
 * @author David Morgan
 */

public class Ellipse extends Shape implements Serializable
{
  static final long serialVersionUID = 7573663252624345584L;

  // 4. Static inner method declarations

  /**
   * <p>Subclass of Shape.Change providing extra operations on Ellipses,
   * and implementing existing operations differently where necessary.</p>
   */ 

  public static class Change extends Shape.Change
  {
    static final long serialVersionUID = 5009134955929445328L;

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
     * Ellipse.Change that provide new functionality.</p>
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
	  controlPoints.add(new Point(-1,-1));
	  controlPoints.add(new Point(0,-1));
	  controlPoints.add(new Point(1,-1));
	  controlPoints.add(new Point(1,0));
	  controlPoints.add(new Point(1,1));
	  controlPoints.add(new Point(0,1));
	  controlPoints.add(new Point(-1,1));
	  controlPoints.add(new Point(-1,0));

	  Ellipse ellipse=new Ellipse(target[0], new Point(0, 0),
				      controlPoints, new Vector(),
				      false, null, null, null);

	  state.addEntity(ellipse);
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
     * Ellipse.Change that provide new functionality.</p>
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
	return "Ellipse.Change:"+super.toString();
      }

      switch(changeType)
      {
	case CREATE:
	{
	  return "Ellipse.Change:CREATE:"+target[0];
	}
  
	default:
	{
	  return "Ellipse.Change:UNKNOWN";
	}
      }
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new Ellipse with the specified identifier, center, Vector
   * of control points, boolean indicating whether it is locked, {@link Brush}
   * and {@link Pen}.</p>
   */

  protected Ellipse(EntityID identifier, Point center, Vector controlPoints,
		    Vector controlPointsAnchored, boolean locked,
		    Shortcut[] shortcut, Brush brush, Pen pen)
  {
    super(identifier, center, controlPoints, controlPointsAnchored,
	  locked, shortcut, brush, pen);

    assert(isEllipseDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isEllipseDataValid()
  {
    return isDrawableDataValid();
  }

  /**
   * <p>In order to render an Entity, supply it with the graphics context to
   * render on, and a Ellipse that details which part of the org.davidmorgan.jinn.is
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
      java.awt.Rectangle bounds=g.getClipBounds();
      int zoomLevel=view.width/bounds.width;
      g.translate(-view.x/zoomLevel+bounds.x, -view.y/zoomLevel+bounds.y);
          
      Point topLeftCorner=(Point)controlPoints.get(0);
      Point topRightCorner=(Point)controlPoints.get(2);
      Point bottomRightCorner=(Point)controlPoints.get(4);

      double width=((double)topLeftCorner.distance(topRightCorner))/zoomLevel;
      double height=((double)topRightCorner.distance(bottomRightCorner))/zoomLevel;

      Ellipse2D ellipse=new Ellipse2D.Double(-width/2, -height/2,
					    width, height);

      g.translate(((double)center.x)/zoomLevel, ((double)center.y)/zoomLevel);
      g.rotate(angle);

      configureGraphicsBrush(g, zoomLevel, brush);
      g.fill(ellipse);

      configureGraphicsPen(g, zoomLevel, pen);
      g.draw(ellipse);

      g.setTransform(new AffineTransform());
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * <p>Create a deep copy of the Ellipse.</p>
   */

  public Object clone()
  {
    Ellipse result=(Ellipse)super.clone();
    
    return result;
  }

  /**
   * <p>Obtain a String representation of the Ellipse.</p>
   */

  public String toString()
  {
    return "Ellipse:("+super.toString()+")";
  }
}
