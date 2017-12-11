/* *******************************************************
 * Released under the MIT License (MIT) --- see ../../LICENSE
 * Copyright (c) 2014 Ankit Singla, Sangeetha Abdu Jyothi, Chi-Yao Hong, Lucian Popa, P. Brighten Godfrey, Alexandra Kolla
 * ******************************************************** */

package lpmaker.graphs;

import java.util.*;
import java.io.*;
import java.math.*;

class TrafficPair {
	public int from;
	public int to;

	TrafficPair(int a, int b) {
		from = a;
		to = b;
	}
}

public class Graph
{
	public static final int INFINITY = 999999999;
	public static final int INVALID_NODE_ID = -1;

	public int noNodes;
	public int numEdges;
	public Vector<Link>[] adjacencyList;

	public int[][] shortestPathLen;

	//no of sub-hosts of a switch
	public int[] weightEachNode;
	public int totalWeight = 0;

	//a common seed used for both PrintToMCF and PrintToMCFFair
	public long gseed;

	//no of packets at this node's Q
	public int[] localQ;

	//creation buffer
	public int[] creationBuffer;

	//default number of hosts associated to each node in the graph
	public static final int DEFAULT_NO_HOSTS = 1;

	int[][] switchLevelMatrix;
	int nCommodities;

	protected boolean[] isSwitch;


	public static Random rand;
	public String name = "graph";

	public Graph()
	{
		//rand = ProduceLP.universalRand;
		rand = new Random();
	}

	public Graph(int size)
	{
		//rand = ProduceLP.universalRand;
		rand = new Random();
		noNodes = size;
		localQ = new int[noNodes];
		creationBuffer = new int[noNodes];
		allocateAdjacencyList();
		setUpFixWeight(DEFAULT_NO_HOSTS);
		setUpAllSwitches();
		switchLevelMatrix = new int[noNodes][noNodes];
	}

	public Graph(int size, int noHosts)
	{
		noNodes = size;
		//rand = ProduceLP.universalRand;
		rand = new Random();
		localQ = new int[noNodes];
		creationBuffer = new int[noNodes];
		allocateAdjacencyList();
		setUpFixWeight(noHosts);
		setUpAllSwitches();
		switchLevelMatrix = new int[noNodes][noNodes];
	}

	public void randomConstructorPerms(Vector<Integer> in_set, Vector<Integer> in_degrees, int cap)
	{
		// Clone the vectors
		Vector<Integer> still_to_link = new Vector<Integer>();
		Vector<Integer> degrees = new Vector<Integer>();
		for (int i = 0; i < in_set.size(); i++) still_to_link.add(((Integer)in_set.elementAt(i)));
		for (int i = 0; i < in_degrees.size(); i++) degrees.add(((Integer)in_degrees.elementAt(i)));
		int[] linksAdded = new int[noNodes];

		boolean found = true;

		while(found)
		{
			//< Permute still_to_link
			int[] permArr = new int[still_to_link.size()];
			for (int i = 0; i < permArr.length; i++)
				permArr[i] = still_to_link.elementAt(i).intValue();

			for(int i = permArr.length; i > 1; i--)
			{
				// swap permArr[i-1] and permArr[rand(0 to i-1)]
				int temprand = rand.nextInt(i);
				int tempswap = permArr[temprand];
				permArr[temprand] = permArr[i-1];
				permArr[i-1] = tempswap;
			}
			//>

			//< Connect stuff not already connected, and clean up
			found = false;
			for (int i = 0; i < permArr.length; i++)
			{
				if (isNeighbor(still_to_link.elementAt(i).intValue(), permArr[i])) continue;

				found = true;
				addBidirNeighbor(permArr[i], still_to_link.elementAt(i).intValue(), cap);
				linksAdded[still_to_link.elementAt(i).intValue()]++;
				linksAdded[permArr[i]]++;
			}

			still_to_link.clear();
			degrees.clear();
			for (int i = 0; i < in_set.size(); i++)
			{
				int remDeg = in_degrees.elementAt(i) - linksAdded[in_set.elementAt(i)];
				if (remDeg > 1)
				{
					degrees.add(new Integer(remDeg));
					still_to_link.add(new Integer(in_set.elementAt(i)));
				}
			}
			if (still_to_link.size() < 2) break;
			//>
		}

		// Edge swaps to fix things up
		System.out.println("Construction phase 2");
		still_to_link.clear();
		degrees.clear();
		for (int i = 0; i < in_set.size(); i++)
		{
			int remDeg = in_degrees.elementAt(i) - linksAdded[in_set.elementAt(i)];
			if (remDeg > 0)
			{
				degrees.add(new Integer(remDeg));
				still_to_link.add(new Integer(in_set.elementAt(i)));
			}
		}

		int fix_iter = 0;
		while (fix_iter < 5000 && still_to_link.size() != 0)
		{
			fix_iter ++;
			int badNode = still_to_link.elementAt(0);
			int degFix = degrees.elementAt(0);
			int anotherBad = badNode;

			if (degFix == 1)   // Find another different bad node
			{
				if (still_to_link.size() == 1) return;
				anotherBad = still_to_link.elementAt(1);
			}

			// Locate edge to break
			int randNode1 = badNode;
			int randNode2 = badNode;
			while (randNode1 == badNode || randNode1 == anotherBad || randNode2 == badNode ||
			        randNode2 == anotherBad || isNeighbor(badNode, randNode1) || isNeighbor(anotherBad, randNode2))
			{
				randNode1 = ((Integer)in_set.elementAt(rand.nextInt(in_set.size()))).intValue();
				int mycap = 0;
				do {
					int rndnbr = rand.nextInt(adjacencyList[randNode1].size());
					randNode2 = adjacencyList[randNode1].elementAt(rndnbr).intValue();
					mycap = adjacencyList[randNode1].elementAt(rndnbr).linkcapacity;

				}
				while (!in_set.contains(new Integer(randNode2)) || mycap != cap);
			}

			// Swap
			removeBidirNeighbor(randNode1, randNode2);
			addBidirNeighbor(badNode, randNode1, cap);
			addBidirNeighbor(anotherBad, randNode2, cap);
			fix_iter = 0;

			// Fix still_to_link and degrees
			if (degFix == 1)
			{
				degrees.set(0, degFix - 1);
				degrees.set(1, degrees.elementAt(1) - 1);
			}
			else degrees.set(0, degFix - 2);

			if (degrees.elementAt(0) == 0)
			{
				still_to_link.remove(0);
				degrees.remove(0);
			}
			if (still_to_link.size() == 0) break;
			if (degrees.elementAt(0) == 0)
			{
				still_to_link.remove(0);
				degrees.remove(0);
			}

			if (still_to_link.size() < 2) continue;
			if (degrees.elementAt(1) == 0)
			{
				still_to_link.remove(1);
				degrees.remove(1);
			}
		}
		System.out.println("Construction done!");
	}

	public void randomConstructor(Vector<Integer> in_set, Vector<Integer> in_degrees, int cap)
	{
		// Clone the vectors
		Vector<Integer> still_to_link = new Vector<Integer>();
		Vector<Integer> degrees = new Vector<Integer>();
		int[] linksAdded = new int[noNodes];

		for (int i = 0; i < in_set.size(); i++) still_to_link.add(((Integer)in_set.elementAt(i)));
		for (int i = 0; i < in_degrees.size(); i++) degrees.add(((Integer)in_degrees.elementAt(i)));

		int stop_sign = 0;

		while(!still_to_link.isEmpty() && stop_sign==0)
		{
			if(still_to_link.size() == 1) 				// Ignores this case of 1 node left out
			{
				stop_sign=1;
			}
			boolean found = false;

			int p1 = -1, p2 = -1;
			Integer n1 = new Integer(0);
			Integer n2 = new Integer(0);

			int iteration = 0;
			int MAX_ITERATION = 1000;

			while(!found && iteration++ < MAX_ITERATION && stop_sign == 0)  // try until a node-pair to connect is found
			{
				p1 = rand.nextInt(still_to_link.size());
				p2 = p1;
				while(p2 == p1)
				{
					p2 = rand.nextInt(still_to_link.size());
				}

				n1 = (Integer)still_to_link.elementAt(p1);
				n2 = (Integer)still_to_link.elementAt(p2);

				// Check if an n1-n2 edge already exists
				int k=0;
				for(int i=0; i<adjacencyList[n1.intValue()].size(); i++)
					if(adjacencyList[n1.intValue()].elementAt(i).intValue() == n2) k=1;

				if(k==0)   // Edge doesn't already exist. Good, add it!
				{
					found = true;
					addBidirNeighbor(n1, n2, cap);
					linksAdded[n1]++;
					linksAdded[n2]++;
				}
			}

			if(stop_sign==0)
			{
				/*
				 * If a clique of nodes is left in the end, this gives up
				 */

				if(iteration >= MAX_ITERATION) 	// Give up if can't find a pair to link
				{
					System.out.println("WARNING: Unable to find new pair for link between:"+still_to_link);
					stop_sign=1;
				}
				degrees.set(p1, new Integer(((Integer)(degrees.elementAt(p1))).intValue()-1));
				degrees.set(p2, new Integer(((Integer)(degrees.elementAt(p2))).intValue()-1));
				boolean deleted_p1 = false;

				if(((Integer)degrees.elementAt(p1)).intValue() == 0)
				{
					// Degree exhausted => No longer available to connect
					still_to_link.remove(p1);
					degrees.remove(p1);
					deleted_p1 = true;
				}

				// Just adjusting the vector index here, nothing related to high-level function
				int p2_updated;
				if(deleted_p1 && p1 < p2)
					p2_updated = p2-1;
				else
					p2_updated = p2;

				if(((Integer)degrees.elementAt(p2_updated)).intValue() == 0)
				{
					// Degree exhausted => No longer available to connect
					still_to_link.remove(p2_updated);
					degrees.remove(p2_updated);
				}
			}
		}

		// Edge swaps to fix things up
		System.out.println("Construction phase 2");
		still_to_link.clear();
		degrees.clear();
		for (int i = 0; i < in_set.size(); i++)
		{
			int remDeg = in_degrees.elementAt(i) - linksAdded[in_set.elementAt(i)];
			if (remDeg > 0)
			{
				degrees.add(new Integer(remDeg));
				still_to_link.add(new Integer(in_set.elementAt(i)));
			}
		}

		int fix_iter = 0;
		while (fix_iter < 5000 && still_to_link.size() != 0)
		{
			fix_iter ++;
			int badNode = still_to_link.elementAt(0);
			int degFix = degrees.elementAt(0);
			int anotherBad = badNode;

			if (degFix == 1)   // Find another different bad node
			{
				if (still_to_link.size() == 1) return;
				anotherBad = still_to_link.elementAt(1);
			}

			// Locate edge to break
			int randNode1 = badNode;
			int randNode2 = badNode;
			while (randNode1 == badNode || randNode1 == anotherBad || randNode2 == badNode ||
			        randNode2 == anotherBad || isNeighbor(badNode, randNode1) || isNeighbor(anotherBad, randNode2))
			{
				randNode1 = ((Integer)in_set.elementAt(rand.nextInt(in_set.size()))).intValue();
				int mycap = 0;
				do {
					int rndnbr = rand.nextInt(adjacencyList[randNode1].size());
					randNode2 = adjacencyList[randNode1].elementAt(rndnbr).intValue();
					mycap = adjacencyList[randNode1].elementAt(rndnbr).linkcapacity;

				}
				while (!in_set.contains(new Integer(randNode2)) || mycap != cap);
			}

			// Swap
			removeBidirNeighbor(randNode1, randNode2);
			addBidirNeighbor(badNode, randNode1, cap);
			addBidirNeighbor(anotherBad, randNode2, cap);
			fix_iter = 0;

			// Fix still_to_link and degrees
			if (degFix == 1)
			{
				degrees.set(0, degFix - 1);
				degrees.set(1, degrees.elementAt(1) - 1);
			}
			else degrees.set(0, degFix - 2);

			if (degrees.elementAt(0) == 0)
			{
				still_to_link.remove(0);
				degrees.remove(0);
			}
			if (still_to_link.size() == 0) break;
			if (degrees.elementAt(0) == 0)
			{
				still_to_link.remove(0);
				degrees.remove(0);
			}

			if (still_to_link.size() < 2) continue;
			if (degrees.elementAt(1) == 0)
			{
				still_to_link.remove(1);
				degrees.remove(1);
			}
		}
	}

	protected void allocateAdjacencyList()
	{
		if(noNodes == 0)
			throw new RuntimeException("Attempting to allocate the adjacency list before setting the number of nodes");
		adjacencyList = new Vector[noNodes];
		for(int i = 0; i < noNodes; i++)
		{
			adjacencyList[i] = new Vector();
		}
	}

	protected void setUpFixWeight(int weight)
	{
		weightEachNode = new int[noNodes];
		for(int i = 0; i < noNodes; i++)
		{
			weightEachNode[i] = weight;
		}
	}

	protected void setUpAllSwitches()
	{
		isSwitch = new boolean[noNodes];
		for(int i = 0; i < noNodes; i++)
		{
			isSwitch[i] = true;
			localQ[i] = 0;
		}
	}

	public int getNoNodes()
	{
		return noNodes;
	}

	//should be overloaded
	public int getNoHosts()
	{
		throw new RuntimeException("getNoHosts should be overloaded in derived classes");
	}

	//should be overloaded
	public int svrToSwitch(int index)
	{
		throw new RuntimeException("svrToSwitch should be overloaded in derived classes");
	}

	//should be overloaded
	public int getNoSwitches()
	{
		throw new RuntimeException("getNoSwitches should be overloaded in derived classes");
	}


	public Vector<Link>[] getAdjacencyList()
	{
		return adjacencyList;
	}


	protected boolean addBidirNeighbor(int n1, int n2)
	{
		return addBidirNeighbor(new Integer(n1), new Integer(n2));
	}

	protected boolean removeBidirNeighbor(int n1, int n2, int cap)
	{
		boolean deleted = false;
		for(int i=0; i< adjacencyList[n1].size(); i++)
			if(adjacencyList[n1].elementAt(i).intValue() == n2)
			{
				if (adjacencyList[n1].elementAt(i).linkcapacity > cap)
					adjacencyList[n1].elementAt(i).linkcapacity -= cap;
				else adjacencyList[n1].remove(i);
				deleted = true;
			}
		for(int i=0; i< adjacencyList[n2].size(); i++)
		{
			if(adjacencyList[n2].elementAt(i).intValue() == n1)
			{
				if (adjacencyList[n2].elementAt(i).linkcapacity > cap)
					adjacencyList[n2].elementAt(i).linkcapacity -= cap;
				else adjacencyList[n2].remove(i);
			}
		}
		return deleted;
	}

	protected boolean removeBidirNeighbor(int n1, int n2)
	{
		boolean deleted = false;
		for(int i=0; i< adjacencyList[n1].size(); i++)
			if(adjacencyList[n1].elementAt(i).intValue() == n2)
			{
				adjacencyList[n1].remove(i);
				deleted = true;
			}
		for(int i=0; i< adjacencyList[n2].size(); i++)
			if(adjacencyList[n2].elementAt(i).intValue() == n1)
				adjacencyList[n2].remove(i);
		return deleted;
	}

	protected boolean addBidirNeighbor(Integer n1, Integer n2)
	{
		if(!addNeighbor(n1.intValue(),n2))
			return false;
		if(!addNeighbor(n2.intValue(),n1))
			return false;
		return true;
	}

	protected boolean addBidirNeighbor(int n1, int n2, int cap)
	{
		return addBidirNeighbor(new Integer(n1), new Integer(n2), cap);
	}

	protected boolean addBidirNeighbor(Integer n1, Integer n2, int cap)
	{
		if(!addNeighbor(n1.intValue(),n2, cap))
			return false;
		if(!addNeighbor(n2.intValue(),n1, cap))
			return false;
		return true;
	}

	protected boolean deleteBidirNeighbor(Integer n1, Integer n2)
	{
		if(!deleteNeighbor(n1.intValue(),n2))
			return false;
		if(!deleteNeighbor(n2.intValue(),n1))
			return false;
		return true;
	}

	protected boolean addNeighbor(int n1, int n2)
	{
		return addNeighbor(n1, new Integer(n2));
	}

	protected boolean addNeighbor(int n1, int n2, int cap)
	{
		return addNeighbor(n1, new Integer(n2), cap);
	}

	protected boolean addNeighbor(int n1, Integer n2_i)
	{
		if(n1 == n2_i.intValue())
		{
			System.err.println("Warning: Graph: addNeighbor: trying to add link to itself for "+n1+" ignoring...");
			return false;
		}
		if(n2_i.intValue() >= noNodes)
		{
			throw new RuntimeException("ERROR: Graph: addNeighbor: adding link to non existing node, between node"+n1+" and "+n2_i.intValue());
		}

		// We allow parallel links! We increase the capacity of link by 1 unit.
		for(int i=0; i<adjacencyList[n1].size(); i++)
		{
			if(adjacencyList[n1].get(i).intValue() == n2_i.intValue())
			{
				adjacencyList[n1].elementAt(i).linkcapacity += 1;
				return true;
			}
		}
		adjacencyList[n1].add(new Link(n2_i));
		return true;
	}

	protected boolean addNeighbor(int n1, Integer n2_i, int cap)
	{
		if(n1 == n2_i.intValue())
		{
			return false;
		}
		if(n2_i.intValue() >= noNodes)
		{
			throw new RuntimeException("ERROR: Graph: addNeighbor: adding link to non existing node, between node"+n1+" and "+n2_i.intValue());
		}

		// We allow parallel links! We increase the capacity of link by 1 unit.
		for(int i=0; i<adjacencyList[n1].size(); i++)
		{
			if(adjacencyList[n1].get(i).intValue() == n2_i.intValue())
			{
				adjacencyList[n1].elementAt(i).linkcapacity += cap;
				return true;
			}
		}
		adjacencyList[n1].add(new Link(n2_i, cap));
		return true;
	}

	protected boolean deleteNeighbor(int n1, Integer n2_i)
	{
		if(n1 == n2_i.intValue())
		{
			System.err.println("Warning: Graph: deleteNeighbor: trying to delete link from itself for "+n1+" ignoring...");
			return false;
		}
		if(n2_i.intValue() >= noNodes)
		{
			throw new RuntimeException("ERROR: Graph: deleteNeighbor: deleting link from non existing node, between node"+n1+" and "+n2_i.intValue());
		}

		// We allow parallel links! We increase the capacity of link by 1 unit.
		for(int i=0; i<adjacencyList[n1].size(); i++)
		{
			if(adjacencyList[n1].get(i).intValue() == n2_i.intValue())
			{
				adjacencyList[n1].elementAt(i).linkcapacity -= 1;
				if (adjacencyList[n1].elementAt(i).linkcapacity == 0) {
					adjacencyList[n1].remove(i);
				}
				return true;
			}
		}
		return false;
	}


	public void assertNeighbor(int n1, int n2)
	{
		if(!adjacencyList[n1].contains(new Integer(n2)))
		{
			throw new RuntimeException("ERROR: neighbor assertion failed: "+n2+" is not a neighbor of current "+n1);
		}
	}

	public boolean isNeighbor(int n1, int n2)
	{
		if (n1 == n2) return true;
		for (int i = 0; i < adjacencyList[n1].size(); i++)
			if (adjacencyList[n1].elementAt(i).intValue() == n2) return true;
		return false;
	}
	public Link findNeighbor(int n1, int n2)
	{
		for (int i = 0; i < adjacencyList[n1].size(); i++)
		{
			if (adjacencyList[n1].elementAt(i).intValue() == n2)
				return adjacencyList[n1].elementAt(i);
		}
		return null;
	}

	public void assertBidirectional()
	{
		for(int i = 0; i < noNodes; i++)
		{
			Integer i_obj = new Integer(i);
			for(int j = 0; j < adjacencyList[i].size(); j++)
			{
				if(findNeighbor(adjacencyList[i].get(j).intValue(), i) == null)
				{
					throw new RuntimeException("Graph NOT Bidirectional: node "+i+" has neighbor "+adjacencyList[i].get(j).intValue()+" but the reciprocal is not true");
				}
			}
		}
	}

	public void assertConstantDegree(int expected_degree)
	{
		for(int i = 0; i < noNodes; i++)
		{
			if(adjacencyList[i].size() != expected_degree)
			{
				throw new RuntimeException("Graph: Constant Degree Assertion failed: number of links of node "+i+" is "+adjacencyList[i].size()+" expecting "+expected_degree);
			}
		}
	}

	public int[] getWeightEachNode()
	{
		return weightEachNode;
	}

	public boolean isSwitch(int node_id)
	{
		return isSwitch[node_id];
	}

	public String toString()
	{
		String s = "";
		for(int i = 0; i < noNodes; i++)
		{
			s += i + " " + weightEachNode[i];
			for(int j = 0; j < adjacencyList[i].size(); j++)
			{
				s += " " + adjacencyList[i].elementAt(j).intValue() + " (" + adjacencyList[i].elementAt(j).linkcapacity + ")";
			}
			if (i != noNodes - 1) s += "\n";
		}
		return s;
	}


	protected void createBidirLink(int i, int j)
	{
		adjacencyList[i].add(new Link(j));
		adjacencyList[j].add(new Link(i));
	}

	//modified by wdm 10/12/2017
	public void failLinks(double percentage, int Clos_K, int pos)
	{
		//pos meaning: 0-> any links; 1-> edge2agg links;  2->core links
		int totalEdges = 0;
		for (int i =0; i < noNodes; i++)
		{
			totalEdges += adjacencyList[i].size();
		}
		totalEdges /= 2;

		int failCount = (int) (totalEdges*percentage);

		int linksPerLayer = Clos_K*Clos_K*Clos_K/4;
		int range, linkStartIndex;
		Set<Integer> candiates = new HashSet<>();
		if (pos == 0) {
			//don't care
			linkStartIndex = 0;
			range = totalEdges;
		}else if(pos == 1) {
			//edge2agg links
			linkStartIndex = 0;
			range = linksPerLayer;
		}else{
			//core links
			linkStartIndex = totalEdges - linksPerLayer;
			range = linksPerLayer;
		}
		//fix links to left-most links
		int index = 0;
		while(candiates.size() < failCount){
			int cand =  index + linkStartIndex;
			candiates.add(cand);
			index++;
			assert(index < range);
		}
		for (int cand: candiates){
			int fromSwitch = cand / (Clos_K/2);
			int linkOffset = cand % (Clos_K/2);
			Vector<Integer> connectivity = new Vector<>();
			for(Link to: adjacencyList[fromSwitch]){
				if(to.linkTo > fromSwitch){
					connectivity.add(to.linkTo);
				}
			}
			Collections.sort(connectivity);
			int toSwitch = connectivity.elementAt(linkOffset);
			System.out.println("remove link:"+cand+"  "+"from "+fromSwitch+" to "+toSwitch);
			removeBidirNeighbor(fromSwitch, toSwitch);
		}

	}


	public int getShortestLength(int srcTor, int destTor)
	{
		return shortestPathLen[srcTor][destTor];
	}

	/*remembers all shortest paths*/
	public void modifiedFloydWarshall()
	{
		shortestPathLen = new int[noNodes][noNodes];

		for(int i = 0; i < noNodes; i++)
		{
			for(int j = 0; j < noNodes; j++)
			{
				if(i == j)
					shortestPathLen[i][j] = 0;
				else if(findNeighbor(i,j) != null)
				{
					shortestPathLen[i][j] = 1;//adjacencyMatrix[i][j];
				}
				else
				{
					shortestPathLen[i][j] = Graph.INFINITY;
				}
			}
		}

		//floyd warshall
		for(int k = 0; k < noNodes; k++)
		{
			for(int i = 0; i < noNodes; i++)
			{
				for(int j = 0; j < noNodes; j++)
				{
					if(shortestPathLen[i][j] > shortestPathLen[i][k] + shortestPathLen[k][j])
					{
						shortestPathLen[i][j] = shortestPathLen[i][k] + shortestPathLen[k][j];
					}
				}
			}
		}
	}

	// Print shortestpaths
	public void printPathLengths(String plFile)
	{
		try
		{
			modifiedFloydWarshall();
			FileWriter fstream = new FileWriter(plFile);
			BufferedWriter out = new BufferedWriter(fstream);

			int totalWeight = 0; // delete
			int weightSum = 0;

			for (int i = 0 ; i < noNodes; i++)
			{
				for (int j = 0 ; j < noNodes; j++)
				{
					if (weightEachNode[i] > 0 && weightEachNode[j] > 0 && i != j)
					{
						int printWeight = weightEachNode[i] * weightEachNode[j];

						totalWeight += printWeight; // delete
						weightSum += shortestPathLen[i][j] * printWeight;

						//out.write(shortestPathLen[i][j] + " " + printWeight + "\n");
					}
				}
			}

			out.write((double)weightSum/(double)totalWeight + "\n");
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public ArrayList TrafficDefinedFlow()
	{
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		ls.add(new TrafficPair(0, 180));
		ls.add(new TrafficPair(1, 10));
		ls.add(new TrafficPair(35, 215));
		ls.add(new TrafficPair(52, 44));
		ls.add(new TrafficPair(108, 10));
		return ls;
	}

	// Send from all servers to some random server
	public ArrayList TrafficGenAllToOne()
	{
		int target = rand.nextInt(totalWeight);

		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		for (int svr = 0; svr < totalWeight; svr++)
			ls.add(new TrafficPair(svr, target));
		System.out.println("ALL-ONE FLOWS = " + ls.size());
		return ls;
	}

	public ArrayList TrafficGenPartialAllToOne(int svrNum) {
		int target = rand.nextInt(svrNum);

		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		for (int svr = 0; svr < svrNum; svr++)
			ls.add(new TrafficPair(svr, target));
		System.out.println("ALL-ONE FLOWS = " + ls.size());
		return ls;
	}

	public ArrayList TrafficGenAllAll()
	{
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		for (int svr = 0; svr < totalWeight; svr++)
			for (int svrto = 0; svrto < totalWeight; svrto++)
				ls.add(new TrafficPair(svr, svrto));

		System.out.println("ALL-ALL FLOWS = " + ls.size());

		return ls;
	}

	public ArrayList TrafficGenStride(int n) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		for (int svr = 0; svr < totalWeight; svr++)
			ls.add(new TrafficPair(svr, (svr+n)%totalWeight));
		System.out.println("STRIDE FLOWS = " + ls.size());
		return ls;
		
	}

	public ArrayList TrafficGenCluster(int n) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		int clusterNum = totalWeight / n;

		for (int i = 0; i < clusterNum; i++) {
			for (int j = i*n; j < i*n+n; j++) {
				for (int t = i*n; t < i*n+n; t++) {
					ls.add(new TrafficPair(j, t));
				}
			}
		}

		for (int j = clusterNum*n; j < totalWeight; j++) {
			for (int t = clusterNum*n; t < totalWeight; t++) {
				ls.add(new TrafficPair(j, t));
			}
		}

		return ls;
	}
	//[12-11] modified by WDM to be pod2pod; c should be the pod size
	public ArrayList TrafficGenManyToMany (int c) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		ArrayList<Integer> ahosts = new ArrayList<Integer> ();
		ArrayList<Integer> bhosts = new ArrayList<Integer> ();
		int i,j;
		for (i=0;i<c;i++){
    		ahosts.add(i);
    		bhosts.add(i+c);
  		}
		for (i=0;i<ahosts.size();i++){
    		for (j=0;j<bhosts.size();j++){
//    			if (i == j)
//    				continue;
    			ls.add(new TrafficPair(ahosts.get(i), bhosts.get(j)));
    		}
  		}
  		return ls;
	}


	public ArrayList TrafficGenLocalManyToMany (int hostPerPod) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();

		ArrayList<Integer> hosts = new ArrayList<Integer> ();
		int t;
		boolean f;
		int i,j;

		int c = 20;

		if (c > hostPerPod)
			c = hostPerPod;
		for (i=0;i<c;i++){
			do {
    			t = (int)(Math.random()*hostPerPod);
    			f = false;
    			for (j=0;j<hosts.size();j++)
					if (hosts.get(j)==t){
						f = true;
						break;
					}
    			}while(f);
    		hosts.add(t);
  		}

		for (i=0;i<hosts.size();i++){
    		for (j=0;j<hosts.size();j++){
    			if (i == j)
    				continue;
    			ls.add(new TrafficPair(hosts.get(i), hosts.get(j)));
    		}
  		}
  		return ls;
	}

	public ArrayList TrafficGenClusterLocalNoLocality(int n, int hostPerPod) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		int clusterNum = totalWeight / n;
		int podNum = totalWeight / hostPerPod;
		System.out.println("totalWeight = " + totalWeight);
		System.out.println("podNum = " + podNum);
		
		/*System.out.println("hostPerPod = " + hostPerPod);
		ArrayList<Integer> potentialHost = new ArrayList<Integer> ();
		for (int i = 0; i < hostPerPod; i++) {
			potentialHost.add(i);
		}*/
		int nextPod = 0;
		ArrayList<Integer> potentialHost = new ArrayList<Integer> ();

		for (int i = 0; i < clusterNum; i++) {
			ArrayList<Integer> curntCluster = new ArrayList<Integer> ();

			while (potentialHost.size() <= n-curntCluster.size()) {
				for (int j = 0; j < potentialHost.size(); j++) {
					curntCluster.add(potentialHost.get(j));
				}
				potentialHost.clear();

				if (nextPod == podNum) {
					break;
				}

				for (int j = nextPod*hostPerPod; j < nextPod*hostPerPod+hostPerPod; j++) {
					potentialHost.add(j);
				}
				nextPod++;
			}

			int pastSize = curntCluster.size();
			for (int j = 0; j < n-pastSize; j++) {
				int randIndex = (int)(Math.random()*potentialHost.size());
				curntCluster.add(potentialHost.get(randIndex));
				potentialHost.remove(randIndex);
			}

			for (int j = 0; j < curntCluster.size(); j++) {
				System.out.print(curntCluster.get(j) + " ");
			}

			for (int j = 0; j < n; j++) {
				for (int t = 0; t < n; t++) {
					ls.add(new TrafficPair(curntCluster.get(j), curntCluster.get(t)));
				}
			}
		}

		for (int j = 0; j < potentialHost.size(); j++) {
			for (int t = 0; t < potentialHost.size(); t++) {
				ls.add(new TrafficPair(potentialHost.get(j), potentialHost.get(t)));
			}
		}

		return ls;
	}

	public ArrayList TrafficGenClusterLocalNoLocalityPartial(int n, int hostPerPod, double percent) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		int clusterNum = (int)((double)totalWeight*percent/(double)n);
		int podNum = totalWeight / hostPerPod;
		System.out.println("clusterNum = " + clusterNum);
		System.out.println("podNum = " + podNum);
		
		/*System.out.println("hostPerPod = " + hostPerPod);
		ArrayList<Integer> potentialHost = new ArrayList<Integer> ();
		for (int i = 0; i < hostPerPod; i++) {
			potentialHost.add(i);
		}*/
		int nextPod = 0;
		ArrayList<Integer> potentialHost = new ArrayList<Integer> ();

		for (int i = 0; i < clusterNum; i++) {
			ArrayList<Integer> curntCluster = new ArrayList<Integer> ();

			while (potentialHost.size() <= n-curntCluster.size()) {
				for (int j = 0; j < potentialHost.size(); j++) {
					curntCluster.add(potentialHost.get(j));
				}
				potentialHost.clear();

				if (nextPod == podNum) {
					break;
				}

				for (int j = nextPod*hostPerPod; j < nextPod*hostPerPod+hostPerPod; j++) {
					potentialHost.add(j);
				}
				nextPod++;
			}

			int pastSize = curntCluster.size();
			for (int j = 0; j < n-pastSize; j++) {
				int randIndex = (int)(Math.random()*potentialHost.size());
				curntCluster.add(potentialHost.get(randIndex));
				potentialHost.remove(randIndex);
			}

			for (int j = 0; j < curntCluster.size(); j++) {
				System.out.print(curntCluster.get(j) + " ");
			}
			System.out.println("\n****************************");

			for (int j = 0; j < n; j++) {
				for (int t = 0; t < n; t++) {
					ls.add(new TrafficPair(curntCluster.get(j), curntCluster.get(t)));
				}
			}
		}

		return ls;
	}

	public ArrayList TrafficGenClusterNoLocality(int n) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		int clusterNum = totalWeight / n;

		ArrayList<Integer> remainingHosts = new ArrayList<Integer> ();
		for (int i = 0; i < totalWeight; i++) {
			remainingHosts.add(i);
		}

		for (int i = 0; i < clusterNum; i++) {
			ArrayList<Integer> curntCluster = new ArrayList<Integer> ();
			for (int j = 0; j < n; j++) {
				int randIndex = (int)(Math.random()*remainingHosts.size());
				curntCluster.add(remainingHosts.get(randIndex));
				remainingHosts.remove(randIndex);
			}

			for (int j = 0; j < n; j++) {
				for (int t = 0; t < n; t++) {
					ls.add(new TrafficPair(curntCluster.get(j), curntCluster.get(t)));
				}
			}
		}


		for (int j = 0; j < remainingHosts.size(); j++) {
			for (int t = 0; t < remainingHosts.size(); t++) {
				ls.add(new TrafficPair(remainingHosts.get(j), remainingHosts.get(t)));
			}
		}

		return ls;
	}

	public ArrayList TrafficGenClusterNoLocalityAllToOne(int n) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		int clusterNum = totalWeight / n;

		ArrayList<Integer> remainingHosts = new ArrayList<Integer> ();
		for (int i = 0; i < totalWeight; i++) {
			remainingHosts.add(i);
		}

		for (int i = 0; i < clusterNum; i++) {
			ArrayList<Integer> curntCluster = new ArrayList<Integer> ();
			for (int j = 0; j < n; j++) {
				int randIndex = (int)(Math.random()*remainingHosts.size());
				curntCluster.add(remainingHosts.get(randIndex));
				remainingHosts.remove(randIndex);
			}

			int targetIndex = rand.nextInt(n);
			for (int j = 0; j < n; j++) {
				ls.add(new TrafficPair(curntCluster.get(j), curntCluster.get(targetIndex)));
				//System.out.println(curntCluster.get(j) + " --> " + curntCluster.get(targetIndex));
			}
		}

		if (!remainingHosts.isEmpty()) {
			int targetIndex = rand.nextInt(remainingHosts.size());
			for (int j = 0; j < remainingHosts.size(); j++) {
				ls.add(new TrafficPair(remainingHosts.get(j), remainingHosts.get(targetIndex)));
				//System.out.println(remainingHosts.get(j) + " --> " + remainingHosts.get(targetIndex));
			}
		}

		return ls;
	}

	public ArrayList TrafficGenHotspot(int n) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();

		int hosts_per_hotspot = n;

		boolean[] is_dest = new boolean[totalWeight];
		boolean[] is_done = new boolean[totalWeight];

		for (int i=0;i<totalWeight;i++){
	    	is_dest[i] = false;
	    	is_done[i] = false;
		}

		int first, src;
		int count = 1;
		for (int k=0;k<count;k++){
			do {
				first = (int)(Math.random()*totalWeight);
	    	}
	    	while (is_dest[first]);
	    
	    	is_dest[first] = true;
	    
		    for (int i=0;i<hosts_per_hotspot;i++){
		    	do{
		        	if (hosts_per_hotspot==totalWeight)
		            	src = i;
		        	else
		        		src = (int)(Math.random()*totalWeight);
		    	}
		    	while(is_done[src]);
		    	is_done[src]=true;

		    	ls.add(new TrafficPair(src, first));
		    	is_done[src] = true;
		    } 
		}
		return ls; 
	}
	
	public ArrayList TrafficGenLocalHotspot(int n) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();

		int hosts_per_hotspot = 100;
		int hosts_per_pod = n;

	    if (hosts_per_hotspot > hosts_per_pod)
	        hosts_per_hotspot = hosts_per_pod;
	    
	    boolean[] is_dest = new boolean[hosts_per_pod];
	    boolean[] is_done = new boolean[hosts_per_pod];

	    for (int i=0;i<hosts_per_pod;i++){
	        is_dest[i] = false;
	        is_done[i] = false;
	    }
	    
	    int first, src;
	    int count = 1;
	    for (int k=0;k<count;k++){
	        do {
	        	first = (int)(Math.random()*hosts_per_pod);
	        }
	        while (is_dest[first]);
	        
	        is_dest[first] = true;
	        
	        for (int i=0;i<hosts_per_hotspot;i++){
	            do{
	                if (hosts_per_hotspot==hosts_per_pod)
	                    src = i;
	                else
	                	src = (int)(Math.random()*hosts_per_pod);
	            }
	            while(is_done[src]);
	            is_done[src]=true;

	            ls.add(new TrafficPair(src, first));
	            
	            is_done[src] = true;
	        }  
	    }
	    return ls;
	}

	public ArrayList TrafficGenClusterAllToOne(int n) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		int clusterNum = totalWeight / n;

		for (int i = 0; i < clusterNum; i++) {
			int target = rand.nextInt(n);
			for (int j = i*n; j < i*n+n; j++) {
				ls.add(new TrafficPair(j, target+i*n));
				//System.out.println(j + " --> " + (target+i*n));
			}
			//System.out.println("%%%%%%%%%%%%%%%%%%%");
		}

		if (totalWeight > clusterNum*n) {
			int target = rand.nextInt(totalWeight-clusterNum*n);
			for (int j = clusterNum*n; j < totalWeight; j++) {
				ls.add(new TrafficPair(j, target+clusterNum*n));
				//System.out.println(j + " --> " + (target+clusterNum*n));
			}
			//System.out.println("%%%%%%%%%%%%%%%%%%%");
		}
			
		return ls;
	}

	public ArrayList TrafficGenPartialCluster(int n, int svrNum) {
		ArrayList<TrafficPair> ls = new ArrayList<TrafficPair>();
		int startIndex = totalWeight - svrNum;
		int clusterNum = svrNum / n;

		for (int i = 0; i < clusterNum; i++) {
			for (int j = i*n; j < i*n+n; j++) {
				for (int t = i*n; t < i*n+n; t++) {
					ls.add(new TrafficPair((j+startIndex), (t+startIndex)));
				}
			}
		}

		for (int j = clusterNum*n; j < svrNum; j++) {
			for (int t = clusterNum*n; t < svrNum; t++) {
				ls.add(new TrafficPair((j+startIndex), (t+startIndex)));
			}
		}

		return ls;
	}

	public ArrayList RandomPermutationPairs(int size)
	{
		int screw;
		ArrayList<TrafficPair> ls;
		do
		{
			screw=0;
			ls=new ArrayList<TrafficPair>();
			for(int i=0; i<size; i++)
			{
				ls.add(new TrafficPair(i, i));
			}
			for(int i=0; i<size; i++)
			{
				int ok=0;
				int k=0;
				int cnt=0;
				do
				{
					//choose a shift in [0,size-1-i]
					k = rand.nextInt(size-i);
					//check if we should swap i and i+k
					int r;
					if(svrToSwitch(i) != svrToSwitch(ls.get(i+k).to))
					{
						ok = 1;
					}
					//System.out.println(i + " " + ls.get(i+k) + " " + i/serverport + " " + ls.get(i+k)/serverport);
					cnt++;
					if(cnt>500)
					{
						screw=1;
						ok=1;
					}
				}
				while(ok==0);
				//swap i's value and i+k's value
				int buf=ls.get(i).to;
				ls.set(i, new TrafficPair(i, ls.get(i+k).to));
				ls.set(i+k, new TrafficPair(i+k, buf));
			}
		}
		while(screw==1);
		System.out.println("PERM FLOWS = " + ls.size());
		return ls;
	}

	private class FlowID
	{
		public int flowID;
		public int srcSwitch;
		public int dstSwitch;
		public FlowID(int fid, int s, int d)
		{
			flowID = fid;
			srcSwitch = s;
			dstSwitch = d;
		}
	}

	// If slack is set to 3, flows can not deviate more than 2 hops from their shortest path distance
	private boolean isFlowZero(FlowID flowID, int linkFrom, int linkTo)
	{
		int SLACK = 4;
		int srcSw = flowID.srcSwitch;
		int destSw = flowID.dstSwitch;

		if((shortestPathLen[srcSw][linkFrom] + 1 + shortestPathLen[linkTo][destSw]) >= (shortestPathLen[srcSw][destSw] + SLACK))
			return true;
		else
			return false;
	}

	// Uses a path-length based heuristic to determine that a certain flow on a certain link will be 0, so no need to factor in LP etc.
	public void PrintGraphforMCFFairCondensed(String filename, int trafficmode, int para)
	{

		modifiedFloydWarshall();
		int r=noNodes; //# of ToR switches
		int svrs=totalWeight;

		int nflowlet = 1;
		try
		{

			// traffic-mode: 0 = randPerm; 1 = all-all; 2 = all-to-one; Any higher n means stride(n)
			ArrayList<TrafficPair> rndmap;
			if (trafficmode == 0) rndmap = RandomPermutationPairs(svrs);
			else if (trafficmode == 1) rndmap = TrafficGenAllAll();
			else if (trafficmode == 2) rndmap = TrafficGenAllToOne();
			else if (trafficmode == 3) rndmap = TrafficGenCluster(para);
			else if (trafficmode == 4) rndmap = TrafficGenPartialAllToOne(para);
			else if (trafficmode == 5) rndmap = TrafficGenPartialCluster(20, para);
			else if (trafficmode == 6) rndmap = TrafficGenClusterAllToOne(para);
			else if (trafficmode == 7) rndmap = TrafficGenClusterNoLocality(para);
			else if (trafficmode == 8) rndmap = TrafficGenClusterNoLocalityAllToOne(para);
			else if (trafficmode == 9) rndmap = TrafficGenClusterLocalNoLocality(20, para);
			else if (trafficmode == 10) rndmap = TrafficGenClusterLocalNoLocalityPartial(20, para, 0.2);
			else if (trafficmode == 11) rndmap = TrafficGenStride(para);
			else if (trafficmode == 12) rndmap = TrafficDefinedFlow();
			else if (trafficmode == 13) rndmap = TrafficGenHotspot(para);
			else if (trafficmode == 14) rndmap = TrafficGenLocalHotspot(para);
			else if (trafficmode == 15) rndmap = TrafficGenManyToMany(para);
			else if (trafficmode == 16) rndmap = TrafficGenLocalManyToMany(para);
			else rndmap = TrafficGenStride(para);

			//[wdm-09-26] output could be enourmous suppress it for now
			/*
			for (int i = 0; i < rndmap.size(); i++) {
				TrafficPair curnt = rndmap.get(i);
				System.out.println("FLOW " + curnt.from + "-->" + curnt.to);
			}*/
			
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);

			// Nodes
			int numEdges = 0;
			for(int i=0; i<noNodes; i++)
			{
				numEdges += adjacencyList[i].size();
			}

			// Edges
			int edgeID = 0;
			int edgeCapacity = 1;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					int to = adjacencyList[i].elementAt(j).intValue();
					edgeID++;
				}
			}

			System.out.println("Number of links " + edgeID/2);

			for (int i = 0; i < rndmap.size(); i++) {
				int from = rndmap.get(i).from;
				int to=rndmap.get(i).to;

				int fromsw = svrToSwitch(from);
				int tosw = svrToSwitch(to);

				// I'm counting only number of connections
				if (fromsw == tosw) continue;
				//System.out.println("fromsw = " + fromsw + " tosw = " + tosw);
				if (switchLevelMatrix[fromsw][tosw] == 0) nCommodities ++;
				switchLevelMatrix[fromsw][tosw] ++;
			}


			// Commodities
			int commodityIndex = 0;
			for (int f = 0; f < noNodes; f ++)
				for (int t = 0; t < noNodes; t++)
					if (switchLevelMatrix[f][t] != 0) commodityIndex ++;


			int numFlows = 0;
			for (int f = 0; f < noNodes; f++)
				for (int t = 0; t < noNodes; t++)
					if(switchLevelMatrix[f][t]>0)
						numFlows++;

			System.out.println(filename + " ***************************** ");

			FlowID[] allFlowIDs = new FlowID[numFlows];
			int curfID=0;
			//Writer output1 = new BufferedWriter(new FileWriter("flowIDmap" + file_index));
			
			for (int f = 0; f < noNodes; f++)
				for (int t = 0; t < noNodes; t++)
					if(switchLevelMatrix[f][t]>0)
					{
						allFlowIDs[curfID] = new FlowID(curfID, f, t);
						//output1.write(curfID + " " + f + " " + t + "\n");
						curfID++;
					}
			
			for (int f = 0; f < noNodes; f++)
				for (int j=0; j<adjacencyList[f].size(); j++) {  //for each out link of f = (f,j)
					String lType = "";

					if (adjacencyList[f].elementAt(j).linkcapacity > 1) lType = "H-H";
					else lType += adjacencyList[f].size() + "-" + adjacencyList[adjacencyList[f].elementAt(j).intValue()].size();
				
					//output2.write(f + "_" + adjacencyList[f].elementAt(j).intValue() + " " + adjacencyList[f].elementAt(j).linkcapacity + " " + adjacencyList[f].size() + " " + lType + "\n");
				}
			boolean fair = true;
			int fid=0;
			String constraint = "";
			if (fair) {
				//< Objective
				out.write("Maximize \n");
				out.write("obj: ");
				String objective = "K";

				// To make CPLEX not fill pipes as freely as it does while keeping the optimal value same
				// Simple idea: For each utilization of capacity, subtract a tiny amount from the objective. 
				// This forces CPLEX to keep the main 'K' part as large as possible, while avoiding wastage of capacity
				/*for (int f = 0; f < noNodes; f++)
				  for (int j=0; j<adjacencyList[f].size(); j++) {  //for each out link of f = (f,j)
				  for (int fid = 0; fid < numFlows; fid ++) {
				  if (!isFlowZero(allFlowIDs[fid], f, adjacencyList[f].elementAt(j).intValue())) {
				  double normalized_factor = 0.00000001 / adjacencyList[f].elementAt(j).linkcapacity;
				  objective += " -" + (new BigDecimal(Double.toString(normalized_factor))).toPlainString() + "f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue(); 
				  }
				  }
				  }*/

				out.write(objective);


				//<Constraints of Type 0: fairness i.e. flow >= K
				out.write("\n\nSUBJECT TO \n\\Type 0: Flow >= K\n");
				System.out.println(new Date() + ": Starting part 0");
				for (int f = 0; f < noNodes; f++)
				{
					for (int t = 0; t < noNodes; t++)
					{
						if(switchLevelMatrix[f][t]>0)	  //for each flow fid with source f
						{
							constraint = "c0_" + fid + ": ";
							//System.out.println("CHECK FLOW =========== " + fid + " " + f + " " + t);

							int writeCons = 0;
							for(int j=0; j<adjacencyList[f].size(); j++)   //for each out link of f = (f,j)
							{
								if (!isFlowZero(allFlowIDs[fid], f, adjacencyList[f].elementAt(j).intValue()))
								{
									constraint += "-f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue() + " ";
									writeCons = 1;
								}
								//if(j!=adjacencyList[f].size()-1) constraint += "- ";
							}
							if (writeCons == 1)
							{
								constraint += " + " + switchLevelMatrix[f][t] + " K <= 0\n";
								out.write(constraint);
							}
							fid++;
						}
					}
				}
				//>
			}

			//>
			else { // no fairness constraints -- max total throughput
				//< Objective
                out.write("Maximize \n");
                out.write("obj: ");
				fid = 0;
                String objective = "";
                for (int f = 0; f < noNodes; f++) {
                    for (int t = 0; t < noNodes; t++) {
                        if(switchLevelMatrix[f][t]>0)   { //for each flow fid with source f
                            for(int j=0; j<adjacencyList[f].size(); j++) { //for each out link of f = (f,j)
                                objective += "f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue() + " ";
                                if(j!=adjacencyList[f].size()-1)
                                    objective += "+ ";
                            }
                            if(fid != commodityIndex-1)
                                objective += "+ ";
                            else
                                objective += "\n";
                            fid++;
                        }
                    }
                }
                out.write(objective);
				out.write("\n\nSUBJECT TO \n\\Type 0: Flow >= K\n");
				System.out.println(new Date() + ": Starting part 0");
			}

			//<Constraints of Type 1: Load on link <= max_load
			out.write("\n\\Type 1: Load on link <= max_load\n");
			System.out.println(new Date() + ": Starting part 1");
			constraint = "";
			int strCapacity = 25*commodityIndex;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					StringBuilder curConstraint = new StringBuilder(strCapacity);
					int writeCons = 0;
					for(int fd_=0; fd_<commodityIndex; fd_++)
					{
						//for each flow fd_
						if (!isFlowZero(allFlowIDs[fd_], i, adjacencyList[i].elementAt(j).intValue()))
						{
							//constraint += "f_" + fd_ + "_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + " + ";
							curConstraint.append("f_" + fd_ + "_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + " + ");
							writeCons = 1;
						}
					}
					constraint = curConstraint.toString();
					if(constraint.endsWith("+ "))
						constraint = constraint.substring(0, constraint.lastIndexOf("+")-1);
					//System.out.println("string size: "+constraint.length());
					if(writeCons == 1)
					{
						out.write("c1_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + ": " + constraint + " <= " +  adjacencyList[i].elementAt(j).linkcapacity + "\n");
						//constraint = "";
					}
				}
				if(i > 0 && i % 50 == 0)
					System.out.println(new Date() + ": "+i+" of "+noNodes+" done");
			}
			//>

			//<Constraints of Type 2: Flow conservation at non-source, non-destination
			int LARGE_VALUE = 20;		// TOPO_COMPARISON
			System.out.println(new Date() + ": Starting part 2");
			out.write("\n\\Type 2: Flow conservation at non-source, non-destination\n");
			fid=0;
			for (int f = 0; f < noNodes; f++)
			{
				for (int t = 0; t < noNodes; t++)
				{
					if(switchLevelMatrix[f][t]>0)	   //for each flow fid
					{
						for(int u=0; u<noNodes; u++)   //for each node u
						{
							constraint = "";
							int writeCons = 0;
							if(u==f)    //src
							{
								constraint = "c2_" + fid + "_" + u + "_1: ";

								for(int j=0; j<adjacencyList[u].size(); j++)   //for each out link of u = (u,j)
								{
									if (!isFlowZero(allFlowIDs[fid], u, adjacencyList[u].elementAt(j).intValue()))
									{
										constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								if (writeCons == 1) out.write(constraint + " <= " + switchLevelMatrix[f][t]*LARGE_VALUE + "\n");
								writeCons = 0;
								constraint = "c2_" + fid + "_" + u + "_2: ";
								for(int j=0; j<adjacencyList[u].size(); j++)   //for each in link of u = (j,u)
								{
									if (!isFlowZero(allFlowIDs[fid], adjacencyList[u].elementAt(j).intValue(), u))
									{
										constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								if (writeCons == 1) out.write(constraint + " = 0\n");
							}
							else if (u==t) {}
							else  // non-src and non-dest
							{
								constraint = "c2_" + fid + "_" + u + "_3: ";
								for(int j=0; j<adjacencyList[u].size(); j++)   //for each out link of u = (u,j)
								{
									if (!isFlowZero(allFlowIDs[fid], u, adjacencyList[u].elementAt(j).intValue()))
									{
										constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								constraint += " - ";

								for(int j=0; j<adjacencyList[u].size(); j++)   //for each in link of u = (j,u)
								{
									if (!isFlowZero(allFlowIDs[fid], adjacencyList[u].elementAt(j).intValue(), u))
									{
										constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " - ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("- ")) constraint = constraint.substring(0, constraint.lastIndexOf("-")-1 );
								if (writeCons == 1) out.write(constraint + " = 0\n");
							}
						}
						fid++;
					}
				}
				if(f > 0 && f % 50 == 0)
					System.out.println(new Date() + ": "+f+" of "+noNodes+" done");
			}
			
			out.write("End\n");
			out.close();
		}
		catch (Exception e)
		{
			System.err.println("PrintGraphforMCFFairCondensed Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void PrintGraphforMCFFairCondensedAverage(String filename, int trafficmode, int para)
	{

		modifiedFloydWarshall();
		int r= noNodes; //# of ToR switches
		int svrs=totalWeight;

		int nflowlet = 1;
		try
		{

			// traffic-mode: 0 = randPerm; 1 = all-all; 2 = all-to-one; Any higher n means stride(n)
			ArrayList<TrafficPair> rndmap;
			if (trafficmode == 0) rndmap = RandomPermutationPairs(svrs);
			else if (trafficmode == 1) rndmap = TrafficGenAllAll();
			else if (trafficmode == 2) rndmap = TrafficGenAllToOne();
			else if (trafficmode == 3) rndmap = TrafficGenCluster(para);
			else if (trafficmode == 4) rndmap = TrafficGenPartialAllToOne(para);
			else if (trafficmode == 5) rndmap = TrafficGenPartialCluster(20, para);
			else if (trafficmode == 6) rndmap = TrafficGenClusterAllToOne(para);
			else if (trafficmode == 7) rndmap = TrafficGenClusterNoLocality(para);
			else if (trafficmode == 8) rndmap = TrafficGenClusterNoLocalityAllToOne(para);
			else if (trafficmode == 9) rndmap = TrafficGenClusterLocalNoLocality(20, para);
			else if (trafficmode == 10) rndmap = TrafficGenClusterLocalNoLocalityPartial(20, para, 0.2);
			else if (trafficmode == 11) rndmap = TrafficGenStride(para);
			else if (trafficmode == 12) rndmap = TrafficDefinedFlow();
			else if (trafficmode == 13) rndmap = TrafficGenHotspot(para);
			else if (trafficmode == 14) rndmap = TrafficGenLocalHotspot(para);
			else if (trafficmode == 15) rndmap = TrafficGenManyToMany(para);
			else if (trafficmode == 16) rndmap = TrafficGenLocalManyToMany(para);
			else rndmap = TrafficGenStride(para);

			/* [wdm-09-26]The output could be enormous, suppress it for now*/
			/*
			for (int i = 0; i < rndmap.size(); i++) {
				TrafficPair curnt = rndmap.get(i);
				System.out.println("FLOW " + curnt.from + "-->" + curnt.to);
			}*/
			
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);

			System.out.println(filename + " ***************************** ");
			// Nodes
			int numEdges = 0;
			for(int i=0; i<noNodes; i++)
			{
				numEdges += adjacencyList[i].size();
			}

			// Edges
			int edgeID = 0;
			int edgeCapacity = 1;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					int to = adjacencyList[i].elementAt(j).intValue();
					edgeID++;
				}
			}

			System.out.println("Number of links " + edgeID/2);


			for (int i = 0; i < rndmap.size(); i++) {
				int from = rndmap.get(i).from;
				int to=rndmap.get(i).to;

				int fromsw=svrToSwitch(from);
				int tosw=svrToSwitch(to);

				// I'm counting only number of connections
				if (fromsw == tosw) continue;
				//System.out.println("fromsw = " + fromsw + " tosw = " + tosw);
				if (switchLevelMatrix[fromsw][tosw] == 0) nCommodities ++;
				switchLevelMatrix[fromsw][tosw] ++;
			}


			// Commodities
			int commodityIndex = 0;
			for (int f = 0; f < noNodes; f ++)
				for (int t = 0; t < noNodes; t++)
					if (switchLevelMatrix[f][t] != 0) commodityIndex ++;


			int numFlows = 0;
			for (int f = 0; f < noNodes; f++)
				for (int t = 0; t < noNodes; t++)
					if(switchLevelMatrix[f][t]>0)
						numFlows++;
			FlowID[] allFlowIDs = new FlowID[numFlows];
			int curfID=0;
			
			for (int f = 0; f < noNodes; f++)
				for (int t = 0; t < noNodes; t++)
					if(switchLevelMatrix[f][t]>0)
					{
						allFlowIDs[curfID] = new FlowID(curfID, f, t);
						//output1.write(curfID + " " + f + " " + t + "\n");
						curfID++;
					}
			
			for (int f = 0; f < noNodes; f++)
				for (int j=0; j<adjacencyList[f].size(); j++) {  //for each out link of f = (f,j)
					String lType = "";

					if (adjacencyList[f].elementAt(j).linkcapacity > 1) lType = "H-H";
					else lType += adjacencyList[f].size() + "-" + adjacencyList[adjacencyList[f].elementAt(j).intValue()].size();
				
					//output2.write(f + "_" + adjacencyList[f].elementAt(j).intValue() + " " + adjacencyList[f].elementAt(j).linkcapacity + " " + adjacencyList[f].size() + " " + lType + "\n");
				}
			//output2.close();

			boolean fair = false;
			//boolean fair = true;
			int fid=0;
			String constraint = "";
			if (fair) {
				//< Objective
				out.write("Maximize \n");
				out.write("obj: ");
				String objective = "K";
				out.write(objective);
				//<Constraints of Type 0: fairness i.e. flow >= K
				out.write("\n\nSUBJECT TO \n");

				out.write("cxx: ");
				for (int fn = 0; fn < numFlows; fn++) {
					out.write("-K" + fn + " ");
				}
				out.write("+ " + numFlows + " K <= 0\n");

				objective += ")/" + numFlows;

				out.write("\\Type 0: Flow >= K\n");
				System.out.println(new Date() + ": Starting part 0");
				for (int f = 0; f < noNodes; f++)
				{
					for (int t = 0; t < noNodes; t++)
					{
						if(switchLevelMatrix[f][t]>0)	  //for each flow fid with source f
						{
							constraint = "c0_" + fid + ": ";
							//System.out.println("CHECK FLOW =========== " + fid + " " + f + " " + t);

							int writeCons = 0;
							for(int j=0; j<adjacencyList[f].size(); j++)   //for each out link of f = (f,j)
							{
								if (!isFlowZero(allFlowIDs[fid], f, adjacencyList[f].elementAt(j).intValue()))
								{
									constraint += "-f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue() + " ";
									writeCons = 1;
								}
								//if(j!=adjacencyList[f].size()-1) constraint += "- ";
							}
							if (writeCons == 1)
							{
								constraint += " + " + switchLevelMatrix[f][t] + " K" + fid + " <= 0\n";
								out.write(constraint);
							}
							fid++;
						}
					}
				}
				//>
			}

			//>
			else { // no fairness constraints -- max total throughput
				//< Objective
                out.write("Maximize \n");
                out.write("obj: ");
				fid = 0;
                String objective = "";
                for (int f = 0; f < noNodes; f++) {
                    for (int t = 0; t < noNodes; t++) {
                        if(switchLevelMatrix[f][t]>0)   { //for each flow fid with source f
                            for(int j=0; j<adjacencyList[f].size(); j++) { //for each out link of f = (f,j)
                                objective += "f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue() + " ";
                                if(j!=adjacencyList[f].size()-1)
                                    objective += "+ ";
                            }
                            if(fid != commodityIndex-1)
                                objective += "+ ";
                            else
                                objective += "\n";
                            fid++;
                        }
                    }
                }
                out.write(objective);
				out.write("\n\nSUBJECT TO \n\\Type 0: Flow >= K\n");
	                        //>
			}

			//<Constraints of Type 1: Load on link <= max_load
			out.write("\n\\Type 1: Load on link <= max_load\n");
			System.out.println(new Date() + ": Starting part 1");
			constraint = "";
			int strCapacity = 25*commodityIndex;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					StringBuilder curConstraint = new StringBuilder(strCapacity);
					int writeCons = 0;
					for(int fd_=0; fd_<commodityIndex; fd_++)
					{
						//for each flow fd_
						if (!isFlowZero(allFlowIDs[fd_], i, adjacencyList[i].elementAt(j).intValue()))
						{
							//constraint += "f_" + fd_ + "_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + " + ";
							curConstraint.append("f_" + fd_ + "_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + " + ");
							writeCons = 1;
						}
					}
					constraint = curConstraint.toString();
					if(constraint.endsWith("+ "))
						constraint = constraint.substring(0, constraint.lastIndexOf("+")-1);
					//System.out.println("string size: "+constraint.length());
					if(writeCons == 1)
					{
						out.write("c1_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + ": " + constraint + " <= " +  adjacencyList[i].elementAt(j).linkcapacity + "\n");
						//constraint = "";
					}
				}
				if(i > 0 && i % 50 == 0)
					System.out.println(new Date() + ": "+i+" of "+noNodes+" done");
			}
			//>

			//<Constraints of Type 2: Flow conservation at non-source, non-destination

			//[wdm09-22 should be a large value (e.g., >20) to saturate the network]
			int LARGE_VALUE = 20;		// TOPO_COMPARISON
			System.out.println(new Date() + ": Starting part 2");
			out.write("\n\\Type 2: Flow conservation at non-source, non-destination\n");
			fid=0;
			for (int f = 0; f < noNodes; f++)
			{
				for (int t = 0; t < noNodes; t++)
				{
					if(switchLevelMatrix[f][t]>0)	   //for each flow fid
					{
						for(int u=0; u<noNodes; u++)   //for each node u
						{
							constraint = "";
							int writeCons = 0;
							if(u==f)    //src
							{
								constraint = "c2_" + fid + "_" + u + "_1: ";

								for(int j=0; j<adjacencyList[u].size(); j++)   //for each out link of u = (u,j)
								{
									if (!isFlowZero(allFlowIDs[fid], u, adjacencyList[u].elementAt(j).intValue()))
									{
										constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								if (writeCons == 1) out.write(constraint + " <= " + switchLevelMatrix[f][t]*LARGE_VALUE + "\n");
								writeCons = 0;
								constraint = "c2_" + fid + "_" + u + "_2: ";
								for(int j=0; j<adjacencyList[u].size(); j++)   //for each in link of u = (j,u)
								{
									if (!isFlowZero(allFlowIDs[fid], adjacencyList[u].elementAt(j).intValue(), u))
									{
										constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								if (writeCons == 1) out.write(constraint + " = 0\n");
							}
							else if (u==t) {}
							else  // non-src and non-dest
							{
								constraint = "c2_" + fid + "_" + u + "_3: ";
								for(int j=0; j<adjacencyList[u].size(); j++)   //for each out link of u = (u,j)
								{
									if (!isFlowZero(allFlowIDs[fid], u, adjacencyList[u].elementAt(j).intValue()))
									{
										constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								constraint += " - ";

								for(int j=0; j<adjacencyList[u].size(); j++)   //for each in link of u = (j,u)
								{
									if (!isFlowZero(allFlowIDs[fid], adjacencyList[u].elementAt(j).intValue(), u))
									{
										constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " - ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("- ")) constraint = constraint.substring(0, constraint.lastIndexOf("-")-1 );
								if (writeCons == 1) out.write(constraint + " = 0\n");
							}
						}
						fid++;
					}
				}
				if(f > 0 && f % 50 == 0)
					System.out.println(new Date() + ": "+f+" of "+noNodes+" done");
			}


			out.write("End\n");
			out.close();
		}
		catch (Exception e)
		{
			System.err.println("PrintGraphforMCFFairCondensed Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Uses a path-length based heuristic to determine that a certain flow on a certain link will be 0, so no need to factor in LP etc.
	public void PrintGraphforMCFFairCondensedNew(String filename, int trafficmode)
	{
		modifiedFloydWarshall();
		int r=noNodes; //# of ToR switches
		int svrs=totalWeight;

		int nflowlet = 1;
		try
		{

			// traffic-mode: 0 = randPerm; 1 = all-all; 2 = all-to-one; Any higher n means stride(n)
			ArrayList<TrafficPair> rndmap;
			if (trafficmode == 0) rndmap = RandomPermutationPairs(svrs);
			else if (trafficmode == 1) rndmap = TrafficGenAllAll();
			else if (trafficmode == 2) rndmap = TrafficGenAllToOne();
			else rndmap = TrafficGenStride(trafficmode);

			//[wdm-09-26] output could be enormous
			/*
			for (int i = 0; i < rndmap.size(); i++) {
				TrafficPair curnt = rndmap.get(i);
				System.out.println("FLOW " + curnt.from + "-->" + curnt.to);
			}*/
			
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);

			// Nodes
			int numEdges = 0;
			for(int i=0; i<noNodes; i++)
			{
				numEdges += adjacencyList[i].size();
			}

			// Edges
			int edgeID = 0;
			int edgeCapacity = 1;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					int to = adjacencyList[i].elementAt(j).intValue();
					edgeID++;
				}
			}

			for (int i = 0; i < rndmap.size(); i++) {
				int from = rndmap.get(i).from;
				int to=rndmap.get(i).to;

				int fromsw=svrToSwitch(from);
				int tosw=svrToSwitch(to);

				// I'm counting only number of connections
				if (fromsw == tosw) continue;
				if (switchLevelMatrix[fromsw][tosw] == 0) nCommodities ++;
				switchLevelMatrix[fromsw][tosw] ++;
			}


			// Commodities
			int commodityIndex = 0;
			for (int f = 0; f < noNodes; f ++)
				for (int t = 0; t < noNodes; t++)
					if (switchLevelMatrix[f][t] != 0) commodityIndex ++;


			int numFlows = 0;
			for (int f = 0; f < noNodes; f++)
				for (int t = 0; t < noNodes; t++)
					if(switchLevelMatrix[f][t]>0)
						numFlows++;

			String file_index = filename.substring(3); 
			file_index = file_index.substring(0, file_index.length() - 4);

			System.out.println(file_index + " ***************************** ");

			FlowID[] allFlowIDs = new FlowID[numFlows];
			int curfID=0;
			//Writer output1 = new BufferedWriter(new FileWriter("flowIDmap" + file_index));
			
			for (int f = 0; f < noNodes; f++)
				for (int t = 0; t < noNodes; t++)
					if(switchLevelMatrix[f][t]>0)
					{
						allFlowIDs[curfID] = new FlowID(curfID, f, t);
						//output1.write(curfID + " " + f + " " + t + "\n");
						curfID++;
					}
			//output1.close();

			//Writer output2 = new BufferedWriter(new FileWriter("linkCaps" + file_index));
			
			for (int f = 0; f < noNodes; f++)
				for (int j=0; j<adjacencyList[f].size(); j++) {  //for each out link of f = (f,j)
					String lType = "";

					if (adjacencyList[f].elementAt(j).linkcapacity > 1) lType = "H-H";
					else lType += adjacencyList[f].size() + "-" + adjacencyList[adjacencyList[f].elementAt(j).intValue()].size();
				
					//output2.write(f + "_" + adjacencyList[f].elementAt(j).intValue() + " " + adjacencyList[f].elementAt(j).linkcapacity + " " + adjacencyList[f].size() + " " + lType + "\n");
				}
			//output2.close();

			//boolean fair = false;
			boolean fair = true;
			int fid=0;
			String constraint = "";
			if (fair) {
				//< Objective
				out.write("Maximize \n");
				out.write("obj: ");
				String objective = "K";

				// To make CPLEX not fill pipes as freely as it does while keeping the optimal value same
				// Simple idea: For each utilization of capacity, subtract a tiny amount from the objective. 
				// This forces CPLEX to keep the main 'K' part as large as possible, while avoiding wastage of capacity
				/*for (int f = 0; f < noNodes; f++)
				  for (int j=0; j<adjacencyList[f].size(); j++) {  //for each out link of f = (f,j)
				  for (int fid = 0; fid < numFlows; fid ++) {
				  if (!isFlowZero(allFlowIDs[fid], f, adjacencyList[f].elementAt(j).intValue())) {
				  double normalized_factor = 0.00000001 / adjacencyList[f].elementAt(j).linkcapacity;
				  objective += " -" + (new BigDecimal(Double.toString(normalized_factor))).toPlainString() + "f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue(); 
				  }
				  }
				  }*/

				out.write(objective);


				//<Constraints of Type 0: fairness i.e. flow >= K
				out.write("\n\nSUBJECT TO \n\\Type 0: Flow >= K\n");
				System.out.println(new Date() + ": Starting part 0");
				for (int f = 0; f < noNodes; f++)
				{
					for (int t = 0; t < noNodes; t++)
					{
						if(switchLevelMatrix[f][t]>0)	  //for each flow fid with source f
						{
							constraint = "c0_" + fid + ": ";
							//System.out.println("CHECK FLOW =========== " + fid + " " + f + " " + t);

							int writeCons = 0;
							for(int j=0; j<adjacencyList[f].size(); j++)   //for each out link of f = (f,j)
							{
								if (!isFlowZero(allFlowIDs[fid], f, adjacencyList[f].elementAt(j).intValue()))
								{
									constraint += "-f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue() + " ";
									writeCons = 1;
								}
								//if(j!=adjacencyList[f].size()-1) constraint += "- ";
							}
							if (writeCons == 1)
							{
								constraint += " + " + switchLevelMatrix[f][t] + " K <= 0\n";
								out.write(constraint);
							}
							fid++;
						}
					}
				}
				//>
			}

			//>
			else { // no fairness constraints -- max total throughput
				//< Objective
                out.write("Maximize \n");
                out.write("obj: ");
				fid = 0;
                String objective = "";
                for (int f = 0; f < noNodes; f++) {
                    for (int t = 0; t < noNodes; t++) {
                        if(switchLevelMatrix[f][t]>0)   { //for each flow fid with source f
                            for(int j=0; j<adjacencyList[f].size(); j++) { //for each out link of f = (f,j)
                                objective += "f_" + fid + "_" + f + "_" + adjacencyList[f].elementAt(j).intValue() + " ";
                                if(j!=adjacencyList[f].size()-1)
                                    objective += "+ ";
                            }
                            if(fid != commodityIndex-1)
                                objective += "+ ";
                            else
                                objective += "\n";
                            fid++;
                        }
                    }
                }
                out.write(objective);
				out.write("\n\nSUBJECT TO \n\\Type 0: Flow >= K\n");
	                        //>
			}

			//<Constraints of Type 1: Load on link <= max_load
			out.write("\n\\Type 1: Load on link <= max_load\n");
			System.out.println(new Date() + ": Starting part 1");
			constraint = "";
			int strCapacity = 25*commodityIndex;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					StringBuilder curConstraint = new StringBuilder(strCapacity);
					int writeCons = 0;
					for(int fd_=0; fd_<commodityIndex; fd_++)
					{
						//for each flow fd_
						if (!isFlowZero(allFlowIDs[fd_], i, adjacencyList[i].elementAt(j).intValue()))
						{
							//constraint += "f_" + fd_ + "_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + " + ";
							curConstraint.append("f_" + fd_ + "_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + " + ");
							writeCons = 1;
						}
					}
					constraint = curConstraint.toString();
					if(constraint.endsWith("+ "))
						constraint = constraint.substring(0, constraint.lastIndexOf("+")-1);
					//System.out.println("string size: "+constraint.length());
					if(writeCons == 1)
					{
						out.write("c1_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + ": " + constraint + " <= " +  adjacencyList[i].elementAt(j).linkcapacity + "\n");
						//constraint = "";
					}
				}
				if(i > 0 && i % 20 == 0)
					System.out.println(new Date() + ": "+i+" of "+noNodes+" done");
			}
			//>

			//<Constraints of Type 2: Flow conservation at non-source, non-destination
			int LARGE_VALUE = 1;		// TOPO_COMPARISON
			System.out.println(new Date() + ": Starting part 2");
			out.write("\n\\Type 2: Flow conservation at non-source, non-destination\n");
			fid=0;
			for (int f = 0; f < noNodes; f++)
			{
				for (int t = 0; t < noNodes; t++)
				{
					if(switchLevelMatrix[f][t]>0)	   //for each flow fid
					{
						for(int u=0; u<noNodes; u++)   //for each node u
						{
							constraint = "";
							int writeCons = 0;
							if(u==f)    //src
							{
								constraint = "c2_" + fid + "_" + u + "_1: ";

								for(int j=0; j<adjacencyList[u].size(); j++)   //for each out link of u = (u,j)
								{
									if (!isFlowZero(allFlowIDs[fid], u, adjacencyList[u].elementAt(j).intValue()))
									{
										constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								if (writeCons == 1) out.write(constraint + " <= " + switchLevelMatrix[f][t]*LARGE_VALUE + "\n");
								writeCons = 0;
								constraint = "c2_" + fid + "_" + u + "_2: ";
								for(int j=0; j<adjacencyList[u].size(); j++)   //for each in link of u = (j,u)
								{
									if (!isFlowZero(allFlowIDs[fid], adjacencyList[u].elementAt(j).intValue(), u))
									{
										constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								if (writeCons == 1) out.write(constraint + " = 0\n");
							}
							else if (u==t) {}
							else  // non-src and non-dest
							{
								constraint = "c2_" + fid + "_" + u + "_3: ";
								for(int j=0; j<adjacencyList[u].size(); j++)   //for each out link of u = (u,j)
								{
									if (!isFlowZero(allFlowIDs[fid], u, adjacencyList[u].elementAt(j).intValue()))
									{
										constraint += "f_" + fid + "_" + u + "_" + adjacencyList[u].elementAt(j).intValue() + " + ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("+ ")) constraint = constraint.substring(0, constraint.lastIndexOf("+")-1 );
								constraint += " - ";

								for(int j=0; j<adjacencyList[u].size(); j++)   //for each in link of u = (j,u)
								{
									if (!isFlowZero(allFlowIDs[fid], adjacencyList[u].elementAt(j).intValue(), u))
									{
										constraint += "f_" + fid + "_" + adjacencyList[u].elementAt(j).intValue() + "_" + u + " - ";
										writeCons = 1;
									}
								}
								if (constraint.endsWith("- ")) constraint = constraint.substring(0, constraint.lastIndexOf("-")-1 );
								if (writeCons == 1) out.write(constraint + " = 0\n");
							}
						}
						fid++;
					}
				}
				if(f > 0 && f % 20 == 0)
					System.out.println(new Date() + ": "+f+" of "+noNodes+" done");
			}
			
			out.write("End\n");
			out.close();
		}
		catch (Exception e)
		{
			System.err.println("PrintGraphforMCFFairCondensed Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void PrintSimpleGraph(String filename, int trafficmode, int para)
	{
		modifiedFloydWarshall();
		int r=noNodes; //# of ToR switches
		int svrs=totalWeight;

		switchLevelMatrix = new int[noNodes][noNodes];

		try
		{
			// traffic-mode: 0 = randPerm; 1 = all-all; 2 = all-to-one; Any higher n means stride(n)
			ArrayList<TrafficPair> rndmap;
			if (trafficmode == 0) {rndmap = RandomPermutationPairs(svrs); System.out.println("RAND-PERM TRAFFIC ************");}
			else if (trafficmode == 1) {rndmap = TrafficGenAllAll(); System.out.println("All-all *********************");}
			else if (trafficmode == 2) rndmap = TrafficGenAllToOne();
			else if (trafficmode == 3) rndmap = TrafficGenCluster(para);
			else rndmap = TrafficGenStride(trafficmode);

			
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			
			int numEdges = 0;
			for(int i=0; i<noNodes; i++)
			{
				numEdges += adjacencyList[i].size();
			}	


			int edgeID = 0;
			int edgeCapacity = 1;
			for(int i=0; i<noNodes; i++)
			{
				for(int j=0; j<adjacencyList[i].size(); j++)
				{
					edgeID++;
				}
			}

			System.out.println("Number of edges " + edgeID);
			System.out.println("Map size " + rndmap.size());

			for (int i = 0; i < rndmap.size(); i++) {
				int from = rndmap.get(i).from;
				int to=rndmap.get(i).to;

				int fromsw=svrToSwitch(from);
				int tosw=svrToSwitch(to);

				// I'm counting only number of connections
				//if (switchLevelMatrix[fromsw][tosw] != 0) nCommodities ++;

				if (fromsw == tosw) continue;

				if (switchLevelMatrix[fromsw][tosw] == 0) nCommodities ++;

				switchLevelMatrix[fromsw][tosw] ++;

			}


			int numFlows = 0;
			for (int f = 0; f < noNodes; f++){			
				for (int t = 0; t < noNodes; t++){
					if(switchLevelMatrix[f][t]>0){
						numFlows++;
					}
				}
			}


			int fid=0;
			String constraint = "";

			out.write("Maximize \n");
			out.write("obj: ");
			String objective = "K";
			out.write(objective);

			//Type 0 - Flows >= K 
			out.write("\n\nSUBJECT TO \n\\Type 0: Flow >= K\n");
			System.out.println(new Date() + ": Starting part 0");

			for (int f = 0; f < noNodes; f++){
				for (int t = 0; t < noNodes; t++){
		 			if(switchLevelMatrix[f][t]>0){
						constraint = "c0_" + fid + ": ";
						constraint += "- f_" + f + "_" + t +  " ";
						constraint += " + " + switchLevelMatrix[f][t] + " K <= 0\n";
						out.write(constraint);
						fid++;
			//			System.out.println("fid " + fid + " f " + f + " t " + t + " flows " + switchLevelMatrix[f][t]);
					}
				}
			}


			//Type 1 - sum (l_i_j_d) over d on link i_j less than link capacity
			out.write("\n\\Type 1: Link capacity constraint\n");
            System.out.println(new Date() + ": Starting part 1");

            for(int i=0; i<noNodes; i++){
                for(int j=0; j<adjacencyList[i].size(); j++){
					constraint = "c2_" + i + "_" + adjacencyList[i].elementAt(j).intValue() + ": ";
                    for(int k=0; k<noNodes; k++){
                        constraint += " + l_"+ i + "_" + adjacencyList[i].elementAt(j).intValue() +"_" + k ;
                    }
					constraint += " <= " +  adjacencyList[i].elementAt(j).linkcapacity + "\n";
					out.write(constraint);
                }
            }

			//Type 2 - flow conservation at nodes
			out.write("\n\\Type 2: Flow conservation at node\n");
			System.out.println(new Date() + ": Starting part 2");

            for(int i=0; i<noNodes; i++){
				for(int k=0; k<noNodes; k++){
					if (i!=k){
						constraint = "c3_" + i + "_" + k + ": ";
						if (switchLevelMatrix[i][k] > 0){
							constraint += " f_" + i + "_" + k  ;	
						}

	                    for(int j=0; j<adjacencyList[i].size(); j++){
							constraint += " + l_" + adjacencyList[i].elementAt(j).intValue() +"_" + i +"_" + k + " ";		
						}
	
                        for(int j=0; j<adjacencyList[i].size(); j++){
                            constraint += " - l_" + i +"_" + adjacencyList[i].elementAt(j).intValue() +"_" + k + " ";              
                        }

						constraint+= " = 0\n";
						out.write(constraint);
					}
					else if (i == k) {
						constraint = "c3_" + i + "_" + k + ": ";
						for(int j=0; j<noNodes; j++){
							if(switchLevelMatrix[j][i] > 0){
								constraint += " - f_" + j + "_" + i;
							}
						}
	
                        for(int j=0; j<adjacencyList[i].size(); j++){
                            constraint += " + l_" + adjacencyList[i].elementAt(j).intValue() +"_" + i +"_" + i + " ";      
                        }
                        constraint+= " = 0\n";
                        out.write(constraint);
					}
				}
			}

			out.close();
		}
		catch (Exception e)
		{
			System.err.println("PrintSimpleGraph Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
