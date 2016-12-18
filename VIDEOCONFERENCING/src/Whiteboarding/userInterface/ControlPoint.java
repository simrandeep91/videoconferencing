
package Whiteboarding.userInterface;

import java.awt.Point;
import java.awt.Color;

/**
 * <p>Class describing a control point being displayed on the org.davidmorgan.jinn.</p>
 */

public class ControlPoint extends Point
{
  /**
   * <p>Color of the control point.</p>
   */

  public Color color;

  /**
   * <p>Time to display the control point for.</p>
   */

  public int timeLeft;

  /**
   * <p>Create a new ControlPoint with the specified {@link Point} position,
   * {@link Color} and integer time to display.</p>
   */

  public ControlPoint(Point point, Color color, int timeLeft)
  {
    super(point);
    this.color=color;
    this.timeLeft=timeLeft;
  }

  /**
   * <p>Determine whether one ControlPoint is equal to another, that is, has
   * the same position, color, and timeLeft.</p>
   */

  public boolean equals(Object value)
  {
    if(!(value instanceof ControlPoint)) return false;

    ControlPoint controlPoint=(ControlPoint)value;

    if(controlPoint.x!=x) return false;
    if(controlPoint.y!=y) return false;

    if(controlPoint.timeLeft!=timeLeft) return false;
    if(!controlPoint.color.equals(color)) return false;

    return true;
  }
}
