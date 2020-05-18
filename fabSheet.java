package fabSetup;

import cads_stamper.Stamper;
import cads_stamper.StamperException;
import java.util.*;
import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import com.ibm.as400.access.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import javax.print.*;
import javax.print.attribute.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import javax.print.attribute.standard.JobName;

public class fabSheet {
    
    public File printPDF;
    public String userID;
    public fabJob Job;
    public String shift;    
    public String saveDirectoryPath;
    public File targetFile;
    public File setupSheet;
    public File qcSheet;
    static String Mode;
    private PdfContentByte cb1;
    private PdfWriter writer;
    private FileOutputStream os;
    private PdfReader reader;    
    private com.itextpdf.text.Image inst;
    private BaseFont bf;
    private Integer pageCount;
    private Document document;
    private PdfCopy copy;
    private FileOutputStream out;
    private PdfReader formReader;
    private PdfReader print;
    private Stamper stamper;
    private AS400 system;
        
    public fabSheet(fabJob param1, String param2, String param3,String param5, String param6 )throws SQLException,InterruptedException,PrinterException,IOException, DocumentException, ParseException,AS400SecurityException{                
        saveDirectoryPath=param5;
        Mode=param6.trim();
        if(Mode.equals("QC"))
            targetFile= new File(saveDirectoryPath+"/Fab JobSheets/QC Sheets/"); 
        else
            targetFile= new File(saveDirectoryPath+"/Fab JobSheets/Job Sheets/"); 
        if (!targetFile.exists()){
            if(targetFile.mkdirs())
                System.out.println(targetFile.getPath()+"Created");
            else 
                System.out.println(targetFile.getPath()+" NOT Created!!");
             // If you require it to make the entire directory path including parents,
             // use directory.mkdirs(); here instead.
        }            
        setupSheet= new File(saveDirectoryPath+"/IsoFiles/Form 7.2.1-8 Setup Quality Check (2).pdf"); 
        qcSheet= new File(saveDirectoryPath+"/IsoFiles/Form 7.2.1-9 QC Quality Check.pdf");         
        Job=param1;
        userID=param2;
        shift=param3;        
        if(!validatePaths()){              
            throw new java.lang.Error("Sheet/Prints not Found");            
        }
 
        try{            
            createPDF(targetFile);                                 
        }
        catch(Exception ex){
            ex.printStackTrace();
            throw new java.lang.Error("this is very bad");
        }
    }     
    
    public void createPDF(File dest)throws IOException, DocumentException, AS400SecurityException, ParseException, StamperException
    {
        system = new AS400("10.10.1.2", "JAVAPROG", "JAVAPROG");
        //System.out.println("Creating PDF");
        //System.out.println("OUT: "+dest);
        document = new Document(PageSize.LETTER, 0, 0, 0, 0);
        out = new FileOutputStream(dest.getPath());
        writer = PdfWriter.getInstance(document, out); 
        document.open();  
        cb1 = writer.getDirectContent();   
        cb1.beginText();
        
  //      System.out.println("*******************START********************************");
        for (Map.Entry<String,Map<String,String>> entry : Job.operations.entrySet())
        {
            //System.out.println("Entry:" +entry.getKey());
            //System.out.println("*************"+"Entry:" +entry.getKey()+"***************************");
            
            if(Mode.equals("S"))
                addsetupSheet(entry.getKey());
            else 
                addqcSheet(entry.getKey());
            if(checkCutOp(entry.getValue()))
            {
                //add fab print
                processPrint(entry.getKey(),Job.fabPrint);
            }
            else
                processPrint(entry.getKey(),Job.diePrint);
           
        }     
        save();
        
    }
    private boolean checkCutOp(Map <String,String>val)
    {
        String Operation=val.get("JOP_OPER");
        
             if(Operation.toLowerCase().contains("cut"))
                return true;
             else
                  return false;
    }
    private void addsetupSheet(String oprcmt)throws DocumentException,IOException, ParseException, AS400SecurityException{
        if(writer.getCurrentPageNumber()>1)
            newPage();
        formReader = new PdfReader(setupSheet.getPath());
        PdfImportedPage page = writer.getImportedPage(formReader, 1);
        inst = com.itextpdf.text.Image.getInstance(page);
        int rotation = formReader.getPageRotation(1);
        inst.setRotationDegrees(-rotation);
        inst.setAbsolutePosition(15, 25);

        Rectangle r = document.getPageSize();
        inst.scaleToFit(r.getWidth()-25, r.getHeight()-30);
        document.add(inst);  
        String timeStamp=new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).format(new Date());
        bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, false);
        Font f = new Font(FontFamily.HELVETICA, 17); 
          
        int lineLength=10;
        int position=10;
        int y=700;
        //System.out.println(Job.Customer.length());
        if(Job.Customer.length()>lineLength){
            int total = Job.Customer.length() / lineLength;
            int remainder = Job.Customer.length() % lineLength; 
            y=y+((total)*13);                        
            cb1.setFontAndSize(bf,8);
            cb1.showTextAligned(Element.ALIGN_RIGHT, "("+Job.CustomerNo+")", 130, y+9, 0);            
            int start=0;
            for (int i=1; i<=total;i++){
                //System.out.println(y);    
                if(position>lineLength)
                    start=position;
                else 
                    start=0;
                
                while(true){
                    //System.out.println(Job.Customer.length());
                    //System.out.println(position);
                    //System.out.println(Integer.compare(position, Job.Customer.length())); 
                    //System.out.println("*******************");
                    if(Integer.compare(position, Job.Customer.length()-1)>0 )
                        break;
                    else if(Job.Customer.charAt(position)==' ')
                        break;
                    else
                        position++;
                }
                if(position>Job.Customer.length())
                    break;
                cb1.setFontAndSize(bf,8);        
                //System.out.println(Job.Customer.substring(start,position));
                cb1.showTextAligned(Element.ALIGN_LEFT, Job.Customer.substring(start,position), 105, y, 0);
                y=y-9;
                position++;
            }
        }
        else{
            cb1.setFontAndSize(bf,10);
            cb1.showTextAligned(Element.ALIGN_LEFT, Job.Customer, 105, 718, 0);
            cb1.setFontAndSize(bf,8);
            cb1.showTextAligned(Element.ALIGN_RIGHT, "("+Job.CustomerNo+")", 130, 725, 0);
        }
        cb1.setFontAndSize(bf,12);
        cb1.showTextAligned(Element.ALIGN_LEFT, timeStamp, 260, 718, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, shift, 390, 718, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, Job.Die, 245, 685, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, Job.MasterExt, 105, 685, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, Job.Length, 125, 655, 0);
        
        cb1.showTextAligned(Element.ALIGN_RIGHT,"Job:"+Job.jobNo+"-"+oprcmt, 580, 750, 0);
    }
    private void addqcSheet(String oprcmt)throws DocumentException,IOException, ParseException, AS400SecurityException{
        if(writer.getCurrentPageNumber()>1)
            newPage();
        formReader = new PdfReader(qcSheet.getPath());
        PdfImportedPage page = writer.getImportedPage(formReader, 1);
        inst = com.itextpdf.text.Image.getInstance(page);
        int rotation = formReader.getPageRotation(1);
        inst.setRotationDegrees(-rotation);
        inst.setAbsolutePosition(15, 25);

        Rectangle r = document.getPageSize();
        inst.scaleToFit(r.getWidth()-25, r.getHeight()-30);
        document.add(inst);  
        String timeStamp=new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).format(new Date());
        bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, false);
        Font f = new Font(FontFamily.HELVETICA, 17); 
          

        int lineLength=10;
        int position=10;
        int y=705;        
        if(Job.Customer.length()>lineLength){
            int total = Job.Customer.length() / lineLength;
            int remainder = Job.Customer.length() % lineLength; 
            //System.out.println(total);
            //System.out.println(remainder);
            y=y+((total)*13);                  
            cb1.setFontAndSize(bf,8);
            cb1.showTextAligned(Element.ALIGN_RIGHT, "("+Job.CustomerNo+")", 130, y+9, 0);            
            int start=0;
            for (int i=1; i<=total;i++){
                //System.out.println(y);    
                if(position>lineLength)
                    start=position;
                else 
                    start=0;
                
                while(true && position<Job.Customer.length()){                    
                    if(Integer.compare(position, Job.Customer.length()-1)>0 ){
                        break;
                    }
                    else if(Job.Customer.charAt(position)==' ')
                        break;
                    else
                        position++;
                }
                if(position>Job.Customer.length() )
                    break;
                cb1.setFontAndSize(bf,8);        
                cb1.showTextAligned(Element.ALIGN_LEFT, Job.Customer.substring(start,position), 105, y, 0);
                y=y-9;
                position++;
            }
        }
        else{
            cb1.setFontAndSize(bf,10);
            cb1.showTextAligned(Element.ALIGN_LEFT, Job.Customer, 105, 718, 0);
            cb1.setFontAndSize(bf,8);
            cb1.showTextAligned(Element.ALIGN_RIGHT, "("+Job.CustomerNo+")", 130, 730, 0);
        }
        cb1.setFontAndSize(bf,12);
        cb1.showTextAligned(Element.ALIGN_LEFT, timeStamp, 265, 718, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, shift, 390, 718, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, Job.Die, 220, 688, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, Job.MasterExt, 80, 688, 0);
        cb1.showTextAligned(Element.ALIGN_LEFT, Job.Length, 110, 655, 0);
        
        cb1.showTextAligned(Element.ALIGN_RIGHT,"Job:"+Job.jobNo+"-"+oprcmt, 580, 750, 0);
    }       
    private void processPrint(String oprcmt, File printFile) throws IOException, AS400SecurityException, AS400SecurityException, IOException, DocumentException,ParseException, DocumentException, StamperException
    {
        if(!printFile.exists())
        {
            newPage();
            printNotFound();
            return;
        }   
        if(print==null){
            try{
                stamper= new Stamper(printFile,userID);            
                printFile=stamper.target;
            }
            catch(Exception ex){
                System.out.println("unable to stamp file"+printFile.getPath());
            }        
            try{
                print = new PdfReader(printFile.getPath());
            }
            catch(IOException ex){
                IFSFile filet_in = new IFSFile(system, printFile.getPath());
                IFSFileInputStream fis_in = new IFSFileInputStream (filet_in, IFSFileInputStream.SHARE_READERS);        
                print = new PdfReader(fis_in);
            }       
        }
        //System.out.println(printFile.getAbsolutePath());
        pageCount=print.getNumberOfPages();
        for(int i=1;i<=pageCount;i++){
            if(i==1)
                newDiePrintPage(i);
            addPrint(oprcmt,i);    
            if(i<pageCount){
                newDiePrintPage(i);
            }            
        }                   
    }
    private void addPrint(String oprcmt,int pageNumber)throws DocumentException,IOException, ParseException, AS400SecurityException{   
        
        PdfImportedPage page = writer.getImportedPage(print, pageNumber);
        int sourceOrientation=page.getRotation();
        inst = com.itextpdf.text.Image.getInstance(page);
        int rotation = print.getPageRotation(1);
        Rectangle r = document.getPageSize();
        
        if(sourceOrientation==0){
            float scalerWidth = ((document.getPageSize().getWidth() - document.leftMargin()
                   - document.rightMargin() - 10) / inst.getWidth()) * 100;
            float scalerHeight = ((document.getPageSize().getHeight() - 25
                   - 25) / inst.getHeight()) * 100;        
            //inst.scalePercent(scalerWidth, scalerHeight);
        }
        else{
            float scalerWidth = ((document.getPageSize().getWidth() - document.leftMargin()
                   - document.rightMargin() - 10) / inst.getWidth()+100) * 100;
            float scalerHeight = ((document.getPageSize().getHeight() - document.leftMargin()
                   - document.rightMargin() - 5) / inst.getHeight()) * 100;        
            scalerHeight=70;
            //inst.scalePercent(scalerWidth, scalerHeight);            
            inst.setRotationDegrees(-rotation);
        }
        
        inst.scaleToFit((float)(r.getWidth()-7), (float)(r.getHeight()-35));        
        inst.setAbsolutePosition(20, 15);
        document.add(inst); 
        
        String timeStamp=new SimpleDateFormat("MM/dd/yy HH:mm:ss a", Locale.ENGLISH).format(new Date());
        
        bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, false);
        Font f = new Font(FontFamily.HELVETICA, 17); 
        Phrase p = new Phrase("User: "+userID+" @ "+timeStamp, f);
        
        float fontsize = 24;     
        
        cb1.setFontAndSize(bf,fontsize);
        //cb1.showTextAligned(Element.ALIGN_RIGHT, "User: "+userID+" @ "+timeStamp, 35, 600, 90);
        cb1.setFontAndSize(bf,24);
        cb1.showTextAligned(Element.ALIGN_RIGHT,"Job:"+Job.jobNo+"-"+oprcmt, (r.getWidth()-150), (r.getHeight()-35), 0);
    }  
    private void newDiePrintPage(int page)throws DocumentException,IOException, ParseException{
        cb1.endText();

        if(getOrientation(print,page)==0)
            document.setPageSize(print.getPageSize(page).rotate());//, 0, 0, 0, 0);
        else
            document.setPageSize(print.getPageSize(page));//, 0, 0, 0, 0); 
              
        document.newPage();                                                           
        cb1.beginText();
    }    
    
    /* Get page orientation */
    private int getOrientation(PdfReader src, int pageNumber){
        Rectangle rectangle = src.getPageSizeWithRotation(pageNumber);        
        if(rectangle.getHeight() >= rectangle.getWidth() || src.getPageRotation(pageNumber)==0 ){            
            return PageFormat.PORTRAIT;
        }
        else{            
            return PageFormat.LANDSCAPE;   
        }
    }    
    private void printNotFound(){
        Rectangle rec = new Rectangle(document.getPageSize());
        cb1.setFontAndSize(bf,22);
        cb1.showTextAligned(Element.ALIGN_CENTER,"Die Print Not Found", rec.getWidth()/2, rec.getHeight()/2, 90);    
    }
    private void newPage()throws DocumentException,IOException, ParseException{
        cb1.endText();
        document.setPageSize(PageSize.LETTER);
        document.newPage();                                                   
        cb1.beginText();
    }
    public final void save() throws DocumentException, IOException{
        cb1.endText();
        document.close();
        out.close();
        formReader.close();
        if(print!=null)
            print.close();
        if(stamper!=null)
            stamper.target.delete();        
    }

    

    private boolean validatePaths()
    {
        //System.out.println(targetFile.getPath());
        //System.out.println(setupSheet.getPath());
        //System.out.println(qcSheet.getPath());
        
        if(!targetFile.exists())
        {
            System.out.println("Path does not exists "+targetFile.getPath() );
            return false;
        }
        else{
            targetFile= new File(targetFile.getPath()+"/"+ Job.jobNo +".pdf"); 
        }
        if(!setupSheet.exists())
        {
            System.out.println("File does not exists "+setupSheet.getPath() );
            return false;
        }   
        if(!qcSheet.exists())
        {
            System.out.println("File does not exists "+qcSheet.getPath() );
            return false;
        }
        
        return true;
    }
}