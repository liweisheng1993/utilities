package com.liws.utilities.distributedlock;

import java.util.Comparator;

/**
 * Created by liweisheng on 16/9/9.
 */
public class SequentialNodeComparator implements Comparator<String> {
    @Override
    public int compare(String node1, String node2) {
        int lastSlideBarIndex1 = node1.lastIndexOf("_");
        int lastSlideBarIndex2 = node2.lastIndexOf("_");

        Long seqNum1 = Long.valueOf(node1.substring(lastSlideBarIndex1));
        Long seqNum2 = Long.valueOf(node2.substring(lastSlideBarIndex2));
        return seqNum1 < seqNum2 ? -1 :(seqNum1 == seqNum2 ? 0 : 1);
    }
}
