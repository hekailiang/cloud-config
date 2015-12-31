package org.squirrelframework.cloud.resource.cache;

import com.google.code.ssm.api.format.SerializationType;
import org.squirrelframework.cloud.resource.CloudResourceConfig;

import javax.validation.constraints.NotNull;

/**
 * Created by kailianghe on 15/12/25.
 */
public class MemcachedResourceConfig extends CloudResourceConfig {

    @NotNull
    private String address;

    private String weights;

    private SerializationType serializationType = SerializationType.PROVIDER;

    private boolean consistentHashing = true;

    private boolean useBinaryProtocol = false;

    // 5 seconds, unit millisecond
    private Integer operationTimeout = 5000;

    private boolean useNameAsKeyPrefix = true;

    private String keyPrefixSeparator = "#";

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWeights() {
        return weights;
    }

    public void setWeights(String weights) {
        this.weights = weights;
    }

    public SerializationType getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    public boolean isConsistentHashing() {
        return consistentHashing;
    }

    public void setConsistentHashing(boolean consistentHashing) {
        this.consistentHashing = consistentHashing;
    }

    public boolean isUseBinaryProtocol() {
        return useBinaryProtocol;
    }

    public void setUseBinaryProtocol(boolean useBinaryProtocol) {
        this.useBinaryProtocol = useBinaryProtocol;
    }

    public Integer getOperationTimeout() {
        return operationTimeout;
    }

    public void setOperationTimeout(Integer operationTimeout) {
        this.operationTimeout = operationTimeout;
    }

    public boolean isUseNameAsKeyPrefix() {
        return useNameAsKeyPrefix;
    }

    public void setUseNameAsKeyPrefix(boolean useNameAsKeyPrefix) {
        this.useNameAsKeyPrefix = useNameAsKeyPrefix;
    }

    public String getKeyPrefixSeparator() {
        return keyPrefixSeparator;
    }

    public void setKeyPrefixSeparator(String keyPrefixSeparator) {
        this.keyPrefixSeparator = keyPrefixSeparator;
    }
}
