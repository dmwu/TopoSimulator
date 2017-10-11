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
        int[] myIntArray = {0, 1, 2, 3, 5, 10, 20};
        for (int i : myIntArray) {
            for (int j = 0; j< 6; j++) {
                FatTreeSigcomm fts = new FatTreeSigcomm(24, i);
                fts.PrintGraphforMCFFairCondensedAverage("fattree24_" + i +"_"+j+".lp", 1, 0);
                F10 f10 = new F10(24, i);
                f10.PrintGraphforMCFFairCondensedAverage("ften24_" + i + "_"+ j + ".lp", 1, 0);
                AspenTree asp = new AspenTree(24, 4, 12, i);
                asp.PrintGraphforMCFFairCondensedAverage("aspen24_" + i +"_" + j + ".lp", 1, 0);
            }

        }


    }
}
