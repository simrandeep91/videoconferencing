
package Whiteboarding.userInterface;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  OpenList combines a label, text field and a list.
 */

public class OpenList extends JPanel
       implements ListSelectionListener, ActionListener
{
   JList _list;
   JScrollPane _scroll;
   JTextField _text;
   JLabel _title;

   public OpenList( String data[], String title )
   {
      setLayout( null );

      _title = new JLabel( title, JLabel.LEFT );
      add( _title );

      _text = new JTextField();
      _text.addActionListener( this );
      add( _text );

      _list = new JList( data );
      _list.setVisibleRowCount( 4 );
      _list.addListSelectionListener( this );

      _scroll = new JScrollPane( _list );
      add( _scroll );
   }

   public void setSelected( String value )
   {
      _list.setSelectedValue( value, true );
      _text.setText( value );
   }

   public void setSelectedInt( int value )
   {
      setSelected( "" + value );
   }

   public Dimension getMaximumSize()
   {
      Insets ins = getInsets();
      Dimension d1 = _title.getMaximumSize();
      Dimension d2 = _text.getMaximumSize();
      Dimension d3 = _scroll.getMaximumSize();

      int w = Math.max( Math.max( d1.width, d2.width ), d3.width );
      int h = d1.height + d2.height + d3.height;

      return new Dimension( w + ins.left + ins.right,
            h + ins.top + ins.bottom );
   }

   public Dimension getMinimumSize()
   {
      Insets ins = getInsets();
      Dimension d1 = _title.getMinimumSize();
      Dimension d2 = _text.getMinimumSize();
      Dimension d3 = _scroll.getMinimumSize();

      int w = Math.max( Math.max( d1.width, d2.width ), d3.width );
      int h = d1.height + d2.height + d3.height;

      return new Dimension( w + ins.left + ins.right,
            h + ins.top + ins.bottom );
   }

   public Dimension getPreferredSize()
   {
      Insets ins = getInsets();
      Dimension d1 = _title.getPreferredSize();
      Dimension d2 = _text.getPreferredSize();
      Dimension d3 = _scroll.getPreferredSize();

      int w = Math.max( Math.max( d1.width, d2.width ), d3.width );
      int h = d1.height + d2.height + d3.height;

      return new Dimension( w + ins.left + ins.right,
            h + ins.top + ins.bottom );
   }

   public String getSelected()
   {
      return _text.getText();
   }

   public int getSelectedInt()
   {
      try
      {
         return Integer.parseInt( getSelected() );
      }
      catch( NumberFormatException nex )
      {
         return -1;
      }
   }

   public void actionPerformed( ActionEvent ev )
   {
      // sync listbox with textbox

      ListModel model = _list.getModel();
      String key = _text.getText().toLowerCase();
      for( int i = 0; i < model.getSize(); i++ )
      {
         String data = ( String ) model.getElementAt( i );
         if( data.toLowerCase().startsWith( key ) )
         {
            _list.setSelectedValue( data, true );
            break;
         }
      }
   }

   public void addListSelectionListener( ListSelectionListener l )
   {
      _list.addListSelectionListener( l );
   }

   public void doLayout()
   {
      Insets ins = getInsets();
      Dimension d = getSize();
      int x = ins.left;
      int y = ins.top;
      int w = d.width - ins.left - ins.right;
      int h = d.height - ins.top - ins.bottom;

      Dimension d1 = _title.getPreferredSize();
      _title.setBounds( x, y, w, d1.height );
      y += d1.height;

      Dimension d2 = _text.getPreferredSize();
      _text.setBounds( x, y, w, d2.height );
      y += d2.height;

      _scroll.setBounds( x, y, w, h - y );
   }

   public void valueChanged( ListSelectionEvent ev )
   {
      // sync textbox with listbox
      Object obj = _list.getSelectedValue();
      if( obj != null )
         _text.setText( obj.toString() );
   }
}
