package org.squirrelframework.cloud.routing;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.squirrelframework.cloud.conf.ZkPath;
import org.squirrelframework.cloud.utils.CloudConfigCommon;
import org.squirrelframework.cloud.utils.RoundRobin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by kailianghe on 15/12/10.
 */
public class DispatchableRoutingKeyResolver implements RoutingKeyResolver {

    private static final Logger logger = LoggerFactory.getLogger(DispatchableRoutingKeyResolver.class);

    private String path;

    private CuratorFramework client;

    Map<String, Iterator<String>> cachedRoundRobinIter = Maps.newHashMap();

    @Override
    public Optional<String> get() {
        List<String> routingKeys = RoutingKeyHolder.getRoutingKeys();
        String zkPath = routingKeys.isEmpty() ? path :
                path + ZkPath.PATH_SEPARATOR + StringUtils.join(routingKeys, ZkPath.PATH_SEPARATOR);
        if(!cachedRoundRobinIter.containsKey(zkPath)) {
            List<String> candidates;
            try {
                candidates = client.getChildren().forPath(zkPath);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot find any dispatchable candidates under \""+zkPath+"\"", e);
            }
            cachedRoundRobinIter.put(zkPath, new RoundRobin<>(candidates).iterator());
        }
        Iterator<String> roundRobinIter = cachedRoundRobinIter.get(zkPath);
        String value = roundRobinIter.next();
        logger.debug("Dispatched to path \"{}/{}\"", zkPath, value);
        return Optional.of(value);
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Autowired
    public void setClient(CuratorFramework client) {
        this.client = client.usingNamespace(CloudConfigCommon.CONFIG_ROOT);
    }
}
