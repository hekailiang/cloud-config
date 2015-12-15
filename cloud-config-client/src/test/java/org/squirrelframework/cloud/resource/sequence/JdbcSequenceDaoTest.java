package org.squirrelframework.cloud.resource.sequence;

import com.jolbox.bonecp.BoneCPDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by kailianghe on 15/12/15.
 */
public class JdbcSequenceDaoTest {

    public static final String INIT_SQL=
            "CREATE TABLE IF NOT EXISTS __sequence_table__ (" +
            "name varchar(64) NOT NULL," +
            "value varchar(20) NOT NULL," +
            "min_limit varchar(20) NOT NULL," +
            "max_limit varchar(20) NOT NULL," +
            "step varchar(20) NOT NULL," +
            "create_time datetime NOT NULL," +
            "modified_time datetime NOT NULL," +
            "PRIMARY KEY (name)" +
            ");";

    public static final String JDBC_URL = "jdbc:h2:mem:dev;MODE=MySQL;INIT="+INIT_SQL;

    private JdbcSequenceDao jdbcSequenceDao;

    private BoneCPDataSource dataSource;

    private int step = 10;

    private int maxLimit = 40;

    @Before
    public void setUp() throws Exception {
        dataSource = new BoneCPDataSource();
        dataSource.setDriverClass("org.h2.Driver");
        dataSource.setJdbcUrl(JDBC_URL);
        jdbcSequenceDao = new JdbcSequenceDao();
        jdbcSequenceDao.setJdbcTemplate(new JdbcTemplate(dataSource));
        jdbcSequenceDao.setStep(step);
        jdbcSequenceDao.setDefaultMaxLimit(maxLimit);
    }

    @After
    public void tearDown() throws Exception {
        dataSource.close();
        jdbcSequenceDao=null;
    }

    @Test
    public void testApplyNewRange() {
        SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr, 1L, step);
        SequenceRange sr2 = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr2, 11L, step);
        SequenceRange sr3 = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr3, 21L, step);
    }

    @Test
    public void testApplyMultipleRange() {
        SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr, 1L, step);
        SequenceRange sr2 = jdbcSequenceDao.applyNextRange("test2");
        validateSequenceRange(sr2, 1L, step);
    }

    @Test
    public void testBeyondMaxLimit() {
        SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr, 1L, step);
        SequenceRange sr2 = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr2, 11L, step);
        SequenceRange sr3 = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr3, 21L, step);
        SequenceRange sr4 = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr4, 31L, step);
        SequenceRange sr5 = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr5, 1L, step);
    }

    private void validateSequenceRange(SequenceRange sr, long min, int step) {
        assertThat(sr.getMin(), is(min));
        assertThat(sr.getMax(), is(min+step));
        assertThat(sr.getValue().get(), is(min));
        for(int i=0; i<step; i++) {
            assertThat(sr.getAndIncrement(), is(min+i));
        }
//        assertThat(sr.getAndIncrement(), is(-1L));
        assertThat(sr.isExhausted(), is(true));
    }

    @Test(timeout = 10000)
    public void testMultiThreadCreation() throws Exception {
        final AtomicBoolean effect = new AtomicBoolean(true);
        final CyclicBarrier barrier = new CyclicBarrier(2, new Runnable() {
            @Override
            public void run() {
                effect.set(false);
            }
        });
        final AtomicInteger invokedTimes = new AtomicInteger(0);
        JdbcTemplate testJdbcTemplate = new JdbcTemplate(){
            @Override
            public SqlRowSet queryForRowSet(String sql, Object... args) throws DataAccessException {
                try {
                    if(effect.get()) {
                        barrier.await();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                invokedTimes.incrementAndGet();
                return super.queryForRowSet(sql, args);
            }
        };
        testJdbcTemplate.setDataSource(dataSource);
        jdbcSequenceDao.setJdbcTemplate(testJdbcTemplate);

        final SequenceRange[] ranges = new SequenceRange[2];
        final CountDownLatch latch = new CountDownLatch(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
                ranges[0]=sr;
                latch.countDown();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
                ranges[1]=sr;
                latch.countDown();
            }
        }).start();

        latch.await();

        if(ranges[0].getMin()>ranges[1].getMin()) {
            validateSequenceRange(ranges[0], 11L, step);
            validateSequenceRange(ranges[1], 1L, step);
        } else {
            validateSequenceRange(ranges[0], 1L, step);
            validateSequenceRange(ranges[1], 11L, step);
        }
        assertThat(invokedTimes.get(), is(3+2));
    }

    @Test
    public void testMultiThreadUpdate() throws InterruptedException {
        final AtomicBoolean effect = new AtomicBoolean(false);
        final CyclicBarrier barrier = new CyclicBarrier(2, new Runnable() {
            @Override
            public void run() {
                effect.set(false);
            }
        });
        final AtomicInteger invokedTimes = new AtomicInteger(0);
        JdbcTemplate testJdbcTemplate = new JdbcTemplate(){
            @Override
            public int update(String sql, Object... args) throws DataAccessException {
                try {
                    if(effect.get()) {
                        barrier.await();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                invokedTimes.incrementAndGet();
                return super.update(sql, args);
            }
        };
        testJdbcTemplate.setDataSource(dataSource);
        jdbcSequenceDao.setJdbcTemplate(testJdbcTemplate);

        SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
        validateSequenceRange(sr, 1L, step);

        effect.set(true);

        final SequenceRange[] ranges = new SequenceRange[2];
        final CountDownLatch latch = new CountDownLatch(2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
                ranges[0]=sr;
                latch.countDown();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SequenceRange sr = jdbcSequenceDao.applyNextRange("test1");
                ranges[1]=sr;
                latch.countDown();
            }
        }).start();

        latch.await();

        if(ranges[0].getMin()>ranges[1].getMin()) {
            validateSequenceRange(ranges[0], 21L, step);
            validateSequenceRange(ranges[1], 11L, step);
        } else {
            validateSequenceRange(ranges[0], 11L, step);
            validateSequenceRange(ranges[1], 21L, step);
        }
        assertThat(invokedTimes.get(), is(3+1));
    }

    @Test
    public void testContinuousGenerateSequence() throws Exception {
        jdbcSequenceDao.setDefaultMaxLimit(4);
        jdbcSequenceDao.setStep(2);
        JdbcSequenceGenerator sg = new JdbcSequenceGenerator();
        sg.setSequenceDao(jdbcSequenceDao);
        sg.setSequenceFormatter(new SequenceFormatter() {
            @Override
            public String format(SequenceRange sequenceRange) {
                return sequenceRange.getAndIncrement()+"";
            }
        });
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<10; i++) {
            builder.append(sg.next("test1"));
        }
        assertThat(Long.valueOf(builder.toString()), is(1234123412L));
    }
}
