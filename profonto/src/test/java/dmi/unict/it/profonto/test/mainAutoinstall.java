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
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class mainAutoinstall
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
            FileUtils.cleanDirectory( (Paths.get("ontologies"+File.separator+"devices")).toFile());  
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies"+File.separator+"devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies"+File.separator+"devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies"+File.separator+"main"));
            ontocore.setOntologiesUsersPath(Paths.get("ontologies"+File.separator+"users"));
            ontocore.setQueryPath(Paths.get("ontologies"+File.separator+"queries"));
            ontocore.setSatellitePath(Paths.get("ontologies"+File.separator+"satellite"));
            ontocore.setMainOntology(ontoFile); 
            ontocore.setMainAbox(aboxFile);
            
            ontocore.setDataBehaviorOntology("http://www.dmi.unict.it/prof-onto-behavior.owl","behavior.owl"); 
            ontocore.setDataRequestOntology("http://www.dmi.unict.it/prof-onto-request.owl","request.owl");
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
        
       
         
       InputStream assistantData=readData("ontologies/test/homeassistant.owl"); 
       ontocore.addDevice(assistantData);            
       
       
      // InputStream load=readData("ontologies/test/lightagent.owl");
     //  ontocore.parseRequest(load);
     //  System.out.println("Loaded template");
       //InputStream request=readData("ontologies/test/light-installation-request-IRI.owl");
       System.out.println("Loading test");
       
        ontocore.parseRequest(readData("ontologies/test/rasb/lightagent-from-template.owl"));    
        ontocore.parseRequest( readData("ontologies/test/rasb/rasb-lightagent.owl"));    
                
        
  //     InputStream request=readData("ontologies/test/rasb/test.owl");
 //      Stream<OWLAxiom> res= ontocore.parseRequest(request).axioms();
//       System.out.println("Request:");
//       if(res!=null)
//           {
//         res.forEach(System.out::println);        
//        
//         System.out.println();
//         System.out.println("Retrieve data:");
//         res=ontocore.retrieveBeliefAssertions("http://www.dmi.unict.it/light-installation-request.owl#light-installation-req-task");
//                                    
//         res.forEach(System.out::println);
//           }
//         else System.out.println("Request unsatisfiable");              
       
          
       
       System.out.println("Add user request");
       ontocore.parseRequest(readData("ontologies/test/rasb-user/alan.owl"));
       InputStream request=readData("ontologies/test/rasb-user/add-user-request.owl");         
       Stream<OWLAxiom> res= ( (OWLOntology) ontocore.parseRequest(request)[0]).axioms();
       System.out.println("Request of device:");
       if(res!=null)
           {
              res.forEach(System.out::println);   
           } 
       
       System.out.println("Add user configuration request");
       request=readData("ontologies/test/add-user-configuration-request.owl");         
       OWLOntology g= ((OWLOntology)ontocore.parseRequest(request)[0]);
       res= g.axioms();
       System.out.println("Request of device:");
       if(res!=null)
           {
              res.forEach(System.out::println);                 
           } 
                    
       
       System.out.println("Remove user configuration request");
       request=readData("ontologies/test/remove-user-configuration-request.owl");         
       g= (OWLOntology) ontocore.parseRequest(request)[0];
       res= g.axioms();
       System.out.println("Request of device:");
       if(res!=null)
           {
              res.forEach(System.out::println);                 
           } 
       
       
       System.out.println("Add belief request");
       request=readData("ontologies/test/add-belief-request.owl");         
       res= ((OWLOntology)ontocore.parseRequest(request)[0]).axioms();
       System.out.println("Request of device:");
       if(res!=null)
           {
              res.forEach(System.out::println);   
           } 
       
       System.out.println("Retrieve belief request");
       request=readData("ontologies/test/retrieve-belief-request.owl");         
       res= ((OWLOntology)ontocore.parseRequest(request)[0]).axioms();
       System.out.println("Request of device:");
       if(res!=null)
           {
              res.forEach(System.out::println);   
           } 
       
       System.out.println("Remove belief request");
       request=readData("ontologies/test/remove-belief-request.owl");         
       res= ((OWLOntology)ontocore.parseRequest(request)[0]).axioms();
       System.out.println("Request of device:");
       if(res!=null)
           {
              res.forEach(System.out::println);   
           } 
              
       System.out.println("Remove user request");
       request=readData("ontologies/test/remove-user-request.owl");         
       res= ((OWLOntology) ontocore.parseRequest(request)[0]).axioms();
       System.out.println("Request of device:");
       if(res!=null)
           {
              res.forEach(System.out::println);   
           } 
       
      }
  }
