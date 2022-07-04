package org.joget.cardano.service;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.helper.model.TransactionResult;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.backend.model.TransactionContent;
import com.bloxbean.cardano.client.crypto.SecretKey;
import com.bloxbean.cardano.client.util.HexUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.joget.apps.form.service.FormUtil;

public class TransactionUtil {

    public static final String NATIVE_MAINNET_TX_EXPLORER_URL = "https://explorer.cardano.org/en/transaction";
    public static final String CARDANOSCAN_MAINNET_TX_EXPLORER_URL = "https://cardanoscan.io/transaction/";
    public static final String CEXPLORER_MAINNET_TX_EXPLORER_URL = "https://cexplorer.io/tx/";
//    public static final String BLOCKCHAIR_MAINNET_TX_EXPLORER_URL = "https://blockchair.com/cardano/transaction/";
    public static final String ADATOOLS_MAINNET_TX_EXPLORER_URL = "https://adatools.io/transactions/";
    
    public static final String NATIVE_TESTNET_TX_EXPLORER_URL = "https://explorer.cardano-testnet.iohkdev.io/en/transaction";
    public static final String CARDANOSCAN_TESTNET_TX_EXPLORER_URL = "https://testnet.cardanoscan.io/transaction/";
    public static final String CEXPLORER_TESTNET_TX_EXPLORER_URL = "https://testnet.cexplorer.io/tx/";
//    public static final String BLOCKCHAIR_TESTNET_TX_EXPLORER_URL = "No testnet available...";
    public static final String ADATOOLS_TESTNET_TX_EXPLORER_URL = "https://testnet.adatools.io/transactions/";
    
    public static final long DEFAULT_WAIT_INTERVAL_MS = 2000;
    
    public static final int DEFAULT_SLOT_TTL = 1000;
    
    public static long getTtl(BlockService blockService) throws ApiException {
        return blockService.getLatestBlock().getValue().getSlot() + DEFAULT_SLOT_TTL;
    }
    
    public static long getTtl(BlockService blockService, int numberOfSlots) throws ApiException {
        return blockService.getLatestBlock().getValue().getSlot() + numberOfSlots;
    }
    
    public static String getAssetId(String policyId, String assetName) {
        return policyId + HexUtil.encodeHexString(assetName.getBytes(StandardCharsets.UTF_8));
    }
    
    //Perhaps allow fully-customizable policy name?
    public static String getFormattedPolicyName(String policyId) {        
        return "mintPolicy-joget-" + policyId.substring(0, 8);
    }
    
    public static boolean validateTransactionId(TransactionService transactionService, String transactionId) throws ApiException {
        //If within a Form Builder, don't make useless API calls
        if (transactionId == null || transactionId.isBlank() || FormUtil.isFormBuilderActive()) {
            return false;
        }
        
        return transactionService.getTransaction(transactionId).code() == 200;
    }
    
    public static String getTransactionExplorerUrl(boolean isTest, String transactionId) {
        return getTransactionExplorerUrl(isTest, transactionId, "native");
    }
    
    public static String getTransactionExplorerUrl(boolean isTest, String transactionId, String explorerType) {
        //No need to return immediately, in case user wants to show link as is
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "";
        }

        String transactionUrl;
        
        switch (explorerType) {
            case "cardanoscan":
                transactionUrl = isTest ? CARDANOSCAN_TESTNET_TX_EXPLORER_URL : CARDANOSCAN_MAINNET_TX_EXPLORER_URL;
                break;
            case "cexplorer":
                transactionUrl = isTest ? CEXPLORER_TESTNET_TX_EXPLORER_URL : CEXPLORER_MAINNET_TX_EXPLORER_URL;
                break;
            case "adatools":
                transactionUrl = isTest ? ADATOOLS_TESTNET_TX_EXPLORER_URL : ADATOOLS_MAINNET_TX_EXPLORER_URL;
                break;
            default:
                transactionUrl = isTest ? NATIVE_TESTNET_TX_EXPLORER_URL : NATIVE_MAINNET_TX_EXPLORER_URL;
                transactionUrl += "?id=";
                break;
        }
        
        transactionUrl += transactionId;
        
        return transactionUrl;
    }
    
    // Combine all secret key(s) into string delimited by semicolon (e.g.: skey1;skey2;skey3)
    public static String getSecretKeysAsCborHexStringList(List<SecretKey> skeys) {
        if (skeys == null || skeys.isEmpty()) {
            return null;
        }
        
        return skeys.stream().map(skey -> skey.getCborHex())
				.collect(Collectors.joining(";"));
    }
    
    public static List<SecretKey> getSecretKeysStringAsList(String skeysStringList) {
        if (skeysStringList == null || skeysStringList.trim().isEmpty()) {
            return null;
        }
        
        List<SecretKey> skeys = new ArrayList<>();
        
        for (String skey : skeysStringList.split(";")) {
            skeys.add(new SecretKey(skey.trim()));
        }
        
        return skeys;
    }
    
    public static Result<TransactionContent> waitForTransactionHash(TransactionService transactionService, Result<String> transactionResult) 
            throws ApiException, InterruptedException {
        if (!transactionResult.isSuccessful()) {
            return null;
        }
        
        //Wait for transaction to be mined
        int count = 0;
        while (count < 60) {
            Result<TransactionContent> txnResult = transactionService.getTransaction(transactionResult.getValue());
            if (txnResult.isSuccessful()) {
                //LogUtil.info(getClass().getName(), JsonUtil.getPrettyJson(txnResult.getValue()));
                return txnResult;
            } else {
                //LogUtil.info(getClass().getName(), "Waiting for transaction to be mined....");
            }

            count++;
            Thread.sleep(DEFAULT_WAIT_INTERVAL_MS);
        }
        
        return null;
    }
    
    public static Result<TransactionContent> waitForTransaction(TransactionService transactionService, Result<TransactionResult> transactionResult) 
            throws ApiException, InterruptedException {
        if (!transactionResult.isSuccessful()) {
            return null;
        }
        
        //Wait for transaction to be mined
        int count = 0;
        while (count < 60) {
            Result<TransactionContent> txnResult = transactionService.getTransaction(transactionResult.getValue().getTransactionId());
            if (txnResult.isSuccessful()) {
                return txnResult;
            }

            count++;
            Thread.sleep(DEFAULT_WAIT_INTERVAL_MS);
        }
        
        return null;
    }
}
