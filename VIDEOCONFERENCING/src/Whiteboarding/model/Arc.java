package Whiteboarding.model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.AffineTransform;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>Class representing the {@link Arc} {@link Entity}. It extends
 * {@link Shape} to provide a Shape that starts off with, and retains,
 * an elliptical shape.</p>
 * <p>A newly created Arc has passes through points (-1, 0), (0, 1)
 * and (1, 0).</p>
 * @author David Morgan
 */

public class Arc extends Shape implements Serializable
{
  static final long serialVersionUID = -194245206806514387L;

  // 4. Static inner method declarations

  /**
   * <p>Subclass of Shape.Change providing extra operations on Arcs,
   * and implementing existing operations differently where necessary.</p>
   */ 

  public static class Change extends Shape.Change
  {
    static final long serialVersionUID = 4335184268952823494L;

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
     * Arc.Change that provide new functionality.</p>
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
	  controlPoints.add(new Point(0,-1));
	  controlPoints.add(new Point(1,0));

	  Arc arc=new Arc(target[0], new Point(0, 0),
				      controlPoints, new Vector(),
				      false, null, null, null);

	  state.addEntity(arc);
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
     * Arc.Change that provide new functionality.</p>
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
	return "Arc.Change:"+super.toString();
      }

      switch(changeType)
      {
	case CREATE:
	{
	  return "Arc.Change:CREATE:"+target[0];
	}
  
	default:
	{
	  return "Arc.Change:UNKNOWN";
	}
      }
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new Arc with the specified identifier, center, Vector
   * of control points, boolean indicating whether it is locked, {@link Brush}
   * and {@link Pen}.</p>
   */

  protected Arc(EntityID identifier, Point center, Vector controlPoints,
		    Vector controlPointsAnchored, boolean locked,
		    Shortcut[] shortcut, Brush brush, Pen pen)
  {
    super(identifier, center, controlPoints, controlPointsAnchored,
	  locked, shortcut, brush, pen);

    assert(isArcDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isArcDataValid()
  {
    return isDrawableDataValid();
  }

  /**
   * <p>In order to render an Entity, supply it with the graphics context to
   * render on, and a Arc that details which part of the org.davidmorgan.jinn.is
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
          
      Point left=(Point)controlPoints.get(0);
      Point middle=(Point)controlPoints.get(1);
      Point right=(Point)controlPoints.get(2);

      /**
       * Find the centre of the circle of which the arc is a part.
       * Thanks to http://mathforum.org/library/drmath/view/55239.html for
       * the requisite algebra.
       */

      double x1=((double)left.x)/zoomLevel;
      double y1=((double)left.y)/zoomLevel;
      double x2=((double)middle.x)/zoomLevel;
      double y2=((double)middle.y)/zoomLevel;
      double x3=((double)right.x)/zoomLevel;
      double y3=((double)right.y)/zoomLevel;

      double x1_=x1*x1;
      double y1_=y1*y1;
      double x2_=x2*x2;
      double y2_=y2*y2;
      double x3_=x3*x3;
      double y3_=y3*y3;

      double n1=((x1_+y1_)*(y2-y3))+
	(y1*((x3_+y3_)-(x2_+y2_)))+
	(((x2_+y2_)*y3)-(y2*(x3_+y3_)));

      double n2=(x1*((x2_+y2_)-(x3_+y3_)))+
	((x1_+y1_)*(x3-x2))+
	((x2*(x3_+y3_))-((x2_+y2_)*x3));

      double d=(x1*(y2-y3))+
	(y1*(x3-x2))+
	((x2*y3)-(y2*x3));

      double xc=n1/(2*d);
      double yc=n2/(2*d);

      System.out.println(n1+" "+n2+" "+d);
      System.out.println("("+xc+", "+yc+")");

      System.out.println(new Point((int)x1, (int)y1).distance(new Point((int)xc, (int)yc)));
      System.out.println(new Point((int)x2, (int)y2).distance(new Point((int)xc, (int)yc)));
      System.out.println(new Point((int)x3, (int)y3).distance(new Point((int)xc, (int)yc)));

      /**
       * Find the radius of the circle of which the arc is a part.
       */

      double radius=new Point((int)x1, (int)y1).distance(new Point((int)xc, (int)yc));

      Arc2D arc=new Arc2D.Double(-radius, -radius,
				 radius*2, radius*2, 0, 360, 0);

      g.translate(xc, yc);
      g.rotate(angle);

      configureGraphicsBrush(g, zoomLevel, brush);
      g.fill(arc);

      configureGraphicsPen(g, zoomLevel, pen);
      g.draw(arc);

      g.setTransform(new AffineTransform());
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * <p>Create a deep copy of the Claim.</p>
   */

  public Object clone()
  {
    Claim result=(Claim)super.clone();
    
    return result;
  }

  /**
   * <p>Obtain a String representation of the Arc.</p>
   */

  public String toString()
  {
    return "Arc:("+super.toString()+")";
  }
}
