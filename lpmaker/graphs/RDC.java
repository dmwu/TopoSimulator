package lpmaker.graphs;

public class RDC extends Graph {

    public int numToRs;
    public int numCores;
    public int serversPerToR;
    public int osRatio;
    public int totalServers;
    public int coreNodeId;
    public RDC(int numToRs, int serversPerToR, int osRatio) {
        super(serversPerToR*numToRs + numToRs + 1);
        this.numToRs = numToRs;
        this.serversPerToR = serversPerToR;
        this.numCores = 1;
        this.totalServers = serversPerToR*numToRs;
        this.coreNodeId = totalServers + this.numToRs;
        this.osRatio = osRatio;
        populateAdjacencyList();
        name = "RDC"+osRatio;

    }

    private void populateAdjacencyList(){
        //edge links
        for(int serverIndex = 0; serverIndex < numToRs*serversPerToR; serverIndex++){
            int torNodeId = serverIndex/serversPerToR + totalServers;
            addBidirNeighbor(serverIndex, torNodeId);
        }

        //aggregation links
        int parallelLinksToCore = serversPerToR/osRatio;
        for(int rackIndex = 0; rackIndex < numToRs; rackIndex++){
            int torNodeId = totalServers + rackIndex;
            for(int parallelIndex = 0; parallelIndex < parallelLinksToCore; parallelIndex++){
                addBidirNeighbor(torNodeId, coreNodeId);
            }
        }
        //each server act as a virtual ToR with weight 1 under the LP simulator's setting
        setUpFixWeight(1);
        totalWeight = totalServers;
    }
    public int svrToSwitch(int i)	//i is the server index. return the switch index.
    {
        return i / weightEachNode[0]; //all switch node has weight 1
    }
}


