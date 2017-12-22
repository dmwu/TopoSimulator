package lpmaker.graphs;

/**
 * Created by wdm on 9/25/17.
 * currently we only support three types of AspenTrees
 * (k,n,d,S) ={(8, 4, 4, 32), (16, 4, 8, 128), (24, 4, 12, 288), (32, 4, 16, 512)}
 */
public class AspenTree extends Graph{
        public int K;
        public int N = 4;
        public int D;
        public int S;
        public AspenTree(int K_, int n_, int d_){
            super((int) ( K_*K_/2* (n_- 0.5) ) ); //S must be K*K/2
            this.K = K_;
            this.N = n_;
            this.D = d_;
            this.S = (int) (Math.pow(K, N-1)/(Math.pow(2, N-2) * D));
            populateAdjacencyList();
            name = "AspenTree";
        }

        public AspenTree(int K_, int n_, int d_, int failureMode, int failedCount, boolean nodeFailure){
            super((int) ( K_ * K_ / 2 * (n_- 0.5) ) );
            this.K = K_;
            this.N = n_;
            this.D = d_;
            this.S = (int) (Math.pow(K, N-1)/(Math.pow(2, N-2) * D)); //S must be K*K/2
            populateAdjacencyList();

            int totalLinks = 3*K*K*K/4;
            if(nodeFailure)
                failNodes(failureMode, failedCount, K_);
            else
                failLinks(failureMode, failedCount, K_);

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
            int redPodNum = 2; //formula: S/(K/2)/(K/2);
            int redSwitchNumPerPod =  K*K/4;
            int redIndexBase = K*K;
            int aggIndexBase = K*K/2;

            //connect middle to redundant layer

            for(int pod = 0; pod < K; pod++){
                int mappedRedPodIndex = pod/(K/2);
                for(int aggIndex = 0; aggIndex < K/2; aggIndex++){
                    for(int aggLink = 0; aggLink < K/2; aggLink++)
                        addBidirNeighbor(aggIndexBase + pod*K/2 + aggIndex,
                            redIndexBase + mappedRedPodIndex * redSwitchNumPerPod + aggIndex*K/2 + aggLink);
                }
            }

            int coreIndexBase = K*K*3/2;
            //connect core layer to redundant layer
            for (int coreType = 0; coreType < K/2; coreType++) {
                for (int incore = 0; incore < K/2; incore++)
                    for (int coreLink = 0; coreLink < K; coreLink++) {
                        addBidirNeighbor(coreIndexBase + coreType * K / 2 + incore, redIndexBase + incore + K / 2 * coreLink);
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
                    //weightEachNode[pod*K/2+i] = 5;
                    //totalWeight += 5;
                }
            }
        }


        public int getK(){
            return K;
        }

        public int getNoHosts(){
            return K*K*K/4;
        }

        public int getNoSwitches(){ return K*K*7/4; }

        public int svrToSwitch(int i)	//i is the server index. return the switch index.
        {
            return i / weightEachNode[0];
        }
}


