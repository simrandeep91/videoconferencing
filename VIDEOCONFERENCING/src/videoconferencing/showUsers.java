/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * showUsers.java
 *
 * Created on 13 Feb, 2012, 1:04:52 PM
 */
package videoconferencing;

import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CaptureDeviceInfo;
import javax.media.MediaLocator;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import videoconferencing.alpha.DBOperations;

/**
 *
 * @author Simran
 */
public class showUsers extends javax.swing.JPanel implements TableModelListener{
    
    public static String otherun=null;
    public static String status=null;
    public static String guestip=null;
    DBOperations db=new DBOperations();
    public static int guestvideoport,guesttextport,myvideoreceiveport;
    static int i=0;
    public CaptureDeviceInfo cdi=null;
    MediaLocator mlr=null;
    Vector v=null;
     private AbstractTableStructure tableobj=null;
    /** Creates new form showUsers */
    public showUsers() {
        initComponents();
        lblWelcome.setText("Welcome "+LoginFrame.un);
      }

    public void showMails(ArrayList al,String s)
    {
        status=s;
        if(al==null)
        {
            System.out.print("hi");
            DBOperations db=new DBOperations();
            al=db.getUsers(s);
        }
            if(al.isEmpty())
            {
                lblMessage.setText("There Are No Users Logged In This Time");
                usernameTable.setEnabled(false);
                btnConnect.setEnabled(false);                
            }String header[]={"Username"};
                /*Object data[][]=new Object[al.size()][3];
                for(int i=0;i<al.size();i++)
                {
                    MessageHeader objBean=(MessageHeader)al.get(i);
                    data[i][0]=objBean.getMessageFrom();
                    data[i][1]=objBean.getMessageSubject();
                    data[i][2]=objBean.getMessageSentDate();
                }
                InboxTable=new JTable(data,header);*/
                
                
                //creating new table according to user specified model
                tableobj=new AbstractTableStructure(header, al,"");
                usernameTable=new JTable(tableobj);
                usernameTable.getColumnModel().getColumn(0).setMaxWidth(220);
                jScrollPane1.setViewportView(usernameTable);
                usernameTable.getModel().addTableModelListener(usernameTable);
                
            

    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblMessage = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        usernameTable = new javax.swing.JTable();
        btnConnect = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        lblWelcome = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        lblMessage.setFont(new java.awt.Font("Tahoma", 0, 18));
        lblMessage.setText("                                                     Online Users");

        usernameTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        usernameTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "UserName"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        usernameTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        usernameTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        usernameTable.setMinimumSize(new java.awt.Dimension(20, 240));
        usernameTable.setNextFocusableComponent(btnConnect);
        usernameTable.setRowHeight(20);
        usernameTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usernameTable.getTableHeader().setReorderingAllowed(false);
        usernameTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usernameTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(usernameTable);
        usernameTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usernameTable.getColumnModel().getColumn(0).setResizable(false);

        btnConnect.setText("Connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        lblWelcome.setFont(new java.awt.Font("Tahoma", 0, 18));
        lblWelcome.setText("jLabel1");

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        jButton1.setText("Refresh");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 238, Short.MAX_VALUE)
                .addComponent(btnLogout)
                .addGap(41, 41, 41))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(151, Short.MAX_VALUE)
                .addComponent(lblMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(446, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnBack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnConnect))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(494, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(73, 73, 73))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnLogout)
                    .addComponent(lblWelcome))
                .addGap(35, 35, 35)
                .addComponent(lblMessage)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConnect)
                    .addComponent(btnBack))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(41, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        // TODO add your handling code here:
        otherun = (String) usernameTable.getValueAt(usernameTable.getSelectedRow(),usernameTable.getSelectedColumn());
        if(status.equals("t"))
        {
            guestip=db.getIP();
            guesttextport=db.gettextport();
            CreateWindow1 cw=new CreateWindow1();
            
            //MainFrame.container.removeAll();
            //cw.setBounds(60,100, 322, 268);
            //cw.setVisible(false);
            //this.add(cw);
            cw.setVisible(true);
        }
         if(status.equals("v"))
         {
             i=i+1;
             otherun = (String) usernameTable.getValueAt(usernameTable.getSelectedRow(),usernameTable.getSelectedColumn());
             guestip=db.getIP();
             guestvideoport=db.getGuestVideoReceivePort();
             myvideoreceiveport=db.getMyVideoReceivePort();
            try {
                new runBoth();
                /*String a[]={""};
                AVReceive22.main(a);*/
                /*String argv[]={"111.93.53.50/8000","111.93.53.50/8002"};
           if (argv.length == 0)
               AVReceive3.prUsage();

           AVReceive3 avReceive = new AVReceive3(argv);
           avReceive.start();
           System.out.println("sdad");
           if (!avReceive.initialize()) {
               System.err.println("Failed to initialize the sessions.");
               System.exit(-1);
           }
                if(i==1)
                {
                    String s[]={""};
                    Vector v=CaptureDeviceManager.getDeviceList(new AudioFormat(null));
                    CaptureDeviceInfo cdi=(CaptureDeviceInfo) v.firstElement();
                    MediaLocator mlr = cdi.getLocator();
                 
                   String args[]={mlr.toString(),"111.93.53.50","8000"};
                   if (args.length < 3) {
                    AVReceive3.prUsage();
           }
           Format fmt = null;
           int i = 0;

           // Create a audio transmit object with the specified params.
           AVTransmit3 at = new AVTransmit3(new MediaLocator(args[i]),
                                                args[i+1], args[i+2], fmt);
           // Start the transmission
           at.start();
                }*/
            } catch (InterruptedException ex) {
                Logger.getLogger(showUsers.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
    }//GEN-LAST:event_btnConnectActionPerformed
    
    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // TODO add your handling code here:
        new DBOperations().setStatus(LoginFrame.un,0);
        System.exit(0);
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        //ShowMyVideoMedia_andReceiveVideo s=new ShowMyVideoMedia_andReceiveVideo();
    }//GEN-LAST:event_btnBackActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if(status.equals("t"))
        {
            ArrayList al = new DBOperations().getUsers("t");
         
         MainFrame.container.removeAll();
         showUsers su=new showUsers();
         su.setBounds(350,100, 642, 485);
         MainFrame.container.setVisible(false);
        MainFrame.container.add(su);
        MainFrame.container.setVisible(true);
        su.showMails(al,"t");
        }
        else if(status.equals("v"))
        {
            ArrayList al = new DBOperations().getUsers("v");
         
         MainFrame.container.removeAll();
         showUsers su=new showUsers();
         su.setBounds(350,100, 642, 485);
         MainFrame.container.setVisible(false);
        MainFrame.container.add(su);
        MainFrame.container.setVisible(true);
        su.showMails(al,"v");
        }
            
    }//GEN-LAST:event_jButton1ActionPerformed

    private void usernameTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usernameTableMouseClicked
        // TODO add your handling code here:
        if(evt.getClickCount()==2)
        {
            System.out.println("hello");
            otherun = (String) usernameTable.getValueAt(usernameTable.getSelectedRow(),usernameTable.getSelectedColumn());
            if(otherun!=null)
            {
                guestip=db.getIP();
             guestvideoport=db.getGuestVideoReceivePort();
             myvideoreceiveport=db.getMyVideoReceivePort();
                    try {
                        new runBoth();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(showUsers.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }
    }//GEN-LAST:event_usernameTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JTable usernameTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void tableChanged(TableModelEvent e) {
               
        TableModel model = (TableModel)e.getSource();
        otherun = (String)model.getValueAt(usernameTable.getSelectedRow(),usernameTable.getSelectedColumn());
    }
}
