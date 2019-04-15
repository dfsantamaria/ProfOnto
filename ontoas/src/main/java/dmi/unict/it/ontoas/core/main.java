/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.core;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * @author danie
 */
public class main
  {
    public static void main(String[] args)
      {
        File ontoFile=new File("ontologies/main/onto-as.owl");
        OntoASCore ontocore=new OntoASCore();
        try
          {
            ontocore.setMainOntology(ontoFile);
          } 
        catch (OWLOntologyCreationException ex)
          {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
          }
        System.out.println(ontocore.mainOntology.getAxiomCount());
      }
  }
