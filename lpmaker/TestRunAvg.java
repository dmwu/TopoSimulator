package lpmaker;

import lpmaker.graphs.AspenTree;
import lpmaker.graphs.F10;
import lpmaker.graphs.FatTreeSigcomm;

import java.io.IOException;

/**
 * Created by wdm on 9/22/17.
 * For aspen trees:   (k,n,d,S) ={(8, 4, 4, 32), (16, 4, 8, 128), (24, 4, 12, 288), (32, 4, 16, 512)}
 * trafficMode: 0 -> permutation; 11-> podStride; 13-> hotspot; 15 ->many2many;
 */

public class TestRunAvg {
    public static void main(String args[]) throws IOException {
        int k = 16;
        int topo = Integer.parseInt(args[0]);
        int trafficMode = Integer.parseInt(args[1]);
        int failurePos = Integer.parseInt(args[2]);
        int failCount = Integer.parseInt(args[3]);
        int trial = Integer.parseInt(args[4]);
        int para = 0;
        String traffic = new String();
        if(trafficMode == 0){
            para = k*k*k/4;
            traffic="perm";
        }else if(trafficMode == 11){
            para = k*k/4;
            traffic="stride";
        }else if(trafficMode == 2){ // maybe we can modify to all2one
            para = 100;
            traffic = "hotspot";
        }else if(trafficMode == 15){
            para = 50;
            traffic = "m2m";
        }
        System.out.println("Topo:"+topo+" position:" +failurePos+" failCount:"+failCount+ " trial:"+trial);
        if(topo == 1) {
            FatTreeSigcomm fts = new FatTreeSigcomm(k, failurePos,failCount);
            fts.PrintGraphforMCFFairCondensedAverage("fattree_avg_k" + k +"_traffic_"+traffic+
                    "_linkType"+failurePos+"_failureCount"+failCount + "_" + trial + ".lp", trafficMode,para);
        }else if(topo == 2){
            F10 f10 = new F10(k, failurePos, failCount);
            f10.PrintGraphforMCFFairCondensedAverage("ften_avg_k" + k +"_traffic_"+traffic+
                    "_linkType"+failurePos+"_failureCount"+failCount + "_" + trial + ".lp", trafficMode,para);
        }else{
                AspenTree asp = new AspenTree(k, 4, 8, failurePos, failCount);
                asp.PrintGraphforMCFFairCondensedAverage("aspen_avg_k" + k +"_traffic_"+traffic+
                        "_linkType"+failurePos+"_failureCount"+failCount + "_" + trial + ".lp", trafficMode,para);
            }
        }
}
