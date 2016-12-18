package Whiteboarding.model;

import java.awt.Font;
import java.io.Serializable;

/**
 * Class representing an editable Property of a Drawable. The classes in which
 * most values are stored remain to be specified.
 */

public class Property implements Serializable
{
  // 1. Static variable field declarations

  static final long serialVersionUID = -2210049571469979385L;

  /**
   * Indicates that the value is an array of Shortcut objects.
   */

  public static final int SHORTCUTS=0; 

  /**
   * Indicates that the value is a Boolean indicating whether the Drawable is
   * locked.
   */

  public static final int IS_LOCKED=1;

  /**
   * Indicates that the value is a Boolean indicating whether a Group is in
   * Normal mode, as opposed to Labelled mode.
   */

  public static final int IS_GROUP_NORMAL=2;

  /**
   * Indicates that the value is a String, the specific use of which depends on
   * the Drawable.
   */

  public static final int TEXT=3;

  /**
   * Indicates that the value is a Brush object.
   */

  public static final int BRUSH=4;

  /**
   * Indicates that the value is a Pen object.
   */

  public static final int PEN=5;

  /**
   * Indicates that the value is a java.awt.Font.
   */

  public static final int FONT=6;

  /**
   * Indicates that the value is a Boolean indicating whether a Polyline is
   * open or closed.
   */

  public static final int IS_OPEN=7;

  /**
   * Indicates that the value is a Boolean indicating whether a Polyline is
   * comprised of straight lines or curves.
   */

  public static final int IS_STRAIGHT_LINES=8;

  /**
   * Indicates that the value is a LineEnding[] object with the first entry
   * describing the start of the line and the second the end of the line.
   *
   * In both cases null indicates a normal ending, and a LineEnding object
   * indicates a special ending.
   */

  public static final int LINE_ENDINGS=9;

  // 2. Instance variable field declarations

  /**
   * Byte representing the type of property.
   */

  private byte type;

  /**
   * Value of the property.
   */

  private Object value;

  // 7. Instance constructor declarations
  
  /**
   * Create a Property with the specified String type and value.
   */

  public Property(int type, Object value)
  {
    this.type=(byte)type;
    this.value=value;

    assert(isDataValid());
  }

  // 9. Instance method declarations

  /**
   * Determine whether type and supplied data match.
   */

  private boolean isDataValid()
  {
    switch(type)
    {
      case SHORTCUTS:
      {
	return (value==null)||(value instanceof Shortcut[]);
      }

      case IS_LOCKED:
      {
	return (value instanceof Boolean);
      }

      case IS_GROUP_NORMAL:
      {
	return (value instanceof Boolean);
      }

      case TEXT:
      {
	return (value instanceof String);
      }

      case BRUSH:
      {
	return (value==null)||(value instanceof Brush);
      }

      case PEN:
      {
	return (value==null)||(value instanceof Pen);
      }

      case FONT:
      {
	return (value instanceof Font);
      }

      case IS_OPEN:
      {
	return (value instanceof Boolean);
      }

      case IS_STRAIGHT_LINES:
      {
	return (value instanceof Boolean);
      }

      case LINE_ENDINGS:
      {
	return (value instanceof LineEnding[])&&
	  (((LineEnding[])value).length==2);
      }
      
      default:
      {
	return false;
      }
    }
  }

  /**
   * <p>Obtain the Property type.</p>
   * @return an integer matching one of the category static integers
   */

  public int getType()
  {
    return type;
  }

  /**
   * <p>Obtain the Property value.</p>
   * @return an Object containing the value with type determined by getType()
   */

  public Object getValue()
  {
    return value;
  }

  /**
   * <p>Obtain a hash code corresponding to the propert value.</p>
   */

  public int getValueHash()
  {
    int result=type;

    if(value==null) return result;

    switch(type)
    {
      case SHORTCUTS:
      {
	return result;
      }
    
      case IS_LOCKED:
      case IS_GROUP_NORMAL:
      case IS_OPEN:
      case IS_STRAIGHT_LINES:
      {
	return ((Boolean)value).booleanValue()?38923*result:result;
      }
    
      case TEXT:
      {
	return result+hashText((String)value);
      }
    
      case BRUSH:
      {
	return result*((Brush)value).getColor().getRGB()
	  +((Brush)value).getStyle();
      }
    
      case PEN:
      {
	Pen pen=(Pen)value;

	return result*pen.getColor().getRGB()+pen.getStyle()+pen.getWidth()+
	  (pen.isAbsolute()?3849:23894);
      }
    
      case FONT:
      {
	Font font=(Font)value;

	return result+hashText(font.getName());
      }
   
    
      case LINE_ENDINGS:
      {
	LineEnding[] lineEnding=(LineEnding[])value;

	if(lineEnding[0]!=null)
	{
	  result+=lineEnding[0].getWidth()*12039;
	  result+=lineEnding[0].getHeight()*23894107;
	}

	if(lineEnding[1]!=null)
	{
	  result+=lineEnding[1].getWidth()*2381;
	  result+=lineEnding[1].getHeight()*23894013;
	}

	return result;
      }

      default:
      {
	assert(false); return result;
      }
    }
  }

  private int hashText(String text)
  {
    int result=4782031;

    for(int i=0; i!=text.length(); i++)
    {
      result*=text.charAt(i);
      result+=text.charAt(i);
    }

    return result;
  }

  /**
   * <p>Return a String representation of the Property.</p>
   */

  public String toString()
  {
    switch(type)
    {
      case SHORTCUTS:
      {
	StringBuffer result=new StringBuffer("Property:SHORTCUTS:");

	Shortcut[] shortcutValue=(Shortcut[])value;

	if(shortcutValue!=null)
	{
	  result.append(shortcutValue.length);
	  
	  for(int i=0; i!=shortcutValue.length; i++)
	  {
	    result.append(":").append(shortcutValue[i].toString());
	  }
	}

	return new String(result);
      }

      case IS_LOCKED:
      {
	return "Property:IS_LOCKED:"+value;
      }

      case IS_GROUP_NORMAL:
      {
	return "Property:IS_GROUP_NORMAL:"+value;
      }

      case TEXT:
      {
	return "Property:TEXT:"+value;
      }

      case BRUSH:
      {
	return "Property:BRUSH:"+value;
      }

      case PEN:
      {
	return "Property:PEN:"+value;
      }

      case FONT:
      {
	return "Property:FONT:"+value;
      }

      case IS_OPEN:
      {
	return "Property:IS_OPEN:"+value;
      }

      case IS_STRAIGHT_LINES:
      {
	return "Property:IS_STRAIGHT_LINES:"+value;
      }

      case LINE_ENDINGS:
      {
	return "Property:LINE_ENDINGS:"+((LineEnding[])value)[0]
	  +((LineEnding[])value)[1];
      }
      
      default:
      {
	return "Property:UNKNOWN";
      }
    }
  }
}
