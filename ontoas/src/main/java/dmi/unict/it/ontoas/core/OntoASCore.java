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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDisjointClassesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredIndividualAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OntoASCore extends OntologyCore
  {
    private HashMap<String, String> devices; //IDdevice, IDOntology
    private HashMap<String, Pair<String,String>> devConfig; //IDconfig <IDdevice, File>
    private OWLOntology dataset;
    private Configuration configuration;    
    
    public OntoASCore()
       {
         super();
         devices=new HashMap<>();
         devConfig=new HashMap<>();
         int paths=3;
         configuration=new Configuration(paths);         
         dataset=null;           
       }
  
    private Configuration getConfiguration(){return configuration;}
    public void setMainOntologiesPath(Path path){this.getConfiguration().getPaths()[0]=path;}
    public Path getMainOntologiesPath(){return this.getConfiguration().getPaths()[0];}
    public void setOntologiesDevicesPath(Path path){this.getConfiguration().getPaths()[1]=path;}
    public Path getOntologiesDevicesPath(){return this.getConfiguration().getPaths()[1];}
    public void setOntologiesDeviceConfigurationsPath(Path path){this.getConfiguration().getPaths()[2]=path;}
    public Path getOntologiesDeviceConfigurationPath(){return this.getConfiguration().getPaths()[2];}
    
    public void startReasoner()
      {
        ReasonerFactory rf=new ReasonerFactory();
        org.semanticweb.HermiT.Configuration config= new org.semanticweb.HermiT.Configuration();
        config.ignoreUnsupportedDatatypes = true;
        setReasoner(rf.createReasoner(getDatasetOntology(),config));
      }
    
    
    
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
        addImportInDataset(this.getMainOntology().getOntologyID().getOntologyIRI().get());         
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
   
     /**
     * Returns the set of connected devices
     * @return the HashMap containing the connected devices
     */
    public HashMap getDeviceConfigurations()
     {
        return devConfig;
     }
    /**
     * Adds to the given ontology a set of axioms
     * @param ontology the ontology to be extended with the axioms
     * @param axioms the axioms to be added
     * @return the extended ontology
     */
    public OWLOntology addAxiomsToOntology(OWLOntology ontology, Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
      ChangeApplied changes=ontology.addAxioms(axioms);      
      try {            
            this.getMainManager().saveOntology(ontology);
            this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
        } 
      catch (OWLOntologyStorageException ex) {
            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
        }
      return ontology;
    }
    
    
    public void addDataToDataSetOntology(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
      {
          addAxiomsToOntology(this.getDatasetOntology(), axioms);      
      }
     
//    public OWLOntology configureDevice(OWLOntology ontodevice, Stream<OWLAxiom> axioms)
//    {      
//      //Edit here to get configuration data from user        //
//      ChangeApplied changes=ontodevice.addAxioms(axioms);      
//      try {            
//            this.getMainManager().saveOntology(ontodevice);
//        } 
//      catch (OWLOntologyStorageException ex) {
//            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
//        }
//      return ontodevice;
//    }
    
    public void syncReasoner(String storeOntology, String file) throws OWLOntologyStorageException
      {
        boolean consistencyCheck = getReasoner().isConsistent();
        if (consistencyCheck)
          {
            getReasoner().precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS, 
                                               InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY, 
                                               InferenceType.OBJECT_PROPERTY_ASSERTIONS);
        List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<>();
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredClassAssertionAxiomGenerator());
        generators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        generators.add(new InferredEquivalentClassAxiomGenerator());
        generators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
        generators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
        generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
        generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        // NOTE: InferredPropertyAssertionGenerator significantly slows down
        // inference computation
        generators.add(new org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredSubDataPropertyAxiomGenerator());
        generators.add(new InferredSubObjectPropertyAxiomGenerator());
        List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms = new ArrayList<>();
        generators.addAll(individualAxioms);
        generators.add(new InferredDisjointClassesAxiomGenerator());
        InferredOntologyGenerator iog = new InferredOntologyGenerator( getReasoner(), generators);
        //OWLOntology inferredAxiomsOntology = manager.createOntology(IRI.create("http://www.dmi.unict.it/webreasoning/2017/exercise/1G1InfHermit"));
        iog.fillOntology(this.getDataFactory(), this.getMainManager().getOntology(IRI.create(storeOntology)));        
        //System.out.println("Axioms: "+ontology.getAxiomCount());     
       // System.out.println("Inferred Axioms: "+inferredAxiomsOntology.getAxiomCount());
       //File infFile=new File("ontologie/E1G1_infHermit.owl");
        this.getMainManager().saveOntology(this.getMainManager().getOntology(IRI.create(storeOntology)),
                                           IRI.create(new File(file)));
          }
        else {System.out.println("Inconsistent Knowledge base");
         }
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
            String filesource=getOntologiesDevicesPath()+File.separator+id+".owl";
            File file=new File(filesource);       
            FileOutputStream outStream = new FileOutputStream(file);               
              
            this.getMainManager().saveOntology(ontodevice, new OWLXMLDocumentFormat(), outStream);
            this.getDevices().put(id, ontodevice.getOntologyID().getOntologyIRI().get().toString());
            outStream.close();
            this.syncReasoner(ontodevice.getOntologyID().getOntologyIRI().get().toString(), file.getAbsolutePath());
            
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
       String tmp= (String)this.getDevices().remove(id);
       OWLOntology ontology=this.getMainManager().getOntology(IRI.create(tmp));
       this.getMainManager().removeOntology(ontology);
       removeImportInDataset(IRI.create(tmp));
       (new File(this.getOntologiesDevicesPath()+File.separator+id+".owl")).delete(); //always be sure to close all the open streams
       
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
