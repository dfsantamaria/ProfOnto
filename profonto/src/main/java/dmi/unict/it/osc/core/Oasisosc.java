package dmi.unict.it.osc.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.11.
 */
@SuppressWarnings("rawtypes")
public class Oasisosc extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b506040516102ed3803806102ed833981810160405260c081101561003357600080fd5b508051602080830151604080850151606080870151608088015160a090980151845180840186523381528551808501875260ff998a16815296891687890152868601949094528387019586528451928301855290871682529686168186019081528184019788528284018290529151600080546001600160a01b03929092166001600160a01b0319909216919091179055925180516001805496830151881661010090810261ff0019938a1660ff19998a16178416179091559190930151600255925160038054925187169094029516931692909217909116919091179055516004556101c8806101256000396000f3fe608060405234801561001057600080fd5b506004361061004c5760003560e01c80637e6e264f1461005157806383197ef01461007d578063893d20e814610087578063f618d2b0146100ab575b600080fd5b6100596100b3565b6040805160ff94851681529290931660208301528183015290519081900360600190f35b6100856100c9565b005b61008f610115565b604080516001600160a01b039092168252519081900360200190f35b610059610124565b60035460045460ff808316926101009004169192565b6000546001600160a01b031633146101125760405162461bcd60e51b815260040180806020018281038252603481526020018061013b6034913960400191505060405180910390fd5b33ff5b6000546001600160a01b031690565b60015460025460ff80831692610100900416919256fe4f6e6c7920636f6e7472616374206f776e657220697320616c6c6f77656420746f2063616c6c20746869732066756e6374696f6ea265627a7a7231582006b22780e039a42b60ebed20f9272b7172b4fe16838c9f3dca27deb8ec7559bd64736f6c637827302e352e31362d6e696768746c792e323032302e312e322b636f6d6d69742e39633332323663650057";

    public static final String FUNC_DESTROY = "destroy";

    public static final String FUNC_GETONTOLOGY = "getOntology";

    public static final String FUNC_GETOWNER = "getOwner";

    public static final String FUNC_GETSPARQLQUERY = "getSPARQLQuery";

    @Deprecated
    protected Oasisosc(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Oasisosc(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Oasisosc(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Oasisosc(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> destroy() {
        final Function function = new Function(
                FUNC_DESTROY, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, byte[]>> getOntology() {
        final Function function = new Function(FUNC_GETONTOLOGY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}));
        return new RemoteFunctionCall<Tuple3<BigInteger, BigInteger, byte[]>>(function,
                new Callable<Tuple3<BigInteger, BigInteger, byte[]>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, byte[]> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, byte[]>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> getOwner() {
        final Function function = new Function(FUNC_GETOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, byte[]>> getSPARQLQuery() {
        final Function function = new Function(FUNC_GETSPARQLQUERY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes32>() {}));
        return new RemoteFunctionCall<Tuple3<BigInteger, BigInteger, byte[]>>(function,
                new Callable<Tuple3<BigInteger, BigInteger, byte[]>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, byte[]> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, byte[]>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue());
                    }
                });
    }

    @Deprecated
    public static Oasisosc load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Oasisosc(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Oasisosc load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Oasisosc(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Oasisosc load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Oasisosc(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Oasisosc load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Oasisosc(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Oasisosc> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ)));
        return deployRemoteCall(Oasisosc.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<Oasisosc> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ)));
        return deployRemoteCall(Oasisosc.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Oasisosc> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ)));
        return deployRemoteCall(Oasisosc.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Oasisosc> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ)));
        return deployRemoteCall(Oasisosc.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }
}
