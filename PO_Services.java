package po_services;
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
 * java PO_Services '10696' 'P' 'NETPRT36' 'AL' 'TESTPO' 
 * java -jar "\\se-fs1\public\General (Deleted After 2 Weeks)\PurchaseOrders\TempPO\Files\PO_Services.jar" 140121 V  PRINTER61 AL QS36F
 *  ===> STRPCCMD PCCMD('"R:\java\Purchase_Orders\Purchase_Orders.bat" 141229 E alo
 *   pez@custom-aluminum.com! AL QS36F') PAUSE(*NO)                                  
 * 
 * 
 * /**
 * java PrintMoveTag_dev 'P' 'RLF' '1' '2' 'NETPRT36' '50270' 
 * java PrintMoveTag_dev 'D' 'AL' '50202'
 * 
/**
 *
 * @author alopez
 *  Class: PO_Services
 *  Arguments: mode, To Address,User ID, Library
 *  description: Creates PDF document of a Purchase Order for viewing, printing, emailing based on desired option.
 */
 class PO_Services {

    /**
     * @param args the command line arguments
     */
    static String poNo;
    static String printerName;
    static FileInputStream fis2;
    static String Mode;
    static String userID;
    static pdfCreator PDF;
    static PurchaseOrder po; 
    static String To_address;
    static String Library;
    static ArrayList<String> To_email=new ArrayList();
    
    public static void main(String[] args)throws SQLException, InterruptedException,FileNotFoundException, IOException, DocumentException, MyOwnException, ParseException, PrinterException, AS400Exception
    {
        // TODO code application logic here'
        poNo="143834";
        Mode="E";
        userID="AL";
        printerName="NETPRT70";
        Library="QS36F";
        To_address="alopez@custom-aluminum.com!";
        To_address=To_address.substring(0, To_address.indexOf('!'));
        String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());

        System.out.println("******"+timeStamp+"******");
        for (String s: args) {
            System.out.println("'" + s + "'");
        }

        if(args.length > 0){
            poNo = args[0].trim();
            if(args.length>1){
                Mode = args[1].trim();

                if(Mode.trim().equalsIgnoreCase("E")){
                    To_address=args[2].trim(); 
                    To_address=To_address.substring(0, To_address.indexOf('!'));                    
                }
                else if(Mode.trim().equalsIgnoreCase("P"))
                    printerName = args[2].substring(0,8).trim();
                userID=args[3].trim();  
                Library=args[4].trim();     
            }
            
            }
        else{
            System.out.println("Not enough arguments");
        //    System.exit(1);
        }
      
        //poCOmplete();
        try{
            po=new PurchaseOrder(poNo,Library,Mode);
            System.out.println("Creating PDF:"+poNo+"...");
            PDF = new pdfCreator(po, userID);  
        }
        catch (Exception ex) {
            poCOmplete();
            StackTraceElement[] elements = ex.getStackTrace();  
            for (int iterator=1; iterator<=elements.length; iterator++)  
                System.out.println("Class Name:"+elements[iterator-1].getClassName()+" Method Name:"+elements[iterator-1].getMethodName()+" Line Number:"+elements[iterator-1].getLineNumber());            
            System.out.println("Issue creating PO");
            System.out.println(ex);
            System.exit(1);
        }
        
        if(Mode.trim().equalsIgnoreCase("E")){
            
            String[] emails= {po.poHeader.get("POH_EMAIL"),po.poHeader.get("POH_EMAIL2"),po.poHeader.get("POH_EMAIL3"),po.poHeader.get("POH_EMAIL4"),};
            //System.out.println("SENT");
            sendEmail(emails);     
        }
        else if(Mode.equalsIgnoreCase("V") || Mode.equalsIgnoreCase("D") )
        {            
            String file=PDF.getAbsolutePath().replace("\\\\SE-FS1\\Public\\", "R:\\");
            String command="cmd /c START \"\" \""+file+"\"";            
            //Works
            //Runtime.getRuntime().exec("\"c:\\Program Files (x86)\\Adobe\\Acrobat Reader DC\\Reader\\AcroRd32.exe\" \""+PDF.getAbsolutePath()+"\"");            
            //Runtime.getRuntime().exec("\"c:\\Program Files (x86)\\Adobe\\Acrobat Reader DC\\Reader\\AcroRd32.exe\" \""+file+"\"");            
            //System.out.println("\"c:\\Program Files (x86)\\Adobe\\Acrobat Reader DC\\Reader\\AcroRd32.exe\" \""+PDF.getAbsolutePath()+"\"");
            Runtime.getRuntime().exec(command);
        }
        else if(Mode.equalsIgnoreCase("P")){
           printPO();
        }
           poCOmplete();
        //PDF.delete();
        System.out.println("******"+"END"+"******");
        System.out.println("");
    }
    
    /*
    *   Send PDF via Email to disclosed recipients
    */
    @SuppressWarnings("CallToPrintStackTrace")
    private static void sendEmail(String[] emails) throws MyOwnException, IOException, SQLException 
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
        properties.put("mail.smtp.from", "kjanz@custom-aluminum.com");
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
            message.setFrom(new InternetAddress(from, "Custom Aluminum Purchasing"));
            
            for (String s: emails) {           
                //Do your stuff here
                To_email.add(s.trim()); 
            }
            To_email.add(getUserEmail().trim());
            //To_email.add("lax1000085@gmail.com");
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(
            "alopez@custom-aluminum.com"));
            // Set To: header field of the header.
            for (int i = 0; i < To_email.size(); i++) {
                if(!To_email.get(i).equals("")){
                   System.out.println("Recipients :"+To_email.get(i));
                   message.addRecipient(Message.RecipientType.TO, new InternetAddress(To_email.get(i)));
                }
            }
            
            String fileAttachment = PDF.getAbsolutePath();
                        
            // Set Subject: header field
            String subj = "PO #" + poNo;
            message.setSubject(subj);
           
            // create the message part 
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            
            //fill message
            String msg = "Attached is a reprint of Purchase Order #" + poNo + " from Custom Aluminum Products";
            messageBodyPart.setText(msg);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(fileAttachment);
            messageBodyPart.setDataHandler(new DataHandler(source));
            //messageBodyPart.setFileName(fileAttachment.substring(33));
            String file = "PO_" + poNo + ".pdf";
            messageBodyPart.setFileName(file);
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

    /*
    *   Get User's email adress
    */    
    private static String getUserEmail() throws MyOwnException, IOException,SQLException{
            DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
            Connection AS400Con = DriverManager.getConnection ("jdbc:as400://10.10.1.2/QS36F;user=JAVAPROG;password=JAVAPROG");
            // Create a statement
            Statement AS400s = AS400Con.createStatement();    
            String query ="SELECT EMU_ADDR FROM QS36F.EMAILUSER WHERE EMU_USER='"+userID+"'";    
            ResultSet rset = AS400s.executeQuery(query);       
            ResultSetMetaData rsmd = rset.getMetaData();            
            String email;
            int columnCount=rsmd.getColumnCount();
            if(rset.next()){
                email= rset.getString(1).trim();
            }
            else
               email=""; 
            AS400s.close();
            AS400Con.close();              
            return email;
    }

    /*
    *   User is done viewing PO
    */    
    private static void poCOmplete() throws  MyOwnException, IOException,SQLException{

            DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
            Connection AS400Con = DriverManager.getConnection ("jdbc:as400://10.10.1.2/QS36F;user=JAVAPROG;password=JAVAPROG");
            // Create a statement
            Statement AS400s = AS400Con.createStatement();    
            String query ="DELETE FROM QS36F.POVIEW WHERE POV_USERID='"+userID+"'";    
            AS400s.execute(query);            
            AS400s.close();
            AS400Con.close();

}  
private static void printPO()throws PrinterException, SQLException, MyOwnException,AS400Exception{
    
    //System.out.println("Printing...");
    try 
    {
        AS400 system = new AS400("10.10.1.2","JAVAPROG", "JAVAPROG");
        //System.out.println("cAttempting to print...");
        fis2 = new FileInputStream(PDF.getAbsolutePath());
                            
        Doc pdfDoc = new SimpleDoc(fis2, DocFlavor.INPUT_STREAM.PDF, null);
        //System.out.println("Looking for printers" );
        
        ISeriesPrintService printService = new ISeriesPrintService(system, printerName);

        // System.out.println("lookup completed for " + printService);
        
        if (printService != null) 
        {
            // System.out.println("Printer: " + printService.getName());
            
            //if (printServices[j].getName().equalsIgnoreCase("NETPRT73")) 
            if (printService.getName().equalsIgnoreCase(printerName)) 
            {
                // Create Print Job
                DocPrintJob pjob = printService.createPrintJob();
                //System.out.println("printer selected: " + printService.getName());
                
                try 
                {
                    pjob.print(pdfDoc, new HashPrintRequestAttributeSet());
                    //System.exit(0);                                       
                }
                catch(PrintException pe)
                {
                    System.out.println(pe);                 
                    //pe.printStackTrace();
                }
                //break;
            }
            
        }
        if (printService == null) 
        {
            throw new PrinterException("Invalid print service name: " + printerName);
        }       
                               
    }
    catch (FileNotFoundException ex) 
    {
        System.out.println("Opps... " + ex);
    }         
    catch (AS400SecurityException ex) 
    {
        System.out.println("Opps... " + ex);
    }
    catch (ErrorCompletingRequestException ex) 
    {
        System.out.println("Opps... " + ex);
    }
    catch (IOException ex) 
    {
        System.out.println("Opps... " + ex);
    }      
    catch (InterruptedException ex) 
    {
        System.out.println("Opps... " + ex);
    }   
    catch (RequestNotSupportedException ex) 
    {
        System.out.println("Opps... " + ex);
    }                                                                                                                                         

}




    
    
}
class MyOwnException extends Exception {
   public MyOwnException(String msg){
      super(msg);
   }
}
class Display
{

    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}


/**
 *
 * @author alopez
 *  Class: PurchaseOrder
 *  Arguments: Integer PoNum
 *  description: Creates a PO object filled with Header, Detail, Comment, 
 *  Metal data to create a PDF
 */
class PurchaseOrder {
    public Map<String, String> poHeader;
    public Map<String, String> poVendor;
    public ArrayList <Map<String, String>> detailLines;
    public String PO_num;
    public String PO_vendor;
    public String PO_vendorabv;
    public String PO_vendorEmail;
    public int PO_Type; 
    private double PO_total;
    private final String user2 = "JAVAPROG";
    private final String password = "JAVAPROG";
    private final String databaseConnection = "QS36F";
    private String database = "QS36F";
    private String header = "PURCHHDR";
    private String comment = "PURCHCMT";
    private String detail = "PURCHDET";
    private Connection AS400Con;   
    private Statement AS400s;
    private String mode;
    
    public PurchaseOrder(String poNum, String Library, String param3 ) throws SQLException, MyOwnException{
        PO_num=poNum;
        mode=param3;
        if(param3.equals("V")){
            header = "POPRTHDR";
            comment = "POPRTCMT";
            detail = "POPRTDET";
        }  

        if(!Library.equals(""))
            database=Library;
        retrievePO();
        PO_vendor=poHeader.get("POH_VENDOR");
        PO_vendorabv=poHeader.get("POH_VNDABV");
        PO_vendorEmail=poHeader.get("VCL_EMAIL");
        PO_Type= Integer.parseInt(poHeader.get("POH_TYPE"));         
        calculateTotal();
    }  
    /*
    *  Collects data for the PO
    */      
    public final void retrievePO() throws SQLException, MyOwnException{

        
        // Load the IBM Toolbox for Java JDBC driver.
        DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());

        // Get a connection to the database.  Since we do not
        // provide a user id or password, a prompt will appear.
        //
        // Note that we provide a default schema here so
        // that we do not need to qualify the table name in
        // SQL statements.
        //
        
        AS400Con = DriverManager.getConnection ("jdbc:as400://10.10.1.2/" + databaseConnection + ";user=" + user2 + ";password=" + password);
        // Create a statement
        AS400s = AS400Con.createStatement();    
            
        //System.out.println("Connection Started.");    
    //String query = "SELECT * FROM "+database+".PURCHHDR WHERE POH_ORDER='"+PO_num+"'"; 
        String query ="SELECT ph.*,VCL_EMAIL FROM "+database+"."+header+" ph \n" +
                    "LEFT JOIN "+"QS36F"+".VENDCONTCT ON VCL_VENDOR=POH_VENDOR AND VCL_PRIMRY='1'\n" +
                    "WHERE POH_ORDER='"+PO_num+"' ";  
        //System.out.println(query)    ;                                
        ResultSet rset = AS400s.executeQuery(query);       
        
        Map<String, String> headerMap = new HashMap();
       
        ArrayList <Map<String, String>> lines = new ArrayList();
        ResultSetMetaData rsmd = rset.getMetaData();
        
        int columnCount=rsmd.getColumnCount();
        if(rset.next()){
            for (int i=1; i <= columnCount;i++)
            {
                //System.out.println(rset.getString(i));
                if(rset.getString(i)==null)
                    headerMap.put(rsmd.getColumnName(i),"");
                else                     
                    headerMap.put(rsmd.getColumnName(i),rset.getString(i));
            }
        }
        else
            throw new MyOwnException("PO not Found");
        /*********************************************/
        /* Collect Vendor Data                       */
        /********************************************/
        Map<String, String> vendorMap = new HashMap();
        String vendor = headerMap.get("POH_VENDOR").replace("'","''");
        query = "SELECT * FROM "+"QS36F"+".CPVEND PD WHERE VND_VENDOR='"+vendor+"'"; 
        //System.out.println(query)    ;    
        rset = AS400s.executeQuery(query); 
        rsmd = rset.getMetaData();
        
        columnCount=rsmd.getColumnCount();
        if(rset.next()){
            for (int i=1; i <= columnCount;i++)
            {
                //System.out.println(rset.getString(i));
                if(rset.getString(i)==null)
                    vendorMap.put(rsmd.getColumnName(i),"");
                else                
                    vendorMap.put(rsmd.getColumnName(i),rset.getString(i));
            }
        }
        else
            throw new MyOwnException("Vendor not Found");                
        
        /*********************************************/
        /* Collect detail lines for the order       */
        /********************************************/
        if(Integer.parseInt(headerMap.get("POH_TYPE"))==1 || Integer.parseInt(headerMap.get("POH_TYPE"))==3)
            query = "SELECT 'D' AS \"TYPE\",PD.POD_LINE AS \"Line\",PD.* FROM "+database+"."+detail+" PD WHERE POD_ORDER='"+PO_num+"'"; 
        else if(Integer.parseInt(headerMap.get("POH_TYPE"))==2){
            query="SELECT 'D' AS \"TYPE\",pd.POD_LINE AS \"Line\",pd.*, m1.*, CAST ( CAST(PMO_LENGTH/12 AS FLOAT)*d1.DIE_ESTWGT AS FLOAT) AS \"Weight Per Piece\"   FROM "+database+"."+detail+" pd\n" +
                "LEFT JOIN "+"QS36F"+".PURMETAL m1 ON pd.POD_ORDER=m1.PMO_PURORD AND pd.POD_LINE=m1.PMO_POLINE\n" +
                "LEFT JOIN "+database+".DIEMAST d1 ON m1.PMO_DIE=d1.DIE_DIENO\n" +
                "WHERE POD_ORDER='"+PO_num+"'";
        }   
        //System.out.println(query);
        rset = AS400s.executeQuery(query);
        rsmd = rset.getMetaData();
        Map<String, String> detailMap ;
        //String[] detailrow = new String [20];
        columnCount=rsmd.getColumnCount();
        while(rset.next()){
            detailMap = new HashMap();
            for (int i=1; i <= columnCount;i++)
            {       
                //System.out.println(rsmd.getColumnName(i)+":"+rset.getString(i));
                
                if(rset.getString(i)==null){
                    detailMap.put(rsmd.getColumnName(i),"0");
                }
                else{
                    detailMap.put(rsmd.getColumnName(i),rset.getString(i));
                }
               // System.out.println(rsmd.getColumnName(i)+":"+detailMap.get(rsmd.getColumnName(i)));
            }
            
            lines.add(detailMap);

        }
        //System.out.println("***********************");
        
        query = "SELECT 'C' AS \"TYPE\",PD.POC_LINE as \"Line\",PD.* FROM "+database+"."+comment+" PD WHERE POC_ORDER='"+PO_num+"'"; 
        rset = AS400s.executeQuery(query);
        rsmd = rset.getMetaData();
        
        columnCount=rsmd.getColumnCount();
        while(rset.next()){
            detailMap = new HashMap();
            for (int i=1; i <= columnCount;i++)
            {       
                if(rset.getString(i)==null)
                    detailMap.put(rsmd.getColumnName(i),"");                
                else
                    detailMap.put(rsmd.getColumnName(i),rset.getString(i));
            }
            lines.add(detailMap);

        }      
        Collections.sort(lines, new MapComparator("Line"));
        poHeader=headerMap;
        detailLines=lines;
        poVendor=vendorMap;
        AS400s.close();
        AS400Con.close();
    }
    /*
    *  Collects detail line data along with die data if it is a Type =2
    */        
    public void retrieveMetal() throws SQLException
    {
           
        //System.out.println("Connection Started.");    
        String query ="SELECT * FROM "+database+".POPRTDET pd\n" +
                        "JOIN "+database+".PURMETAL m1 ON pd.POD_ORDER=m1.PMO_PURORD AND (pd.POD_LINE*2)+6=m1.PMO_POLINE \n" +
                        "WHERE POD_ORDER='"+PO_num+"'";

        ResultSet rset = AS400s.executeQuery(query);       
        
        Map<String, String> metalMap = new HashMap();
       
        ArrayList <Map<String, String>> lines = new ArrayList();
        ResultSetMetaData rsmd = rset.getMetaData();
        
        int columnCount=rsmd.getColumnCount();
        if(rset.next()){
            for (int i=1; i <= columnCount;i++)
            {
                metalMap.put(rsmd.getColumnName(i),rset.getString(i));
            }
        }
        
    }
    /*
    *   Get Tolerance in string format to dispaly on PDF
    */
    public String getTolerance(int line){
        String temper;
        temper="+"+detailLines.get(line).get("PMO_TOLPOS")+" / "+detailLines.get(line).get("PMO_TOLNEG");
        return temper;
    }
    /*
    *   Calculates the Order Total for the PO
    */    
    public final void calculateTotal(){
        for (int i = 0; i < detailLines.size(); i++) {
            if(detailLines.get(i).get("TYPE").equals("D")){
                double total=0;
                double quantity= Double.parseDouble(detailLines.get(i).get("POD_ORDQTY"));
                double cost= Double.parseDouble(detailLines.get(i).get("POD_COST"));
                total= Double.parseDouble(detailLines.get(i).get("POD_ORDAMT"));
                PO_total+=total;/*
                if(detailLines.get(i).get("POD_UM").equals("M"))
                {
                    total=(quantity*cost)/1000;
                }
                else if (detailLines.get(i).get("POD_UM").equals("C")){
                    total=(quantity*cost)/100;   
                }
                else if (detailLines.get(i).get("POD_UM").equals("LT")){
                    total=cost;   
                }               
                else{

                    total=(quantity*cost);
                }             
                PO_total+=total;  */                      
                }
        }          
    }
    /*
    *   Returns the PO Order Toal
    */
    public double getTotal(){
        //System.out.println("Total = "+PO_total);
        return PO_total;
    }
    /*
    *  Returns the Total Pieces for the PO
    */    
    public double getTotalPieces(){
        double ttl=0;
        for (int i = 0; i < detailLines.size(); i++) {
            if(detailLines.get(i).get("TYPE").equals("D")){
                double quantity= Double.parseDouble(detailLines.get(i).get("PMO_QTYORP"));
                ttl+=quantity;                        
            }
        }  
        return ttl;
    }
    /*
    *  Returns the Total Pounds for the PO
    */        
    public double getTotalPounds(){
        double ttl=0;
        for (int i = 0; i < detailLines.size(); i++) {
            if(detailLines.get(i).get("TYPE").equals("D")&&!detailLines.get(i).get("PMO_POTYPE").equals("1")&&!detailLines.get(i).get("PMO_POTYPE").equals("4")){
                double quantity= Double.parseDouble(detailLines.get(i).get("PMO_QTYORL"));
                ttl+=quantity;                        
            }
        }  
        return ttl;
    }  
    /*
    *  Returns the terms for the PO
    */        
    public String getTerms(){
        String terms=""; 
        if(Double.parseDouble(poVendor.get("VND_DSCPCT"))> 0.0){
            double discount_double=Double.parseDouble(poVendor.get("VND_DSCPCT"));
            int discount_int =(int) discount_double;
            terms=terms+discount_int+"%";
        }    
        terms=terms+" NET "+poVendor.get("VND_TERMS");
        return terms;
   }
    /*
    *  Converts the Due date to m/d/yy and returns it as a string
    */        
    public String getDueDate(){
    
        return "";
    }
    /*
    *  Converts the Order date to m/d/yy and returns it as a string
    */        
    public String getOrderDate() throws ParseException{
        
        String ordDate=poHeader.get("POH_ORDDTE");
        String newDate;
        DateFormat format;
        DateFormat format2 = new SimpleDateFormat("mm/dd/yy", Locale.ENGLISH);
        java.util.Date date;
        if(ordDate.length()==6)
            format = new SimpleDateFormat("yymmdd", Locale.ENGLISH);
        else{
            ordDate="0"+ordDate;
            format= new SimpleDateFormat("yymmdd", Locale.ENGLISH);
        }
        date = format.parse(ordDate);
        newDate=format2.format(date);        
        return newDate;
    }
    /*
    *  Converts the Due date to m/d/yy and returns it as a string for a specific line
    */        
    public String getDueDate(int line) throws ParseException{
        String dueDate=detailLines.get(line).get("POD_DUEDTE");
        String newDate;
        DateFormat format;
        DateFormat format2 = new SimpleDateFormat("m/dd/yy", Locale.ENGLISH);
        java.util.Date date;
        if(dueDate.length()==6)
            format= new SimpleDateFormat("yymmdd", Locale.ENGLISH);
        else{
            dueDate="0"+dueDate;
            format = new SimpleDateFormat("yymmdd", Locale.ENGLISH);
        }
        date = format.parse(dueDate);
        newDate=format2.format(date);        
        return newDate;
    }
    /*
    *  Retunrs the PO Num with requestor user id
    */        
    public String getPOnum_Header(){
        String po_String=PO_num.trim();
        
        if(!"".equals(poHeader.get("POH_REQINT").trim()))
            po_String=po_String+"-"+poHeader.get("POH_REQINT").trim();
        return po_String;
    }
    /*
    *  Checks if the entire PO is tax exempt
    */        
    public boolean checkTax(){
        boolean taxExempt=true;

            for (int i = 0; i < detailLines.size(); i++) {
                if(detailLines.get(i).get("TYPE").equals("D")){
                    if(!detailLines.get(i).get("POD_TAX").equals("Y")){
                        taxExempt=false;
                    }

                }
            }    
        return taxExempt;
    }
    /*
    *  Returns the string "Tax Exempt" if the line is exempt
    */        
    public String checkTax(int line ){
        String taxExempt="";
        if(line>-1){
            if(detailLines.get(line).get("POD_TAX").equals("Y")){
                        taxExempt="TAX EXEMPT";
                    }
        }
        return taxExempt;
    }
    /*
    *  Returns CTLACT and SUBACT in a string
    */        
    public String getVendorSubString(int line){
        String subString="";
        if(!"".equals(detailLines.get(line).get("POD_COM").trim()))
            subString=subString+detailLines.get(line).get("POD_COM");
        if(!"".equals(detailLines.get(line).get("POD_CTLACT").trim()))
            subString=subString+"-"+detailLines.get(line).get("POD_CTLACT");
        if(!"".equals(detailLines.get(line).get("POD_SUBACT").trim()))
            subString=subString+"-"+detailLines.get(line).get("POD_SUBACT");
        return subString;
    }
}




class pdfCreator {
    static PurchaseOrder po;
    static File printPDF;
    static File saveDirectory = new File("//QNTC/SE-FS1/Public/General (Deleted After 2 Weeks)/PurchaseOrders/TempPO/");
    //static File saveDirectory = new File("\\\\QNTC\\SE-FS1\\Public\\General (Deleted After 2 Weeks)\\PurchaseOrders\\TempPO");
    static File loadDirectory = new File("/javasource/PurchaseOrders/");
    static Document document;
    public String userID;
    private static PdfContentByte cb1;
    private static PdfWriter writer;
    private static FileOutputStream os;
    private static PdfReader reader ;
    private static com.itextpdf.text.Image inst;
    private static BaseFont bf;
    private Integer pageCount;
    pdfCreator(PurchaseOrder poObject) throws InterruptedException, IOException, DocumentException, ParseException,MyOwnException{
        init(poObject);
    }
    pdfCreator(PurchaseOrder poObject, String B) throws InterruptedException, IOException, DocumentException, ParseException,MyOwnException{

        userID=B;      
        init(poObject);
    }    
    private void init(PurchaseOrder poObject )throws InterruptedException, IOException, DocumentException, ParseException,MyOwnException{
        po=poObject;
        PageFormat pf = PrinterJob.getPrinterJob().defaultPage();            
        pageCount=1;
        if(new File("//SE-FS1/Public").exists()){
            saveDirectory = new File("//SE-FS1/Public/General (Deleted After 2 Weeks)/PurchaseOrders/TempPO/");
        }
        if(!saveDirectory.exists()){
            System.out.println("Save Directory does Not exists\n "+saveDirectory.getAbsolutePath());
            System.exit(0);
        }      
        if(!loadDirectory.exists()){
            loadDirectory = new File("//SE-FS1/Public/Java/Purchase_Orders/Files/");
            if(!loadDirectory.exists()){
                System.out.println("Load Directory does Not exists\n"+loadDirectory.getAbsolutePath());
                System.exit(0);
            }
        }           
        if(po.PO_Type==1 || po.PO_Type==3){
            /*if(userID!=null)
                printPDF = new File(saveDirectory.getPath()+"/"+userID+".pdf");
            else{
                printPDF = new File(saveDirectory.getPath()+"/"+"PO_"+po.PO_num+".pdf");
                printPDF.deleteOnExit();
            }*/

            //System.out.println(saveDirectory.getPath()+"/"+userID+".pdf");
            printPDF = new File(saveDirectory.getPath()+"/"+userID+".pdf");
            document = new Document(PageSize.LETTER, 0, 0, 0, 0);
            reader = new PdfReader(loadDirectory.getPath()+"/"+"CUSTOMPO.pdf");
            
            try{
                os = new FileOutputStream(printPDF);
            }
            catch (Exception ex) {
                int x=0;
                while(true){
                    if(!new File(saveDirectory.getPath()+"/"+userID+x+".pdf").exists()){
                        printPDF = new File(saveDirectory.getPath()+"/"+userID+x+".pdf");
                        os = new FileOutputStream(printPDF);                    
                        break;
                    }
                    x++;
                }
                
            }
            writer = PdfWriter.getInstance(document, os);  
            document.open();

            cb1 = writer.getDirectContent();
            PdfImportedPage page = writer.getImportedPage(reader, 1); 

            inst = com.itextpdf.text.Image.getInstance(page);
            int rotation = reader.getPageRotation(1);
            inst.setRotationDegrees(-rotation);
            inst.setAbsolutePosition(15, 25);

            Rectangle r = document.getPageSize();
            inst.scaleToFit(r.getWidth()-25, r.getHeight()-15);
            document.add(inst);  
            writeHeader();
            writeLines();
            
        }
        else if(po.PO_Type==2){
            if(userID!=null)
                printPDF = new File(saveDirectory.getPath()+"/"+userID+".pdf");
            else{
                printPDF = new File(saveDirectory.getPath()+"/"+"PO_"+po.PO_num+".pdf");
                printPDF.deleteOnExit();
            }
            document = new Document(new RectangleReadOnly(842,595), 0, 0, 0, 0);
            reader = new PdfReader(loadDirectory.getPath()+"/"+"PURMETALPO.pdf");
            try{
                os = new FileOutputStream(printPDF);
            }
            catch (Exception ex) {
                int x=0;
                while(true){
                    if(!new File(saveDirectory.getPath()+"/"+userID+x+".pdf").exists()){
                        printPDF = new File(saveDirectory.getPath()+"/"+userID+x+".pdf");
                        os = new FileOutputStream(printPDF);                    
                        break;
                    }
                    x++;
                }
                
            }            
            //os = new FileOutputStream(printPDF);
            writer = PdfWriter.getInstance(document, os);  
            document.open();

            cb1 = writer.getDirectContent();
            PdfImportedPage page = writer.getImportedPage(reader, 1); 

            inst = com.itextpdf.text.Image.getInstance(page);
            int rotation = reader.getPageRotation(1);
            //System.out.println(rotation);
            inst.setRotationDegrees(-rotation);            
            inst.setAbsolutePosition(50, 5);

            Rectangle r = document.getPageSize();
            //System.out.println("Width: "+r.getWidth()+"/Height :"+r.getHeight());
            inst.scaleToFit(r.getWidth()+200, r.getHeight()-15);    
            
            //inst.scaleToFit((float)100.00, (float)200.00);
            document.add(inst); 
            writeHeader();
            writeLines_metal();
        }  
        save();   
    }
    public final void writeHeader() throws DocumentException, IOException, ParseException{
        bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, false);
    float fSize = 10;
    //float fSize2 = 11;
        cb1.setFontAndSize(bf, fSize);
        cb1.beginText();
        
        if(po.PO_Type==1 || po.PO_Type==3){
            cb1.setFontAndSize(bf, 14);
            ///cb1.showTextAligned(Element.ALIGN_LEFT, "Vanedor Name", 100f, 675f, 0f);
            cb1.showTextAligned(Element.ALIGN_RIGHT, po.getPOnum_Header(), 593f, 760f, 0f);
            cb1.setFontAndSize(bf, fSize);
            if(!po.poVendor.get("VND_NAME").equals("")){
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poVendor.get("VND_NAME"), 85f, 667f, 0f);
                //To 2
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poVendor.get("VND_ADDR1"), 85f, 655f, 0f);
                int yposition=643;
                //To 3
                if(!po.poVendor.get("VND_ADDR2").equals("")){
                    cb1.showTextAligned(Element.ALIGN_LEFT, po.poVendor.get("VND_ADDR2"), 85f, yposition, 0f);   
                    yposition=yposition-12;
                }
                if(!po.poVendor.get("VND_ADDR3").equals("")){
                    cb1.showTextAligned(Element.ALIGN_LEFT, po.poVendor.get("VND_ADDR3"), 85f, yposition, 0f);   
                    yposition=yposition-12;
                }
                if(!po.poVendor.get("VND_ADDR4").equals("")){
                    cb1.showTextAligned(Element.ALIGN_LEFT, po.poVendor.get("VND_ADDR4"), 85f, yposition, 0f);   
                    yposition=yposition-12;
                }
            }
            //cb1.showTextAligned(Element.ALIGN_LEFT, "CUSTOM ALUMINUM PRODUCTS", 355f, 667f, 0f);
            if(!"".equals( po.poHeader.get("POH_SHPADR").trim())){
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPATN").trim(), 380f, 679f, 0f);
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPNAM").trim(), 380f, 667f, 0f);
                //To 2
                //cb1.showTextAligned(Element.ALIGN_LEFT, "500 WEST DIVISION STREET", 355f, 645f, 0f);
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPADR").trim(), 380f, 655f, 0f);
                //To 3
                //cb1.showTextAligned(Element.ALIGN_LEFT, "SOUTH ELGIN, IL 60177", 355f, 625f, 0f);
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPCTY").trim()+", "+po.poHeader.get("POH_SHPSTA").trim()+' '+po.poHeader.get("POH_SHPZIP").trim(), 380f, 643f, 0f);
            }
            //write Date
            cb1.showTextAligned(Element.ALIGN_RIGHT, po.getOrderDate(), 139f, 600f, 0f);

            cb1.setFontAndSize(bf, 10);  
            cb1.showTextAligned(Element.ALIGN_LEFT, "SEE BELOW", 233f, 600f, 0f);
            cb1.setFontAndSize(bf, fSize);  
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_BUYER"), 145f, 577f, 0f);

            cb1.showTextAligned(Element.ALIGN_LEFT, po.getTerms(), 330f, 577f, 0f);        
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_FOB"), 320f, 600f, 0f);                
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPVIA"), 511f, 600f, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_VIAACT"), 470, 577f, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT, "Page "+pageCount, 550f, 700f, 0f);
        }         
        else if(po.PO_Type==2){
            cb1.setFontAndSize(bf, 10);
            cb1.showTextAligned(Element.ALIGN_LEFT, po.getPOnum_Header(), 165f, 480f, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_VENDOR"), 135f, 462f, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT, po.getOrderDate(), 155f, 442f, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT, po.getTerms(), 300f, 442f, 0f); 
            if(!"".equals( po.poHeader.get("POH_SHPADR").trim()))
            {
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPADR").trim(), 470f, 442f, 0f); 
                cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPCTY").trim()+", "+po.poHeader.get("POH_SHPSTA").trim()+' '+po.poHeader.get("POH_SHPZIP").trim(), 470f, 432f, 0f);
            }
            /*cb1.showTextAligned(Element.ALIGN_LEFT, "640 Division ", 470f, 442f, 0f); 
            cb1.showTextAligned(Element.ALIGN_LEFT, "South Elgin, IL 60177", 470f, 432f, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT, "PAGE "+pageCount, 725f, 458f, 0f);*/
        
        }
        cb1.endText();
        
    }
    public final void writeLines() throws DocumentException, IOException, ParseException{
        bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, false);
    float fSize = 10;
    float fSize2 = 8;
        cb1.setFontAndSize(bf, fSize);
        cb1.beginText();
        
        DecimalFormat df = new DecimalFormat("#,###.00");
        DecimalFormat formatter = new DecimalFormat("#,###");
        int yasix=525;
        for (int i = 0; i < po.detailLines.size(); i++) {

            String Type=po.detailLines.get(i).get("TYPE").trim();
            if(i%12 == 0 && i!=0){
                if(!po.checkTax()){
                    cb1.setFontAndSize(bf, 14);
                    cb1.showTextAligned(Element.ALIGN_RIGHT, "X", 44.8f, 49, 0f);
                }   
                newPage();
                yasix=525;
                cb1.setFontAndSize(bf, fSize);
                cb1.beginText();
            }
            if(Type.equals("D")){                
            cb1.showTextAligned(Element.ALIGN_RIGHT,formatter.format(Integer.parseInt( po.detailLines.get(i).get("POD_ORDQTY"))), 65f, yasix, 0f);                    
            cb1.showTextAligned(Element.ALIGN_RIGHT, po.detailLines.get(i).get("POD_ITEM"), 182, yasix, 0f);
            if(po.detailLines.get(i).get("POD_ITEM").trim().length()>12)
                yasix=yasix-10;
            cb1.showTextAligned(Element.ALIGN_LEFT, po.detailLines.get(i).get("POD_DESC"), 169f, yasix, 0f); 
            cb1.showTextAligned(Element.ALIGN_RIGHT, df.format(Double.parseDouble(po.detailLines.get(i).get("POD_STDCST"))), 464f, yasix, 0f);
            cb1.showTextAligned(Element.ALIGN_RIGHT, df.format(Double.parseDouble(po.detailLines.get(i).get("POD_ORDAMT"))), 586f, yasix, 0f);
            cb1.showTextAligned(Element.ALIGN_CENTER, po.detailLines.get(i).get("POD_UM")+".", 480f, yasix, 0f);
            
            
            /* Second Line for this item*/
            cb1.setFontAndSize(bf, fSize2);
            //if(po.detailLines.get(i).get("POD_VNDPRT")!=null && !po.detailLines.get(i).get("POD_VNDPRT").isEmpty())
            if(po.detailLines.get(i).get("POD_VNDPRT").trim().length()>0)
            {
                yasix=yasix-10;
                cb1.showTextAligned(Element.ALIGN_LEFT, "Vendor Part No.: "+po.detailLines.get(i).get("POD_VNDPRT"), 170f, yasix, 0f);
            }
            cb1.setFontAndSize(bf, fSize2);
            //if(po.detailLines.get(i).get("POD_VNDPRT")!=null && !po.detailLines.get(i).get("POD_VNDPRT").isEmpty())
            if(po.detailLines.get(i).get("POD_QUOTE").trim().length()>0)
            {
                yasix=yasix-10;
                cb1.showTextAligned(Element.ALIGN_LEFT, "Quote: "+po.detailLines.get(i).get("POD_QUOTE"), 170f, yasix, 0f);
            }            
            yasix=yasix-10;
            cb1.showTextAligned(Element.ALIGN_LEFT, "Due Date: ", 170f, yasix, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT, po.getDueDate(i), 216f, yasix, 0f);
            cb1.showTextAligned(Element.ALIGN_LEFT,po.checkTax(i), 336f, yasix, 0f);
                       
            cb1.showTextAligned(Element.ALIGN_LEFT, po.getVendorSubString(i), 260f, yasix, 0f);
              
            cb1.setFontAndSize(bf, fSize);       
            }
            else if(Type.equals("C")){
                cb1.showTextAligned(Element.ALIGN_LEFT, po.detailLines.get(i).get("POC_COMMNT"), 169f, yasix, 0f);   

            }
            yasix=yasix-25;
        }     
        if(po.poHeader.get("POH_SHPCMT").length()>0){
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPCMT").substring(0,42), 169f, yasix, 0f);   
            yasix=yasix-25;
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPCMT").substring(43), 169f, yasix, 0f);   
        }
        writeFooter();
    }
    public void writeFooter(){
        if(po.checkTax()){
            cb1.setFontAndSize(bf, 14);
            cb1.showTextAligned(Element.ALIGN_RIGHT, "X", 44.8f, 49, 0f);
        }
        DecimalFormat df = new DecimalFormat("#,###.00");
        DecimalFormat formatter = new DecimalFormat("#,###");
        cb1.setFontAndSize(bf, 14); 
        cb1.showTextAligned(Element.ALIGN_RIGHT, df.format(po.getTotal()), 586f, 40, 0f);
        cb1.endText();    
    }
    public final void writeLines_metal() throws DocumentException, IOException, ParseException{
        bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, false);
    float fSize = 8;
    float fSize2 = 8;
        cb1.setFontAndSize(bf, fSize);
        cb1.beginText();
        
        //DecimalFormat df = new DecimalFormat("0.00##");
        DecimalFormat df = new DecimalFormat("#,###.00");
        DecimalFormat df_price = new DecimalFormat("#,###.000");
        DecimalFormat lengthdf = new DecimalFormat("0.000");
        
        DecimalFormat formatter = new DecimalFormat("#,###");
        int yasix=380;
        for (int i = 0; i < po.detailLines.size(); i++) {
            String Type=po.detailLines.get(i).get("TYPE").trim();
            if(i%12 == 0 && i!=0){
                cb1.showTextAligned(Element.ALIGN_LEFT,"Continued...", 580f, 67, 0f); 
                //writeFooterMessage();
                newPage();
                yasix=380;
                cb1.setFontAndSize(bf, fSize);
                cb1.beginText();
            }
            if(Type.equals("D")){      
                if(po.detailLines.get(i).get("PMO_POTYPE").equals("1")||po.detailLines.get(i).get("PMO_POTYPE").equals("4"))
                {   
                    cb1.showTextAligned(Element.ALIGN_RIGHT,po.detailLines.get(i).get("PMO_DIE"), 125f, yasix, 0f); 
                    if(po.detailLines.get(i).get("PMO_POTYPE").equals("1"))
                        cb1.showTextAligned(Element.ALIGN_LEFT,"TOOLING", 240f, yasix, 0f); 
                    else
                        cb1.showTextAligned(Element.ALIGN_LEFT,po.detailLines.get(i).get("PMO_ITMDES"), 240f, yasix, 0f);                             
                    
                    cb1.showTextAligned(Element.ALIGN_RIGHT,formatter.format(Integer.parseInt(po.detailLines.get(i).get("PMO_QTYORP"))), 465f, yasix, 0f);
                    /*cb1.showTextAligned(Element.ALIGN_RIGHT, "$", 563f, yasix, 0f);  
                    cb1.showTextAligned(Element.ALIGN_RIGHT,df_price.format(Double.parseDouble(po.detailLines.get(i).get("PMO_PCTOTP"))), 623f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT, "$", 634f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT, df.format(Double.parseDouble(po.detailLines.get(i).get("POD_ORDAMT"))), 706f, yasix, 0f); 
                    if(Integer.parseInt(po.detailLines.get(i).get("PMO_RCVQTP"))>=Integer.parseInt(po.detailLines.get(i).get("PMO_QTYORP")))
                        cb1.showTextAligned(Element.ALIGN_LEFT, "*Complete*", 715f, yasix, 0f);
                    else
                        cb1.showTextAligned(Element.ALIGN_LEFT, po.getDueDate(i), 730f, yasix, 0f);*/
                }
                else if(po.detailLines.get(i).get("PMO_POTYPE").equals("2")||po.detailLines.get(i).get("PMO_POTYPE").equals("3"))
                {   
                    cb1.showTextAligned(Element.ALIGN_RIGHT,po.detailLines.get(i).get("PMO_DIE"), 125f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT, lengthdf.format(Double.parseDouble(po.detailLines.get(i).get("PMO_LENGTH"))), 175f, yasix, 0f);
                    if(po.detailLines.get(i).get("PMO_POTYPE").equals("2"))
                        cb1.showTextAligned(Element.ALIGN_RIGHT, "SAMPLES", 175f, yasix-10, 0f);

                    cb1.showTextAligned(Element.ALIGN_RIGHT,po.detailLines.get(i).get("PMO_ALLOY"), 200f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_LEFT,po.detailLines.get(i).get("PMO_TEMPER"), 225f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_CENTER,po.getTolerance(i), 278f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT,lengthdf.format(Double.parseDouble(po.detailLines.get(i).get("Weight Per Piece"))), 375f, yasix, 0f);
                    cb1.showTextAligned(Element.ALIGN_RIGHT,formatter.format(Integer.parseInt(po.detailLines.get(i).get("PMO_QTYORL"))), 420f, yasix, 0f);
                    cb1.showTextAligned(Element.ALIGN_RIGHT,formatter.format(Integer.parseInt(po.detailLines.get(i).get("PMO_QTYORP"))), 465f, yasix, 0f);

                    cb1.showTextAligned(Element.ALIGN_RIGHT,po.detailLines.get(i).get("PMO_LMECHG"), 505f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT,po.detailLines.get(i).get("PMO_LBTOTP"), 550f, yasix, 0f); 

                 /* cb1.showTextAligned(Element.ALIGN_RIGHT, "$", 563f, yasix, 0f);  
                    cb1.showTextAligned(Element.ALIGN_RIGHT,df_price.format(Double.parseDouble(po.detailLines.get(i).get("PMO_PCTOTP"))), 623f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT, "$", 634f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT, df.format(Double.parseDouble(po.detailLines.get(i).get("POD_ORDAMT"))), 706f, yasix, 0f); 
                    if(Integer.parseInt(po.detailLines.get(i).get("PMO_RCVQTP"))>=Integer.parseInt(po.detailLines.get(i).get("PMO_QTYORP")))
                        cb1.showTextAligned(Element.ALIGN_LEFT, "*Complete*", 715f, yasix, 0f);
                    else
                        cb1.showTextAligned(Element.ALIGN_LEFT, po.getDueDate(i), 730f, yasix, 0f);*/                    

                }  
                    cb1.showTextAligned(Element.ALIGN_RIGHT, "$", 560f, yasix, 0f);  
                    try{
                        cb1.showTextAligned(Element.ALIGN_RIGHT,df_price.format(Double.parseDouble(po.detailLines.get(i).get("PMO_PCTOTP"))), 618f, yasix, 0f);
                    } 
                    catch(java.lang.NumberFormatException ex){
                        cb1.showTextAligned(Element.ALIGN_RIGHT,df_price.format(Double.parseDouble("0")), 618f, yasix, 0f);
                    }
                    cb1.showTextAligned(Element.ALIGN_RIGHT, "$", 627f, yasix, 0f); 
                    cb1.showTextAligned(Element.ALIGN_RIGHT, df.format(Double.parseDouble(po.detailLines.get(i).get("POD_ORDAMT"))), 698f, yasix, 0f); 
                    if(Integer.parseInt(po.detailLines.get(i).get("PMO_RCVQTP"))>=Integer.parseInt(po.detailLines.get(i).get("PMO_QTYORP")))
                        cb1.showTextAligned(Element.ALIGN_LEFT, "*Complete*", 710f, yasix, 0f);
                    else
                        cb1.showTextAligned(Element.ALIGN_LEFT, po.getDueDate(i), 725f, yasix, 0f);
            }
            else if(Type.equals("C")){
                cb1.showTextAligned(Element.ALIGN_LEFT, po.detailLines.get(i).get("POC_COMMNT"), 165f, yasix, 0f);           
            }
            yasix=yasix-23;
        }     
        if(po.poHeader.get("POH_SHPCMT").length()>0){
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPCMT").substring(0,42), 169f, yasix, 0f);   
            yasix=yasix-23;
            cb1.showTextAligned(Element.ALIGN_LEFT, po.poHeader.get("POH_SHPCMT").substring(43), 169f, yasix, 0f);   
        }
        
        writeFooter_metal();
    }    
    public void writeFooter_metal(){
        DecimalFormat df = new DecimalFormat("#,###.00");
        DecimalFormat df_price = new DecimalFormat("#,###.000");
        DecimalFormat lengthdf = new DecimalFormat("0.000##");
        DecimalFormat formatter = new DecimalFormat("#,###"); 
       
         if(po.checkTax()){
            cb1.setFontAndSize(bf, 10);
            //cb1.showTextAligned(Element.ALIGN_RIGHT, "X", 31.5f, 25, 0f);
        } 
        writeFooterMessage();
        cb1.setFontAndSize(bf, 10);
        cb1.showTextAligned(Element.ALIGN_LEFT,"Totals:", 275f, 85, 0f); 
        cb1.showTextAligned(Element.ALIGN_RIGHT, formatter.format(po.getTotalPieces()), 465f, 85, 0f);
        cb1.showTextAligned(Element.ALIGN_RIGHT, formatter.format(po.getTotalPounds()), 420f, 85, 0f);
        cb1.showTextAligned(Element.ALIGN_RIGHT, "$", 627f, 85, 0f); 
        cb1.showTextAligned(Element.ALIGN_RIGHT, df.format(po.getTotal()), 698f, 85, 0f);
        cb1.endText();        
    
   
    }
    private void writeFooterMessage(){
        cb1.setFontAndSize(bf, 10);
        cb1.showTextAligned(Element.ALIGN_LEFT,"*All Parts must pass sample approval prior to shipping production*", 125f, 60, 0f); 
        cb1.showTextAligned(Element.ALIGN_LEFT,"Parts must conform to prints as listed", 125f, 45, 0f); 
        cb1.showTextAligned(Element.ALIGN_LEFT,"Please acknowledge receipt of order along with price and delivery", 125f, 30, 0f); 
    }
    /*
    *  Insert New Page
    */    
    private void newPage()throws DocumentException,IOException, ParseException{
        cb1.endText();
        pageCount++;
        document.newPage();                                                   
        document.add(inst);
        writeHeader();
    }
    /*
    *   Send document
    */
    public final void save() throws DocumentException, IOException,MyOwnException{
        document.close();
        os.close();
        reader.close();
        System.out.println("File location: "+getAbsolutePath()); 
    }
    public static String getCurrentTimeStamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
    Date now;
    now = new Date();
    String strDate = sdf.format(now);
    return strDate;
}
   public  String getAbsolutePath() throws MyOwnException{
       if(printPDF.exists())
            return printPDF.getAbsolutePath();
       else
           throw new MyOwnException("PDF not Found");
    }
   public  void delete(){
   
       printPDF.delete();
   }
}



