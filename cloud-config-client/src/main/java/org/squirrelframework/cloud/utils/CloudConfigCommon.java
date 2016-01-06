package org.squirrelframework.cloud.utils;

import com.google.common.cache.CacheLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public static final String ZK_DEFAULT_CIPHER_ENCODER_BEAN_ID = "zk-default-cipher-encoder";

    public static final String ZK_DEFAULT_CIPHER_DECODER_BEAN_ID = "zk-default-cipher-decoder";

    public static final String DB_NAME_KEY = "dbName";

    public static final String DB_DATE_KEY = "dbDate";

    public static final String DB_DATE_STR_KEY = "dbDateStr";

    public static final String SEQUENCE_VALUE_KEY = "sequenceValue";

    public static final ExecutorService EVENT_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    public static final String STRING_ARRAY_SEPARATOR = " ,;|";

    public static final String SIGNATURE_ALGORITHM = "SHA1WithRSA";

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
        return Boolean.FALSE;
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

    public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }
        if (endIndexExclusive > array.length) {
            endIndexExclusive = array.length;
        }
        int newSize = endIndexExclusive - startIndexInclusive;
        if (newSize <= 0) {
            return new byte[0];
        }

        byte[] subarray = new byte[newSize];
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }

    public static byte[] addAll(byte[] array1, byte[] array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static byte[] clone(byte[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }
}
