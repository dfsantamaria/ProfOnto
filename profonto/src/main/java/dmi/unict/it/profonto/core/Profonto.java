/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Resource;
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
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.rdf.turtle.parser.TurtleOntologyParser;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
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
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import ru.avicomp.ontapi.OntologyModel;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class Profonto extends OntologyCore
{
    private OWLOntology mainAbox;
    private final HashMap<String, String> devices; //IDdevice, IDOntology
    private final HashMap<String, String[]> devConfig; //IDconfig <IDdevice-  IDOntology- IDuser
    private final HashMap<String, String> users; //IDconfig <IDdevice, IDOntology> //IDdevice, IDOntology
    private final HashMap<String, String> queries; //FileName, content
    private Pair<String, OWLOntology> databehavior;
    private Pair<String, OWLOntology> databelief;
    private final Configuration configuration;
    private List<InferredAxiomGenerator<? extends OWLAxiom>> generators;

    public Profonto()
    {
        super();
        devices = new HashMap<>();
        devConfig = new HashMap<>();
        users = new HashMap<>();
        queries=new HashMap<>();
        int paths = 5;
        configuration = new Configuration(paths);
        databehavior = null;
        databelief = null;
        mainAbox=null;        
        generators = new ArrayList<>();
        setDefaultReasonerGenerators(generators);
    }

    public void setMainAbox(File inputFile) throws OWLOntologyCreationException 
      {        
        mainAbox= this.getMainManager().loadOntologyFromOntologyDocument(inputFile);
      } 
    
     public OWLOntology getMainAbox() 
      {        
        return mainAbox;
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
        //  generators.add(new org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredSubDataPropertyAxiomGenerator());
        generators.add(new InferredSubObjectPropertyAxiomGenerator());

        List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms = new ArrayList<>();
        generators.addAll(individualAxioms);
        generators.add(new InferredDisjointClassesAxiomGenerator());
    }

    private Configuration getConfiguration()
    {
        return configuration;
    }

    public void setMainOntologiesPath(Path path)
    {
        this.getConfiguration().getPaths()[0] = path;
        createFolder(path);
    }

    public Path getMainOntologiesPath()
    {
        return this.getConfiguration().getPaths()[0];
    }

    public void setOntologiesDevicesPath(Path path)
    {
        this.getConfiguration().getPaths()[1] = path;
        createFolder(path);
    }

    public Path getOntologiesDevicesPath()
    {
        return this.getConfiguration().getPaths()[1];
    }

    public void setOntologiesDeviceConfigurationsPath(Path path)
    {
        this.getConfiguration().getPaths()[2] = path;
        createFolder(path);
    }

    public Path getOntologiesDeviceConfigurationPath()
    {
        return this.getConfiguration().getPaths()[2];
    }

    public void setOntologiesUsersPath(Path path)
    {
        this.getConfiguration().getPaths()[3] = path;
        createFolder(path);
    }
    
  public Path getOntologiesUsersPath()
    {
        return this.getConfiguration().getPaths()[3];
    }
  
    public void setQueryPath(Path path)
    {
       this.getConfiguration().getPaths()[4]=path; 
       for(File f : path.toFile().listFiles())
         {
           try
             {
               String name=f.getName();
               String content=readQuery(f.getCanonicalPath());                
               getQueries().put(name, content);
             } 
           catch (IOException ex)
             {
               Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
             }
         }    
    }
    
    public HashMap<String,String> getQueries()
      {
        return this.queries;
      }
    
    public Path getQueryPath()
    {
      return this.getConfiguration().getPaths()[4];
    }
    
   

    /**
     * Creates the folder from the given path if it does not exist
     *
     * @param path the path of the folder
     */
    public void createFolder(Path path)
    {
        File directory = path.toFile();
        if (!directory.exists())
        {
            directory.mkdirs();
        }
    }

//    public void startReasoner()
//      {
//        ReasonerFactory rf=new ReasonerFactory();
//        org.semanticweb.HermiT.Configuration config= new org.semanticweb.HermiT.Configuration();
//        config.ignoreUnsupportedDatatypes = true;        
//        setReasoner(rf.createReasoner(this.getDataBeliefOntology(),config));
//        this.getReasoner().precomputeInferences(InferenceType.CLASS_HIERARCHY, 
//                                                 InferenceType.CLASS_ASSERTIONS, 
//                                                 InferenceType.OBJECT_PROPERTY_HIERARCHY, 
//                                                 InferenceType.DATA_PROPERTY_HIERARCHY, 
//                                                 InferenceType.OBJECT_PROPERTY_ASSERTIONS);
//      }
    /**
     * Sets the data-belief ontology from file
     *
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDataBeliefOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        databelief = new Pair(this.getMainOntologiesPath() + File.separator + inputFile.getName(), this.getMainManager().loadOntologyFromOntologyDocument(inputFile));
    }

    /**
     * Sets the data-behavior ontology from file
     *
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDataBehaviorOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        databehavior = new Pair(this.getMainOntologiesPath() + File.separator + inputFile.getName(), this.getMainManager().loadOntologyFromOntologyDocument(inputFile));
    }

    /**
     * Sets the data-belief ontology from file name
     *
     * @param iri The IRI of the ontology
     * @param name the name of the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDataBeliefOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        OWLOntology dt = this.getMainManager().createOntology(IRI.create(iri));
        File inputFile = new File(this.getMainOntologiesPath() + File.separator + name);
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));
        setDataBeliefOntology(inputFile);
        addImportToOntology(this.getDataBehaviorOntology(), this.getMainOntology().getOntologyID().getOntologyIRI().get());
        addImportToOntology(this.getDataBeliefOntology(), this.getDataBehaviorOntology().getOntologyID().getOntologyIRI().get());
    }

    /**
     * Sets the data-belief ontology from file name
     *
     * @param iri The IRI of the ontology
     * @param name the name of the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDataBehaviorOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        OWLOntology dt = this.getMainManager().createOntology(IRI.create(iri));
        File inputFile = new File(this.getMainOntologiesPath() + File.separator + name);
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));
        setDataBehaviorOntology(inputFile);
        addImportToOntology(this.getDataBehaviorOntology(), this.getMainOntology().getOntologyID().getOntologyIRI().get());
    }

    /**
     * Imports an ontology into the given ontology
     *
     * @param ontology The ontology to which add the import axiom
     * @param iri The iri to import into the dataset ontology
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void addImportToOntology(OWLOntology ontology, IRI iri) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        OWLImportsDeclaration importDeclaration = this.getDataFactory().getOWLImportsDeclaration(iri);
        this.getMainManager().applyChange(new AddImport(ontology, importDeclaration));
        this.getMainManager().saveOntology(ontology);
        this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
    }

    /**
     * Removes the import of an ontology from the given ontology
     *
     * @param ontology The ontology the import axiom has to be removed from.
     * @param iri The iri to remove from the dataset ontology
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void removeImportFromOntology(OWLOntology ontology, IRI iri) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        OWLImportsDeclaration importDeclaration = this.getMainManager().getOWLDataFactory().getOWLImportsDeclaration(iri);
        this.getMainManager().applyChange(new RemoveImport(ontology, importDeclaration));
        this.getMainManager().saveOntology(ontology);
        this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
    }

    /**
     * Returns the data-behavior ontology
     *
     * @return the data-behavior ontology
     */
    public Pair<String, OWLOntology> getDataBehaviorInfo()
    {
        return this.databehavior;
    }

    /**
     * Returns the data-belief ontology
     *
     * @return the data-belief ontology
     */
    public OWLOntology getDataBeliefOntology()
    {
        return getDataBeliefInfo().getValue();
    }

    /**
     * Returns the data-belief ontology
     *
     * @return the data-belief ontology
     */
    public Pair<String, OWLOntology> getDataBeliefInfo()
    {
        return this.databelief;
    }

    /**
     * Returns the data-behavior ontology
     *
     * @return the data-behavior ontology
     */
    public OWLOntology getDataBehaviorOntology()
    {
        return getDataBehaviorInfo().getValue();
    }

    /**
     * Returns the set of connected devices
     *
     * @return the HashMap containing the connected devices
     */
    public HashMap getDevices()
    {
        return devices;
    }

    /**
     * Returns the set of devices configurations
     *
     * @return the HashMap containing the devices configuaration
     */
    public HashMap getDeviceConfigurations()
    {
        return devConfig;
    }

    /**
     * Returns the set of users
     *
     * @return the HashMap containing the users
     */
    public HashMap getUsers()
    {
        return users;
    }

    /**
     * Adds to the given ontology a set of axioms
     *
     * @param ontology the ontology to be extended with the axioms
     * @param axioms the axioms to be added
     * @return the extended ontology
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public OWLOntology addAxiomsToOntology(OWLOntology ontology, Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        ChangeApplied changes = ontology.addAxioms(axioms);
        try
        {
            this.getMainManager().saveOntology(ontology);
            this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
        } catch (OWLOntologyStorageException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ontology;
    }

    public void addDataToDataBehavior(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        addAxiomsToOntology(this.getDataBehaviorOntology(), axioms);
    }
    
     public void addDataToDataBelief(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
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
//            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
//        }
//      return ontodevice;
//    }
    private void syncReasoner(String storeOntology, String file) throws OWLOntologyStorageException
    {
        OWLOntology m = this.getMainManager().getOntology(IRI.create(storeOntology));
        //   m.imports().forEach(ont ->ont.axioms().forEach(a-> m.addAxiom(a)));     
        ReasonerFactory rf = new ReasonerFactory();
        org.semanticweb.HermiT.Configuration config = new org.semanticweb.HermiT.Configuration();
        config.ignoreUnsupportedDatatypes = true;
        OWLReasoner reasoner = rf.createReasoner(m, config);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
                InferenceType.CLASS_ASSERTIONS,
                InferenceType.OBJECT_PROPERTY_HIERARCHY,
                InferenceType.DATA_PROPERTY_HIERARCHY,
                InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        boolean consistencyCheck = reasoner.isConsistent();
        if (consistencyCheck)
        {
            InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, generators);
            iog.fillOntology(this.getMainManager().getOWLDataFactory(), m);
            reasoner.flush();
            this.getMainManager().saveOntology(m,
                    IRI.create(new File(file)));
        } else
        {
            System.out.println("Inconsistent Knowledge base");
        }
    }

    /**
     * Runs the reasoner on the behavior data
     *
     * @throws OWLOntologyStorageException
     */
    public void syncReasonerDataBehavior() throws OWLOntologyStorageException
    {
        this.syncReasoner(this.getDataBehaviorOntology().getOntologyID().getOntologyIRI().get().toString(),
                this.getDataBehaviorInfo().getKey());
    }

    private void refreshOntology(OWLOntology ontology) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        Stream<OWLImportsDeclaration> imports = ontology.importsDeclarations();
        ontology.removeAxioms(ontology.axioms());
        imports.forEach(dec -> this.getMainManager().applyChange(new AddImport(ontology, dec)));
        this.getMainManager().saveOntology(ontology);
        this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
    }

    /**
     * refreshes the behavior dataset
     *
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void refreshDataBehavior() throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        refreshOntology(this.getDataBehaviorOntology());
    }

    /**
     * insert a new user given its ontology data
     *
     * @param ontologyData the inputstream representing the ontology data
     * @param id the name of the ontology file representing the device
     * @return the created ontology
     */
    public OWLOntology addUser(InputStream ontologyData, String id)
    {
        OWLOntology ontouser = null;
        try
        {
            ontouser = this.getMainManager().loadOntologyFromOntologyDocument(ontologyData);
            addImportToOntology(this.getDataBehaviorOntology(), ontouser.getOntologyID().getOntologyIRI().get());
            String filesource = this.getOntologiesUsersPath() + File.separator + id + ".owl";
            File file = new File(filesource);

            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontouser.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

            FileOutputStream outStream = new FileOutputStream(file);

            this.getMainManager().saveOntology(ontouser, new OWLXMLDocumentFormat(), outStream);
            this.getUsers().put(id, ontouser.getOntologyID().getOntologyIRI().get().toString());
            outStream.close();
            //   syncReasonerDataBehavior();            
        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ontouser;
    }

    /**
     * insert a new device given its ontology data
     *
     * @param ontologyData the inputstream representing the ontology data
     * @param id the name of the ontology file representing the device
     * @return the created ontology
     */
    public OWLOntology addDevice(InputStream ontologyData, String id)
    {
        OWLOntology ontodevice = null;
        try
        {
            ontodevice = this.getMainManager().loadOntologyFromOntologyDocument(ontologyData);
            addImportToOntology(this.getDataBehaviorOntology(), ontodevice.getOntologyID().getOntologyIRI().get());
            String filesource = this.getOntologiesDevicesPath() + File.separator + id + ".owl";
            File file = new File(filesource);

            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontodevice.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

            FileOutputStream outStream = new FileOutputStream(file);

            this.getMainManager().saveOntology(ontodevice, new OWLXMLDocumentFormat(), outStream);
            this.getDevices().put(id, ontodevice.getOntologyID().getOntologyIRI().get().toString());
            outStream.close();
            //  syncReasonerDataBehavior();

        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ontodevice;
    }

    /**
     * Returns the ontology corresponding to the given user id
     *
     * @param id the id of the device
     * @return the ontology representing the user
     */
    public OWLOntology getUser(String id)
    {
        String tmp = (String) this.getUsers().get(id);
        return this.getMainManager().getOntology(IRI.create(tmp));
    }

    /**
     * Returns the ontology corresponding to the given device id
     *
     * @param id the id of the device
     * @return the ontology representing the device
     */
    public OWLOntology getDevice(String id)
    {
        String tmp = (String) this.getDevices().get(id);
        return this.getMainManager().getOntology(IRI.create(tmp));
    }

    /**
     * Returns the ontology corresponding to the given device id configuration
     *
     * @param id the id of the device configuration
     * @return the ontology representing the device configuration
     */
    public String[] getDeviceConfiguration(String id)
    {
        return (String[]) this.getDeviceConfigurations().get(id);
    }

    /**
     * Removes a given user
     *
     * @param id the id of the user to be removed
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     * @throws java.io.IOException
     */
    public void removePermanentUser(String id) throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        OWLOntology ontology = this.getUser(id);
        String tmp = (String) this.getUsers().remove(id); //this.getMainManager().getOntology(IRI.create(tmp));             
        this.getMainManager().removeOntology(ontology);
        removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(tmp));
        File f = new File(this.getOntologiesUsersPath() + File.separator + id + ".owl");
        this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                IRI.create(f.getCanonicalFile())));
        f.delete(); //always be sure to close all the open streams
        removePermanentConfigurationsFromUser(id);
    }

    /**
     * Removes a given device
     *
     * @param id the id of the device to be removed
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     * @throws java.io.IOException
     */
    public void removePermanentDevice(String id) throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        OWLOntology ontology = this.getDevice(id);
        String tmp = (String) this.getDevices().remove(id); //this.getMainManager().getOntology(IRI.create(tmp));             
        this.getMainManager().removeOntology(ontology);
        removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(tmp));
        File f = new File(this.getOntologiesDevicesPath() + File.separator + id + ".owl");
        this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                IRI.create(f.getCanonicalFile())));

        f.delete(); //always be sure to close all the open streams
        removePermanentConfigurationsFromDevice(id);

    }

    public void loadDevicesFromPath(boolean toimport) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        Path path = this.getOntologiesDevicesPath();
        //HashMap<String, Pair<OWLOntology,File>>
        File[] files = path.toFile().listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isFile())
            {
                String name = files[i].getName();
                OWLOntology ontology = this.getMainManager().loadOntologyFromOntologyDocument(files[i]);
                this.getDevices().put(name, new Pair(ontology.getOntologyID().getOntologyIRI().get().toString(), files[i]));
                if (toimport)
                {
                    this.addImportToOntology(this.getDataBehaviorOntology(), ontology.getOntologyID().getOntologyIRI().get());
                    this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                            IRI.create(files[i].getCanonicalFile())));
                }

                //Getting configurations from the current device
                File confFolder = new File(this.getOntologiesDeviceConfigurationPath() + File.separator + name);
                if (confFolder.isDirectory())
                {
                    File[] confs = confFolder.listFiles();
                    for (int j = 0; j < confs.length; j++)
                    {
                        ontology = this.getMainManager().loadOntologyFromOntologyDocument(confs[j]);//IDconfig <IDdevice, IDOntology> 
                        /*attention here - need to be modified since the user is not got from the ontology*/
                        this.getDeviceConfigurations().put(confs[j].getName(), new String[]
                        {
                            name, ontology.getOntologyID().getOntologyIRI().get().toString(), "iduser"
                        });
                        if (toimport)
                        {
                            this.addImportToOntology(this.getDataBehaviorOntology(), ontology.getOntologyID().getOntologyIRI().get());
                            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                                    IRI.create(confs[j].getCanonicalFile())));
                        }
                    }
                }
            }
        }

    }

    /**
     * Adds a configuration device
     *
     * @param deviceConfig data configuration
     * @param idDevice ID of the device
     * @param idConfig ID of the configuration
     * @return the ontology representing the configuration
     */
    public OWLOntology addDeviceConfiguration(InputStream deviceConfig, String idDevice, String idConfig, String iduser)
    {
        File directory = new File(this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice);
        if (!directory.exists())
        {
            directory.mkdir();
        }

        OWLOntology ontodevConf = null;
        try
        {
            ontodevConf = this.getMainManager().loadOntologyFromOntologyDocument(deviceConfig);
            addImportToOntology(this.getDataBehaviorOntology(), ontodevConf.getOntologyID().getOntologyIRI().get());
            String filesource = directory.getAbsolutePath() + File.separator + idConfig + ".owl";
            File file = new File(filesource);
            FileOutputStream outStream = new FileOutputStream(file);

            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontodevConf.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

            this.getMainManager().saveOntology(ontodevConf, new OWLXMLDocumentFormat(), outStream);
            this.getDeviceConfigurations().put(idConfig, new String[]
            {
                idDevice, ontodevConf.getOntologyID().getOntologyIRI().get().toString(), iduser
            });
            outStream.close();
            //    this.syncReasonerDataBehavior();

        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ontodevConf;
    }

    /**
     * Removes a given device
     *
     * @param id the id of the device to be removed
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    //IDconfig <IDdevice-  IDOntology- IDuser
    public void removePermanentConfigurationFromDevice(String id) throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        String[] device = (String[]) this.getDeviceConfigurations().remove(id);
        OWLOntology ontology = this.getMainManager().getOntology(IRI.create((String) device[1]));
        this.getMainManager().removeOntology(ontology);
        removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
        String file = this.getOntologiesDeviceConfigurationPath() + File.separator + (String) device[1] + File.separator + id + ".owl";
        File f = new File(file);
        this.getMainManager().getIRIMappers().remove(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                IRI.create(f.getCanonicalFile())));
        f.delete(); //always be sure to close all the open streams       
    }

    /**
     * Removes all the configurations of a device
     *
     * @param idDevice the device id
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void removePermanentConfigurationsFromDevice(String idDevice) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        Iterator it = getDeviceConfigurations().entrySet().iterator(); //IDconfig <IDdevice, IDOntology, IDuser> 
        ArrayList<String> toremove = new ArrayList();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            String[] pair = (String[]) entry.getValue();
            if (((String) pair[0]).equals(idDevice))
            {
                OWLOntology ontology = this.getMainManager().getOntology(IRI.create((String) pair[1]));
                this.getMainManager().removeOntology(ontology);
                removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
                String file = this.getOntologiesDeviceConfigurationPath() + File.separator + pair[0] + File.separator
                        + (String) entry.getKey() + ".owl";

                (new File(file)).delete();
                toremove.add((String) entry.getKey());
            }
        }
        String folder = this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice;
        (new File(folder)).delete();
        for (String s : toremove)
        {
            getDeviceConfigurations().remove(s);
        }
    }

    /**
     * Removes all the configurations of a user
     *
     * @param idDevice the device id
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void removePermanentConfigurationsFromUser(String idUser) throws OWLOntologyStorageException, OWLOntologyCreationException
    {
        Iterator it = getDeviceConfigurations().entrySet().iterator(); //IDconfig Pair <IDdevice, <IDOntology IDuser> >
        ArrayList<String> toremove = new ArrayList();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            String[] pair = (String[]) entry.getValue();
            String idDevice = pair[0];
            String userMatch = pair[2];
            if (userMatch.equals(idUser))
            {
                OWLOntology ontology = this.getMainManager().getOntology(IRI.create(pair[1]));
                this.getMainManager().removeOntology(ontology);
                removeImportFromOntology(this.getDataBehaviorOntology(), IRI.create(ontology.getOntologyID().getOntologyIRI().get().toString()));
                String file = this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice + File.separator
                        + (String) entry.getKey() + ".owl";
                (new File(file)).delete();
                toremove.add((String) entry.getKey());
                String folder = this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice;
                File f = new File(folder);
                String[] files = f.list();
                if (files.length == 0)
                {
                    f.delete();
                }

            }
        }

        for (String s : toremove)
        {
            getDeviceConfigurations().remove(s);
        }
    }

    
     public static String readQuery(String path)
      {
        String query="";
        BufferedReader queryReader=null;
         try
          {
            queryReader=new  BufferedReader(new FileReader(path));
            String currentLine="";
            while ((currentLine = queryReader.readLine()) != null)
              {
                  query+=currentLine+"\n";
              }
            queryReader.close();
          } 
        catch (FileNotFoundException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            
          }
        catch (IOException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
          }
      
        return query;
      }
    
    /**
     *
     * @param request The InputStream object representing the request
     * @param IRItask The IRI of the OASIS Task instance from the request
     * @param IRIuser The IRI of the OASIS USER instance of the user
     * @param IRIexec The IRI of the OASIS TaskExcecution instance to create
     * @param IRIGoalExec The IRI of the OASIS GoalExecution instance to create
     * @return The OWLAxiom Stream containing the result of the query
     */
    public Stream<OWLAxiom> acceptUserRequest(InputStream request, String IRItask, String IRIuser, String IRIexec, String IRIGoalExec)
    {
        OntologyModel res=null;
        OWLOntology ontology;
        
        try
          {
            ontology = this.getMainManager().loadOntologyFromOntologyDocument(request);
          
            //Merge and query here         
            String query=this.getQueries().get("prefix01.sparql");            
            query+="PREFIX prof: <"+this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#>\n";
            query+="CONSTRUCT {\n";
            query+=(this.getQueries().get("construct01a.sparql").replaceAll("//exec//","<"+IRIexec+">").replaceAll("//goal//","<"+IRIGoalExec+">"));
            query+=(this.getQueries().get("construct01b.sparql"));
            query+="}";
            query+="WHERE { \n";
            query+=(this.getQueries().get("body01a.sparql").replaceAll("//task//", "<"+IRItask+">"));
            query+=(this.getQueries().get("body01b.sparql"));
            query+=(this.getQueries().get("body01c.sparql").replaceAll("//user//", "<"+IRIuser+">"));
            query+=this.getQueries().get("body01d.sparql");
            query+="}";
          
            //System.out.println(query);   
            res=performQuery(ontology, query);
            //res.axioms().forEach(System.out::println); 
          }
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
          }   
        if(res==null) 
            return null;
        return res.axioms();
        
    }

    public OntologyModel performQuery(OWLOntology ontology, String query)
      {
        OntologyModel res=null;
          try
            {        
        this.getMainManager().ontologies().forEach(x->ontology.addAxioms(x.axioms()));
        QueryExecution execQ = this.createQuery(ontology, query);
        res=this.performConstructQuery(execQ);
        this.addDataToDataBelief(res.axioms());
        this.getMainManager().removeOntology(ontology);
       } 
        catch (OWLOntologyCreationException | IOException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
      }
    
    /**
     * Returns the set of axioms concerning the execution of the given request
     * @param request The ontology of the request     
     * @return the set of axioms representing the execution of the given request
     */
    public Stream<OWLAxiom> parseRequest(InputStream request)
    {
        OntologyModel res=null;
        Stream<OWLAxiom> axioms;
        OWLOntology ontology;        
        try
          {
            ontology = this.getMainManager().loadOntologyFromOntologyDocument(request); 
            this.getDataBeliefOntology().addAxioms(ontology.axioms());
            String IRIrequest=ontology.getOntologyID().getOntologyIRI().get().toString();
            String prefix=this.getQueries().get("prefix01.sparql");
            prefix+="PREFIX prof: <"+this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#>\n";
            prefix+="PREFIX abox: <"+this.getMainAbox().getOntologyID().getOntologyIRI().get().toString()+"#>\n";
            prefix+="PREFIX base: <"+IRIrequest+">\n";
            String query=prefix;
            //Subquery over request
            String subquery=prefix+this.getQueries().get("body02a.sparql");             
            QueryExecution execQ = this.createQuery(ontology, subquery);
            ResultSet setIRI=execQ.execSelect();                        
            QuerySolution qs=setIRI.next();
            
            String[] subqueryParam={qs.getResource("selected_device").getURI(),
                                   qs.getResource("task").getURI(),
                                   qs.getResource("operation").getURI(),
                                   qs.getResource("device_object").getURI(),            
                                   qs.getResource("obtype").getURI(), 
                                   null};            
            Resource r=qs.getResource("parameter");
            if(r!=null)             
                subqueryParam[5]=r.getURI();           
            query+="CONSTRUCT {\n";        
            for(String s : subqueryParam)
              query+="<"+s+"> "+"rdf:type owl:NamedIndividual.";         
            
            String taskExec="<"+subqueryParam[1]+"_execution>";
            if(subqueryParam[5]!=null)
              {
                query+=taskExec + "prof:hasTaskParameter "+ " <"+subqueryParam[5]+"> ." ;
              }
            //query+="?selected_device" + " <"+subqueryParam[2]+">" + " <"+subqueryParam[4]+"> ." ; 
            
            query+="?selected_device rdf:type owl:NamedIndividual.";
            query+="?selected_device prof:performs "+ taskExec+" .";
            query+=taskExec+" rdf:type prof:TaskExecution .";
            query+=taskExec+" prof:hasTaskObject "+ "<"+subqueryParam[0]+">"+" .";
            query+=taskExec+" prof:hasTaskOperator "+ "<"+subqueryParam[2]+">"+" .";
            
            query+="}\n";
            query+="WHERE { \n";         
            query+=this.getQueries().get("body02b.sparql").replaceAll("//operation//", "<"+subqueryParam[2]+">")
                    .replaceAll("//obtype//", "<"+subqueryParam[4]+">"); 
            query+="}";
                        
            res=performQuery(ontology, query);
            //res.axioms().forEach(System.out::println);         
          }
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
          }   
        if(res==null) 
            return null;
        return res.axioms();
        
    }
    
    public Stream<OWLAxiom> retrieveAssertions(String iriInd)
      {         
         
         OWLOntology onto=this.getDataBeliefOntology();
         OWLNamedIndividual individual=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(iriInd);
         
         Stream<OWLAxiom> axioms=onto.axioms().filter(x->x.isLogicalAxiom())
                                              .filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION) || x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))
                                              .filter(x->x.containsEntityInSignature(individual));
         return axioms;        
      }
    
}
