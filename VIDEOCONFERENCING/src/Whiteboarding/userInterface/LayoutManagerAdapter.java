package Whiteboarding.userInterface;

import java.awt.*;

public class LayoutManagerAdapter implements LayoutManager
{
  Dimension minimum;
  Dimension preferred;

  public LayoutManagerAdapter(int minimumWidth, int minimumHeight,
			      int preferredWidth, int preferredHeight)
  {
    minimum=new Dimension(minimumWidth, minimumHeight);
    preferred=new Dimension(preferredWidth, preferredHeight);
  }

  public void addLayoutComponent(String name, Component component) {}
  public Dimension minimumLayoutSize(Container parent) {return minimum;}
  public Dimension preferredLayoutSize(Container parent) {return preferred;}
  public void removeLayoutComponent(Component component) {}
  public void layoutContainer(Container parent) {}
}
