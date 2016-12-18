/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import videoconferencing.alpha.DBOperations;

/**
 *
 * @author Simran
 */
public class SocketserverVideo extends Thread{
    DBOperations db=new DBOperations();
    private InetAddress ia;
    private DatagramSocket ds;
    private byte buffer[];
    private DatagramPacket dp;
    @Override
    public void run()
{
ServerSocket ss=null;
Socket c=null;
try
{
ss=new ServerSocket(9000);
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
System.out.println(un);
int i=JOptionPane.showConfirmDialog(MainFrame.container,"Connection from "+un+"\r\nDo you want to Connect","Connection Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
         if(i==1)
         {
          ia=InetAddress.getByName(chk);
          ds=new DatagramSocket();
          buffer="false".getBytes();
          dp=new DatagramPacket(buffer, buffer.length, ia, 9002);
          ds.send(dp);
          System.out.println("refuse");
          }
         else
         {
              ia=InetAddress.getByName(chk);
              ds=new DatagramSocket();
              buffer="true".getBytes();
              dp=new DatagramPacket(buffer, buffer.length, ia, 9002);
              ds.send(dp);
              System.out.println(un);
              showUsers.guestip=chk;
              showUsers.guestvideoport=db.getGuestVideoReceivePort();
              new runBoth("");
              db.setmyvideoport();
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
