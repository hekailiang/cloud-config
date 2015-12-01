package org.squirrelframework.cloud.conf;

import org.junit.Test;

import java.util.Properties;

/**
 * Created by kailianghe on 11/9/15.
 */
public class JsonFlattenConverterTest {
    private static String testJson = "{\n" +
            "   \"Port\":\n" +
            "   {\n" +
            "       \"@alias\": \"defaultHttp\",\n" +
            "       \"Enabled\": \"true\",\n" +
            "       \"Number\": \"10092\",\n" +
            "       \"Protocol\": \"http\",\n" +
            "       \"KeepAliveTimeout\": \"20000\",\n" +
            "       \"ThreadPool\":\n" +
            "       {\n" +
            "           \"@enabled\": \"false\",\n" +
            "           \"Max\": \"150\",\n" +
            "           \"ThreadPriority\": \"5\"\n" +
            "       },\n" +
            "       \"ExtendedProperties\":\n" +
            "       {\n" +
            "           \"Property\":\n" +
            "           [                         \n" +
            "               {\n" +
            "                   \"@name\": \"connectionTimeout\",\n" +
            "                   \"$\": \"20000\"\n" +
            "               }\n" +
            "           ]\n" +
            "       }\n" +
            "   }\n" +
            "}";

    @Test
    public void testCreatingKeyValues() {
        Properties properties = new Properties();
        new JsonFlattenConverter().flatten(testJson, properties);
        properties.list(System.out);

        assert properties.size()==10;
    }
}
