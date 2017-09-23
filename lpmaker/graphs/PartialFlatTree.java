
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class PartialFlatTree extends Graph{
	
	public int K;
	public int flatPodNum;

	//int nodeNum;
	int hostNum;
    int podNum;
    int edgeSwitchPerPod;
    int aggrSwitchPerPod;
    int edgeSwitchNum;
    int aggrSwitchNum;
    int coreSwitchNum;
    int coreGroupSize;
    int coreGroupNum;

    int hostPerEdge;
    int hostPerAggr;
    int hostPerCore;


	public PartialFlatTree(int K_, int flat_pod_num){
		super(K_*K_*5/4);
		this.K = K_;
		setParameters(K_);
		flatPodNum = flat_pod_num;
		populateAdjacencyList();
		name = "flat";		
	}
	public PartialFlatTree(int K_, int flat_pod_num, double fail_rate){
		super(K_*K_*5/4);
		this.K = K_;
		System.out.println(K_ + " ^^^^^^^^^^^^^^^^^^^^ ");
		setParameters(K_);
		flatPodNum = flat_pod_num;
		populateAdjacencyList();
		name = "flat";
		failLinks(fail_rate);
	}

	public void setParameters (int k) {
		hostNum = k*k*k/4;
	    podNum = k;
	    edgeSwitchPerPod = k/2;
	    aggrSwitchPerPod = k/2;
	    edgeSwitchNum = podNum * edgeSwitchPerPod;
	    aggrSwitchNum = podNum * aggrSwitchPerPod;
	    coreSwitchNum = k*k/4;
	    coreGroupSize = k/2;
	    coreGroupNum = k/2;
		//nodeNum = hostNum + edgeSwitchNum + aggrSwitchNum + coreSwitchNum;

		hostPerEdge = k/4;
		hostPerAggr = (int)Math.ceil((double)k/8);
		hostPerCore = k/2 - hostPerEdge - hostPerAggr;
	}

	/*public ArrayList TrafficGenAllAll()
	{

		// Over-ridden because I need traffic only between nodes with terminals
		// Server j on node i i.e. [(noNodeswithTerminals - 1) * i + j] th server sends to jth server on all other nodes

		int noNodeswithTerminals = K*K/2;
		int numPerms = noNodeswithTerminals - 1;

		ArrayList<Integer> ls = new ArrayList<Integer>();
		for (int i = 0; i < noNodeswithTerminals; i++) {
			int target = 0;
			for (int svr = 0; svr < numPerms; svr++) {
				if (target == i) target ++;
				ls.add(numPerms * target  + svr);
				target ++;
			}
		}

		System.out.println("NUM FLOWS = " + ls.size());

		return ls;
	}*/
	
	/*public ArrayList TrafficGenPermutations()
	{
		// Over-ridden because I need traffic only between nodes with terminals
		
		int noNodeswithTerminals = K*K/2;
		int numPerms = totalWeight / noNodeswithTerminals;
		int[][] allPerms = new int[numPerms][noNodeswithTerminals];
		Random rand = new Random(ProduceLP.universalRand.nextInt(10));

		for (int currPerm = 0; currPerm < numPerms; currPerm ++)
		{
			allPerms[currPerm] = new int[noNodeswithTerminals];
			for (int i = 0; i < noNodeswithTerminals; i++)
			{
				int temprand = rand.nextInt(i + 1);
				allPerms[currPerm][i] = allPerms[currPerm][temprand];
				allPerms[currPerm][temprand] = i;
			}

			// fix cases where a node is sending to itself
			Vector<Integer> badCases = new Vector<Integer>();
			for (int i = 0; i < noNodeswithTerminals; i++)
			{
				if (allPerms[currPerm][i] == i) badCases.add(new Integer(i));
			}
			for (Integer badOne : badCases)
			{
				int temprand= -1;
				do
				{
					temprand = rand.nextInt(noNodeswithTerminals);
				}
				while (allPerms[currPerm][temprand] == temprand);

				allPerms[currPerm][badOne.intValue()] = allPerms[currPerm][temprand];
				allPerms[currPerm][temprand] = badOne.intValue();
			}
		}

		ArrayList<Integer> ls = new ArrayList<Integer>();
		for (int i = 0; i < noNodeswithTerminals; i++)
		{
			for (int currPerm = 0; currPerm < numPerms; currPerm ++)
			{
				ls.add(numPerms * allPerms[currPerm][i] + currPerm);
			}
		}
		return ls;
	}*/

	// +++++++++++ THIS CONSTRUCTION ROUTINE FOR VANILLA FAT TREE +++++++++++++++++++++++++++++++
	private void populateAdjacencyList(){
		//layout
		
		//0-(K*K/2) lower layer close to hosts 
		//(K*K/2) - K*K middle layer
		//K*K - 5/4*K*K core layer

		//connect lower to middle
		/*for(int pod = 0; pod < K; pod++){
			for(int i = 0; i < K/2; i++){
				for(int l = 0; l < K/2; l++){
					addBidirNeighbor(pod*K/2+i, K*K/2+pod*K/2+l);
				}
			}
		}*/
		
		//connect middle to core
		/*for(int core_type = 0; core_type < K/2; core_type++){
			for(int incore = 0; incore < K/2; incore++){
				for(int l = 0; l < K; l++){
					addBidirNeighbor(K*K+core_type*K/2+incore, K*K/2+l*K/2+core_type);
				}
			}
		}*/

		setUpFixWeight(0);

		for (int i = 0; i < podNum; i++) {
			for (int j = 0; j < edgeSwitchPerPod; j++) {
				int edgeSwitchIndex = i * edgeSwitchPerPod + j;
				for (int t = 0; t < aggrSwitchPerPod; t++) {
					int aggrSwitchIndex = i * edgeSwitchPerPod + t;

					addBidirNeighbor(getNodeIndex("A"+aggrSwitchIndex), getNodeIndex("E"+edgeSwitchIndex));
					//setConnection("A"+aggrSwitchIndex, "E"+edgeSwitchIndex);
					//System.out.println("A"+aggrSwitchIndex + " <--> E"+edgeSwitchIndex);
				}
			}
		}

		for (int i = flatPodNum; i < podNum; i++) {
			for (int j = 0; j < aggrSwitchPerPod; j++) {
				int aggrSwitchIndex = i * aggrSwitchPerPod + j;
				for (int t = 0; t < coreGroupSize; t++) {
					int coreSwitchIndex = j * coreGroupSize + t;

					addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("A"+aggrSwitchIndex));
					//System.out.println("C"+coreSwitchIndex + " <--> A"+aggrSwitchIndex);
				}
			}
		}

		for (int i = 0; i < flatPodNum; i++) {
			for (int j = 0; j < edgeSwitchPerPod; j++) {
				int edgeSwitchIndex = i * edgeSwitchPerPod + j;	
				int aggrSwitchIndex = i * edgeSwitchPerPod + j;
				int coreSwitchIndex;
				int coreGroupIndex = j;
				
				HashSet<Integer> hostToCore = new HashSet<Integer> ();
				HashSet<Integer> hostToAggr = new HashSet<Integer> ();
				
				//System.out.println("pod #" + i);
				//System.out.println("edge #" + j);

				for (int t = 0; t < hostPerCore; t++) {
					int coreInGroup = i * hostPerCore + t;
					int coreInGroupIndex = coreInGroup % coreGroupSize;
					coreSwitchIndex = coreGroupIndex*coreGroupSize+coreInGroupIndex;
					//System.out.print(coreSwitchIndex + " ");
					hostToCore.add(coreSwitchIndex);
					weightEachNode[getNodeIndex("C"+coreSwitchIndex)] += 1;
					totalWeight += 1;
				}
				//System.out.println();
				
				for (int t = 0; t < hostPerAggr; t++) {
					if (K % 4 == 0) {						
						int coreInGroup = i * hostPerAggr + t;
						int oppositeIndex = coreInGroup % coreGroupSize;
						coreSwitchIndex = coreGroupIndex*coreGroupSize+(coreGroupSize-1-oppositeIndex);
					}
					else {
						int coreInGroup = (i * hostPerCore + hostPerCore + t) % coreGroupSize;
						coreSwitchIndex = coreGroupIndex*coreGroupSize + coreInGroup;	
					}

					addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("E"+edgeSwitchIndex));
					//setConnection("C"+coreSwitchIndex, "E"+edgeSwitchIndex);
					//System.out.println("C"+coreSwitchIndex + " <--> E"+edgeSwitchIndex);
					
					//System.out.print(coreSwitchIndex + " ");
					hostToAggr.add(coreSwitchIndex);
				}
				
				for (int t = 0; t < coreGroupSize; t++) {
					coreSwitchIndex = j * coreGroupSize + t;
					if (!hostToCore.contains(coreSwitchIndex) && !hostToAggr.contains(coreSwitchIndex)) {
						addBidirNeighbor(getNodeIndex("C"+coreSwitchIndex), getNodeIndex("A"+aggrSwitchIndex));
						//setConnection("C"+coreSwitchIndex, "A"+aggrSwitchIndex);
						//System.out.println("C"+coreSwitchIndex + " <--> A"+aggrSwitchIndex);
					}
				}
			}
		}

		for (int i = 0; i < flatPodNum; i++) {
			int oneSideNum = K/4;
			
			for (int j = 0; j < hostPerCore; j++) {
				for (int t = 0; t < oneSideNum; t++) {
					int srcIndex = i * edgeSwitchPerPod + t;
					int destOppositeIndex = (srcIndex + j) % oneSideNum;
					int destInPodIndex = edgeSwitchPerPod - 1 - destOppositeIndex;
					int destPodIndex = (i - 1 + flatPodNum) % flatPodNum;
					int destIndex = destPodIndex * edgeSwitchPerPod + destInPodIndex;
					
					addBidirNeighbor(getNodeIndex("E"+srcIndex), getNodeIndex("E"+destIndex));
					//setConnection("E"+srcIndex, "E"+destIndex);
					//System.out.println("E"+srcIndex + " <--> E"+destIndex);
					addBidirNeighbor(getNodeIndex("A"+srcIndex), getNodeIndex("A"+destIndex));
					//setConnection("A"+srcIndex, "A"+ destIndex);
				}			
			}
		}
		
		
		//set weights
		//setUpFixWeight(0);
		//int total = 0;

		for (int i = 0; i < flatPodNum; i++) {
			for (int j = 0; j < edgeSwitchPerPod; j++) {
				int edgeIndex = i * edgeSwitchPerPod + j;
				weightEachNode[getNodeIndex("E"+edgeIndex)] = hostPerEdge;
				totalWeight += hostPerEdge;
			}
			for (int j = 0; j < aggrSwitchPerPod; j++) {
				int aggrIndex = i * aggrSwitchPerPod + j;
				weightEachNode[getNodeIndex("A"+aggrIndex)] = hostPerAggr;
				totalWeight += hostPerAggr;
			}
		}

		for (int i = flatPodNum; i < podNum; i++) {
			for (int j = 0; j < edgeSwitchPerPod; j++) {
				int edgeIndex = i * edgeSwitchPerPod + j;
				weightEachNode[getNodeIndex("E"+edgeIndex)] = K/2;
				totalWeight += K/2;
			}
		}

		/*for (int i = 0; i < edgeSwitchNum; i++) {
			int edgeIndex = getNodeIndex("E"+i);
			weightEachNode[edgeIndex] = hostPerEdge;
			totalWeight += hostPerEdge;
		}

		for (int i = 0; i < aggrSwitchNum; i++) {
			int aggrIndex = getNodeIndex("A"+i);
			weightEachNode[aggrIndex] = hostPerAggr;
			totalWeight += hostPerAggr;
		}

		int accumuHostPerCore = (hostNum-hostPerEdge*edgeSwitchNum-hostPerAggr*aggrSwitchNum)/coreSwitchNum;
		for (int i = 0; i < coreSwitchNum; i++) {
			int coreIndex = getNodeIndex("C"+i);
			weightEachNode[coreIndex] = accumuHostPerCore;
			totalWeight += accumuHostPerCore;
		}*/
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
	
	public int getK(){
		return K;
	}
	
	public int getNoHosts(){
		return K*K*K/4;
	}
	
	public int getNoSwitches(){
		return K*K*5/4;
	}
	
	public int svrToSwitch(int i)	//i is the server index. return the switch index.
	{
		//return i / weightEachNode[0];
		/*int accumuHostPerCore = (hostNum-hostPerEdge*edgeSwitchNum-hostPerAggr*aggrSwitchNum)/coreSwitchNum;

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
		}*/


		int hostPerPod = hostNum / podNum;

		if (i < hostPerPod * flatPodNum) {
			int podIndex = i / hostPerPod;
			int hostInPod = i - podIndex * hostPerPod;
			int hostEdgeIndex = hostInPod / (K/2);
			int hostInEdge = hostInPod - hostEdgeIndex * (K/2);

			if (hostInEdge < hostPerEdge) {
				int edgeIndex = podIndex * edgeSwitchPerPod + hostEdgeIndex;
				//System.out.println(i + "-->" + "E" + edgeIndex);
				return getNodeIndex("E"+edgeIndex);
			}
			else if (hostInEdge < (hostPerEdge + hostPerAggr)) {
				int aggrIndex = podIndex * aggrSwitchPerPod + hostEdgeIndex;
				//System.out.println(i + "-->" + "A" + aggrIndex);
				return getNodeIndex("A"+aggrIndex);
			}
			else {
				int hostCore = hostInEdge - hostPerEdge - hostPerAggr;
				int coreInGroup = podIndex * hostPerCore + hostCore;
				int coreInGroupIndex = coreInGroup % coreGroupSize;
				int coreIndex = hostEdgeIndex*coreGroupSize+coreInGroupIndex;
				//System.out.println(i + "-->" + "C" + coreIndex);
				return getNodeIndex("C"+coreIndex);
			}
		}
		else {
			int indexInZone = i - hostPerPod * flatPodNum;
			int edgeSwitchIndex = indexInZone / (K/2) + flatPodNum * edgeSwitchPerPod;
			//System.out.println(i + "-->" + "E" + edgeSwitchIndex);
			return getNodeIndex("E"+edgeSwitchIndex);
		}
	}

	/*public static void main (String args[]) {
		PartialFlatTree myTree = new PartialFlatTree(6, 2);
		for (int i = 0; i < myTree.hostNum; i++) {
			myTree.svrToSwitch(i);
		}
		System.out.println();
		System.out.println(myTree.totalWeight);
		System.out.println();
		for (int i = 0; i < myTree.noNodes; i++) {
			if (myTree.weightEachNode[i] > 0) {
				System.out.println(i + " " + myTree.weightEachNode[i]);
			}
		}
	}*/
}
