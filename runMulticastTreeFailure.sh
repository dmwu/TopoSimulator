#!/bin/bash
javac lpmaker/graphs/Graph.java
javac lpmaker/TestMulticastTree.java
rm ./*.lp

for trafficMode in 0
    do
    for topo in 0

        do
        java lpmaker.TestMulticastTree "$topo" "$trafficMode" 0 0 0
        for entry in ./*.lp
            do
            echo "$entry"
            gurobi_cl Threads=12 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry
        done
        rm ./*.lp

        for failureMode in 5 6 7
            do
            for failCount in 1
                do
                for trial in 1 2 3
                    do
                    java lpmaker.TestMulticastTree "$topo" "$trafficMode" "$failureMode" "$failCount" "$trial"
                done
                for entry in ./*.lp
                    do
                    echo "$entry"
                    gurobi_cl Threads=10 Method=2 Crossover=0 ResultFile=$entry'.sol' $entry
                done
                rm ./*.lp
            done
        done
    done

    for entry in ./*.lp.sol
        do
        echo "$entry"
        line=$(head -n 1 "$entry")
        echo -e "${entry} ${line}" >> "Multicast_Avg${trafficMode}.txt"
    done
    rm ./*.lp.sol
done
#wait
