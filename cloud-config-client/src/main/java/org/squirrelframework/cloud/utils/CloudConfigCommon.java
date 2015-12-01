package org.squirrelframework.cloud.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by kailianghe on 9/17/15.
 */
public abstract class CloudConfigCommon {

    public static final String ZK_CONNECT_STRING_KEY = "config.center.url";

    public static final String NAMESPACE = getConfProperty("namespace", "root");

    public static final String PROPERTY_ROOT = NAMESPACE + "/properties";

    public static final String CONFIG_ROOT   = NAMESPACE + "/config";

    public static String CONFIG_PROFILE_KEY = "config.profile";

    public static String SPRING_PROFILE_KEY = "spring.profiles.active";

    public static String DEFAULT_CONFIG_PROFILE = "dev";

    public static final String ZK_CLIENT_BEAN_NAME = "org.squirrelframework.cloud.conf.ZkClientBean";

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

    public static Boolean getSafeBoolean(final String value) {
        if(StringUtils.isNotBlank(value)) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    public static String bytes2String(byte[] content) throws UnsupportedEncodingException {
        return new String(content, "UTF-8");
    }

    public static void safeCreateZkNodeIfNotExists(CuratorFramework zkClient, String targetPath) throws Exception {
        safeCreateZkNodeIfNotExists(zkClient, targetPath, "".getBytes());
    }

    public static void safeCreateZkNodeIfNotExists(CuratorFramework zkClient, String targetPath, byte[] data) throws Exception {
        if(targetPath!=null && targetPath.length()>0 && targetPath.charAt(0)!='/') {
            targetPath = "/"+targetPath;
        }
        if(zkClient.checkExists().forPath(targetPath) == null) {
            zkClient.create().creatingParentsIfNeeded().forPath(targetPath, data);
        }
    }
}
