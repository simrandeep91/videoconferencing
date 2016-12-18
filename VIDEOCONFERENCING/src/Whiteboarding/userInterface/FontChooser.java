
package Whiteboarding.userInterface;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class FontChooser extends JDialog
{

   JCheckBox _boldCheck;

   OpenList _fontNameList;
   JCheckBox _italicCheck;
   int _option = JOptionPane.CLOSED_OPTION;
   JLabel _preview;

   public FontChooser( JFrame parent, String fontNames[], String fontSizes[] )
   {
      super( parent, "Font", true );

      getContentPane().setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

      JPanel p = new JPanel( new GridLayout( 1, 2, 10, 2 ) );
      p.setBorder( new TitledBorder( new EtchedBorder(), "Font" ) );

      _fontNameList = new OpenList( fontNames , "");
      p.add( _fontNameList );

      getContentPane().add( p );

      p = new JPanel( new GridLayout( 1, 2, 10, 5 ) );
      p.setBorder( new TitledBorder( new EtchedBorder(), "Style" ) );

      _boldCheck = new JCheckBox( "Bold" );
      p.add( _boldCheck );

      _italicCheck = new JCheckBox( "Italic" );
      p.add( _italicCheck );

      getContentPane().add( p );

      getContentPane().add( Box.createVerticalStrut( 5 ) );
      p = new JPanel();
      p.setLayout( new BoxLayout( p, BoxLayout.X_AXIS ) );
      p.add( Box.createHorizontalStrut( 10 ) );
      p.add( Box.createHorizontalStrut( 20 ) );

      p.add( Box.createHorizontalStrut( 10 ) );
      getContentPane().add( p );

      p = new JPanel( new BorderLayout() );
      p.setBorder( new TitledBorder( new EtchedBorder(), "Preview" ) );
      _preview = new JLabel( "The quick brown fox jumps over the lazy dog.", JLabel.CENTER );
      _preview.setBackground( Color.WHITE );
      _preview.setForeground( Color.BLACK );
      _preview.setOpaque( true );
      _preview.setBorder( new LineBorder( Color.BLACK ) );
      _preview.setPreferredSize( new Dimension( 320, 40 ) );
      p.add( _preview, BorderLayout.CENTER );
      getContentPane().add( p );

      JButton okButton = new JButton( "Ok" );
      okButton.addActionListener(
         new ActionListener()
         {
            public void actionPerformed( ActionEvent ev )
            {
               _option = JOptionPane.OK_OPTION;
               setVisible( false );
            }
         } );

      JButton cancelButton = new JButton( "Cancel" );
      cancelButton.addActionListener(
         new ActionListener()
         {
            public void actionPerformed( ActionEvent ev )
            {
               _option = JOptionPane.CANCEL_OPTION;
               setVisible( false );
            }
         } );

      p = new JPanel( new FlowLayout() );
      JPanel p1 = new JPanel( new GridLayout( 1, 2, 10, 2 ) );
      p1.add( okButton );
      p1.add( cancelButton );
      p.add( p1 );
      getContentPane().add( p );

      pack();
      setResizable( false );

      ListSelectionListener listSelListener =
         new ListSelectionListener()
         {
            public void valueChanged( ListSelectionEvent ev )
            {
               updatePreview();
            }
         };

      _fontNameList.addListSelectionListener( listSelListener );

      ActionListener actionListener =
         new ActionListener()
         {
            public void actionPerformed( ActionEvent ev )
            {
               updatePreview();
            }
         };

      _boldCheck.addActionListener( actionListener );
      _italicCheck.addActionListener( actionListener );
   }

   public void setFont(Font font)
   {
      String fontName = font.getFamily();
      _fontNameList.setSelected(fontName);

      _boldCheck.setSelected(font.isBold());
      _italicCheck.setSelected(font.isItalic());

      updatePreview();
   }

   public Font getFont()
   {
      return new Font(_fontNameList.getSelected(),
		      (_boldCheck.isSelected()?Font.BOLD:0)|
		      (_italicCheck.isSelected()?Font.ITALIC:0), 12);
   }

   public int getOption()
   {
      return _option;
   }

   private void updatePreview()
   {
      String fontName = _fontNameList.getSelected();

      int fontStyle = Font.PLAIN;
      if( _boldCheck.isSelected() )
         fontStyle |= Font.BOLD;
      if( _italicCheck.isSelected() )
         fontStyle |= Font.ITALIC;

      Font font = new Font( fontName, fontStyle, 12);
      _preview.setFont( font );

      _preview.repaint();
   }

}
