package dmi.unict.it.osc.test;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import dmi.unict.it.osc.core.OSCUtility;
import dmi.unict.it.osc.core.OSCUtilityConnectionExeception;
import dmi.unict.it.osc.core.Oasisosc;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class TestOSCUtility
  {
    public static void main (String[] args)
      {
         OSCUtility osc;
          try
            {
               osc=new OSCUtility("/ip4/127.0.0.1/tcp/5001", "http://localhost:7545", "d315a8d6a184a816419d598e57a5a0ee2df66a977fee86bc219be4c02f40991e");  
            } 
            catch (OSCUtilityConnectionExeception e)
            {
              System.out.println(e.toString());
              return;
            }       
        
         Oasisosc contract=osc.uploadContract("this is an ontology", "this is a query", "0x0000000000000000000000000000000000000000");  
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
  }
