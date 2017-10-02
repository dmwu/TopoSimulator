package lpmaker;

import lpmaker.graphs.AspenTree;
import lpmaker.graphs.F10;
import lpmaker.graphs.FatTreeSigcomm;

import java.io.IOException;

/**
 * Created by wdm on 10/2/17.
 */
public class TestRunAvg {
    public static void main(String args[]) throws IOException {
        int[] myIntArray = {0,5,10,20};
        for (int i : myIntArray) {
            for (int j = 0; j< 5; j++) {
                FatTreeSigcomm fts = new FatTreeSigcomm(16, i);
                fts.PrintGraphforMCFFairCondensedAverage("fattree16_" + i +"_"+j+".lp", 1, 0);
                F10 f10 = new F10(16, i);
                f10.PrintGraphforMCFFairCondensedAverage("ften16_" + i + "_"+ j + ".lp", 1, 0);
                AspenTree asp = new AspenTree(16, 4, 8, i);
                asp.PrintGraphforMCFFairCondensedAverage("aspen16_" + i +"_" + j + ".lp", 1, 0);
            }

        }


    }
}
