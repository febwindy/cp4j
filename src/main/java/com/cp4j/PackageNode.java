package com.cp4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by liwenhe on 2016/7/19.
 * @author 李文禾
 */
public class PackageNode {

    private String word;

    private Set<String> files;

    private Map<String, PackageNode> childNodes;

    public PackageNode() {
    }

    public PackageNode(String word, Set<String> files, Map<String, PackageNode> childNodes) {
        this.word = word;
        this.files = files;
        this.childNodes = childNodes;
    }

    public String getWord() {
        return word;
    }

    public Set<String> getFiles() {
        return new CopyOnWriteArraySet<String>(this.files);
    }

    public Map<String, PackageNode> getChildNodes() {
        if (null != this.childNodes) {
            return new ConcurrentHashMap<String, PackageNode>(this.childNodes);
        } else {
            return new ConcurrentHashMap<String, PackageNode>(0);
        }
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }

    public void setChildNodes(Map<String, PackageNode> childNodes) {
        if (null == this.childNodes) {
            this.childNodes = childNodes;
        } else {
            this.childNodes.putAll(childNodes);
        }
    }

}
