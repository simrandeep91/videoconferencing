/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import videoconferencing.alpha.UserMasterBean;

/**
 *
 * @author deepika
 */
public class AbstractTableStructure extends AbstractTableModel {
    private String[] header=null;
    private Object[][] data=null;
    public AbstractTableStructure(String[] header,ArrayList al,String s)
    {
        this.header=header;
        if(s.equals("1"))
        addContent(al);
        else
        addTableContent(al);
    }
    public final void addTableContent(ArrayList al)
    {
        data=new Object[al.size()][1];
        for(int i=0;i<al.size();i++)
        {
            UserMasterBean objBean=(UserMasterBean)al.get(i);
            data[i][0]=objBean.getUsername();
        }
    }
    public final void addContent(ArrayList al)
    {
        data=new Object[al.size()][1];
        for(int i=0;i<al.size();i++)
        {
            data[i][0]=al.get(i);
        }
    }
    public int getColumnCount() {
            return header.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return header[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        
        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
                return false;
            
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
}
