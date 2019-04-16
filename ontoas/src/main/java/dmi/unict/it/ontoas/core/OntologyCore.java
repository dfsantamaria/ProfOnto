/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.core;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;


/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OntologyCore
  {
    private final OWLOntologyManager manager; 
    private final OWLDataFactory datafactory;
    private OWLOntology mainOntology;
    
    /**
     * Returns the main datafactory
     * @return the main datafactory
     */
    public OWLDataFactory getDataFactory()
      {
         return this.datafactory;
      }
    
    /**
     * Returns the main ontology
     * @return the main ontology
     */
    public OWLOntology getMainOntology()
      {
         return this.mainOntology;
      }
    
    /**
     * Returns the main ontology manager
     * @return the main ontology manager
     */
    public OWLOntologyManager getMainManager()
      {
        return this.manager;
      }
    
    /**
     *   Constructs an empty OntologyCore object with empty ontology
     */
    public OntologyCore ()
      {
        manager = OWLManager.createOWLOntologyManager(); //create the manager  
        datafactory = manager.getOWLDataFactory();
      }
    
    /**
     * Assigns the main ontology to the OntologyCore object
     * @param inputFile
     * @throws OWLOntologyCreationException
     */
    public void setMainOntology(File inputFile) throws OWLOntologyCreationException 
      {        
        mainOntology=manager.loadOntologyFromOntologyDocument(inputFile);
      } 
    
    /**
     * Merges  the main ontology with a given file representing an ontology
     * @param mergeFile The file to be merged
     * @return true if the merge has been successifully done
     */    
    public boolean addAxioms(File mergeFile)
      {
        OWLOntologyManager managerMerge=OWLManager.createOWLOntologyManager();         
        try
          {
            OWLOntology mergeOntology= managerMerge.loadOntologyFromOntologyDocument(mergeFile);
            return addAxioms(mergeOntology);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntologyCore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }
        
      }
    
    /**
     * Merges the main ontology with a given  ontology
     * @param mergeOntology the ontology to be merged
     * @return true if the merge has been successifully done
     */
    public boolean addAxioms(OWLOntology mergeOntology)
      {
          ChangeApplied changes=getMainOntology().addAxioms(mergeOntology.axioms());
          return changes== ChangeApplied.SUCCESSFULLY;
      }
  }
