package org.squirrelframework.cloud.utils;

import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.resource.database.JdbcDataSourceConfig;
import org.squirrelframework.cloud.resource.keystore.KeystoreConfig;
import org.squirrelframework.cloud.resource.sequence.SequenceDao;
import org.squirrelframework.cloud.resource.sequence.SequenceGenerator;

import javax.sql.DataSource;

/**
 * Created by kailianghe on 15/12/17.
 */
public class BeanIdGenerator {

    public static String generateBeanId(String path, Class<?> type) {
        String postfix = "?";
        if(DataSource.class.isAssignableFrom(type)) {
            postfix = "DS";
        } else if(CloudResourceConfig.class.isAssignableFrom(type)) {
            if(JdbcDataSourceConfig.class.isAssignableFrom(type)) {
                postfix = "DS";
            } else if(KeystoreConfig.class.isAssignableFrom(type)) {
                postfix = "KS";
            } else {
                postfix = type.getSimpleName();
            }
            postfix = postfix + "_CNF";
        } else if(SequenceDao.class.isAssignableFrom(type)) {
            postfix = "DAO";
        } else if(SequenceGenerator.class.isAssignableFrom(type)) {
            postfix = "SEQ";
        }
        return "_"+path.replace('/', '_')+postfix;
    }

    public static String getDataSourceBeanId(String path) {
        return generateBeanId(path, DataSource.class);
    }

    public static String getResourceConfigBeanId(String path, Class<? extends CloudResourceConfig> type) {
        return generateBeanId(path, type);
    }

    public static String getSequenceDaoBeanId(String path) {
        return generateBeanId(path, SequenceDao.class);
    }

    public static String getSequenceGeneratorBeanId(String path) {
        return generateBeanId(path, SequenceGenerator.class);
    }
}
