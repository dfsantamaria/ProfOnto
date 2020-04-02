package dmi.unict.it.osc.core;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
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
    public static final String BINARY = "608060405234801561001057600080fd5b50604051610473380380610473833981810160405260e081101561003357600080fd5b81019080805190602001909291908051906020019092919080519060200190929190805190602001909291908051906020019092919080519060200190929190805190602001909291905050506040518060800160405280336001600160a01b0316815260200160405180606001604052808a60ff1681526020018960ff16815260200188815250815260200160405180606001604052808760ff1681526020018660ff168152602001858152508152602001826001600160a01b03168152506000808201518160000160006101000a8154816001600160a01b0302191690836001600160a01b0316021790555060208201518160010160008201518160000160006101000a81548160ff021916908360ff16021790555060208201518160000160016101000a81548160ff021916908360ff16021790555060408201518160010155505060408201518160030160008201518160000160006101000a81548160ff021916908360ff16021790555060208201518160000160016101000a81548160ff021916908360ff16021790555060408201518160010155505060608201518160050160006101000a8154816001600160a01b0302191690836001600160a01b03160217905550905050336001600160a01b03167f0cd0502e76ab9a90ab900076d81b15a79a97f208a205f467468083e63a0450088684604051808381526020018281526020019250505060405180910390a2505050505050506102158061025e6000396000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c8063754dc93a1461005c5780637e6e264f1461008057806383197ef0146100ac578063893d20e8146100b6578063f618d2b0146100be575b600080fd5b6100646100c6565b604080516001600160a01b039092168252519081900360200190f35b6100886100d5565b6040805160ff94851681529290931660208301528183015290519081900360600190f35b6100b46100eb565b005b610064610162565b610088610171565b6005546001600160a01b031690565b60035460045460ff808316926101009004169192565b6000546001600160a01b031633146101345760405162461bcd60e51b81526004018080602001828103825260348152602001806101886034913960400191505060405180910390fd5b60405133907f5158809353c1d603d8c77564e8556eea254ce047d03c767920eeb497481c1e4f90600090a233ff5b6000546001600160a01b031690565b60015460025460ff80831692610100900416919256fe4f6e6c7920636f6e7472616374206f776e657220697320616c6c6f77656420746f2063616c6c20746869732066756e6374696f6ea265627a7a723158207626a838cfd62718b19c934477231ed2ee737defb08156ae6b731acf0bd33fdf64736f6c637827302e352e31362d6e696768746c792e323032302e312e322b636f6d6d69742e39633332323663650057";

    public static final String FUNC_DESTROY = "destroy";

    public static final String FUNC_GETONTOLOGY = "getOntology";

    public static final String FUNC_GETOWNER = "getOwner";

    public static final String FUNC_GETPREVIOUSVERSION = "getPreviousVersion";

    public static final String FUNC_GETSPARQLQUERY = "getSPARQLQuery";

    public static final Event DESTROYEVENT_EVENT = new Event("DestroyEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event STORAGEEVENT_EVENT = new Event("StorageEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}));
    ;

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

    public List<DestroyEventEventResponse> getDestroyEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DESTROYEVENT_EVENT, transactionReceipt);
        ArrayList<DestroyEventEventResponse> responses = new ArrayList<DestroyEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DestroyEventEventResponse typedResponse = new DestroyEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DestroyEventEventResponse> destroyEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, DestroyEventEventResponse>() {
            @Override
            public DestroyEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DESTROYEVENT_EVENT, log);
                DestroyEventEventResponse typedResponse = new DestroyEventEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DestroyEventEventResponse> destroyEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DESTROYEVENT_EVENT));
        return destroyEventEventFlowable(filter);
    }

    public List<StorageEventEventResponse> getStorageEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(STORAGEEVENT_EVENT, transactionReceipt);
        ArrayList<StorageEventEventResponse> responses = new ArrayList<StorageEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            StorageEventEventResponse typedResponse = new StorageEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.digestO = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.digestQ = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<StorageEventEventResponse> storageEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, StorageEventEventResponse>() {
            @Override
            public StorageEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(STORAGEEVENT_EVENT, log);
                StorageEventEventResponse typedResponse = new StorageEventEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.digestO = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.digestQ = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<StorageEventEventResponse> storageEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(STORAGEEVENT_EVENT));
        return storageEventEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> destroy() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_DESTROY, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, byte[]>> getOntology() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETONTOLOGY, 
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
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> getPreviousVersion() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETPREVIOUSVERSION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, byte[]>> getSPARQLQuery() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETSPARQLQUERY, 
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

    public static RemoteCall<Oasisosc> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ, String _prev) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ), 
                new org.web3j.abi.datatypes.Address(160, _prev)));
        return deployRemoteCall(Oasisosc.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<Oasisosc> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ, String _prev) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ), 
                new org.web3j.abi.datatypes.Address(160, _prev)));
        return deployRemoteCall(Oasisosc.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Oasisosc> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ, String _prev) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ), 
                new org.web3j.abi.datatypes.Address(160, _prev)));
        return deployRemoteCall(Oasisosc.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Oasisosc> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger hashO, BigInteger sizeO, byte[] digestO, BigInteger hashQ, BigInteger sizeQ, byte[] digestQ, String _prev) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint8(hashO), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeO), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestO), 
                new org.web3j.abi.datatypes.generated.Uint8(hashQ), 
                new org.web3j.abi.datatypes.generated.Uint8(sizeQ), 
                new org.web3j.abi.datatypes.generated.Bytes32(digestQ), 
                new org.web3j.abi.datatypes.Address(160, _prev)));
        return deployRemoteCall(Oasisosc.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class DestroyEventEventResponse extends BaseEventResponse {
        public String from;
    }

    public static class StorageEventEventResponse extends BaseEventResponse {
        public String from;

        public byte[] digestO;

        public byte[] digestQ;
    }
}