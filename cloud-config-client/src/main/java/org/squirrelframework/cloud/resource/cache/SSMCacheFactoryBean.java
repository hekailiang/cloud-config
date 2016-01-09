package org.squirrelframework.cloud.resource.cache;

import com.google.code.ssm.Cache;
import com.google.code.ssm.spring.SSMCache;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Created by kailianghe on 16/1/9.
 */
public class SSMCacheFactoryBean extends AbstractFactoryBean<SSMCache> {

    private com.google.code.ssm.Cache cache;

    private int expiration = 300;

    private boolean allowClear = true;

    private boolean registerAliases = false;

    @Override
    public Class<?> getObjectType() {
        return SSMCache.class;
    }

    @Override
    protected SSMCache createInstance() throws Exception {
        return new SSMCache(cache, expiration, allowClear, registerAliases);
    }

    @Required
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public void setAllowClear(boolean allowClear) {
        this.allowClear = allowClear;
    }

    public void setRegisterAliases(boolean registerAliases) {
        this.registerAliases = registerAliases;
    }
}
