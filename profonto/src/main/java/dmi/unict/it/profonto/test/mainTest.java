/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.test;

import dmi.unict.it.profonto.core.Profonto;
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
        File ontoFile=new File("ontologies/main/oasis.owl");
        File aboxFile=new File("ontologies/main/oasis-abox.owl");
       // File dataFile=new File("ontologies/main/dataset.owl");
        Profonto ontocore=new Profonto();
        try
          {
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies"+File.separator+"devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies"+File.separator+"devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies"+File.separator+"main"));
            ontocore.setOntologiesUsersPath(Paths.get("ontologies"+File.separator+"users"));
            ontocore.setQueryPath(Paths.get("ontologies"+File.separator+"queries"));
            
            ontocore.setMainOntology(ontoFile); 
            ontocore.setMainAbox(aboxFile);
            
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
        
        if(!(new File("ontologies/devices/ProfHomeAssistant.owl").exists()))
          {
            InputStream assistantData=readData("ontologies/test/homeassistant.owl"); 
            ontocore.addDevice(assistantData, "ProfHomeAssistant");
          }
        
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
  
         InputStream request=readData("ontologies/test/user-request.owl");
         Stream<OWLAxiom> res= ontocore.acceptUserRequest(request, "http://www.dmi.unict.it/user-request.owl#alan-task-1-1-1", 
                                    "http://www.dmi.unict.it/ontoas/alan.owl#Alan",
                                    "http://www.dmi.unict.it/user-request.owl#alan-task-1-1-1-exec",
                                    "http://www.dmi.unict.it/user-request.owl#alan-goal-1-1-1");
         res.forEach(System.out::println);
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
