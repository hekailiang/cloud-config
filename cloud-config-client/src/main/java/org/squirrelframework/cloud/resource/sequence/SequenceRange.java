package org.squirrelframework.cloud.resource.sequence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by kailianghe on 15/12/14.
 */

/**
 * Range [min, max)
 */
public class SequenceRange {
    private final long min;
    private final long max;
    private final AtomicLong value;
    private final Date date;
    private volatile boolean drained = false;

    public SequenceRange(long min, long max, Date date) {
        this.min = min;
        this.max = max;
        this.date = date;
        this.value = new AtomicLong(min);
    }

    public long getAndIncrement() {
        long currentSeq = this.value.getAndIncrement();
        if(currentSeq >= this.max) {
            drained = true;
            return -1L;
        } else {
            return currentSeq;
        }
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public AtomicLong getValue() {
        return value;
    }

    public boolean isDrained() {
        return drained || value.get() >= this.max;
    }

    public Date getDate() {
        return date;
    }

    public String getFormattedDate(String format) {
        return new SimpleDateFormat(format).format(date);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceRange)) return false;

        SequenceRange that = (SequenceRange) o;

        if (min != that.min) return false;
        if (max != that.max) return false;
        if (drained != that.drained) return false;
        if (!value.equals(that.value)) return false;
        return date.equals(that.date);

    }

    @Override
    public int hashCode() {
        int result = (int) (min ^ (min >>> 32));
        result = 31 * result + (int) (max ^ (max >>> 32));
        result = 31 * result + value.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + (drained ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SequenceRange{" +
                "min=" + min +
                ", max=" + max +
                ", value=" + value +
                ", date=" + date +
                ", drained=" + drained +
                '}';
    }
}
