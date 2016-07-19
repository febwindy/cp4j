package com.cp4j;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Created by liwenhe on 2016/7/19.
 * @author 李文禾
 */
public class PackageTree {

    private final static char PATH_REPLACE_SEPARATOR = '/';

    private final ClassLoader classLoader;

    private final List<Builder.IPathFilter> pathFilters;

    protected PackageTree() {
        this.classLoader = getDefaultClassLoader();
        pathFilters = new ArrayList<Builder.IPathFilter>();
    }

    protected PackageTree(ClassLoader classLoader) {
        this.classLoader = classLoader;
        pathFilters = new ArrayList<Builder.IPathFilter>();
    }

    protected PackageNode build() {
        // 得到classpath路径
        URL classLoaderURL = this.classLoader.getResource("");
        File file = new File(classLoaderURL.getFile());
        PackageNode root = new PackageNode();
        addNode(file, root);
        return root.getChildNodes().get("");
    }

    protected void addNode(File file, PackageNode node) {
        String[] paths = resolvePath(file.getPath());
        PackageNode tmpNode = node;
        for (String path : paths) {
            if (null != tmpNode.getChildNodes() && null == tmpNode.getChildNodes().get(path)) {
                PackageNode newNode = new PackageNode();
                newNode.setState(PackageNodeState.COMPLETE);
                newNode.setWord(path);
                Map<String, PackageNode> newNodeMap = new HashMap<String, PackageNode>();
                newNodeMap.put(path, newNode);
                tmpNode.setChildNodes(newNodeMap);

                // 处理目录结构和文件资源的节点
                File[] childFiles = file.listFiles();
                Set<String> fileSet = new HashSet<String>();
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        addNode(childFile, node);
                    } else {
                        fileSet.add(childFile.getName());
                    }
                }
                newNode.setFiles(fileSet);
            }
            tmpNode = tmpNode.getChildNodes().get(path);
        }
    }

    protected String[] resolvePath(String path) {
        // 得到classpath路径
        URL classLoaderURL = this.classLoader.getResource("");
        File file = new File(classLoaderURL.getFile());
        String classpath = file.getPath();
        // 截取路径
        String processPath = (classpath.length() != path.length()) ? path.substring(classpath.length()) : "";
        // 通过过滤器处理路径
        for (Builder.IPathFilter filter : pathFilters) {
            processPath = filter.accept(processPath);
        }
        // 进行split
        return processPath.split(String.valueOf(PATH_REPLACE_SEPARATOR));
    }

    protected ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = PackageTree.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    public static class Builder {

        private final PackageTree packageTree;

        public Builder() {
            this.packageTree = new PackageTree();
            this.packageTree.pathFilters.add(new DefaultPathFilter());
        }

        public PackageNode build() {
            return this.packageTree.build();
        }

        public Builder addFilter(IPathFilter filter) {
            this.packageTree.pathFilters.add(filter);
            return this;
        }

        public interface IPathFilter {
            String accept(String path);
        }

        public class DefaultPathFilter implements IPathFilter {
            public String accept(String path) {
                // 路径转换char[]
                char[] pathCHARS = path.toCharArray();
                // 对路径进行处理
                for (int i = 0; i < pathCHARS.length; i++) {
                    if (pathCHARS[i] == '\\') {
                        pathCHARS[i] = PATH_REPLACE_SEPARATOR;
                    }
                }
                return String.valueOf(pathCHARS);
            }
        }
    }

}
