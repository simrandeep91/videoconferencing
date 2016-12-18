package Whiteboarding.model;

import java.awt.Color;

import java.io.Serializable;

/**
 * <p>Class representing the outline style, width and color of a {@link Shape}
 * {@link Entity}.</p>
 * <p>Note that for lines, all that is visible is outline.</p>
 */

public class Pen implements Serializable
{
  static final long serialVersionUID = 5670173103085939652L;

  // 1. Static variable field declarations

  /**
   * <p>Indicates that the outline is solid.</p>
   */

  public static final int SOLID=0;

  /**
   * <p>Indicates that the outline is clear.</p>
   */

  public static final int CLEAR=1;

  /**
   * <p>Indicates that the outline is dotted.</p>
   */

  public static final int DOTTED=2;

  /**
   * <p>Indicates that the outline is dashed.</p>
   */

  public static final int DASHED=3;

  // 2. Instance variable field declarations

  /**
   * <p>Integer storing the pen style.</p>
   */

  private int style;

  /**
   * <p>Integer storing the pen width.</p>
   */

  private int width;
  
  /**
   * <p>{@link Color} storing the pen color.</p>
   */

  private Color color;

  /**
   * <p>Boolean indicating whether the width is true: absolute or false:
   * pixel-based.</p>
   */

  private boolean absolute;

  // 7. Instance constructor declarations

  /**
   * <p>Create a new pen with the specified style, pixel width, and Color.</p>
   */

  public Pen(int style, int width, Color color)
  {
    this.style=style;
    this.width=width;
    this.color=color;
    this.absolute=false;

    assert(isDataValid());
  }

  /**
   * Create a new pen with the specified style, width, Color, and boolean
   * indicating true: width is absolute, or false: width is pixel-based.
   */

  public Pen(int style, int width, Color color, boolean absolute)
  {
    this.style=style;
    this.width=width;
    this.color=color;
    this.absolute=absolute;

    assert(isDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate instance data.</p>
   */
  
  private boolean isDataValid()
  {
    return (style>=SOLID)&&(style<=DASHED)&&(width>0)&&
      ((style==CLEAR)==(color==null));
  }

  /**
   * <p>Obtain an integer representing the Pen style.</p>
   */

  public int getStyle()
  {
    return style;
  }
  
  /**
   * <p>Obtain an integer representing the pen width.</p>
   */

  public int getWidth()
  {
    return width;
  }

  /**
   * <p>Obtain a boolean indicating whether the pen width is true: absolute or
   * false: pixel-based.</p>
   */

  public boolean isAbsolute()
  {
    return absolute;
  }

  /**
   * <p>Obtain the {@link Color} of the Pen. If the pen style is CLEAR, null is
   * returned.</p>
   */

  public Color getColor()
  {
    return color;
  }

  /**
   * <p>Obtain a String representation of the Pen.</p>
   */

  public String toString()
  {
    String styleString="UNKNOWN";

    switch(style)
    {
      case SOLID: styleString="SOLID"; break;
      case CLEAR: return "Pen:CLEAR";
      case DOTTED: styleString="DOTTED"; break;
      case DASHED: styleString="DASHED"; break;
    }

    return "Pen:"+styleString+":"+width+"("+(absolute?"absolute":"pixels")+
      "):"+color;
  }
}
