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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private final HashMap<String, String> devices; //IDdevice, IDOntology
    private final HashMap<String, Pair<String,String>> devConfig; //IDconfig <IDdevice, IDOntology>
    private HashMap<String, String> users; //IDdevice, IDOntology
    private Pair<String, OWLOntology> databelief;
    private final Configuration configuration;  
    private List<InferredAxiomGenerator<? extends OWLAxiom>> generators;
    
    public OntoASCore()
       {
         super();
         devices=new HashMap<>();
         devConfig=new HashMap<>();
         int paths=3;
         configuration=new Configuration(paths);         
         databelief=null;  
         generators = new ArrayList<>();
         setDefaultReasonerGenerators(generators);       
       }
    
    
   private void setDefaultReasonerGenerators(List<InferredAxiomGenerator<? extends OWLAxiom>> generators)
      {
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
       // generators.add(new InferredSubDataPropertyAxiomGenerator());
        generators.add(new InferredSubObjectPropertyAxiomGenerator());
        List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms = new ArrayList<>();
        generators.addAll(individualAxioms);
        generators.add(new InferredDisjointClassesAxiomGenerator());      
      }
   
   
   
    private Configuration getConfiguration(){return configuration;}      
    public void setMainOntologiesPath(Path path){this.getConfiguration().getPaths()[0]=path; createFolder(path);}
    public Path getMainOntologiesPath(){return this.getConfiguration().getPaths()[0];}
    public void setOntologiesDevicesPath(Path path){this.getConfiguration().getPaths()[1]=path; createFolder(path);}
    public Path getOntologiesDevicesPath(){return this.getConfiguration().getPaths()[1];}
    public void setOntologiesDeviceConfigurationsPath(Path path){this.getConfiguration().getPaths()[2]=path; createFolder(path);}
    public Path getOntologiesDeviceConfigurationPath(){return this.getConfiguration().getPaths()[2];}
    
    

    
    
    
    
    /**
     * Creates the folder from the given path if it does not exist
     * @param path the path of the folder
     */
    public void createFolder(Path path)
    {
      File directory = path.toFile();
      if (! directory.exists())
        directory.mkdirs();
    }
    
    public void startReasoner()
      {
        ReasonerFactory rf=new ReasonerFactory();
        org.semanticweb.HermiT.Configuration config= new org.semanticweb.HermiT.Configuration();
        config.ignoreUnsupportedDatatypes = true;        
        setReasoner(rf.createReasoner(this.getDataBeliefOntology(),config));
        this.getReasoner().precomputeInferences(InferenceType.CLASS_HIERARCHY, 
                                                 InferenceType.CLASS_ASSERTIONS, 
                                                 InferenceType.OBJECT_PROPERTY_HIERARCHY, 
                                                 InferenceType.DATA_PROPERTY_HIERARCHY, 
                                                 InferenceType.OBJECT_PROPERTY_ASSERTIONS);
      }
    
    
    
    /**
     * Sets the dataset ontology from file
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDataBeliefOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException 
      {        
        databelief= new Pair(inputFile.getName(), this.getMainManager().loadOntologyFromOntologyDocument(inputFile));               
      } 
    
     /**
     * Sets the dataset ontology from file name
     * @param iri The IRI of the ontology
     * @param name the name of the file  serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDataBeliefOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException 
      {        
        OWLOntology dt=this.getMainManager().createOntology(IRI.create(iri));        
        File inputFile=new File(this.getMainOntologiesPath()+File.separator+name);   
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));                    
        setDataBeliefOntology(inputFile);       
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
        this.getMainManager().applyChange(new AddImport(getDataBeliefOntology(), importDeclaration));       
        this.getMainManager().saveOntology(this.getDataBeliefOntology());
        this.getMainManager().loadOntology(this.getDataBeliefOntology().getOntologyID().getOntologyIRI().get());            
      }
    
     /**
     * Removes the import of an ontology into the dataset ontology
     * @param iri The iri to remove from the dataset ontology
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
     public void removeImportInDataset(IRI iri) throws OWLOntologyStorageException, OWLOntologyCreationException
      {
        OWLImportsDeclaration importDeclaration=this.getMainManager().getOWLDataFactory().getOWLImportsDeclaration(iri);
        this.getMainManager().applyChange(new RemoveImport(this.getDataBeliefOntology(), importDeclaration));
        this.getMainManager().saveOntology(this.getDataBeliefOntology());
        this.getMainManager().loadOntology(this.getDataBeliefOntology().getOntologyID().getOntologyIRI().get());    
      }
    
    
     /**
     * Returns the dataset ontology
     * @return the dataset ontology
     */
    public Pair<String, OWLOntology> getDataBeliefInfo()
      {
         return this.databelief;
      }
    
      /**
     * Returns the dataset ontology
     * @return the dataset ontology
     */
    public OWLOntology getDataBeliefOntology()
      {
         return getDataBeliefInfo().getValue();
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
     * Returns the set of devices configurations
     * @return the HashMap containing the devices configuaration
     */
    public HashMap getDeviceConfigurations()
     {
        return devConfig;
     }
    
      /**
     * Returns the set of users
     * @return the HashMap containing the  users
     */
    public HashMap getUsers()
     {
        return users;
     }
    
    /**
     * Adds to the given ontology a set of axioms
     * @param ontology the ontology to be extended with the axioms
     * @param axioms the axioms to be added
     * @return the extended ontology
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
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
          addAxiomsToOntology(this.getDataBeliefOntology(), axioms);      
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
        boolean consistencyCheck = this.getReasoner().isConsistent();
        if (consistencyCheck)
          {             
        InferredOntologyGenerator iog = new InferredOntologyGenerator( this.getReasoner(), generators);        
        iog.fillOntology(this.getMainManager().getOWLDataFactory(), this.getMainManager().getOntology(IRI.create(storeOntology)));        
        
       getReasoner().flush();
       this.getMainManager().saveOntology(this.getMainManager().getOntology(IRI.create(storeOntology)),
                                           IRI.create(new File(file)));
          }
        else {System.out.println("Inconsistent Knowledge base");
         }
      }
    
     private void syncReasonerDataBelief() throws OWLOntologyStorageException
      {
        this.syncReasoner(this.getDataBeliefOntology().getOntologyID().getOntologyIRI().get().toString(),
                this.getDataBeliefInfo().getKey());
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
            syncReasonerDataBelief();
            
           } 
        catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
          }
        return ontodevice;
    }
    
    
     /**
     * Returns the ontology corresponding to the given device  id
     * @param id the id of the device 
     * @return the ontology representing the device
     */
    public OWLOntology getDevice(String id)
    {
      String tmp= (String)this.getDevices().get(id);
      return this.getMainManager().getOntology(IRI.create(tmp));
    }
    
    
    /**
     * Returns the ontology corresponding to the given device id configuration
     * @param id the id of the device configuration
     * @return the ontology representing the device configuration
     */
    public Pair<String,String> getDeviceConfiguration(String id)
    {       
      return  (Pair<String,String>) this.getDeviceConfigurations().get(id);
    }
    
      
    /**
     * Removes a given device
     * @param id the id of the device to be removed
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public void removePermanentDevice(String id) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
       OWLOntology ontology=this.getDevice(id);
       String tmp= (String)this.getDevices().remove(id); //this.getMainManager().getOntology(IRI.create(tmp));             
       this.getMainManager().removeOntology(ontology);
       removeImportInDataset(IRI.create(tmp));
       (new File(this.getOntologiesDevicesPath()+File.separator+id+".owl")).delete(); //always be sure to close all the open streams
       removePermanentConfigurations(id);
       
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
                  this.getDevices().put(name, new Pair(ontology.getOntologyID().getOntologyIRI().get().toString(), files[i]));    
                  if(toimport)
                    this.addImportInDataset(ontology.getOntologyID().getOntologyIRI().get());
                  
                  //Getting configurations from the current device
                  
                  File confFolder=new File(this.getOntologiesDeviceConfigurationPath()+File.separator+name);
                  if(confFolder.isDirectory())
                    {
                      File[] confs=confFolder.listFiles();
                      for (int j=0; j<confs.length;j++)
                         {
                            ontology= this.getMainManager().loadOntologyFromOntologyDocument(confs[j]);//IDconfig <IDdevice, IDOntology> 
                            this.getDeviceConfigurations().put(confs[j].getName(), new Pair( name, ontology.getOntologyID().getOntologyIRI().get().toString()));  
                            if(toimport)
                               this.addImportInDataset(ontology.getOntologyID().getOntologyIRI().get());
                         }
                    }
                }
           }         
           
      }  

    /**
     * Adds a configuration device
     * @param deviceConfig data configuration
     * @param idDevice ID of the device
     * @param idConfig ID of the configuration
     * @return the ontology representing the configuration
     */
    public OWLOntology addDeviceConfiguration(InputStream deviceConfig, String idDevice, String idConfig)
      {
        File directory = new File(this.getOntologiesDeviceConfigurationPath()+File.separator+idDevice);
        if (! directory.exists())
            directory.mkdir();
        
        OWLOntology ontodevConf=null;
        try
          {      
            ontodevConf=this.getMainManager().loadOntologyFromOntologyDocument(deviceConfig);
            addImportInDataset(ontodevConf.getOntologyID().getOntologyIRI().get());          
            String filesource=directory.getAbsolutePath()+File.separator+idConfig+".owl";
            File file=new File(filesource);       
            FileOutputStream outStream = new FileOutputStream(file);              
              
            this.getMainManager().saveOntology(ontodevConf, new OWLXMLDocumentFormat(), outStream);
            this.getDeviceConfigurations().put(idConfig, new Pair(idDevice, ontodevConf.getOntologyID().getOntologyIRI().get().toString()));
            outStream.close();            
            this.syncReasonerDataBelief();
            
           } 
        catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
          {
            Logger.getLogger(OntoASCore.class.getName()).log(Level.SEVERE, null, ex);
          }
        return ontodevConf;        
      }
    
    
      /**
     * Removes a given device
     * @param id the id of the device to be removed
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public void removePermanentConfiguration(String id) throws OWLOntologyStorageException, OWLOntologyCreationException
    {       
       Pair<String, String> device = (Pair<String, String>) this.getDeviceConfigurations().remove(id);
       OWLOntology ontology= this.getMainManager().getOntology(IRI.create((String)device.getValue()));             
       this.getMainManager().removeOntology(ontology);
       removeImportInDataset(IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
       String file= this.getOntologiesDeviceConfigurationPath()+File.separator+(String)device.getKey()+File.separator+id+".owl";       
       (new File(file)).delete(); //always be sure to close all the open streams       
    }
    
    /**
     * Removes all the configurations of a device
     * @param idDevice the device id
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void removePermanentConfigurations(String idDevice) throws OWLOntologyStorageException, OWLOntologyCreationException
      {
        Iterator it = getDeviceConfigurations().entrySet().iterator(); //IDconfig <IDdevice, IDOntology>        
         while (it.hasNext())
          {
            Map.Entry entry = (Map.Entry)it.next();
            Pair<String,String> pair = (Pair<String,String>) entry.getValue();
            if(((String)pair.getKey()).equals(idDevice))
             {
               OWLOntology ontology= this.getMainManager().getOntology(IRI.create((String)pair.getValue()));             
               this.getMainManager().removeOntology(ontology);
               removeImportInDataset(IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
               String file= this.getOntologiesDeviceConfigurationPath()+File.separator+(String)pair.getKey()+File.separator+
                             (String)entry.getKey()+".owl";       
               (new File(file)).delete();                       
             }
          }
         String folder=this.getOntologiesDeviceConfigurationPath()+File.separator+idDevice;         
         (new File(folder)).delete();             
      }
    
  }
