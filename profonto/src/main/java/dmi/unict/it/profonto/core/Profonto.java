/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.core;

import java.io.BufferedReader;
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
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredIndividualAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
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
    private final HashMap<String, String> satellite; //filename, idontology
    
    private Pair<String, OWLOntology> databehavior;
    private Pair<String, OWLOntology> databelief;
    private Pair<String, OWLOntology> datarequest;
    
    private final Configuration configuration;
    private final List<InferredAxiomGenerator<? extends OWLAxiom>> generators;

    public Profonto()
    {
        super();
        devices = new HashMap<>();
        devConfig = new HashMap<>();
        users = new HashMap<>();
        queries=new HashMap<>(); 
        satellite = new HashMap<>();
        int paths = 6;
        configuration = new Configuration(paths);
        databehavior = null;
        databelief = null;
        datarequest = null;
       
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
       // generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        // NOTE: InferredPropertyAssertionGenerator significantly slows down
        // inference computation
        generators.add(new org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredSubDataPropertyAxiomGenerator());
        generators.add(new InferredSubObjectPropertyAxiomGenerator());

        List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms = new ArrayList<>();
        generators.addAll(individualAxioms);
     //   generators.add(new InferredDisjointClassesAxiomGenerator()); //THIS ENTRY IS PROBLEMATIC
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
    
    public Path getSatellitePath()
    {
      return this.getConfiguration().getPaths()[5];
    }

    public void setSatellitePath(Path path)
    {
        this.getConfiguration().getPaths()[5] = path;
        createFolder(path);
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
     * Sets the data-request ontology from file
     *
     * @param inputFile the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     */
    public void setDataRequestOntology(File inputFile) throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        datarequest = new Pair(this.getMainOntologiesPath() + File.separator + inputFile.getName(), this.getMainManager().loadOntologyFromOntologyDocument(inputFile));
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
        //addImportToOntology(this.getDataBehaviorOntology(), this.getMainOntology().getOntologyID().getOntologyIRI().get());
        addImportToOntology(this.getDataBeliefOntology(), this.getDataRequestOntology().getOntologyID().getOntologyIRI().get());
    }

    
      /**
     * Sets the data-request ontology from file name
     *
     * @param iri The IRI of the ontology
     * @param name the name of the file serializing the ontology
     * @throws OWLOntologyCreationException
     * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException
     * @throws java.io.IOException
     */
    public void setDataRequestOntology(String iri, String name) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        OWLOntology dt = this.getMainManager().createOntology(IRI.create(iri));
        File inputFile = new File(this.getMainOntologiesPath() + File.separator + name);
        this.getMainManager().saveOntology(dt, new OWLXMLDocumentFormat(), IRI.create(inputFile));
        Profonto.this.setDataRequestOntology(inputFile);
        addImportToOntology(this.getDataRequestOntology(), this.getMainOntology().getOntologyID().getOntologyIRI().get());
        addImportToOntology(this.getDataRequestOntology(), this.getDataBehaviorOntology().getOntologyID().getOntologyIRI().get());
    }
    
    public void emptyRequestOntology() throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
       String iri=this.getDataRequestOntology().getOntologyID().getOntologyIRI().get().toString();          
       this.getMainManager().removeOntology(this.getDataRequestOntology());
       this.setDataRequestOntology(iri, new File(this.getDataRequestInfo().getKey()).getName());         
    }
    
    
    /**
     * Sets the data-beheavior ontology from file name
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
     * Returns the data-request ontology
     *
     * @return the data-request ontology
     */
    public OWLOntology getDataRequestOntology()
    {
        return getDataRequestInfo().getValue();
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
     * Returns the data-request ontology
     *
     * @return the data-request ontology
     */
    public Pair<String, OWLOntology> getDataRequestInfo()
    {
        return this.datarequest;
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

    public HashMap getSatellite()
      {
        return satellite;
      }
    /**
     * Adds to the given ontology a set of axioms
     *
     * @param ontology the ontology to be extended with the axioms
     * @param axioms the axioms to be added
     * @return the extended ontology
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public int addAxiomsToOntology(OWLOntology ontology, Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        ChangeApplied changes = ontology.addAxioms(axioms);
        try
        {            
            this.getMainManager().saveOntology(ontology);
            this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
        } catch (OWLOntologyStorageException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        if(changes.equals(ChangeApplied.SUCCESSFULLY))
                return 0;
        return -1;
    }

    public int removeAxiomsFromOntology(OWLOntology ontology, Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        ChangeApplied changes = ontology.remove(axioms);
        try
        {            
            this.getMainManager().saveOntology(ontology);
            this.getMainManager().loadOntology(ontology.getOntologyID().getOntologyIRI().get());
        } catch (OWLOntologyStorageException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        if(changes.equals(ChangeApplied.SUCCESSFULLY))
                return 0;
        return -1;
    }
    
    public void addDataToDataBehavior(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        addAxiomsToOntology(this.getDataBehaviorOntology(), axioms);
    }
    
    public boolean checkHasExecutionStatutInfo(OWLOntology ontology)
      {
        String query=getQueryPrefix(null)+"\n"+this.getQueries().get("ask02a.sparql");          
        boolean res=false;
        try
          {
            QueryExecution execQ = this.createQuery(ontology, query);
            res= execQ.execAsk();
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            return false;
          }        
        return res;
      }
    
    public int addDataToDataBelief(InputStream ontologystring) throws OWLOntologyCreationException
    {
        OWLOntology ontology=this.getMainManager().loadOntologyFromOntologyDocument(ontologystring);
        int r=0;
       // if(checkHasExecutionStatutInfo(ontology))
        // r=addAxiomsToOntology(this.getDataRequestOntology(), ontology.axioms()); 
        //else
        r=addAxiomsToOntology(this.getDataBeliefOntology(), ontology.axioms());   
        this.getMainManager().removeOntology(ontology);
        return r;
    }
    
    public int removeDataFromDataBelief(InputStream ontologystring) throws OWLOntologyCreationException
    {
        OWLOntology ontology=this.getMainManager().loadOntologyFromOntologyDocument(ontologystring);
        int r=0;
       // if(checkHasExecutionStatutInfo(ontology))
       //  r= removeAxiomsFromOntology(this.getDataRequestOntology(), ontology.axioms()); 
       // else
         r= removeAxiomsFromOntology(this.getDataBeliefOntology(), ontology.axioms());   
        this.getMainManager().removeOntology(ontology);
        return r;
    }
    
    public void addDataToDataBelief(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        addAxiomsToOntology(this.getDataBeliefOntology(), axioms);
    }

    public void addDataToDataRequest(Stream<OWLAxiom> axioms) throws OWLOntologyCreationException
    {
        addAxiomsToOntology(this.getDataRequestOntology(), axioms);
    } 

    private void syncReasoner(OWLOntology ontology, String file) throws OWLOntologyStorageException
    {     
          
        ReasonerFactory rf = new ReasonerFactory();
        org.semanticweb.HermiT.Configuration config = new org.semanticweb.HermiT.Configuration();
        config.ignoreUnsupportedDatatypes = true;
        OWLReasoner reasoner = rf.createReasoner(ontology, config);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY,
                InferenceType.CLASS_ASSERTIONS,
                InferenceType.OBJECT_PROPERTY_HIERARCHY,
                InferenceType.DATA_PROPERTY_HIERARCHY,
                InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        boolean consistencyCheck = reasoner.isConsistent();
        if (consistencyCheck)
        {
            InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, generators);
            iog.fillOntology(this.getMainManager().getOWLDataFactory(), ontology);
            reasoner.flush();
            if(!(file==null || file.equals("")))
                this.getMainManager().saveOntology(ontology,
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
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     */
    public void syncReasonerDataBehavior() throws OWLOntologyStorageException, OWLOntologyCreationException
    {      
        OWLOntology m = this.getDataBehaviorOntology();
        m.imports().forEach(ont ->ont.axioms().forEach(a-> m.addAxiom(a)));     
        this.syncReasoner(m, this.getDataBehaviorInfo().getKey());
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
     * @return the created ontology
     */
    public String addUser(InputStream ontologyData)
    {
        OWLOntology ontouser = null;
        String val[]=new String[]{""};
        try
        {
            
            ontouser = this.getMainManager().loadOntologyFromOntologyDocument(ontologyData);
            addImportToOntology(this.getDataBehaviorOntology(), ontouser.getOntologyID().getOntologyIRI().get()); 
            String userclass=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#User";
            ontouser.logicalAxioms().filter(x->x.isOfType(AxiomType.CLASS_ASSERTION)).forEach(
              element -> {
                            OWLClassAssertionAxiom ax=((OWLClassAssertionAxiom) element);
                            if(ax.getClassExpression().asOWLClass().toStringID().equals(userclass))                              
                                val[0]=getEntityName(ax.getIndividual().asOWLNamedIndividual().toStringID());                            
                         });  
            
            
            String filesource = this.getOntologiesUsersPath() + File.separator + val[0] + ".owl";
            File file = new File(filesource);

            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontouser.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

            FileOutputStream outStream = new FileOutputStream(file);

            this.getMainManager().saveOntology(ontouser, new OWLXMLDocumentFormat(), outStream);
            this.getUsers().put(val[0], ontouser.getOntologyID().getOntologyIRI().get().toString());
            outStream.close();
            //   syncReasonerDataBehavior();            
        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return val[0];
    }

    
    public String[] getConnectionInfo(String device)
    {       
     OWLOntology ontology= this.getDevice(device); 
     String[] value={"",""};
     ontology.axioms().filter(x->x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))
                      .forEach( x  ->
     {
        OWLDataPropertyAssertionAxiom ax = ((OWLDataPropertyAssertionAxiom) x);
        if(ax.getProperty().asOWLDataProperty().toStringID().equals(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasIPAddress"))
            value[0]= ax.getObject().getLiteral();
        else if(ax.getProperty().asOWLDataProperty().toStringID().equals(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasPortNumber"))
            value[1]= ax.getObject().getLiteral();
     });
    return value;
    }
    
    /**
     * insert a new device given its ontology data
     *
     * @return the created ontology
     */
    
    public String addDevice(String URL)
    {
        try
        {
           OWLOntology ontodevice=this.getMainManager().loadOntology(IRI.create(URL)); 
           return addDevice(ontodevice);
        }
        catch (OWLOntologyCreationException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
       
    }
    
    
    public String addDevice(InputStream ontologyData)
    {
      OWLOntology ontology;
        try
        {
          ontology = this.getMainManager().loadOntologyFromOntologyDocument(ontologyData);
        } 
        
       // catch (OWLOntologyAlreadyExistsException ex)
       // {
        //  String value= (String) this.getSatellite().get(ex.getOntologyID().getOntologyIRI().get().toString());          
       //   ontology= this.getMainManager().getOntology(ex.getOntologyID());
      //    if(value!=null)
      //        this.moveSatelliteData(ontology, this.getOntologiesDevicesPath().toString());
      //    return addDevice(ontology);            
       // } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            return null;
          }        
      return addDevice(ontology);
    }
    
    public String addDevice(OWLOntology ontodevice)
    {        
        String val[]={""}; 
        try
        {            
            this.moveSatelliteData(ontodevice, this.getOntologiesDevicesPath().toString());
            addImportToOntology(this.getDataBehaviorOntology(), ontodevice.getOntologyID().getOntologyIRI().get());            
            String deviceclass=this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#Device";
            ontodevice.logicalAxioms().filter(x->x.isOfType(AxiomType.CLASS_ASSERTION)).forEach(
              element -> {
                            OWLClassAssertionAxiom ax=((OWLClassAssertionAxiom) element);
                            if(ax.getClassExpression().asOWLClass().toStringID().equals(deviceclass))                              
                                val[0]=getEntityName(ax.getIndividual().asOWLNamedIndividual().toStringID());                            
                         });  
            
            String filesource = this.getOntologiesDevicesPath() + File.separator + val[0] + ".owl";
            File file = new File(filesource);

            this.getMainManager().getIRIMappers().add(new SimpleIRIMapper(ontodevice.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

          //  FileOutputStream outStream = new FileOutputStream(file);
          syncReasoner(ontodevice, filesource);
         //   this.getMainManager().saveOntology(ontodevice, new OWLXMLDocumentFormat(), outStream);
            this.getDevices().put(val[0], ontodevice.getOntologyID().getOntologyIRI().get().toString());
        //    outStream.close();
            //  syncReasonerDataBehavior();

        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return val[0];
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

    public void loadDevicesFromPath(boolean toimport) throws OWLOntologyStorageException, IOException
    {
        Path path = this.getOntologiesDevicesPath();
        //HashMap<String, Pair<OWLOntology,File>>
        File[] files = path.toFile().listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isFile())
            {
                try {
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
                   } catch (OWLOntologyCreationException ex) 
                     {
                      Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
                     }
            }
        }

    }

    
    public String getEntityName(String s)
      {
        return s.substring(s.lastIndexOf("#") + 1);
      }
    
    public String addDeviceConfiguration(InputStream deviceConfig) throws OWLOntologyCreationException
    {
       
      OWLOntology ontodevConf = null;
      String []vals=new String[]{"","",""};  
      try
        {           
            ontodevConf = this.getMainManager().loadOntologyFromOntologyDocument(deviceConfig);
            String settles=(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#settles");
            String config=(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#configurationProvidedBy");
            ontodevConf.logicalAxioms().filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)).forEach(
              element -> {
                            OWLObjectPropertyAssertionAxiom ax=((OWLObjectPropertyAssertionAxiom) element);
                            if(ax.getProperty().getNamedProperty().toStringID().equals(settles))                              
                                vals[0]=getEntityName(ax.getSubject().asOWLNamedIndividual().toStringID());
                            else if(ax.getProperty().getNamedProperty().toStringID().equals(config)) 
                              {
                                 vals[1]=getEntityName(ax.getSubject().asOWLNamedIndividual().toStringID());
                                 vals[2]=getEntityName(ax.getObject().asOWLNamedIndividual().toStringID());
                                
                              }
                         });  
            
            File directory = new File(this.getOntologiesDeviceConfigurationPath() + File.separator + vals[0]);
            if (!directory.exists())
            {
             directory.mkdir();
            }
            addImportToOntology(this.getDataBehaviorOntology(), ontodevConf.getOntologyID().getOntologyIRI().get());
            String filesource = directory.getAbsolutePath() + File.separator + vals[1] + ".owl";
            File file = new File(filesource);
            FileOutputStream outStream = new FileOutputStream(file);

            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontodevConf.getOntologyID().getOntologyIRI().get(),
                    IRI.create(file.getCanonicalFile())));

            this.getMainManager().saveOntology(ontodevConf, new OWLXMLDocumentFormat(), outStream);
            this.getDeviceConfigurations().put(vals[1], new String[]
            {
                vals[0], ontodevConf.getOntologyID().getOntologyIRI().get().toString(), vals[2]
            });
            outStream.close();
            //    this.syncReasonerDataBehavior();

        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return vals[1];      
    }
    
    /**
     * Adds a configuration device
     *
     * @param deviceConfig data configuration
     * @param idDevice ID of the device
     * @param idConfig ID of the configuration
     * @return the ontology representing the configuration
     */
//    public OWLOntology addDeviceConfiguration(InputStream deviceConfig, String idDevice, String idConfig, String iduser)
//    {
//        File directory = new File(this.getOntologiesDeviceConfigurationPath() + File.separator + idDevice);
//        if (!directory.exists())
//        {
//            directory.mkdir();
//        }
//
//        OWLOntology ontodevConf = null;
//        try
//        {
//            ontodevConf = this.getMainManager().loadOntologyFromOntologyDocument(deviceConfig);
//            addImportToOntology(this.getDataBehaviorOntology(), ontodevConf.getOntologyID().getOntologyIRI().get());
//            String filesource = directory.getAbsolutePath() + File.separator + idConfig + ".owl";
//            File file = new File(filesource);
//            FileOutputStream outStream = new FileOutputStream(file);
//
//            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontodevConf.getOntologyID().getOntologyIRI().get(),
//                    IRI.create(file.getCanonicalFile())));
//
//            this.getMainManager().saveOntology(ontodevConf, new OWLXMLDocumentFormat(), outStream);
//            this.getDeviceConfigurations().put(idConfig, new String[]
//            {
//                idDevice, ontodevConf.getOntologyID().getOntologyIRI().get().toString(), iduser
//            });
//            outStream.close();
//            //    this.syncReasonerDataBehavior();
//
//        } catch (IOException | OWLOntologyStorageException | OWLOntologyCreationException ex)
//        {
//            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return ontodevConf;
//    }

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
        String file = this.getOntologiesDeviceConfigurationPath() + File.separator + (String) device[0] + File.separator + id + ".owl";
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
    
//    /**
//     *
//     * @param request The InputStream object representing the request
//     * @param IRItask The IRI of the OASIS Task instance from the request
//     * @param IRIuser The IRI of the OASIS USER instance of the user
//     * @param IRIexec The IRI of the OASIS TaskExcecution instance to create
//     * @param IRIGoalExec The IRI of the OASIS GoalExecution instance to create
//     * @return The OWLAxiom Stream containing the result of the query
//     */
//    public Stream<OWLAxiom> acceptUserRequest(InputStream request, String IRItask, String IRIuser, String IRIexec, String IRIGoalExec)
//    {
//        OntologyModel res=null;
//        OWLOntology ontology;
//        
//        try
//          {
//            ontology = this.getMainManager().loadOntologyFromOntologyDocument(request);
//          
//            //Merge and query here         
//            String query=this.getQueries().get("prefix01.sparql");            
//            query+="PREFIX prof: <"+this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#>\n";
//            query+="CONSTRUCT {\n";
//            query+=(this.getQueries().get("construct01a.sparql").replaceAll("//exec//","<"+IRIexec+">").replaceAll("//goal//","<"+IRIGoalExec+">"));
//            query+=(this.getQueries().get("construct01b.sparql"));
//            query+="}";
//            query+="WHERE { \n";
//            query+=(this.getQueries().get("body01a.sparql").replaceAll("//task//", "<"+IRItask+">"));
//            query+=(this.getQueries().get("body01b.sparql"));
//            query+=(this.getQueries().get("body01c.sparql").replaceAll("//user//", "<"+IRIuser+">"));
//            query+=this.getQueries().get("body01d.sparql");
//            query+="}";
//          
//            //System.out.println(query);   
//            res=performQuery(ontology, query);
//            //res.axioms().forEach(System.out::println); 
//          }
//        catch (OWLOntologyCreationException ex)
//          {
//            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
//          }   
//        if(res==null) 
//            return null;
//        return res.axioms();
//        
//    }

    
     public OntologyModel performQuery(String query) throws OWLOntologyCreationException
      {
        OWLOntology ontology=this.getMainManager().createOntology();
        return this.performQuery(ontology,query);
      }
    
    public OntologyModel performQuery(OWLOntology ontology, String query)
      {
        OntologyModel res=null;
          try
            {        
        this.getMainManager().ontologies().forEach(x->ontology.addAxioms(x.axioms()));
        QueryExecution execQ = this.createQuery(ontology, query);
        res=this.performConstructQuery(execQ);
        this.addDataToDataRequest(res.axioms());
  //      this.getMainManager().removeOntology(ontology);
       } 
        catch (OWLOntologyCreationException | IOException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
      }
    
    
    
    
    private ArrayList<String[]> retrieveDependencies(OWLOntology ontology, String prefix) throws OWLOntologyCreationException
      {       
        String subquery = prefix + this.getQueries().get("body01b.sparql");        
        QueryExecution execQ = this.createQuery(ontology, subquery);
        ResultSet setIRI = execQ.execSelect();
        ArrayList<String[]> depends=new ArrayList();
       
        while(setIRI.hasNext())
          {
            QuerySolution qs = setIRI.next();
            String[] entry=new String[2];
            entry[0]=qs.getResource("taska").getURI();
            entry[1]=qs.getResource("taskb").getURI();
            depends.add(entry);           
          }
        return depends;
      }
    
    
    public String getQueryPrefix(String IRIrequest)
      {
         String prefix = this.getQueries().get("prefix01.sparql");
         prefix += "PREFIX prof: <" + this.getMainOntology().getOntologyID().getOntologyIRI().get().toString() + "#>\n";
         String mainID = this.getMainAbox().getOntologyID().getOntologyIRI().get().toString();
         prefix += "PREFIX abox: <" + mainID + "#>\n";
         if(IRIrequest!=null)
           prefix += "PREFIX base: <" + IRIrequest + ">\n";
         return prefix;
      }
    
    
    public void moveSatelliteData(OWLOntology ont, String path)
      {
        try
          {
            String key =ont.getOntologyID().getOntologyIRI().get().toString();
            String value= (String) this.getSatellite().get(key);
            if(value==null)
                return;
            
            this.getSatellite().remove(key);
            String name=value.substring(value.lastIndexOf(File.separator)+1, value.length());
            File oldfile=new File(this.getSatellitePath() + File.separator + name);            
            this.getMainManager().removeIRIMapper(new SimpleIRIMapper(ont.getOntologyID().getOntologyIRI().get(),
                                                  IRI.create(oldfile.getCanonicalFile())));                     
            oldfile.delete();
            
            String filesource = this.getOntologiesDevicesPath()+ "/" + name;
            File file = new File(filesource);          
            FileOutputStream outStream = new FileOutputStream(file);
            this.getMainManager().saveOntology(ont, new OWLXMLDocumentFormat(), outStream);
            this.getSatellite().put(ont.getOntologyID().getOntologyIRI().get().toString(),filesource);
            outStream.close();
          } 
        catch (IOException | OWLOntologyStorageException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
    
    
    public void addSatelliteData(OWLOntology ontology)
      {       
        try
        {            
            String name=ontology.getOntologyID().getOntologyIRI().get().toString();
            int ind=name.lastIndexOf("#"); 
            name=name.substring(name.lastIndexOf("/")+1, (ind >= 0)? ind: name.length());            
            String filesource = this.getSatellitePath() + File.separator + name;
            File file = new File(filesource);            
            this.getMainManager().addIRIMapper(new SimpleIRIMapper(ontology.getOntologyID().getOntologyIRI().get(),
                                                                   IRI.create(file.getCanonicalFile())));
            FileOutputStream outStream = new FileOutputStream(file);
            this.getMainManager().saveOntology(ontology, new OWLXMLDocumentFormat(), outStream);
            this.getSatellite().put(ontology.getOntologyID().getOntologyIRI().get().toString(), name);
            outStream.close();
            
            System.out.println(ontology.getOntologyID().getOntologyIRI().get().toString() + " "+ name);//
            
        } 
        catch (IOException | OWLOntologyStorageException ex)
        {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
        }      
      }
    
    
    
    
    /**
     * Returns the set of axioms concerning the execution of the given request
     * @param request The ontology of the request     
     * @return the set of axioms representing the execution of the given request
     */
    public OWLOntology parseRequest(InputStream request)
      {
        OntologyModel res = null;
        ArrayList<String[]> depends=new ArrayList();
        ArrayList<String[]> configs=null;
        Stream<OWLAxiom> axioms = Stream.of();
       
        OWLOntology ontology;
        try
          {
            ontology = this.getMainManager().loadOntologyFromOntologyDocument(request);
            boolean[] brequest= {false};
            ontology.axioms().filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)).forEach
            (
            element -> { 
              OWLObjectPropertyAssertionAxiom ax=(OWLObjectPropertyAssertionAxiom) element;
             if( ax.getProperty().asOWLObjectProperty().toStringID().equals(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#requests"))
                brequest[0]= true;
               }
             ); 
             
            if(brequest[0]==false)  //satellite
              {                 
                addSatelliteData(ontology);                
                return null;
              }                       
            //prefix
            String IRIrequest = ontology.getOntologyID().getOntologyIRI().get().toString();
            String prefix = getQueryPrefix(IRIrequest);

            //dependencies
            depends= retrieveDependencies(ontology, prefix);
            
            //Filtering configuration            
            String subquery = prefix + this.getQueries().get("body01a.sparql");
            QueryExecution execQ = this.createQuery(ontology, subquery);
            ResultSet setIRI = execQ.execSelect();          
                                 
            configs = new ArrayList();           
            while (setIRI.hasNext())
                 {   
                   
                   QuerySolution qs = setIRI.next();
                   String[] entry=new String[3];
                   entry[0]= qs.getResource("task").getURI();
                   entry[1]= qs.getResource("prop").getURI();
                   entry[2]= qs.getResource("thetype").getURI();     
                   configs.add(entry);                   
                 }
              
//            for(Pair<String,String> s : configs)
//                  System.out.println(s.getKey()+" "+s.getValue());
            //ontology.;
            syncReasoner(ontology, null);
            this.getDataRequestOntology().addAxioms(ontology.axioms());
            ontology.addAxioms(this.getDataBehaviorOntology().axioms());

            //String query = prefix; //here
            //Subquery over request
            subquery = prefix + this.getQueries().get("body02a.sparql");
            execQ = this.createQuery(ontology, subquery);
            setIRI = execQ.execSelect();
            //iterate over the number of requests
            while (setIRI.hasNext())
              {                
                String query = prefix;
                QuerySolution qs = setIRI.next();
                String[] subqueryParam =
                  {
                    qs.getResource("selected_device").getURI(),
                    qs.getResource("task").getURI(),
                    qs.getResource("operation").getURI(),
                    qs.getResource("device_object").getURI(),
                    null, //qs.getResource("obtype").getURI(),
                    //qs.getResource("hasTask").getURI(),  
                    null
                  };                

                 String theobject = " <" + subqueryParam[3] + ">"; //edit this line
                
                 execQ = this.createQuery(ontology, prefix + this.getQueries().get("ask01a.sparql").replaceAll("//theobj//", theobject));
                 if(execQ.execAsk())
                  {
                    theobject = " ?device_object ";
                  }
                
                Resource r = qs.getResource("tparameter");
                if (r != null)
                  {
                    subqueryParam[4] = r.getURI();
                    axioms = retrieveAssertions(subqueryParam[4], ontology.axioms());
                  }
                r=qs.getResource("paramtype");
                if(r!=null)
                {
                  subqueryParam[5]=r.getURI();
                }
                
                query += "CONSTRUCT {\n";

                for (String s : subqueryParam)
                  {
                    if (s != null)
                      {
                        query += "<" + s + "> " + "rdf:type owl:NamedIndividual. \n";
                      }
                  }

                String taskExec = "<" + subqueryParam[1] + "_execution>";
                if (subqueryParam[4] != null)
                  {
                    query += taskExec + " prof:hasTaskInputParameter " + " <" + subqueryParam[4] + "> .";
                  }                            

                query += this.getQueries().get("construct01.sparql").replaceAll("//taskexec//", " " + taskExec + " ")
                        .replaceAll("//param1//", " <" + subqueryParam[1] + "> ")
                        .replaceAll("//param2//", " <" + subqueryParam[2] + "> ")
                        .replaceAll("//theobject//", " " + theobject + " ");
                query += "}\n";
                query += "WHERE { \n";
                query += this.getQueries().get("body02b.sparql");
                query += this.getQueries().get("body02c.sparql").replaceAll("//operation//", "<" + subqueryParam[2] + ">")
                        .replaceAll("//taskrequest//", " <" + subqueryParam[3] + "> ");

                if(subqueryParam[5] != null )
                 {                     
                    query+=this.getQueries().get("body02e.sparql").replaceAll("//paramt//", " <"+ subqueryParam[5]+"> ");
                 }
                
                if (configs.size() > 0)
                  {
                    query += this.getQueries().get("body02d.sparql");
                    for (int i = 0; i < configs.size(); i++)
                      {
                        if(subqueryParam[0].equals(configs.get(i)[0]))
                          {
                        String thevar = " ?configs" + i + " ";
                        String thekey = " <" + configs.get(i)[1] + "> ";
                        String thevalue = " <" + configs.get(i)[2] + "> ";
                        query += "?setted " + thekey + thevar + ".\n";
                        query += thevar + "prof:hasType" + thevalue + ".\n";
                          }
                      }
                  }
                query += "}";
                 //System.out.println(query);            
                res = performQuery(ontology, query);
                //res.axioms().forEach(System.out::println);    
          if (res.axioms().count() == 0)
          {
            return null;
          }
        axioms = Stream.concat(res.axioms(), axioms);
        String[] conn=new String[]{""};
        res.logicalAxioms().filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)).forEach
        (
          element -> { 
              OWLObjectPropertyAssertionAxiom ax=(OWLObjectPropertyAssertionAxiom) element;
             if( ax.getProperty().asOWLObjectProperty().toStringID().equals(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasConnection"))
                 conn[0] = ax.getObject().toStringID();
          }
        );       
        axioms = Stream.concat(retrieveAssertions(conn[0], ontology.axioms()), axioms);
          }    
      } 
      catch (Exception ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            return null;
          }
        
        this.getMainManager().removeOntology(ontology);        
        if(depends.size()>0)
          {
            OWLAxiom[] depAxioms= new OWLAxiom [depends.size()];
            for(int i=0; i<depends.size();i++)
              {
                OWLAxiom ax= this.getMainManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
                this.getMainManager().getOWLDataFactory().getOWLObjectProperty(IRI.create(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#dependsOn")), 
                this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(IRI.create(depends.get(i)[0]+"_execution")),
                this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(IRI.create(depends.get(i)[1]+"_execution")));
                depAxioms[i]=ax;
              }
              axioms = Stream.concat(Stream.of(depAxioms), axioms);
          }
        OWLOntology out = null;
        try
          {
            out = getMainManager().createOntology(axioms);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(Profonto.class.getName()).log(Level.SEVERE, null, ex);
            return null;
          }
        return out;

      }
    
    
    public Stream<OWLAxiom> retrieveBeliefAssertions(String iriInd)
      {
         
         return retrieveAssertions(iriInd, Stream.concat(getDataRequestOntology().axioms(), 
                                                         getDataBeliefOntology().axioms()));
      }
    
    public Stream<OWLAxiom> retrieveDataBelief(InputStream input) throws OWLOntologyCreationException
    {
      OWLOntology ontology=this.getMainManager().loadOntologyFromOntologyDocument(input);
      Stream<OWLAxiom> axioms=Stream.empty();
      Iterator<OWLNamedIndividual> iterator=ontology.individualsInSignature().iterator();
      while(iterator.hasNext())
      { 
        OWLNamedIndividual individual=iterator.next();
              axioms=Stream.concat(this.getDataBeliefOntology().axioms().filter(x->x.isLogicalAxiom())
                                              .filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION) || x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))
                                              .filter(x->x.containsEntityInSignature( individual))
                    ,axioms);
      }
      this.getMainManager().removeOntology(ontology);
      return axioms;
    }
    
    private Stream<OWLAxiom> retrieveAssertions(String iriInd, Stream<OWLAxiom> ontology)
      {     
         
         OWLNamedIndividual individual=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(iriInd);         
         Stream<OWLAxiom> axioms=ontology.filter(x->x.isLogicalAxiom())
                                              .filter(x->x.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION) || x.isOfType(AxiomType.DATA_PROPERTY_ASSERTION))
                                              .filter(x->x.containsEntityInSignature(individual));
         return axioms;        
      }
    
    public void setExecutionStatus (String execution, String status) throws OWLOntologyStorageException
      {
        OWLNamedIndividual individual=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(execution);
        OWLNamedIndividual indstatus=this.getMainManager().getOWLDataFactory().getOWLNamedIndividual(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#"+status);
        OWLObjectProperty property=this.getMainManager().getOWLDataFactory().getOWLObjectProperty(this.getMainOntology().getOntologyID().getOntologyIRI().get().toString()+"#hasStatus");
        this.getDataRequestOntology().addAxiom(this.getMainManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
                property, individual, indstatus));
        this.getMainManager().saveOntology(this.getDataRequestOntology());
      
      }
}
