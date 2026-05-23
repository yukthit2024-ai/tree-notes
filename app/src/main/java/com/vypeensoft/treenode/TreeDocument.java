package com.vypeensoft.treenode;

import java.util.ArrayList;
import java.util.List;

public class TreeDocument {
    private String id;
    private String name;
    private List<TreeNode> rootNodes;

    public TreeDocument() {
        this.rootNodes = new ArrayList<>();
    }

    public TreeDocument(String id, String name) {
        this.id = id;
        this.name = name;
        this.rootNodes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TreeNode> getRootNodes() {
        if (rootNodes == null) {
            rootNodes = new ArrayList<>();
        }
        return rootNodes;
    }

    public void setRootNodes(List<TreeNode> rootNodes) {
        this.rootNodes = rootNodes;
    }
}

