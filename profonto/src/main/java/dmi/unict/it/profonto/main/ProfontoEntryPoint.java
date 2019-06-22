/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.main;

import dmi.unict.it.profonto.core.Profonto;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import py4j.GatewayServer;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class ProfontoEntryPoint
{

    public static void main(String[] args)
    {
        GatewayServer gatewayServer = new GatewayServer(new ProfontoEntryPoint());
        gatewayServer.start();
        System.out.println("Prof-Onto's core has been started. Wait for Prof-Onto to start.");
    }

    static Profonto ontocore;

    public ProfontoEntryPoint()
    {
        File ontoFile = new File("ontologies/main/oasis.owl");
        File aboxFile=new File("ontologies/main/oasis-abox.owl");
        // File dataFile=new File("ontologies/main/dataset.owl");
        ontocore = new Profonto();
        try
        {
            ontocore.setOntologiesDeviceConfigurationsPath(Paths.get("ontologies" + File.separator + "devConfigs"));
            ontocore.setOntologiesDevicesPath(Paths.get("ontologies" + File.separator + "devices"));
            ontocore.setMainOntologiesPath(Paths.get("ontologies" + File.separator + "main"));
            ontocore.setOntologiesUsersPath(Paths.get("ontologies" + File.separator + "users"));
            ontocore.setQueryPath(Paths.get("ontologies"+File.separator+"queries"));
            
            ontocore.setMainOntology(ontoFile);
            ontocore.setMainAbox(aboxFile);
              
            ontocore.setDataBehaviorOntology("http://www.dmi.unict.it/prof-onto-behavior.owl", "behavior.owl");
            ontocore.setDataChronoOntology("http://www.dmi.unict.it/prof-onto-chrono.owl","chronology.owl");
            ontocore.setDataBeliefOntology("http://www.dmi.unict.it/prof-onto-belief.owl", "belief.owl");
            ontocore.loadDevicesFromPath(true); //use this if the devices folder is not empty 
            // ontocore.startReasoner();
        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static InputStream getInputStream(String description)
    {
        InputStream inputstream = null;
        inputstream = new ByteArrayInputStream(description.getBytes());
        try
        {
            inputstream.close();
        } catch (IOException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        return inputstream;
    }

    public static String addDevice(String description)
    {
        return ontocore.addDevice(getInputStream(description));
        
    }

    public static String addUser(String description)
    {
        return ontocore.addUser(getInputStream(description));
        
    }

    
   public static String addConfiguration(String description)
    {
       String value="";
        try    
          {
            value=ontocore.addDeviceConfiguration(getInputStream(description));
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            
          }
        return value;
    }
    
//    public static int addConfiguration(String description, String iddevice, String idconfig, String iduser)
//    {
//        ontocore.addDeviceConfiguration(getInputStream(description), iddevice, idconfig, iduser);
//        return 0;
//    }

    public static int removeUser(String id)
    {
        try
        {
            ontocore.removePermanentUser(id);
        } catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return 0;
    }

    public static int removeDevice(String id)
    {
        try
        {
            ontocore.removePermanentDevice(id);
        } catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return 0;
    }

    public static int addDataBelief(String input)
    {
        try
        {
            return ontocore.addDataToDataBelief(getInputStream(input));
        } catch (OWLOntologyCreationException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    public static int removeDataBelief(String input)
    {
       try
        {
            return ontocore.removeDataFromDataBelief(getInputStream(input));
        } 
       catch (OWLOntologyCreationException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    public String retrieveDataBelief(String input)
    {
        try
        {
            Stream<OWLAxiom> res=ontocore.retrieveDataBelief(getInputStream(input));
            if(res!=null)
            {
                StringBuilder out=new StringBuilder();
                res.forEach(x->out.append(x.toString()).append("\n"));
                return out.toString();
            }
            return "";
        }
        catch (OWLOntologyCreationException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    public static int syncReasonerDataBehavior()
    {
        try
        {
            ontocore.syncReasonerDataBehavior();
        }
        catch (OWLOntologyStorageException | OWLOntologyCreationException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return 0;
    }

//    public static String acceptUserRequest(String request, String IRItask, String IRIUser, String IRIExec, String IRIgoalexec)
//    {
//       Stream<OWLAxiom> res= ontocore.acceptUserRequest(getInputStream(request), IRItask, IRIUser, IRIExec, IRIgoalexec);
//       StringBuilder out=new StringBuilder();
//      // res.forEach(x->System.out.println(x.));
//       res.forEach(x->out.append(x.toString()).append("\n"));
//       return out.toString();
//    }
    
    public static String parseRequest(String request)
      {
        OWLOntology res= ontocore.parseRequest(getInputStream(request));
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        if(res!=null)
          { 
            try
              {
                res.saveOntology(new RDFXMLDocumentFormat(), out);
                if(out!=null)
                   out.close(); 
                return out.toString();
              } 
            catch (OWLOntologyStorageException | IOException ex)
              {
                Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
                return "";
              }                      
      }
        return "";
      }

    public static int removeConfiguration(String configuration)
      {
        try
          {
            ontocore.removePermanentConfigurationFromDevice(configuration);
          } 
        catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException ex)
          {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
          }
        return 0;
      }
    
    public static String retrieveAssertions(String individual)
      {
        Stream<OWLAxiom> res=ontocore.retrieveBeliefAssertions(individual);
        if(res!=null)
          { 
            StringBuilder out=new StringBuilder();      
            res.forEach(x->out.append(x.toString()).append("\n"));
            return out.toString();
          }
        return "";
      }
    
    public int emptyChronology()
    {
        try
        {
            ontocore.emptyChronology();
            return 0;
        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException ex)
        {
            Logger.getLogger(ProfontoEntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
}
