package com.liws.utilities.distributedlock;

import com.sun.tools.javac.util.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.Collections;
import java.util.List;

/**
 * Created by liweisheng on 16/9/8.
 */
class PathUtil {
    public static void createPathIfNotExist(ZooKeeper zkClient, String path) throws Exception{
        String[] splitedPath = splitPath(path);

        String prefix = "/";

        for(String p : splitedPath){
            prefix += p;
            if(zkClient.exists(prefix,false) == null){
                zkClient.create(prefix,null,null, CreateMode.PERSISTENT);
            }
        }
    }

    public static String createEphermalSeqNode(ZooKeeper zkClient, String path, String name) throws Exception{
        String lockname = path + "/" + name;
        return zkClient.create(lockname,null,null,CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public static List<String> sortSeqentialNodes(List<String> childrenNodes){
        Collections.sort(childrenNodes, new SequentialNodeComparator());
        return childrenNodes;
    }

    public static String getSeqNodeBeforeCurrent(List<String> childrenNodes, String currentNode){
        int currentNodeIndex = childrenNodes.indexOf(currentNode);
        return childrenNodes.get(currentNodeIndex-1);
    }

    private static String[] splitPath(String path) throws Exception{
        validatePath(path);
        return path.split("/");
    }

    private static void validatePath(String path) throws Exception{
        if(path == null || path.isEmpty() || !path.startsWith("/")){
            throw  new Exception("invalid lock path:" + path);
        }
    }
}
