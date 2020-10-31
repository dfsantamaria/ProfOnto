/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.test;

import dmi.unict.it.profonto.core.Profonto;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Order;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author danie
 */
public class test
{
    static Profonto ontocore;
    
   
      
    
    
    @Test
    @Order(1)
    public void testAddRemoveManualUser()
    {
        ByteArrayInputStream request;          
          
          //ADDING USERS
          String id= "";        
          String userId="";          
          InputStream userData=readData("ontologies/test/alan.owl"); 
          userId=ontocore.addUser(userData);
          try 
          {
              ontocore.removePermanentUser(userId);
          } 
          catch (Exception ex) 
          {
              System.out.println("Error deleting user");
          } 
    }
    
    @Test
    @Order(2)
    public void testParseAddUserRequest()
    {
      //   ADDING USERS
          ontocore.parseRequest(readData("ontologies/test/alan.owl"));
          ByteArrayInputStream request=readData("ontologies/test/add-user-request.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];          
          writeDown("addUserRequest", out);
    }
    
   
    
    @Test
    @Order(3)
    public void testParseUserRequest()
    {
        //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest(readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
        
       //   ADDING USERS
          ontocore.parseRequest(readData("ontologies/test/alan.owl"));
          ByteArrayInputStream request=readData("ontologies/test/user-request.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          writeDown("userRequest", out);
       //REMOVE
        ontocore.removePermanentDevice("http://www.dmi.unict.it/lightagent.owl");
    }
    
   @Order(4) 
   public void testParseInstallDeviceRequest()
    {
        //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest(readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
        
       //   ADDING USERS
          ontocore.parseRequest(readData("ontologies/test/alan.owl"));
          ByteArrayInputStream request=readData("ontologies/test/test.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          writeDown("installRequest", out);
       //REMOVE
        ontocore.removePermanentDevice("http://www.dmi.unict.it/lightagent.owl");
    }
    
    @BeforeClass
    public static void setUp()
    {      
                
        File ontoFile=new File("ontologies/main/oasis.owl");
        File aboxFile=new File("ontologies/main/oasis-abox.owl");
       // File dataFile=new File("ontologies/main/dataset.owl");
        ontocore=new Profonto();
        try
          { 
            FileUtils.cleanDirectory( (Paths.get("ontologies"+File.separator+"devices")).toFile());
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies"+File.separator+"devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies"+File.separator+"devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies"+File.separator+"main"));
            ontocore.setOntologiesUsersPath(Paths.get("ontologies"+File.separator+"users"));
            ontocore.setQueryPath(Paths.get("ontologies"+File.separator+"queries"));
            ontocore.setSatellitePath(Paths.get("ontologies"+File.separator+"satellite"));
            ontocore.setBackupPath(Paths.get("ontologies"+File.separator+"backup"));
            
            ontocore.setMainOntology(ontoFile); 
            ontocore.setMainAbox(aboxFile);
            
            ontocore.setDataBehaviorOntology("http://www.dmi.unict.it/prof-onto-behavior.owl","behavior.owl");  
            ontocore.setDataRequestOntology("http://www.dmi.unict.it/prof-onto-request.owl","request.owl");
            ontocore.setDataBeliefOntology("http://www.dmi.unict.it/prof-onto-belief.owl","belief.owl");
            ontocore.loadDevicesFromPath(false); //use this if the devices folder is not empty 
           
            
           // ontocore.startReasoner();
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
          {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
          }
        
          if(!(new File("ontologies/devices/homeassistant.owl").exists()))
          {
            InputStream assistantData=readData("ontologies/test/homeassistant.owl");            
            String dv= ontocore.addDevice(assistantData);
            System.out.println("Connection of Assistant:");
            for( String s : ontocore.getConnectionInfo(dv))
                System.out.println(s);
          }     
        
    }
    
    @AfterClass
    public static void delFiles()
    {
     
      File[] exception=new File[]{new File("oasis.owl"), new File("oasis-abox.owl")};
      emptyFolder(ontocore.getBackupPath().toFile(), new File[0]);
      emptyFolder(ontocore.getOntologiesDeviceConfigurationPath().toFile(), new File[0]);
      emptyFolder(ontocore.getOntologiesDevicesPath().toFile(), new File[0]);
      emptyFolder(ontocore.getMainOntologiesPath().toFile(), exception);
      emptyFolder(ontocore.getSatellitePath().toFile(), new File[0]);
      emptyFolder(ontocore.getOntologiesUsersPath().toFile(), new File[0]);
    }
    
    public static void emptyFolder(File dir, File[]exception)
    {
      File[]l=dir.listFiles();
      for(int i=0;i<l.length;i++)
      {
          int j=0;
          for(;j<exception.length;j++)
          {
            if(l[i].getName().equals(exception[j].getName()))
                break;
          }
          if(j==exception.length)
              l[i].delete();
      }
    }

    public void writeDown(String m, String s)
    {
        BufferedWriter writer = null;
        try {
            File filesource = new File("ontologies" + File.separator + "outTest" + File.separator+ m + ".owl");
            filesource.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(filesource));
            writer.write(s);
            writer.close();
        } catch (IOException ex)
        {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally
        {
            try 
            {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
            
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    
    public static String[] toStringOntology(ByteArrayOutputStream[] array)
      {           
        String[] ret = new String[array.length];                   
            try
              { 
                for (int i = 0; i < ret.length; i++)
                 {    
                   if(array[i]!=null)
                     {                      
                      ret[i]=array[i].toString("UTF-8");
                     }
                 }
              } 
            catch (UnsupportedEncodingException ex)
              {                
                for (int j = 0; j < ret.length; j++)
                    ret[j]=null;
                return ret;
              }          
        return ret;
      }
      
    public static String readQuery(String path)
      {
        String query="";
         try
          {
            BufferedReader queryReader=new  BufferedReader(new FileReader(path));
            String currentLine="";
            while ((currentLine = queryReader.readLine()) != null)
              {
                  query+=currentLine;
              }
          } 
        catch (FileNotFoundException ex)
          {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
          }
        catch (IOException ex)
          {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
          }
      
        return query;
      }
    
    public static ByteArrayInputStream readData(String file)
    {
        ByteArrayInputStream inputstream=null;
        String input="";
        try {
            FileReader fileReader =  new FileReader(file);
            BufferedReader bufferedReader =  new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
                input+=line;
            }       
            fileReader.close();
            bufferedReader.close();
            inputstream=new ByteArrayInputStream(input.getBytes());
            inputstream.close();
        }
        catch (IOException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
            
        } 
        return inputstream;
    }
    
}
