/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import videoconferencing.alpha.DBOperations;

/**
 *
 * @author Simran
 */
public class Socketserver extends Thread{
    DBOperations db=new DBOperations();
    @Override
    public void run()
{
ServerSocket ss=null;
Socket c=null;
try
{
ss=new ServerSocket(8000,10);
while(true)
{
Thread.sleep(1000);
Thread.currentThread().getName();
c=ss.accept();
if(c.isConnected())
{
System.out.println("connection from "+c.getInetAddress().getHostName());
System.out.println("connection from "+c.getInetAddress());
System.out.println(InetAddress.getLocalHost().getHostName().toString());
String chk=c.getInetAddress().toString();
chk=chk.substring(chk.lastIndexOf("/")+1);
String un=db.getusernamefromip(chk);
showUsers.otherun=un;
int i=JOptionPane.showConfirmDialog(MainFrame.container,"Connection from "+un+"\r\nDo you want to Connect","Connection Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
         if(i==1)
         {
          return;
        }
         else
         {
             //CreateWindow1.textport=db.getmytextport();
             //CreateWindow1.sendport=db.getportfromun(un);
             showUsers.guestip=chk;
             CreateWindow1 cw=new CreateWindow1(db.getmytextport(),db.getportfromun(un));
             cw.setVisible(true);
             db.setmytextport(db.getmytextport(),db.getportfromun(un));
         }   
}
try
{
c.close();
}
catch(Exception ex)
{
System.out.println(ex.getMessage());
}
}
}
catch(Exception e)
{
System.out.println(e.getMessage());
}
finally
{
try
{
c.close();
ss.close();
}
catch(Exception ex)
{
System.out.println(ex.getMessage());
}
}

}
}
