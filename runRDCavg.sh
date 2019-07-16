#!/bin/bash

javac lpmaker/TestRunAvg_rdc.java
rm ./*.lp
#for topo in 1 2 3
for trafficMode in 0 11 15 17
    do
    for trial in 0 1 2
        do
        java lpmaker.TestRunAvg_rdc "$trafficMode" "$trial"
        for entry in ./*.lp
            do
            echo "$entry"
            gurobi_cl Threads=12 Method=2 Crossover=0 ResultFile=${entry}'.sol' ${entry}
        done
        rm ./*.lp
    done

    for entry in ./*.lp.sol
        do
        echo "$entry"
        line=$(head -n 2 "$entry")
        echo -e "${entry} ${line}" >> "finalAvg_TP${trafficMode}.txt"
    done
    rm ./*.lp.sol
done

