/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.test;

import static dmi.unict.it.profonto.test.mainTest.readData;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author danie
 */
public class TestLoad
  {
    public static void main (String[] args)
      {
        try
          {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            manager.loadOntologyFromOntologyDocument(new File("ontologies/main/oasis.owl"));
            
            InputStream assistantData=readData("ontologies/test/rasb/lightagent-from-template.owl");           
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(assistantData);
            
            assistantData=readData("ontologies/test/rasb/rasb-lightagent.owl");
            ontology = manager.loadOntologyFromOntologyDocument(assistantData);
            
            System.out.println(ontology.getOntologyID().getOntologyIRI().toString());
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(TestLoad.class.getName()).log(Level.SEVERE, null, ex);
          }
      
      }
  }
