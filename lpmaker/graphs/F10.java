package lpmaker.graphs;

/**
 * Created by wdm on 9/22/17.
 */

public class F10 extends Graph {

    public int K;

    public static F10 createF10ByHosts(int hosts){
        double K_ = Math.pow(hosts*4, 1.0/3.0);
        int K_int = (int)K_;
        if(K_int % 2 != 0){
            K_int++;
        }
        System.out.println("K would be "+K_+" rounding to "+K_int+ " leading to "+(K_int*K_int*K_int/4)+" hosts");
        return new F10(K_int);
    }

    public F10(int K_){
        super(K_*K_*5/4);
        this.K = K_;
        populateAdjacencyList();
        name = "F10";
    }
    public F10(int K_, int failureMode, double fail_rate){
        super(K_*K_*5/4);
        this.K = K_;
        populateAdjacencyList();
        name = "F10";
        failLinks(failureMode,fail_rate,K_);
    }

    public F10(int K_, int failureMode, int failLinkCount){
        super(K_*K_*5/4);
        this.K = K_;
        populateAdjacencyList();
        name = "F10";
        int totalLinks = K*K*K/2;
        double failRate = failLinkCount/(double)totalLinks;
        failLinks(failureMode, failRate, K_);
    }

    // +++++++++++ THIS CONSTRUCTION ROUTINE FOR VANILLA FAT TREE +++++++++++++++++++++++++++++++
    private void populateAdjacencyList(){
        //layout

        //0-(K*K/2) lower layer close to hosts
        //(K*K/2) - K*K middle layer
        //K*K - 5/4*K*K core layer

        //connect lower to middle
        for(int pod = 0; pod < K; pod++){
            for(int i = 0; i < K/2; i++){
                for(int l = 0; l < K/2; l++){
                    addBidirNeighbor(pod*K/2+i, K*K/2+pod*K/2+l);
                }
            }
        }

        //connect middle to core
        int coreIndexBase = K*K;
        int aggIndexBase = K*K/2;
        for(int pod = 0; pod < K; pod++){
                for (int aggIndex = 0; aggIndex < K/2; aggIndex++){
                    if(pod % 2 == 0) {
                        //Type A
                        for (int linkIndex = 0; linkIndex < K / 2; linkIndex++)
                            addBidirNeighbor(aggIndexBase + pod * K / 2 + aggIndex, coreIndexBase + aggIndex * K/2 + linkIndex);
                    }
                    else{
                        //Type B
                        for (int linkIndex = 0; linkIndex < K / 2; linkIndex++)
                            addBidirNeighbor(aggIndexBase + pod * K / 2 + aggIndex, coreIndexBase + linkIndex * K/2 + aggIndex);
                    }
                }
        }



        //set weights
        setUpFixWeight(0);
        //int total = 0;
        for(int pod = 0; pod < K; pod++){
            for(int i = 0; i < K/2; i++){
                // For new comparison method, ANKIT changed this to set up arbitrary numbers of terminals!
                weightEachNode[pod*K/2+i] = K/2;
                totalWeight += K/2;
            }
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
        return i / weightEachNode[0];
    }
}
