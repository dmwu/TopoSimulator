package lpmaker;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;

import lpmaker.graphs.*;

public class RunFlattreeClos {

	public static void main(String args[]) throws IOException {
		int m0 = Integer.parseInt(args[0]);
		int n0 = Integer.parseInt(args[1]);
		int r0 = Integer.parseInt(args[2]);
		int m1 = Integer.parseInt(args[3]);
		int n1 = Integer.parseInt(args[4]);
		int r1 = Integer.parseInt(args[5]);
		int m = Integer.parseInt(args[6]);
		int n = Integer.parseInt(args[7]);

		Graph mynet = new FlatTreeOnClos(m0, n0, r0, m1, n1, r1, m, n, 0);

		mynet.PrintGraphforMCFFairCondensed(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_0_min.lp", 0, 2*n0*r0);
		mynet.PrintGraphforMCFFairCondensedAverage(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_0_avg.lp", 0, 2*n0*r0);

		mynet.PrintGraphforMCFFairCondensed(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_1_min.lp", 11, n0*r0/r1);
		mynet.PrintGraphforMCFFairCondensedAverage(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_1_avg.lp", 11, n0*r0/r1);

		mynet.PrintGraphforMCFFairCondensed(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_2_min.lp", 11, 2*n0*r0/r1);
		mynet.PrintGraphforMCFFairCondensedAverage(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_2_avg.lp", 11, 2*n0*r0/r1);

		mynet.PrintGraphforMCFFairCondensed(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_3_min.lp", 13, 100);
		mynet.PrintGraphforMCFFairCondensedAverage(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_3_avg.lp", 13, 100);

		mynet.PrintGraphforMCFFairCondensed(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_4_min.lp", 15, 20);
		mynet.PrintGraphforMCFFairCondensedAverage(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_4_avg.lp", 15, 20);

		mynet.PrintGraphforMCFFairCondensed(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_5_min.lp", 14, n0*r0/r1);
		mynet.PrintGraphforMCFFairCondensedAverage(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_5_avg.lp", 14, n0*r0/r1);

		mynet.PrintGraphforMCFFairCondensed(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_6_min.lp", 16, n0*r0/r1);
		mynet.PrintGraphforMCFFairCondensedAverage(m0+"_"+n0+"_"+r0+"_"+m1+"_"+n1+"_"+r1+"_"+m+"_"+n + "_6_avg.lp", 16, n0*r0/r1);
	}
}
