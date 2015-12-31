package org.squirrelframework.cloud.resource.cache;

import com.google.common.collect.Maps;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;

/**
 * Created by kailianghe on 15/12/31.
 */
public class MockUserService {

    private int findInvokeCount = 0;

    public Map<String, MockUser> userStore = Maps.newHashMap();

    public MockUserService() {
        userStore.put("1", new MockUser("1", "hhe1", 31));
        userStore.put("2", new MockUser("2", "hhe2", 32));
        userStore.put("3", new MockUser("3", "hhe3", 33));
        userStore.put("4", new MockUser("4", "hhe4", 34));
    }

    @Cacheable("user")
    public MockUser findUserById(String id) {
        findInvokeCount++;
        return userStore.get(id);
    }

    @CacheEvict(value = "user", allEntries = true)
    public void clearAllCached() {
        System.out.println("Evict all user caches");
    }

    @Cacheable("user2")
    public MockUser findUserById2(String id) {
        findInvokeCount++;
        return userStore.get(id);
    }

    @CacheEvict(value = "user2", allEntries = true)
    public void clearAllCached2() {
        System.out.println("Evict all user2 caches");
    }

    public int getFindInvokeCount() {
        return findInvokeCount;
    }
}
