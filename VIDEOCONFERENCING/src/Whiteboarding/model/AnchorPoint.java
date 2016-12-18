package Whiteboarding.model;

import java.awt.Point;

import java.io.Serializable;

/**
 * <p>AnchorPoint extends {@link Point} to provide addition information
 * about what the point is anchored to.</p>
 * <p>It provides no additional services other than the storage of an
 * {@link EntityID}.</p>
 */

public class AnchorPoint extends Point implements Serializable
{
  static final long serialVersionUID = 3283341539181985751L;

  // 2. Instance variable declarations

  /**
   * <p>{@link EntityID} pointing to the Drawable to which the point is
   * anchored.</p>
   */

  public EntityID target;

  // 7. Instance constructor declarations

  /**
   * <p>Create a new AnchorPoint with the specified x, y and target.</p>
   */

  public AnchorPoint(int x, int y, EntityID target)
  {
    super(x, y);
    this.target=target;
  }

  /**
   * <p>Create a new AnchorPoint with the same position as an existing Point
   * and the specified x, y, and target.</p>
   */

  public AnchorPoint(Point point, EntityID target)
  {
    super(point);
    this.target=target;
  }
}
