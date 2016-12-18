package Whiteboarding.model;

import java.awt.Color;

import java.io.Serializable;

/**
 * <p>Class representing the fill style and color of a {@link Shape}
 * {@link Entity}.</p>
 */

public class Brush implements Serializable
{
  // 1. Static variable field declarations

  static final long serialVersionUID = -1417420078498066865L;

  /**
   * Indicates that a Shape is not filled.
   */

  public static final int CLEAR=0;
 
  /**
   * Indicates that a Shape is filled solidly.
   */

  public static final int SOLID=1;

  // 2. Instance variable field declarations

  /**
   * <p>Integer holding the style.</p>
   */

  private int style;

  /**
   * <p>{@link Color} holding the color.</p>
   */

  private Color color;

  // 7. Instance constructor declarations

  /**
   * <p>Create a new Brush with the specified fill style and {@link Color}.
   * If the fill style is clear you must supply null as the color.</p>
   */

  public Brush(int style, Color color)
  {
    this.style=style;
    this.color=color;

    assert(isDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate instance data.</p>
   */

  public boolean isDataValid()
  {
    return ((style==CLEAR)&&(color==null))||
      ((style==SOLID)&&(color!=null));
  }

  /**
   * <p>Obtain an integer representing the fill style.</p>
   */

  public int getStyle()
  {
    return style;
  }

  /**
   * <p>Obtain the fill {@link Color}</p>.
   */

  public Color getColor()
  {
    return color;
  }

  /**
   * <p>Obtain a String representation of the Brush.</p>
   */

  public String toString()
  {
    return "Brush:"+(style==CLEAR?"clear":"solid:"+color);

  }
}
