package org.squirrelframework.cloud.utils;

import com.google.code.ssm.Cache;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.resource.cache.MemcachedResourceConfig;
import org.squirrelframework.cloud.resource.security.*;
import org.squirrelframework.cloud.resource.database.JdbcDataSourceConfig;
import org.squirrelframework.cloud.resource.sequence.SequenceDao;
import org.squirrelframework.cloud.resource.sequence.SequenceGenerator;

import javax.sql.DataSource;

/**
 * Created by kailianghe on 15/12/17.
 */
public class BeanIdGenerator {

    public static String generateBeanId(String path, Class<?> type) {
        String postfix;
        if(DataSource.class.isAssignableFrom(type)) {
            postfix = "DS";
        } else if(CloudResourceConfig.class.isAssignableFrom(type)) {
            if(JdbcDataSourceConfig.class.isAssignableFrom(type)) {
                postfix = "DS";
            } else if(CoderConfig.class.isAssignableFrom(type)) {
                postfix = "CC";
            } else if(MemcachedResourceConfig.class.isAssignableFrom(type)) {
                postfix = "CAC";
            } else {
                postfix = type.getSimpleName();
            }
            postfix = postfix + "_CNF";
        } else if(SequenceDao.class.isAssignableFrom(type)) {
            postfix = "DAO";
        } else if(SequenceGenerator.class.isAssignableFrom(type)) {
            postfix = "SEQ";
        } else if(CipherEncoder.class.isAssignableFrom(type)) {
            postfix = "CE";
        } else if(CipherDecoder.class.isAssignableFrom(type)) {
            postfix = "CD";
        } else if(SignatureChecker.class.isAssignableFrom(type)) {
            postfix = "SC";
        } else if(RSASignatureEncoder.class.isAssignableFrom(type)) {
            postfix = "SE";
        } else if(Cache.class.isAssignableFrom(type)) {
            postfix = "CAC";
        } else {
            postfix = type.getSimpleName();
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

    public static String getCipherEncoderBeanId(String path) {
        return generateBeanId(path, CipherEncoder.class);
    }

    public static String getCipherDecoderBeanId(String path) {
        return generateBeanId(path, CipherDecoder.class);
    }

    public static String getSignatureEncoderBeanId(String path) {
        return generateBeanId(path, RSASignatureEncoder.class);
    }

    public static String getSignatureCheckerBeanId(String path) {
        return generateBeanId(path, SignatureChecker.class);
    }

    public static String getMemcachedBeanId(String path) {
        return generateBeanId(path, Cache.class);
    }
}
