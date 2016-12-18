
package Whiteboarding.userInterface;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.*;

public class StatusBar extends JPanel
{
  private JLabel messageLabel;

  public StatusBar()
  {
    super(false);

    final JPanel messagePanel=new JPanel(false);
    messageLabel=new JLabel("", JLabel.LEFT);
    messagePanel.setLayout(new GridLayout(1, 1));
    messagePanel.add(messageLabel);
    messagePanel.setBorder(BorderFactory.createEtchedBorder());
  
    add(messagePanel);

    setSize(640, 20);

    setLayout(new LayoutManagerAdapter(160, 18, 640, 18)
      {
	public void layoutContainer(Container parent)
	{
	  messagePanel.setBounds(1, 1, getWidth(), 18);
	};
      });
   }

  public void setMessage(String message)
  {
    messageLabel.setText(message);
  }

  public String getMessage()
  {
    return messageLabel.getText();
  }
 }
