package Whiteboarding.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>Class representing the Claim Entity.</p>
 * <p>A newly created Claim has dimensions (2, 2) at position (-1, -1).</p>
 * @author David Morgan
 */

public class Claim extends Drawable implements Serializable, Cloneable
{
  static final long serialVersionUID = -573651082428163802L;

  // 2. Instance variable field declarations

  /**
   * The EntityID corresponding to the UserID of the claiming user.
   */

  protected EntityID owner;

  // 4. Static member inner class declarations

  /**
   * <p>Subclass of {@link Drawable.Change} providing extra operations on
   * Claims, and implementing existing operations differently where
   * necessary.</p>
   */ 

  public static class Change extends Drawable.Change
  {
    static final long serialVersionUID = -5479444762141722664L;

    // 4.1 Static variable field declarations

     /**
      * Indicates an operation that sets the owner of a Claim.
      *
      * There are no parameters.
      *
      * The first target specifies the Claim to alter; the second target
      * specifies the UserInfo Entity of the owner. It only has an effect an
      * owner is not already set.
      */

    public static final int SET_CLAIM_OWNER=19;

    // 4.7 Instance constructor declarations

    /**
     * <p>Protected empty constructor.</p>
     */
    
    protected Change() {}

    /**
     * <p>Change modifies a Claim in a specified way.</p>
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
	case SET_CLAIM_OWNER:
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
	  return (parameter==null)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case SET_CLAIM_OWNER:
	{
	  return (parameter==null)&&
	    (target!=null)&&(target.length==2)&&(target[0]!=null);
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
	  return (undoData==null);
	}

	case SET_CLAIM_OWNER:
	{
	  return (undoData!=null)&&(undoData.length==1)
	    &&((undoData[0]==null)||(undoData[0] instanceof EntityID));
	}

	default:
	{
	  return super.isUndoDataValid();
	}
      }
    }

    /**
     * <p>Apply the change. This must be overriden by subclasses of Claim.Change
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
          // Gather data for undo

          // Make the change

	  Vector controlPoints=new Vector();
	  controlPoints.add(new Point(-1,-1));
	  controlPoints.add(new Point(1,-1));
	  controlPoints.add(new Point(1,1));
	  controlPoints.add(new Point(-1,1));

	  Claim claim=new Claim(target[0], new Point(0, 0), controlPoints,
				new Vector(), false, null, null);

	  state.addEntity(claim);
	  break;
	}

	case SET_CLAIM_OWNER:
	{
	  Claim claim=(Claim)state.getEntity(target[0]);

          // Gather data for undo

	  undoData=new Object[] { claim.getOwner() };

          // Make the change
	  
	  claim.owner=target[1];
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
	  state.removeEntity(target[0]);
	  break;
	}

	case SET_CLAIM_OWNER:
	{
	  Claim claim=(Claim)state.getEntity(target[0]);
	  claim.owner=(EntityID)undoData[0];
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
	  return new Entity.Change(DELETE, undoData, target);
	}

	case SET_CLAIM_OWNER:
	{
	  return new Claim.Change(SET_CLAIM_OWNER, null, new EntityID[]
	      { target[0], (EntityID)undoData[0] });
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
	return "Claim:"+super.toString();
      }

      switch(changeType)
      {
	case CREATE:
	{
	  return "Claim.Change:CREATE:"+target[0];
	}

	case SET_CLAIM_OWNER:
	{
	  return "Claim.Change:SET_CLAIM_OWNER:"+target[0]+":"+target[1];
	}
  
	default:
	{
	  return "Claim.Change:UNKNOWN";
	}
      }	
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new Claim with the specified identifier, 
   * center, Vector of control points, boolean indicating whether
   * it is locked, array of {@link Shorcut}s and owner.</p>
   */

  protected Claim(EntityID identifier, Point center,
		  Vector controlPoints, Vector controlPointsAnchored,
		  boolean locked, Shortcut[] shortcut, EntityID owner)
  {
    super(identifier, center, controlPoints, controlPointsAnchored,
	  locked, shortcut);
    
    this.owner=owner;
    
    assert(isClaimDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isClaimDataValid()
  {
    return isDrawableDataValid();
  }

  /**
   * Obtain an EntityID pointing to the owner's UserInfo Entity.
   */

  public EntityID getOwner()
  {
    return owner;
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
      java.awt.Rectangle bounds=g.getClipBounds();
      int zoomLevel=view.width/bounds.width;
      g.translate(-view.x/zoomLevel+bounds.x, -view.y/zoomLevel+bounds.y);

      UserInfo userInfo=null;

      if(owner!=null) userInfo=(UserInfo)state.getEntity(owner);

      Color mainColor;

      if((owner==null)||(userInfo==null)) mainColor=Color.BLACK;
      else mainColor=userInfo.getColor();

      BasicStroke dashed=new BasicStroke(1, BasicStroke.CAP_BUTT, 
					 BasicStroke.JOIN_BEVEL, 1,
					 new float[]{2,2},
					 ((float)(System.currentTimeMillis()%4000))/250);

      BasicStroke antiDashed=new BasicStroke(1, BasicStroke.CAP_BUTT, 
					 BasicStroke.JOIN_BEVEL, 1,
					 new float[]{2,2},
					 2+((float)(System.currentTimeMillis()%4000))/250);

      int[] x=new int[4];
      int[] y=new int[4];

      for(int i=0; i!=4; i++)
      {
	Point point=(Point)controlPoints.get(i);

	x[i]=point.x/zoomLevel;
	y[i]=point.y/zoomLevel;
      }

      Polygon polygon=new Polygon(x, y, 4);     // This polygon is inbuilt class.

      g.setStroke(dashed);
      g.setColor(mainColor);
      g.draw(polygon);

      g.setStroke(antiDashed);
      g.setColor(Color.WHITE);
      g.draw(polygon);

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
   * <p>Override Drawable.absoluteRotate() in order to prevent the
   * Claim rectangle being rotated; have it move instead.</p>
   */

  protected void absoluteRotate(Point center, double angle)
  {
    Point translation=new Point(center);

    absoluteRotatePoint(translation, center, angle);

    translation.setLocation(translation.x-center.x,
			    translation.y-center.y);

    move(translation);
  }

  /**
   * <p>Obtain a String representation of the Claim.</p>
   */

  public String toString()
  {
    return "Claim:("+super.toString()+"):"
      +owner;
  }
}
