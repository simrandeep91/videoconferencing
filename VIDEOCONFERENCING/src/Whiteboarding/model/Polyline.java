package Whiteboarding.model;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>Class representing the {@link Polyline} {@link Entity}. It extends
 * {@link Shape} to provide a Shape that is an arbitrary polyline or
 * polygon.</p>
 * <p>A newly created Polyline has no points and is at position (0,0).</p>
 * @author David Morgan
 */
public class Polyline extends Shape implements Serializable {
    // 1. Static variable field declarations

    static final long serialVersionUID = 2526823627547702474L;
    /**
     * <p>A boolean specifying whether resize by bounding rectangle uses
     * the actual bounding rectangle or the most distant control point.</p>
     *
     * <p>Overriden here to specify use of the actual bounding rectangle.</p>
     */
    protected static boolean RESIZE_USES_BOUNDING_RECTANGLE = true;
    // 2. Instance variable field declarations
    /**
     * <p>Boolean indicating whether the polyline is open or closed, i.e.
     * whether it is a polyline or an irregular polygon.</p>
     */
    protected boolean closed;
    /**
     * <p>Boolean indicating whether the polyline consists of straight lines
     * or curved lines.</p>
     */
    protected boolean curved;
    /**
     * <p>Pair of {@link LineEnding} objects deciding how the lines end.</p>
     */
    protected LineEnding[] lineEnding;

    // 4. Static inner method declarations
    /**
     * <p>Subclass of Shape.Change providing extra operations on Polylines,
     * and implementing existing operations differently where necessary.</p>
     */
    public static class Change extends Shape.Change {

        static final long serialVersionUID = -641793972605585810L;
        // 4.1 Static variable field declarations
        /**
         * <p>Indicates an operation that moves a Control Point.</p>
         *
         *	<p>First parameter is an Integer specifying the index of the Control	
         * Point moved. Second parameter is a Point specifying the translation
         * applied to it.</p>
         *
         * <p>Only one target Drawable is allowed.</p>
         *
         * <p>Special case: an index of -1 indicates the center point.</p>
         */
        public static final int MOVE_POINT = 21;
        /**
         * <p>Indicates an operation that deletes a Control Point.</p>
         *
         *	<p>First and only parameter is an Integer specifying the index of the
         * Control Point to be deleted.</p>
         *
         * <p>Only one target Drawable is allowed.</p>
         */
        public static final int DELETE_POINT = 22;
        /**
         * <p>Indicates an operation that adds a Control Point.</p>
         *
         *	<p>First parameter is an Integer specifying the index of the Control
         * Point before which the new Control Point is added. Second parameter is
         * a Point specifying the position of the new Control Point.</p>
         *
         * <p>Only one target Drawable is allowed.</p>
         */
        public static final int ADD_POINT = 23;
        /**
         * <p>Indicates an operation that anchors a Control Point.</p>
         *
         *	<p>First and only parameter is an Integer specifying the index of the
         * Control Point to be anchored.</p>
         *
         * <p>First target specifies the Drawable containing the point to be
         * anchored. Second target specifies the Drawable to which it is
         * anchored.</p>
         */
        public static final int ANCHOR_POINT = 24;
        /**
         * <p>Indicates an operation that detaches an anchored Control Point.</p>
         *
         * <p>First and only parameter is an Integer specifying the index of the
         * Control point to be detached.</p>
         *
         * <p>Only one target Drawable is allowed.</p>
         */
        public static final int DETACH_POINT = 25;

        // 4.7 Instance constructor declarations
        /**
         * <p>Protected empty constructor.</p>
         */
        protected Change() {
        }

        /**
         * <p>Change modifies Shape in a specified way.</p>
         */
        public Change(int changeType, Object[] parameter, EntityID[] target) {
            initialize(changeType, parameter, target);
        }

        // 4.9 Instance method declarations
        protected void initialize(int changeType, Object[] parameter,
                EntityID[] target) {
            // Determine whether this change is handled here or by Shape.Change

            if (isParent(changeType)) {
                super.initialize(changeType, parameter, target);
            } else {
                this.changeType = (byte) changeType;
                this.parameter = parameter;
                this.target = target;

                assert (isDataValid());
            }
        }

        /**
         * <p>Determine whether a particular changeType should be handed by
         * the parent class.</p>
         */
        private boolean isParent(int changeType) {
            switch (changeType) {
                case CREATE:
                case MOVE_POINT:
                case DELETE_POINT:
                case ADD_POINT:
                case ANCHOR_POINT:
                case DETACH_POINT: {
                    return false;
                }

                default: {
                    return true;
                }
            }
        }

        /**
         * <p>Validate the main data.</p>
         */
        protected boolean isDataValid() {
            switch (changeType) {
                case CREATE: {
                    return (parameter == null) && (target != null) && (target.length == 1)
                            && (target[0] != null);
                }

                case MOVE_POINT: {
                    return (parameter != null) && (parameter.length == 2)
                            && (parameter[0] instanceof Integer) && (parameter[1] instanceof Point)
                            && (target != null) && (target.length == 1) && (target[0] != null);
                }

                case DELETE_POINT: {
                    return (parameter != null) && (parameter.length == 1)
                            && (parameter[0] instanceof Integer)
                            && (target != null) && (target.length == 1) && (target[0] != null);
                }

                case ADD_POINT: {
                    return (parameter != null) && (parameter.length == 2)
                            && (parameter[0] instanceof Integer) && (parameter[1] instanceof Point)
                            && (target != null) && (target.length == 1) && (target[0] != null);
                }

                case ANCHOR_POINT: {
                    return (parameter != null) && (parameter.length == 1)
                            && (parameter[0] instanceof Integer) && (target != null)
                            && (target.length == 2) && (target[0] != null) && (target[1] != null);
                }

                case DETACH_POINT: {
                    return (parameter != null) && (parameter.length == 1)
                            && (parameter[0] instanceof Integer) && (target != null)
                            && (target.length == 1) && (target[0] != null);
                }

                default: {
                    return super.isDataValid();
                }
            }
        }

        /**
         * <p>Validate the undo data.</p>
         */
        protected boolean isUndoDataValid() {
            switch (changeType) {
                case CREATE: {
                    return (undoData == null);
                }

                case MOVE_POINT: {
                    return (undoData == null);
                }

                case DELETE_POINT: {
                    return (undoData != null) && (undoData.length == 1)
                            && (undoData[0] instanceof Point);
                }

                case ADD_POINT: {
                    return (undoData == null);
                }

                case ANCHOR_POINT: {
                    return (undoData == null);
                }

                case DETACH_POINT: {
                    return (undoData != null) && (undoData.length == 1)
                            && (undoData[0] instanceof EntityID);
                }

                default: {
                    return super.isDataValid();
                }
            }
        }

        /**
         * <p>Apply the change. This must be overriden by subclasses of
         * Polyline.Change that provide new functionality.</p>
         */
        public void apply(SessionState state) {
            // Determine whether this change is handled here or by Shape.Change

            if (isParent(changeType)) {
                super.apply(state);
                return;
            }

            // Preconditions; supplied state is not null

            assert (state != null);

            // Method proper

            switch (changeType) {
                case CREATE: {
                    // Gather data for undo

                    // Make the change

                    Vector controlPoints = new Vector();

                    Polyline polyline = new Polyline(target[0], new Point(0, 0),
                            controlPoints, new Vector(), false,
                            null, null, null, false, false,
                            new LineEnding[2]);

                    state.addEntity(polyline);
                    break;
                }

                case MOVE_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    // Gather data for undo

                    // Make the change

                    int index = ((Integer) parameter[0]).intValue();

                    Point point = (index == -1) ? polyline.center
                            : (Point) polyline.controlPoints.get(index);

                    Point translation = (Point) parameter[1];
                    point.translate(translation.x, translation.y);

                    break;
                }

                case DELETE_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    // Gather data for undo

                    int index = ((Integer) parameter[0]).intValue();

                    undoData = new Object[]{polyline.controlPoints.get(index)};

                    // Make the change

                    polyline.controlPoints.remove(index);

                    break;
                }

                case ADD_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    // Gather data for undo

                    // Make the change

                    polyline.controlPoints.add(((Integer) parameter[0]).intValue(),
                            new Point((Point) parameter[1]));
                    break;
                }

                case ANCHOR_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    // Gather data for undo

                    // Make the change

                    int index = ((Integer) parameter[0]).intValue();

                    polyline.detachPoint(state, index);
                    polyline.anchorPoint(state, target[1], index);

                    break;
                }

                case DETACH_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);
                    int index = ((Integer) parameter[0]).intValue();

                    // Gather data for undo

                    Point point = (Point) polyline.controlPoints.get(index);
                    if (point instanceof AnchorPoint) {
                        undoData = new Object[]{((AnchorPoint) point).target};
                    }

                    // Make the change

                    polyline.detachPoint(state, index);

                    break;
                }

                default: {
                    assert (false);
                }
            }

            // Postconditions; undo data is valid

            assert (isUndoDataValid());
        }

        /**
         * <p>Undo the change. This must be overriden by subclasses of
         * Polyline.Change that provide new functionality.</p>
         */
        public void undo(SessionState state) {
            // Determine whether this change is handled here or by Shape.Change

            if (isParent(changeType)) {
                super.undo(state);
                return;
            }

            // Preconditions; supplied state not null, undo data valid

            assert (state != null);
            assert (isUndoDataValid());

            // Method proper

            switch (changeType) {
                case CREATE: {
                    state.removeEntity(target[0]);
                    break;
                }

                case MOVE_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    int index = ((Integer) parameter[0]).intValue();

                    Point point = (index == -1) ? polyline.center
                            : (Point) polyline.controlPoints.get(index);

                    Point translation = (Point) parameter[1];
                    point.translate(-translation.x, -translation.y);

                    break;
                }

                case DELETE_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    int index = ((Integer) parameter[0]).intValue();

                    polyline.controlPoints.add(index, new Point((Point) undoData[0]));

                    break;
                }

                case ADD_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    polyline.controlPoints.remove(((Integer) parameter[0]).intValue());

                    break;
                }

                case ANCHOR_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    int index = ((Integer) parameter[0]).intValue();

                    polyline.detachPoint(state, index);

                    break;
                }

                case DETACH_POINT: {
                    Polyline polyline = (Polyline) state.getEntity(target[0]);

                    int index = ((Integer) parameter[0]).intValue();

                    polyline.anchorPoint(state, (EntityID) undoData[0], index);

                    break;
                }

                default: {
                    assert (false);
                }
            }
        }

        /**
         * <p>Obtain a new Change representing the reverse of the operation. Intended
         * to be used during undo, so an undo operation is still a new operation.</p>
         */
        public Entity.Change reverse() {
            // Determine whether this change is handled here or by Entity.Change

            if (isParent(changeType)) {
                return super.reverse();
            }

            // Preconditions

            assert (isUndoDataValid());

            // Method proper

            switch (changeType) {
                case CREATE: {
                    return new Entity.Change(DELETE, undoData, target);
                }

                case MOVE_POINT: {
                    return new Polyline.Change(MOVE_POINT, new Object[]{parameter[0],
                                negatePoint((Point) parameter[1])},
                            target);
                }

                case DELETE_POINT: {
                    return new Polyline.Change(ADD_POINT, new Object[]{parameter[0],
                                undoData[0]},
                            target);
                }

                case ADD_POINT: {
                    return new Polyline.Change(DELETE_POINT, new Object[]{parameter[0]},
                            target);
                }

                case ANCHOR_POINT: {
                    return new Polyline.Change(DETACH_POINT, parameter,
                            new EntityID[]{target[0]});
                }

                case DETACH_POINT: {
                    return new Polyline.Change(ANCHOR_POINT, parameter,
                            new EntityID[]{target[0], (EntityID) undoData[0]});
                }

                default: {
                    assert (false);
                    return null;
                }
            }
        }

        /**
         * <p>Obtain a String representation of the Change.</p>
         * @return a String describing the effect of applying this Change
         */
        public String toString() {
            // Determine whether this change is handled here or by Entity.Change

            if (isParent(changeType)) {
                return "Polyline.Change:" + super.toString();
            }

            switch (changeType) {
                case CREATE: {
                    return "Polyline.Change:CREATE:" + target[0];
                }

                case MOVE_POINT: {
                    return "Polyline.Change:MOVE_POINT:" + target[0] + ":"
                            + parameter[0] + ":" + parameter[1];
                }

                case DELETE_POINT: {
                    return "Polyline.Change:DELETE_POINT:" + target[0] + ":"
                            + parameter[0];
                }

                case ADD_POINT: {
                    return "Polyline.Change:ADD_POINT:" + target[0] + ":"
                            + parameter[0] + ":" + parameter[1];
                }

                case ANCHOR_POINT: {
                    return "Polyline.Change:ANCHOR_POINT:" + target[0] + ":" + target[1] + ":"
                            + parameter[0];
                }

                case DETACH_POINT: {
                    return "Polyline.Change:DETACH_POINT:" + target[0] + ":" + ":" + parameter[0];
                }

                default: {
                    return "Polyline.Change:UNKNOWN";
                }
            }
        }
    }

    // 7. Instance constructor declarations
    /**
     * <p>Create a new Polyline with the specified identifier, center, Vector
     * of control points, boolean indicating whether it is locked, {@link Brush},
     * {@link Pen}, boolean indicating whether it is closed, boolean
     * indicating whether it is curved, and {@link LineEnding} pair.</p>
     */
    protected Polyline(EntityID identifier, Point center, Vector controlPoints,
            Vector controlPointsAnchored, boolean locked,
            Shortcut[] shortcut, Brush brush, Pen pen, boolean closed,
            boolean curved, LineEnding[] lineEnding) {
        super(identifier, center, controlPoints, controlPointsAnchored,
                locked, shortcut, brush, pen);

        this.closed = closed;
        this.curved = curved;
        this.lineEnding = lineEnding;

        assert (isPolylineDataValid());
    }

    // 9. Instance method declarations
    /**
     * <p>Validate the instance data.</p>
     */
    protected boolean isPolylineDataValid() {
        if (!isShapeDataValid()) {
            return false;
        }

        return (lineEnding != null) && (lineEnding.length == 2);
    }

    /**
     * <p>Obtain the Bounding Rectangle of the Polyline, at the specified
     * zoom level, and without taking line widths into account.</p>
     */
    public java.awt.Rectangle getInnerBoundingRectangle(int zoomLevel) {
        java.awt.Rectangle withoutArrows = super.getInnerBoundingRectangle(zoomLevel);

        if (closed || (controlPoints.size() <= 1)) {
            return withoutArrows;
        }

        int left = withoutArrows.x;
        int right = withoutArrows.x + withoutArrows.width;

        int top = withoutArrows.y;
        int bottom = withoutArrows.y + withoutArrows.height;

        for (int i = 0; i != 2; i++) {
            if (lineEnding[i] != null) {
                Point tip = i == 0 ? (Point) controlPoints.get(0)
                        : (Point) controlPoints.get(controlPoints.size() - 1);

                Point previous = i == 0 ? (Point) controlPoints.get(1)
                        : (Point) controlPoints.get(controlPoints.size() - 2);

                Point[] arrowPoint = getArrowPoints(tip, previous, lineEnding[i],
                        zoomLevel);

                for (int j = 0; j != arrowPoint.length; j++) {
                    if (arrowPoint[j].x < left) {
                        left = arrowPoint[j].x;
                    }
                    if (arrowPoint[j].x > right) {
                        right = arrowPoint[j].x;
                    }

                    if (arrowPoint[j].y < top) {
                        top = arrowPoint[j].y;
                    }
                    if (arrowPoint[j].y > bottom) {
                        bottom = arrowPoint[j].y;
                    }
                }
            }
        }

        return new java.awt.Rectangle(left, top, right - left, bottom - top);
    }

    /**
     * <p>Override Shape.getPropeties to provide access to IS_OPEN,
     * IS_STRAIGHT_LINES and LINE_ENDINGS {@link Property} types.
     * data.</p>
     * @return a Vector of Property objects describing the Shape
     */
    public Vector getProperties() {
        Vector result = super.getProperties();

        result.add(new Property(Property.IS_OPEN, new Boolean(!closed)));
        result.add(new Property(Property.IS_STRAIGHT_LINES, new Boolean(!curved)));
        result.add(new Property(Property.LINE_ENDINGS, lineEnding));

        return result;
    }

    /**
     * <p>Set a property of the Polygon. Subclasses must override this to
     * all setting of whatever properties they have.</p>
     * @param property the {@link Property} to set
     */
    protected void edit(Property property) {
        // Preconditions; property not null

        assert (property != null);

        // Method proper

        switch (property.getType()) {
            case Property.IS_OPEN: {
                closed = !((Boolean) property.getValue()).booleanValue();
                break;
            }

            case Property.IS_STRAIGHT_LINES: {
                curved = !((Boolean) property.getValue()).booleanValue();
                break;
            }

            case Property.LINE_ENDINGS: {
                lineEnding = (LineEnding[]) property.getValue();
                break;
            }

            default: {
                super.edit(property);
            }
        }

        // Postconditions; data is valid

        assert (isPolylineDataValid());
    }

    /**
     * <p>In order to render an Entity, supply it with the graphics context to
     * render on, and a Polyline that details which part of the org.davidmorgan.jinn.is
     * visible. </p>
     *
     * <p>Subclasses will override render to provide their own rendering
     * routines.</p>
     *
     * @return a boolean indicating whether any drawing was done
     */
    public boolean render(SessionState state, Graphics2D g,
            java.awt.Rectangle view) {
        // Preconditions; state, g and view not null

        assert ((state != null) && (g != null) && (view != null));

        // Method proper

        if (view.intersects(getBoundingRectangle())) {
            java.awt.Rectangle bounds = g.getClipBounds();
            int zoomLevel = view.width / bounds.width;
            g.translate(-view.x / zoomLevel + bounds.x, -view.y / zoomLevel + bounds.y);

            GeneralPath path = new GeneralPath();

            if (!curved) {
                for (int i = 0; i < controlPoints.size(); i++) {
                    Point point = (Point) controlPoints.get(i);
                    float x = ((float) point.x) / zoomLevel;
                    float y = ((float) point.y) / zoomLevel;

                    if (i == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
            } else {
                for (int i = 0; i != controlPoints.size() / 2; i++) {
                    Point[] point = new Point[3];

                    if (i == 0) {
                        point[0] = safeControlPointGet(controlPoints, 0);
                        point[1] = safeControlPointGet(controlPoints, 2);
                        point[2] = safeControlPointGet(controlPoints, 1);
                    } else {
                        point[0] = safeControlPointGet(controlPoints, i * 2 - 1);
                        point[1] = safeControlPointGet(controlPoints, i * 2 + 2);
                        point[2] = safeControlPointGet(controlPoints, i * 2 + 1);
                    }

                    float[] x = new float[3];
                    float[] y = new float[3];

                    for (int j = 0; j != 3; j++) {
                        x[j] = ((float) point[j].x) / zoomLevel;
                        y[j] = ((float) point[j].y) / zoomLevel;
                    }

                    if (i == 0) {
                        path.moveTo(x[0], y[0]);
                    }
                    path.curveTo(x[0], y[0], x[1], y[1], x[2], y[2]);
                }
            }

            if (closed) {
                path.closePath();
                configureGraphicsBrush(g, zoomLevel, brush);
                g.fill(path);
            }

            configureGraphicsPen(g, zoomLevel, pen);
            g.draw(path);

            if ((!closed) && (controlPoints.size() >= 2)) {
                for (int i = 0; i != 2; i++) {
                    if (lineEnding[i] != null) {
                        // Get data needed to determine arrow geometry

                        Point tip = i == 0 ? (Point) controlPoints.get(0)
                                : (Point) controlPoints.get(controlPoints.size() - 1);

                        Point previous = i == 0 ? (Point) controlPoints.get(1)
                                : (Point) controlPoints.get(controlPoints.size() - 2);

                        Point[] arrowPoint = getArrowPoints(tip, previous, lineEnding[i],
                                zoomLevel);

                        // Create arrow GeneralPath

                        GeneralPath arrow = new GeneralPath();

                        arrow.moveTo(arrowPoint[0].x / zoomLevel,
                                arrowPoint[0].y / zoomLevel);

                        arrow.lineTo(arrowPoint[1].x / zoomLevel,
                                arrowPoint[1].y / zoomLevel);

                        arrow.lineTo(arrowPoint[2].x / zoomLevel,
                                arrowPoint[2].y / zoomLevel);

                        arrow.closePath();

                        // Obtain arrow Brush and Pen

                        Brush brush = lineEnding[i].getBrush();
                        if (brush == null) {
                            brush = this.brush;
                        }

                        Pen pen = lineEnding[i].getPen();
                        if (pen == null) {
                            pen = this.pen;
                        }

                        configureGraphicsPen(g, zoomLevel, pen);
                        g.draw(arrow);

                        configureGraphicsBrush(g, zoomLevel, brush);
                        g.fill(arrow);
                    }
                }
            }

            g.setTransform(new AffineTransform());
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>Return a control point from a Vector of control points with the
     * given index. If the index is negative or too high, the nearest control
     * point is returned.</p>
     */
    private Point safeControlPointGet(Vector controlPoints, int index) {
        if (index < 0) {
            index = 0;
        }
        if (index >= controlPoints.size()) {
            index = controlPoints.size() - 1;
        }

        return (Point) controlPoints.get(index);
    }

    /**
     * <p>Return an array of three points, with the second point being the
     * tip, representing an arrow. The arrow ends at the Point indicating
     * the tip, and follows the direction from the previous Point.</p>
     */
    private Point[] getArrowPoints(Point tip, Point previous,
            LineEnding lineEnding,
            int zoomLevel) {
        Point[] result = new Point[3];

        double arrowDirX = tip.x - previous.x;
        double arrowDirY = tip.y - previous.y;

        arrowDirX /= tip.distance(previous);
        arrowDirY /= tip.distance(previous);

        int width = lineEnding.getWidth() * zoomLevel;
        int height = lineEnding.getHeight() * zoomLevel;

        return new Point[]{new Point((int) (tip.x - arrowDirX * height - arrowDirY * width),
                    (int) (tip.y - arrowDirY * height + arrowDirX * width)),
                    new Point(tip.x, tip.y),
                    new Point((int) (tip.x - arrowDirX * height + arrowDirY * width),
                    (int) (tip.y - arrowDirY * height - arrowDirX * width))};
    }

    /**
     * <p>Create a deep copy of the Polyline.</p>
     */
    public Object clone() {
        Polyline result = (Polyline) super.clone();

        return result;
    }

    /**
     * <p>Obtain a String representation of the Polyline.</p>
     */
    public String toString() {
        return "Polyline:(" + super.toString() + "):"
                + (closed ? "closed" : "open") + ":"
                + (curved ? "curved" : "straight") + ":"
                + lineEnding[0] + ":"
                + lineEnding[1];
    }
}
