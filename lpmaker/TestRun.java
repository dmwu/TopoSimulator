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
        //FatTreeSigcomm fts = new FatTreeSigcomm(4);
        // fts.PrintGraphforMCFFairCondensed("fattreeLP.txt",1,0);
        F10 f10 = new F10(8);
        f10.PrintGraphforMCFFairCondensed("F10Model_k=8.lp",1,0);
        AspenTree asp = new AspenTree(8, 4, 4);
        asp.PrintGraphforMCFFairCondensed("AspenTree_k=8.lp",1,0);
    }
}
