package lpmaker;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import lpmaker.graphs.*;

/**
 * Created by wdm on 9/22/17.
 * For aspen trees:   (k,n,d,S) ={(8, 4, 4, 32), (16, 4, 8, 128), (24, 4, 12, 288), (32, 4, 16, 512)}
 */
public class TestRunMin {
    public static void main(String args[]) throws IOException {
        int[] myIntArray = {0, 1, 2, 3, 4, 5};
        for (int i: myIntArray) {
            for (int j = 0; j< 6; j++) {
                FatTreeSigcomm fts = new FatTreeSigcomm(16, i);
                fts.PrintGraphforMCFFairCondensed("fattree16_" + i +"_"+j+".lp", 1, 0);
                F10 f10 = new F10(16, i);
                f10.PrintGraphforMCFFairCondensed("ften16_" + i + "_"+ j + ".lp", 1, 0);
                AspenTree asp = new AspenTree(16, 4, 8, i);
                asp.PrintGraphforMCFFairCondensed("aspen16_" + i +"_" + j + ".lp", 1, 0);
            }

        }


    }
}