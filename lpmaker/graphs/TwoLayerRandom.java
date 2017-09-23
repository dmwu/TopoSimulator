
/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;
import java.util.*;
import java.io.*;
import java.util.ArrayList;

public class TwoLayerRandom extends Graph{
	
	int K;

    int coreSwitchNum;
    int switchNum;
    int switchPerPod;
    int podNum;
    int hostPerPod;

    public TwoLayerRandom(int K_){
		super(K_*K_*5/4);
		this.K = K_;
		setParameters(K_);
		populateAdjacencyList();
		name = "two-layer-random";
	}

    public TwoLayerRandom(int K_, double fail_rate){
		super(K_*K_*5/4);
		this.K = K_;
		System.out.println(K_ + " ^^^^^^^^^^^^^^^^^^^^ ");
		setParameters(K_);
		populateAdjacencyList();
		name = "two-layer-random";
		failLinks(fail_rate);
	}

	public void setParameters(int k){
		coreSwitchNum = k*k/4;
		switchNum = k*k*5/4;
		switchPerPod = k;
		podNum = k;
		hostPerPod = k*k/4;
	}

	// +++++++++++ THIS CONSTRUCTION ROUTINE FOR VANILLA FAT TREE +++++++++++++++++++++++++++++++
	private void populateAdjacencyList(){
		//layout
		
		int[] remainPort = new int[switchNum];
	    ArrayList<Integer> remainSwitch = new ArrayList<Integer> ();

	    setUpFixWeight(0);

	    for (int i = 0; i < podNum; i++) {
	    	remainSwitch.clear();

	    	for (int j = 0; j < switchPerPod; j++) {
	    		int switchIndex = i * switchPerPod + j;
	    		int curntHostNum;

	    		if (K % 4 == 2 && j % 2 == 1)
	    			curntHostNum = K/4+1;
	    		else
	    			curntHostNum = K/4;

	    		weightEachNode[switchIndex] += curntHostNum;
	    		totalWeight += curntHostNum;

	    		remainPort[switchIndex] = K/2;
	    		remainSwitch.add(switchIndex);
	    	}

	    	createRandomGraph(remainSwitch, remainPort, i*switchPerPod, i*switchPerPod+switchPerPod);
	    }

	    for (int i = 0; i < podNum; i++) {
	    	for (int j = 0; j < switchPerPod; j++) {
	    		int switchIndex = i * switchPerPod + j;

	    		if (K % 4 == 2 && j % 2 == 0)
	    			remainPort[switchIndex] = K/4+1;
	    		else
	    			remainPort[switchIndex] = K/4;
	    	}
	    }

		for (int i = switchNum-coreSwitchNum; i < switchNum; i++) {
	    	remainPort[i] = K;
	    }

	    remainSwitch.clear();
	    for(int i = 0; i < switchNum; i++)
	    	remainSwitch.add(i);

	    createRandomGraph(remainSwitch, remainPort, 0, switchNum);
	}

	public void createRandomGraph (ArrayList<Integer> remainSwitch, int[] remainPort, int swStart, int swEnd) {
		System.out.println("**************************");

		HashMap<Integer, HashMap<Integer, Integer>> edges = new HashMap<Integer, HashMap<Integer, Integer>> ();

	    while (remainSwitch.size() > 1) {
	    	int rand1 = (int)(Math.random() * remainSwitch.size());
	    	int rand2 = (int)(Math.random() * remainSwitch.size());

	    	if (rand1 == rand2)
	    		continue;
	    	
	    	int switchIndex1 = remainSwitch.get(rand1);
	    	int switchIndex2 = remainSwitch.get(rand2);
	    	//addBidirNeighbor(switchIndex1, switchIndex2);
	    	
	    	//if (checkEdge(edges, switchIndex1, switchIndex2))
	    		//continue;

	    	System.out.println(switchIndex1 + " <--> " + switchIndex2);
	    	addEdge(edges, switchIndex1, switchIndex2);
	    	
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
	    	int switchIndex = remainSwitch.get(0);
	    	while (remainPort[switchIndex] > 1) {
	    		int randIndex = (int)(Math.random()*(swEnd-swStart))+swStart;
	    		if (randIndex == switchIndex)
	    			continue;
	    		
	    		int targetIndex = -1;
	    		if (!edges.containsKey(randIndex))
	    			continue;

	    		//System.out.println(randIndex);

	    		/*HashSet<Integer> curnt = edges.get(randIndex);
	    		Iterator iter = curnt.iterator();
	    		while (iter.hasNext()) {
	    			targetIndex = (int)iter.next();
	    			if (targetIndex != switchIndex && !checkEdge(edges, switchIndex, targetIndex)) {
	    				break;
	    			}
	    		}*/

	    		HashMap<Integer, Integer> curnt = edges.get(randIndex);
	    		Iterator iter = curnt.entrySet().iterator();
	    		while (iter.hasNext()) {
	    			Map.Entry curntPair = (Map.Entry)iter.next();

	    			targetIndex = (int)curntPair.getKey();
	    			if (targetIndex != switchIndex && !checkEdge(edges, switchIndex, targetIndex)) {
	    				break;
	    			}
	    		}

	    		/*for (int i = 0; i < switchNum; i++) {
	    			if (i != randIndex && i != switchIndex && isNeighbor(randIndex, i)) {
	    				targetIndex = i;
	    				break;
	    			}
	    			else
	    				continue;
	    		}*/
	    		
	    		if (targetIndex != -1) {
	    			//deleteBidirNeighbor(randIndex, targetIndex);
	    			System.out.println("***** deleted: " + randIndex + " <--> " + targetIndex);
	    			deleteEdge(edges, randIndex, targetIndex);
	    			//addBidirNeighbor(randIndex, switchIndex);
	    			System.out.println(randIndex + " <--> " + switchIndex);
	    			addEdge(edges, randIndex, switchIndex);
	    			//addBidirNeighbor(targetIndex, switchIndex);
	    			System.out.println(targetIndex + " <--> " + switchIndex);
	    			addEdge(edges, targetIndex, switchIndex);
	    			remainPort[switchIndex] -= 2;
	    		}	
	    	}
	    }

	    Iterator it = edges.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();

	        int id1 = (int)pair.getKey();
	        int id2;

	        HashMap<Integer, Integer> curnt = (HashMap<Integer, Integer>)pair.getValue();
	        Iterator iter = curnt.entrySet().iterator();
	        while (iter.hasNext()) {
	        	Map.Entry curntPair = (Map.Entry)iter.next();
	        	id2 = (int)curntPair.getKey();
	        	int weight = (int)curntPair.getValue();
	        	if (id1 < id2) {
	        		for (int t = 0; t < weight; t++) {
	        			addBidirNeighbor(id1, id2);
	        		}
	        	}
	        }
	    }
	}

	public void addEdge (HashMap<Integer, HashMap<Integer, Integer>> edges, int id1, int id2) {
		if (!edges.containsKey(id1)) {
			HashMap<Integer, Integer> curnt = new HashMap<Integer, Integer> ();
			curnt.put(id2, 1);
			edges.put(id1, curnt);
		}
		else {
			HashMap<Integer, Integer> curnt = edges.get(id1);
			int weight;
			if (curnt.containsKey(id2)) {
				//System.err.println("Adding an existent edge! " + id1 + " <--> " + id2);
				//System.exit(-1);
				weight = curnt.get(id2);
				weight++;
			}
			else
				weight = 1;

			curnt.put(id2, weight);
			edges.put(id1, curnt);
		}

		if (!edges.containsKey(id2)) {
			HashMap<Integer, Integer> curnt = new HashMap<Integer, Integer> ();
			curnt.put(id1, 1);
			edges.put(id2, curnt);
		}
		else {
			HashMap<Integer, Integer> curnt = edges.get(id2);
			int weight;
			if (curnt.containsKey(id1)) {
				//System.err.println("Adding an existent edge! " + id1 + " <--> " + id2);
				//System.exit(-1);
				weight = curnt.get(id1);
				weight++;
			}
			else
				weight = 1;

			curnt.put(id1, weight);
			edges.put(id2, curnt);
		}
	}

	public void deleteEdge (HashMap<Integer, HashMap<Integer, Integer>> edges, int id1, int id2) {
		if (!edges.containsKey(id1)) {
			System.err.println("Deleting an unexistent edge! " + id1 + " <--> " + id2);
			System.exit(-1);
		}
		else {
			HashMap<Integer, Integer> curnt = edges.get(id1);
			if (!curnt.containsKey(id2)) {
				System.err.println("Deleting an unexistent edge! " + id1 + " <--> " + id2);
				System.exit(-1);
			}
			else {
				int weight = curnt.get(id2);
				weight--;

				if (weight == 0)
					curnt.remove(id2);
				else
					curnt.put(id2, weight);
			}

			if (curnt.isEmpty())
				edges.remove(id1);
			else
				edges.put(id1, curnt);
		}

		if (!edges.containsKey(id2)) {
			System.err.println("Deleting an unexistent edge! " + id2 + " <--> " + id1);
			System.exit(-1);
		}
		else {
			HashMap<Integer, Integer> curnt = edges.get(id2);
			if (!curnt.containsKey(id1)) {
				System.err.println("Deleting an unexistent edge! " + id2 + " <--> " + id1);
				System.exit(-1);
			}
			else {
				int weight = curnt.get(id1);
				weight--;

				if (weight == 0)
					curnt.remove(id1);
				else
					curnt.put(id1, weight);
			}
			
			if (curnt.isEmpty())
				edges.remove(id2);
			else
				edges.put(id2, curnt);
		}
	}


	public boolean checkEdge (HashMap<Integer, HashMap<Integer, Integer>> edges, int id1, int id2) {
		if (!edges.containsKey(id1))
			return false;
		else {
			HashMap<Integer, Integer> curnt = edges.get(id1);
			if (!curnt.containsKey(id2))
				return false;
			else
				return true;
		}
	}

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
		int sw;
		if (K % 4 == 0)
			sw = i/(K/4); 
		else {
			int pairIndex = i/(K/2);
			int offset = i - (K/2)*pairIndex;

			if (offset < (K/4))
				sw = 2*pairIndex;
			else
				sw = 2*pairIndex+1;
		}
		//System.out.println(i + " TO " + sw);
		return sw;
	}

	public static void main (String args[]) {
		TwoLayerRandom myRandom = new TwoLayerRandom(6, 0);
		/*int k = myRandom.K;
		for (int i = 0; i < k*k*k/4; i++) {
			myRandom.svrToSwitch(i);
		}*/
	}
}
