package org.squirrelframework.cloud;

import org.apache.curator.framework.CuratorFramework;

/**
 * Created by kailianghe on 15/12/10.
 */
public class ZkUtils {
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
