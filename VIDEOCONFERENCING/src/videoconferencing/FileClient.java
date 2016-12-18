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

public class FileClient{
    public static String pip="";
    public static int fileport;
    
  //public static void main (String [] args ) throws IOException {
    public FileClient(String pip,int fileport,String filename)
    {
        try
        {
    int filesize=6022386; // filesize temporary hardcoded

    long start = System.currentTimeMillis();
    int bytesRead;
    int current = 0;
    // localhost for testing
    Socket sock = new Socket(pip,fileport);
    System.out.println("Connecting...");

    // receive file
    byte [] mybytearray  = new byte [filesize];
    InputStream is = sock.getInputStream();
    FileOutputStream fos = new FileOutputStream(filename);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    bytesRead = is.read(mybytearray,0,mybytearray.length);
    current = bytesRead;

    // thanks to A. Cádiz for the bug fix
    do {
       bytesRead =
          is.read(mybytearray, current, (mybytearray.length-current));
       if(bytesRead >= 0) current += bytesRead;
    } while(bytesRead > -1);

    bos.write(mybytearray, 0 , current);
    bos.flush();
    long end = System.currentTimeMillis();
    System.out.println(end-start);
    bos.close();
    sock.close();
    JOptionPane.showMessageDialog(MainFrame.container,"File Saved","Message",JOptionPane.INFORMATION_MESSAGE);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
  }
}