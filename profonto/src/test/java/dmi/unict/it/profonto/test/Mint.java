/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.profonto.test;

import dmi.unict.it.osc.core.OSCUtility;
import dmi.unict.it.osc.core.OSCUtilityConnectionException;
import io.ipfs.api.IPFS;
import io.ipfs.multiaddr.MultiAddress;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author danie
 */
public class Mint
{
    public static void main(String[]args) throws OSCUtilityConnectionException, IOException, Exception
    {     
    OSCUtility util=new OSCUtility("/ip4/127.0.0.1/tcp/5001", "", "");
    util.load("0x36194ab80f7649572cab9ec524950df32f638b08");
    String ontcontent=new String (Files.readAllBytes(Paths.get("")));
    String querycontent=new String (Files.readAllBytes(Paths.get("")));
    //util.mint(ontcontent, querycontent, BigInteger.ONE);
    String[] s=util.getTokenInfo(BigInteger.valueOf(4));
    System.out.println(s[0]);
    System.out.println(s[1]);
    System.out.println(s[2]);
    }
}

