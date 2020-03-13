/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmi.unict.it.osc.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;

/**
 *
 * @author Daniele Francesco Santamaria
 */
 class OSCSimpleGasProvider implements ContractGasProvider
      {    
        private BigDecimal gasprice;
        private BigInteger gasLimit;
        
        public OSCSimpleGasProvider(BigDecimal _gasprice, BigInteger _gasLimit)
          {
             gasprice=_gasprice;
             gasLimit=_gasLimit;
          }

        @Override
        public BigInteger getGasPrice(String string)
          {            
           return Convert.toWei(gasprice, Convert.Unit.GWEI).toBigInteger();             
          }       

        @Override
        public BigInteger getGasLimit(String string)
          {
            return gasLimit;
          }

        @Override
        public BigInteger getGasPrice()
          {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

        @Override
        public BigInteger getGasLimit()
          {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

      }
