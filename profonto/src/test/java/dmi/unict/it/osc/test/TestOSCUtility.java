package dmi.unict.it.osc.test;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import dmi.unict.it.osc.core.OSCUtility;
import dmi.unict.it.osc.core.Oasisosc;

/**
 *
 * @author Daniele Francesco Santamaria
 */
public class TestOSCUtility
  {
    public static void main (String[] args)
      {
         OSCUtility osc=new OSCUtility("/ip4/127.0.0.1/tcp/5001", "http://localhost:7545", "d315a8d6a184a816419d598e57a5a0ee2df66a977fee86bc219be4c02f40991e");
         Oasisosc contract=osc.uploadContract("this is an ontology", "this is a query");         
         System.out.println("The contract address is "+contract.getContractAddress());
         System.out.println("The ontology is "+osc.getOntologyFromContract(contract));
         System.out.println("The SPARQL query is "+osc.getOntologyFromContract(contract));
         System.out.println("Testing contract address");
         System.out.println("The ontology is "+osc.getOntologyFromContract(contract.getContractAddress()));
         System.out.println("The SPARQL query is "+osc.getOntologyFromContract(contract.getContractAddress()));
         
      }
  }
