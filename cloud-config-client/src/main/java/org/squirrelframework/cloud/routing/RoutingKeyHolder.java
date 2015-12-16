package org.squirrelframework.cloud.routing;

import java.util.*;

/**
 * Created by kailianghe on 15/12/10.
 */
public class RoutingKeyHolder {

    private static final ThreadLocal<Boolean> routingEntryFlag = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.TRUE;
        }
    };

    public static final boolean isRoutingEntry() {
        return routingEntryFlag.get();
    }

    public static void setRoutingEntry(boolean isRoutingEntry) {
        routingEntryFlag.set(isRoutingEntry);
    }

    public static void removeRoutingEntry() {
        routingEntryFlag.remove();
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

    public static String pollDeclarativeRoutingKey() {
        return declarativeRoutingKeyHolder.get().poll();
    }

    public static String peekDeclarativeRoutingKey() {
        return declarativeRoutingKeyHolder.get().peek();
    }

    public static String cycleGetDeclarativeRoutingKey() {
        String routingKey = pollDeclarativeRoutingKey();
        putDeclarativeRoutingKey(routingKey);
        return routingKey;
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
