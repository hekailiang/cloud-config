package org.squirrelframework.cloud.routing;

/**
 * Created by kailianghe on 15/12/9.
 */
public class DeclarativeRoutingKeyHolder {

    private static final ThreadLocal<String> holder = new ThreadLocal<String>();

    public static void setRoutingKey(String routingKey) {
        holder.set(routingKey);
    }

    public static void resetRoutingKey() {
        holder.set(null);
    }

    public static String getRoutingKey() {
        return holder.get();
    }
}
