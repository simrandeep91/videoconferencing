/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.swing.JOptionPane;
import videoconferencing.alpha.DBOperations;

/**
 *
 * @author Simran
 */
public class runBoth implements Cloneable{
    public static int UserNumber=0;
    static MediaLocator mlr,mmlr=null;
    public int p1;
    public int p2;
    public static String myip="";
    DBOperations db=new DBOperations();
    String str="";
    runBoth() throws InterruptedException
    {
        p1=showUsers.myvideoreceiveport;
        p2=p1+2;
        Thread.sleep(1000);
        UserNumber=UserNumber+1;
        if(UserNumber>6)
        {
            JOptionPane.showMessageDialog(MainFrame.container,"Reached max Limit for Users","Error", JOptionPane.OK_OPTION);
            return;
        }
        AVReceive3.cl=false;
       boolean b=db.getconnectionVideo();
        try {
            DatagramSocket ds = new DatagramSocket(9002);
            byte buffer[] = new byte[20];
            do
            {
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            ds.receive(dp);
            str = new String(dp.getData());
            }while(str.trim().equals(""));
            ds.close();
         } catch (IOException iOException) {
             System.out.println(iOException.getMessage());
        }
        
        if(b==false || str.trim().equals("false"))
        {
            JOptionPane.showMessageDialog(MainFrame.container,"Connection Refused by User","Error", JOptionPane.INFORMATION_MESSAGE);
            str="";
            return;
        }
        else if(str.trim().equals("true"))
        {
            str="";
            runTrans();
            runReceive();
        }
        //runTrans();
        //runReceive();
   }

    runBoth(String string) throws InterruptedException {
        p1=db.getMyVideoReceivePort();
        p2=p1+2;
        Thread.sleep(1000);
        UserNumber=UserNumber+1;
        if(UserNumber>6)
        {
            JOptionPane.showMessageDialog(MainFrame.container,"Reached max Limit for Users","Error", JOptionPane.OK_OPTION);
            return;
        }
        AVReceive3.cl=false;
        runTrans();
        runReceive();
    }
    public final void runReceive()
    {
        String argv[]={showUsers.guestip+"/"+p1,showUsers.guestip+"/"+p2};
        //String argv[]={myip+"/8000",myip+"/8002"};
	if (argv.length == 0)
	    AVReceive3.prUsage();

	AVReceive3 avReceive = new AVReceive3(argv);
        avReceive.start();
        if (!avReceive.initialize()) {
	    System.err.println("Failed to initialize the sessions.");
	    System.exit(-1);
	}
    }
    
    public final void runTrans()
    {
        int k=0;
        if(UserNumber==1)
        {
          Vector v=CaptureDeviceManager.getDeviceList(new AudioFormat(null));
           CaptureDeviceInfo cdi=(CaptureDeviceInfo) v.firstElement();
           mlr = cdi.getLocator();
        }     
        String args[]={mlr.toString(),showUsers.guestip,showUsers.guestvideoport+""};
	//String arg[]={mlr.toString(),showUsers.guestip,showUsers.guestvideoport+""};
        //String args[]={mlr.toString(),myip,"8000"};
	if (args.length < 3) {
	    AVTransmit3.prUsage();
	}

	Format fmt = null;
	int i = 0;

	// Create a audio transmit object with the specified params.
	AVTransmit3 at = new AVTransmit3(new MediaLocator(args[i]),
					     args[i+1], args[i+2], fmt);
	// Start the transmission
        at.start();
	
	// result will be non-null if there was an error. The return
	// value is a String describing the possible error. Print it.
	k=k+1;
	// Transmit for 60 seconds and then close the processor
	// This is a safeguard when using a capture data source
	// so that the capture device will be properly released
	// before quitting.
	// The right thing to do would be to have a GUI with a
	// "Stop" button that would call stop on AVTransmit2
        if(k==1)
        {
            if(UserNumber==1)
            {
            Vector vv=CaptureDeviceManager.getDeviceList(new VideoFormat(null));
            CaptureDeviceInfo ccdi=(CaptureDeviceInfo) vv.firstElement();
            mmlr = ccdi.getLocator();
            }
            String argv[]={mmlr.toString(),showUsers.guestip,(showUsers.guestvideoport+2)+""};
          //String arga[]={mmlr.toString(),showUsers.guestip,(showUsers.guestport+2)+""};
            //String argv[]={mmlr.toString(),myip,"8002"};
            if (argv.length < 3) {
	    AVTransmit1.prUsage();
            }
            fmt = null;
	
            i=0;
	// Create a video transmit object with the specified params.
	AVTransmit1 att = new AVTransmit1(new MediaLocator(argv[i]),
					     argv[i+1], argv[i+2], fmt);
        att.start();
        // result will be non-null if there was an error. The return
	// value is a String describing the possible error. Print it.
        }
    }
    public static void main(String args[])
    {
        try {
            runBoth rb=new runBoth();
        } catch (InterruptedException ex) {
            Logger.getLogger(runBoth.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
 