package org.squirrelframework.cloud.routing;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by kailianghe on 15/12/9.
 */
public class DeclarativeRoutingKeyHolder {

    private static final ThreadLocal<Queue<String>> holder = new ThreadLocal<Queue<String>>() {
        @Override
        protected Queue<String> initialValue() {
            return new LinkedList<>();
        }
    };

    public static void putRoutingKey(String routingKey) {
        holder.get().offer(routingKey);
    }

    public static void removeRoutingKey() {
        if(holder.get().isEmpty()) {
            holder.remove();
        }
    }

    public static String getRoutingKey() {
        return holder.get().poll();
    }
}
