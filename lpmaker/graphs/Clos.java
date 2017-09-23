
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class Clos extends Graph{
	
	int m0, n0, r0;
	int m1, n1, r1;
	
	int nodeNum;
	int hostNum;
    int podNum;
    int edgeSwitchPerPod;
    int aggrSwitchPerPod;
    int edgeSwitchNum;
    int aggrSwitchNum;
    int coreSwitchNum;
    int edgeInPort;
    int edgeOutPort;
    int aggrInPort;
    int aggrOutPort;
    int corePort;

	public Clos(int _m0, int _n0, int _r0, int _m1, int _n1, int _r1, double fail_rate){
		super(2*_r0 + 2*_m0*_r1 + _m0*_m1);
		System.out.println("total sw = " + (2*_r0 + 2*_m0*_r1 + _m0*_m1));

		m0 = _m0;
		n0 = _n0;
		r0 = _r0;
		m1 = _m1;
		n1 = _n1;
		r1 = _r1;
		
		edgeSwitchNum = 2*r0;
		edgeInPort = n0;
		edgeOutPort = m0;
		
		aggrSwitchNum = 2*m0*r1;
		aggrInPort = n1;
		aggrOutPort = m1;
		
		coreSwitchNum = m0*m1;
		corePort = 2*r1;
		
		hostNum = edgeSwitchNum * edgeInPort;

	    podNum = 2*r1;
	    edgeSwitchPerPod = edgeSwitchNum/podNum;
	    aggrSwitchPerPod = aggrSwitchNum/podNum;

	    nodeNum = hostNum + edgeSwitchNum + aggrSwitchNum + coreSwitchNum;

		System.out.println(" ^^^^^^^^^^^^^^^^^^^^ ");
		populateAdjacencyList();
		name = "clos";
		failLinks(fail_rate);
	}

	// +++++++++++ THIS CONSTRUCTION ROUTINE FOR VANILLA FAT TREE +++++++++++++++++++++++++++++++
	private void populateAdjacencyList(){
		//layout
		
		//0-(K*K/2) lower layer close to hosts 
		//(K*K/2) - K*K middle layer
		//K*K - 5/4*K*K core layer
		
		for (int i = 0; i < podNum; i++) {
			for (int j = 0; j < edgeSwitchPerPod; j++) {
				int edgeSwitchIndex = i * edgeSwitchPerPod + j;
				for (int t = 0; t < aggrSwitchPerPod; t++) {
					int aggrSwitchIndex = i * aggrSwitchPerPod + t;
					
					addBidirNeighbor(getNodeIndex("A"+aggrSwitchIndex), getNodeIndex("E"+edgeSwitchIndex));
					//System.out.println("A"+aggrSwitchIndex + " <--> E"+edgeSwitchIndex);
				}
			}
			
			for (int j = 0; j < aggrSwitchPerPod; j++) {
	    		int aggrSwitchIndex = i*aggrSwitchPerPod+j;
	    		
	    		for (int t = 0; t < aggrOutPort; t++) {
	    			int coreSwitchIndex = j*aggrOutPort+t;
	    			
	    			addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("A"+aggrSwitchIndex));	    			
	    			//System.out.println("C"+coreSwitchIndex + " <--> A"+aggrSwitchIndex);
	    		}
	    	}
		}		
		
		//set weights
		setUpFixWeight(0);
		for (int i = 0; i < edgeSwitchNum; i++) {
			weightEachNode[i] = edgeInPort;
			totalWeight += edgeInPort;			
		}
	}

	public int getNodeIndex (String nodeName) {
		String type = nodeName.substring(0,1);
		String strID = nodeName.substring(1, nodeName.length());
		int id = Integer.parseInt(strID);
		
		if (type.equals("E")) {
			return id;
		}
		else if (type.equals("A")) {
			return (id + edgeSwitchNum);
		}
		else {
			return (id + edgeSwitchNum + aggrSwitchNum);
		}
	}
	
	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		return i / weightEachNode[0];
	}
}
