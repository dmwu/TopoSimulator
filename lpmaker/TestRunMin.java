package lpmaker;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import lpmaker.graphs.*;

/**
 * Created by wdm on 9/22/17.
 * For aspen trees:   (k,n,d,S) ={(8, 4, 4, 32), (16, 4, 8, 128), (24, 4, 12, 288), (32, 4, 16, 512)}
 * parameters: topology: 1->fat-tree;  2->f10; 3-> aspentree
 *              failureCount: count of failed links
 *             failurePos: 0-> global random;  1->edge2agg links; 2-> core links
 */
public class TestRunMin {
    public static void main(String args[]) throws IOException {
        int k = 24;
        int topo = Integer.parseInt(args[0]);
        int failurePos = Integer.parseInt(args[1]);
        int failCount = Integer.parseInt(args[2]);
        int trial = Integer.parseInt(args[3]);
        System.out.println("Topo:"+topo+" position:" +failurePos+" failCount:"+failCount+ " trial:"+trial);
        if(topo == 1) {
            FatTreeSigcomm fts = new FatTreeSigcomm(k , failurePos, failCount);
            fts.PrintGraphforMCFFairCondensed("fattree_min_k" + k + "_linkType"+failurePos+"_failureCount"+failCount + "_" + trial + ".lp", 1, 0);
        }else if(topo == 2){
            F10 f10 = new F10(k, failurePos, failCount);
            f10.PrintGraphforMCFFairCondensed("ften_min_k" + k + "_linkType"+failurePos+"_failureCount"+failCount + "_" + trial + ".lp", 1, 0);
        }else{
            AspenTree asp = new AspenTree(k, 4, 12, failurePos,failCount);
            asp.PrintGraphforMCFFairCondensed("aspen_min_k" + k + "_linkType"+failurePos+"_failureCount"+failCount + "_" + trial + ".lp", 1, 0);
        }

    }
}