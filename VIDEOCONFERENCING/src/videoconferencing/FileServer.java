/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing;

/**
 *
 * @author Simran
 */
import java.net.*;
import java.io.*;
import javax.swing.JOptionPane;

public class FileServer extends Thread{
    public static String filename="";
    public static int port;
  public void run() {
    try
    {
    // create socket
        JOptionPane.showMessageDialog(MainFrame.container,"File Uploaded","Message", JOptionPane.INFORMATION_MESSAGE);
    ServerSocket servsock = new ServerSocket(port);
    while (true) {
      System.out.println("Waiting...");
      Thread.currentThread().sleep(1000);
      Socket sock = servsock.accept();
      System.out.println("Accepted connection : " + sock);

      // sendfile
      File myFile = new File (filename);
      byte [] mybytearray  = new byte [(int)myFile.length()];
      FileInputStream fis = new FileInputStream(myFile);
      BufferedInputStream bis = new BufferedInputStream(fis);
      System.out.println(mybytearray.length);
      bis.read(mybytearray,0,mybytearray.length);
      OutputStream os = sock.getOutputStream();
      System.out.println("Sending...");
      os.write(mybytearray,0,mybytearray.length);
      os.flush();
      sock.close();
      }
    }
    catch(Exception ex)
    {
        ex.printStackTrace();
        System.out.println(ex.getMessage());
        System.out.println(ex.getMessage());
    }
    }
}
