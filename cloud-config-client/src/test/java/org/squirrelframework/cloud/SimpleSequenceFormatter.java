package org.squirrelframework.cloud;

import org.squirrelframework.cloud.resource.sequence.SequenceFormatter;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import java.util.Map;

/**
 * Created by kailianghe on 15/12/16.
 */
public class SimpleSequenceFormatter implements SequenceFormatter {

    @Override
    public String format(Map<String, Object> parameters) {
        return parameters.get(CloudConfigCommon.SEQUENCE_VALUE_KEY).toString();
    }
}
