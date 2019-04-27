/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.test;

import dmi.unict.it.ontoas.core.OntoASCore;
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
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
        OntoASCore ontocore=new OntoASCore();
        try
          {
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies"+File.separator+"devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies"+File.separator+"devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies"+File.separator+"main"));
            ontocore.setMainOntology(ontoFile);           
            ontocore.setDataBeliefOntology("http://www.dmi.unict.it/profeta-dataset.owl","dataset.owl");            
            ontocore.loadDevicesFromPath(true); //use this if the devices folder is not empty 
            ontocore.startReasoner();
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        
       
        //
        String id= "dev"+ new Timestamp(new Date().getTime()).toString().replace(" ","").replace(":","").replace(".","");
        Stream<OWLAxiom> axioms=Stream.empty();
        InputStream ontologyData=readData("ontologies/test/lightagent.owl");      
        
        ontocore.addDevice(ontologyData, id);
        
  
                  
        
        try
          {
            OWLOntologyManager localM=OWLManager.createOWLOntologyManager();
            localM.loadOntologyFromOntologyDocument(new File("ontologies/main/onto-as.owl"));
            OWLOntology agent=localM.loadOntologyFromOntologyDocument(new File("ontologies/test/lightagent.owl"));
            OWLOntology config=localM.loadOntologyFromOntologyDocument(new File("ontologies/test/alan-config.owl"));
            OWLOntology request=localM.loadOntologyFromOntologyDocument(new File("ontologies/test/user-request.owl"));
          

            
            ontocore.addDataToDataSetOntology(agent.axioms());
            ontocore.addDataToDataSetOntology(config.axioms());
            ontocore.addDataToDataSetOntology(request.axioms());
            ontocore.getMainManager().removeOntology(request);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
                
        
         System.out.println("Main ontology axioms count: " +ontocore.getMainOntology().getAxiomCount());
         System.out.println("Dataset ontology axioms count: " +ontocore.getDatasetOntology().getAxiomCount());
        
         //Testing a select query
        String query=readQuery("ontologies/test/query.sparql");
            
        try
          {
            QueryExecution execQ = ontocore.createQuery(ontocore.getDatasetOntology(), query);
            ResultSet res=ontocore.performSelectQuery(execQ);
            System.out.println(ResultSetFormatter.asText(res));
          } catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }

        //Testing a construct query
        query=readQuery("ontologies/test/querycon.sparql");
        
          try
          {
            
            QueryExecution execQ = ontocore.createQuery(ontocore.getDatasetOntology(), query);
            System.out.println("Output:");
            //ontocore.performConstructQuery(execQ).forEach(System.out::println);            
            ontocore.addDataToDataSetOntology(ontocore.performConstructQuery(execQ));
           
            
          } catch (OWLOntologyCreationException | IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        
         try
          {            
            ontocore.removePermanentDevice(id);
          } 
        catch (OWLOntologyStorageException | OWLOntologyCreationException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        
      }
  }
