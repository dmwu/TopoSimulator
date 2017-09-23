package lpmaker;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import lpmaker.graphs.*;

/**
 * Created by wdm on 9/22/17.
 */
public class TestRun {
    public static void main(String args[]) throws IOException {
        //FatTreeSigcomm fts = new FatTreeSigcomm(4);
        // fts.PrintGraphforMCFFairCondensed("fattreeLP.txt",1,0);
        F10 f10 = new F10(4);
        f10.PrintGraphforMCFFairCondensed("F10Model.lp",1,0);
    }
}
