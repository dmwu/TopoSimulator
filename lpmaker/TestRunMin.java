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
        int[] myIntArray = {0, 1, 2, 3, 5, 10, 20};
        for (int i: myIntArray) {
            for (int j = 0; j< 6; j++) {
                FatTreeSigcomm fts = new FatTreeSigcomm(24, i);
                fts.PrintGraphforMCFFairCondensed("fattree24_" + i +"_"+j+".lp", 1, 0);
                F10 f10 = new F10(24, i);
                f10.PrintGraphforMCFFairCondensed("ften24_" + i + "_"+ j + ".lp", 1, 0);
                AspenTree asp = new AspenTree(24, 4, 12, i);
                asp.PrintGraphforMCFFairCondensed("aspen24_" + i +"_" + j + ".lp", 1, 0);
            }

        }


    }
}