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
        FatTreeSigcomm fts = new FatTreeSigcomm(4);
        fts.PrintGraphforMCFFairCondensed("fattreeLP.txt",1,0);

    }
}
