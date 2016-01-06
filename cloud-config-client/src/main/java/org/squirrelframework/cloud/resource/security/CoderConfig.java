package org.squirrelframework.cloud.resource.security;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squirrelframework.cloud.resource.CloudResourceConfig;

/**
 * Created by kailianghe on 15/12/19.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="__type__")
public abstract class CoderConfig extends CloudResourceConfig {
}
