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
        for (int i = 0; i <= 20; i+=10) {
            FatTreeSigcomm fts = new FatTreeSigcomm(16, i*0.01 );
            fts.PrintGraphforMCFFairCondensed("fattreek_"+i*0.01+".lp", 1, 0);
            F10 f10 = new F10(16, i*0.01 );
            f10.PrintGraphforMCFFairCondensed("f10k_"+i*0.01+".lp", 1, 0);
            AspenTree asp = new AspenTree(16, 4, 8, i*0.01 );
            asp.PrintGraphforMCFFairCondensed("aspenk_"+i*0.01+".lp", 1, 0);
        }
    }
}
