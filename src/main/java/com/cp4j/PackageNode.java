package com.cp4j;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    private Map<Integer, PackageNode> childNodes;

    private PackageNodeState state;

    protected PackageNode(String word, Set<String> files, Map<Integer, PackageNode> childNodes, PackageNodeState state) {
        this.word = word;
        this.files = files;
        this.childNodes = childNodes;
        this.state = state;
    }

    public String getWord() {
        return word;
    }

    public Set<String> getFiles() {
        return new CopyOnWriteArraySet<String>(this.files);
    }

    public Map<Integer, PackageNode> getChildNodes() {
        return new ConcurrentHashMap<Integer, PackageNode>(this.childNodes);
    }

    public PackageNodeState getState() {
        return state;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }

    public void setChildNodes(Map<Integer, PackageNode> childNodes) {
        this.childNodes = childNodes;
    }

    public void setState(PackageNodeState state) {
        this.state = state;
    }

    public static final class Builder {

        private String word;

        private Set<String> files;

        private Map<Integer, PackageNode> childNodes;

        private PackageNodeState state;

        public Builder() {
            this.files = new LinkedHashSet<String>();
            this.childNodes = new LinkedHashMap<Integer, PackageNode>();
        }

        public PackageNode build() {
            return new PackageNode(this.word, this.files,this.childNodes, this.state);
        }

        public Builder addWord(String word) {
            this.word = word;
            return this;
        }

        public Builder addFile(String file) {
            this.files.add(file);
            return this;
        }

        public Builder addChildNode(Integer key, PackageNode childNode) {
            this.childNodes.put(key, childNode);
            return this;
        }

        public Builder addState(PackageNodeState state) {
            this.state = state;
            return this;
        }

    }
}
