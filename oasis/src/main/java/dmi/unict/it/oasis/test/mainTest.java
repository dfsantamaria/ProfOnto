/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.oasis.test;

import dmi.unict.it.oasis.core.Oasis;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class mainTest
  {
    
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
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        catch (IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
      
        return query;
      }
    
    public static InputStream readData(String file)
    {
        InputStream inputstream=null;
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
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
            
        } 
        return inputstream;
    }
    
    public static void main(String[] args)
      {
        File ontoFile=new File("ontologies/main/onto-as.owl");
       // File dataFile=new File("ontologies/main/dataset.owl");
        Oasis ontocore=new Oasis();
        try
          {
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies"+File.separator+"devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies"+File.separator+"devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies"+File.separator+"main"));
            ontocore.setOntologiesUsersPath(Paths.get("ontologies"+File.separator+"users"));
            
            ontocore.setMainOntology(ontoFile);           
            ontocore.setDataBehaviorOntology("http://www.dmi.unict.it/prof-onto-behavior.owl","behavior.owl");  
            ontocore.setDataBeliefOntology("http://www.dmi.unict.it/prof-onto-belief.owl","belief.owl");
            ontocore.loadDevicesFromPath(true); //use this if the devices folder is not empty 
           // ontocore.startReasoner();
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
                   
         
        //Device
        String id= "dev"+ new Timestamp(new Date().getTime()).toString().replace(" ","").replace(":","").replace(".","");
        //Stream<OWLAxiom> axioms=Stream.empty();
        String userId="ALAN";
        
        InputStream userData=readData("ontologies/test/alan.owl"); 
        ontocore.addUser(userData, userId);
        
        InputStream ontologyData=readData("ontologies/test/lightagent.owl");        
        ontocore.addDevice(ontologyData, id);
        
        InputStream deviceConfig=readData("ontologies/test/alan-config.owl");
        ontocore.addDeviceConfiguration(deviceConfig, id, id+"Conf-1","ALAN");        
        try
          {
            ontocore.syncReasonerDataBehavior();
          } 
        catch (OWLOntologyStorageException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
  
// remove an user and the related configurations        
//        try
//          {
//            ontocore.removePermanentConfigurationsFromUser("ALAN");
//          } catch (OWLOntologyStorageException | OWLOntologyCreationException ex)
//          {
//            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
//          }
        
        try
          {            
            ontocore.removePermanentUser(userId);
            ontocore.removePermanentDevice(id);
            ontocore.refreshDataBehavior();            
          }
        catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
          
            
            
            
//        try
//          {              
//            ontocore.removePermanentDevice(id);
//
//          } 
//        catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex)
//          {
//            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
//          }
          
        
        
      }
  }
