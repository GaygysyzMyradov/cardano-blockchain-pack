package org.joget.cardano.model;

import com.bloxbean.cardano.client.api.helper.FeeCalculationService;
import com.bloxbean.cardano.client.api.helper.TransactionHelperService;
import com.bloxbean.cardano.client.api.helper.UtxoTransactionBuilder;
import com.bloxbean.cardano.client.backend.api.AccountService;
import com.bloxbean.cardano.client.backend.api.AddressService;
import com.bloxbean.cardano.client.backend.api.AssetService;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.BlockService;
import com.bloxbean.cardano.client.backend.api.EpochService;
import com.bloxbean.cardano.client.backend.api.MetadataService;
import com.bloxbean.cardano.client.backend.api.NetworkInfoService;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.backend.api.UtxoService;
import java.util.Map;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.cardano.util.BackendUtil;
import org.joget.cardano.util.PluginUtil;
import org.joget.commons.util.LogUtil;

public abstract class CardanoFormBinder extends FormBinder implements FormLoadBinder {
    
    //Cardano backend services
    protected AssetService assetService;
    protected BlockService blockService;
    protected NetworkInfoService networkInfoService;
    protected TransactionService transactionService;
    protected UtxoService utxoService;
    protected AddressService addressService;
    protected AccountService accountService;
    protected EpochService epochService;
    protected MetadataService metadataService;
    protected TransactionHelperService transactionHelperService;
    protected UtxoTransactionBuilder utxoTransactionBuilder;
    protected FeeCalculationService feeCalculationService;
    
    //Plugin properties
    protected Map props;
    
    /**
     * Used to validate necessary input values prior to executing API calls. This method is wrapped by load().
     * @return A boolean value to continue or skip plugin execution. Default value is true.
     */
    public boolean isInputDataValid() {
        return true;
    }
    
    /**
     * Loads data based on a primary key. This method is wrapped by load().
     * 
     * @param element The element to load the data into.
     * @param primaryKey
     * @param formData
     * 
     * @return A Collection of Map objects. Each Map object contains property=value pairs to represent a data row.
     */
    public abstract FormRowSet loadData(Element element, String primaryKey, FormData formData);
    
    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        this.props = getProperties();
        initUtils();
        
        if (!isInputDataValid()) {
            LogUtil.debug(getClassName(), "Invalid input(s) detected. Aborting plugin execution.");
            return null;
        }
        
        FormRowSet rows = new FormRowSet();
        
        try {
            final BackendService backendService = BackendUtil.getBackendService(props);
            
            if (backendService != null) {
                initBackendServices(backendService);
                rows = loadData(element, primaryKey, formData);
            }
        } catch (Exception ex) {
            LogUtil.error(getClassName(), ex, "Error executing form binder plugin...");
        }
        
        return rows;
    }
    
    private void initUtils() { }
    
    private void initBackendServices(BackendService backendService) {
        assetService = backendService.getAssetService();
        blockService = backendService.getBlockService();
        networkInfoService = backendService.getNetworkInfoService();
        transactionService = backendService.getTransactionService();
        utxoService = backendService.getUtxoService();
        addressService = backendService.getAddressService();
        accountService = backendService.getAccountService();
        epochService = backendService.getEpochService();
        metadataService = backendService.getMetadataService();
        transactionHelperService = backendService.getTransactionHelperService();
        utxoTransactionBuilder = backendService.getUtxoTransactionBuilder();
        feeCalculationService = backendService.getFeeCalculationService();
    }
    
    @Override
    public String getVersion() {
        return PluginUtil.getProjectVersion(this.getClass());
    }
    
    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
}
