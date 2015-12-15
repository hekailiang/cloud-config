package org.squirrelframework.cloud.resource.sequence;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.Date;

/**
 * Created by kailianghe on 15/12/14.
 */
public class JdbcSequenceDao implements SequenceDao, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JdbcSequenceDao.class);

    // by default support mysql grammar, however use can config corresponding sql in either zookeeper or property file to override the default sql
    public static final String DEFAULT_INSERT_SQL = "insert into __sequence_table__(name, min_limit, max_limit, step, value, create_time, modified_time) values (? , ?, ?, ?, ?, current_timestamp(), current_timestamp())";
    public static final String DEFAULT_UPDATE_SQL = "update __sequence_table__ set value=?, modified_time = current_timestamp() where name =? and value = ?";
    public static final String DEFAULT_SELECT_SQL = "select name, min_limit, max_limit, step, value, modified_time from __sequence_table__ where (name = ?)";
    public static final String DEFAULT_DBDATE_SQL = "select current_timestamp();";

    private String insertSql = DEFAULT_INSERT_SQL;

    private String updateSql = DEFAULT_UPDATE_SQL;

    private String selectSql = DEFAULT_SELECT_SQL;

    private String dbDateSql = DEFAULT_DBDATE_SQL;

    private int step = 100;

    private JdbcTemplate jdbcTemplate;

    private int maxRetryTimes = 8;

    private long defaultMinLimit = 1;

    private long defaultMaxLimit = Long.MAX_VALUE;

    private DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(defaultMaxLimit < step) {
            logger.warn("defaultMaxLimit({}) cannot less than step({})", defaultMaxLimit, step);
            defaultMaxLimit = step;
        }
        if(defaultMaxLimit <= defaultMinLimit) {
            throw new IllegalArgumentException("defaultMaxLimit("+defaultMaxLimit
                    +") must larger than defaultMinLimit("+defaultMinLimit+")");
        }
        if(jdbcTemplate==null && dataSource!=null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
    }

    @Override
    public SequenceRange applyNextRange(String seqName) {
        for(int i=0; i<maxRetryTimes; ++i) {
            try {
                SequenceRange result = applyNextRange0(seqName);
                if (result!=null) {
                    return result;
                }
                logger.debug("apply next range for sequence return null {} time", i+1);
            } catch (Exception e) {
                logger.debug("apply next range for sequence \"{}\" failed {} time caused by: {}",
                        seqName, i+1, e.getCause().getMessage());
            }
        }
        throw new RuntimeException("fail to apply next sequence range retried too many times.");
    }

    private SequenceRange applyNextRange0(String seqName) {
        SequenceRange result = null;
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(selectSql, seqName);
        if(rowSet.next()) {
            long maxLimit = rowSet.getLong("max_limit");
            long minLimit = rowSet.getLong("min_limit");
            int range = rowSet.getInt("step");
            int value = rowSet.getInt("value");

            long min = value;
            long max = value + range;
            if(max < 0 || max-1 > maxLimit) {
                min = minLimit;
                max = minLimit + range;
            }
            int updatedRows = jdbcTemplate.update(updateSql, max, seqName, value);
            if(updatedRows==1) {
                result = new SequenceRange(min, max, getDbDate());
            }
        } else {
            long value = defaultMinLimit+step;
            int insertedRows = jdbcTemplate.update(insertSql, seqName, defaultMinLimit, defaultMaxLimit, step, value);
            if(insertedRows == 1) {
                result = new SequenceRange(defaultMinLimit, value, getDbDate());
            }
        }
        return result;
    }

    private Date getDbDate() {
        try {
            return jdbcTemplate.queryForRowSet(dbDateSql, null).getDate(1);
        } catch (Exception e) {
            return new Date();
        }
    }

    public void setStep(int step) {
        Preconditions.checkArgument(step >1 && step <100000, "sequence step must between 1-100000");
        this.step = step;
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public void setDbDateSql(String dbDateSql) {
        this.dbDateSql = dbDateSql;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public void setDefaultMinLimit(long defaultMinLimit) {
        this.defaultMinLimit = defaultMinLimit;
    }

    public void setDefaultMaxLimit(long defaultMaxLimit) {
        this.defaultMaxLimit = defaultMaxLimit;
    }

}
