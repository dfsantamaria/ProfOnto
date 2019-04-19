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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.vocab.OWL2Datatype;


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
        mainOntology=null;
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
    
    
    /**
     * Deletes from the main ontology an  ontology represented by the given file
     * @param removeFile the ontology to be removed
     * @return true if the delete has been successifully done
     */
    public boolean removeAxioms(File removeFile)
      {
        OWLOntologyManager managerMerge=OWLManager.createOWLOntologyManager();         
        try
          {
            OWLOntology removeOntology= managerMerge.loadOntologyFromOntologyDocument(removeFile);
            return removeAxioms(removeOntology);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntologyCore.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }
        
      }
    
    /**
     * Removes from the main ontology a given  ontology
     * @param removeOntology the ontology to be removed
     * @return true if the delete has been successifully done
     */
    public boolean removeAxioms(OWLOntology removeOntology)
      {
          ChangeApplied changes=getMainOntology().remove(removeOntology.axioms());
          return changes== ChangeApplied.SUCCESSFULLY;
      }
    
      
    public OWLNamedIndividual createIndividualInBase(String indIriBase, OWLClass owlclass, OWLOntology ontology, OWLDataFactory datafactory) {
        OWLNamedIndividual individual = datafactory.getOWLNamedIndividual(indIriBase);
        ontology.addAxiom(datafactory.getOWLClassAssertionAxiom(owlclass, individual));
        return individual;
    }

    public OWLNamedIndividual createIndividualInBase(String indIriBase, String classIriBase, OWLOntology ontology, OWLDataFactory datafactory) {
        OWLNamedIndividual individual = datafactory.getOWLNamedIndividual(indIriBase);
        ontology.addAxiom(datafactory.getOWLClassAssertionAxiom(datafactory.getOWLClass(classIriBase), individual));
        return individual;
    }

    public void createObjectPropertyAssertionAxiom(String property, OWLNamedIndividual subject, OWLNamedIndividual object, OWLOntology ontology, OWLDataFactory datafactory) {
        ontology.addAxiom(datafactory.getOWLObjectPropertyAssertionAxiom(
                datafactory.getOWLObjectProperty(property), subject, object));
    }

    public void createDataPropertyAssertionAxiom(String property, OWLNamedIndividual individual, String value, OWL2Datatype datatype, OWLOntology ontology, OWLDataFactory datafactory) {
        ontology.addAxiom(datafactory.getOWLDataPropertyAssertionAxiom(
                datafactory.getOWLDataProperty(property), individual,
                datafactory.getOWLLiteral(value, datatype)));
    }
    
 }
