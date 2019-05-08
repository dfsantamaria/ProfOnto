/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.main;

import dmi.unict.it.ontoas.core.OntoASCore;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class main
  {
    public static void main(String[] args)
      {
        File ontoFile=new File("ontologies/main/onto-as.owl");
        File dataFile=new File("ontologies/main/dataset.owl");
        OntoASCore ontocore=new OntoASCore();
        try
          {
            ontocore.setMainOntology(ontoFile);
            ontocore.setDataBehaviorOntology(dataFile);
            //ontocore.setConfiguration(new ArrayList<>(Arrays.asList("config","devices")));
            
            
          } 
        catch (OWLOntologyCreationException | OWLOntologyStorageException ex)
          {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
          }
        System.out.println(ontocore.getMainOntology().getAxiomCount());
        
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
            ontocore.addDataToDataSetOntology(ontocore.performConstructQuery(execQ));
           
            
          } catch (OWLOntologyCreationException | IOException ex)
          {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }

*/