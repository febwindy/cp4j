package com.cp4j;

import java.io.File;
import java.net.URL;
import java.util.Collections;

/**
 * Created by liwenhe on 2016/7/19.
 * @author 李文禾
 */
public class PackageTree {

    private final static char PATH_REPLACE_COMMA = ',';

    private final ClassLoader classLoader;

    public PackageTree() {
        this.classLoader = getDefaultClassLoader();
    }

    public PackageTree(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public PackageNode createNode() {
        // 得到classpath路径
        URL classLoaderURL = this.classLoader.getResource("");
        File file = new File(classLoaderURL.getFile());
        PackageNode root = new PackageNode.Builder().build();
        addNode(file, root);
        return root;
    }

    protected void addNode(File file, final PackageNode node) {
        String[] paths = resolvePath(file.getPath());
        PackageNode tmpNode = node;
        for (String path : paths) {
            int i = 0;
            char[] chArr = path.toLowerCase().toCharArray();
            while (i < chArr.length - 1) {
                int diff = chArr[i] - 'a';
                if (null == tmpNode.getChildNodes().get(diff)) {
                    PackageNode newNode = new PackageNode.Builder().addState(PackageNodeState.UNCOMPLETE)
                                                                   .addWord(path.substring(0, i))
                                                                   .build();
                    tmpNode.getChildNodes().put(diff, newNode);
                }
                tmpNode = tmpNode.getChildNodes().get(diff);
                i ++;
            }

            // 添加最后完整包名的节点
            if (i == (chArr.length - 1)) {
                int diff = chArr[i] - 'a';
                File[] childFiles = file.listFiles();
                for (File childFile : childFiles) {
                    if (!childFile.isDirectory()) {
                        PackageNode newNode = new PackageNode.Builder().addState(PackageNodeState.COMPLETE)
                                .addWord(path)
                                .addFile(childFile.getName())
                                .build();
                        tmpNode.getChildNodes().put(diff, newNode);
                    }
                }
                i ++;
            }

            // 处理目录结构的节点
            if (i == chArr.length) {
                File[] childFiles = file.listFiles();
                for (File childFile : childFiles) {
                    if (childFile.isDirectory()) {
                        addNode(childFile, node);
                    } else {
                        tmpNode.setFiles(Collections.singleton(file.getName()));
                    }
                }
            }
        }
    }

    protected String[] resolvePath(String path) {
        // 得到classpath路径
        URL classLoaderURL = this.classLoader.getResource("");
        File file = new File(classLoaderURL.getFile());
        String classpath = file.getPath();
        // 截取路径
        String processPath = (classpath.length() != path.length()) ? path.substring(classpath.length()) : "";
        // 路径转换char[]
        char[] pathCHARS = processPath.toCharArray();

        // 对路径进行处理
        for (int i = 0; i < pathCHARS.length; i++) {
            if ((pathCHARS[i] < 'a' || pathCHARS[i] > 'z')
                    && (pathCHARS[i] < 'A' || pathCHARS[i] > 'Z')) {
                pathCHARS[i] = PATH_REPLACE_COMMA;
            }
        }

        // 进行split
        return String.valueOf(pathCHARS).split(String.valueOf(PATH_REPLACE_COMMA));
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

}
