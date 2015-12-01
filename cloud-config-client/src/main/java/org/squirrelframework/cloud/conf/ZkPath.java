package org.squirrelframework.cloud.conf;

import org.springframework.util.StringUtils;

/**
 * Created by kailianghe on 8/28/15.
 */
public class ZkPath {

    public final static char PATH_SEPARATOR = '/';

    private final String path;

    private ZkPath(String path) {
        this.path = path;
    }

    public ZkPath getRoot() {
        int idx = path.indexOf(PATH_SEPARATOR);
        return idx<0 ? this : create( path.substring(0, idx) );
    }

    public ZkPath getParent() {
        int idx = path.lastIndexOf(PATH_SEPARATOR);
        return idx<=0 ? null : create( path.substring(0, idx) );
    }

    public ZkPath appendChild(String childPath) {
        return create( path + PATH_SEPARATOR + childPath );
    }

    public int getDepth() {
        return path.isEmpty() ? 0 : StringUtils.countOccurrencesOf(path, "/")+1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZkPath)) return false;

        ZkPath zkPath = (ZkPath) o;

        if (!path.equals(zkPath.path)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path;
    }

    public static ZkPath create(String path) {
        if (path==null || path.isEmpty()) {
            throw new IllegalStateException("Not a valid path");
        }
        return new ZkPath(path);
    }
}
