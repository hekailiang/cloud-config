package org.squirrelframework.cloud.routing;

import java.util.*;

/**
 * Created by kailianghe on 15/12/10.
 */
public class RoutingKeyHolder {

    private static final ThreadLocal<Boolean> newEntryFlag = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.TRUE;
        }
    };

    public static final boolean isNewEntry() {
        return newEntryFlag.get();
    }

    public static void setNewEntry(boolean isNewEntry) {
        newEntryFlag.set(isNewEntry);
    }

    public static void removeNewEntry() {
        newEntryFlag.remove();
    }

    private static final ThreadLocal<Queue<String>> declarativeRoutingKeyHolder = new ThreadLocal<Queue<String>>() {
        @Override
        protected Queue<String> initialValue() {
            return new LinkedList<>();
        }
    };

    public static void putDeclarativeRoutingKey(String routingKey) {
        declarativeRoutingKeyHolder.get().offer(routingKey);
    }

    public static void removeDeclarativeRoutingKeys() {
        declarativeRoutingKeyHolder.remove();
    }

    public static String getDeclarativeRoutingKey() {
        return declarativeRoutingKeyHolder.get().poll();
    }

    private static final ThreadLocal<List<String>> holder = new ThreadLocal<List<String>>() {
        @Override
        protected List<String> initialValue() {
            return new ArrayList<>();
        }
    };

    public static void putRoutingKey(String routingKey) {
        holder.get().add(routingKey);
    }

    public static void removeRoutingKeys() {
        holder.remove();
    }

    public static List<String> getRoutingKeys() {
        return Collections.unmodifiableList(holder.get());
    }

    private static final ThreadLocal<Boolean> routingKeyTraceEnabled = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public static void setRoutingKeyTraceEnabled(boolean isEnable) {
        routingKeyTraceEnabled.set(isEnable);
    }

    public static boolean isRoutingKeyTraceEnabled() {
        return routingKeyTraceEnabled.get();
    }

    public static void removeRoutingKeyTraceEnabled() {
        routingKeyTraceEnabled.remove();
    }
}
