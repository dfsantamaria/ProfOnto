/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.test;

import dmi.unict.it.profonto.core.Profonto;
import static dmi.unict.it.profonto.test.mainAutoinstall.readData;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class mainTest
  {
    
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
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        catch (IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
            
        } 
        return inputstream;
    }
    
    public static void main(String[] args)
      {
        
        File ontoFile=new File("ontologies/main/oasis.owl");
        File aboxFile=new File("ontologies/main/oasis-abox.owl");
       // File dataFile=new File("ontologies/main/dataset.owl");
        Profonto ontocore=new Profonto();
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
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        
          if(!(new File("ontologies/devices/homeassistant.owl").exists()))
          {
            InputStream assistantData=readData("ontologies/test/homeassistant.owl");            
            String dv= ontocore.addDevice(assistantData);
            System.out.println("Connection of Assistant:");
            for( String s : ontocore.getConnectionInfo(dv))
                System.out.println(s);
          }   
          
          
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
       //   ADDING USERS
          ontocore.parseRequest(readData("ontologies/test/alan.owl"));
          request=readData("ontologies/test/add-user-request.owl");  
          String out=toStringOntology(ontocore.parseRequest(request))[0];
          System.out.println(out);
          
        //Manually adding a device  
        ontocore.parseRequest(readData("ontologies/test/lightagent-from-template.owl"));    
        ontocore.parseRequest( readData("ontologies/test/rasb-lightagent.owl")); 
        System.out.println("Adding light agent");
        ontocore.addDevice("http://www.dmi.unict.it/lightagent.owl");
      }
  }















/*

  try
          {
            OWLOntologyManager localM=OWLManager.createOWLOntologyManager();
            localM.loadOntologyFromOntologyDocument(new File("ontologies/main/onto-as.owl"));
            OWLOntology agent=localM.loadOntologyFromOntologyDocument(new File("ontologies/test/lightagent.owl"));
            OWLOntology config=localM.loadOntologyFromOntologyDocument(new File("ontologies/test/alan-config.owl"));
            OWLOntology request=localM.loadOntologyFromOntologyDocument(new File("ontologies/test/user-request.owl"));
          
            ontocore.addDataToDataBehavior(agent.axioms());
            ontocore.addDataToDataBehavior(config.axioms());
            ontocore.addDataToDataBehavior(request.axioms());
            ontocore.getMainManager().removeOntology(request);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
                
        
         System.out.println("Main ontology axioms count: " +ontocore.getMainOntology().getAxiomCount());
         System.out.println("Dataset ontology axioms count: " +ontocore.getDataBeliefOntology().getAxiomCount());
        
         //Testing a select query
        String query=readQuery("ontologies/test/query.sparql");
            
        try
          {
            QueryExecution execQ = ontocore.createQuery(ontocore.getDataBeliefOntology(), query);
            ResultSet res=ontocore.performSelectQuery(execQ);
            System.out.println(ResultSetFormatter.asText(res));
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }

        //Testing a construct query
        query=readQuery("ontologies/test/querycon.sparql");
        
          try
          {
            
            QueryExecution execQ = ontocore.createQuery(ontocore.getDataBeliefOntology(), query);
            System.out.println("Output:");
            //ontocore.performConstructQuery(execQ).forEach(System.out::println);            
            ontocore.addDataToDataBehavior(ontocore.performConstructQuery(execQ));
           
            
          } catch (OWLOntologyCreationException | IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }

*/
