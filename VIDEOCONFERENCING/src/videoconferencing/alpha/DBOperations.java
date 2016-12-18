/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing.alpha;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Manager;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import videoconferencing.CreateWindow1;
import videoconferencing.LoginFrame;
import videoconferencing.runBoth;
import videoconferencing.showServers;
import videoconferencing.showUsers;

/**
 *
 * @author Simran
 */
public class DBOperations {
        
    public UserMasterBean validateUser(String username, String password)
    {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        UserMasterBean objBean = new UserMasterBean();
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select * from personaldetail where username='"+username+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                if(rs.getString("password").equals(password))
                {
                    objBean.setUsername(rs.getString("username"));
                    objBean.setPassword(rs.getString("password"));
                    objBean.setPassword(rs.getString("emailid"));
                    return objBean;
                }
            }
            return null;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                rs.close();
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
    }
        
    public void receiveStream() throws Exception
    {
    // Create capture device
    CaptureDeviceInfo deviceInfo = CaptureDeviceManager.getDevice("vfw:Microsoft WDM Image Capture (Win32):0");
   /* Boolean a= CaptureDeviceManager.addDevice(deviceInfo);
System.out.println(a);*/

    System.out.println("hi"+deviceInfo.toString());
    Player player = Manager.createRealizedPlayer(deviceInfo.getLocator());
    if(player==null)
        System.exit(0);
    player.start();

    // Wait a few seconds for camera to initialise (otherwise img==null)
    Thread.sleep(2500);

    // Grab a frame from the capture device
    FrameGrabbingControl frameGrabber = (FrameGrabbingControl)player.getControl("javax.media.control.FrameGrabbingControl");
        javax.media.Buffer buf = frameGrabber.grabFrame();

    // Convert frame to an buffered image so it can be processed and saved
    Image img = (new BufferToImage((VideoFormat)buf.getFormat()).createImage(buf));
    BufferedImage buffImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = buffImg.createGraphics();
    g.drawImage(img, null, null);

    // Overlay curent time on image
    g.setColor(Color.RED);
    g.setFont(new Font("Verdana", Font.BOLD, 16));
    g.drawString((new Date()).toString(), 10, 25);

    // Save image to disk as PNG
    ImageIO.write(buffImg, "png", new File("C:/webcam.png"));

    // Stop using webcam
    player.close();
    player.deallocate();
    System.exit(0);
    }

    public void createClient() throws Exception{
        
        /*Thread t=new CreateClientTextChat();
        t.start();
        ds4 = new DatagramSocket(1555);
        TheServer();*/

    }

    public void setStatus(String username,int check) {
        
        Connection con = null;
        Statement stmt = null;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="update status set loggedin ='"+check+"' where username='"+username+"';";
            stmt.executeUpdate(qry);
            qry="update status set ip = NULL where username='"+username+"';";
            stmt.executeUpdate(qry);
            qry="delete from filename where username='"+username+"';";
            stmt.executeUpdate(qry);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public void setChatStatus(String username, String string) {
        
        Connection con = null;
        Statement stmt = null;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="update status set chat='"+string+"' where username='"+username+"';";
            stmt.executeUpdate(qry);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public ArrayList getUsers(String string) {
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList alt= new ArrayList();
        ArrayList alv=new ArrayList();
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select * from status where username <>'"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                if(rs.getString("loggedin").equals("1"))
                {
                    if(rs.getString("chat").equals("v"))
                    {
                    UserMasterBean objBean = new UserMasterBean();
                    objBean.setUsername(rs.getString("username"));
                    objBean.setPassword("alpha");
                    alv.add(objBean);
                    }
                    if(rs.getString("chat").equals("t"))
                    {
                    UserMasterBean objBean = new UserMasterBean();
                    objBean.setUsername(rs.getString("username"));
                    objBean.setPassword("alpha");
                    alt.add(objBean);
                    }
                }
            }
            if(string.equals("v"))
            {
                return alv;
            }
            else
            {
                return alt;
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                rs.close();
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }
    }

    public void setIP() {
        
        Connection con = null;
        Statement stmt = null;
        String ip = null;
        try
        {
            InetAddress ownIP=InetAddress.getLocalHost();
            ip=ownIP.getHostAddress();
            runBoth.myip=ip;
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="update status set ip ='"+ip+"' where username='"+LoginFrame.un+"';";
            stmt.executeUpdate(qry);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public String getIP() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        String ip = null;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select ip from status where username='"+showUsers.otherun+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getString("ip");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }
    }

    public void setReceivePort() {
        Connection con = null;
        Statement stmt = null;
        String ip = null;
        Random rand = new Random();
        try
        {            
            LoginFrame.rport = rand.nextInt(65535-49152+1)+49152;
            //LoginFrame.tport=LoginFrame.rport+1000;
            if(LoginFrame.rport%2==1)
                LoginFrame.rport+=1;
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="update status set rport ='"+LoginFrame.rport+"' where username='"+LoginFrame.un+"';";
            stmt.executeUpdate(qry);
            qry="update status set tport ='"+(LoginFrame.rport+100)+"' where username='"+LoginFrame.un+"';";
            stmt.executeUpdate(qry);
            qry="update status set fileport ='"+(LoginFrame.rport-100)+"' where username='"+LoginFrame.un+"';";
            stmt.executeUpdate(qry);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public int getGuestVideoReceivePort() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        int ip=-1;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select rport from status where username='"+showUsers.otherun+"'and username<>'"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getInt("rport");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }
    }

    public Boolean checkUsername(String username) {
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select * from personaldetail where username='"+username+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                return true;
            }
            return false;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return true;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return true;
            }
        }
    }
    public String updatePersonalRecord(UserPersonalDetailBean objBean)
    {
        Connection con=null;
        PreparedStatement pstmt=null,stmt=null;
        String result="failed";
        try
        {
            con=DBConnection.getConnection();
                pstmt=(PreparedStatement) con.prepareStatement("update userpersonaldetail set name=?,date_of_birth=?,address=?,phone=?,mobile=?,email=? where username=?;");
                stmt=(PreparedStatement) con.prepareStatement("update personaldetail set emailid=? where username=?;");
                pstmt.setString(7,objBean.getUsername());
                pstmt.setString(1,objBean.getName());
                pstmt.setString(2,objBean.getDate());
                pstmt.setString(3,objBean.getAddress());
                pstmt.setString(4,objBean.getPhone());
                pstmt.setString(5,objBean.getMobile());
                pstmt.setString(6,objBean.getEmail());
                stmt.setString(1,objBean.getEmail());
                stmt.setString(2, objBean.getUsername());
                int i=pstmt.executeUpdate();
                i=stmt.executeUpdate();
                if(i>0)
                    result="updated";
            
        }
        catch(Exception e)
        {
                System.out.println("in updatePersonalRecord function"+e);
                return result;
        }
        finally
        {
            try
            {
                pstmt.close();
                con.close();
                stmt.close();
            }
              catch(Exception e)
             {
                System.out.println("in finally of updatePersonalRecord function"+e);
                return result;
               }
        }
        return result;
    }

     public UserPersonalDetailBean getAllPersonalRecord(String Username)
    {
        Connection con=null;
        Statement stmt=null;
        ResultSet rs=null;
        UserPersonalDetailBean objBean=new UserPersonalDetailBean();
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            rs=stmt.executeQuery("select * from userpersonaldetail where username='"+Username+"';");
            while(rs.next())
            {
                objBean.setUsername(Username);
                objBean.setName(rs.getString("name"));
                objBean.setAddress(rs.getString("address"));
                objBean.setDate(rs.getString("Date_Of_Birth"));
                objBean.setPhone(rs.getString("phone"));
                objBean.setMobile(rs.getString("mobile"));
                objBean.setEmail(rs.getString("email"));
            }
        }
        catch(Exception e)
        {
            System.out.println("in getAllProfileAccountDetail of DBOperation"+e);
            return objBean;
        }
        finally
        {
            try
            {
             rs.close();
             stmt.close();
             con.close();
            }
            catch(Exception e)
            {
            System.out.println("in finally of getAllProfileAccountDetail of DBOperation"+e);
            return objBean;
            }
        }
        return objBean;
    }
     
    public int signup(String un,String pw,String eid) {
        
        Connection con = null;
        PreparedStatement pstmt = null,stmt=null,st=null;
        int i=0;
        try
        {
            con=DBConnection.getConnection();
            pstmt=(PreparedStatement) con.prepareStatement("insert into personaldetail (username,password,emailid) values(?,?,?);");
            pstmt.setString(1,un);
            pstmt.setString(2,pw);
            pstmt.setString(3,eid);
            stmt=(PreparedStatement) con.prepareStatement("insert into status(username,loggedin) values(?,?);");
            stmt.setString(1,un);
            stmt.setBoolean(2,false);
            st=(PreparedStatement) con.prepareStatement("insert into userpersonaldetail(username,email) values(?,?);");
            st.setString(1,un);
            st.setString(2,eid);
            i=pstmt.executeUpdate();
            i=stmt.executeUpdate();
            i=st.executeUpdate();
            return i;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                pstmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }    
    }

    public Boolean checkPassword(String pw) {
         Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select password from personaldetail where username='"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                if(rs.getString("password").equals(pw))
                return true;
            }
            return false;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return false;
            }
        }
    }

    public int changePassword(String npw) {
        Connection con = null;
        Statement stmt = null;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="update personaldetail set password='"+npw+"'where username='"+LoginFrame.un+"';";
            int i=stmt.executeUpdate(qry);
            return i;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
               
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }
    }

    public String getEmailfromUserName() {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        String email="";
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select emailid from personaldetail where username='"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                email=rs.getString("emailid");
                return email;
            }
            return null;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }
        
    }

    public String getPassword() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        String email="";
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select password from personaldetail where username='"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                email=rs.getString("password");
                return email;
            }
            return null;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }
        
    }

    public int gettextport() {

        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        int ip=-1;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select tport from status where username='"+showUsers.otherun+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getInt("tport");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }

    }

    public int getmytextport() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        int ip=-1;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select tport from status where username='"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getInt("tport");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }
    }

    public void setmytextport(int i,int s) {
        Connection con = null;
        Statement stmt = null;
        try
        {            
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="update status set tport ='"+(i+2)+"' where username='"+LoginFrame.un+"';";
            stmt.executeUpdate(qry);
            qry="update status set tport ='"+(s+2)+"' where username='"+showUsers.otherun+"';";
            stmt.executeUpdate(qry);

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public void getconnection() {
        Socket echoSocket = null;
        try {
            System.out.println(showUsers.guestip);
            System.out.println(showUsers.guesttextport);
        echoSocket = new Socket(showUsers.guestip,8000);
       // echoSocket = new Socket("localhost",8000);
        } 
        catch (UnknownHostException e) {
        System.err.println("Don't know about host: taranis.");
        //System.exit(1);
        } catch (Exception e) {
        System.err.println("Couldn't get I/O for "+ "the connection to: taranis.");
        e.printStackTrace();
        //System.exit(1);
        }
finally
{
try
{
echoSocket.close();
}
catch(Exception ex)
{
System.out.println(ex.getMessage());
}
}
    }

    public int getportfromun(String un) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        int ip=-1;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select tport from status where username='"+un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getInt("tport");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }
    }

    public String getusernamefromip(String chk) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        String ip="";
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select username from status where ip='"+chk+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getString("username");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }
        
    }

    public int getfileport() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        int ip=-1;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select fileport from status where username='"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getInt("fileport");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }
    }

    public void setfilename(String name, int port) {
        Connection con = null;
        PreparedStatement pstmt = null,stmt=null,st=null;
        int i=0;
        try
        {
            con=DBConnection.getConnection();
            pstmt=(PreparedStatement) con.prepareStatement("insert into filename (username,filename,fileport) values(?,?,?);");
            pstmt.setString(1,LoginFrame.un);
            pstmt.setString(2,name);
            pstmt.setInt(3,port);
            pstmt.executeUpdate();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                pstmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }    
    }

    public ArrayList getfilesfromip(String ip) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null,sr=null;
        ArrayList al=new ArrayList();
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select username from status where ip='"+ip+"'and username<>'"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                String username=rs.getString("username");
                qry="select filename from filename where username='"+username+"';";
                sr=stmt.executeQuery(qry);
                while(sr.next())
                {
                    String filename=sr.getString("filename");
    //                System.out.println(filename);
                    al.add(filename);
                }
                return al;
            }
            System.out.println("in side al");
            return null;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("ot side al");
            e.printStackTrace();
            return null;
        }
        
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                System.out.println("indasda side al");
                e.printStackTrace();
                return null;
            }
        }
    }

    public int getportfromip(String ip,String file) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null,sr=null;
        int port=-1;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select username from status where ip='"+ip+"'and username<>'"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                String username=rs.getString("username");
                qry="select fileport from filename where username='"+username+"'and filename='"+file+"';";
                sr=stmt.executeQuery(qry);
                while(sr.next())
                {
                    port=sr.getInt("fileport");
                }
                return port;
            }
            return -1;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return -1;
        }
        
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }
    }

    public boolean getconnectionVideo() {
        Socket echoSocket = null;
        try {
            
        echoSocket = new Socket(showUsers.guestip,9000);
        if(echoSocket!=null)
            return true;
        else
            return false;
        } 
        catch (UnknownHostException e) {
        System.err.println("Don't know about host: taranis.");
        return false;
        } catch (Exception e) {
        System.err.println("Couldn't get I/O for "+ "the connection to: taranis.");
        return false;
        
        //System.exit(1);
        }
finally
{
try
{
echoSocket.close();
}
catch(Exception ex)
{
System.out.println(ex.getMessage());
return false;
}
}
}

public int getMyVideoReceivePort() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        int port=-1;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select rport from status where username='"+LoginFrame.un+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                port=rs.getInt("rport");
            }
            return port;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return -1;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return -1;
            }
        }
    }

    public void setmyvideoport() {
        Connection con = null;
        Statement stmt = null;
        try
        {            
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="update status set rport=rport+4 where username='"+LoginFrame.un+"';";
            stmt.executeUpdate(qry);
            qry="update status set rport=rport+4 where username='"+showUsers.otherun+"';";
            stmt.executeUpdate(qry);

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    public ArrayList getloggedinusers() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null,sr=null;
        ArrayList al=new ArrayList();
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select username from status where loggedin=1;";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                String username=rs.getString("username");
                    al.add(username); 
            }
            return al;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("ot side al");
            e.printStackTrace();
            return null;
        }
        
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                System.out.println("indasda side al");
                e.printStackTrace();
                return null;
            }
        }
    }

    public String getIPforwhiteboard() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs=null;
        String ip = null;
        try
        {
            con=DBConnection.getConnection();
            stmt=(Statement) con.createStatement();
            String qry="select ip from status where username='"+showServers.s+"';";
            rs=stmt.executeQuery(qry);
            while(rs.next())
            {
                ip=rs.getString("ip");
            }
            return ip;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
                con.close();
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
                return null;
            }
        }
    }
}