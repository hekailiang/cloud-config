package org.squirrelframework.cloud.conf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by kailianghe on 8/28/15.
 */
public class JsonFlattenConverter {

    private static final Logger logger = LoggerFactory.getLogger(JsonFlattenConverter.class);

    private boolean isAllowOverride = true;

    private boolean ignoreInvalidJson = false;

    public void flatten(String json, Properties props) {
        JsonNode jsonNode;
        try {
            jsonNode = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true).readTree(json);
            addKeys("", jsonNode, props);
        } catch (IOException e) {
            String errMsg = "Invalid json format\n"+json;
            if(ignoreInvalidJson) {
                logger.error(errMsg);
            } else {
                throw new RuntimeException(errMsg, e);
            }
        }
    }

    private void addKeys(String currentPath, JsonNode jsonNode, Map<Object, Object> props) {
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
            String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";

            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                addKeys(pathPrefix + entry.getKey(), entry.getValue(), props);
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                addKeys(currentPath + "[" + i + "]", arrayNode.get(i), props);
            }
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            if(isAllowOverride || !props.containsKey(currentPath))
                props.put(currentPath, valueNode.asText());
        }
    }

    public void setAllowOverride(boolean isAllowOverride) {
        this.isAllowOverride = isAllowOverride;
    }

    public void setIgnoreInvalidJson(boolean ignoreInvalidJson) {
        this.ignoreInvalidJson = ignoreInvalidJson;
    }

}
