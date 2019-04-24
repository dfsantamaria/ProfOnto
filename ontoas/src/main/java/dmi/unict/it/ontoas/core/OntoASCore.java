/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.core;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.util.Pair;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OntoASCore extends OntologyCore
  {
    private HashMap<String, Pair<OWLOntology,File>> devices; 
    private OWLOntology dataset;
    private Configuration configuration;    
    
    public OntoASCore()
       {
         super();
         devices=new HashMap<>();
         int paths=3;
         configuration=new Configuration(paths);         
         dataset=null;         
       }
  
    private Configuration getConfiguration(){return configuration;}
    public void setMainOntologiesPath(Path path){this.getConfiguration().getPaths()[0]=path;}
    public Path getMainOntologiesPath(){return this.getConfiguration().getPaths()[0];}
    public void setOntologiesDevicesPath(Path path){this.getConfiguration().getPaths()[1]=path;}
    public Path getOntologiesDevicesPath(){return this.getConfiguration().getPaths()[1];}
    
    
    
    
    
    
    /**
     * Sets the dataset ontology from file
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDatasetOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException 
      {        
        dataset= this.getMainManager().loadOntologyFromOntologyDocument(inputFile);               
      } 
    
     /**
     * Sets the dataset ontology from file name
     * @param iri The IRI of the ontology
     * @param name the name of the file  serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDatasetOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException 
      {        
        OWLOntology dt=this.getMainManager().createOntology(IRI.create(iri));        
        File inputFile=new File(this.getMainOntologiesPath()+File.separator+name);   
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));                    
        setDatasetOntology(inputFile);            
        addImportInDataset(this.getDatasetOntology().getOntologyID().getOntologyIRI().get());         
      } 
    
    
    /**
     * Imports an ontology into the dataset ontology
     * @param iri The iri to import into the dataset ontology
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void addImportInDataset(IRI iri) throws OWLOntologyStorageException, OWLOntologyCreationException
      {
        OWLImportsDeclaration importDeclaration=this.getDataFactory().getOWLImportsDeclaration(iri);
        this.getMainManager().applyChange(new AddImport(getDatasetOntology(), importDeclaration));       
        this.getMainManager().saveOntology(this.getDatasetOntology());
        this.getMainManager().loadOntology(this.getDatasetOntology().getOntologyID().getOntologyIRI().get());            
      }
    
     /**
     * Removes the import of an ontology into the dataset ontology
     * @param iri The iri to remove from the dataset ontology
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
     public void removeImportInDataset(IRI iri) throws OWLOntologyStorageException, OWLOntologyCreationException
      {
        OWLImportsDeclaration importDeclaration=this.getDataFactory().getOWLImportsDeclaration(iri);
        this.getMainManager().applyChange(new RemoveImport(this.getDatasetOntology(), importDeclaration));
        this.getMainManager().saveOntology(this.getDatasetOntology());
        this.getMainManager().loadOntology(this.getDatasetOntology().getOntologyID().getOntologyIRI().get());    
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
     
      
     
    public OWLOntology configureDevice(OWLOntology ontodevice, Stream<OWLAxiom> axioms)
    {      
      //Edit here to get configuration data from user        //
      ChangeApplied changes=ontodevice.addAxioms(axioms);      
      try {            
            this.getMainManager().saveOntology(ontodevice);
        } 
      catch (OWLOntologyStorageException ex) {
            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
        }
      return ontodevice;
    }
    
       
    /**
     * insert a new device given its ontology data
     * @param ontologyData the inputstream representing the ontology data
     * @param id the name of the ontology file representing the device
     * @return the created ontology
   */
    public OWLOntology addDevice(InputStream ontologyData, String id)
    { 
        OWLOntology ontodevice=null;
        try
          {      
            ontodevice=this.getMainManager().loadOntologyFromOntologyDocument(ontologyData);
            addImportInDataset(ontodevice.getOntologyID().getOntologyIRI().get());          
            
            File file=new File(getOntologiesDevicesPath()+File.separator+id+".owl");       
            FileOutputStream outStream = new FileOutputStream(file);               
              
            this.getMainManager().saveOntology(ontodevice, new OWLXMLDocumentFormat(), outStream);
            this.getDevices().put(id, new Pair(ontodevice,file));
            outStream.close();
            
           } 
        catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
          }
        return ontodevice;
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
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public void removePermanentDevice(String id) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
       Pair tmp= (Pair<OWLOntology, File>)this.getDevices().remove(id);
       this.getMainManager().removeOntology((OWLOntology)tmp.getKey());
       removeImportInDataset(((OWLOntology)tmp.getKey()).getOntologyID().getOntologyIRI().get());
       ((File)tmp.getValue()).delete(); //always be sure to close all the open streams
       
    }

    public void loadDevicesFromPath(boolean toimport) throws OWLOntologyCreationException, OWLOntologyStorageException
      {
         Path path=this.getOntologiesDevicesPath();
          //HashMap<String, Pair<OWLOntology,File>>
         File[] files=path.toFile().listFiles();
         for(int i=0; i<files.length; i++)
           {
              if(files[i].isFile())
                {
                  String name=files[i].getName();
                  OWLOntology ontology= this.getMainManager().loadOntologyFromOntologyDocument(files[i]);
                  this.getDevices().put(name, new Pair(ontology, files[i]));    
                  if(toimport)
                    this.addImportInDataset(ontology.getOntologyID().getOntologyIRI().get());
                }
           }
           
      }  
  }
