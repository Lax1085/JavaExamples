/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unapprovedtime;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.IFSFileInputStream;
import com.ibm.as400.access.RequestNotSupportedException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.*;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPrintPage;
import java.awt.print.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.sql.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.print.*;
import javax.print.attribute.*;
import javax.swing.JOptionPane;
import javax.print.attribute.standard.Copies;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;
import java.text.ParseException;
import java.awt.Desktop;
import javax.swing.JOptionPane;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import com.ibm.as400.javax.print.ISeriesPrintService;
/**
 *
 * @author alopez
 *  Class: UnapprovedTime
 *  Arguments: 
 *  description: Checks for any time swipes that have not been approved. Sends notification email to spervisors based on location of swipes. 
 */public class UnapprovedTime {

    /**
     * @param args the command line arguments
     */
    static Connection connection;
    static ResultSet rs;
    static ResultSet rs2;    
    static ArrayList<String> southElginEmail = new ArrayList();
    static ArrayList<String> genoaEmail = new ArrayList();
    static ArrayList<String> cascoEmail = new ArrayList();
    static String DATE_FORMAT = "yyyyMMdd";    
    static Map<String,ArrayList<String>> times = new HashMap();
    public static void main(String[] args) {
        // TODO code application logic here
        try{
            initProgram();
        }
        catch(Exception ex)
        {
            StackTraceElement[] elements = ex.getStackTrace();  
            for (int iterator=1; iterator<=elements.length; iterator++)  
                System.out.println("Class Name:"+elements[iterator-1].getClassName()+" Method Name:"+elements[iterator-1].getMethodName()+" Line Number:"+elements[iterator-1].getLineNumber());            
            System.out.println("Issue creating Sending out Email");
            System.out.println(ex);
            System.exit(1);       
        }
    }
    public static void initProgram() throws MyOwnException, IOException, SQLException
    {
        times.put("1",new ArrayList());
        times.put("2",new ArrayList());
        String user = "JAVAPROG";
        String psswd = "JAVAPROG";

        String sDriver = "com.ibm.as400.access.AS400JDBCDriver";
            //translate binary=true;extended metadata=true
        String sURL = "jdbc:as400://10.10.1.2/;user=" + user + ";password=" + psswd;
        try 
        {
            Class.forName(sDriver);
            connection = DriverManager.getConnection(sURL);
            //stmt = connection.prepareStatement(sDriver);
            //stmt2 = connection.prepareStatement(sDriver);
            
        } catch (Exception e) {
            System.err.println("Unable to Connect.");
            e.printStackTrace();
            return;
        }    
        try{
            getEmailAddresses();
        }
        catch(Exception ex)
        {
            System.out.println("Unable to collect Email list");
            System.exit(1);
        }
        try{
            getTimes();
        }
        catch (Exception ex)
        {
            System.out.println("Unable to collect times");
            System.exit(1);          
        }
        if(!times.get("1").isEmpty())
        {
            sendEmail("South Elgin",times.get("1"),southElginEmail);
        }
        if(!times.get("1").isEmpty())
        {
            sendEmail("Genoa",times.get("2"),genoaEmail);
        }        
    }
    private static void getEmailAddresses() throws SQLException{
        PreparedStatement stmt2 = connection.prepareStatement("select eno_not, eno_ausr, cac_email from qs36f.emailnot, qs36f.cacont where eno_not = 'UNAPPROVED_TIME_SOUTH_ELGIN' and eno_ausr = cac_as4usr");
        rs2 = stmt2.executeQuery();

        while(rs2.next()) {
            String emailaddr = rs2.getObject(3).toString();
            southElginEmail.add(emailaddr);
        }

        stmt2 = connection.prepareStatement("select eno_not, eno_ausr, cac_email from qs36f.emailnot, qs36f.cacont where eno_not = 'UNAPPROVED_TIME_GENOA' and eno_ausr = cac_as4usr");
        rs2 = stmt2.executeQuery();

        while(rs2.next()) {
            String emailaddr = rs2.getObject(3).toString();
            genoaEmail.add(emailaddr);
        }    
    }
    public static void sendEmail2(String location, ArrayList data,ArrayList emails){
    
    }
    public static void sendEmail(String location, ArrayList data,ArrayList emails) throws MyOwnException, IOException, SQLException 
    {
        //System.out.println("emailing...");
        // Sender's emal ID needs to be mentioned
        String from = "noreply@custom-aluminum.com";
        
        final String USERNAME = "alopez";
        final String PASSWORD = "67LdOp*99";
        // Assuming you are sending email from localhost
        String host = "10.10.1.4";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", "25");
        properties.setProperty("mail.smtp.auth", "true");
        //properties.put("mail.smtp.from", "alopez@custom-aluminum.com");
        // Get the default Session object.
        //Session session = Session.getDefaultInstance(properties);
        Session session = Session.getInstance(properties,
             new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                   return new PasswordAuthentication(USERNAME, PASSWORD);
           }
             });
        try{

            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from, "Custom Aluminum Labor"));

            //To_email.add("lax1000085@gmail.com");
            message.addRecipient(Message.RecipientType.CC, new InternetAddress("alopez@custom-aluminum.com"));
            // Set To: header field of the header.
            for (int i = 0; i < emails.size(); i++) {
                if(!emails.get(i).equals("")){
                   //System.out.println("Recipients :"+emails.get(i));
                   message.addRecipient(Message.RecipientType.TO, new InternetAddress(emails.get(i).toString()));
                }
            }           
            // Set Subject: header field
            String subj = "Unapproved Labor for " + location + "!";
            message.setSubject(subj);
            
            String msgBody = "PLEASE MAKE ALL APPROVALS BY 11:00am \n\n"
            + "Labor has not yet been approved for the following stations: \n";
            for (int i = 0; i < data.size(); i++) {
                if(!emails.get(i).equals("")){
                   //System.out.println("Departments :"+data.get(i));
                   msgBody=msgBody+data.get(i).toString();
                }
            }                 
            
           
            // create the message part 
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            
            //fill message            
            messageBodyPart.setText(msgBody);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Put parts in message
            message.setContent(multipart);           

            // Send message
            Transport.send(message);
            
            //rset.close();
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
        
    }    
    public static void getTimes() throws SQLException
    {
        PreparedStatement stmt = connection.prepareStatement("select distinct tim_shfdat, tim_shift, tim_dptabv, tim_dptplt, tmd_dptloc, tmd_dptdsc from qs36f.timrecpre, qs36f.timrecdpt where (tim_aprby is null or tim_aprby = '') and tim_machno = tmd_machno order by tmd_dptloc, tim_shfdat, tim_shift, tim_dptabv, tim_dptplt");
        rs = stmt.executeQuery();              
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Calendar today = Calendar.getInstance(); // today
        String todayString = sdf.format(today.getTime()).toString();
        //System.out.println("Today is " + sdf.format(today.getTime()));
        
	String productionDate;
	String productionShift;
	String productionDept;
	String productionPlant;
	String productionLocation;
	String productionDeptDesc;
        
        String holdProductionLocation = "";
        String emailBody = "";
        
        while(rs.next())
        {   

            productionDate = rs.getObject(1).toString();
            productionShift = rs.getObject(2).toString();
            productionDept = rs.getObject(3).toString();
            productionPlant = rs.getObject(4).toString();
            productionLocation = rs.getObject(5).toString();
            productionDeptDesc = rs.getObject(6).toString().trim();             
            
            if (productionDate.equals(todayString)){
                // skip today
            } else {
                if (productionLocation.equals(holdProductionLocation)) {
                    
                }else {
                    if (holdProductionLocation.isEmpty()){
                        // First time through, do not send email
                    }
                    else {
                        //UnapprovedLabor.sendEmail(emailBody, holdProductionLocation);
                        //emailBody = ""; //reset the message body for the next location
                    }
                    
                    holdProductionLocation = productionLocation;
                }
                String prodDateFmtd = productionDate.substring(4,6) + "/" + productionDate.substring(6,8) + "/" + productionDate.substring(0,4);
                emailBody = emailBody + prodDateFmtd + " " + productionDeptDesc + " shift " + productionShift + "\n ";
                
                times.get(productionLocation).add(prodDateFmtd + " " + productionDeptDesc + " shift " + productionShift + "\n ");
            }
        }   
    }
    
}
class MyOwnException extends Exception {
   public MyOwnException(String msg){
      super(msg);
   }
}