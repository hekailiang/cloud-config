package org.squirrelframework.cloud.utils;

import com.google.common.cache.CacheLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by kailianghe on 9/17/15.
 */
public abstract class CloudConfigCommon {

    public static final String ZK_CONNECT_STRING_KEY = "config.center.url";

    public static final String NAMESPACE = getConfProperty("namespace", "root");

    public static final String PROPERTY_ROOT = NAMESPACE + "/properties";

    public static final String CONFIG_ROOT   = NAMESPACE + "/config";

    public static final String CONFIG_PROFILE_KEY = "config.profile";

    public static final String ENABLE_ENCRYPTION = "enable.encryption";

    public static final String SPRING_PROFILE_KEY = "spring.profiles.active";

    public static final String DEFAULT_CONFIG_PROFILE = "dev";

    public static final String ZK_CLIENT_BEAN_NAME = "org.squirrelframework.cloud.conf.ZkClientBean";

    public static final String DB_NAME_KEY = "dbName";
    public static final String DB_DATE_KEY = "dbDate";
    public static final String DB_DATE_STR_KEY = "dbDateStr";
    public static final String SEQUENCE_VALUE_KEY = "sequenceValue";

    public static String[] getProfiles() {
        Set<String> result = new LinkedHashSet<>();
        String value = getConfProperty(CONFIG_PROFILE_KEY, DEFAULT_CONFIG_PROFILE) +
                 "," + getConfProperty(SPRING_PROFILE_KEY, "");
        String[] profiles =  StringUtils.split(value, ',');
        for(int i=0; i<profiles.length; ++i) {
            result.add(profiles[i].trim());
        }
        return result.toArray(new String[0]);
    }

    public static boolean isEncryptionEnabled() {
        return "true".equals( getConfProperty(ENABLE_ENCRYPTION, "false") );
    }

    public static String getConfProperty(String key, String defaultValue) {
        return System.getProperty(key)!=null ? System.getProperty(key) :
                System.getenv(key)!=null ? System.getenv(key) : defaultValue;
    }

    public static Integer getSafeInteger(final String value) {
        if(StringUtils.isNumeric(value)) {
            return Integer.valueOf(value);
        }
        return null;
    }

    public static Long getSafeLong(final String value) {
        if(StringUtils.isNumeric(value)) {
            return Long.valueOf(value);
        }
        return null;
    }

    public static Boolean getSafeBoolean(final String value) {
        if(StringUtils.isNotBlank(value)) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    public static String bytes2String(byte[] content) throws UnsupportedEncodingException {
        return new String(content, "UTF-8");
    }

    public static final CacheLoader<String, Expression> EL_EXPRESSION_LOADER = new CacheLoader<String, Expression>() {
        @Override
        public Expression load(String elExpr) throws Exception {
            ExpressionParser parser = new SpelExpressionParser();
            Expression expression = parser.parseExpression(elExpr);
            return expression;
        }
    };
}
