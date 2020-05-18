/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deliveryticketupload;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author alopez
 *  Class: DeliveryTicketUpload
 *  Arguments: 
 *  description: Prompts user to upload a scanned document, and verify data of documents is corerect.
     If correct, it will store on a network drive in its corresponding folder based on data from invoice. 
 */

public class DeliveryTicketUpload extends javax.swing.JFrame  {
    static Connection AS400Con;
    static Statement AS400s;
    static ResultSet rset;
    static Map <String,String> Invoice;
    static String scanfile="\\\\SE-FS1\\Public\\Invoicing\\Upload Folder\\scan.pdf";
    /**
     * Creates new form dialogForm
     */
    public DeliveryTicketUpload() {
        initComponents();
        initConnection();
        //deliveryTicketInput.setText("16935000");
        //invoiceInput.setText("368803");  
        //jButton2.setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        invoiceInput = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        deliveryTicketInput = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Delivery Ticket Upload");
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton1.setText("Verify");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButton2.setText("Upload");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Delivery Ticket");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Invoice Number");

        invoiceInput.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText("*File must be named scan.pdf and saved under Upload Folder*");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setText("View Invoicing Folder (Click Here)");

        try {
            deliveryTicketInput.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("######-##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        deliveryTicketInput.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        deliveryTicketInput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                deliveryTicketInputMousePressed(evt);
            }
        });
        deliveryTicketInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                deliveryTicketInputKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                deliveryTicketInputKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
                            .addComponent(deliveryTicketInput))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(invoiceInput)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(invoiceInput, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(deliveryTicketInput))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(4, 4, 4)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            // TODO add your handling code here:
            validateEntry(deliveryTicketInput.getText(),invoiceInput.getText());
        } catch (SQLException ex) {
            Logger.getLogger(DeliveryTicketUpload.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DeliveryTicketUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        uploadFile();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        String command="cmd /c START "+"\\\\se-fs1\\public\\invoicing";                    
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(DeliveryTicketUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formMouseClicked

    private void deliveryTicketInputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deliveryTicketInputKeyPressed

          
    }//GEN-LAST:event_deliveryTicketInputKeyPressed

    private void deliveryTicketInputMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deliveryTicketInputMousePressed
        String text=deliveryTicketInput.getText().replace(" ","");        
        if(text.length()==1)
            deliveryTicketInput.setCaretPosition(0);
    }//GEN-LAST:event_deliveryTicketInputMousePressed

    private void deliveryTicketInputKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_deliveryTicketInputKeyReleased
        String value=deliveryTicketInput.getText().replace(" ","");
        if(value.length()==9){
           invoiceInput.requestFocus();
        }
    }//GEN-LAST:event_deliveryTicketInputKeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DeliveryTicketUpload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DeliveryTicketUpload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DeliveryTicketUpload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DeliveryTicketUpload.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DeliveryTicketUpload().setVisible(true);
            }
        });
    }
    private void initConnection(){
        try{
            String userName = "JAVAPROG";
            String password = "JAVAPROG";
            //String database = "QS36F";
            String database = "QS36F";
            //System.out.println("Connecting...");
            DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
             //DriverManager.deregisterDriver(new com.ibm.as400.access.AS400JDBCDriver());
            AS400Con = DriverManager.getConnection ("jdbc:as400://10.10.1.2/" + database + ";user=" + userName + ";password=" + password);
            // Create a statement
            AS400s = (Statement) AS400Con.createStatement();     
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(null, "Error. Contact IT"+ex);  
            System.out.println("Opps... " + ex);
            ex.printStackTrace();            
            System.exit(0);
        }
    }
    private void validateEntry(String delTicket, String invoiceNumber) throws SQLException, IOException
    {
        jButton2.setEnabled(false);
        if(delTicket.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Delivery Ticket field is empty. Please Verify.");
            return;
        }     
        if(invoiceNumber.isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Invoice Number field is empty. Please Verify.");
            return;
        }            
        if(!new File(scanfile).exists())
        {
            JOptionPane.showMessageDialog(null, "Scan File Not Found. Please Verify.");
            return;
        }
        if(!delTicket.contains ("-"))
        {
            JOptionPane.showMessageDialog(null, "Invalid Delivery Ticket");
            return;        
        }
        delTicket = delTicket.replace("-", "");
        System.out.println(delTicket);
        Invoice = new HashMap();
        String stmt="SELECT * FROM QS36F.INVHSHDR LEFT JOIN QS36F.CUSMAS on CUS_NUMBER=IVH_CUSTNO WHERE IVH_INVOIC="+invoiceNumber;
        rset = AS400s.executeQuery(stmt);
        if(rset.next())
        {
            ResultSetMetaData rsmd = rset.getMetaData();
            int columnCount=rsmd.getColumnCount();           
            for (int i=1; i <= columnCount;i++)
            {       
                //System.out.println(rsmd.getColumnName(i)+" : "+rset.getString(i));
                try{
                    Invoice.put(rsmd.getColumnName(i),rset.getString(i).trim());
                }
                catch(Exception ex){
                    Invoice.put(rsmd.getColumnName(i),"");
                }
            }
            System.out.println(Invoice.get("IVH_TICKET"));
            if(Invoice.get("IVH_TICKET").equals(delTicket))
            {               
                String command="cmd /c START \"\" \""+scanfile+"\"";                    
                Runtime.getRuntime().exec(command);
                int dialogButton = JOptionPane.YES_NO_OPTION;
                int dialogResult = JOptionPane.showConfirmDialog (null, "Is the PDF Correct? (close PDF before proceeding)","Warning",dialogButton);
                if(dialogResult == JOptionPane.YES_OPTION){
                    jButton2.setEnabled(true);
                }
            }
            else
                JOptionPane.showMessageDialog(null, "Invalid Entry.");    
        }
        else{
                JOptionPane.showMessageDialog(null, "Error Establishing Connection. Contact IT");   
                return;
        } 
    }
    public void uploadFile(){
        //System.out.println(System.getProperty("user.home") + "\\Desktop\\deliveryticketscan");
        //return;
        File sourceFile= new File(scanfile);     
        
        String date=Invoice.get("IVH_INVDTE").substring(4,6)+"."+Invoice.get("IVH_INVDTE").substring(6,8)+"."+Invoice.get("IVH_INVDTE").substring(2,4);
        String path="\\\\SE-FS1\\Public\\Invoicing\\"+Invoice.get("IVH_INVDTE").substring(0, 4)+" Invoices\\"+Invoice.get("CUS_NAME")+"\\"+date+"\\";
        String filename=Invoice.get("IVH_TICKET").substring(0,Invoice.get("IVH_TICKET").length()-2)+".pdf";
        System.out.println(path);
        File directory=new File(path);
        boolean directoryExists=true;
        if(!directory.exists())
        {
            directoryExists=false;
            //JOptionPane.showMessageDialog(null, "Creating Directory"+directory.getAbsolutePath()); 
            //Files.createDirectory(directory.toPath());
            if(directory.mkdirs()){
                System.out.println("Directory Created");
                directoryExists=true;
            }
            else
                System.out.println("Directory not created");
        }
        if(directoryExists){
            File targetFile = new File(path + "/" + filename);
            if(!targetFile.exists()){
                System.out.println("Source location"+sourceFile.getAbsolutePath());
                System.out.println("Target location"+targetFile.getAbsolutePath());
                if(sourceFile.renameTo((targetFile)))
                    JOptionPane.showMessageDialog(null, "File Uploaded @"+targetFile.getAbsolutePath());    
                else
                    JOptionPane.showMessageDialog(null, "Not able to upload file. Contact IT.");    
            }
            else{
                JOptionPane.showMessageDialog(null, "Delivery ticket PDF already exists. Contact IT.");    
            }
        }
        deliveryTicketInput.setText("");
        invoiceInput.setText("");   
        jButton2.setEnabled(false);
    }    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField deliveryTicketInput;
    private javax.swing.JTextField invoiceInput;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables
}
