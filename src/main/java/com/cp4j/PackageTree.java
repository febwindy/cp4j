package com.cp4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Created by liwenhe on 2016/7/19.
 * @author 李文禾
 */
public class PackageTree {

    private final static Logger LOGGER = LoggerFactory.getLogger(PackageTree.class);

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

    /**
     * 构建PackageNode并通过addNode方法初始化
     * @return
     */
    protected PackageNode build() {
        LOGGER.debug("初始化并构建trie树");
        URL classLoaderURL = this.classLoader.getResource("");
        File file = new File(classLoaderURL.getFile());
        PackageNode root = new PackageNode();
        addNode(file, root);
        LOGGER.debug("构建trie树完成");
        return root.getChildNodes().get("");
    }

    /**
     * 解析资源，并构建trie树
     * @param file
     * @param node
     */
    protected void addNode(File file, PackageNode node) {
        String[] paths = resolvePath(file.getPath());
        PackageNode tmpNode = node;
        for (String path : paths) {
            if (null != tmpNode.getChildNodes() && null == tmpNode.getChildNodes().get(path)) {
                LOGGER.debug("添加子节点[key={}]的数据", path);
                PackageNode newNode = new PackageNode();
                newNode.setWord(path);
                Map<String, PackageNode> newNodeMap = new HashMap<String, PackageNode>();
                newNodeMap.put(path, newNode);
                tmpNode.setChildNodes(newNodeMap);

                // 处理目录结构和文件资源的节点
                LOGGER.debug("处理文件[{}]下的子文件", file.getName());
                File[] childFiles = file.listFiles();
                Set<String> fileSet = new HashSet<String>();
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        LOGGER.debug("子文件[{}]属于包,递归进行处理", childFile.getName());
                        addNode(childFile, node);
                    } else {
                        LOGGER.debug("添加文件[{}]", childFile.getName());
                        fileSet.add(childFile.getName());
                    }
                }
                newNode.setFiles(fileSet);
            }
            tmpNode = tmpNode.getChildNodes().get(path);
        }
    }

    /**
     * 解析路径，并通过过滤进行处理
     * @param path
     * @return
     */
    protected String[] resolvePath(String path) {
        LOGGER.debug("解析路径");
        // 得到classpath路径
        URL classLoaderURL = this.classLoader.getResource("");
        File file = new File(classLoaderURL.getFile());
        String classpath = file.getPath();
        // 截取路径
        String processPath = (classpath.length() != path.length()) ? path.substring(classpath.length()) : "";
        // 通过过滤器处理路径
        LOGGER.debug("通过路径过滤器处理路径");
        for (Builder.IPathFilter filter : pathFilters) {
            processPath = filter.accept(processPath);
        }
        // 进行split
        return processPath.split(String.valueOf(PATH_REPLACE_SEPARATOR));
    }

    /**
     * 加载classloader
     * @return
     */
    protected ClassLoader getDefaultClassLoader() {
        LOGGER.debug("加载默认的classloader");
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            LOGGER.warn(ex.getMessage(), ex);
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
                    LOGGER.warn(ex.getMessage(), ex);
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    /**
     * 构建器，通过这个构建PackageTree
     */
    public static class Builder {

        private final Logger LOGGER = LoggerFactory.getLogger(PackageTree.class);

        private final PackageTree packageTree;

        public Builder() {
            this.packageTree = new PackageTree();
            this.packageTree.pathFilters.add(new DefaultPathFilter());
        }

        public PackageNode build() {
            return this.packageTree.build();
        }

        /**
         * 添加过滤器
         * @param filter
         * @return
         */
        public Builder addFilter(IPathFilter filter) {
            LOGGER.debug("添加过滤器[{}]", filter.getClass());
            this.packageTree.pathFilters.add(filter);
            return this;
        }

        /**
         * 路径过滤器接口
         */
        public interface IPathFilter {
            String accept(String path);
        }

        /**
         * 默认的过滤器
         */
        public class DefaultPathFilter implements IPathFilter {

            private final Logger LOGGER = LoggerFactory.getLogger(PackageTree.class);

            public String accept(String path) {
                LOGGER.debug("在过滤器中[{}]处理路径[path={}]", getClass(), path);
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
