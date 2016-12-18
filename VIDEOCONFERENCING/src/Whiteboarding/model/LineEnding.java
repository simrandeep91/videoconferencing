package Whiteboarding.model;

import java.io.Serializable;

/**
 * <p>Class representing the ending style of one end of an open Polyline or
 * Arc.</p>
 */

public class LineEnding implements Serializable
{
  static final long serialVersionUID = 4596313685669783735L;

  // 2. Instance variable field declarations

  /**
   * <p>Arrow width.</p>
   */

  private int width;

  /**
   * <p>Arrow height.</p>
   */

  private int height;

  /**
   * <p>Arrow brush.</p>
   */

  private Brush brush;

  /**
   * <p>Arrow pen.</p>
   */

  private Pen pen;
  
  // 7. Instance constructor declaration

  /**
   * <p>Create a LineEnding object with the specified arrow width, arrow height,
   * Brush and Pen. Note that a null LineEnding indicates no special ending,
   * and a null Brush and/or Pen indicates that the Brush and/or Pen of the
   * owning Drawable is to be used.</p>
   *
   * <p>The arrow heights and widths are interpreted as absolute (as opposed to
   * pixel based) if and only if the Pen uses absolute width.</p>
   */
  
  public LineEnding(int width, int height, Brush brush, Pen pen)
  {
    this.width=width;
    this.height=height;
    this.brush=brush;
    this.pen=pen;

    assert(isDataValid());
  }

  // 9. Instance method declarations
  
  /**
   * <p>Validate instance data.</p>
   */

  private boolean isDataValid()
  {
    return (width>0)&&(height>0);
  }
  
  /**
   * <p>Get the arrow width.</p>
   */

  public int getWidth()
  {
    return width;
  }

  /**
   * <p>Get the arrow height.</p>
   */

  public int getHeight()
  {
    return height;
  }
  
  /**
   * <p>Obtain the Brush the arrow is to be filled with, or null if the Brush of
   * the parent Drawable should be used.</p>
   */

  public Brush getBrush()
  {
    return brush;
  }

  /**
   * <p>Obtain the Pen the arrow is to be outline with, or null if the Pen of the
   * parent Drawable should be used.</p>
   */

  public Pen getPen()
  {
    return pen;
  }

  /**
   * <p>Obtain a String representation of the LineEnding.</p>
   */

  public String toString()
  {
    return "LineEnding:"+width+":"+height+":"+
      (brush==null?"Brush:inherited":brush.toString())+":"+
      (pen==null?"Pen:inherited":pen.toString());
  }
}
