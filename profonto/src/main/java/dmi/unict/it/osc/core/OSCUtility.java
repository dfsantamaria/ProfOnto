/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.osc.core;
import java.math.BigInteger;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multiaddr.MultiAddress;
import io.ipfs.multihash.Multihash;
import java.io.IOException;
import java.math.BigDecimal;
import javax.xml.bind.DatatypeConverter;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Numeric;
/**
 *
 * @author Daniele Francesco Santamaria
 */
public class OSCUtility
  {
    private  IPFS ipfs; 
    private  Web3j web3;
    private  Credentials credentials;   
    
    public OSCUtility(String ipfsaddress, String ethereumAddr, String privateKey) throws OSCUtilityConnectionExeception
    {
     try
       {
        ipfs = new IPFS(new MultiAddress(ipfsaddress));    
        System.out.println("Connected to IPFS at "+ipfsaddress);
        web3 = Web3j.build(new HttpService(ethereumAddr));
        System.out.println("Connected to Ethereum node: "+web3.web3ClientVersion().send().getWeb3ClientVersion());
        credentials=Credentials.create(privateKey);         
        System.out.println("Welcome address: "+credentials.getAddress());
       }    
     catch (IOException e )
       {
         this.handleStartUpException();
         throw new OSCUtilityConnectionExeception("Cannot correctly create OSCUtility. Couldn't connect to Ethereum node or invalid credentials.");
       } 
     catch(Exception e)
       {
         this.handleStartUpException();
         throw new OSCUtilityConnectionExeception("Cannot correctly create OSCUtility. Couldn't connect to IPFS or invalid Ethereum credentials.");
       }
    } 

    private void handleStartUpException()
      {
        this.ipfs=null;
        this. web3=null;
        this.credentials=null; 
      }

   public Web3j getWeb3jClient()
     {
      return this.web3;
     }
     
   public IPFS getIPFSClient()
     {
       return this.ipfs;
     }
   
   public Credentials getCredentials()
     {
       return this.credentials;
     }
   
    public Oasisosc uploadContract(String ontology, String query, String previous)
      {
        try      
          { 
            NamedStreamable.ByteArrayWrapper ontologyIPFS = new NamedStreamable.ByteArrayWrapper(ontology.getBytes());  
            NamedStreamable.ByteArrayWrapper queryIPFS = new NamedStreamable.ByteArrayWrapper(query.getBytes());  
            MerkleNode ontologyMN = ipfs.add(ontologyIPFS).get(0);
            MerkleNode queryMN = ipfs.add(queryIPFS).get(0);           
            String ontologyEX=DatatypeConverter.printHexBinary(ontologyMN.hash.toBytes());
            String queryEX=DatatypeConverter.printHexBinary(queryMN.hash.toBytes());
            byte[] ontologyDigest = Numeric.hexStringToByteArray(ontologyEX.substring(4, ontologyEX.length()));
            byte[] queryDigest = Numeric.hexStringToByteArray(queryEX.substring(4, ontologyEX.length()));    
            
            Oasisosc contract = Oasisosc.deploy(web3, credentials,  new OSCSimpleGasProvider(new BigDecimal(10), BigInteger.valueOf(3000000)),
                                                                  new BigInteger(ontologyEX.substring(0, 2)), new BigInteger(ontologyEX.substring(2, 4)), ontologyDigest,
                                                                  new BigInteger(queryEX.substring(0, 2)),  new BigInteger(queryEX.substring(2, 4)), queryDigest, 
                                                                  previous).send();            
           return contract;
          } 
          catch (IOException ex)
          {
              System.out.println(ex.toString());
          } 
         catch (Exception ex)
          {
            System.out.println(ex.toString());
          }
          return null;
      } 
    
    public String computeIPFSCIDFromMultiHash( Tuple3<BigInteger,BigInteger, byte[] > ipfsmultihash)
      {         
        String ipfscid=ipfsmultihash.component1()+""+ipfsmultihash.component2()+ DatatypeConverter.printHexBinary(ipfsmultihash.component3());
        return ipfscid;
      }
    
    
    public String getOntologyFromContract(String contractAddress)
      {
        Oasisosc osc = Oasisosc.load(contractAddress,this.getWeb3jClient(), this.getCredentials(), 
                       new OSCSimpleGasProvider(new BigDecimal(1), BigInteger.valueOf(3000000)));
        return this.getOntologyFromContract(osc);
      }
    
     public String getSPARQLQueryFromContract(String contractAddress)
      {
        Oasisosc osc = Oasisosc.load(contractAddress,this.getWeb3jClient(), this.getCredentials(), 
                       new OSCSimpleGasProvider(new BigDecimal(1), BigInteger.valueOf(3000000)));
        return this.getSPARQLQueryFromContract(osc);
      }
    
     public String getPreviousVersionFromContract(String contractAddress)
      {
        Oasisosc osc = Oasisosc.load(contractAddress,this.getWeb3jClient(), this.getCredentials(), 
                       new OSCSimpleGasProvider(new BigDecimal(1), BigInteger.valueOf(3000000)));
        return this.getPreviousVersionFromContract(osc);
      } 
     
     public String getOwnerFromContract(String contractAddress)
      {
        Oasisosc osc = Oasisosc.load(contractAddress,this.getWeb3jClient(), this.getCredentials(), 
                       new OSCSimpleGasProvider(new BigDecimal(1), BigInteger.valueOf(3000000)));
        return this.getOwnerFromContract(osc);
      } 
     
     public String getPreviousVersionFromContract(Oasisosc contract)
       {
         if(contract==null)
            return null;        
        String result=null;
        try
          {
            String value=contract.getPreviousVersion().send();
            return value;
          } 
        catch (Exception ex)
          {
            System.out.println(ex.toString());
            return null;
          }       
       }
     
      public String getOwnerFromContract(Oasisosc contract)
       {
         if(contract==null)
            return null;        
        String result=null;
        try
          {
            String value=contract.getOwner().send();
            return value;
          } 
        catch (Exception ex)
          {
            System.out.println(ex.toString());
            return null;
          }       
       }
      
     
    public String getOntologyFromContract(Oasisosc contract)
      {
        if(contract==null)
            return null;        
        String result=null;
        try
          {
            String ipfscid=this.computeIPFSCIDFromMultiHash(contract.getOntology().send());
            result=this.readFromIPFSCID(ipfscid);
            return result;
          } 
        catch (Exception ex)
          {
             System.out.println(ex.toString());
            return null;
          }
      }
    
       public String getSPARQLQueryFromContract(Oasisosc contract)
      {
        if(contract==null)
            return null;        
        String result=null;
        try
          {
            String ipfscid=this.computeIPFSCIDFromMultiHash(contract.getSPARQLQuery().send());
            result=this.readFromIPFSCID(ipfscid);
            return result;
          } 
        catch (Exception ex)
          {
             System.out.println(ex.toString());
            return null;
          }
      }
    
       
    
    private String readFromIPFSCID(String ipfscid)
      {  
       
        try
          {
            Multihash multihash = Multihash.fromHex(ipfscid);               
            byte[] content;
            content = ipfs.cat(multihash);
            return new String(content);
          } 
        catch (IOException ex)
          {
            System.out.println(ex.toString());
            return null;
          }        
      }

     public String getSPARQLQueryIPFSCID(String contractAddress)
      {
        Oasisosc osc = Oasisosc.load(contractAddress,this.getWeb3jClient(), this.getCredentials(), 
                       new OSCSimpleGasProvider(new BigDecimal(1), BigInteger.valueOf(3000000)));
        return this.getSPARQLQueryIPFSCID(osc);
      }
    
    public String getSPARQLQueryIPFSCID(Oasisosc contract)
         {
        if(contract==null)
            return null;         
        try
          {
            String ipfscid=this.computeIPFSCIDFromMultiHash(contract.getSPARQLQuery().send());           
            Multihash m=Multihash.fromHex(ipfscid);
            return m.toBase58();
          } 
        catch (Exception ex)
          {
             System.out.println(ex.toString());
            return null;
          }
      }
    
       public String getOntologyIPFSCID(String contractAddress)
      {
        Oasisosc osc = Oasisosc.load(contractAddress,this.getWeb3jClient(), this.getCredentials(), 
                       new OSCSimpleGasProvider(new BigDecimal(1), BigInteger.valueOf(3000000)));
        return this.getOntologyIPFSCID(osc);
      }
    
    public String getOntologyIPFSCID(Oasisosc contract)
         {
        if(contract==null)
            return null;         
        try
          {
            String ipfscid=this.computeIPFSCIDFromMultiHash(contract.getOntology().send());           
            Multihash m=Multihash.fromHex(ipfscid);
            return m.toBase58();
          } 
        catch (Exception ex)
          {
             System.out.println(ex.toString());
            return null;
          }
      }    
  }
