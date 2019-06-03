/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.main;

import dmi.unict.it.profonto.core.Profonto;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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
        System.out.println("Prof-Onto's core has been started. Welcome!");
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

    public static int addDevice(String description, String id)
    {
        ontocore.addDevice(getInputStream(description), id);
        return 0;
    }

    public static int addUser(String description, String id)
    {
        ontocore.addUser(getInputStream(description), id);
        return 0;
    }

    public static int addConfiguration(String description, String iddevice, String idconfig, String iduser)
    {
        ontocore.addDeviceConfiguration(getInputStream(description), iddevice, idconfig, iduser);
        return 0;
    }

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

    public static String acceptUserRequest(String request, String IRItask, String IRIUser, String IRIExec, String IRIgoalexec)
    {
       Stream<OWLAxiom> res= ontocore.acceptUserRequest(getInputStream(request), IRItask, IRIUser, IRIExec, IRIgoalexec);
       StringBuilder out=new StringBuilder();
      // res.forEach(x->System.out.println(x.));
       res.forEach(x->out.append(x.toString()).append("\n"));
       return out.toString();
    }
    
    public static String parseRequest(String request)
      {
        Stream<OWLAxiom> res= ontocore.parseRequest(getInputStream(request));
        StringBuilder out=new StringBuilder();   
        if(res!=null)
          { 
        res.forEach(x->out.append(x.toString()).append("\n"));
        return out.toString();
          }
        return "";
      }

    public static String retrieveAssertions(String individual)
      {
        Stream<OWLAxiom> res=ontocore.retrieveAssertions(individual);
        if(res!=null)
          { 
            StringBuilder out=new StringBuilder();      
            res.forEach(x->out.append(x.toString()).append("\n"));
            return out.toString();
          }
        return "";
      }
}
