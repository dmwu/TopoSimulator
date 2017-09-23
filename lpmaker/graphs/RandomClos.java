
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class RandomClos extends Graph{
	
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

    int[] hostSwithMap;

	public RandomClos(int _m0, int _n0, int _r0, int _m1, int _n1, int _r1, double fail_rate){
		super(2*_r0 + 2*_m0*_r1 + _m0*_m1);

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

	    hostSwithMap = new int[hostNum];

		System.out.println(" ^^^^^^^^^^^^^^^^^^^^ ");
		populateAdjacencyList();
		name = "flattree_on_clos";
		failLinks(fail_rate);
	}

	// +++++++++++ THIS CONSTRUCTION ROUTINE FOR VANILLA FAT TREE +++++++++++++++++++++++++++++++
	private void populateAdjacencyList(){
		//layout
		
		int edgeTotalPort = edgeInPort + edgeOutPort;
		int aggrTotalPort = aggrInPort + aggrOutPort;
		int coreTotalPort = corePort;
		
		int totalPortCount = edgeSwitchNum*edgeTotalPort + aggrSwitchNum*aggrTotalPort + coreSwitchNum*coreTotalPort;
		double ratio = (double)hostNum/(double)totalPortCount;
		
		int hostPerEdge = (int)(ratio*edgeTotalPort);
		int hostPerAggr = (int)(ratio*aggrTotalPort);
		int hostPerCore = (int)(ratio*coreTotalPort);

		int switchNum = edgeSwitchNum + aggrSwitchNum + coreSwitchNum;
		
		int[] remainPort = new int[switchNum];
	    ArrayList<Integer> remainSwitch = new ArrayList<Integer> ();

	    setUpFixWeight(0);

	    int assignedHostNum = 0;
	    
	    for (int i = 0; i < edgeSwitchNum; i++) {
	    	for (int j = 0; j < hostPerEdge; j++) {
	    		weightEachNode[i]++;
				totalWeight++;
	    		assignedHostNum++;
	    	}

	    	remainPort[i] = edgeTotalPort - hostPerEdge;
	    	remainSwitch.add(i);
	    }
	    for (int i = edgeSwitchNum; i < edgeSwitchNum + aggrSwitchNum; i++) {
	    	for (int j = 0; j < hostPerAggr; j++) {
	    		weightEachNode[i]++;
				totalWeight++;
	    		assignedHostNum++;
	    	}

	    	remainPort[i] = aggrTotalPort - hostPerAggr;
	    	remainSwitch.add(i);
	    }
	    for (int i = edgeSwitchNum + aggrSwitchNum; i < switchNum; i++) {
	    	for (int j = 0; j < hostPerCore; j++) {
	    		weightEachNode[i]++;
				totalWeight++;
	    		assignedHostNum++;
	    	}

	    	remainPort[i] = coreTotalPort - hostPerCore;
	    	remainSwitch.add(i);
	    }
	    
	    int switchIndex = 0;
	    while (assignedHostNum < hostNum) {
	    	weightEachNode[switchIndex]++;
			totalWeight++;
	    	remainPort[switchIndex]--;
	    	assignedHostNum++;
	    	switchIndex++;
	    }

	    int hostIndex = 0;
	    for (int i = 0; i < weightEachNode.length; i++) {
	    	for (int j = 0; j < weightEachNode[i]; j++) {
	    		hostSwithMap[hostIndex] = i;
	    		hostIndex++;
	    	}
	    }
	    
	    while (remainSwitch.size() > 1) {
	    	int rand1 = (int)(Math.random() * remainSwitch.size());
	    	int rand2 = (int)(Math.random() * remainSwitch.size());
	    	if (rand1 == rand2)
	    		continue;
	    	
	    	int switchIndex1 = remainSwitch.get(rand1);
	    	int switchIndex2 = remainSwitch.get(rand2);
	    	addBidirNeighbor(switchIndex1, switchIndex2);
	    	
	    	remainPort[switchIndex1]--;
	    	if (remainPort[switchIndex1] == 0) {
	    		deleteObj(remainSwitch, switchIndex1);
	    	}
	    	
	    	remainPort[switchIndex2]--;
	    	if (remainPort[switchIndex2] == 0) {
	    		deleteObj(remainSwitch, switchIndex2);
	    	}
	    }
	    
	    if (remainSwitch.size() == 1) {
	    	switchIndex = remainSwitch.get(0);
	    	while (remainPort[switchIndex] > 1) {
	    		int randIndex = (int)(Math.random()*switchNum);
	    		if (randIndex == switchIndex)
	    			continue;
	    		
	    		int targetIndex = -1;
	    		for (int i = 0; i < switchNum; i++) {
	    			if (i != randIndex && i != switchIndex && isNeighbor(randIndex, i)) {
	    				targetIndex = i;
	    				break;
	    			}
	    			else
	    				continue;
	    		}
	    		
	    		if (targetIndex != -1) {
	    			System.out.println("***** deleted " + randIndex + " " + targetIndex);
	    			deleteBidirNeighbor(randIndex, targetIndex);
	    			addBidirNeighbor(randIndex, switchIndex);
	    			addBidirNeighbor(targetIndex, switchIndex);
	    			remainPort[switchIndex] -= 2;
	    		}	
	    	}
	    }

	    
	}

	/*public int getNodeIndex (String nodeName) {
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
	}*/

	public void deleteObj (ArrayList<Integer> list, int obj) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == obj) {
				list.remove(i);
				break;
			}
		}		
	}
	
	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		return hostSwithMap[i];
	}
}
