


package movetagservices;
/**
 *
 * @author ALopez
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;
import com.itextpdf.text.Image;
import com.ibm.as400.access.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.print.*;
import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.PrinterName;
import com.ibm.as400.javax.print.ISeriesPrintService; 


/**
 * java PrintMoveTag_dev 'P' 'RLF' '1' '2' 'NETPRT36' '50270' 
 * java PrintMoveTag_dev 'D' 'AL' '50202'
 * 
/**
 *
 * @author alopez
 *  Class: MoveTagServices
 *  Arguments: mode, tagNumber, From Location, From Department, From Plant, User ID
 *  description: Creates a PDF document of a Move Tag identify material on cart
 */
public class MoveTagServices
{
    static Map <String,String> MoveTag;
    static String database;
    static String pdfFileName;
    static String userId;
    static String mode;
    static String printerName;

    static String filePath;
    static String reason="";
    static String disposition="";
    static int refLot=0;	

    static String inspectedByEmp="";;
    static String inspectedDate="";
    static String inspectedByLname = "";
    static String inspectedByFname = "";
    
    static boolean die = false;
    static File printPDF ;  //contains the TAG being generated.
    static AS400 system; //as400 calls
    static Connection AS400Con;
    static Statement AS400s;
    static ResultSet rset;
    static Statement AS400s2;
    static ResultSet rset2;
    static final String username = "JAVAPROG";
    static final String password = "JAVAPROG";
    static final String databaseConnection = "QS36F";    
    public static void main(String[] args) throws SQLException, FileNotFoundException, DocumentException, IOException, InterruptedException, ParseException, PrinterException
    {

    	String tagNo="196015";
    	String fromLoc="2";
    	String fromDept="DPT";
    	String fromPlant="2";
    	String userId="AL";
    	String[] arguments=args;
    	
        mode="P";
        //tagNo = "123456789";
        String dir="";

        if(new File("/esdi/websmart/MoveTags").exists()){
            dir="/esdi/websmart/MoveTags";
        }
        else if(new File("//10.10.1.2/root/esdi/websmart/MoveTags").exists()){

            //System.out.println("//QNTC/CUST05/Prints");
            dir="//10.10.1.2/root/esdi/websmart/MoveTags";            
        }
        //System.out.println(dir);
        
        if(args.length > 0)
        {
        	mode = args[0].toString();
                fromLoc = args[1];
                fromDept = args[2];
                fromPlant = args[3];
                if(mode.equals("D"))
                    userId = args[4];		
                else
                    printerName = args[4];		
	        tagNo = args[5];                
        }
        if(mode.equals("P")){            
            pdfFileName = dir+"/MoveTag_" + tagNo + ".pdf"; 
        }
		else if (mode.equals("D")){
            
            pdfFileName = dir+"/MoveTag_Display_" + userId + ".pdf";
            //System.out.println("Creating Move Tag for Display: "+tagNo  +" Executed by : "+userId);
		}
		else
		{
            System.out.println("No valid mode entered");
            for (String s: arguments) {
                System.out.println("'" + s + "'");
            }         			
            System.exit(1);
		}
        printerName="NETPRT94";
        String fileName = "";
        String dieNumber = "";
        //String filePath = "";
         
        try {
            database = args[6].toString().trim();	
		}
		catch ( IndexOutOfBoundsException e ) {
	    	database = "QS36F";
		}        
        
        if(database.equals(""))
        	database = "QS36F";
        
        try{
            //String database = "QS36F";

            //System.out.println("Connecting...");
            // Load the IBM Toolbox for Java JDBC driver.
            DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
            //AS400Con = DriverManager.getConnection ("jdbc:as400://10.10.1.2/"+ ";user=" + username + ";password=" + password);
            AS400Con = DriverManager.getConnection ("jdbc:as400://10.10.1.2/" + databaseConnection + ";user=" + username + ";password=" + password);
            // Create a statement
            AS400s = (Statement) AS400Con.createStatement();   
            AS400s2 = (Statement) AS400Con.createStatement();   
            
            //Create Move Tag
            createMoveTag(tagNo);
            
            rset.close();
            AS400s.close();
            AS400Con.close();
            System.exit(0);			
		}
		catch(Exception ex){
            System.out.println("Error: "+ex);
            for (String s: arguments) {
                System.out.println("'" + s + "'");
            }                
            System.out.println("Printing stack trace:");
            StackTraceElement[] elements = ex.getStackTrace();
            for (int i = 1; i < elements.length; i++) {
                StackTraceElement s = elements[i];
                System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
            + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
              }
            rset.close();
            AS400s.close();
            AS400Con.close();                
            System.exit(1);		
        }
    }
    private static void createMoveTag(String tagNo)throws SQLException, FileNotFoundException, DocumentException, IOException, InterruptedException, ParseException, PrinterException,AS400SecurityException,AS400Exception,ErrorCompletingRequestException,RequestNotSupportedException
    {
    	
			String stmt ="";
			MoveTag = new HashMap();
			if(database.equals("QS36F"))
				stmt= "SELECT * FROM  " + database + ".MATLMVETAG"
							+ " WHERE MMT_TAGNO = '" + tagNo + "'";
			else
				stmt= "SELECT * FROM " + database + ".MATLMVETAG"
							+ " WHERE MMT_TAGNO = '" + tagNo + "'";				
			//System.out.println(stmt);
			try{
				rset = AS400s.executeQuery(stmt);	
			}
			catch(SQLException error){
				System.out.println( database);
				throw new java.lang.Error("Query Failed: "+ stmt);
			}

			if(rset.next())
			{
                            ResultSetMetaData rsmd = rset.getMetaData();
                            int columnCount=rsmd.getColumnCount();           
                            for (int i=1; i <= columnCount;i++)
                            {       
                                //System.out.println(rsmd.getColumnName(i)+" : "+rset.getString(i));
                                try{
                                    MoveTag.put(rsmd.getColumnName(i),rset.getString(i).trim());
                                }
                                catch(Exception ex){
                                    MoveTag.put(rsmd.getColumnName(i),"");
                                }
                            }
			}
			else{
				throw new java.lang.Error("Tag not Found:"+tagNo);
			} 
			
			String extrusionDate="";
			extrusionDate=getExtrusionDate();
			
			//if(MoveTag.get("MMT_FRMDPT").equals("FAB")||MoveTag.get("MMT_FRMDPT").equals("ANO")||MoveTag.get("MMT_FRMDPT").equals("PNT"))
			//	filePath=checkFabPrint();
			//else
			filePath=getDiePrint();
			//System.out.println(filePath);


			// Get the Oven tag record for QC info
			boolean Inspected;
		
			Inspected=getOvnDetails();
                        
                        system = new AS400("10.10.1.2", "JAVAPROG", "JAVAPROG");
			IFSFile file;
			IFSFileInputStream fis;
			PdfReader reader;
			printPDF = new File(pdfFileName);
			FileOutputStream os = new FileOutputStream(printPDF);
			Document document = new Document(PageSize.LETTER.rotate(), 0, 0, 0, 0);
			PdfWriter writer = PdfWriter.getInstance(document, os); 
			document.open();
			PdfContentByte cb1 = writer.getDirectContent();                             
			PdfImportedPage page;
			document.newPage();   	
			BaseFont bf = BaseFont.createFont(BaseFont.COURIER_BOLD, BaseFont.CP1252, false);					
			Rectangle rec = new Rectangle(document.getPageSize());
			float topPage = (float)rec.getHeight();
			float midX = (float)rec.getWidth() / 2;
			float yOffset = 55;
			float lineGap = 50;
			cb1.moveTo(midX, 0);
			cb1.lineTo(midX, topPage);
			cb1.stroke();
			String user = "alopez";   
			float fSize;
			

			try{
				file = new IFSFile(system, filePath);			
				if (!file.exists())
					throw new java.lang.Error("CAD DIAGRAM NOT FOUND. MOVE TAG NOT CREATED"); 
				fis = new IFSFileInputStream (file, IFSFileInputStream.SHARE_READERS);
				reader = new PdfReader(fis);
				//System.out.println("Reader created...");
				
				//FileOutputStream os = new FileOutputStream(printPDF.getAbsolutePath());
				//System.out.println("OS establisted...");  
				page = writer.getImportedPage(reader, 1); 
								//Add Die Print
				Image inst = Image.getInstance(page);
				int rotation = reader.getPageRotation(1);
				if(rotation == 0)
				{
						inst.setRotationDegrees(90);
				}
				if(rotation == 270)
				{
						inst.setRotationDegrees(180);
				}
				inst.setAbsolutePosition(0, 6);                
				inst.scalePercent(49f);
				writer.getDirectContent().addImage(inst);				
				
			}
			catch(Exception Ex){
				filePath="";
				fSize= 40;

				cb1.setFontAndSize(bf, fSize);
				cb1.beginText();			
				cb1.showTextAligned(Element.ALIGN_LEFT, "Die Print Not Found", (float)rec.getWidth() /4, 75,90f);								
				cb1.endText();
			}
				
			

			fSize = 55;
			cb1.setFontAndSize(bf, fSize);
			String dispLoc = "";

			if(MoveTag.get("MMT_TOLOC").equals("1"))
			{
					dispLoc = "SE";
			}
			else if(MoveTag.get("MMT_TOLOC").equals("2"))
			{
					dispLoc = "G";
			}

			//To Department
			cb1.beginText();
			if(!MoveTag.get("MMT_STATUS").equals("H") || MoveTag.get("MMT_TODPT").equals("QC") )
				cb1.showTextAligned(Element.ALIGN_LEFT, "To:" + dispLoc + "-" + MoveTag.get("MMT_TODPT") + "-" + MoveTag.get("MMT_TOPLT"), (float)midX + 20, (float)topPage - yOffset, 0f);
			else{
				
				try{
					if(Integer.parseInt(MoveTag.get("MMT_REJECT"))>0 && Integer.parseInt(MoveTag.get("MMT_REWORK"))==0)
						cb1.showTextAligned(Element.ALIGN_LEFT, "QC HOLD", (float)midX + 20, (float)topPage - yOffset, 0f);
					else{
						cb1.setFontAndSize(bf, 40);
						cb1.showTextAligned(Element.ALIGN_LEFT, "PRODUCTION HOLD", (float)midX + 20, (float)topPage - yOffset, 0f);
						cb1.setFontAndSize(bf, 55);
					}
				}
				catch(Exception ex){
					cb1.showTextAligned(Element.ALIGN_LEFT, "To:" + dispLoc + "-" + MoveTag.get("MMT_TODPT") + "-" + MoveTag.get("MMT_TOPLT"), (float)midX + 20, (float)topPage - yOffset, 0f);
				}
			}

			cb1.endText();

			yOffset = yOffset + lineGap;

			//Die Number
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Die: " + MoveTag.get("MMT_DIE"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();
			try{
				if(MoveTag.get("MMT_TOOPER").length()>0){

					yOffset = yOffset + 30;

					fSize = 20;
					lineGap = 20;				
					cb1.setFontAndSize(bf, fSize);

					cb1.beginText();
					cb1.showTextAligned(Element.ALIGN_LEFT, "Operation: "+MoveTag.get("MMT_TOOPER"), (float)midX + 20, (float)topPage - yOffset, 0f);
					cb1.endText();
					yOffset = yOffset + 20;
				}
				else if(reason.length()>0 && disposition.length()==0)
				{
					yOffset = yOffset + 30;

					fSize =20;
					lineGap = 20;				
					cb1.setFontAndSize(bf, fSize);

					cb1.beginText();
					cb1.showTextAligned(Element.ALIGN_LEFT, "Reason: "+reason, (float)midX + 20, (float)topPage - yOffset, 0f);
					cb1.endText();
					yOffset = yOffset + 20;						
				}
				else if(reason.length()>0 && disposition.length()>0)						
				{
					yOffset = yOffset + 20;

					fSize =12;
					lineGap = 20;				
					cb1.setFontAndSize(bf, fSize);

					cb1.beginText();
					cb1.showTextAligned(Element.ALIGN_LEFT, "Reason: "+reason, (float)midX + 20, (float)topPage - yOffset, 0f);
					yOffset = yOffset + 18;												
					cb1.showTextAligned(Element.ALIGN_LEFT, "Disposition: "+disposition, (float)midX + 20, (float)topPage - yOffset, 0f);
					cb1.endText();
					yOffset = yOffset + 17;												
				}
				else
					yOffset = yOffset + lineGap;
			}
			catch(Exception ex){
				yOffset = yOffset + lineGap;
			}

			fSize = 14;
			lineGap = 20;				
			cb1.setFontAndSize(bf, fSize);
			//Customer
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Customer: " + MoveTag.get("MMT_CUSNAM"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();
			
			//ffset = yOffset + lineGap;
			
			//Customer Number
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "#" + MoveTag.get("MMT_CUSTNO"), (float)rec.getWidth() - (midX/6), (float)topPage - yOffset, 0f);
			cb1.endText();

			yOffset = yOffset + lineGap;

			//Qty
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Qty: " + MoveTag.get("MMT_QTY"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();
			
			String dispLength="";

			if(MoveTag.get("MMT_RANDOM").equals("R"))
			{
				dispLength = MoveTag.get("MMT_LENGTH") + " Random";
			}
			else
				dispLength=MoveTag.get("MMT_LENGTH");
		
			//Length
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Lth: " + dispLength, (float) ((float)midX + (midX*.33)), (float)topPage - yOffset, 0f);
			cb1.endText();
			
			//Pounds
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Weight: " + MoveTag.get("MMT_LBS"), (float) ((float)midX + (midX*.66)), (float)topPage - yOffset, 0f);
			cb1.endText();

                        
			yOffset = yOffset + lineGap;

			//Lot Number
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Lot#: " + MoveTag.get("MMT_LOTNO"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();

			yOffset = yOffset + lineGap;

			//CPN
			String cpn = MoveTag.get("MMT_DIE") + "-" + MoveTag.get("MMT_LENGTH") + "-" + MoveTag.get("MMT_FINTYP")+MoveTag.get("MMT_FINCOD") + "-" + MoveTag.get("MMT_FABNO");
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "CPN: " + cpn, (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();

			yOffset = yOffset + lineGap;

			//Supervisor
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Supervisor: " + MoveTag.get("MMT_SPRVSR"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();

			//Initials line
			cb1.moveTo((float)rec.getWidth() - (midX/3), (float)topPage - yOffset);
			cb1.lineTo((float)rec.getWidth() - ((midX/3) - 75), (float)topPage - yOffset);
			cb1.stroke();

			fSize = 7;
			cb1.setFontAndSize(bf, fSize);
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Initials/Date", (float)rec.getWidth() - ((midX/4) + 25), (float)topPage - (yOffset + 5), 0f);
			cb1.endText();

			fSize = 14;
			cb1.setFontAndSize(bf, fSize);

			yOffset = yOffset + lineGap;

			//Temper
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Temper: " + MoveTag.get("MMT_TEMPER") + "   Alloy: " + MoveTag.get("MMT_ALLOY"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();

			yOffset = yOffset + lineGap;

			//Cart No.
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Cart Number: " + MoveTag.get("MMT_CARTNO"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();

			yOffset = yOffset + lineGap;

			//Tag Comments
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Comments: " + MoveTag.get("MMT_CMNTS"), (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();

			yOffset = yOffset + lineGap;

			String crtDate="";
			String crtTime="";

			try{
				if(MoveTag.get("MMT_CRTDAT").length() == 6)
				{
					crtDate = MoveTag.get("MMT_CRTDAT");
				}
				else if(MoveTag.get("MMT_CRTDAT").length() == 5)
				{
					crtDate = "0" + MoveTag.get("MMT_CRTDAT");
				}
				else if(MoveTag.get("MMT_CRTDAT").length() == 4)
				{
					crtDate = "00" + MoveTag.get("MMT_CRTDAT");
				}				
				else if(MoveTag.get("MMT_CRTDAT").length() == 3)
				{
					crtDate = "000" + MoveTag.get("MMT_CRTDAT");
				}			
				else if(MoveTag.get("MMT_CRTDAT").length() == 2)
				{
					crtDate = "0000" + MoveTag.get("MMT_CRTDAT");
				}	
				else if(MoveTag.get("MMT_CRTDAT").length() == 1)
				{
					crtDate = "00000" + MoveTag.get("MMT_CRTDAT");
				}
				else
				{
					crtDate	= "000000";
				}
			}
			catch(Exception ex){
				crtDate	= "000000";
			}
			try{
				if(MoveTag.get("MMT_CRTTIM").length() == 6)
				{
					crtTime = MoveTag.get("MMT_CRTTIM");
				}
				else if(MoveTag.get("MMT_CRTTIM").length() == 5)
				{
					crtTime = "0" + MoveTag.get("MMT_CRTTIM");
				}
				else if(MoveTag.get("MMT_CRTTIM").length() == 4)
				{
					crtTime = "00" + MoveTag.get("MMT_CRTTIM");
				}				
				else if(MoveTag.get("MMT_CRTTIM").length() == 3)
				{
					crtTime = "000" + MoveTag.get("MMT_CRTTIM");
				}			
				else if(MoveTag.get("MMT_CRTTIM").length() == 2)
				{
					crtTime = "0000" + MoveTag.get("MMT_CRTTIM");
				}	
				else if(MoveTag.get("MMT_CRTTIM").length() == 1)
				{
					crtTime = "00000" + MoveTag.get("MMT_CRTTIM");
				}
				else
				{
					crtTime	= "000000";
				}	
			}
			catch(Exception ex){
				crtTime	= "000000";
			}
			String formatDate = crtDate.substring(2, 4) + "/" + crtDate.substring(4, 6) + "/" + crtDate.substring(0, 2);
			String formatTime = crtTime.substring(0, 2) + ":" + crtTime.substring(2, 4) + ":" + crtTime.substring(4, 6);
			
			//Date
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Date Created: " + formatDate + " " + formatTime, (float)midX + 20, (float)topPage - yOffset, 0f);
			cb1.endText();
									
			yOffset = yOffset + lineGap;
			
			dispLoc = "";
			yOffset = yOffset + lineGap - 75 ;

			String tagNo2 = String.format("%015d", Integer.parseInt(tagNo));
			
			//Heat Treat Box
			cb1.moveTo((float)midX + (midX/8), yOffset);
			cb1.lineTo((float)rec.getWidth() - (midX/8), yOffset);
			cb1.lineTo((float)rec.getWidth() - (midX/8), yOffset - 100);
			cb1.lineTo((float)midX + (midX/8), yOffset - 100);
			cb1.closePath();
			cb1.stroke();

			fSize = 12;
			cb1.setFontAndSize(bf, fSize);
			cb1.beginText();
			cb1.showTextAligned(Element.ALIGN_LEFT, "Heat Treat", midX + ((midX/2)-30), yOffset - 115, 0f);
			cb1.endText();
			//System.out.println(Inspected);
			if(Inspected==true){
				//QC Inspected by
                                //System.out.println(inspectedByFname + " " + inspectedByLname);
				if(inspectedByFname.length()>0 && inspectedByLname.length()>0){
					fSize = 12;
					cb1.setFontAndSize(bf, fSize);
					cb1.beginText();
					cb1.showTextAligned(Element.ALIGN_LEFT, "Inspected by:", midX + ((midX/2)-40), yOffset - 25, 0f);
					cb1.endText();
					cb1.beginText();
					cb1.showTextAligned(Element.ALIGN_LEFT, inspectedByFname + ", " + inspectedByLname, midX + ((midX/2)-25), yOffset - 45, 0f);
					cb1.endText();
				}
                                else
                                {
                                    
                                }
                                if(inspectedDate.length()>0){	
					//Date heat treat inspected
					String formatInspDate = inspectedDate.substring(4,6) + "/" + inspectedDate.substring(6, 8) + "/" + inspectedDate.substring(2, 4);						
					fSize = 12;
					cb1.setFontAndSize(bf, fSize);
					cb1.beginText();
					cb1.showTextAligned(Element.ALIGN_LEFT, "Date: " + formatInspDate, midX + ((midX/2)-40), yOffset - 75, 0f);
					cb1.endText();
				}

				fSize = 14;
				cb1.setFontAndSize(bf, fSize);

				//String tagNo2 = String.format("%015d", Integer.parseInt(tagNo));
				//System.out.println(tagNo2);


				//Add Heat Treat Stamp
				String imgurl = "http://10.10.1.2:8010/websmart/v9.4/CustomAluminum/images/qc_approved.jpeg";
				
				if (MoveTag.get("MMT_HEATTR").equals("R")) {
					imgurl = "http://10.10.1.2:8010/websmart/v9.4/CustomAluminum/images/QC-Rejectedlogo.jpg";
				}
				
				Image img = Image.getInstance(imgurl);
				img.scaleToFit(90,90);  
				img.setAbsolutePosition(rec.getRight() - 340, rec.getTop() - 425);						
				writer.getDirectContent().addImage(img);
			}		
			
			//Add Barcode

			Barcode39 bc = new Barcode39();
			tagNo2 = "M" + tagNo2.replaceFirst("^0+(?!$)", "");				
			bc.setCode(tagNo2);
			Image barcode = bc.createImageWithBarcode(cb1, null, null);
			barcode.scalePercent(175f);
			int offset = 15 - tagNo2.replaceFirst("^0+(?!$)", "").length();
			offset = offset * 5 + 85;
			barcode.setAbsolutePosition(midX + offset, topPage/8);
			writer.getDirectContent().addImage(barcode);


			//Extrusion Date
			//bf = BaseFont.createFont(BaseFont.COURIER_BOLD, BaseFont.CP1252, false);
                        
			String fontPath = "Chunkfive.otf";
			String weekNo="";
			try{
				if(Integer.parseInt(extrusionDate)>0){
					String weeksql="SELECT GPW_WEEKNO FROM QS36F.GENWEEKCTL WHERE GPW_YEAR="+extrusionDate.substring(0,2)+" AND GPW_START<="+extrusionDate+" AND GPW_END>="+extrusionDate;
					//System.out.println(weeksql);
					rset2 = AS400s2.executeQuery(weeksql);
					while(rset2.next())
					{            
						weekNo = rset2.getObject(1).toString();
					}		
				}
				else
					weekNo="0";
			}
			catch(Exception ex){
				weekNo="0";
			}
                        BaseFont bf2;
                        
			try{
                            System.out.println(fontPath);
                            bf2 = BaseFont.createFont(fontPath, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                        }
                        catch(IOException ex){
                            System.out.println("HERE");
                            bf2 = BaseFont.createFont("\\\\SE-FS1\\Public\\Java\\Move Tag Services\\Chunkfive.ost", BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                        }
			cb1.setFontAndSize(bf2, 120);						
			midX=(float)rec.getWidth() / 2;
			cb1.showTextAligned(Element.ALIGN_LEFT, weekNo, (float)midX+0, 20 , 0f);


			//Add QR Code
			String url = "http://10.10.1.2:8150/wsphp/matmovemaint.php?task=disp&MMT_TAGNO=" + MoveTag.get("MMT_TAGNO");
			BarcodeQRCode qrcode = new BarcodeQRCode(url, 100, 100, null);
			Image qr = qrcode.getImage();			
			qr.scalePercent(75f);
			qr.setAbsolutePosition(((float)rec.getWidth() - qr.getScaledWidth())-10,20);
			writer.getDirectContent().addImage(qr);
                        
                        document.close();
                        os.close();
			//document.close();
			//writer.close();
			//os.close();
                        System.out.println(printPDF.getAbsolutePath());
			if(mode.equals("P")){
				//send to printer
				PrintPDF(tagNo);
			}		
			//System.out.println("File Created: "+pdfFileName);
	
    }
    //print PDF document
    private static void PrintPDF(String tagNo) throws PrinterException,SQLException, AS400Exception, AS400SecurityException,ErrorCompletingRequestException,IOException,InterruptedException,RequestNotSupportedException{
		
		FileInputStream fis2;
		try {
			fis2 = new FileInputStream(printPDF.getAbsolutePath());
								
			Doc pdfDoc = new SimpleDoc(fis2, DocFlavor.INPUT_STREAM.PDF, null);
			//System.out.println("Looking for printers" );
			
			ISeriesPrintService printService = new ISeriesPrintService(system, printerName);	
			//System.out.println("lookup completed for " + printService);
			
			if (printService != null) 
			{
				 //System.out.println("Printer: " + printService.getName());
				
				//if (printServices[j].getName().equalsIgnoreCase("NETPRT73")) 
				if (printService.getName().equalsIgnoreCase(printerName)) 
				{
					// Create Print Job
					DocPrintJob pjob = printService.createPrintJob();
					//System.out.println("printer selected: " + printService.getName());
					try 
					{
						//System.out.println(printPDF.getAbsolutePath());
						//System.exit(0);
						pjob.print(pdfDoc, new HashPrintRequestAttributeSet());
						printPDF.deleteOnExit();
						
						DateFormat df1 = new SimpleDateFormat("yyMMdd");
						String formattedDate1 = df1.format(new Date());
						DateFormat df2 = new SimpleDateFormat("HHmmss");
						String formattedTime1 = df2.format(new Date());

						String stmt_print = "UPDATE " + database + ".MATLMVETAG"
								+ " SET MMT_PRINT = 'Y'"
								+ " , MMT_PRTDAT = '" + formattedDate1 +"'"
								+ " , MMT_PRTTIM = '" + formattedTime1 +"'"
								+ " WHERE MMT_TAGNO = " + tagNo;
						//System.out.println(stmt_print);
						AS400s2.execute(stmt_print);	

						stmt_print = "UPDATE " + database + ".MATMOVETAG"
								+ " SET MMT_PRINT = 'Y'"
								+ " , MMT_PRTDAT = '" + formattedDate1 +"'"
								+ " , MMT_PRTTIM = '" + formattedTime1 +"'"
								+ " WHERE MMT_TAGNO = " + tagNo;
						//System.out.println(stmt_print);
						AS400s2.execute(stmt_print);										
								
					}
					catch(PrintException pe)
					{
						System.out.println("PrintPDF: "+pe);					

						System.exit(1);
						//pe.printStackTrace();
					}
					
				}
				else
				{
					System.out.println("PrintPDF: Printer passed does not match "+printerName+": "+printService.getName());
					System.exit(1);
				}
				
			}
			if (printService == null) 
			{
				throw new PrinterException("PrintPDF: Invalid print service name: " + printerName);
			}       
								   
		}
		catch (FileNotFoundException ex) 
		{
			System.out.println("PrintPDF: Opps... " + ex);
		} 

    }
    /*
    *   Get Fab Print Path
    */    
    private static String writeFabPrint(int Printsequence) throws SQLException{
    	String numberAsString = String.valueOf(Printsequence);
        String paddedNumberAsString = "000".substring(numberAsString.length()) + numberAsString;
    	
		String diePrintpath="";
		String stmt2="SELECT FILENAME FROM CADS.DIELIST WHERE dieNumber = '"+MoveTag.get("MMT_DIE")+"'   AND NAME LIKE '%F-"+paddedNumberAsString+"%' AND TYPE='FP'";
		rset2 = AS400s2.executeQuery(stmt2);
        if(rset2.next()){
            diePrintpath = rset2.getString(1).trim();
            diePrintpath = diePrintpath.replace("\\", "/");
			diePrintpath = "//QNTC/CUST05/Prints" + diePrintpath.substring(2).trim();
        }    
        return diePrintpath;
    }

    /*
    *   Get date maertial was extruded
    */
    private static String  getExtrusionDate() throws SQLException{
		try{
				if(Integer.parseInt(MoveTag.get("MMT_REJECT"))>0 && Integer.parseInt(MoveTag.get("MMT_REWORK"))==0)
				{
					String reworkStr="SELECT RTF_REASN1,RTF_REASN2, RTF_DISPS1,RTF_DISPS2 FROM "+database+".REJTAGFL WHERE RTF_REJNO="+MoveTag.get("MMT_REJECT");
					rset2 = AS400s2.executeQuery(reworkStr);
					if(rset2.next())
					{            
						reason=rset2.getString(1).trim()+" "+rset2.getString(2).trim();
						disposition=rset2.getString(3).trim()+" "+rset2.getString(4).trim();
					}					
				}
				
				String reworkStr="SELECT ORF_REFLOT FROM "+database+".REWORKHDR WHERE ORF_LOTNO="+MoveTag.get("MMT_LOTNO");
				rset2 = AS400s2.executeQuery(reworkStr);
				while(rset2.next())
				{            
					refLot = rset2.getInt(1);
				}
		}
		catch(Exception ex){
			refLot=0;
		}

		String exstr;
		if(refLot==0)
			exstr=" SELECT MIN(MMT_CRTDAT)  FROM QS36F.JOBPACKMLT LEFT JOIN "+database+".MATLMVETAG ON MMT_LOTNO=JLT_MSTEXT WHERE  JLT_LOTNO="+MoveTag.get("MMT_LOTNO");
		else
			exstr=" SELECT MIN(MMT_CRTDAT)  FROM QS36F.JOBPACKMLT LEFT JOIN "+database+".MATLMVETAG ON MMT_LOTNO=JLT_MSTEXT WHERE  JLT_LOTNO="+refLot;

		String extrusionDate;
		rset2 = AS400s2.executeQuery(exstr);
		if(rset2.next())
		{            
			try{
				extrusionDate = rset2.getObject(1).toString();
			}
			catch(NullPointerException ex){
				extrusionDate="0";
			}
		}
		else
			extrusionDate="0";
		return extrusionDate;
    }
    /*
    *   get for Die Print path
    */    
    private static String getDiePrint() throws SQLException{
    	String die=MoveTag.get("MMT_DIE");
    	if(die.equals("114"))
    		die="0114";

    	String print="";
		String stmt2 = "SELECT * FROM CADS.DIELIST"
				+ " WHERE DIENUMBER = '" + die + "'"
				+ " order by name";
		//System.out.println(stmt2);
		rset2 = AS400s2.executeQuery(stmt2);

		while(rset2.next())
		{            
			if(!rset2.getObject(3).toString().contains("obsolete"))
			{
				//FILE PATH
				print = rset2.getString(3);
				// System.out.println("Old Path: " + filePath);
				print = print.replace("\\", "/");
				print = "//QNTC/CUST05/Prints" + print.substring(2).trim();
				//System.out.println("New Path: " + filePath);
				break;
			}
		}    
		return print;
    }
    /*
    *   Check for Fabrication Print 
    */    
    private static String checkFabPrint()throws SQLException{
    	String die=MoveTag.get("MMT_DIE");
    	if(die.equals("114"))
    		die="0114";
		int printSequence=0;
		String stmt2 = "SELECT * FROM CADS.DIELIST"
				+ " WHERE DIENUMBER = '" +die + "'"
				+ " order by name";
		stmt2="SELECT FBH_PRTSEQ FROM QS36F.FABOPERHD  WHERE FBH_DIE='"+die+"' AND FBH_LENGTH="+MoveTag.get("MMT_LENGTH")+" AND FBH_FABNO='"+MoveTag.get("MMT_FABNO")+"'";
		rset2 = AS400s2.executeQuery(stmt2);
		
		if(rset2.next())
		{    
			try{
				 printSequence=rset2.getInt(1);
			}
			catch(Exception ex){
				 printSequence=0;
			}
			if(printSequence>0){
				return writeFabPrint(printSequence);
			}
			else 
				return "";
		}
		return "";    	
    }

    /*
    *  Check if material passed through oven
    */  	  
    private static boolean getOvnDetails()throws SQLException{
        boolean Inspected;
        if(MoveTag.get("MMT_HEATTR").equalsIgnoreCase("Y") || MoveTag.get("MMT_HEATTR").equalsIgnoreCase("R") ){
            try{
                String stmt2;
                if(Integer.parseInt(MoveTag.get("MMT_OVNTAG"))>0)
                     stmt2 = "SELECT TMG_FNAME,TMG_LNAME,OVT_QC_INS, OVT_QC_DTE,OVT_OVEN,OVT_SSHIFT,TMD_DPTDSC FROM " + "QS36F" + ".OVENTAGS "
                             +"  LEFT JOIN QS36F.TIMRECGEN ON TMG_EMPNO=OVT_QC_INS "
                             +" LEFT JOIN QS36F.TIMRECDPT ON TMD_DPTABV='OVN' AND TMD_DPTPLT=OVT_OVEN"
                             +" WHERE OVT_TO_TAG in (" + Integer.parseInt(MoveTag.get("MMT_OVNTAG"))+")";
                else
                    stmt2 = "SELECT TMG_FNAME,TMG_LNAME,OVT_QC_INS, OVT_QC_DTE, OVT_OVEN,OVT_SSHIFT,TMD_DPTDSC FROM " + "QS36F" + ".OVENTAGS "
                            +" LEFT JOIN QS36F.TIMRECGEN ON TMG_EMPNO=OVT_QC_INS"
                            +" LEFT JOIN QS36F.TIMRECDPT ON TMD_DPTABV='OVN' AND TMD_DPTPLT=OVT_OVEN"
                            +" WHERE OVT_TO_TAG in (" + Integer.parseInt(MoveTag.get("MMT_TAGNO"))+")";
                rset2 = AS400s2.executeQuery(stmt2);		
                inspectedByEmp = "";
                if(rset2.next())
                {            
                        try{
                            if(rset2.getObject(1).toString().trim().length()>0 && rset2.getObject(2).toString().trim().length()>0){
                                inspectedByLname = rset2.getObject(1).toString().trim();
                                inspectedByFname = rset2.getObject(2).toString().trim();                                        
                            }
                            else{
                              //  inspectedByLname = "Shift: "+rset2.getObject(6).toString().trim();
                                inspectedByFname = rset2.getObject(7).toString().trim();
                            }
                            
                        }
                        catch(Exception ex){
                            //inspectedByLname = "Shift: "+rset2.getObject(6).toString().trim();
                            inspectedByFname = rset2.getObject(7).toString().trim();
                        }
                        inspectedByEmp = rset2.getObject(3).toString().trim();
                        inspectedDate = rset2.getObject(4).toString().trim();
                }	
                Inspected=true;
            }
            catch(Exception ex){
                    Inspected=true;
            }
        }
        else
            Inspected=false;    
        return Inspected;
    }
    
}
