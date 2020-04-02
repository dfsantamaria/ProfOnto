package dmi.unict.it.osc.test;

import dmi.unict.it.osc.core.OSCUtility;
import dmi.unict.it.osc.core.OSCUtilityConnectionExeception;
import dmi.unict.it.osc.core.Oasisosc;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Daniele Francesco Santamaria
 */
public class TestOSCUtility
  {
    public static void main (String[] args)
      {
          try
            {
              OSCUtility osc;  
              try
                {
                  /*
                    For example osc=new OSCUtility("/ip4/127.0.0.1/tcp/5001", "http://localhost:7545", "9caae095fdc0791d706c0a8ccd2f934de599a9ca2e7f55579d67b68391ab18eb");
                  */                  
                  osc=new OSCUtility(args[0], args[1], args[2]);                        
                }
              catch (OSCUtilityConnectionExeception e)
                {
                  System.out.println(e.toString());
                  return;
                }                         
              /*
                Path of the ontology file and path of the query file
              */
              String ontcontent=new String (Files.readAllBytes(Paths.get(args[3])));
              String querycontent=new String (Files.readAllBytes(Paths.get(args[4])));
              
              Oasisosc contract=osc.uploadContract(ontcontent, querycontent, args[5]);
              if(contract!=null)
                {
                  System.out.println("The contract address is "+contract.getContractAddress());
                  System.out.println("The ontology is "+osc.getOntologyFromContract(contract));
                  System.out.println("The ontology IPFS CID is "+osc.getOntologyIPFSCID(contract));
                  System.out.println("The SPARQL query is "+osc.getSPARQLQueryFromContract(contract));
                  System.out.println("The SPARQL query IPFS CID is "+osc.getSPARQLQueryIPFSCID(contract));
                  System.out.println("The previous version address is: "+osc.getPreviousVersionFromContract(contract));
                  System.out.println("The owner of the contract is: "+osc.getOwnerFromContract(contract));
                  
                  System.out.println("Testing contract address");
                  System.out.println("The ontology is "+osc.getOntologyFromContract(contract.getContractAddress()));
                  System.out.println("The ontology IPFS CID is "+osc.getOntologyIPFSCID(contract.getContractAddress()));
                  System.out.println("The SPARQL query is "+osc.getSPARQLQueryFromContract(contract.getContractAddress()));
                  System.out.println("The SPARQL query IPFS CID is "+osc.getSPARQLQueryIPFSCID(contract.getContractAddress()));
                  
                  System.out.println("The previous version address is: "+osc.getPreviousVersionFromContract(contract.getContractAddress()));
                  System.out.println("The owner of the contract is: "+osc.getOwnerFromContract(contract.getContractAddress()));
                }
              
            } 
            catch (IOException ex)
            {
              Logger.getLogger(TestOSCUtility.class.getName()).log(Level.SEVERE, null, ex);
            }       
         
      }
  }
