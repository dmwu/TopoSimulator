package lpmaker;

import lpmaker.graphs.AspenTree;
import lpmaker.graphs.F10;
import lpmaker.graphs.FatTreeSigcomm;

import java.io.IOException;

/**
 * Created by wdm on 9/22/17.
 * For aspen trees:   (k,n,d,S) ={(8, 4, 4, 32), (16, 4, 8, 128), (24, 4, 12, 288), (32, 4, 16, 512)}
 */

public class TestRunAvg {
    public static void main(String args[]) throws IOException {
        //int[] myIntArray = {0, 1, 2, 3, 5, 10}
        int topo = Integer.parseInt(args[0]);
        int k = 24;
        int failCount = Integer.parseInt(args[1]);
        int trial = Integer.parseInt(args[2]);
        System.out.println("Topo:"+topo+" "+"failCount:"+failCount+" "+"trial:"+trial);
        if(topo == 1) {
            FatTreeSigcomm fts = new FatTreeSigcomm(k, failCount);
            fts.PrintGraphforMCFFairCondensedAverage("fattree_k" + k + "_"+failCount + "_" + trial + ".lp", 1, 0);
        }else if(topo == 2){
            F10 f10 = new F10(k, failCount);
            f10.PrintGraphforMCFFairCondensedAverage("ften_k" + k + "_"+failCount + "_" + trial + ".lp", 1, 0);
        }else{
                AspenTree asp = new AspenTree(k, 4, 12, failCount);
                asp.PrintGraphforMCFFairCondensedAverage("aspen_k" + k + "_" + failCount + "_" + trial + ".lp", 1, 0);
            }

        }
}
