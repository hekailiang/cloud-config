package org.squirrelframework.cloud.resource.sequence;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kailianghe on 15/12/15.
 */
public class JdbcSequenceGenerator implements SequenceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SequenceGenerator.class);

    private SequenceDao sequenceDao;

    private SequenceFormatter sequenceFormatter;

    private String dateFormat = "yyyyMMdd";

    private final Lock locker = new ReentrantLock();

    private CacheLoader<String, Iterator<String>> loader = new CacheLoader<String, Iterator<String>>() {
        @Override
        public Iterator<String> load(final String seqName) throws Exception {
            final AtomicReference<SequenceRange> sequenceRangeHolder = new AtomicReference<>();
            return new Iterator<String>() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public String next() {
                    if(sequenceRangeHolder.get()==null) {
                        locker.lock();
                        try {
                            if(sequenceRangeHolder.get()==null) {
                                sequenceRangeHolder.set(sequenceDao.applyNextRange(seqName));
                            }
                        } finally {
                            locker.unlock();
                        }
                    }
                    long value = sequenceRangeHolder.get().getAndIncrement();
                    if(value<0) {
                        locker.lock();
                        try {
                            do {
                                if(sequenceRangeHolder.get().isDrained()) {
                                    sequenceRangeHolder.set(sequenceDao.applyNextRange(seqName));
                                }
                                value = sequenceRangeHolder.get().getAndIncrement();
                            } while(value<0);
                        } finally {
                            locker.unlock();
                        }
                    }
                    Map<String, Object> parameters = Maps.newHashMap();
                    parameters.put(CloudConfigCommon.DB_DATE_KEY, sequenceRangeHolder.get().getDate());
                    parameters.put(CloudConfigCommon.DB_DATE_STR_KEY, sequenceRangeHolder.get().getFormattedDate(dateFormat));
                    parameters.put(CloudConfigCommon.DB_NAME_KEY, sequenceRangeHolder.get().getDbName());
                    parameters.put(CloudConfigCommon.SEQUENCE_VALUE_KEY, value);
                    String sequence = sequenceFormatter.format(parameters);
                    logger.debug("new '{}' sequence: {}", seqName, sequence);
                    return sequence;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("cannot remove element");
                }
            };
        }
    };

    private LoadingCache<String, Iterator<String>> cachedSequenceRange = CacheBuilder.newBuilder().build(loader);

    @Override
    public String next(String seqName) throws Exception {
        return cachedSequenceRange.get(seqName).next();
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setSequenceFormatter(SequenceFormatter sequenceFormatter) {
        this.sequenceFormatter = sequenceFormatter;
    }
}
