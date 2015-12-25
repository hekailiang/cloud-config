package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.squirrelframework.cloud.conf.ZkPath;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kailianghe on 15/12/23.
 */
public abstract class DispatchableRoutingResolver implements RoutingKeyResolver, InitializingBean {

    protected static final Logger logger = LoggerFactory.getLogger(DispatchableRoutingResolver.class);

    private String path;

    protected CuratorFramework client;

    private boolean autoRefresh = false;

    private int refreshInterval = 10;

    abstract protected CacheLoader<String, Iterator<String>> getCacheLoader();

    private LoadingCache<String, Iterator<String>> cachedIterators;

    @Override
    public void afterPropertiesSet() throws Exception {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        if(autoRefresh) {
            cacheBuilder.refreshAfterWrite(refreshInterval, TimeUnit.MINUTES);
        }
        cachedIterators = cacheBuilder.build(getCacheLoader());
    }

    @Override
    public Optional<String> get() {
        String routingPath = buildRoutingPath();
        try {
            Iterator<String> iterator = cachedIterators.get(routingPath);
            String value = iterator.next();
            logger.debug("Dispatched to path \"{}/{}\"", routingPath, value);
            return Optional.of(value);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Cannot find any dispatchable candidates under \""+routingPath+"\"", e.getCause());
        }
    }

    protected String buildRoutingPath() {
        List<String> routingKeys = RoutingKeyHolder.getRoutingKeys();
        String zkPath = path;
        if(!routingKeys.isEmpty()) {
            zkPath += ZkPath.PATH_SEPARATOR + StringUtils.join(routingKeys, ZkPath.PATH_SEPARATOR);
        }
        return zkPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Autowired
    public void setClient(CuratorFramework client) {
        this.client = client.usingNamespace(CloudConfigCommon.CONFIG_ROOT);
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
