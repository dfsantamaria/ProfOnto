/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.core;

import java.io.File;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OntoASCore
  {
    OWLOntologyManager manager; 
    OWLDataFactory datafactory;
    OWLOntology mainOntology;
    
    public OntoASCore ()
      {
        manager = OWLManager.createOWLOntologyManager(); //create the manager  
        datafactory = manager.getOWLDataFactory();
      }
    
    public void setMainOntology(File inputFile) throws OWLOntologyCreationException 
      {        
        mainOntology=manager.loadOntologyFromOntologyDocument(inputFile);
      }           
  }
