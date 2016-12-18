/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package emailplus.smtp;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 *
 * @author Administrator
 */
public class SMTPAuthenticator extends Authenticator{
    private String username,password;
	public SMTPAuthenticator(String username,String password){
		this.username=username;
		this.password=password;
		System.out.println("username = "+this.username+" \npasword are set");
	}
	public javax.mail.PasswordAuthentication getPasswordAuthentication(){
		System.out.println("verifying username and password with ="+this.username);
		return new PasswordAuthentication(this.username,this.password);
	}

}
