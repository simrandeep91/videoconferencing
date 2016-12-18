/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package videoconferencing.alpha;

/**
 *
 * @author Simran
 */
public class UserMasterBean {

    private String username="";

    
    private String password="";
    private String emailid="";
    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }
    public String getPassword() {
        return password;
    }

       public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
}
