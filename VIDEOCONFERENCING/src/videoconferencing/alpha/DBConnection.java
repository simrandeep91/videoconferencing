/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing.alpha;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Simran
 */
public class DBConnection {
    
    public static String dbip="";
    public static Connection getConnection()
    {
        Connection con = null;
        try
        {
           Class.forName("com.mysql.jdbc.Driver");
           con = (Connection) DriverManager.getConnection("jdbc:mysql://"+dbip+"/videoconferencing" , "root" , "mysql");
        }
        catch(Exception e)
        {
           System.out.println("Database Connection cannot be made"+e.getMessage());         
        }
        return con;
    }
}
