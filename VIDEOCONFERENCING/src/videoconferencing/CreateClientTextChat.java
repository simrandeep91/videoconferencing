package videoconferencing;
import java.net.*;
import java.util.Random;

public class CreateClientTextChat extends Thread{
public static int buffer_size = 1024;
public static DatagramSocket ds3,ds4;
public static byte buffer[] =new byte[buffer_size];
public static String msg;
int text,send;
String ip="";
int k=0,i;
private static int t,s;
CreateClientTextChat(int textport, int sendport) {
    t=textport;
    s=sendport;
    }
public static void TheServer() throws Exception {
buffer=new byte[buffer_size];
msg=CreateWindow1.txtMsg.getText();
buffer=msg.getBytes();
//ds4.send(new DatagramPacket(buffer,msg.length(),InetAddress.getLocalHost(),1888));
ds4.send(new DatagramPacket(buffer,msg.length(),InetAddress.getByName(showUsers.guestip),s));
}
public void run()
{
try
{
    text=t;
    ip=showUsers.guestip;
    send=s;
    ds3=new DatagramSocket(text);
    //ds3=new DatagramSocket(1777);
    System.out.println("holdin the port");
while(true) {
k=k+1;
buffer=new byte[buffer_size];
DatagramPacket p = new DatagramPacket(buffer, buffer.length);
/*if(k==1)
    i=JOptionPane.showConfirmDialog(MainFrame.container,"Do you want to connect to "+showUsers.otherun,"Connection Confirmation",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
    if(i==1)
    {
          return;
    }
    new DBOperations().setmytextport();*/
ds3.receive(p);
System.out.println(new String(p.getData(), 0, p.getLength())+"kkkhk");
CreateWindow1.txtSend.append(showUsers.otherun+": "+new String(p.getData(), 0, p.getLength())+"\r\n");
}
}
catch(Exception e)
{
}
}
public static void main(String args[]) throws Exception {
Thread t=new CreateClientTextChat(-1,-1);
//t.start();
Random rand=new Random();
int k=rand.nextInt(65535-49152+1)+49152;
ds4 = new DatagramSocket(k);
//ds4 = new DatagramSocket(1555);
TheServer();
}
}