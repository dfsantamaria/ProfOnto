/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.main;

import dmi.unict.it.ontoas.core.OntoASCore;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class main
  {
    public static void main(String[] args)
      {
        File ontoFile=new File("ontologies/main/onto-as.owl");
        File dataFile=new File("ontologies/main/dataset.owl");
        OntoASCore ontocore=new OntoASCore();
        try
          {
            ontocore.setMainOntology(ontoFile);
            ontocore.setDatasetOntology(dataFile);
            ontocore.setConfiguration(new ArrayList<>(Arrays.asList("config","devices")));
            
            
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
          }
        System.out.println(ontocore.getMainOntology().getAxiomCount());
        
      }
  }
