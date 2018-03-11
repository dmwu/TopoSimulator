package lpmaker;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import lpmaker.graphs.*;

/**
 * Created by wdm on 9/22/17.
 * For aspen trees:   (k,n,d,S) ={(8, 4, 4, 32), (16, 4, 8, 128), (24, 4, 12, 288), (32, 4, 16, 512)}
 * parameters: topology: 0->fat-tree; 1->oneBackup; 2->twoBackups; 3->f10; 4-> aspentree
 * trafficMode: 0 -> permutation;  11-> podStride;  13-> hotspot; 15 ->many2many;
 * failureCount: count of failed links
 * failureMode: 0-random: 1->randomAgg; 2->randomCore;
 */
public class TestRunMin {
    public static void main(String args[]) throws IOException {
        int k = 4;
        boolean NODE_FAILURE = true;

        int topo = Integer.parseInt(args[0]);
        int trafficMode = Integer.parseInt(args[1]);
        int failureMode = Integer.parseInt(args[2]);
        int failCount = Integer.parseInt(args[3]);
        int trial = Integer.parseInt(args[4]);
        int para = 0;
        String traffic = new String();
        if(trafficMode == 0){
            para = k*k*k/4;
            traffic="perm";
        }else if(trafficMode == 2){
            //make it all2one
            para = 0;
            traffic = "hotspot";
        }else if(trafficMode == 11){
            para = k*k/4;
            traffic="stride";
        }else if(trafficMode == 15){
            //make it 2pod to 2pod
            para = k*k/4;
            traffic = "m2m";
        }

        System.out.println("Topo:"+topo+" failureMode:" +failureMode+" failCount:"+failCount+ " trial:"+trial);
        if(topo == 0) {
            FatTreeSigcomm fts = new FatTreeSigcomm(k, failureMode, failCount, 0, NODE_FAILURE);
            fts.PrintGraphforMCFFairCondensed("fattree_k" + k + "_traffic_" + traffic +
                    "_failMode" + failureMode + "_failureCount" + failCount + "_" + trial + ".lp", trafficMode, para);
        }else if(topo == 1) {
            FatTreeSigcomm fts = new FatTreeSigcomm(k , failureMode, failCount, 1, NODE_FAILURE);
            fts.PrintGraphforMCFFairCondensed("Backup1_k" + k +"_traffic_"+traffic+
                    "_failMode"+failureMode+"_failureCount"+failCount + "_" + trial + ".lp", trafficMode, para);
        }else if(topo == 2) {
            FatTreeSigcomm fts = new FatTreeSigcomm(k , failureMode, failCount, 2, NODE_FAILURE);
            fts.PrintGraphforMCFFairCondensed("Backup2_k" + k +"_traffic_"+traffic+
                    "_failMode"+failureMode+"_failureCount"+failCount + "_" + trial + ".lp", trafficMode,para);
        }else if(topo == 3){
            F10 f10 = new F10(k, failureMode, failCount, NODE_FAILURE);
            f10.PrintGraphforMCFFairCondensed("ften_k" + k +"_traffic_"+traffic+
                    "_failMode"+failureMode+"_failureCount"+failCount + "_" + trial + ".lp", trafficMode,para);
        }else{
            AspenTree asp = new AspenTree(k, 4, 8, failureMode, failCount, NODE_FAILURE);
            asp.PrintGraphforMCFFairCondensed("aspen_k" + k +"_traffic_"+traffic+
                    "_failMode"+failureMode+"_failureCount"+failCount + "_" + trial + ".lp", trafficMode,para);
        }
    }
}