package com.google.code.ssm.providers.xmemcached;

import com.google.code.ssm.providers.*;
import net.rubyeye.xmemcached.MemcachedClient;
import org.squirrelframework.cloud.routing.RoutingKeyResolver;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.*;

/**
 * Created by kailianghe on 15/12/27.
 */
public class RoutingMemcacheClientFactoryImpl extends MemcacheClientFactoryImpl {

    private final RoutingKeyResolver resolver;

    public RoutingMemcacheClientFactoryImpl(RoutingKeyResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public CacheClient create(final List<InetSocketAddress> addrs, final CacheConfiguration conf) throws IOException {
        MemcacheClientWrapper clientWrapper = (MemcacheClientWrapper)super.create(addrs, conf);
        if (resolver==null) {
            return clientWrapper;
        }
        final MemcachedClient memcachedClient = (MemcachedClient) clientWrapper.getNativeClient();
        Object proxyInstance = Proxy.newProxyInstance( getClass().getClassLoader(), new Class[]{ MemcachedClient.class }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if(resolver.get().isPresent()) {
                    String routingKey = resolver.get().get();
                    memcachedClient.beginWithNamespace(routingKey);
                    try {
                        return method.invoke(memcachedClient, args);
                    } finally {
                        memcachedClient.endWithNamespace();
                    }
                } else {
                    throw new IllegalStateException("Unresolved routing key");
                }
            }
        });
        return new MemcacheClientWrapper((MemcachedClient) proxyInstance) ;
    }
}
