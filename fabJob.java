package fabSetup;


import com.ibm.as400.access.AS400;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class fabJob{
    String userName = "JAVAPROG";
    String password = "JAVAPROG";  
    String library;
    String jobNo;
    String Die;
    public String Length;
    public String FabNo;
    public String Sequence;
    public String Customer;
    public String MasterExt;
    public String CustomerNo;
    public String Plant;
    AS400 system; //as400 calls
    Connection AS400Con;
    Statement AS400s;
    ResultSet rset;
    Statement AS400s2;
    ResultSet rset2;
    File diePrint;
    File fabPrint;
    public static Map <String,Map<String,String>> operations;
    public fabJob(String param, String param2) throws SQLException{
        library=param2;
        jobNo=param;
        DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
        AS400Con = DriverManager.getConnection ("jdbc:as400://10.10.1.2/"+ ";user=" + userName + ";password=" + password);

        // Create a statement
        AS400s = (Statement) AS400Con.createStatement();   
        
        //System.out.println("Connection Started...");
        operations= new HashMap();
//          String stmt = "SELECT * FROM " + database + ".MATMOVETAG WHERE MMT_TAGNO = " + tagNo;
            String stmt = "SELECT JOP_DIE, JOP_FABNO,JOP_LENGTH,JOP_OPERSQ, JOP_OPER,JOP_CUSNAM,JOP_CUSTNO, JOP_MSTEXT, JOP_OPRCMT,JOP_PLANT"
                            +" FROM "+library+".JPFABOPS"                                         
                            +" WHERE JOP_JOBNO="+jobNo
                            +"  GROUP BY JOP_DIE, JOP_FABNO,JOP_LENGTH,JOP_OPERSQ, JOP_OPER,JOP_CUSNAM,JOP_CUSTNO, JOP_MSTEXT, JOP_OPRCMT,JOP_PLANT";
                            

            //System.out.println(stmt);

            rset = AS400s.executeQuery(stmt);
            ResultSetMetaData rsmd = rset.getMetaData();
            int columnCount=rsmd.getColumnCount();
            if (!rset.isBeforeFirst() ) {    
                throw new java.lang.Error("No Operations found for this job: "+jobNo); 
                
            }       
            
            while(rset.next())
            {       
                    Map<String, String> Lots = new HashMap();
                    Die = rset.getObject(1).toString().trim();
                    Length = rset.getObject(3).toString().trim();
                    FabNo = rset.getObject(2).toString().trim();
                    Customer = rset.getObject(6).toString().trim();
                    CustomerNo = rset.getObject(7).toString().trim();
                    MasterExt = rset.getObject(8).toString().trim();
                    for (int i=1; i <= columnCount;i++)
                    {       
                        Lots.put(rsmd.getColumnName(i),rset.getString(i));
                    }
                    operations.put(rset.getObject(9).toString(),Lots);
                    Plant = rset.getObject(10).toString().trim();
            }
            //System.out.println("Plant="+Plant);
            stmt = "SELECT * FROM "+library+".FABOPERHD WHERE"
		+" FBH_DIE='"+String.format("%5s", Integer.parseInt(Die))+"'"
		+" AND FBH_LENGTH ="+Length
		+" AND FBH_FABNO ='"+FabNo+"'";               
            //System.out.println(stmt);

            rset = AS400s.executeQuery(stmt);
            rsmd = rset.getMetaData();
            columnCount=rsmd.getColumnCount();
            if (!rset.isBeforeFirst() ) {    
                Sequence = "0";
            }       
            
            if(rset.next())
            {
                try{
                    Sequence = rset.getObject(5).toString().trim();
                }
                catch(NullPointerException ex){
                    Sequence = "0";
                }            
            }
            //System.out.println(Sequence);
                            
            
            collectDiePrints();
            AS400Con.close();
            //system1.disconnectService(AS400.COMMAND);
    }
    
    private void collectDiePrints() throws SQLException{
        String numberAsString = String.valueOf(Sequence);
        String paddedNumberAsString = "000".substring(numberAsString.length()) + numberAsString;
        String stmt= "SELECT FILENAME FROM cads.DIELIST WHERE DIENUMBER='"+Die+"'"
        +" AND NAME LIKE '%F-"+String.format("%03d", Integer.parseInt(Sequence))+"%' AND TYPE='FP'";
        //System.out.println(stmt);        
       
        String diePrintpath="";
        String fabPrintpath="";
        rset = AS400s.executeQuery(stmt);
        if (!rset.isBeforeFirst() ) {    
            stmt= "SELECT FILENAME FROM cads.DIELIST WHERE DIENUMBER='"+Die+"'"
            +" AND TYPE='D'";          
            rset = AS400s.executeQuery(stmt);
            if (!rset.isBeforeFirst() ) {    
                throw new java.lang.Error(jobNo+": No Die or Fab Print Found"); 
            }
            if(rset.next()){
                fabPrintpath = rset.getObject(1).toString().trim();
            }            
            
        }          
        if(rset.next()){
            fabPrintpath = rset.getObject(1).toString().trim();
        }
        stmt= "SELECT FILENAME FROM cads.DIELIST WHERE DIENUMBER='"+Die+"'"
        +" AND TYPE='D'";
        //System.out.println(stmt);
        rset = AS400s.executeQuery(stmt);
        if (!rset.isBeforeFirst() ) {    
            stmt= "SELECT FILENAME FROM cads.DIELIST WHERE DIENUMBER='"+Die+"'"
            +" AND NAME LIKE '%F-"+paddedNumberAsString+"%' AND TYPE='FP'";            
            rset = AS400s.executeQuery(stmt);
            if (!rset.isBeforeFirst() ) {    
                throw new java.lang.Error(jobNo+": No Die or Fab Print Found"); 
            }
            if(rset.next()){
                diePrintpath = rset.getObject(1).toString().trim();
            }            
            
        }                     
        if(rset.next())  
            diePrintpath = rset.getObject(1).toString().trim();
        
        //System.out.println("******************START***********************");
        //System.out.println(diePrintpath);
        //System.out.println(fabPrintpath);
        
        fabPrintpath=fabPrintpath.replace("\\:", "/");
        diePrintpath=diePrintpath.replace("\\:", "/");    
        
        fabPrintpath=fabPrintpath.replace("\\", "/");
        diePrintpath=diePrintpath.replace("\\", "/"); 
        
        //System.out.println(diePrintpath);
        //System.out.println(fabPrintpath);
        
        if(new File("//CUST05/Prints").exists()){
            fabPrintpath=fabPrintpath.replace("N:", "//CUST05/Prints");
            diePrintpath=diePrintpath.replace("N:","//CUST05/Prints");
        }
        else if(new File("//QNTC/CUST05/Prints").exists()){
            fabPrintpath=fabPrintpath.replace("N:", "//QNTC/CUST05/Prints");
            diePrintpath=diePrintpath.replace("N:","//QNTC/CUST05/Prints");            
        }
        else{
                throw new java.lang.Error(jobNo+": Unable to verify CADS Prints Locations"); 
        }
   
        
        //System.out.println(diePrintpath);
        //System.out.println(fabPrintpath);
        
        diePrint = new File(diePrintpath);
        fabPrint= new File(fabPrintpath);
        
        if(!diePrint.exists()){
            throw new java.lang.Error(jobNo+": Die Print :"+diePrint.getPath()+" PATH DOES NOT EXIST"); 
        }
        if(!fabPrint.exists()){
            throw new java.lang.Error(jobNo+": Fab Print:"+fabPrint.getPath()+" PATH DOES NOT EXIST"); 
        }
    }

}