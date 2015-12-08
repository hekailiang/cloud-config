package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kailianghe on 12/4/15.
 */
public class TransactionTypeRoutingKeyResolver implements RoutingKeyResolver {

    private String[] readKeyValues = new String[] {"read"};

    private String writeKeyValue = "write";

    private AtomicLong selector = new AtomicLong(0L);

    @Override
    public Optional<String> get() {
        return Optional.of(TransactionSynchronizationManager.
                isCurrentTransactionReadOnly() ? readKeyValues[selectReadKey()] : writeKeyValue);
    }

    private int selectReadKey() {
        return (int) (selector.getAndIncrement() % readKeyValues.length);
    }

    public void setReadKeyValues(String[] readKeyValues) {
        if(readKeyValues==null || readKeyValues.length==0) {
            throw new IllegalArgumentException("Invalid routing key values");
        }
        this.readKeyValues = readKeyValues;
    }

    public void setWriteKeyValue(String writeKeyValue) {
        this.writeKeyValue = writeKeyValue;
    }
}
