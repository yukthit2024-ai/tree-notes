package com.vypeensoft.treenode;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String id;
    private String title;
    private String content;
    private List<TreeNode> children;
    private long createdDate;
    private long updatedDate;

    public TreeNode() {
        this.children = new ArrayList<>();
    }

    public TreeNode(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.children = new ArrayList<>();
        long now = System.currentTimeMillis();
        this.createdDate = now;
        this.updatedDate = now;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedDate = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedDate = System.currentTimeMillis();
    }

    public List<TreeNode> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public void addChild(TreeNode child) {
        getChildren().add(child);
        this.updatedDate = System.currentTimeMillis();
    }

    public void removeChild(TreeNode child) {
        getChildren().remove(child);
        this.updatedDate = System.currentTimeMillis();
    }
}

