package lpmaker;

import lpmaker.graphs.AspenTree;
import lpmaker.graphs.F10;
import lpmaker.graphs.FatTreeSigcomm;

import java.io.IOException;

public class TestMulticastTree {

    public static void main(String args[]) throws IOException {
        int k = 16;
        boolean NODE_FAILURE = true;

        int topo = Integer.parseInt(args[0]);
        int trafficMode = Integer.parseInt(args[1]);
        int failureMode = Integer.parseInt(args[2]);
        int failCount = Integer.parseInt(args[3]);
        int trial = Integer.parseInt(args[4]);
        int para = 0;
        String traffic = new String();
        if (trafficMode == 0) {
            para = k * k * k / 4;
            traffic = "perm";
        } else if (trafficMode == 2) {
            //make it all2one
            para = 0;
            traffic = "hotspot";
        } else if (trafficMode == 11) {
            para = k * k / 4;
            traffic = "stride";
        } else if (trafficMode == 15) {
            //make it 2pod to 2pod
            para = k * k / 4;
            traffic = "m2m";
        }

        System.out.println("Topo:" + topo + " failureMode:" + failureMode + " failCount:" + failCount + " trial:" + trial);
        if (topo == 0) {
            FatTreeSigcomm fts = new FatTreeSigcomm(k, failureMode, failCount, 0, false);
            fts.PrintGraphforMCFFairCondensedAverage("fattree_k" + k + "_traffic_" + traffic +
                    "_failMode" + failureMode + "_failureCount" + failCount + "_" + trial + ".lp", trafficMode, para);
        }
    }
}


