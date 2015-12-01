package org.squirrelframework.cloud;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import static org.springframework.http.MediaType.*;
import org.springframework.web.bind.annotation.*;
import org.squirrelframework.cloud.annotation.UIProperty;
import org.squirrelframework.cloud.conf.JsonFlattenConverter;
import org.squirrelframework.cloud.conf.ZkPath;
import org.squirrelframework.cloud.resource.CloudResourceConfig;
import org.squirrelframework.cloud.utils.CloudConfigCommon;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api")
public class CloudConfigController {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CuratorFramework zkClient;

    @Autowired
    private JsonFlattenConverter jsonFlattenConverter;

    @PreDestroy
    public void destroy() {
        zkClient.close();
    }

    public static String toLabelFormat(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    public static final String BODY_FORMAT = "{\"__type__\":\"%s\", \"title\":\"%s\", \"properties\":{%s}}";
    public static final String PROPERTY_FORMAT = "\"%s\":{\"type\":\"%s\",\"label\":\"%s\",\"placeholder\":\"%s\",\"defaultValue\":\"%s\",\"order\":\"%d\"}";
    public static String extractResourceConfigMeta(CloudResourceConfig object) throws Exception {
        Class<?> currClazz = object.getClass();
        UIProperty clazzProp = object.getClass().getAnnotation(UIProperty.class);
        String title = (clazzProp!=null) ? clazzProp.label() : toLabelFormat(object.getClass().getSimpleName());

        StringBuilder propertiesBuilder = new StringBuilder();
        do {
            for (Method method : currClazz.getDeclaredMethods()) {
                if(method.getName().startsWith("set") && method.getParameterTypes().length==1) {
                    String type = method.getParameterTypes()[0].getSimpleName().toLowerCase();
                    UIProperty props = null;
                    try {
                        String prefix = "boolean".equals(type) ? "is" : "get";
                        Method getter = currClazz.getDeclaredMethod(prefix+method.getName().substring(3), new Class[0]);
                        props = getter.getAnnotation(UIProperty.class);
                    } catch (Exception e) {}

                    String label = (props!=null) ? props.label() : toLabelFormat(method.getName().substring(3));
                    String placeholder = (props!=null && props.placeholder().length()>0) ? props.placeholder() : label;
                    String name =  Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
                    int order =  (props!=null) ? props.order() : 0;

                    Field f = null;
                    boolean orgAcc = false;
                    String defaultValue = "";
                    try {
                        f = currClazz.getDeclaredField(name);
                        orgAcc = f.isAccessible();
                        f.setAccessible(true);

                        Object fVal = f.get(object);
                        defaultValue = fVal!=null ? fVal.toString() : null;
                        if(defaultValue == null && props!=null && props.defaultValue().length()>0) {
                            defaultValue = props.defaultValue();
                        }
                    } finally {
                        if(f!=null) {
                            f.setAccessible(orgAcc);
                        }
                    }

                    if(propertiesBuilder.length()>0) {
                        propertiesBuilder.append(',');
                    }
                    propertiesBuilder.append(String.format(PROPERTY_FORMAT, name, type, label, placeholder, defaultValue, order));
                }
            }
            currClazz = currClazz.getSuperclass();
        } while(currClazz!=null && CloudResourceConfig.class!=currClazz);
        return String.format(BODY_FORMAT, object.getClass().getName(), title, propertiesBuilder.toString());
    }

    @ResponseBody
    @RequestMapping(value ="/structure", produces="application/json;charset=UTF-8")
    public TreeNode getStructure() throws Exception {
        String rootPath = "/"+CloudConfigCommon.NAMESPACE;
        TreeNode rootNode = new TreeNode(CloudConfigCommon.NAMESPACE, rootPath);
        Object checkExist = zkClient.checkExists().forPath(rootPath);
        if(checkExist==null) {
            throw new IllegalAccessException(rootPath+" is not exists.");
        }
        buildStructure(rootNode);
        return rootNode;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value ="/structure", method = RequestMethod.POST,
            consumes = APPLICATION_JSON_UTF8_VALUE, produces = TEXT_PLAIN_VALUE)
    public String addNode(@RequestBody Map<String, String> body) throws Exception {
        String path = body.get("path");
        String  value = body.get("value");
        String actualPath = path+"/"+value;
        Object checkExist = zkClient.checkExists().forPath(actualPath);
        if(checkExist==null) {
            zkClient.create().creatingParentsIfNeeded().forPath(actualPath, "".getBytes());
        }
        return "OK";
    }

    @RequestMapping(value = "/structure", method = RequestMethod.DELETE, produces = TEXT_PLAIN_VALUE)
    public String removeNode(@RequestParam(value="path", required=true)String path) throws Exception {
        Object exists = zkClient.checkExists().forPath(path);
        if(exists!=null) {
            zkClient.delete().forPath(path);
        }
        return "OK";
    }

    private void buildStructure(TreeNode parent) throws Exception {
        List<String> children = zkClient.getChildren().forPath(parent.getPath());
        if(children!=null && children.size()>0) {
            TreeNode settingNode = new TreeNode(".settings", parent.getPath());
            parent.addChild(settingNode);

            for(String child : children) {
                String childPath = parent.getPath()+"/"+child;
                TreeNode childNode = new TreeNode(child, childPath);
                parent.addChild(childNode);
                buildStructure(childNode);
            }
        }
    }

    private Properties loadProperties(String path, int depth) throws Exception {
        ZkPath zkPath = ZkPath.create(path);
        Object checkExist = zkClient.checkExists().forPath(path);
        if(checkExist==null) {
            throw new IllegalArgumentException("Unknown path "+path);
        }
        Properties properties = new Properties();
        ZkPath currPath = zkPath;
        while (currPath!=null && depth-- > 0) {
            String value = new String(zkClient.getData().forPath(currPath.toString()), "UTF-8");
            if(!value.isEmpty()) {
                jsonFlattenConverter.flatten(value, properties);
            }
            currPath = currPath.getParent();
        }
        return properties;
    }

    @RequestMapping(value ="/data", produces=APPLICATION_JSON_UTF8_VALUE)
    public String getData(@RequestParam(value="path", required=true)String path,
                          @RequestParam(value="name", required=true)String name) throws Exception {
        boolean isSettingsFile = ".settings".equals(name);
        Properties properties = isSettingsFile || isUnderPropertiesFolder(path) ?
                loadProperties(path, 1) : loadProperties(path, 5);
        String jsonValue = mapper.writeValueAsString(properties);
        String className = properties.getProperty("__type__");
        if(!isSettingsFile && className!=null) {
            try {
                Class<?> clazz = Class.forName(className);
                Object objectValue = mapper.readValue(jsonValue, clazz);
                return extractResourceConfigMeta((CloudResourceConfig)objectValue);
            } catch (Exception e) {
                return jsonValue;
            }
        }
        return jsonValue;
    }

    public static boolean isUnderPropertiesFolder(String path) {
        return path.startsWith("/"+CloudConfigCommon.PROPERTY_ROOT);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value ="/data", method = RequestMethod.POST,
            consumes = APPLICATION_JSON_UTF8_VALUE, produces = TEXT_PLAIN_VALUE)
    public String saveData(@RequestParam(value="path", required=true)String path,
                           @RequestBody String data) throws Exception {
        if(zkClient.checkExists().forPath(path)!=null) {
            zkClient.setData().forPath(path, data.getBytes());
        }
        return "OK";
    }

//    @ExceptionHandler(DataAccessException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public String handleDataAccessException(DataAccessException ex) {
//        // Do some stuff
//        return "errorView";
//    }
}
