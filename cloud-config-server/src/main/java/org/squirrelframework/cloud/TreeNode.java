package org.squirrelframework.cloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by kailianghe on 11/27/15.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class TreeNode {

    private List<TreeNode> children;

    private String name;

    private String path;

    TreeNode(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    void addChild(TreeNode child) {
        if(name.charAt(0)=='.') {
            throw new IllegalArgumentException("cannot add child for property file");
        } else {
            if(children==null) {
                children = Lists.newArrayList();
            }
            children.add(child);
        }
    }

    void removeChild(TreeNode child) {
        if(children!=null) {
            children.remove(child);
        }
    }

    TreeNode findChildByName(String name) {
        if(children==null) return null;
        for(TreeNode child : children) {
            if(child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    public String getType() {
        return (children!=null && !children.isEmpty()) ? "folder" : "file";
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}