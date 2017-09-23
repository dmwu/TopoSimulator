
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class FlatTreeOnClos extends Graph{
	
	int m0, n0, r0;
	int m1, n1, r1;
	int m, n;
	
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

    int hostPerEdge;
	int hostPerAggr;
	int hostPerCore;
	
	int ratio;
	int coreGroupSize;

	public FlatTreeOnClos(int _m0, int _n0, int _r0, int _m1, int _n1, int _r1, int _m, int _n, double fail_rate){
		super(2*_r0 + 2*_m0*_r1 + _m0*_m1);
		
		m0 = _m0;
		n0 = _n0;
		r0 = _r0;
		m1 = _m1;
		n1 = _n1;
		r1 = _r1;

		m = _m;
		n = _n;
		
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

	    hostPerEdge = edgeInPort - m - n;
		hostPerAggr = n * edgeSwitchNum / aggrSwitchNum;
		hostPerCore = m * edgeSwitchNum / coreSwitchNum;
		
		ratio = edgeSwitchPerPod/aggrSwitchPerPod;
		coreGroupSize = aggrOutPort/ratio;

		System.out.println(" ^^^^^^^^^^^^^^^^^^^^ ");
		populateAdjacencyList();
		name = "flattree_on_clos";
		failLinks(fail_rate);
	}

	// +++++++++++ THIS CONSTRUCTION ROUTINE FOR VANILLA FAT TREE +++++++++++++++++++++++++++++++
	private void populateAdjacencyList(){
		//layout
		
		//0-(K*K/2) lower layer close to hosts 
		//(K*K/2) - K*K middle layer
		//K*K - 5/4*K*K core layer

		setUpFixWeight(0);
		for (int i = 0; i < edgeSwitchNum; i++) {
			for (int j = 0; j < hostPerEdge; j++) {
				weightEachNode[getNodeIndex("E"+i)]++;
				totalWeight++;
				//System.out.println("E"+i + " <--> H"+hostIndex);
			}			
		}
		for (int i = 0; i < aggrSwitchNum; i++) {
			for (int j = 0; j < hostPerAggr; j++) {
				weightEachNode[getNodeIndex("A"+i)]++;
				totalWeight++;
				//System.out.println("A"+i + " <--> H"+hostIndex);
			}			
		}
		for (int i = 0; i < coreSwitchNum; i++) {
			for (int j = 0; j < hostPerCore; j++) {
				weightEachNode[getNodeIndex("C"+i)]++;
				totalWeight++;
				//System.out.println("C"+i + " <--> H"+hostIndex);
			}			
		}

		for (int i = 0; i < podNum; i++) {
			for (int j = 0; j < edgeSwitchPerPod; j++) {
				int edgeSwitchIndex = i * edgeSwitchPerPod + j;
				for (int t = 0; t < aggrSwitchPerPod; t++) {
					int aggrSwitchIndex = i * aggrSwitchPerPod + t;
					
					addBidirNeighbor(getNodeIndex("A"+aggrSwitchIndex), getNodeIndex("E"+edgeSwitchIndex));
					//System.out.println("A"+aggrSwitchIndex + " <--> E"+edgeSwitchIndex);
				}
			}
			
			// wiring pattern 1
			if (m > 0 && coreGroupSize % m != 0) {
				for (int j = 0; j < edgeSwitchPerPod; j++) {
					int edgeSwitchIndex = i * edgeSwitchPerPod + j;
					for (int t = 0; t < n; t++) {
						int coreIndexInGroup = (m * i + m + t) % coreGroupSize;
						int coreSwitchIndex = coreGroupSize * j + coreIndexInGroup;
						addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("E"+edgeSwitchIndex));
						//System.out.println("C"+coreSwitchIndex + " <--> E"+edgeSwitchIndex);
					}
					
					int aggrSwitchIndex = edgeSwitchIndex / ratio;
					for (int t = 0; t < (coreGroupSize - m - n); t++) {
						int coreIndexInGroup = (m * i + m + n + t) % coreGroupSize;
						int coreSwitchIndex = coreGroupSize * j + coreIndexInGroup;
						addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("A"+aggrSwitchIndex));
						//System.out.println("C"+coreSwitchIndex + " <--> A"+aggrSwitchIndex);
					}
				}
			}
			// wiring pattern 2
			else {
				for (int j = 0; j < edgeSwitchPerPod; j++) {
					int edgeSwitchIndex = i * edgeSwitchPerPod + j;
					for (int t = 0; t < n; t++) {
						int coreIndexInGroup = (i + m + t) % coreGroupSize;
						int coreSwitchIndex = coreGroupSize * j + coreIndexInGroup;
						addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("E"+edgeSwitchIndex));
						//System.out.println("C"+coreSwitchIndex + " <--> E"+edgeSwitchIndex);
					}
					
					int aggrSwitchIndex = edgeSwitchIndex / ratio;
					for (int t = 0; t < (coreGroupSize - m - n); t++) {
						int coreIndexInGroup = (i + m + n + t) % coreGroupSize;
						int coreSwitchIndex = coreGroupSize * j + coreIndexInGroup;
						addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("A"+aggrSwitchIndex));
						//System.out.println("C"+coreSwitchIndex + " <--> A"+aggrSwitchIndex);
					}
				}
			}
			
			int oneSideNum = edgeSwitchPerPod/2;
			
			for (int j = 0; j < m; j++) {
				for (int t = 0; t < oneSideNum; t++) {
					int srcIndex = i * edgeSwitchPerPod + t;
					int srcAggrIndex = srcIndex / ratio;
					int destOppositeIndex = (srcIndex + j) % oneSideNum;
					int destInPodIndex = edgeSwitchPerPod - 1 - destOppositeIndex;
					int destPodIndex = (i - 1 + podNum) % podNum;
					int destIndex = destPodIndex * edgeSwitchPerPod + destInPodIndex;
					int destAggrIndex = destIndex / ratio;
					
					if (j % 2 == 0) {
						addBidirNeighbor(getNodeIndex("E"+srcIndex), getNodeIndex("E"+destIndex));
						//System.out.println("E"+srcIndex + " <--> E"+destIndex);
						addBidirNeighbor(getNodeIndex("A"+srcAggrIndex), getNodeIndex("A"+destAggrIndex));
					}
					else {
						addBidirNeighbor(getNodeIndex("E"+srcIndex), getNodeIndex("A"+destAggrIndex));
						//System.out.println("E"+srcIndex + " <--> A"+destIndex);
						addBidirNeighbor(getNodeIndex("A"+srcAggrIndex), getNodeIndex("E"+destIndex));
					}
				}			
			}
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
		int accumuHostPerCore = (hostNum-hostPerEdge*edgeSwitchNum-hostPerAggr*aggrSwitchNum)/coreSwitchNum;

		if (i < edgeSwitchNum*hostPerEdge) {
			int edgeIndex = i / hostPerEdge;
			return getNodeIndex("E"+edgeIndex);
		}
		else if (i < edgeSwitchNum*hostPerEdge+aggrSwitchNum*hostPerAggr) {
			int aggrIndex = (i-edgeSwitchNum*hostPerEdge)/hostPerAggr;
			return getNodeIndex("A"+aggrIndex);
		}
		else {
			int coreIndex = (i-edgeSwitchNum*hostPerEdge-aggrSwitchNum*hostPerAggr)/accumuHostPerCore;
			return getNodeIndex("C"+coreIndex);
		}
	}
}
