import networkx as nx
import nxmetis
import numpy as np
import Queue
import sys,time, operator,random
edges = []

def generateGraphFromFile(filename, numNodes):
    G = nx.Graph()
    G.add_nodes_from(range(numNodes))
    with open(filename, 'r') as ff:
        for line in ff:
            nodePair = line.strip().split(',')
            a = int(nodePair[0])
            b =  int(nodePair[1])
            edges.append((a, b))
            if a != b:
                if(G.has_edge(a, b)):
                    G[a][b]['weight'] += 1
                else:
                    G.add_edge( a, b, weight = 1)
    ff.close()
    return G



if __name__ == "__main__":
    totalNumNodes = int(sys.argv[3])
    g = generateGraphFromFile(sys.argv[1], totalNumNodes)
    opts = nxmetis.types.MetisOptions()
    opts.objtype = nxmetis.enums.MetisObjType.cut
    opts.contig = False
    (objVal, parts) = nxmetis.partition(g, int(sys.argv[2]), options=opts)
    newID = 0
    print (parts)
    mapping={}
    for x in parts:
        for y in x:
            mapping[y] = newID
            newID += 1

    with open(sys.argv[1]+'.mapped','w') as ff:
        for (a,b) in edges:
            ff.write(str(mapping[a])+','+str(mapping[b])+'\n')

    ff.close()
