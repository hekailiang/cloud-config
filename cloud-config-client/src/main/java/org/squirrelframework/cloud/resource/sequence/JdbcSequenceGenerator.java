package org.squirrelframework.cloud.resource.sequence;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by kailianghe on 15/12/15.
 */
public class JdbcSequenceGenerator implements SequenceGenerator {

    private SequenceDao sequenceDao;

    private SequenceFormatter sequenceFormatter;

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
                    if(sequenceRangeHolder.get()==null || sequenceRangeHolder.get().isExhausted()) {
                        sequenceRangeHolder.set(sequenceDao.applyNextRange(seqName));
                    }
                    SequenceRange sequenceRange = sequenceRangeHolder.get();
                    return sequenceFormatter.format(sequenceRange);
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

    @Autowired
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    @Autowired
    public void setSequenceFormatter(SequenceFormatter sequenceFormatter) {
        this.sequenceFormatter = sequenceFormatter;
    }
}
