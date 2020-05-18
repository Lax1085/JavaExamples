package fabSetup;
import java.io.*;
import javax.print.attribute.*;

/**RUNJVA CLASS('/javasource/fab jobsheets/setup_sheets.jar') 
 * RUNJVA CLASS('/javasource/fab jobsheets/setup_sheets.jar') PARM('902')
 * java PDF_WaterMark '//QNTC/cust05/prints/cadspdf/cad/7000-7999/7833.pdf                                               x' 'AL       x'
 * CALL PGM(TESTFAB/TEST) PARM('937' 'AL' 'NETPRT36' '1' 'TESTFAB')
 * @author alopez
 */
public class SetupSheets {
    static fabSheet PDF;
    static fabJob Job;
    static String jobNo;
    static String userID;
    static String shift;
    static String directory;
    static String printer;
    static String library;
    static String sheet;
    static String[] arguments;
    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     * @throws com.itextpdf.text.DocumentException
     * @throws java.text.ParseException
     */
    public static void main(String[] args) {
       sheet="";
        // TODO code application logic here
        if(args.length > 0 ){
            jobNo = args[0];
            sheet=args[1];
            userID = args[2];
            printer = args[3];
            shift = args[4];
            library = args[5];
        }
        else{
            System.out.println("Not enough arguments");
            System.exit(0);
        } 
        arguments=args;
        
        //jobNo="902";
        //userID="AL";
        //System.out.println("Arg[1]: "+user);
        //PDF = new fabSheet("//QNTC/CUST05/Prints/CADSPDF/Cad/13000-13999/13114.pdf", "AL");  
        
        if(new File("//10.10.1.2/root").exists()){
            //System.out.println("//10.10.1.2/root Exits");
            directory="//10.10.1.2/root/esdi/websmart";
        }
        else if(new File("/esdi/websmart").exists()){
            directory="/esdi";
            //System.out.println("/esdi Exits");
        }
        
        try
        {                    
            //jobNo="1580";
            Job = new fabJob(jobNo, library);
            PDF = new fabSheet(Job, userID,shift,directory, sheet);  
            System.out.println(PDF.targetFile.getPath());
            if(PDF.targetFile.exists()){
              //PDF.PrintPDF(printer);
              //PDF.targetFile.delete();
            }
        }
        catch(Exception ex){    
            System.out.println("Error: "+ex);
            System.out.println("Printing stack trace:");
            StackTraceElement[] elements = ex.getStackTrace();
            for (int i = 1; i < elements.length; i++) {
                StackTraceElement s = elements[i];
                System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
            + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");               
            }                              
                System.out.println(ex.getStackTrace()[0].getLineNumber());                
        }        
    }
}


