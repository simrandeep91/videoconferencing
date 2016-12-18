package videoconferencing.smtp;

import java.util.Properties;
import java.util.Date;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendSMTP {

    
    static Multipart mp=null;
    public SendSMTP()
    {
        mp=new MimeMultipart();
    }
    public static String sendMail(String toEmailId, String text, String subject,String username,String password,String host) {
        String result = "failed";
        //System.out.println(toEmailId);
        //System.out.println(text);
        //System.out.println(subject);
        //System.out.println(username);
        //System.out.println(password);
        //System.out.println(host);
        try {

//--[ Set up the default parameters

            Properties p = System.getProperties();
            p.put("mail.smtp.auth", "true");
            p.put("mail.transport.protocol", "smtp");
            p.put("mail.smtp.host", host);
            p.put("mail.smtp.port", "587");
            p.put("mail.smtp.starttls.enable", "true");

//--[ Create the session and create a new mail message
            Authenticator auth = new SMTPAuthenticator(username, password);
            Session mailSession = Session.getInstance(p, auth);
            //Session mailSession = Session.getInstance(p, null);
            Message msg = new MimeMessage(mailSession);

//--[ Set the FROM, TO, DATE and SUBJECT fields
            msg.setFrom(new InternetAddress(username));
            String rec[] = toEmailId.split(";");


            int count = 1;
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rec[0]));
            while (count < rec.length) {
                msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(rec[count]));
                count++;
            }

            msg.setSentDate(new Date());
            msg.setSubject(subject);

//--[ Create the body of the mail
            if(mp!=null)
            {
                MimeBodyPart bp = new MimeBodyPart();
                bp.setText(text);
                mp.addBodyPart(bp);
                msg.setContent(mp);
            }
            else
            {
                msg.setContent(text, "text/plain");
            }

//--[ Ask the Transport class to send our mail message

            Transport trans = mailSession.getTransport();

            //trans.connect(host,username,password);
            trans.connect();
            Address[] ad = msg.getAllRecipients();
            //for (int i = 0; i < ad.length; i++) {

                try {
                    //Address[] ad1 = new Address[1];
                    //ad1[0] = ad[i];

                    trans.sendMessage(msg, ad);
                    //Transport.send(msg);
                    result = "sent";

                } catch (Exception e) {
                    System.out.println("in catch block of senaMail"+e);
                }
            //}
        } catch (Exception E) {
            System.out.println("Something gone wrong while sending mail!"+E);
            E.printStackTrace();
            return result;
        }

        return result;
    }
    public int attachFile(String filepath,String name)
    {
        int flag=0;
        try
        {
                MimeBodyPart bp = new MimeBodyPart();
                // Put a file in the body part
                FileDataSource fds = new FileDataSource(filepath);
               
                bp.setDataHandler(new DataHandler(fds));
                bp.setFileName(name);
                mp.addBodyPart(bp);
                //System.out.println(mp.getCount());
                flag=1;
        }
        catch(Exception e)
        {
            System.out.println("in attachFile function "+e);
            return flag;
        }
        return flag;
    }
    public static void removeFile(int i)
    {
        try
        {
            mp.removeBodyPart(i);
            //System.out.println("after removal "+mp.getCount());
        }
        catch(Exception e)
        {
            System.out.println("in catch of removeFile function "+e);
            
        }
        
    }
}