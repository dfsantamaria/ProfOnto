/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.ontoas.test;

import dmi.unict.it.ontoas.core.OntoASCore;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class mainTest
  {
    
    public static InputStream readData(String file)
    {
        InputStream inputstream=null;
        String input="";
        try {
            FileReader fileReader =  new FileReader(file);
            BufferedReader bufferedReader =  new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
                input+=line;
            }            
            bufferedReader.close();
            inputstream=new ByteArrayInputStream(Charset.forName("UTF-16").encode(input).array());
        }
        catch (IOException ex) {
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
            
        } 
        return inputstream;
    }
    
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
            Logger.getLogger(mainTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        System.out.println(ontocore.getMainOntology().getAxiomCount());
        
      }
  }
