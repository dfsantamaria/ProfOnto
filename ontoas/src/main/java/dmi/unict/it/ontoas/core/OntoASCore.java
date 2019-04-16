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
public class OntoASCore
  {
    private final OWLOntologyManager manager; 
    private final OWLDataFactory datafactory;
    private OWLOntology mainOntology;
    
    public OWLDataFactory getDataFactory()
      {
         return this.datafactory;
      }
    
    public OWLOntology getMainOntology()
      {
         return this.mainOntology;
      }
    
    public OWLOntologyManager getMainManager()
      {
        return this.manager;
      }
    
    public OntoASCore ()
      {
        manager = OWLManager.createOWLOntologyManager(); //create the manager  
        datafactory = manager.getOWLDataFactory();
      }
    
    public void setMainOntology(File inputFile) throws OWLOntologyCreationException 
      {        
        mainOntology=manager.loadOntologyFromOntologyDocument(inputFile);
      } 
    
    
    
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
            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }
        
      }
    
    public boolean addAxioms(OWLOntology mergeOntology)
      {
          ChangeApplied changes=getMainOntology().addAxioms(mergeOntology.axioms());
          return changes== ChangeApplied.SUCCESSFULLY;
      }
  }
