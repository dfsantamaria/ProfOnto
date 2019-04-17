/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.core;

import java.io.File;
import java.util.HashMap;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OntoASCore extends OntologyCore
  {
    private HashMap<String,OWLOntology> devices; 
    private OWLOntology dataset;
    public OntoASCore()
       {
         super();
         devices=new HashMap<>();
         dataset=null;
       }
    
    /**
     * Sets the dataset ontology from file
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     */
    public void setDatasetOntology(File inputFile) throws OWLOntologyCreationException 
      {        
        dataset= this.getMainManager().loadOntologyFromOntologyDocument(inputFile);
      } 
    
     /**
     * Returns the dataset ontology
     * @return the dataset ontology
     */
    public OWLOntology getDatasetOntology()
      {
         return this.dataset;
      }
    
    /**
     * Returns the set of connected devices
     * @return the HashMap containing the connected devices
     */
    public HashMap getDevices()
     {
        return devices;
     }
    
    /**
     * Inserts a new device
     * @param id the device id
     * @param device the ontology representing the device
     */
    public void insertDevices(String id, OWLOntology device)
    {
      this.getDevices().put(id, device);
    }
    
    /**
     * Returns the ontology corresponding to the given device id
     * @param id the id of the device
     * @return the OWLOntologyy representing the device
     */
    public OWLOntology getDevice(String id)
    {
       return (OWLOntology) this.getDevices().get(id);
    }
    
    /**
     * Removes a given device
     * @param id the id of the device to be removed
     */
    public void removeDevice(String id)
    {
       this.getDevices().remove(id);
    }
            
  }
