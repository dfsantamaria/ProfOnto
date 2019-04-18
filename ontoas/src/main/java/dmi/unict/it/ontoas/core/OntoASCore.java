/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.core;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OntoASCore extends OntologyCore
  {
    private HashMap<String, Pair<OWLOntology,File>> devices; 
    private OWLOntology dataset;
    private ArrayList <String> configuration;
    
    public OntoASCore()
       {
         super();
         devices=new HashMap<>();
         dataset=null;
         configuration=new ArrayList<>();        
       }
    
    public void setConfiguration(ArrayList<String> conf)
      {
         configuration=conf;
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
     * @param ontologyData the string representing the ontology data
     */
    public void insertDevice(String ontologyData)
    {         
      String id= "dev"+ new Timestamp(new Date().getTime()).toString();
      File file=new File(id);      
        try
          {
            FileWriter fwrite=new FileWriter(file);
            
            fwrite.write(ontologyData);
            OWLOntology tmp=this.getMainManager().loadOntologyFromOntologyDocument(file);       
            this.getDevices().put(id, new Pair(tmp,file));  
          } 
        catch (IOException | OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
          }
      
          
    }
    
    /**
     * Returns the ontology corresponding to the given device id
     * @param id the id of the device
     * @return the ontology representing the device
     */
    public OWLOntology getDevice(String id)
    {
      return (OWLOntology) this.getDevices().get(id);
    }
    
    /**
     * Removes a given device
     * @param id the id of the device to be removed
     */
    public void removePermanentDevice(String id)
    {
       Pair tmp= (Pair<OWLOntology, File>)this.getDevices().remove(id);
       this.getMainManager().removeOntology((OWLOntology)tmp.getKey());
       ((File)tmp.getValue()).delete();
    }
            
  }
