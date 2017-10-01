package lpmaker;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import lpmaker.graphs.*;

/**
 * Created by wdm on 9/22/17.
 * For aspen trees:   (k,n,d,S) ={(8, 4, 4, 32), (16, 4, 8, 128), (24, 4, 12, 288), (32, 4, 16, 512)}
 */
public class TestRun {
    public static void main(String args[]) throws IOException {

        for (int i = 0; i <= 20; i+=5) {
            for (int j = 0; j< 4; j++) {
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
