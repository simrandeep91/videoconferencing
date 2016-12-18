package Whiteboarding.userInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A modal dialog that asks the user for a user name and password.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/PasswordDialog.html">ostermiller.org</a>.
 *
 * <code>
 * <pre>
 * PasswordDialog p = new PasswordDialog(null, "Test");
 * if(p.showDialog()){
 *     System.out.println("Name: " + p.getName());
 *     System.out.println("Pass: " + p.getPass());
 * } else {
 *     System.out.println("User selected cancel");
 * }
 * </pre>
 * </code>
 */
public class PasswordDialog extends JDialog {

    /**
     * Where the password is typed.
     */
    protected JPasswordField pass;
    /**
     * The OK button.
     */
    protected JButton okButton;
    /**
     * The cancel button.
     */
    protected JButton cancelButton;
    /**
     * The label for the field in which the password is typed.
     */
    protected JLabel passLabel;

    /**
     * Set the password that appears as the default
     * An empty string will be used if this in not specified
     * before the dialog is displayed.
     *
     * @param pass default password to be displayed.
     */
    public void setPass(String pass){
        this.pass.setText(pass);
    }

    /**
     * Set the label on the OK button.
     * The default is a localized string.
     *
     * @param ok label for the ok button.
     */
    public void setOKText(String ok){
        this.okButton.setText(ok);
        pack();
    }

    /**
     * Set the label on the cancel button.
     * The default is a localized string.
     *
     * @param cancel label for the cancel button.
     */
    public void setCancelText(String cancel){
        this.cancelButton.setText(cancel);
        pack();
    }

    /**
     * Set the label for the field in which the password is entered.
     * The default is a localized string.
     *
     * @param pass label for the password field.
     */
    public void setPassLabel(String pass){
        this.passLabel.setText(pass);
        pack();
    }

    /**
     * Get the password that was entered into the dialog before
     * the dialog was closed.
     *
     * @return the password from the password field.
     */
    public String getPass(){
        return new String(pass.getPassword());
    }

    /**
     * Finds out if user used the OK button or an equivalent action
     * to close the dialog.
     * Pressing enter in the password field may be the same as
     * 'OK' but closing the dialog and pressing the cancel button
     * are not.
     *
     * @return true if the the user hit OK, false if the user canceled.
     */
    public boolean okPressed(){
        return pressed_OK;
    }

    /**
     * update this variable when the user makes an action
     */
    private boolean pressed_OK = false;

    /**
     * Create this dialog with the given parent and title.
     *
     * @param parent window from which this dialog is launched
     * @param title the title for the dialog box window
     */
    public PasswordDialog(Frame parent, String title) {

        super(parent, title, true);

        if (title==null){
            setTitle("Enter Password");
        }
        if (parent != null){
            setLocationRelativeTo(parent);
        }
        // super calls dialogInit, so we don't need to do it again.
    }

    /**
     * Create this dialog with the given parent and the default title.
     *
     * @param parent window from which this dialog is launched
     */
    public PasswordDialog(Frame parent) {
        this(parent, null);
    }

    /**
     * Create this dialog with the default title.
     */
    public PasswordDialog() {
        this(null, null);
    }

    /**
     * Called by constructors to initialize the dialog.
     */
    protected void dialogInit(){

        pass = new JPasswordField("", 20);
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        passLabel = new JLabel("Password: ");

        super.dialogInit();

        KeyListener keyListener = (new KeyAdapter() {
            public void keyPressed(KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                        (e.getSource() == cancelButton
                        && e.getKeyCode() == KeyEvent.VK_ENTER)){
                    pressed_OK = false;
                    PasswordDialog.this.hide();
                }
                if (e.getSource() == okButton &&
                        e.getKeyCode() == KeyEvent.VK_ENTER){
                    pressed_OK = true;
                    PasswordDialog.this.hide();
                }
            }
        });
        addKeyListener(keyListener);

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e){
	      Object source = e.getSource();
	      pressed_OK = (source == pass || source == okButton);
	      PasswordDialog.this.hide();
            }
        };

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets.top = 5;
        c.insets.bottom = 5;
        JPanel pane = new JPanel(gridbag);
        pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        JLabel label;

        c.anchor = GridBagConstraints.EAST;

        c.gridy = 1;
        gridbag.setConstraints(passLabel, c);
        pane.add(passLabel);

        gridbag.setConstraints(pass, c);
        pass.addActionListener(actionListener);
        pass.addKeyListener(keyListener);
        pane.add(pass);

        c.gridy = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.CENTER;
        JPanel panel = new JPanel();
        okButton.addActionListener(actionListener);
        okButton.addKeyListener(keyListener);
        panel.add(okButton);
        cancelButton.addActionListener(actionListener);
        cancelButton.addKeyListener(keyListener);
        panel.add(cancelButton);
        gridbag.setConstraints(panel, c);
        pane.add(panel);

        getContentPane().add(pane);

        pack();
    }

    /**
     * Shows the dialog and returns true if the user pressed ok.
     *
     * @return true if the the user hit OK, false if the user canceled.
     */
    public boolean showDialog(){
        show();
        return okPressed();
    }

    /**
     * A simple example to show how this might be used.
     * If there are arguments passed to this program, the first
     * is treated as the default name, the second as the default password
     *
     * @param args command line arguments: name and password (optional)
     */
    private static void main(String[] args){
        PasswordDialog p = new PasswordDialog();
        if(args.length > 0){
            p.setName(args[0]);
        }
        if(args.length > 1){
            p.setPass(args[1]);
        }
        if(p.showDialog()){
            System.out.println("Name: " + p.getName());
            System.out.println("Pass: " + p.getPass());
        } else {
            System.out.println("User selected cancel");
        }
        p.dispose();
        p = null;
        System.exit(0);
    }
}
